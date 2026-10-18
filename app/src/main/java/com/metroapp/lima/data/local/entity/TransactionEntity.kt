package com.metroapp.lima.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.metroapp.lima.domain.model.MetroTransaction
import com.metroapp.lima.domain.model.MovementOrigin
import com.metroapp.lima.domain.model.MovementType
import com.metroapp.lima.domain.model.TransactionStatus

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CardEntity::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["cardId"]),
        Index(value = ["timestamp"]),
        Index(value = ["status"]),
        Index(value = ["cardId", "scheduledSlot"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cardId: Long,
    val type: String,
    val amountCents: Long,
    val resultingBalanceCents: Long = 0,
    val fareAppliedCents: Long? = null,
    val fareExplanation: String? = null,
    val origin: String = "MANUAL",
    val status: String = "CONFIRMED",
    val timestamp: Long = System.currentTimeMillis(),
    val scheduledSlot: String? = null,
    val paymentMethod: String? = null,
    val operationNumber: String? = null,
    val description: String,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): MetroTransaction = MetroTransaction(
        id = id,
        cardId = cardId,
        type = MovementType.fromString(type),
        amountCents = amountCents,
        resultingBalanceCents = resultingBalanceCents,
        fareAppliedCents = fareAppliedCents,
        fareExplanation = fareExplanation,
        origin = MovementOrigin.fromString(origin),
        status = TransactionStatus.fromString(status),
        timestamp = timestamp,
        scheduledSlot = scheduledSlot,
        paymentMethod = paymentMethod,
        operationNumber = operationNumber,
        description = description,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(tx: MetroTransaction): TransactionEntity = TransactionEntity(
            id = tx.id,
            cardId = tx.cardId,
            type = tx.type.name,
            amountCents = tx.amountCents,
            resultingBalanceCents = tx.resultingBalanceCents,
            fareAppliedCents = tx.fareAppliedCents,
            fareExplanation = tx.fareExplanation,
            origin = tx.origin.name,
            status = tx.status.name,
            timestamp = tx.timestamp,
            scheduledSlot = tx.scheduledSlot,
            paymentMethod = tx.paymentMethod,
            operationNumber = tx.operationNumber,
            description = tx.description,
            notes = tx.notes,
            createdAt = tx.createdAt,
            updatedAt = tx.updatedAt
        )
    }
}
