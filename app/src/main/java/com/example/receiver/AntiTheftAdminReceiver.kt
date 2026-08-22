package com.example.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.model.EventType
import com.example.data.repository.SecurityPreferences
import com.example.security.LocationHelper
import com.example.security.SirenController
import com.example.security.SmsCommandHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AntiTheftAdminReceiver : DeviceAdminReceiver() {

    companion object {
        private const val TAG = "AntiTheftAdminReceiver"
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.d(TAG, "Device Administrator Enabled")
        val prefs = SecurityPreferences(context)
        prefs.logEvent(
            title = "Administrador Ativado",
            description = "Privilégios de Administrador do Dispositivo concedidos com sucesso.",
            type = EventType.ARMED
        )
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.d(TAG, "Device Administrator Disabled")
        val prefs = SecurityPreferences(context)
        prefs.logEvent(
            title = "Administrador Desativado",
            description = "Atenção: Privilégios de Administrador do Dispositivo foram revogados.",
            type = EventType.DISARMED
        )
    }

    override fun onPasswordFailed(context: Context, intent: Intent) {
        super.onPasswordFailed(context, intent)
        val prefs = SecurityPreferences(context)

        if (!prefs.isArmed() || !prefs.isAntiBruteForceEnabled()) {
            return
        }

        val failedAttempts = prefs.incrementFailedAttempts()
        Log.w(TAG, "Failed unlock attempt: $failedAttempts")

        prefs.logEvent(
            title = "Tentativa de Desbloqueio Incorreta",
            description = "Tentativa $failedAttempts de 3 falhas de PIN/Senha do dispositivo.",
            type = EventType.WRONG_PIN_ATTEMPT
        )

        // If 3 or more failed attempts, trigger anti-brute force response
        if (failedAttempts >= 3) {
            Log.e(TAG, "Anti-Brute Force triggered! 3 wrong PIN attempts reached.")

            // 1. Trigger Siren and Strobe Flashlight for 10 seconds
            SirenController.triggerAlarmAndFlash(context, durationSeconds = 10)

            // 2. Alert emergency contacts via SMS
            val contacts = prefs.getEmergencyContacts()
            CoroutineScope(Dispatchers.IO).launch {
                val status = LocationHelper.getFullDeviceStatus(context)
                val alertMsg = "ALERTA ANTI-ROUBO MZ PRO: Detectadas 3 tentativas erradas de PIN no celular! Localização: ${status.googleMapsUrl} (Bateria: ${status.batteryPercentage}%)."

                for (contact in contacts) {
                    if (contact.phoneNumber.isNotBlank()) {
                        SmsCommandHandler.sendSms(context, contact.phoneNumber, alertMsg)
                    }
                }
            }
        }
    }

    override fun onPasswordSucceeded(context: Context, intent: Intent) {
        super.onPasswordSucceeded(context, intent)
        val prefs = SecurityPreferences(context)
        prefs.resetFailedAttempts()
    }
}
