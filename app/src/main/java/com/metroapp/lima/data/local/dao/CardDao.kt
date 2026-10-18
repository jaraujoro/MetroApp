package com.metroapp.lima.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.metroapp.lima.data.local.entity.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CardDao {
    @Query("SELECT * FROM cards ORDER BY isDefault DESC, createdAt DESC")
    abstract fun getAllCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE isActive = 1 ORDER BY isDefault DESC, createdAt DESC")
    abstract fun getActiveCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :cardId")
    abstract suspend fun getCardById(cardId: Long): CardEntity?

    @Query("SELECT * FROM cards WHERE id = :cardId")
    abstract fun getCardByIdFlow(cardId: Long): Flow<CardEntity?>

    @Query("SELECT * FROM cards WHERE isDefault = 1 LIMIT 1")
    abstract suspend fun getDefaultCard(): CardEntity?

    @Query("SELECT * FROM cards WHERE isDefault = 1 LIMIT 1")
    abstract fun getDefaultCardFlow(): Flow<CardEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertCard(card: CardEntity): Long

    @Update
    abstract suspend fun updateCard(card: CardEntity)

    @Query("UPDATE cards SET isDefault = 0")
    abstract suspend fun clearDefaultFlags(): Int

    @Query("UPDATE cards SET isDefault = 1 WHERE id = :cardId")
    abstract suspend fun setDefaultFlag(cardId: Long): Int

    @Transaction
    open suspend fun makeDefaultCard(cardId: Long) {
        clearDefaultFlags()
        setDefaultFlag(cardId)
    }

    @Query("UPDATE cards SET cachedBalanceCents = :balanceCents, updatedAt = :updatedAt WHERE id = :cardId")
    abstract suspend fun updateCachedBalance(cardId: Long, balanceCents: Long, updatedAt: Long): Int

    @Query("UPDATE cards SET cachedBalanceCents = :balanceCents, lastVerifiedRealBalanceCents = :realBalanceCents, lastReconciledAt = :reconciledAt, updatedAt = :updatedAt WHERE id = :cardId")
    abstract suspend fun updateReconciledBalance(
        cardId: Long,
        balanceCents: Long,
        realBalanceCents: Long,
        reconciledAt: Long,
        updatedAt: Long
    ): Int

    @Delete
    abstract suspend fun deleteCard(card: CardEntity)
}
