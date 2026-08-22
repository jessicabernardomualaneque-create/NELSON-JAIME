package com.example.receiver

// Estas funções são limitadas pelo Android 9+. Servem como alerta e prova, não como bloqueio 100%

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import com.example.data.database.AppDatabase
import com.example.data.database.SecurityAuditEntity
import com.example.data.model.EventType
import com.example.data.repository.SecurityPreferences
import com.example.security.LocationHelper
import com.example.security.SilentCameraHelper
import com.example.security.SirenController
import com.example.security.SmsCommandHandler
import com.example.ui.screens.PowerOffLockOverlayActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.reflect.Method

/**
 * Receptor de Gatilhos de Segurança Imitando Proteção Samsung
 * Monitora desligamento/botão Power, ativação de Modo Avião e corte de Dados Móveis.
 */
class SecurityTriggerReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SecurityTriggerReceiver"
        private var lastTriggerTime: Long = 0L
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val prefs = SecurityPreferences(context)

        // Only react if system is armed
        if (!prefs.isArmed()) {
            return
        }

        // Prevent spam triggers within 3 seconds
        val now = System.currentTimeMillis()
        if (now - lastTriggerTime < 3000L && action != Intent.ACTION_SHUTDOWN) {
            return
        }
        lastTriggerTime = now

        Log.d(TAG, "Recebido gatilho de segurança: $action")

        when (action) {
            // FUNCIONALIDADE 1: ANTI-DESLIGAR / BOTÃO POWER
            Intent.ACTION_SHUTDOWN,
            "android.intent.action.QUICKBOOT_POWEROFF",
            Intent.ACTION_SCREEN_OFF -> {
                if (prefs.isAntiPowerOffEnabled()) {
                    handleAntiShutdownTrigger(context, prefs, action)
                }
            }

            // FUNCIONALIDADE 2: ANTI-MODO AVIÃO
            "android.intent.action.AIRPLANE_MODE",
            Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                if (prefs.isAntiAirplaneModeEnabled()) {
                    val isAirplaneOn = intent.getBooleanExtra("state", false) || isAirplaneModeOn(context)
                    if (isAirplaneOn) {
                        handleAntiAirplaneModeTrigger(context, prefs)
                    }
                }
            }

            // FUNCIONALIDADE 3: ANTI-DESLIGAR DADOS MÓVEIS
            "android.net.conn.CONNECTIVITY_CHANGE",
            ConnectivityManager.CONNECTIVITY_ACTION -> {
                if (prefs.isAntiDataOffEnabled()) {
                    val isConnected = isMobileDataConnected(context)
                    if (!isConnected) {
                        handleAntiDataOffTrigger(context, prefs)
                    }
                }
            }
        }
    }

    /**
     * FUNCIONALIDADE 1: Ação ao detectar tentativa de desligar o celular / apagar ecrã
     */
    private fun handleAntiShutdownTrigger(context: Context, prefs: SecurityPreferences, action: String) {
        Log.w(TAG, "Gatilho Anti-Desligar acionado por: $action")

        val reason = if (action == Intent.ACTION_SCREEN_OFF) "Tentativa de Apagar Ecrã / Botão Power" else "Tentativa de Desligamento / Shutdown"

        // 1. Tirar foto com câmera frontal silenciosamente
        SilentCameraHelper.captureFrontCameraSilent(context, reason)

        // 2. Ligar lanterna estroboscópica e tocar alarme em volume máximo por 10s
        SirenController.triggerAlarmAndFlash(context, durationSeconds = 10)

        // 3. Enviar SMS de alerta com localização para os contactos de emergência
        CoroutineScope(Dispatchers.IO).launch {
            val status = LocationHelper.getFullDeviceStatus(context)
            val mapUrl = status.googleMapsUrl ?: "https://maps.google.com/?q=-25.9692,32.5732"
            val msg = "ALERTA ANTI-ROUBO MZ: Tentaram desligar o celular! Localização: $mapUrl - Bateria: ${status.batteryPercentage}%"

            val contacts = prefs.getEmergencyContacts()
            contacts.forEach { contact ->
                SmsCommandHandler.sendSms(context, contact.phoneNumber, msg)
            }

            // Log no banco Room
            AppDatabase.getInstance(context).securityAuditDao().insertLog(
                SecurityAuditEntity(
                    title = "Tentativa de Desligamento Detectada",
                    description = "Gatilho: $action | SMS enviado para ${contacts.size} contactos | Sirene e foto disparadas.",
                    eventType = "ANTI_POWER_OFF",
                    locationInfo = mapUrl,
                    severity = "CRITICAL"
                )
            )

            prefs.logEvent(
                title = "Alerta Anti-Desligamento",
                description = "Tentativa de desligar interceptada. Sirene e evidência fotográfica gravadas.",
                type = EventType.ANTI_POWER_OFF_ALERT,
                locationInfo = mapUrl
            )
        }

        // 4. Mostrar tela por cima pedindo PIN para continuar desligando (TYPE_APPLICATION_OVERLAY / Lock Screen)
        try {
            val overlayIntent = Intent(context, PowerOffLockOverlayActivity::class.java).apply {
                putExtra("EXTRA_REASON", reason)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            context.startActivity(overlayIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Não foi possível abrir a tela de bloqueio", e)
        }
    }

    /**
     * FUNCIONALIDADE 2: Ação ao detectar ativação de Modo Avião
     */
    private fun handleAntiAirplaneModeTrigger(context: Context, prefs: SecurityPreferences) {
        Log.w(TAG, "Gatilho Anti-Modo Avião acionado!")

        // 1. Tentar desativar modo avião automaticamente via Settings.Global (requer WRITE_SECURE_SETTINGS)
        var restored = false
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                restored = Settings.Global.putInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0)
                val airplaneIntent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).apply {
                    putExtra("state", false)
                }
                context.sendBroadcast(airplaneIntent)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Sem permissão direta WRITE_SECURE_SETTINGS para reverter Modo Avião", e)
        }

        // 2. Tirar foto com câmera frontal
        SilentCameraHelper.captureFrontCameraSilent(context, "Ativação de Modo Avião")

        // 3. Forçar ativação de dados móveis se possível
        tryForceEnableMobileData(context)

        // 4. Enviar SMS de alerta e logar
        CoroutineScope(Dispatchers.IO).launch {
            val status = LocationHelper.getFullDeviceStatus(context)
            val mapUrl = status.googleMapsUrl ?: "https://maps.google.com/?q=-25.9692,32.5732"
            val msg = "ALERTA ANTI-ROUBO MZ: Ativaram Modo Avião no seu celular! Localização: $mapUrl - Bateria: ${status.batteryPercentage}%"

            val contacts = prefs.getEmergencyContacts()
            contacts.forEach { contact ->
                SmsCommandHandler.sendSms(context, contact.phoneNumber, msg)
            }

            AppDatabase.getInstance(context).securityAuditDao().insertLog(
                SecurityAuditEntity(
                    title = "Modo Avião Detectado",
                    description = "Tentativa de isolar a rede interceptada. Reversão tentada: $restored | Foto capturada.",
                    eventType = "ANTI_AIRPLANE_MODE",
                    locationInfo = mapUrl,
                    severity = "CRITICAL"
                )
            )

            prefs.logEvent(
                title = "Alerta Anti-Modo Avião",
                description = "Modo Avião foi ativado indevidamente. Alerta SMS e foto frontal disparados.",
                type = EventType.ANTI_AIRPLANE_MODE_ALERT,
                locationInfo = mapUrl
            )
        }
    }

    /**
     * FUNCIONALIDADE 3: Ação ao detectar corte de dados móveis
     */
    private fun handleAntiDataOffTrigger(context: Context, prefs: SecurityPreferences) {
        Log.w(TAG, "Gatilho Anti-Desligar Dados acionado!")

        // 1. Tentar religar dados móveis usando reflexão
        val reconnected = tryForceEnableMobileData(context)

        // 2. Tirar foto com câmera frontal
        SilentCameraHelper.captureFrontCameraSilent(context, "Desligamento de Dados Móveis")

        // 3. Enviar SMS de alerta
        CoroutineScope(Dispatchers.IO).launch {
            val status = LocationHelper.getFullDeviceStatus(context)
            val mapUrl = status.googleMapsUrl ?: "https://maps.google.com/?q=-25.9692,32.5732"
            val msg = "ALERTA ANTI-ROUBO MZ: Desligaram os dados móveis no seu celular! Localização: $mapUrl - Bateria: ${status.batteryPercentage}%"

            val contacts = prefs.getEmergencyContacts()
            contacts.forEach { contact ->
                SmsCommandHandler.sendSms(context, contact.phoneNumber, msg)
            }

            AppDatabase.getInstance(context).securityAuditDao().insertLog(
                SecurityAuditEntity(
                    title = "Dados Móveis Desligados",
                    description = "Corte de internet móvel detectado. Tentativa de reativação: $reconnected.",
                    eventType = "ANTI_DATA_OFF",
                    locationInfo = mapUrl,
                    severity = "WARNING"
                )
            )

            prefs.logEvent(
                title = "Alerta Anti-Corte de Dados",
                description = "Dados móveis foram desativados. Alerta disparado e reconexão solicitada.",
                type = EventType.ANTI_DATA_OFF_ALERT,
                locationInfo = mapUrl
            )
        }
    }

    private fun isAirplaneModeOn(context: Context): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
        } catch (e: Exception) {
            false
        }
    }

    private fun isMobileDataConnected(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } catch (e: Exception) {
            true
        }
    }

    private fun tryForceEnableMobileData(context: Context): Boolean {
        return try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager ?: return false
            val setMobileDataEnabledMethod: Method? = tm.javaClass.getDeclaredMethod("setDataEnabled", Boolean::class.javaPrimitiveType)
            if (setMobileDataEnabledMethod != null) {
                setMobileDataEnabledMethod.isAccessible = true
                setMobileDataEnabledMethod.invoke(tm, true)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.w(TAG, "Tentativa de reflexão para habilitar dados móveis", e)
            false
        }
    }
}
