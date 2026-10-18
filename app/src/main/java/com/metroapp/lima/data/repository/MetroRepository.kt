package com.metroapp.lima.data.repository

import androidx.room.withTransaction
import com.metroapp.lima.data.local.MetroDatabase
import com.metroapp.lima.data.local.entity.AuditLogEntity
import com.metroapp.lima.data.local.entity.AutomationConfigEntity
import com.metroapp.lima.data.local.entity.CardEntity
import com.metroapp.lima.data.local.entity.FareRuleEntity
import com.metroapp.lima.data.local.entity.HolidayEntity
import com.metroapp.lima.data.local.entity.ReconciliationEntity
import com.metroapp.lima.data.local.entity.TransactionEntity
import com.metroapp.lima.domain.engine.FareCalculationResult
import com.metroapp.lima.domain.engine.TariffEngine
import com.metroapp.lima.domain.model.AuditLog
import com.metroapp.lima.domain.model.AutomationConfig
import com.metroapp.lima.domain.model.Card
import com.metroapp.lima.domain.model.FareRule
import com.metroapp.lima.domain.model.Holiday
import com.metroapp.lima.domain.model.MetroTransaction
import com.metroapp.lima.domain.model.MovementOrigin
import com.metroapp.lima.domain.model.MovementType
import com.metroapp.lima.domain.model.Reconciliation
import com.metroapp.lima.domain.model.TransactionStatus
import com.metroapp.lima.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class MetroRepository(
    private val database: MetroDatabase
) {
    private val cardDao = database.cardDao()
    private val transactionDao = database.transactionDao()
    private val holidayDao = database.holidayDao()
    private val fareRuleDao = database.fareRuleDao()
    private val auditDao = database.auditLogDao()
    private val reconciliationDao = database.reconciliationDao()
    private val automationDao = database.automationConfigDao()

    // ==========================================
    // CARDS
    // ==========================================
    fun getAllCards(): Flow<List<Card>> = cardDao.getAllCards().map { list -> list.map { it.toDomain() } }

    fun getActiveCards(): Flow<List<Card>> = cardDao.getActiveCards().map { list -> list.map { it.toDomain() } }

    suspend fun getCardById(id: Long): Card? = cardDao.getCardById(id)?.toDomain()

    fun getCardByIdFlow(id: Long): Flow<Card?> = cardDao.getCardByIdFlow(id).map { it?.toDomain() }

    suspend fun getDefaultCard(): Card? = cardDao.getDefaultCard()?.toDomain()

    fun getDefaultCardFlow(): Flow<Card?> = cardDao.getDefaultCardFlow().map { it?.toDomain() }

    suspend fun saveCard(card: Card): Long {
        val entity = CardEntity.fromDomain(card)
        val id = cardDao.insertCard(entity)
        if (card.isDefault) {
            cardDao.makeDefaultCard(if (card.id == 0L) id else card.id)
        }
        return id
    }

    suspend fun updateCard(card: Card) {
        val entity = CardEntity.fromDomain(card)
        cardDao.updateCard(entity)
        if (card.isDefault) {
            cardDao.makeDefaultCard(card.id)
        }
        recalculateCardBalance(card.id)
    }

    suspend fun getTransactionCountForCard(cardId: Long): Int =
        transactionDao.getTransactionCountForCard(cardId)

    suspend fun deleteCardWithMigration(sourceCardId: Long, targetCardId: Long?): Result<Unit> {
        return try {
            database.withTransaction {
                val sourceCard = cardDao.getCardById(sourceCardId) ?: return@withTransaction
                val txCount = transactionDao.getTransactionCountForCard(sourceCardId)

                if (txCount > 0) {
                    if (targetCardId == null || targetCardId == sourceCardId) {
                        throw IllegalArgumentException("Debes seleccionar una tarjeta de destino para migrar los movimientos.")
                    }
                    // Migrate transactions and reconciliations
                    transactionDao.migrateTransactions(sourceCardId, targetCardId)
                    reconciliationDao.migrateReconciliations(sourceCardId, targetCardId)
                    // Recalculate target card balance from its newly consolidated history
                    recalculateCardBalance(targetCardId)
                }

                // If deleted card was default, make targetCard default if available
                if (sourceCard.isDefault && targetCardId != null) {
                    cardDao.makeDefaultCard(targetCardId)
                }

                // If automation config was using this card, reassign or reset to null
                val autoConfig = automationDao.getConfig()
                if (autoConfig != null && autoConfig.defaultCardId == sourceCardId) {
                    automationDao.saveConfig(autoConfig.copy(defaultCardId = targetCardId))
                }

                // Delete source card
                cardDao.deleteCard(sourceCard)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setDefaultCard(cardId: Long) {
        cardDao.makeDefaultCard(cardId)
    }

    suspend fun deleteCard(card: Card) {
        cardDao.deleteCard(CardEntity.fromDomain(card))
    }

    suspend fun recalculateCardBalance(cardId: Long): Long {
        val card = cardDao.getCardById(cardId) ?: return 0L
        val txs = transactionDao.getTransactionsByCardAscending(cardId)

        var runningBalance = card.initialBalanceCents
        for (tx in txs) {
            if (tx.status != TransactionStatus.CONFIRMED.name) continue
            val type = MovementType.fromString(tx.type)
            when (type) {
                MovementType.TRIP, MovementType.NEGATIVE_ADJUSTMENT -> {
                    runningBalance -= tx.amountCents
                }
                MovementType.TOP_UP, MovementType.POSITIVE_ADJUSTMENT -> {
                    runningBalance += tx.amountCents
                }
                MovementType.REVERSAL -> {
                    // Reversals store net signed or specific correction
                }
            }
        }
        cardDao.updateCachedBalance(cardId, runningBalance, System.currentTimeMillis())
        return runningBalance
    }

    // ==========================================
    // TRANSACTIONS
    // ==========================================
    fun getAllTransactions(): Flow<List<MetroTransaction>> =
        transactionDao.getAllTransactions().map { list -> list.map { it.toDomain() } }

    fun getTransactionsByCard(cardId: Long): Flow<List<MetroTransaction>> =
        transactionDao.getTransactionsByCard(cardId).map { list -> list.map { it.toDomain() } }

    fun getRecentTransactions(limit: Int = 10): Flow<List<MetroTransaction>> =
        transactionDao.getRecentTransactions(limit).map { list -> list.map { it.toDomain() } }

    fun getPendingTransactions(): Flow<List<MetroTransaction>> =
        transactionDao.getPendingTransactions().map { list -> list.map { it.toDomain() } }

    fun getPendingCountFlow(): Flow<Int> = transactionDao.getPendingCountFlow()

    suspend fun getTransactionById(id: Long): MetroTransaction? =
        transactionDao.getTransactionById(id)?.toDomain()

    suspend fun registerTrip(
        cardId: Long,
        dateTime: LocalDateTime = LocalDateTime.now(DateUtils.LIMA_ZONE),
        allowNegativeBalance: Boolean = false,
        origin: MovementOrigin = MovementOrigin.MANUAL,
        status: TransactionStatus = TransactionStatus.CONFIRMED,
        scheduledSlot: String? = null,
        customFareCents: Long? = null,
        notes: String? = null
    ): Result<Long> {
        val card = cardDao.getCardById(cardId) ?: return Result.failure(IllegalArgumentException("Tarjeta no encontrada"))

        // Calculate fare
        val fareResult = if (customFareCents != null) {
            FareCalculationResult(customFareCents, "Tarifa personalizada", false)
        } else {
            calculateFare(card.cardType, dateTime)
        }

        val fare = fareResult.fareCents

        if (status == TransactionStatus.CONFIRMED && !allowNegativeBalance && (card.cachedBalanceCents - fare < 0)) {
            return Result.failure(IllegalStateException("Saldo insuficiente: Saldo disponible S/ ${card.cachedBalanceCents / 100.0}, pasaje S/ ${fare / 100.0}"))
        }

        val timestamp = DateUtils.toEpochMillis(dateTime)
        val newBalance = if (status == TransactionStatus.CONFIRMED) card.cachedBalanceCents - fare else card.cachedBalanceCents

        val entity = TransactionEntity(
            cardId = cardId,
            type = MovementType.TRIP.name,
            amountCents = fare,
            resultingBalanceCents = newBalance,
            fareAppliedCents = fare,
            fareExplanation = fareResult.explanation,
            origin = origin.name,
            status = status.name,
            timestamp = timestamp,
            scheduledSlot = scheduledSlot,
            description = "Viaje en Metro (${card.name})",
            notes = notes,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        return try {
            val txId = database.withTransaction {
                val id = transactionDao.insertTransaction(entity)
                if (status == TransactionStatus.CONFIRMED) {
                    cardDao.updateCachedBalance(cardId, newBalance, System.currentTimeMillis())
                }
                id
            }
            Result.success(txId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerTopUp(
        cardId: Long,
        amountCents: Long,
        paymentMethod: String? = null,
        operationNumber: String? = null,
        notes: String? = null,
        dateTime: LocalDateTime = LocalDateTime.now(DateUtils.LIMA_ZONE)
    ): Result<Long> {
        if (amountCents <= 0) return Result.failure(IllegalArgumentException("El importe de la recarga debe ser mayor a 0"))
        val card = cardDao.getCardById(cardId) ?: return Result.failure(IllegalArgumentException("Tarjeta no encontrada"))

        val timestamp = DateUtils.toEpochMillis(dateTime)
        val newBalance = card.cachedBalanceCents + amountCents

        val entity = TransactionEntity(
            cardId = cardId,
            type = MovementType.TOP_UP.name,
            amountCents = amountCents,
            resultingBalanceCents = newBalance,
            fareAppliedCents = null,
            fareExplanation = null,
            origin = MovementOrigin.MANUAL.name,
            status = TransactionStatus.CONFIRMED.name,
            timestamp = timestamp,
            paymentMethod = paymentMethod,
            operationNumber = operationNumber,
            description = "Recarga de saldo",
            notes = notes,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        return try {
            val txId = database.withTransaction {
                val id = transactionDao.insertTransaction(entity)
                cardDao.updateCachedBalance(cardId, newBalance, System.currentTimeMillis())
                id
            }
            Result.success(txId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun confirmPendingTransaction(transactionId: Long, allowNegativeBalance: Boolean = false): Result<Unit> {
        val tx = transactionDao.getTransactionById(transactionId) ?: return Result.failure(IllegalArgumentException("Movimiento no encontrado"))
        if (tx.status != TransactionStatus.PENDING.name) return Result.failure(IllegalStateException("El movimiento no está pendiente"))

        val card = cardDao.getCardById(tx.cardId) ?: return Result.failure(IllegalArgumentException("Tarjeta no encontrada"))
        val newBalance = card.cachedBalanceCents - tx.amountCents

        if (!allowNegativeBalance && newBalance < 0) {
            return Result.failure(IllegalStateException("Saldo insuficiente para confirmar este viaje"))
        }

        return try {
            database.withTransaction {
                val updatedTx = tx.copy(
                    status = TransactionStatus.CONFIRMED.name,
                    resultingBalanceCents = newBalance,
                    updatedAt = System.currentTimeMillis()
                )
                transactionDao.updateTransaction(updatedTx)
                cardDao.updateCachedBalance(tx.cardId, newBalance, System.currentTimeMillis())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelTransaction(transactionId: Long, reason: String): Result<Unit> {
        val tx = transactionDao.getTransactionById(transactionId) ?: return Result.failure(IllegalArgumentException("Movimiento no encontrado"))
        if (tx.status == TransactionStatus.CANCELLED.name) return Result.failure(IllegalStateException("El movimiento ya está anulado"))

        return try {
            database.withTransaction {
                val updatedTx = tx.copy(
                    status = TransactionStatus.CANCELLED.name,
                    updatedAt = System.currentTimeMillis()
                )
                transactionDao.updateTransaction(updatedTx)

                // Audit log
                auditDao.insertAuditLog(
                    AuditLogEntity(
                        transactionId = transactionId,
                        action = "CANCEL",
                        previousStateSummary = "Estado: ${tx.status}, Importe: ${tx.amountCents}",
                        newStateSummary = "Estado: CANCELLED",
                        reason = reason,
                        timestamp = System.currentTimeMillis()
                    )
                )

                // Recompute balance
                recalculateCardBalance(tx.cardId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTransaction(
        transactionId: Long,
        newAmountCents: Long,
        newTimestamp: Long,
        newNotes: String?,
        reason: String
    ): Result<Unit> {
        val tx = transactionDao.getTransactionById(transactionId) ?: return Result.failure(IllegalArgumentException("Movimiento no encontrado"))

        return try {
            database.withTransaction {
                val updatedTx = tx.copy(
                    amountCents = newAmountCents,
                    timestamp = newTimestamp,
                    notes = newNotes,
                    updatedAt = System.currentTimeMillis()
                )
                transactionDao.updateTransaction(updatedTx)

                auditDao.insertAuditLog(
                    AuditLogEntity(
                        transactionId = transactionId,
                        action = "UPDATE",
                        previousStateSummary = "Monto: ${tx.amountCents}, Fecha: ${tx.timestamp}",
                        newStateSummary = "Monto: $newAmountCents, Fecha: $newTimestamp",
                        reason = reason,
                        timestamp = System.currentTimeMillis()
                    )
                )

                recalculateCardBalance(tx.cardId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(transactionId: Long, reason: String): Result<Unit> {
        val tx = transactionDao.getTransactionById(transactionId) ?: return Result.failure(IllegalArgumentException("Movimiento no encontrado"))

        return try {
            database.withTransaction {
                transactionDao.deleteTransaction(tx)
                auditDao.insertAuditLog(
                    AuditLogEntity(
                        transactionId = transactionId,
                        action = "DELETE",
                        previousStateSummary = "Tipo: ${tx.type}, Monto: ${tx.amountCents}",
                        newStateSummary = "ELIMINADO FISICAMENTE",
                        reason = reason,
                        timestamp = System.currentTimeMillis()
                    )
                )
                recalculateCardBalance(tx.cardId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // RECONCILIATION
    // ==========================================
    suspend fun reconcileBalance(
        cardId: Long,
        realBalanceCents: Long,
        reason: String
    ): Result<Reconciliation> {
        val card = cardDao.getCardById(cardId) ?: return Result.failure(IllegalArgumentException("Tarjeta no encontrada"))
        val calculatedBalance = recalculateCardBalance(cardId)
        val difference = realBalanceCents - calculatedBalance

        return try {
            val rec = database.withTransaction {
                var adjTxId: Long? = null
                if (difference != 0L) {
                    val isPositive = difference > 0
                    val adjTx = TransactionEntity(
                        cardId = cardId,
                        type = if (isPositive) MovementType.POSITIVE_ADJUSTMENT.name else MovementType.NEGATIVE_ADJUSTMENT.name,
                        amountCents = kotlin.math.abs(difference),
                        resultingBalanceCents = realBalanceCents,
                        origin = MovementOrigin.MANUAL.name,
                        status = TransactionStatus.CONFIRMED.name,
                        description = "Conciliación de saldo: " + if (isPositive) "Ajuste positivo" else "Ajuste negativo",
                        notes = reason,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    adjTxId = transactionDao.insertTransaction(adjTx)
                }

                cardDao.updateReconciledBalance(
                    cardId = cardId,
                    balanceCents = realBalanceCents,
                    realBalanceCents = realBalanceCents,
                    reconciledAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                val reconciliation = ReconciliationEntity(
                    cardId = cardId,
                    calculatedBalanceCents = calculatedBalance,
                    realBalanceCents = realBalanceCents,
                    differenceCents = difference,
                    adjustmentTransactionId = adjTxId,
                    reason = reason,
                    timestamp = System.currentTimeMillis()
                )
                val id = reconciliationDao.insertReconciliation(reconciliation)
                reconciliation.copy(id = id).toDomain()
            }
            Result.success(rec)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getReconciliationsForCard(cardId: Long): Flow<List<Reconciliation>> =
        reconciliationDao.getReconciliationsForCard(cardId).map { list -> list.map { it.toDomain() } }

    fun getAllReconciliations(): Flow<List<Reconciliation>> =
        reconciliationDao.getAllReconciliations().map { list -> list.map { it.toDomain() } }

    // ==========================================
    // TARIFF ENGINE & HOLIDAYS
    // ==========================================
    suspend fun calculateFare(cardTypeStr: String, dateTime: LocalDateTime): FareCalculationResult {
        val dateIso = DateUtils.formatIsoDate(DateUtils.toEpochMillis(dateTime))
        val holiday = holidayDao.getHolidayByDate(dateIso)?.toDomain()
        val rule = fareRuleDao.getFareRuleByCardType(cardTypeStr)?.toDomain()
        return TariffEngine.calculateFare(
            cardType = cardTypeStr,
            dateTime = dateTime,
            holiday = holiday,
            fareRule = rule
        )
    }

    fun getAllFareRules(): Flow<List<FareRule>> =
        fareRuleDao.getAllFareRules().map { list -> list.map { it.toDomain() } }

    suspend fun saveFareRule(rule: FareRule): Long {
        return fareRuleDao.insertOrUpdate(FareRuleEntity.fromDomain(rule))
    }

    suspend fun updateFareRule(rule: FareRule) {
        fareRuleDao.insertOrUpdate(FareRuleEntity.fromDomain(rule))
    }

    suspend fun deleteFareRule(rule: FareRule): Result<Unit> {
        val cards = cardDao.getAllCardsList()
        val inUse = cards.any { it.cardType.equals(rule.cardType, ignoreCase = true) }
        if (inUse) {
            return Result.failure(IllegalStateException("No se puede eliminar la tarifa '${rule.displayName}' porque está asignada a una o más tarjetas registradas. Cambie la tarifa de dichas tarjetas primero."))
        }
        fareRuleDao.deleteFareRule(FareRuleEntity.fromDomain(rule))
        return Result.success(Unit)
    }

    fun getAllHolidays(): Flow<List<Holiday>> =
        holidayDao.getAllHolidays().map { list -> list.map { it.toDomain() } }

    suspend fun addHoliday(holiday: Holiday): Long =
        holidayDao.insertHoliday(HolidayEntity.fromDomain(holiday))

    suspend fun deleteHoliday(dateIso: String) =
        holidayDao.deleteByDate(dateIso)

    // ==========================================
    // AUTOMATION CONFIG
    // ==========================================
    fun getAutomationConfigFlow(): Flow<AutomationConfig> =
        automationDao.getConfigFlow().map { it?.toDomain() ?: AutomationConfig() }

    suspend fun getAutomationConfig(): AutomationConfig =
        automationDao.getConfig()?.toDomain() ?: AutomationConfig()

    suspend fun saveAutomationConfig(config: AutomationConfig) =
        automationDao.saveConfig(AutomationConfigEntity.fromDomain(config))

    suspend fun resetAutomationConfig() {
        automationDao.deleteConfig()
        automationDao.saveConfig(
            AutomationConfigEntity(
                id = 1,
                title = "Automatización de Viajes Diarios",
                isEnabled = false,
                autoConfirm = false,
                morningTimeSlot = "07:30",
                eveningTimeSlot = "18:00",
                defaultCardId = null,
                tripsPerDay = 2,
                activeDays = "1,2,3,4,5",
                customDates = ""
            )
        )
    }
}
