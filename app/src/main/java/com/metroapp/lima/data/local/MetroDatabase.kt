package com.metroapp.lima.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.metroapp.lima.data.local.dao.AuditLogDao
import com.metroapp.lima.data.local.dao.AutomationConfigDao
import com.metroapp.lima.data.local.dao.CardDao
import com.metroapp.lima.data.local.dao.FareRuleDao
import com.metroapp.lima.data.local.dao.HolidayDao
import com.metroapp.lima.data.local.dao.ReconciliationDao
import com.metroapp.lima.data.local.dao.TransactionDao
import com.metroapp.lima.data.local.entity.AuditLogEntity
import com.metroapp.lima.data.local.entity.AutomationConfigEntity
import com.metroapp.lima.data.local.entity.CardEntity
import com.metroapp.lima.data.local.entity.FareRuleEntity
import com.metroapp.lima.data.local.entity.HolidayEntity
import com.metroapp.lima.data.local.entity.ReconciliationEntity
import com.metroapp.lima.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CardEntity::class,
        TransactionEntity::class,
        HolidayEntity::class,
        FareRuleEntity::class,
        AuditLogEntity::class,
        ReconciliationEntity::class,
        AutomationConfigEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MetroDatabase : RoomDatabase() {

    abstract fun cardDao(): CardDao
    abstract fun transactionDao(): TransactionDao
    abstract fun holidayDao(): HolidayDao
    abstract fun fareRuleDao(): FareRuleDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun reconciliationDao(): ReconciliationDao
    abstract fun automationConfigDao(): AutomationConfigDao

    companion object {
        @Volatile
        private var INSTANCE: MetroDatabase? = null

        fun getInstance(context: Context): MetroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MetroDatabase::class.java,
                    "metro_lima.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: MetroDatabase) {
                // Populate default fare rules
                val fareRules = listOf(
                    FareRuleEntity(
                        cardType = "ADULT",
                        weekdayFareCents = 150L,
                        saturdayFareCents = 150L,
                        sundayFareCents = 150L,
                        holidayFareCents = 150L
                    ),
                    FareRuleEntity(
                        cardType = "SCHOOL",
                        weekdayFareCents = 75L,
                        saturdayFareCents = 150L,
                        sundayFareCents = 150L,
                        holidayFareCents = 150L,
                        validityStartMonthDay = "02-15",
                        validityEndMonthDay = "12-31"
                    ),
                    FareRuleEntity(
                        cardType = "UNIVERSITY",
                        weekdayFareCents = 75L,
                        saturdayFareCents = 75L,
                        sundayFareCents = 150L,
                        holidayFareCents = 150L,
                        validityStartMonthDay = null,
                        validityEndMonthDay = null
                    )
                )
                database.fareRuleDao().insertAllFareRules(fareRules)

                // Populate Peruvian National Holidays
                val currentYear = java.time.LocalDate.now().year
                val years = listOf(currentYear - 1, currentYear, currentYear + 1)
                val baseHolidays = listOf(
                    "01-01" to "Año Nuevo",
                    "05-01" to "Día del Trabajo",
                    "06-07" to "Batalla de Arica y Día de la Bandera",
                    "06-29" to "San Pedro y San Pablo",
                    "07-23" to "Día de la Fuerza Aérea del Perú",
                    "07-28" to "Fiestas Patrias",
                    "07-29" to "Fiestas Patrias",
                    "08-06" to "Batalla de Junín",
                    "08-30" to "Santa Rosa de Lima",
                    "10-08" to "Combate de Angamos",
                    "11-01" to "Día de Todos los Santos",
                    "12-08" to "Inmaculada Concepción",
                    "12-09" to "Batalla de Ayacucho",
                    "12-25" to "Navidad"
                )

                val holidayEntities = mutableListOf<HolidayEntity>()
                for (year in years) {
                    for ((mmdd, name) in baseHolidays) {
                        holidayEntities.add(
                            HolidayEntity(
                                dateIso = "$year-$mmdd",
                                name = name,
                                isMovable = false,
                                isCustom = false
                            )
                        )
                    }
                }

                // Add holy week holidays for known years (e.g. 2026: April 2 & 3)
                holidayEntities.add(HolidayEntity(dateIso = "2026-04-02", name = "Jueves Santo", isMovable = true))
                holidayEntities.add(HolidayEntity(dateIso = "2026-04-03", name = "Viernes Santo", isMovable = true))
                holidayEntities.add(HolidayEntity(dateIso = "2027-03-25", name = "Jueves Santo", isMovable = true))
                holidayEntities.add(HolidayEntity(dateIso = "2027-03-26", name = "Viernes Santo", isMovable = true))

                database.holidayDao().insertAllHolidays(holidayEntities)

                // Default Automation Config
                database.automationConfigDao().saveConfig(
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

                // Default card if none exists
                val defaultCard = CardEntity(
                    name = "Mi Tarjeta Metro",
                    cardType = "ADULT",
                    externalCardNumber = "12345678",
                    initialBalanceCents = 1000L, // S/ 10.00 inicial
                    cachedBalanceCents = 1000L,
                    isDefault = true,
                    isActive = true,
                    notes = "Tarjeta predeterminada de la Línea 1"
                )
                database.cardDao().insertCard(defaultCard)
            }
        }
    }
}
