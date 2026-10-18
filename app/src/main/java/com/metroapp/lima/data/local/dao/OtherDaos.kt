package com.metroapp.lima.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.metroapp.lima.data.local.entity.AuditLogEntity
import com.metroapp.lima.data.local.entity.AutomationConfigEntity
import com.metroapp.lima.data.local.entity.FareRuleEntity
import com.metroapp.lima.data.local.entity.HolidayEntity
import com.metroapp.lima.data.local.entity.ReconciliationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holidays ORDER BY dateIso ASC")
    fun getAllHolidays(): Flow<List<HolidayEntity>>

    @Query("SELECT * FROM holidays ORDER BY dateIso ASC")
    suspend fun getAllHolidaysList(): List<HolidayEntity>

    @Query("SELECT * FROM holidays WHERE dateIso = :dateIso LIMIT 1")
    suspend fun getHolidayByDate(dateIso: String): HolidayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoliday(holiday: HolidayEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllHolidays(holidays: List<HolidayEntity>)

    @Update
    suspend fun updateHoliday(holiday: HolidayEntity)

    @Delete
    suspend fun deleteHoliday(holiday: HolidayEntity)

    @Query("DELETE FROM holidays WHERE dateIso = :dateIso")
    suspend fun deleteByDate(dateIso: String): Int
}

@Dao
interface FareRuleDao {
    @Query("SELECT * FROM fare_rules")
    fun getAllFareRules(): Flow<List<FareRuleEntity>>

    @Query("SELECT * FROM fare_rules")
    suspend fun getAllFareRulesList(): List<FareRuleEntity>

    @Query("SELECT * FROM fare_rules WHERE cardType = :cardType LIMIT 1")
    suspend fun getFareRuleByCardType(cardType: String): FareRuleEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllFareRules(rules: List<FareRuleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(rule: FareRuleEntity): Long

    @Update
    suspend fun updateFareRule(rule: FareRuleEntity)
}

@Dao
interface AuditLogDao {
    @Insert
    suspend fun insertAuditLog(log: AuditLogEntity): Long

    @Query("SELECT * FROM audit_logs WHERE transactionId = :transactionId ORDER BY timestamp DESC")
    fun getAuditLogsForTransaction(transactionId: Long): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>
}

@Dao
interface ReconciliationDao {
    @Insert
    suspend fun insertReconciliation(rec: ReconciliationEntity): Long

    @Query("SELECT * FROM reconciliations WHERE cardId = :cardId ORDER BY timestamp DESC")
    fun getReconciliationsForCard(cardId: Long): Flow<List<ReconciliationEntity>>

    @Query("SELECT * FROM reconciliations ORDER BY timestamp DESC")
    fun getAllReconciliations(): Flow<List<ReconciliationEntity>>

    @Query("UPDATE reconciliations SET cardId = :targetCardId WHERE cardId = :sourceCardId")
    suspend fun migrateReconciliations(sourceCardId: Long, targetCardId: Long): Int
}

@Dao
interface AutomationConfigDao {
    @Query("SELECT * FROM automation_config WHERE id = 1 LIMIT 1")
    fun getConfigFlow(): Flow<AutomationConfigEntity?>

    @Query("SELECT * FROM automation_config WHERE id = 1 LIMIT 1")
    suspend fun getConfig(): AutomationConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: AutomationConfigEntity)
}
