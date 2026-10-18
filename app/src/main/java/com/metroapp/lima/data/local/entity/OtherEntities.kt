package com.metroapp.lima.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.metroapp.lima.domain.model.AuditLog
import com.metroapp.lima.domain.model.AutomationConfig
import com.metroapp.lima.domain.model.CardType
import com.metroapp.lima.domain.model.FareRule
import com.metroapp.lima.domain.model.Holiday
import com.metroapp.lima.domain.model.Reconciliation

@Entity(
    tableName = "holidays",
    indices = [Index(value = ["dateIso"], unique = true)]
)
data class HolidayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateIso: String,
    val name: String,
    val isMovable: Boolean = false,
    val isCustom: Boolean = false
) {
    fun toDomain(): Holiday = Holiday(
        id = id,
        dateIso = dateIso,
        name = name,
        isMovable = isMovable,
        isCustom = isCustom
    )

    companion object {
        fun fromDomain(holiday: Holiday): HolidayEntity = HolidayEntity(
            id = holiday.id,
            dateIso = holiday.dateIso,
            name = holiday.name,
            isMovable = holiday.isMovable,
            isCustom = holiday.isCustom
        )
    }
}

@Entity(
    tableName = "fare_rules",
    indices = [Index(value = ["cardType"], unique = true)]
)
data class FareRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cardType: String,
    val weekdayFareCents: Long,
    val saturdayFareCents: Long,
    val sundayFareCents: Long,
    val holidayFareCents: Long,
    val validityStartMonthDay: String? = null,
    val validityEndMonthDay: String? = null
) {
    fun toDomain(): FareRule = FareRule(
        id = id,
        cardType = cardType,
        weekdayFareCents = weekdayFareCents,
        saturdayFareCents = saturdayFareCents,
        sundayFareCents = sundayFareCents,
        holidayFareCents = holidayFareCents,
        validityStartMonthDay = validityStartMonthDay,
        validityEndMonthDay = validityEndMonthDay
    )

    companion object {
        fun fromDomain(rule: FareRule): FareRuleEntity = FareRuleEntity(
            id = rule.id,
            cardType = rule.cardType,
            weekdayFareCents = rule.weekdayFareCents,
            saturdayFareCents = rule.saturdayFareCents,
            sundayFareCents = rule.sundayFareCents,
            holidayFareCents = rule.holidayFareCents,
            validityStartMonthDay = rule.validityStartMonthDay,
            validityEndMonthDay = rule.validityEndMonthDay
        )
    }
}

@Entity(
    tableName = "audit_logs",
    indices = [Index(value = ["transactionId"]), Index(value = ["timestamp"])]
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionId: Long,
    val action: String,
    val previousStateSummary: String,
    val newStateSummary: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): AuditLog = AuditLog(
        id = id,
        transactionId = transactionId,
        action = action,
        previousStateSummary = previousStateSummary,
        newStateSummary = newStateSummary,
        reason = reason,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(log: AuditLog): AuditLogEntity = AuditLogEntity(
            id = log.id,
            transactionId = log.transactionId,
            action = log.action,
            previousStateSummary = log.previousStateSummary,
            newStateSummary = log.newStateSummary,
            reason = log.reason,
            timestamp = log.timestamp
        )
    }
}

@Entity(
    tableName = "reconciliations",
    indices = [Index(value = ["cardId"]), Index(value = ["timestamp"])]
)
data class ReconciliationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cardId: Long,
    val calculatedBalanceCents: Long,
    val realBalanceCents: Long,
    val differenceCents: Long,
    val adjustmentTransactionId: Long? = null,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): Reconciliation = Reconciliation(
        id = id,
        cardId = cardId,
        calculatedBalanceCents = calculatedBalanceCents,
        realBalanceCents = realBalanceCents,
        differenceCents = differenceCents,
        adjustmentTransactionId = adjustmentTransactionId,
        reason = reason,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(rec: Reconciliation): ReconciliationEntity = ReconciliationEntity(
            id = rec.id,
            cardId = rec.cardId,
            calculatedBalanceCents = rec.calculatedBalanceCents,
            realBalanceCents = rec.realBalanceCents,
            differenceCents = rec.differenceCents,
            adjustmentTransactionId = rec.adjustmentTransactionId,
            reason = rec.reason,
            timestamp = rec.timestamp
        )
    }
}

@Entity(tableName = "automation_config")
data class AutomationConfigEntity(
    @PrimaryKey
    val id: Long = 1,
    val isEnabled: Boolean = false,
    val autoConfirm: Boolean = false,
    val morningTimeSlot: String = "07:30",
    val eveningTimeSlot: String = "18:00",
    val defaultCardId: Long? = null,
    val tripsPerDay: Int = 2
) {
    fun toDomain(): AutomationConfig = AutomationConfig(
        id = id,
        isEnabled = isEnabled,
        autoConfirm = autoConfirm,
        morningTimeSlot = morningTimeSlot,
        eveningTimeSlot = eveningTimeSlot,
        defaultCardId = defaultCardId,
        tripsPerDay = tripsPerDay
    )

    companion object {
        fun fromDomain(config: AutomationConfig): AutomationConfigEntity = AutomationConfigEntity(
            id = config.id,
            isEnabled = config.isEnabled,
            autoConfirm = config.autoConfirm,
            morningTimeSlot = config.morningTimeSlot,
            eveningTimeSlot = config.eveningTimeSlot,
            defaultCardId = config.defaultCardId,
            tripsPerDay = config.tripsPerDay
        )
    }
}
