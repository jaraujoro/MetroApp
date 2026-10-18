package com.metroapp.lima.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.metroapp.lima.domain.model.Card
import com.metroapp.lima.domain.model.CardType

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true)
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
    fun toDomain(): Card = Card(
        id = id,
        name = name,
        cardType = CardType.fromString(cardType),
        externalCardNumber = externalCardNumber,
        initialBalanceCents = initialBalanceCents,
        cachedBalanceCents = cachedBalanceCents,
        lastVerifiedRealBalanceCents = lastVerifiedRealBalanceCents,
        lastReconciledAt = lastReconciledAt,
        isDefault = isDefault,
        isActive = isActive,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(card: Card): CardEntity = CardEntity(
            id = card.id,
            name = card.name,
            cardType = card.cardType.name,
            externalCardNumber = card.externalCardNumber,
            initialBalanceCents = card.initialBalanceCents,
            cachedBalanceCents = card.cachedBalanceCents,
            lastVerifiedRealBalanceCents = card.lastVerifiedRealBalanceCents,
            lastReconciledAt = card.lastReconciledAt,
            isDefault = card.isDefault,
            isActive = card.isActive,
            notes = card.notes,
            createdAt = card.createdAt,
            updatedAt = card.updatedAt
        )
    }
}
