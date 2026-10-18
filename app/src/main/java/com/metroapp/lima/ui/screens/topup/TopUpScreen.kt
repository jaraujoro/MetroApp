package com.metroapp.lima.ui.screens.topup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metroapp.lima.ui.theme.MetroGreenContainer
import com.metroapp.lima.ui.theme.MetroGreenPrimary
import com.metroapp.lima.ui.theme.MetroOnGreenContainer
import com.metroapp.lima.ui.viewmodel.MetroViewModel
import com.metroapp.lima.util.CurrencyUtils
import kotlinx.coroutines.launch

@Composable
fun TopUpScreen(
    viewModel: MetroViewModel,
    onTopUpSuccess: () -> Unit
) {
    val cards by viewModel.cards.collectAsState()
    val selectedCard by viewModel.selectedCard.collectAsState()

    var showCardSelectionModal by remember { mutableStateOf(false) }
    var amountInput by remember { mutableStateOf("10.00") }
    var selectedMethod by remember { mutableStateOf("Efectivo") }
    var operationNumberInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val currentBalanceCents = selectedCard?.cachedBalanceCents ?: 0L
    val topUpCents = CurrencyUtils.parseSolesToCents(amountInput)
    val resultingBalanceCents = currentBalanceCents + topUpCents

    val quickAmounts = listOf("5.00", "10.00", "20.00", "50.00", "100.00")
    val paymentMethods = listOf("Efectivo", "Yape", "Plin", "Agente", "Banca Móvil")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Recargar Saldo de Tarjeta",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MetroGreenPrimary
            )
            Text(
                text = "Registra tus recargas para mantener tu saldo al día",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            // Dedicated Card Selector (Always asks/allows switching via modal)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCardSelectionModal = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MetroGreenContainer, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AddCard, contentDescription = null, tint = MetroGreenPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Tarjeta a recargar:", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = selectedCard?.let { "${it.name} (${it.cardType.displayName})" } ?: "Seleccionar tarjeta...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Button(
                        onClick = { showCardSelectionModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MetroGreenPrimary),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cambiar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Balance Simulation Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCardSelectionModal = true },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MetroGreenContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tarjeta: ${selectedCard?.name ?: "Mi Tarjeta"}",
                            fontWeight = FontWeight.Bold,
                            color = MetroOnGreenContainer,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Toca para cambiar",
                            fontSize = 11.sp,
                            color = MetroGreenPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Saldo Actual", fontSize = 11.sp, color = Color.DarkGray)
                            Text(
                                text = CurrencyUtils.formatCents(currentBalanceCents),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                        }

                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MetroGreenPrimary)

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Nuevo Saldo", fontSize = 11.sp, color = MetroGreenPrimary, fontWeight = FontWeight.Bold)
                            Text(
                                text = CurrencyUtils.formatCents(resultingBalanceCents),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MetroGreenPrimary
                            )
                        }
                    }
                }
            }

            // Quick Amounts
            Text(text = "Selecciona un importe rápido:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(quickAmounts) { amt ->
                    val isSelected = amountInput == amt
                    FilterChip(
                        selected = isSelected,
                        onClick = { amountInput = amt },
                        label = { Text("S/ $amt", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MetroGreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Custom Amount Input
            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                label = { Text("Importe a recargar (S/)") },
                placeholder = { Text("0.00") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Payment Methods
            Text(text = "Medio de Recarga:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(paymentMethods) { method ->
                    val isSelected = selectedMethod == method
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedMethod = method },
                        label = { Text(method) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MetroGreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            OutlinedTextField(
                value = operationNumberInput,
                onValueChange = { operationNumberInput = it },
                label = { Text("Número de operación (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = notesInput,
                onValueChange = { notesInput = it },
                label = { Text("Observaciones / Notas (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val card = selectedCard
                    if (card == null) {
                        scope.launch { snackbarHostState.showSnackbar("Por favor selecciona una tarjeta") }
                        return@Button
                    }
                    if (topUpCents <= 0) {
                        scope.launch { snackbarHostState.showSnackbar("Ingresa un monto válido mayor a 0") }
                        return@Button
                    }

                    viewModel.registerTopUp(
                        cardId = card.id,
                        amountCents = topUpCents,
                        paymentMethod = selectedMethod,
                        operationNumber = operationNumberInput.ifEmpty { null },
                        notes = notesInput.ifEmpty { null }
                    ) { success, msg ->
                        if (success) {
                            scope.launch {
                                snackbarHostState.showSnackbar("¡Recarga de S/ $amountInput registrada con éxito!")
                            }
                            onTopUpSuccess()
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(msg ?: "Error al procesar recarga")
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MetroGreenPrimary)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirmar Recarga de S/ $amountInput", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    if (showCardSelectionModal) {
        com.metroapp.lima.ui.components.CardSelectionModalDialog(
            cards = cards,
            selectedCardId = selectedCard?.id,
            title = "Seleccionar Tarjeta a Recargar",
            subtitle = "Elige a qué tarjeta deseas abonar la recarga:",
            onCardSelected = { card ->
                viewModel.selectCard(card.id)
                showCardSelectionModal = false
                scope.launch {
                    snackbarHostState.showSnackbar("Tarjeta seleccionada: ${card.name}")
                }
            },
            onDismissRequest = { showCardSelectionModal = false }
        )
    }
}
