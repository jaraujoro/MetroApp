package com.metroapp.lima.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object AutomationScheduler {

    private const val WORK_NAME = "metro_daily_commute_work"

    fun scheduleDailyWork(context: Context, isEnabled: Boolean) {
        val workManager = WorkManager.getInstance(context)

        if (!isEnabled) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }

        // Periodic check every 6 hours to handle morning/evening commute slots
        val constraints = Constraints.Builder().build()

        val periodicWork = PeriodicWorkRequestBuilder<DailyTripWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWork
        )
    }
}
