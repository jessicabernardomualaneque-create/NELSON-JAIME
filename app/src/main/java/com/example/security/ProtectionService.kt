package com.example.security

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.receiver.SecurityTriggerReceiver

/**
 * Serviço de Primeiro Plano de Proteção Ativa 24h
 * Mantém os receptores dinâmicos de segurança (Anti-Desligar, Anti-Modo Avião, Anti-Dados)
 * registrados e ativos em segundo plano mesmo quando a aplicação estiver fechada.
 */
class ProtectionService : Service() {

    companion object {
        private const val TAG = "ProtectionService"
        private const val CHANNEL_ID = "anti_theft_protection_channel"
        private const val NOTIFICATION_ID = 1001

        fun startProtection(context: Context) {
            val intent = Intent(context, ProtectionService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopProtection(context: Context) {
            val intent = Intent(context, ProtectionService::class.java)
            context.stopService(intent)
        }
    }

    private var securityReceiver: SecurityTriggerReceiver? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "ProtectionService criado")
        registerDynamicReceivers()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        SubscriptionManager.init(this)
        if (SubscriptionManager.bloquearApp() && !SubscriptionManager.isAdmin()) {
            Log.w(TAG, "Proteção interrompida: Trial expirado e plano inativo.")
            stopSelf()
            return START_NOT_STICKY
        }

        createNotificationChannel()
        val notification = buildForegroundNotification()
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    private fun registerDynamicReceivers() {
        if (securityReceiver == null) {
            securityReceiver = SecurityTriggerReceiver()
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SHUTDOWN)
                addAction("android.intent.action.QUICKBOOT_POWEROFF")
                addAction("android.intent.action.AIRPLANE_MODE")
                addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
                addAction(ConnectivityManager.CONNECTIVITY_ACTION)
                addAction("android.net.conn.CONNECTIVITY_CHANGE")
            }
            registerReceiver(securityReceiver, filter)
            Log.d(TAG, "SecurityTriggerReceiver registrado dinamicamente com sucesso")
        }
    }

    private fun unregisterDynamicReceivers() {
        securityReceiver?.let {
            try {
                unregisterReceiver(it)
                securityReceiver = null
                Log.d(TAG, "SecurityTriggerReceiver desregistrado")
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao desregistrar receiver", e)
            }
        }
    }

    private fun buildForegroundNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Anti-Roubo MZ Pro Ativo")
            .setContentText("Proteção contra furto 24h • Moçambique (+258)")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Serviço de Proteção Contínua MZ",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificação contínua de rastreamento e segurança anti-roubo"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterDynamicReceivers()
        Log.d(TAG, "ProtectionService destruído")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
