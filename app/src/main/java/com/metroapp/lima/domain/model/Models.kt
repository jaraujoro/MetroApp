package com.metroapp.lima.domain.model

data class Card(
    val id: Long = 0,
    val name: String,
    val cardType: String,
    val externalCardNumber: String? = null,
    val initialBalanceCents: Long = 0,
    val cachedBalanceCents: Long = 0,
    val lastVerifiedRealBalanceCents: Long? = null,
    val lastReconciledAt: Long? = null,
    val isDefault: Boolean = false,
    val isActive: Boolean = true,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val cardTypeDisplayName: String
        get() = when (cardType.uppercase()) {
            "ADULT" -> "Adulto"
            "SCHOOL" -> "Escolar"
            "UNIVERSITY" -> "Universitario / Instituto"
            else -> cardType
        }
}

data class MetroTransaction(
    val id: Long = 0,
    val cardId: Long,
    val type: MovementType,
    val amountCents: Long,
    val resultingBalanceCents: Long = 0,
    val fareAppliedCents: Long? = null,
    val fareExplanation: String? = null,
    val origin: MovementOrigin = MovementOrigin.MANUAL,
    val status: TransactionStatus = TransactionStatus.CONFIRMED,
    val timestamp: Long = System.currentTimeMillis(),
    val scheduledSlot: String? = null,
    val paymentMethod: String? = null,
    val operationNumber: String? = null,
    val description: String,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class Holiday(
    val id: Long = 0,
    val dateIso: String, // "YYYY-MM-DD"
    val name: String,
    val isMovable: Boolean = false,
    val isCustom: Boolean = false
)

data class FareRule(
    val id: Long = 0,
    val cardType: String,
    val weekdayFareCents: Long,
    val saturdayFareCents: Long,
    val sundayFareCents: Long,
    val holidayFareCents: Long,
    val validityStartMonthDay: String? = null, // "MM-DD", e.g., "02-15"
    val validityEndMonthDay: String? = null   // "MM-DD", e.g., "12-31"
) {
    val displayName: String
        get() = when (cardType.uppercase()) {
            "ADULT" -> "Adulto"
            "SCHOOL" -> "Escolar"
            "UNIVERSITY" -> "Universitario / Instituto"
            else -> cardType
        }
}

data class AuditLog(
    val id: Long = 0,
    val transactionId: Long,
    val action: String, // CREATE, UPDATE, CANCEL, DELETE
    val previousStateSummary: String,
    val newStateSummary: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class Reconciliation(
    val id: Long = 0,
    val cardId: Long,
    val calculatedBalanceCents: Long,
    val realBalanceCents: Long,
    val differenceCents: Long,
    val adjustmentTransactionId: Long? = null,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class AutomationConfig(
    val id: Long = 1,
    val title: String = "Automatización de Viajes Diarios",
    val isEnabled: Boolean = false,
    val autoConfirm: Boolean = false,
    val morningTimeSlot: String = "07:30",
    val eveningTimeSlot: String = "18:00",
    val defaultCardId: Long? = null,
    val tripsPerDay: Int = 2,
    val activeDays: String = "1,2,3,4,5", // Lunes a Viernes por defecto (1=Lunes, ..., 7=Domingo)
    val customDates: String = "" // Fechas específicas separadas por comas "YYYY-MM-DD,YYYY-MM-DD"
)
