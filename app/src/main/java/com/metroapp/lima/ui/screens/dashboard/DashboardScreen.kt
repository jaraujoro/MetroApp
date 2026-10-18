package com.metroapp.lima.ui.screens.dashboard

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metroapp.lima.domain.model.MovementType
import com.metroapp.lima.domain.model.TransactionStatus
import com.metroapp.lima.ui.components.SimpleBarChart
import com.metroapp.lima.ui.theme.MetroGreenContainer
import com.metroapp.lima.ui.theme.MetroGreenDark
import com.metroapp.lima.ui.theme.MetroGreenPrimary
import com.metroapp.lima.ui.theme.MetroOnGreenContainer
import com.metroapp.lima.ui.theme.MetroRed
import com.metroapp.lima.ui.theme.MetroYellowContainer
import com.metroapp.lima.ui.viewmodel.MetroViewModel
import com.metroapp.lima.util.CurrencyUtils
import com.metroapp.lima.util.DateUtils
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    viewModel: MetroViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToTopUp: () -> Unit
) {
    val cards by viewModel.cards.collectAsState()
    val selectedCard by viewModel.selectedCard.collectAsState()
    val metrics by viewModel.dashboardMetrics.collectAsState()
    val pendingTransactions by viewModel.pendingTransactions.collectAsState()
    val recentTransactions by viewModel.transactions.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showTripDialog by remember { mutableStateOf(false) }
    var showReconcileDialog by remember { mutableStateOf(false) }
    var cardDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showTripDialog = true },
                containerColor = MetroGreenPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.DirectionsSubway, contentDescription = "Registrar Viaje")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                // Card Selector & Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Línea 1 del Metro",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MetroGreenPrimary
                        )
                        Text(
                            text = "Control de Saldo y Pasajes",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    Box {
                        OutlinedButton(
                            onClick = { cardDropdownExpanded = true },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = selectedCard?.name ?: "Elegir Tarjeta", fontSize = 13.sp)
                        }

                        DropdownMenu(
                            expanded = cardDropdownExpanded,
                            onDismissRequest = { cardDropdownExpanded = false }
                        ) {
                            cards.forEach { card ->
                                DropdownMenuItem(
                                    text = { Text("${card.name} (${card.cardType.displayName})") },
                                    onClick = {
                                        viewModel.selectCard(card.id)
                                        cardDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Main Balance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MetroGreenDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SALDO CALCULADO",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedCard?.cardType?.displayName ?: "Adulto",
                                color = MetroGreenContainer,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = CurrencyUtils.formatCents(metrics.calculatedBalanceCents),
                            color = Color.White,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Real vs Calculated info
                        if (metrics.realVerifiedBalanceCents != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Último saldo real: ${CurrencyUtils.formatCents(metrics.realVerifiedBalanceCents!!)}",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                                val diff = metrics.balanceDifferenceCents
                                val diffText = if (diff >= 0) "+${CurrencyUtils.formatCents(diff)}" else CurrencyUtils.formatCents(diff)
                                Text(
                                    text = "Dif: $diffText",
                                    color = if (diff == 0L) Color.White.copy(alpha = 0.7f) else Color(0xFFFFB4AB),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Fast actions inside card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showTripDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MetroGreenDark)
                            ) {
                                Text("Registrar Viaje", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Button(
                                onClick = onNavigateToTopUp,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MetroGreenContainer, contentColor = MetroOnGreenContainer)
                            ) {
                                Text("Recargar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = { showReconcileDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = "Conciliar", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Pending Trips Banner (if any)
            if (pendingTransactions.isNotEmpty()) {
                item {
                    val pending = pendingTransactions.first()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MetroYellowContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color(0xFF7F6000),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Viaje programado pendiente",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF382C00)
                                    )
                                    Text(
                                        text = "${CurrencyUtils.formatCents(pending.amountCents)} • ${DateUtils.formatTime(pending.timestamp)}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF5C4900)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.confirmPendingTrip(pending.id) { success, msg ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(if (success) "Viaje confirmado exitosamente" else (msg ?: "Error"))
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F6000))
                            ) {
                                Text("Confirmar", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 4-Indicators Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(
                        title = "Gastado Hoy",
                        value = CurrencyUtils.formatCents(metrics.spentTodayCents),
                        subtitle = "${metrics.tripsCountToday} viajes hoy",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Gastado Semana",
                        value = CurrencyUtils.formatCents(metrics.spentThisWeekCents),
                        subtitle = "Semana en curso",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(
                        title = "Gastado en el Mes",
                        value = CurrencyUtils.formatCents(metrics.spentThisMonthCents),
                        subtitle = "Gasto mensual acumulado",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Recargas del Mes",
                        value = "${metrics.topUpsCountMonth} recargas",
                        subtitle = "Realizadas este mes",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Mini Chart: Last 7 days expenses
            item {
                SimpleBarChart(
                    title = "Gastos en Viajes (Últimos 7 días)",
                    dataPoints = metrics.dailyChartPoints
                )
            }

            // Recent Transactions Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Últimos Movimientos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onNavigateToTransactions) {
                        Text("Ver Todos", color = MetroGreenPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            val recentList = recentTransactions.take(5)
            if (recentList.isEmpty()) {
                item {
                    Text(
                        text = "No hay movimientos registrados. ¡Registra tu primer viaje o recarga!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(recentList) { tx ->
                    TransactionItemRow(tx = tx)
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Modal: Quick Trip Registration
    if (showTripDialog) {
        val currentCard = selectedCard
        var allowNegative by remember { mutableStateOf(false) }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showTripDialog = false },
            title = { Text("Registrar Viaje en Metro") },
            text = {
                Column {
                    Text("Tarjeta: ${currentCard?.name ?: "Predeterminada"}")
                    Text("Tipo: ${currentCard?.cardType?.displayName ?: "Adulto"}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "La tarifa se calcula automáticamente según las reglas de la Línea 1, fecha y feriados.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Estación / Nota (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (currentCard != null) {
                            viewModel.registerTrip(
                                cardId = currentCard.id,
                                allowNegative = allowNegative,
                                notes = notes.ifEmpty { null }
                            ) { success, msg ->
                                showTripDialog = false
                                scope.launch {
                                    snackbarHostState.showSnackbar(if (success) "Viaje registrado correctamente" else (msg ?: "Error"))
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MetroGreenPrimary)
                ) {
                    Text("Confirmar Viaje")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTripDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal: Reconcile Balance
    if (showReconcileDialog) {
        val currentCard = selectedCard
        var realBalanceInput by remember { mutableStateOf("") }
        var reasonInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showReconcileDialog = false },
            title = { Text("Conciliar Saldo Físico") },
            text = {
                Column {
                    Text("Saldo calculado actual: ${CurrencyUtils.formatCents(currentCard?.cachedBalanceCents ?: 0L)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = realBalanceInput,
                        onValueChange = { realBalanceInput = it },
                        label = { Text("Saldo real que ves en la tarjeta (S/)") },
                        placeholder = { Text("ej. 14.50") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reasonInput,
                        onValueChange = { reasonInput = it },
                        label = { Text("Motivo del ajuste (obligatorio)") },
                        placeholder = { Text("ej. Verificado en torniquete estación Angamos") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cents = CurrencyUtils.parseSolesToCents(realBalanceInput)
                        if (reasonInput.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("El motivo es obligatorio para auditar la conciliación") }
                            return@Button
                        }
                        if (currentCard != null) {
                            viewModel.reconcileBalance(currentCard.id, cents, reasonInput) { success, msg ->
                                showReconcileDialog = false
                                scope.launch {
                                    snackbarHostState.showSnackbar(if (success) "Saldo conciliado y ajustado exitosamente" else (msg ?: "Error"))
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MetroGreenPrimary)
                ) {
                    Text("Guardar Conciliación")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReconcileDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun MetricBox(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(text = title, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 11.sp, color = Color.DarkGray)
        }
    }
}

@Composable
fun TransactionItemRow(
    tx: com.metroapp.lima.domain.model.MetroTransaction
) {
    val isDeduction = tx.type.isDeduction
    val icon = if (isDeduction) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward
    val iconColor = if (tx.status == TransactionStatus.CANCELLED) Color.Gray else if (isDeduction) MetroRed else MetroGreenPrimary

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = tx.description, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        text = "${DateUtils.formatDate(tx.timestamp)} ${DateUtils.formatTime(tx.timestamp)} • ${tx.origin.displayName}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val prefix = if (isDeduction) "- " else "+ "
                Text(
                    text = prefix + CurrencyUtils.formatCents(tx.amountCents),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = iconColor
                )
                Text(
                    text = "Saldo: ${CurrencyUtils.formatCents(tx.resultingBalanceCents)}",
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}
