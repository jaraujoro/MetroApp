package com.metroapp.lima.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metroapp.lima.domain.model.Card
import com.metroapp.lima.domain.model.CardType
import com.metroapp.lima.domain.model.FareRule
import com.metroapp.lima.domain.model.Holiday
import com.metroapp.lima.ui.theme.MetroGreenContainer
import com.metroapp.lima.ui.theme.MetroGreenPrimary
import com.metroapp.lima.ui.theme.MetroRed
import com.metroapp.lima.ui.viewmodel.MetroViewModel
import com.metroapp.lima.util.CurrencyUtils
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: MetroViewModel
) {
    val cards by viewModel.cards.collectAsState()
    val fareRules by viewModel.fareRules.collectAsState()
    val holidays by viewModel.holidays.collectAsState()
    val automationConfig by viewModel.automationConfig.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAddCardDialog by remember { mutableStateOf(false) }
    var showAddHolidayDialog by remember { mutableStateOf(false) }
    var showEditFareDialog by remember { mutableStateOf<FareRule?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Configuración",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MetroGreenPrimary
                )
                Text(
                    text = "Tarjetas, tarifario, feriados y automatizaciones",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // 1. CARDS MANAGEMENT SECTION
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mis Tarjetas",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Button(
                                onClick = { showAddCardDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MetroGreenPrimary),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Añadir", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        cards.forEach { card ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = card.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        if (card.isDefault) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(Predeterminada)",
                                                fontSize = 11.sp,
                                                color = MetroGreenPrimary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${card.cardType.displayName} • Saldo: ${CurrencyUtils.formatCents(card.cachedBalanceCents)}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }

                                if (!card.isDefault) {
                                    TextButton(onClick = { viewModel.setDefaultCard(card.id) }) {
                                        Text("Predeterminada", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. AUTOMATION WORKMANAGER SECTION
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = MetroGreenPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Automatización de Viajes Diarios",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Registrar 2 viajes automáticos diarios", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                Text("Genera los viajes en la mañana y en la tarde", fontSize = 11.sp, color = Color.Gray)
                            }
                            Switch(
                                checked = automationConfig.isEnabled,
                                onCheckedChange = { checked ->
                                    viewModel.saveAutomationConfig(automationConfig.copy(isEnabled = checked))
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = MetroGreenPrimary)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Autoconfirmar viajes", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                Text(
                                    text = if (automationConfig.autoConfirm) "Se descuenta el saldo automáticamente" else "Se crean como pendientes para tu confirmación",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Switch(
                                checked = automationConfig.autoConfirm,
                                onCheckedChange = { checked ->
                                    viewModel.saveAutomationConfig(automationConfig.copy(autoConfirm = checked))
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = MetroGreenPrimary)
                            )
                        }
                    }
                }
            }

            // 3. FARE RULES SECTION
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tarifario de Referencia",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Valores editables. No alteran transacciones históricas previas.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        fareRules.forEach { rule ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(rule.cardType.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        "L-V: ${CurrencyUtils.formatCents(rule.weekdayFareCents)} | Sáb: ${CurrencyUtils.formatCents(rule.saturdayFareCents)} | Dom/Fer: ${CurrencyUtils.formatCents(rule.sundayFareCents)}",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                }
                                IconButton(onClick = { showEditFareDialog = rule }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // 4. PERUVIAN HOLIDAYS SECTION
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MetroGreenPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Feriados Peruanos (${holidays.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            TextButton(onClick = { showAddHolidayDialog = true }) {
                                Text("+ Añadir", color = MetroGreenPrimary, fontSize = 12.sp)
                            }
                        }

                        Text(
                            text = "Los feriados anulan el medio pasaje aplicando tarifa completa.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        holidays.take(8).forEach { holiday ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "${holiday.dateIso}: ${holiday.name}", fontSize = 12.sp)
                                if (holiday.isCustom) {
                                    IconButton(
                                        onClick = { viewModel.deleteHoliday(holiday.dateIso) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MetroRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. APP ABOUT
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("MetroApp - Línea 1 del Metro de Lima", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Versión 1.0.0 • SQLite/Room 100% Offline-First", fontSize = 11.sp, color = Color.Gray)
                    Text("Almacenamiento local seguro en el dispositivo", fontSize = 11.sp, color = Color.Gray)
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Modal: Add Card
    if (showAddCardDialog) {
        var cardNameInput by remember { mutableStateOf("") }
        var selectedCardType by remember { mutableStateOf(CardType.ADULT) }
        var initialBalanceInput by remember { mutableStateOf("0.00") }
        var cardNumInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCardDialog = false },
            title = { Text("Registrar Nueva Tarjeta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cardNameInput,
                        onValueChange = { cardNameInput = it },
                        label = { Text("Nombre (ej. Tarjeta Personal)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Tipo de Tarjeta:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CardType.entries.forEach { type ->
                            TextButton(
                                onClick = { selectedCardType = type },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = if (selectedCardType == type) MetroGreenPrimary else Color.Gray
                                )
                            ) {
                                Text(type.displayName, fontSize = 11.sp, fontWeight = if (selectedCardType == type) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = initialBalanceInput,
                        onValueChange = { initialBalanceInput = it },
                        label = { Text("Saldo Inicial (S/)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = cardNumInput,
                        onValueChange = { cardNumInput = it },
                        label = { Text("Nº Físico de Tarjeta (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cardNameInput.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Ingresa un nombre para la tarjeta") }
                            return@Button
                        }
                        val cents = CurrencyUtils.parseSolesToCents(initialBalanceInput)
                        val newCard = Card(
                            name = cardNameInput,
                            cardType = selectedCardType,
                            initialBalanceCents = cents,
                            cachedBalanceCents = cents,
                            externalCardNumber = cardNumInput.ifEmpty { null },
                            isDefault = cards.isEmpty()
                        )
                        viewModel.saveCard(newCard) { success, msg ->
                            showAddCardDialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar(if (success) "Tarjeta guardada con éxito" else (msg ?: "Error"))
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MetroGreenPrimary)
                ) {
                    Text("Guardar Tarjeta")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCardDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal: Add Holiday
    if (showAddHolidayDialog) {
        var holidayDateInput by remember { mutableStateOf("2026-11-20") }
        var holidayNameInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddHolidayDialog = false },
            title = { Text("Añadir Feriado Local") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = holidayDateInput,
                        onValueChange = { holidayDateInput = it },
                        label = { Text("Fecha (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = holidayNameInput,
                        onValueChange = { holidayNameInput = it },
                        label = { Text("Nombre del feriado") },
                        placeholder = { Text("ej. Día no laborable especial") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (holidayNameInput.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Ingresa el nombre del feriado") }
                            return@Button
                        }
                        viewModel.addHoliday(holidayDateInput, holidayNameInput)
                        showAddHolidayDialog = false
                        scope.launch { snackbarHostState.showSnackbar("Feriado agregado") }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MetroGreenPrimary)
                ) {
                    Text("Guardar Feriado")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddHolidayDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal: Edit Fare Rule
    showEditFareDialog?.let { rule ->
        var weekdayInput by remember { mutableStateOf((rule.weekdayFareCents / 100.0).toString()) }
        var saturdayInput by remember { mutableStateOf((rule.saturdayFareCents / 100.0).toString()) }
        var sundayInput by remember { mutableStateOf((rule.sundayFareCents / 100.0).toString()) }

        AlertDialog(
            onDismissRequest = { showEditFareDialog = null },
            title = { Text("Editar Tarifas: ${rule.cardType.displayName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weekdayInput,
                        onValueChange = { weekdayInput = it },
                        label = { Text("Lunes a Viernes (S/)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = saturdayInput,
                        onValueChange = { saturdayInput = it },
                        label = { Text("Sábado (S/)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sundayInput,
                        onValueChange = { sundayInput = it },
                        label = { Text("Domingo y Feriados (S/)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = rule.copy(
                            weekdayFareCents = CurrencyUtils.parseSolesToCents(weekdayInput),
                            saturdayFareCents = CurrencyUtils.parseSolesToCents(saturdayInput),
                            sundayFareCents = CurrencyUtils.parseSolesToCents(sundayInput),
                            holidayFareCents = CurrencyUtils.parseSolesToCents(sundayInput)
                        )
                        viewModel.updateFareRule(updated)
                        showEditFareDialog = null
                        scope.launch { snackbarHostState.showSnackbar("Tarifa actualizada") }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MetroGreenPrimary)
                ) {
                    Text("Actualizar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditFareDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
