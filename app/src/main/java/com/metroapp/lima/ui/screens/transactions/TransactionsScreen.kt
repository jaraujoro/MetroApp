package com.metroapp.lima.ui.screens.transactions

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotInterested
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.metroapp.lima.domain.model.MetroTransaction
import com.metroapp.lima.domain.model.MovementType
import com.metroapp.lima.domain.model.TransactionStatus
import com.metroapp.lima.ui.theme.MetroGreenContainer
import com.metroapp.lima.ui.theme.MetroGreenPrimary
import com.metroapp.lima.ui.theme.MetroRed
import com.metroapp.lima.ui.theme.MetroRedContainer
import com.metroapp.lima.ui.theme.MetroYellowContainer
import com.metroapp.lima.ui.viewmodel.MetroViewModel
import com.metroapp.lima.util.CurrencyUtils
import com.metroapp.lima.util.DateUtils
import kotlinx.coroutines.launch

@Composable
fun TransactionsScreen(
    viewModel: MetroViewModel
) {
    val transactions by viewModel.transactions.collectAsState()
    val cards by viewModel.cards.collectAsState()
    val cardsMap = remember(cards) { cards.associateBy { it.id } }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedTypeFilter by remember { mutableStateOf<String?>("TODOS") }
    var selectedTxForDetail by remember { mutableStateOf<MetroTransaction?>(null) }
    var showEditDialog by remember { mutableStateOf<MetroTransaction?>(null) }
    var showCancelDialog by remember { mutableStateOf<MetroTransaction?>(null) }
    var showDeleteDialog by remember { mutableStateOf<MetroTransaction?>(null) }

    val filteredTransactions = remember(transactions, selectedTypeFilter) {
        when (selectedTypeFilter) {
            "VIAJES" -> transactions.filter { it.type == MovementType.TRIP }
            "RECARGAS" -> transactions.filter { it.type == MovementType.TOP_UP }
            "AJUSTES" -> transactions.filter { it.type == MovementType.POSITIVE_ADJUSTMENT || it.type == MovementType.NEGATIVE_ADJUSTMENT }
            "PENDIENTES" -> transactions.filter { it.status == TransactionStatus.PENDING }
            "ANULADOS" -> transactions.filter { it.status == TransactionStatus.CANCELLED }
            else -> transactions
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Historial de Movimientos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MetroGreenPrimary
            )
            Text(
                text = "${filteredTransactions.size} registros encontrados",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            val filters = listOf("TODOS", "VIAJES", "RECARGAS", "AJUSTES", "PENDIENTES", "ANULADOS")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filters) { filter ->
                    val isSelected = selectedTypeFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTypeFilter = filter },
                        label = { Text(filter, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MetroGreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron movimientos con el filtro seleccionado.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTransactions, key = { it.id }) { tx ->
                        val cardName = cardsMap[tx.cardId]?.name ?: "Tarjeta #${tx.cardId}"
                        TransactionFullItem(
                            tx = tx,
                            cardName = cardName,
                            onClick = { selectedTxForDetail = tx }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }
        }
    }

    // Detail Dialog
    selectedTxForDetail?.let { tx ->
        val cardName = cardsMap[tx.cardId]?.name ?: "Tarjeta #${tx.cardId}"
        AlertDialog(
            onDismissRequest = { selectedTxForDetail = null },
            title = { Text(tx.description, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Tarjeta: $cardName", fontWeight = FontWeight.Medium)
                    Text("Tipo: ${tx.type.displayName}")
                    Text("Monto: ${CurrencyUtils.formatCents(tx.amountCents)}", fontWeight = FontWeight.Bold)
                    Text("Saldo Resultante: ${CurrencyUtils.formatCents(tx.resultingBalanceCents)}")
                    Text("Fecha y Hora: ${DateUtils.formatDateTime(tx.timestamp)}")
                    Text("Origen: ${tx.origin.displayName}")
                    Text("Estado: ${tx.status.displayName}", color = if (tx.status == TransactionStatus.CONFIRMED) MetroGreenPrimary else if (tx.status == TransactionStatus.PENDING) Color(0xFFB28704) else MetroRed)

                    if (!tx.fareExplanation.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Regla de tarifa: ${tx.fareExplanation}", fontSize = 12.sp, color = Color.DarkGray)
                    }
                    if (!tx.paymentMethod.isNullOrBlank()) {
                        Text("Medio de recarga: ${tx.paymentMethod}", fontSize = 12.sp)
                    }
                    if (!tx.operationNumber.isNullOrBlank()) {
                        Text("Nº Operación: ${tx.operationNumber}", fontSize = 12.sp)
                    }
                    if (!tx.notes.isNullOrBlank()) {
                        Text("Notas: ${tx.notes}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (tx.status == TransactionStatus.PENDING) {
                        Button(
                            onClick = {
                                viewModel.confirmPendingTrip(tx.id) { success, msg ->
                                    selectedTxForDetail = null
                                    scope.launch {
                                        snackbarHostState.showSnackbar(if (success) "Viaje confirmado" else (msg ?: "Error"))
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MetroGreenPrimary)
                        ) {
                            Text("Confirmar")
                        }
                    } else if (tx.status == TransactionStatus.CONFIRMED) {
                        OutlinedButton(
                            onClick = {
                                selectedTxForDetail = null
                                showCancelDialog = tx
                            }
                        ) {
                            Text("Anular", color = MetroRed)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            selectedTxForDetail = null
                            showEditDialog = tx
                        }
                    ) {
                        Text("Editar")
                    }

                    TextButton(onClick = { selectedTxForDetail = null }) {
                        Text("Cerrar")
                    }
                }
            }
        )
    }

    // Cancel Transaction Dialog (Anular sin borrar)
    showCancelDialog?.let { tx ->
        var cancelReason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCancelDialog = null },
            title = { Text("Anular Movimiento", color = MetroRed) },
            text = {
                Column {
                    Text("¿Estás seguro de anular este movimiento de ${CurrencyUtils.formatCents(tx.amountCents)}? Se revertirá su impacto en el saldo.")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Motivo de anulación (obligatorio)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cancelReason.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("El motivo es obligatorio") }
                            return@Button
                        }
                        viewModel.cancelTransaction(tx.id, cancelReason) { success, msg ->
                            showCancelDialog = null
                            scope.launch {
                                snackbarHostState.showSnackbar(if (success) "Movimiento anulado y saldo recalculado" else (msg ?: "Error"))
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MetroRed)
                ) {
                    Text("Confirmar Anulación")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Edit Transaction Dialog
    showEditDialog?.let { tx ->
        var newAmountInput by remember { mutableStateOf((tx.amountCents / 100.0).toString()) }
        var newNotesInput by remember { mutableStateOf(tx.notes ?: "") }
        var editReason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = { Text("Editar Movimiento") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newAmountInput,
                        onValueChange = { newAmountInput = it },
                        label = { Text("Importe (S/)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newNotesInput,
                        onValueChange = { newNotesInput = it },
                        label = { Text("Notas / Observaciones") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editReason,
                        onValueChange = { editReason = it },
                        label = { Text("Motivo de corrección (para auditoría)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cents = CurrencyUtils.parseSolesToCents(newAmountInput)
                        if (editReason.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Indica el motivo de corrección") }
                            return@Button
                        }
                        viewModel.updateTransaction(
                            txId = tx.id,
                            newAmountCents = cents,
                            newTimestamp = tx.timestamp,
                            notes = newNotesInput.ifEmpty { null },
                            reason = editReason
                        ) { success, msg ->
                            showEditDialog = null
                            scope.launch {
                                snackbarHostState.showSnackbar(if (success) "Movimiento actualizado y saldo recalculado" else (msg ?: "Error"))
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MetroGreenPrimary)
                ) {
                    Text("Guardar Cambios")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun TransactionFullItem(
    tx: MetroTransaction,
    cardName: String,
    onClick: () -> Unit
) {
    val isDeduction = tx.type.isDeduction
    val icon = if (isDeduction) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward
    val statusColor = when (tx.status) {
        TransactionStatus.CONFIRMED -> if (isDeduction) MetroRed else MetroGreenPrimary
        TransactionStatus.PENDING -> Color(0xFFB28704)
        TransactionStatus.CANCELLED -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = tx.description, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = cardName, fontSize = 12.sp, color = MetroGreenPrimary, fontWeight = FontWeight.Medium)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    val sign = if (isDeduction) "- " else "+ "
                    Text(
                        text = sign + CurrencyUtils.formatCents(tx.amountCents),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = statusColor
                    )
                    Text(
                        text = "Saldo: ${CurrencyUtils.formatCents(tx.resultingBalanceCents)}",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${DateUtils.formatDateTime(tx.timestamp)} • ${tx.origin.displayName}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                // Status Badge
                Box(
                    modifier = Modifier
                        .background(
                            when (tx.status) {
                                TransactionStatus.CONFIRMED -> MetroGreenContainer
                                TransactionStatus.PENDING -> MetroYellowContainer
                                TransactionStatus.CANCELLED -> Color(0xFFEEEEEE)
                            },
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tx.status.displayName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (tx.status) {
                            TransactionStatus.CONFIRMED -> MetroGreenPrimary
                            TransactionStatus.PENDING -> Color(0xFF7F6000)
                            TransactionStatus.CANCELLED -> Color.DarkGray
                        }
                    )
                }
            }
        }
    }
}
