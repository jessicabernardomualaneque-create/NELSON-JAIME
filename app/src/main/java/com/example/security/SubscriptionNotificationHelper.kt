package com.example.security

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

/**
 * Gestor de Notificações Obrigatórias de Trial e Assinatura Anti-Roubo MZ Pro
 *
 * - 3 dias antes: "Faltam 3 dias do trial"
 * - No dia / Expirado: Notificação fixa "TRIAL EXPIRADO. Pague para 869705734" (IMPORTANCE_HIGH, fixo)
 */
object SubscriptionNotificationHelper {

    private const val CHANNEL_TRIAL_ID = "channel_trial_mz_alerts"
    private const val NOTIF_TRIAL_WARNING_ID = 2001
    private const val NOTIF_TRIAL_EXPIRED_ID = 2002

    fun checkAndNotifySubscriptionStatus(context: Context) {
        SubscriptionManager.init(context)
        SubscriptionManager.refreshState()

        if (SubscriptionManager.isAdmin()) {
            // Admin never receives expiry notifications
            cancelAllTrialNotifications(context)
            return
        }

        createNotificationChannel(context)
        val daysLeft = SubscriptionManager.diasRestantes()
        val isLocked = SubscriptionManager.bloquearApp()

        if (isLocked) {
            showTrialExpiredNotification(context)
        } else if (SubscriptionManager.isTrial() && daysLeft in 1L..3L) {
            showTrialEndingWarningNotification(context, daysLeft)
        } else {
            cancelAllTrialNotifications(context)
        }
    }

    private fun showTrialEndingWarningNotification(context: Context, daysRemaining: Long) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_TRIAL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Alerta Anti-Roubo MZ Pro")
            .setContentText("Faltam apenas $daysRemaining dias do seu período de trial grátis.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Faltam $daysRemaining dias do seu período de trial grátis. Para manter o rastreamento 24h ativo, pague via M-Pesa/E-mola para o número 869705734.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIF_TRIAL_WARNING_ID, builder.build())
    }

    private fun showTrialExpiredNotification(context: Context) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_TRIAL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("TRIAL EXPIRADO • ANTI-ROUBO MZ PRO")
            .setContentText("Pague para 869705734 via M-Pesa/E-mola para reativar a proteção.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("O período de teste do Anti-Roubo MZ Pro terminou. O sistema anti-furto está pausado. Pague para 869705734 (Diário 7 MT | Semanal 25 MT | Mensal 80 MT) e insira o código de ativação.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Notificação fixa

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIF_TRIAL_EXPIRED_ID, builder.build())
    }

    private fun cancelAllTrialNotifications(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.cancel(NOTIF_TRIAL_WARNING_ID)
        manager?.cancel(NOTIF_TRIAL_EXPIRED_ID)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_TRIAL_ID,
                "Alertas de Assinatura Anti-Roubo MZ",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações sobre status do trial e ativação de planos de segurança"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
