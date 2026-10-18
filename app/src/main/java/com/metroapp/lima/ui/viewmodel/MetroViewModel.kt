package com.metroapp.lima.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.metroapp.lima.data.local.MetroDatabase
import com.metroapp.lima.data.repository.MetroRepository
import com.metroapp.lima.data.worker.AutomationScheduler
import com.metroapp.lima.domain.model.AutomationConfig
import com.metroapp.lima.domain.model.Card
import com.metroapp.lima.domain.model.FareRule
import com.metroapp.lima.domain.model.Holiday
import com.metroapp.lima.domain.model.MetroTransaction
import com.metroapp.lima.domain.usecase.DashboardMetrics
import com.metroapp.lima.domain.usecase.ExportUseCase
import com.metroapp.lima.domain.usecase.ReportsData
import com.metroapp.lima.domain.usecase.StatsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime

class MetroViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MetroDatabase.getInstance(application)
    val repository = MetroRepository(database)
    private val statsUseCase = StatsUseCase()
    val exportUseCase = ExportUseCase(application)

    val cards: StateFlow<List<Card>> = repository.getAllCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCardId = MutableStateFlow<Long?>(null)
    val selectedCardId: StateFlow<Long?> = _selectedCardId.asStateFlow()

    val selectedCard: StateFlow<Card?> = combine(cards, _selectedCardId) { cardList, selectedId ->
        if (selectedId != null) {
            cardList.firstOrNull { it.id == selectedId }
        } else {
            cardList.firstOrNull { it.isDefault } ?: cardList.firstOrNull()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactions: StateFlow<List<MetroTransaction>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTransactions: StateFlow<List<MetroTransaction>> = repository.getPendingTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingCount: StateFlow<Int> = repository.getPendingCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val fareRules: StateFlow<List<FareRule>> = repository.getAllFareRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val holidays: StateFlow<List<Holiday>> = repository.getAllHolidays()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val automationConfig: StateFlow<AutomationConfig> = repository.getAutomationConfigFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AutomationConfig())

    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        selectedCard,
        transactions
    ) { card, txList ->
        if (card == null) {
            DashboardMetrics()
        } else {
            val cardTxs = txList.filter { it.cardId == card.id }
            statsUseCase.calculateDashboardMetrics(
                cachedBalanceCents = card.cachedBalanceCents,
                realVerifiedBalanceCents = card.lastVerifiedRealBalanceCents,
                transactions = cardTxs
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    val reportsData: StateFlow<ReportsData> = transactions.map { txList ->
        statsUseCase.calculateReports(txList, null, null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportsData())

    fun selectCard(cardId: Long) {
        _selectedCardId.value = cardId
    }

    fun registerTrip(
        cardId: Long,
        customFareCents: Long? = null,
        allowNegative: Boolean = false,
        notes: String? = null,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.registerTrip(
                cardId = cardId,
                allowNegativeBalance = allowNegative,
                customFareCents = customFareCents,
                notes = notes
            )
            if (result.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Error al registrar viaje")
            }
        }
    }

    fun registerTopUp(
        cardId: Long,
        amountCents: Long,
        paymentMethod: String?,
        operationNumber: String?,
        notes: String?,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.registerTopUp(
                cardId = cardId,
                amountCents = amountCents,
                paymentMethod = paymentMethod,
                operationNumber = operationNumber,
                notes = notes
            )
            if (result.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Error al recargar")
            }
        }
    }

    fun confirmPendingTrip(txId: Long, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.confirmPendingTransaction(txId)
            if (result.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Error al confirmar viaje")
            }
        }
    }

    fun cancelTransaction(txId: Long, reason: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.cancelTransaction(txId, reason)
            if (result.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Error al anular movimiento")
            }
        }
    }

    fun updateTransaction(
        txId: Long,
        newAmountCents: Long,
        newTimestamp: Long,
        notes: String?,
        reason: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.updateTransaction(txId, newAmountCents, newTimestamp, notes, reason)
            if (result.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Error al actualizar movimiento")
            }
        }
    }

    fun deleteTransaction(txId: Long, reason: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteTransaction(txId, reason)
            if (result.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Error al eliminar movimiento")
            }
        }
    }

    fun reconcileBalance(
        cardId: Long,
        realBalanceCents: Long,
        reason: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.reconcileBalance(cardId, realBalanceCents, reason)
            if (result.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Error en conciliación")
            }
        }
    }

    fun saveCard(card: Card, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.saveCard(card)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun setDefaultCard(cardId: Long) {
        viewModelScope.launch {
            repository.setDefaultCard(cardId)
        }
    }

    fun updateCard(card: Card, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateCard(card)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    suspend fun getTransactionCountForCard(cardId: Long): Int {
        return repository.getTransactionCountForCard(cardId)
    }

    fun deleteCardWithMigration(
        sourceCardId: Long,
        targetCardId: Long?,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.deleteCardWithMigration(sourceCardId, targetCardId)
            if (result.isSuccess) {
                if (_selectedCardId.value == sourceCardId) {
                    _selectedCardId.value = targetCardId
                }
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Error al eliminar tarjeta")
            }
        }
    }

    fun deleteCard(card: Card) {
        viewModelScope.launch {
            repository.deleteCard(card)
        }
    }

    fun updateFareRule(rule: FareRule) {
        viewModelScope.launch {
            repository.updateFareRule(rule)
        }
    }

    fun saveFareRule(rule: FareRule, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.saveFareRule(rule)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message ?: "Error al guardar regla de tarifa")
            }
        }
    }

    fun deleteFareRule(rule: FareRule, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteFareRule(rule)
            if (result.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Error al eliminar tarifa")
            }
        }
    }

    fun addHoliday(dateIso: String, name: String) {
        viewModelScope.launch {
            repository.addHoliday(Holiday(dateIso = dateIso, name = name, isCustom = true))
        }
    }

    fun deleteHoliday(dateIso: String) {
        viewModelScope.launch {
            repository.deleteHoliday(dateIso)
        }
    }

    fun saveAutomationConfig(config: AutomationConfig) {
        viewModelScope.launch {
            repository.saveAutomationConfig(config)
            AutomationScheduler.scheduleDailyWork(getApplication(), config.isEnabled)
        }
    }

    fun resetAutomationConfig() {
        viewModelScope.launch {
            repository.resetAutomationConfig()
            AutomationScheduler.scheduleDailyWork(getApplication(), false)
        }
    }

    fun exportCsv(): File {
        val cardsMap = cards.value.associateBy { it.id }
        return exportUseCase.exportToCsv(transactions.value, cardsMap)
    }

    fun exportPdf(): File {
        val cardsMap = cards.value.associateBy { it.id }
        val r = reportsData.value
        return exportUseCase.exportToPdf(transactions.value, cardsMap, r.totalSpentCents, r.totalTopUpCents)
    }
}

private fun <T, R> StateFlow<T>.map(transform: (T) -> R): kotlinx.coroutines.flow.Flow<R> {
    val source = this
    return kotlinx.coroutines.flow.flow {
        source.collect { emit(transform(it)) }
    }
}
