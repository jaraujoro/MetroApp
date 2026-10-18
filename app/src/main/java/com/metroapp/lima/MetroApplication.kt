package com.metroapp.lima

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.metroapp.lima.data.local.MetroDatabase

class MetroApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Pre-warm database
        MetroDatabase.getInstance(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "metro_trip_channel",
                "Viajes Automáticos del Metro",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de viajes programados y pendientes de confirmación"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
