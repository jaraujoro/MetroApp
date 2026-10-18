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
import com.metroapp.lima.ui.theme.MetroGreenDark
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
    var showEditAutomationDialog by remember { mutableStateOf(false) }
    var showDeleteAutomationDialog by remember { mutableStateOf(false) }

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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = MetroGreenPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = automationConfig.title.ifBlank { "Automatización de Viajes Diarios" },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = if (automationConfig.isEnabled) "Estado: Activo" else "Estado: Desactivado",
                                        fontSize = 11.sp,
                                        color = if (automationConfig.isEnabled) MetroGreenPrimary else Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { showEditAutomationDialog = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar automatización", tint = MetroGreenPrimary, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { showDeleteAutomationDialog = true }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar / Reiniciar", tint = Color.Red, modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Switch 1: Habilitar automatización
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Habilitar automatización", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                Text("Ejecuta los viajes automáticos programados", fontSize = 11.sp, color = Color.Gray)
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

                        // Switch 2: Autoconfirmar viajes
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

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFEEEEEE))

                        // Resumen de parámetros configurados
                        val targetCardName = remember(cards, automationConfig.defaultCardId) {
                            if (automationConfig.defaultCardId != null) {
                                cards.firstOrNull { it.id == automationConfig.defaultCardId }?.let { "${it.name} (${it.cardTypeDisplayName})" } ?: "Tarjeta predeterminada"
                            } else {
                                cards.firstOrNull { it.isDefault }?.let { "${it.name} (${it.cardTypeDisplayName}) (Predeterminada)" } ?: "Tarjeta predeterminada"
                            }
                        }

                        val daysLabels = mapOf(
                            "1" to "Lun", "2" to "Mar", "3" to "Mié", "4" to "Jue", "5" to "Vie", "6" to "Sáb", "7" to "Dom"
                        )
                        val activeDaysSummary = remember(automationConfig.activeDays) {
                            val activeList = automationConfig.activeDays.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            if (activeList.size == 7) "Todos los días (Lun - Dom)"
                            else if (activeList == listOf("1", "2", "3", "4", "5")) "Lunes a Viernes"
                            else if (activeList.isEmpty()) "Ningún día de la semana"
                            else activeList.mapNotNull { daysLabels[it] }.joinToString(", ")
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tarjeta asignada:", fontSize = 12.sp, color = Color.Gray)
                                Text(targetCardName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MetroGreenDark)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Viajes por día:", fontSize = 12.sp, color = Color.Gray)
                                Text("${automationConfig.tripsPerDay} viaje(s) diario(s)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Horarios configurados:", fontSize = 12.sp, color = Color.Gray)
                                val slotText = if (automationConfig.tripsPerDay == 1) {
                                    automationConfig.morningTimeSlot
                                } else {
                                    "${automationConfig.morningTimeSlot} (Mañana) • ${automationConfig.eveningTimeSlot} (Tarde)"
                                }
                                Text(slotText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Días activos:", fontSize = 12.sp, color = Color.Gray)
                                Text(activeDaysSummary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            if (automationConfig.customDates.isNotBlank()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Fechas específicas:", fontSize = 12.sp, color = Color.Gray)
                                    Text(automationConfig.customDates, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MetroGreenPrimary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showEditAutomationDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MetroGreenPrimary)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Configurar Parámetros y Días", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
        var selectedTargetCardId by remember(card.id) { mutableStateOf(otherCards.firstOrNull()?.id) }
        var deleteDirectlyWithoutMigration by remember(card.id) { mutableStateOf(false) }

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
                                    text = "Puedes trasladar todo su historial a otra tarjeta para no perder tus reportes, o eliminarla directamente.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF664D03)
                                )
                            }
                        }

                        if (otherCards.isNotEmpty()) {
                            Text("Selecciona la tarjeta destino para el traslado:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            otherCards.forEach { target ->
                                val isChosen = target.id == selectedTargetCardId && !deleteDirectlyWithoutMigration
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedTargetCardId = target.id
                                            deleteDirectlyWithoutMigration = false
                                        },
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

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { deleteDirectlyWithoutMigration = !deleteDirectlyWithoutMigration },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.Checkbox(
                                    checked = deleteDirectlyWithoutMigration,
                                    onCheckedChange = { deleteDirectlyWithoutMigration = it }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Eliminar sin trasladar historial (borra sus movimientos)",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                            }
                        } else {
                            Text(
                                text = "¿Deseas eliminar definitivamente esta tarjeta y todos sus movimientos?",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }
                    } else {
                        Text("Esta tarjeta no tiene movimientos registrados. ¿Estás seguro de que deseas eliminarla?")
                    }
                }
            },
            confirmButton = {
                val canProceed = txCount == 0 || otherCards.isEmpty() || deleteDirectlyWithoutMigration || (otherCards.isNotEmpty() && selectedTargetCardId != null)
                Button(
                    onClick = {
                        val destinationId = if (deleteDirectlyWithoutMigration || otherCards.isEmpty()) null else selectedTargetCardId
                        val targetName = otherCards.firstOrNull { it.id == destinationId }?.name ?: ""
                        viewModel.deleteCardWithMigration(card.id, destinationId) { success, msg ->
                            showDeleteCardDialog = null
                            scope.launch {
                                if (success) {
                                    val message = if (destinationId != null && txCount > 0) {
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
                    val label = if (txCount > 0 && !deleteDirectlyWithoutMigration && otherCards.isNotEmpty()) {
                        "Trasladar y Eliminar"
                    } else {
                        "Eliminar"
                    }
                    Text(label, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCardDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal: Edit Automation Config (Full CRUD)
    if (showEditAutomationDialog) {
        var titleInput by remember { mutableStateOf(automationConfig.title) }
        var morningTimeInput by remember { mutableStateOf(automationConfig.morningTimeSlot) }
        var eveningTimeInput by remember { mutableStateOf(automationConfig.eveningTimeSlot) }
        var selectedCardId by remember { mutableStateOf(automationConfig.defaultCardId) }
        var tripsPerDayInput by remember { mutableStateOf(automationConfig.tripsPerDay) }
        var activeDaysSet by remember {
            val list = automationConfig.activeDays.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            mutableStateOf(list.toSet())
        }
        var customDatesInput by remember { mutableStateOf(automationConfig.customDates) }

        val dayOptions = listOf(
            "1" to "Lun",
            "2" to "Mar",
            "3" to "Mié",
            "4" to "Jue",
            "5" to "Vie",
            "6" to "Sáb",
            "7" to "Dom"
        )

        AlertDialog(
            onDismissRequest = { showEditAutomationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = MetroGreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Configurar Automatización")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Título
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Título de la Automatización") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // 2. Tarjeta a cargar
                    Text("Tarjeta a la que se aplicará el viaje:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Opción: Usar tarjeta predeterminada
                        FilterChip(
                            selected = selectedCardId == null,
                            onClick = { selectedCardId = null },
                            label = { Text("Predeterminada", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MetroGreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                        cards.forEach { c ->
                            FilterChip(
                                selected = selectedCardId == c.id,
                                onClick = { selectedCardId = c.id },
                                label = { Text("${c.name} (${c.cardTypeDisplayName})", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MetroGreenPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // 3. Cantidad de viajes por día
                    Text("Cantidad de viajes por día:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1 to "1 viaje al día", 2 to "2 viajes al día").forEach { (count, label) ->
                            FilterChip(
                                selected = tripsPerDayInput == count,
                                onClick = { tripsPerDayInput = count },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MetroGreenPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        // Opción personalizada para 3, 4, 5 o más viajes
                        val isCustomSelected = tripsPerDayInput > 2
                        FilterChip(
                            selected = isCustomSelected,
                            onClick = {
                                if (!isCustomSelected) tripsPerDayInput = 3
                            },
                            label = { Text("Personalizado", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MetroGreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    if (tripsPerDayInput > 2) {
                        OutlinedTextField(
                            value = if (tripsPerDayInput > 0) tripsPerDayInput.toString() else "",
                            onValueChange = { newVal ->
                                val filtered = newVal.filter { it.isDigit() }
                                tripsPerDayInput = filtered.toIntOrNull()?.coerceIn(1, 20) ?: 1
                            },
                            label = { Text("Número de viajes por día (ej. 3, 4, 5)") },
                            placeholder = { Text("3") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            )
                        )
                    }

                    // 4. Horarios
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = morningTimeInput,
                            onValueChange = { morningTimeInput = it },
                            label = {
                                Text(
                                    when (tripsPerDayInput) {
                                        1 -> "Hora del viaje"
                                        2 -> "Hora Mañana"
                                        else -> "Primer Horario"
                                    }
                                )
                            },
                            placeholder = { Text("07:30") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        if (tripsPerDayInput >= 2) {
                            OutlinedTextField(
                                value = eveningTimeInput,
                                onValueChange = { eveningTimeInput = it },
                                label = {
                                    Text(
                                        if (tripsPerDayInput == 2) "Hora Tarde" else "Segundo Horario"
                                    )
                                },
                                placeholder = { Text("18:00") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }

                    // 5. Calendario: Días de la semana activos
                    Text("Días de la semana para aplicar viajes:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        dayOptions.forEach { (key, label) ->
                            val isSelected = activeDaysSet.contains(key)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    activeDaysSet = if (isSelected) activeDaysSet - key else activeDaysSet + key
                                },
                                label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MetroGreenPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Atajos rápidos para días
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(
                            onClick = { activeDaysSet = setOf("1", "2", "3", "4", "5") },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Lun a Vie", fontSize = 10.sp)
                        }
                        TextButton(
                            onClick = { activeDaysSet = setOf("1", "2", "3", "4", "5", "6", "7") },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Todos", fontSize = 10.sp)
                        }
                        TextButton(
                            onClick = { activeDaysSet = emptySet() },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Ninguno", fontSize = 10.sp)
                        }
                    }

                    // 6. Fechas específicas (Calendario por fechas opcional)
                    OutlinedTextField(
                        value = customDatesInput,
                        onValueChange = { customDatesInput = it },
                        label = { Text("Fechas específicas (YYYY-MM-DD)") },
                        placeholder = { Text("2026-10-20, 2026-10-21") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Opcional: Si escribes fechas específicas separadas por comas, los viajes automáticos se generarán exclusivamente en esas fechas.",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = automationConfig.copy(
                            title = titleInput.trim().ifBlank { "Automatización de Viajes Diarios" },
                            morningTimeSlot = morningTimeInput.trim().ifBlank { "07:30" },
                            eveningTimeSlot = eveningTimeInput.trim().ifBlank { "18:00" },
                            defaultCardId = selectedCardId,
                            tripsPerDay = tripsPerDayInput,
                            activeDays = activeDaysSet.sorted().joinToString(","),
                            customDates = customDatesInput.trim()
                        )
                        viewModel.saveAutomationConfig(updated)
                        showEditAutomationDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Automatización guardada exitosamente")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MetroGreenPrimary)
                ) {
                    Text("Guardar Cambios")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditAutomationDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal: Delete / Reset Automation Config
    if (showDeleteAutomationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAutomationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar / Reiniciar Automatización", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column {
                    Text(
                        "¿Deseas eliminar la configuración actual de automatización de viajes diarios y restablecerla a los valores de fábrica?",
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Esto desactivará los viajes automáticos, cancelará las tareas de WorkManager y restablecerá los horarios y días por defecto.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAutomationConfig()
                        showDeleteAutomationDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Automatización restablecida a valores por defecto")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar / Restablecer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAutomationDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
