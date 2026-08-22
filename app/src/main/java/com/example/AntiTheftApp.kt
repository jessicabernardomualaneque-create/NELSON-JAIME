package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.repository.SecurityPreferences

class AntiTheftApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        initializeSecurityState()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "anti_theft_security_channel"
            val channelName = "Proteção Anti-Roubo MZ Pro"
            val descriptionText = "Notificações de segurança, alertas e comandos SMS"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initializeSecurityState() {
        val prefs = SecurityPreferences(this)
        if (prefs.getLastKnownSim().isEmpty()) {
            prefs.setLastKnownOperator("Vodacom MZ / Movitel")
            prefs.setLastKnownSim("init_mz_carrier")
        }
    }
}
