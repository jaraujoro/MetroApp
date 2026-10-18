package com.metroapp.lima.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
    var showEditCardDialog by remember { mutableStateOf<Card?>(null) }
    var showDeleteCardDialog by remember { mutableStateOf<Pair<Card, Int>?>(null) }
    var showAddHolidayDialog by remember { mutableStateOf(false) }
    var showEditFareDialog by remember { mutableStateOf<FareRule?>(null) }
    var showAddFareDialog by remember { mutableStateOf(false) }
    var showDeleteFareDialog by remember { mutableStateOf<FareRule?>(null) }

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
                                        text = "${card.cardTypeDisplayName} • Saldo: ${CurrencyUtils.formatCents(card.cachedBalanceCents)}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!card.isDefault) {
                                        TextButton(
                                            onClick = { viewModel.setDefaultCard(card.id) },
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Predeterminada", fontSize = 11.sp)
                                        }
                                    }

                                    // Edit Card Button
                                    IconButton(
                                        onClick = { showEditCardDialog = card },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Editar datos de tarjeta",
                                            tint = MetroGreenPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Delete Card Button (with history migration)
                                    if (cards.size > 1) {
                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    val count = viewModel.getTransactionCountForCard(card.id)
                                                    showDeleteCardDialog = card to count
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Eliminar tarjeta y trasladar historial",
                                                tint = MetroRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PriceChange, contentDescription = null, tint = MetroGreenPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tarifario de Referencia (${fareRules.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            TextButton(onClick = { showAddFareDialog = true }) {
                                Text("+ Añadir", color = MetroGreenPrimary, fontSize = 12.sp)
                            }
                        }
                        Text(
                            text = "Valores editables. Añade, modifica o elimina tarifas según tus necesidades.",
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
                                    Text(rule.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        "L-V: ${CurrencyUtils.formatCents(rule.weekdayFareCents)} | Sáb: ${CurrencyUtils.formatCents(rule.saturdayFareCents)} | Dom: ${CurrencyUtils.formatCents(rule.sundayFareCents)} | Fer: ${CurrencyUtils.formatCents(rule.holidayFareCents)}",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                    if (!rule.validityStartMonthDay.isNullOrBlank() && !rule.validityEndMonthDay.isNullOrBlank()) {
                                        Text(
                                            "Vigencia: ${rule.validityStartMonthDay} al ${rule.validityEndMonthDay}",
                                            fontSize = 10.sp,
                                            color = MetroGreenPrimary
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { showEditFareDialog = rule },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MetroGreenPrimary, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { showDeleteFareDialog = rule },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
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
        val availableTypes = remember(fareRules) {
            if (fareRules.isNotEmpty()) {
                fareRules.map { it.cardType to it.displayName }
            } else {
                listOf(
                    "ADULT" to "Adulto",
                    "SCHOOL" to "Escolar",
                    "UNIVERSITY" to "Universitario / Instituto"
                )
            }
        }
        var selectedCardType by remember {
            mutableStateOf(availableTypes.firstOrNull()?.first ?: "ADULT")
        }
        var initialBalanceInput by remember { mutableStateOf("0.00") }
        var cardNumInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCardDialog = false },
            title = { Text("Registrar Nueva Tarjeta") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = cardNameInput,
                        onValueChange = { cardNameInput = it },
                        label = { Text("Nombre (ej. Tarjeta Personal)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Tipo / Tarifa de la Tarjeta:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableTypes.forEach { (typeKey, typeLabel) ->
                            FilterChip(
                                selected = selectedCardType.equals(typeKey, ignoreCase = true),
                                onClick = { selectedCardType = typeKey },
                                label = { Text(typeLabel, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MetroGreenPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
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
        var holidayInput by remember { mutableStateOf((rule.holidayFareCents / 100.0).toString()) }
        var validityStartInput by remember { mutableStateOf(rule.validityStartMonthDay ?: "") }
        var validityEndInput by remember { mutableStateOf(rule.validityEndMonthDay ?: "") }

        AlertDialog(
            onDismissRequest = { showEditFareDialog = null },
            title = { Text("Editar Tarifa: ${rule.displayName}") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                        label = { Text("Domingo (S/)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = holidayInput,
                        onValueChange = { holidayInput = it },
                        label = { Text("Feriados (S/)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = validityStartInput,
                        onValueChange = { validityStartInput = it },
                        label = { Text("Vigencia Inicio (MM-DD, opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = validityEndInput,
                        onValueChange = { validityEndInput = it },
                        label = { Text("Vigencia Fin (MM-DD, opcional)") },
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
                            holidayFareCents = CurrencyUtils.parseSolesToCents(holidayInput),
                            validityStartMonthDay = validityStartInput.ifBlank { null },
                            validityEndMonthDay = validityEndInput.ifBlank { null }
                        )
                        viewModel.updateFareRule(updated)
                        showEditFareDialog = null
                        scope.launch { snackbarHostState.showSnackbar("Tarifa '${rule.displayName}' actualizada") }
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

    // Modal: Add Fare Rule
    if (showAddFareDialog) {
        var cardTypeInput by remember { mutableStateOf("") }
        var weekdayInput by remember { mutableStateOf("1.50") }
        var saturdayInput by remember { mutableStateOf("1.50") }
        var sundayInput by remember { mutableStateOf("1.50") }
        var holidayInput by remember { mutableStateOf("1.50") }
        var validityStartInput by remember { mutableStateOf("") }
        var validityEndInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddFareDialog = false },
            title = { Text("Añadir Nueva Tarifa") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = cardTypeInput,
                        onValueChange = { cardTypeInput = it },
                        label = { Text("Nombre de la Tarifa (ej. Tarifa Preferencial)") },
                        modifier = Modifier.fillMaxWidth()
                    )
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
                        label = { Text("Domingo (S/)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = holidayInput,
                        onValueChange = { holidayInput = it },
                        label = { Text("Feriados (S/)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = validityStartInput,
                        onValueChange = { validityStartInput = it },
                        label = { Text("Vigencia Inicio (MM-DD, opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = validityEndInput,
                        onValueChange = { validityEndInput = it },
                        label = { Text("Vigencia Fin (MM-DD, opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedType = cardTypeInput.trim()
                        if (trimmedType.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Ingresa un nombre para la tarifa") }
                            return@Button
                        }
                        if (fareRules.any { it.cardType.equals(trimmedType, ignoreCase = true) }) {
                            scope.launch { snackbarHostState.showSnackbar("Ya existe una tarifa con ese nombre") }
                            return@Button
                        }
                        val newRule = FareRule(
                            cardType = trimmedType,
                            weekdayFareCents = CurrencyUtils.parseSolesToCents(weekdayInput),
                            saturdayFareCents = CurrencyUtils.parseSolesToCents(saturdayInput),
                            sundayFareCents = CurrencyUtils.parseSolesToCents(sundayInput),
                            holidayFareCents = CurrencyUtils.parseSolesToCents(holidayInput),
                            validityStartMonthDay = validityStartInput.ifBlank { null },
                            validityEndMonthDay = validityEndInput.ifBlank { null }
                        )
                        viewModel.saveFareRule(newRule) { success, msg ->
                            if (success) {
                                showAddFareDialog = false
                                scope.launch { snackbarHostState.showSnackbar("Tarifa añadida exitosamente") }
                            } else {
                                scope.launch { snackbarHostState.showSnackbar(msg ?: "Error al guardar tarifa") }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MetroGreenPrimary)
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFareDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal: Delete Fare Rule Confirmation
    showDeleteFareDialog?.let { rule ->
        AlertDialog(
            onDismissRequest = { showDeleteFareDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar Tarifa", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        "¿Estás seguro de que deseas eliminar la tarifa '${rule.displayName}'?",
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Nota: Si alguna de tus tarjetas tiene asignada esta tarifa, primero deberás cambiarle de tarifa para poder eliminarla.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ruleToDelete = rule
                        viewModel.deleteFareRule(ruleToDelete) { success, msg ->
                            showDeleteFareDialog = null
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (success) "Tarifa eliminada" else (msg ?: "Error al eliminar tarifa")
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteFareDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal: Edit Card Data
    showEditCardDialog?.let { card ->
        var nameInput by remember { mutableStateOf(card.name) }
        var typeInput by remember { mutableStateOf(card.cardType) }
        var numberInput by remember { mutableStateOf(card.externalCardNumber ?: "") }
        var notesInput by remember { mutableStateOf(card.notes ?: "") }
        var isDefaultInput by remember { mutableStateOf(card.isDefault) }

        val availableTypes = remember(fareRules, card.cardType) {
            val list = mutableListOf<Pair<String, String>>()
            fareRules.forEach { list.add(it.cardType to it.displayName) }
            if (list.none { it.first.equals(card.cardType, ignoreCase = true) }) {
                list.add(card.cardType to card.cardTypeDisplayName)
            }
            if (list.isEmpty()) {
                listOf(
                    "ADULT" to "Adulto",
                    "SCHOOL" to "Escolar",
                    "UNIVERSITY" to "Universitario / Instituto"
                )
            } else {
                list
            }
        }

        AlertDialog(
            onDismissRequest = { showEditCardDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MetroGreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editar Datos de Tarjeta")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Nombre de la tarjeta") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Tipo / Tarifa de la Tarjeta:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableTypes.forEach { (typeKey, typeLabel) ->
                            FilterChip(
                                selected = typeInput.equals(typeKey, ignoreCase = true),
                                onClick = { typeInput = typeKey },
                                label = { Text(typeLabel, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MetroGreenPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = numberInput,
                        onValueChange = { numberInput = it },
                        label = { Text("Nº físico de la tarjeta (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Notas u observaciones (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Establecer como predeterminada", fontSize = 13.sp)
                        Switch(
                            checked = isDefaultInput,
                            onCheckedChange = { isDefaultInput = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = MetroGreenPrimary)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("El nombre de la tarjeta no puede estar vacío") }
                            return@Button
                        }
                        val updated = card.copy(
                            name = nameInput.trim(),
                            cardType = typeInput,
                            externalCardNumber = numberInput.trim().ifEmpty { null },
                            notes = notesInput.trim().ifEmpty { null },
                            isDefault = isDefaultInput,
                            updatedAt = System.currentTimeMillis()
                        )
                        viewModel.updateCard(updated) { success, msg ->
                            showEditCardDialog = null
                            scope.launch {
                                snackbarHostState.showSnackbar(if (success) "Tarjeta actualizada exitosamente" else (msg ?: "Error"))
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MetroGreenPrimary)
                ) {
                    Text("Guardar Cambios")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditCardDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal: Delete Card with History Migration
    showDeleteCardDialog?.let { (card, txCount) ->
        val otherCards = cards.filter { it.id != card.id }
        var selectedTargetCardId by remember { mutableStateOf(otherCards.firstOrNull()?.id) }

        AlertDialog(
            onDismissRequest = { showDeleteCardDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MetroRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar Tarjeta: ${card.name}", color = MetroRed, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (txCount > 0) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "⚠️ Esta tarjeta tiene $txCount movimientos registrados (viajes, recargas o ajustes).",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF664D03)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Para no perder tu historial contable ni alterar tus reportes, todos sus movimientos serán trasladados a la tarjeta que elijas a continuación:",
                                    fontSize = 12.sp,
                                    color = Color(0xFF664D03)
                                )
                            }
                        }

                        if (otherCards.isEmpty()) {
                            Text(
                                text = "No tienes otra tarjeta registrada para trasladar los datos. Registra primero otra tarjeta antes de eliminar esta.",
                                color = MetroRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text("Selecciona la tarjeta destino para el traslado:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            otherCards.forEach { target ->
                                val isChosen = target.id == selectedTargetCardId
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedTargetCardId = target.id },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isChosen) MetroGreenContainer else Color(0xFFF5F5F5)
                                    ),
                                    border = if (isChosen) BorderStroke(1.dp, MetroGreenPrimary) else null
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(target.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(target.cardTypeDisplayName, fontSize = 11.sp, color = Color.Gray)
                                        }
                                        Text(
                                            CurrencyUtils.formatCents(target.cachedBalanceCents),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isChosen) MetroGreenPrimary else Color.DarkGray
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text("Esta tarjeta no tiene movimientos registrados. ¿Estás seguro de que deseas eliminarla?")
                    }
                }
            },
            confirmButton = {
                val canProceed = txCount == 0 || (otherCards.isNotEmpty() && selectedTargetCardId != null)
                Button(
                    onClick = {
                        val targetName = otherCards.firstOrNull { it.id == selectedTargetCardId }?.name ?: ""
                        viewModel.deleteCardWithMigration(card.id, selectedTargetCardId) { success, msg ->
                            showDeleteCardDialog = null
                            scope.launch {
                                if (success) {
                                    val message = if (txCount > 0) {
                                        "Tarjeta eliminada. Se trasladaron $txCount movimientos a $targetName"
                                    } else {
                                        "Tarjeta eliminada correctamente"
                                    }
                                    snackbarHostState.showSnackbar(message)
                                } else {
                                    snackbarHostState.showSnackbar(msg ?: "Error al eliminar tarjeta")
                                }
                            }
                        }
                    },
                    enabled = canProceed,
                    colors = ButtonDefaults.buttonColors(containerColor = MetroRed)
                ) {
                    Text(if (txCount > 0) "Trasladar y Eliminar" else "Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCardDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
