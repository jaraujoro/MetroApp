package com.metroapp.lima.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.metroapp.lima.MainActivity
import com.metroapp.lima.data.local.MetroDatabase
import com.metroapp.lima.data.repository.MetroRepository
import com.metroapp.lima.domain.model.MovementOrigin
import com.metroapp.lima.domain.model.TransactionStatus
import com.metroapp.lima.util.DateUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DailyTripWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = MetroDatabase.getInstance(context)
        val repository = MetroRepository(database)

        val config = repository.getAutomationConfig()
        if (!config.isEnabled) {
            return Result.success()
        }

        val nowLima = LocalDateTime.now(DateUtils.LIMA_ZONE)
        val todayIso = DateUtils.formatIsoDate(DateUtils.toEpochMillis(nowLima))

        // Check active days of week (1=Monday ... 7=Sunday) and custom dates
        val dayOfWeekValue = nowLima.dayOfWeek.value // 1..7
        val activeDayList = config.activeDays.split(",").mapNotNull { it.trim().toIntOrNull() }
        val customDateList = config.customDates.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        val isDayActive = if (customDateList.isNotEmpty()) {
            customDateList.contains(todayIso) || activeDayList.contains(dayOfWeekValue)
        } else if (activeDayList.isNotEmpty()) {
            activeDayList.contains(dayOfWeekValue)
        } else {
            true
        }

        if (!isDayActive) {
            return Result.success() // Hoy no está programado viaje automático
        }

        val card = if (config.defaultCardId != null) {
            repository.getCardById(config.defaultCardId)
        } else {
            repository.getDefaultCard()
        } ?: return Result.success()

        // Determine current slot (morning or evening) based on current hour and tripsPerDay
        val currentHour = nowLima.hour
        val slot = if (currentHour < 14) "MORNING" else "EVENING"

        // If only 1 trip per day is configured, only run on morning slot (or first slot)
        if (config.tripsPerDay == 1 && slot == "EVENING") {
            return Result.success()
        }

        val scheduledSlotKey = "${card.id}_${todayIso}_$slot"

        // Check if already created or existing for this slot
        val existing = database.transactionDao().findByScheduledSlot(card.id, scheduledSlotKey)
        if (existing != null) {
            return Result.success() // Already processed for this slot today!
        }

        // Calculate fare for this card and date
        val fareResult = repository.calculateFare(card.cardType, nowLima)
        val status = if (config.autoConfirm) TransactionStatus.CONFIRMED else TransactionStatus.PENDING

        val txResult = repository.registerTrip(
            cardId = card.id,
            dateTime = nowLima,
            allowNegativeBalance = false,
            origin = MovementOrigin.AUTOMATIC,
            status = status,
            scheduledSlot = scheduledSlotKey,
            customFareCents = fareResult.fareCents,
            notes = "Viaje automático ($slot) - ${fareResult.explanation}"
        )

        if (txResult.isSuccess) {
            val txId = txResult.getOrThrow()
            showTripNotification(context, card.name, fareResult.fareCents, status == TransactionStatus.CONFIRMED, txId)
        }

        return Result.success()
    }

    private fun showTripNotification(
        context: Context,
        cardName: String,
        fareCents: Long,
        isAutoConfirmed: Boolean,
        txId: Long
    ) {
        val channelId = "metro_trip_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Viajes Automáticos del Metro",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de viajes programados y pendientes"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_TRANSACTION_ID", txId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            txId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isAutoConfirmed) "Viaje del Metro Registrado" else "Viaje Programado Pendiente"
        val message = if (isAutoConfirmed) {
            "Se descontó S/ ${fareCents / 100.0} de tu tarjeta $cardName."
        } else {
            "¿Realizaste este viaje en $cardName por S/ ${fareCents / 100.0}? Toca para confirmar."
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(txId.toInt(), builder.build())
    }
}
