package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.example.data.model.EventType
import com.example.data.repository.SecurityPreferences
import com.example.security.LocationHelper
import com.example.security.SmsCommandHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootAndSimReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootAndSimReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val prefs = SecurityPreferences(context)

        Log.d(TAG, "Received broadcast action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED) {
            prefs.logEvent(
                title = "Dispositivo Reiniciado",
                description = "Módulo de proteção em 2º plano reativado após reinicialização.",
                type = EventType.ARMED
            )
            checkSimCard(context, prefs)
        } else if (action == "android.intent.action.SIM_STATE_CHANGED") {
            if (prefs.isSimWatchdogEnabled()) {
                checkSimCard(context, prefs)
            }
        }
    }

    private fun checkSimCard(context: Context, prefs: SecurityPreferences) {
        try {
            val hwInfo = com.example.security.telephony.DeviceHardwareHelper.getFullDeviceHardwareInfo(context)
            val primarySim = hwInfo.primarySim
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

            val simState = tm?.simState ?: TelephonyManager.SIM_STATE_UNKNOWN
            if (simState != TelephonyManager.SIM_STATE_READY && primarySim == null) {
                return
            }

            val currentOperator = primarySim?.operatorName ?: tm?.networkOperatorName?.ifBlank { tm.simOperatorName }?.ifBlank { "Desconhecida (MZ)" } ?: "Desconhecida (MZ)"
            val storedSimId = prefs.getLastKnownSim()
            val currentSimId = primarySim?.simSerialNumber?.ifBlank { null }
                ?: primarySim?.operatorCode?.ifBlank { null }
                ?: tm?.simOperator ?: currentOperator

            if (storedSimId.isNotBlank() && storedSimId != currentSimId) {
                // SIM card has been replaced!
                prefs.logEvent(
                    title = "ALERTA: Troca de Chip SIM Detectada!",
                    description = "Novo operador detectado: $currentOperator (Anterior: ${prefs.getLastKnownOperator()}) | Aparelho: ${hwInfo.brand} ${hwInfo.model} | IMEI: ${hwInfo.imei1}",
                    type = EventType.SIM_CHANGED_ALERT
                )

                // Dispatch SMS to emergency contacts
                val contacts = prefs.getEmergencyContacts()
                CoroutineScope(Dispatchers.IO).launch {
                    val status = LocationHelper.getFullDeviceStatus(context)
                    val alertSms = "ALERTA ANTI-ROUBO MZ PRO! Chip SIM trocado no smartphone ${hwInfo.brand} ${hwInfo.model}. IMEI: ${hwInfo.imei1}. Nova Operadora: $currentOperator. Localização atual: ${status.googleMapsUrl} (Bateria: ${status.batteryPercentage}%)."

                    for (c in contacts) {
                        if (c.phoneNumber.isNotBlank()) {
                            SmsCommandHandler.sendSms(context, c.phoneNumber, alertSms)
                        }
                    }
                }
            }

            // Update stored operator info
            prefs.setLastKnownSim(currentSimId)
            prefs.setLastKnownOperator(currentOperator)
        } catch (e: Exception) {
            Log.e(TAG, "Error evaluating SIM state: ${e.message}", e)
        }
    }
}
