package com.example.security

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import com.example.data.model.EventType
import com.example.data.repository.SecurityPreferences
import com.example.receiver.AntiTheftAdminReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SmsCommandHandler {

    data class CommandResult(
        val success: Boolean,
        val commandName: String,
        val responseMessage: String,
        val details: String,
        val batteryPct: Int = 80,
        val isCharging: Boolean = false,
        val gpsCoordinates: String = "-25.9692, 32.5732",
        val googleMapsUrl: String = "https://maps.google.com/?q=-25.9692,32.5732",
        val deviceModel: String = "Android Device",
        val operatorName: String = "Vodacom Moçambique",
        val ownerName: String = "Proprietário MZ",
        val ownerPhone: String = "+258 84 000 0000"
    )

    fun processIncomingMessage(
        context: Context,
        senderNumber: String,
        messageBody: String,
        onResult: ((CommandResult) -> Unit)? = null
    ) {
        val prefs = SecurityPreferences(context)
        val trimmed = messageBody.trim()
        val parts = trimmed.split("\\s+".toRegex())

        if (parts.isEmpty()) return

        val rawCommand = parts[0].uppercase(Locale.ROOT)
        val rawPin = if (parts.size > 1) parts[1].replace("[", "").replace("]", "").trim() else ""

        val configuredPin = prefs.getSecurityPin()
        val (battery, isCharging) = LocationHelper.getBatteryInfo(context)
        val profile = prefs.getUserProfile()
        val ownerName = profile?.fullName ?: "Proprietário Não Registado"
        val ownerPhone = profile?.phoneNumber ?: senderNumber
        val operatorName = profile?.operator?.displayName ?: prefs.getLastKnownOperator()
        val deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"

        // Validate PIN
        if (rawPin != configuredPin) {
            val failedCount = prefs.incrementFailedAttempts()
            val response = "ANTI-ROUBO MZ PRO: PIN Incorreto! Tentativa $failedCount/3 registrada no sistema de Moçambique."
            prefs.logEvent(
                title = "Comando SMS com PIN Incorreto",
                description = "Origem: $senderNumber | Texto: $messageBody | Bateria: $battery%",
                type = EventType.WRONG_PIN_ATTEMPT,
                senderNumber = senderNumber
            )
            sendSms(context, senderNumber, response)
            onResult?.invoke(
                CommandResult(
                    success = false,
                    commandName = rawCommand,
                    responseMessage = response,
                    details = "PIN rejeitado ($rawPin != $configuredPin)",
                    batteryPct = battery,
                    isCharging = isCharging,
                    deviceModel = deviceModel,
                    operatorName = operatorName,
                    ownerName = ownerName,
                    ownerPhone = ownerPhone
                )
            )
            return
        }

        // Reset failed count on valid PIN
        prefs.resetFailedAttempts()

        // Subscription Trial & Premium Verification (Regra 7)
        SubscriptionManager.init(context)
        if (SubscriptionManager.bloquearApp() && !SubscriptionManager.isAdmin()) {
            if (rawCommand == "PAGAR" || rawCommand == "ATIVAR" || rawCommand == "PLANO") {
                val paymentResponse = "ANTI-ROUBO MZ PRO: Para reativar, envie M-Pesa/E-mola para 869705734. Planos: Diário (7 MT), Semanal (25 MT), Mensal (80 MT). Digite o código no app."
                sendSms(context, senderNumber, paymentResponse)
                onResult?.invoke(
                    CommandResult(
                        success = true,
                        commandName = rawCommand,
                        responseMessage = paymentResponse,
                        details = "Informações de pagamento enviadas",
                        batteryPct = battery,
                        isCharging = isCharging,
                        deviceModel = deviceModel,
                        operatorName = operatorName,
                        ownerName = ownerName,
                        ownerPhone = ownerPhone
                    )
                )
                return
            }

            val expiredResponse = "ALERTA ANTI-ROUBO MZ: Período de teste expirado! Comandos remotos bloqueados. Pague via M-Pesa/E-mola para 869705734 (Diário 7 MT | Semanal 25 MT | Mensal 80 MT) para reativar a proteção."
            sendSms(context, senderNumber, expiredResponse)
            prefs.logEvent(
                title = "Comando SMS Bloqueado (Trial Expirado)",
                description = "Origem: $senderNumber | Comando: $rawCommand rejeitado devido ao término do período de trial",
                type = EventType.SMS_COMMAND_RECEIVED,
                senderNumber = senderNumber
            )
            onResult?.invoke(
                CommandResult(
                    success = false,
                    commandName = rawCommand,
                    responseMessage = expiredResponse,
                    details = "Trial expirado - Necessário pagamento para 869705734",
                    batteryPct = battery,
                    isCharging = isCharging,
                    deviceModel = deviceModel,
                    operatorName = operatorName,
                    ownerName = ownerName,
                    ownerPhone = ownerPhone
                )
            )
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            when (rawCommand) {
                "LOCALIZAR", "GPS", "ONDE" -> {
                    handleLocalizarCommand(context, prefs, senderNumber, onResult, battery, isCharging, deviceModel, operatorName, ownerName, ownerPhone)
                }
                "ALARME", "SIRENE", "ALARM" -> {
                    handleAlarmeCommand(context, prefs, senderNumber, onResult, battery, isCharging, deviceModel, operatorName, ownerName, ownerPhone)
                }
                "BLOQUEAR", "LOCK", "TRAVAR" -> {
                    handleBloquearCommand(context, prefs, senderNumber, onResult, battery, isCharging, deviceModel, operatorName, ownerName, ownerPhone)
                }
                "FOTO", "PHOTO", "CAMERA" -> {
                    handleFotoCommand(context, prefs, senderNumber, onResult, battery, isCharging, deviceModel, operatorName, ownerName, ownerPhone)
                }
                "STATUS", "ESTADO", "INFO" -> {
                    handleStatusCommand(context, prefs, senderNumber, onResult, battery, isCharging, deviceModel, operatorName, ownerName, ownerPhone)
                }
                "AJUDA", "HELP", "COMANDOS" -> {
                    handleAjudaCommand(context, senderNumber, onResult, battery, isCharging, deviceModel, operatorName, ownerName, ownerPhone)
                }
                else -> {
                    val helpResponse = "ANTI-ROUBO MZ PRO: Comando desconhecido '$rawCommand'. Use: LOCALIZAR, ALARME, BLOQUEAR, FOTO, STATUS, AJUDA seguido do seu PIN."
                    sendSms(context, senderNumber, helpResponse)
                    onResult?.invoke(
                        CommandResult(
                            success = false,
                            commandName = rawCommand,
                            responseMessage = helpResponse,
                            details = "Comando não reconhecido",
                            batteryPct = battery,
                            isCharging = isCharging,
                            deviceModel = deviceModel,
                            operatorName = operatorName,
                            ownerName = ownerName,
                            ownerPhone = ownerPhone
                        )
                    )
                }
            }
        }
    }

    private suspend fun handleLocalizarCommand(
        context: Context,
        prefs: SecurityPreferences,
        senderNumber: String,
        onResult: ((CommandResult) -> Unit)?,
        battery: Int,
        isCharging: Boolean,
        deviceModel: String,
        operatorName: String,
        ownerName: String,
        ownerPhone: String
    ) {
        val status = LocationHelper.getFullDeviceStatus(context)
        val dateStr = SimpleDateFormat("dd/MM HH:mm", Locale("pt", "MZ")).format(Date())
        val chargingStr = if (status.isCharging) "(⚡ Carga)" else "(🔋 Bateria)"

        val response = "ANTI-ROUBO MZ PRO: Localização: ${status.googleMapsUrl} | Precisão: ${status.accuracy?.toInt() ?: 15}m | Carga: ${status.batteryPercentage}% $chargingStr | Aparelho: $deviceModel | $dateStr"

        sendSms(context, senderNumber, response)

        prefs.logEvent(
            title = "Comando LOCALIZAR Executado",
            description = "Coordenadas enviadas via SMS para $senderNumber (${status.latitude}, ${status.longitude}) - Bateria: ${status.batteryPercentage}%",
            type = EventType.SMS_LOCATION_DISPATCHED,
            locationInfo = status.googleMapsUrl,
            senderNumber = senderNumber
        )

        onResult?.invoke(
            CommandResult(
                success = true,
                commandName = "LOCALIZAR",
                responseMessage = response,
                details = "GPS: ${status.latitude}, ${status.longitude}",
                batteryPct = status.batteryPercentage,
                isCharging = status.isCharging,
                gpsCoordinates = "${status.latitude}, ${status.longitude} (Maputo/MZ)",
                googleMapsUrl = status.googleMapsUrl ?: "https://maps.google.com/?q=-25.9692,32.5732",
                deviceModel = deviceModel,
                operatorName = operatorName,
                ownerName = ownerName,
                ownerPhone = ownerPhone
            )
        )
    }

    private fun handleAlarmeCommand(
        context: Context,
        prefs: SecurityPreferences,
        senderNumber: String,
        onResult: ((CommandResult) -> Unit)?,
        battery: Int,
        isCharging: Boolean,
        deviceModel: String,
        operatorName: String,
        ownerName: String,
        ownerPhone: String
    ) {
        SirenController.triggerAlarmAndFlash(context, durationSeconds = 10)

        val chargingStr = if (isCharging) "⚡ Em Carga" else "🔋 Bateria"
        val response = "ANTI-ROUBO MZ PRO: Alarme sonoro max volume e estroboscópio ativados por 10s. Carga Atual: $battery% ($chargingStr)."
        sendSms(context, senderNumber, response)

        prefs.logEvent(
            title = "Comando ALARME Executado",
            description = "Sirene de emergência e lanterna disparados via comando SMS de $senderNumber | Bateria: $battery%",
            type = EventType.ALARM_TRIGGERED,
            senderNumber = senderNumber
        )

        onResult?.invoke(
            CommandResult(
                success = true,
                commandName = "ALARME",
                responseMessage = response,
                details = "Sirene 10s + Estroboscópio disparados",
                batteryPct = battery,
                isCharging = isCharging,
                deviceModel = deviceModel,
                operatorName = operatorName,
                ownerName = ownerName,
                ownerPhone = ownerPhone
            )
        )
    }

    private fun handleBloquearCommand(
        context: Context,
        prefs: SecurityPreferences,
        senderNumber: String,
        onResult: ((CommandResult) -> Unit)?,
        battery: Int,
        isCharging: Boolean,
        deviceModel: String,
        operatorName: String,
        ownerName: String,
        ownerPhone: String
    ) {
        prefs.setStealthModeEnabled(true)

        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        val adminComponent = ComponentName(context, AntiTheftAdminReceiver::class.java)

        val locked = if (dpm != null && dpm.isAdminActive(adminComponent)) {
            try {
                dpm.lockNow()
                true
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }

        val chargingStr = if (isCharging) "⚡ Carga" else "🔋 Bateria"
        val lockDetail = if (locked) "Tela bloqueada via DeviceAdmin." else "Modo Furtivo ativado."
        val response = "ANTI-ROUBO MZ PRO: $lockDetail Rastreamento contínuo habilitado. Carga do Dispositivo: $battery% ($chargingStr)."

        sendSms(context, senderNumber, response)

        prefs.logEvent(
            title = "Comando BLOQUEAR Executado",
            description = "$lockDetail Solicitado por $senderNumber | Bateria: $battery%",
            type = EventType.DEVICE_LOCKED,
            senderNumber = senderNumber
        )

        onResult?.invoke(
            CommandResult(
                success = true,
                commandName = "BLOQUEAR",
                responseMessage = response,
                details = lockDetail,
                batteryPct = battery,
                isCharging = isCharging,
                deviceModel = deviceModel,
                operatorName = operatorName,
                ownerName = ownerName,
                ownerPhone = ownerPhone
            )
        )
    }

    private fun handleFotoCommand(
        context: Context,
        prefs: SecurityPreferences,
        senderNumber: String,
        onResult: ((CommandResult) -> Unit)?,
        battery: Int,
        isCharging: Boolean,
        deviceModel: String,
        operatorName: String,
        ownerName: String,
        ownerPhone: String
    ) {
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "MZ")).format(Date())
        val chargingStr = if (isCharging) "⚡ Em Carga" else "🔋 Bateria"
        val response = "ANTI-ROUBO MZ PRO: Evidência de captura frontal e foto de ecrã acionadas em $dateStr. Carga Atual: $battery% ($chargingStr) | Guardado em armazenamento seguro."

        sendSms(context, senderNumber, response)

        prefs.logEvent(
            title = "Comando FOTO Executado",
            description = "Captura de evidência visual do intruso e foto de ecrã acionadas por $senderNumber | Bateria: $battery%",
            type = EventType.SMS_COMMAND_RECEIVED,
            senderNumber = senderNumber
        )

        onResult?.invoke(
            CommandResult(
                success = true,
                commandName = "FOTO",
                responseMessage = response,
                details = "Captura de evidência frontal & ecrã acionada",
                batteryPct = battery,
                isCharging = isCharging,
                deviceModel = deviceModel,
                operatorName = operatorName,
                ownerName = ownerName,
                ownerPhone = ownerPhone
            )
        )
    }

    private fun handleStatusCommand(
        context: Context,
        prefs: SecurityPreferences,
        senderNumber: String,
        onResult: ((CommandResult) -> Unit)?,
        battery: Int,
        isCharging: Boolean,
        deviceModel: String,
        operatorName: String,
        ownerName: String,
        ownerPhone: String
    ) {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val carrier = tm?.networkOperatorName?.ifEmpty { null } ?: operatorName
        val armed = if (prefs.isArmed()) "ARMADO" else "DESARMADO"
        val stealth = if (prefs.isStealthModeEnabled()) "FURTIVO" else "NORMAL"
        val chargingStr = if (isCharging) "(⚡ Em Carregamento)" else "(🔋 Em Descarga)"

        val response = "ANTI-ROUBO MZ PRO: Titular: $ownerName | Estado: $armed | Carga Atual: $battery% $chargingStr | Aparelho: $deviceModel | Operadora: $carrier | Watchdog: ATIVO"

        sendSms(context, senderNumber, response)

        prefs.logEvent(
            title = "Comando STATUS Executado",
            description = "Diagnóstico completo enviado para $senderNumber (Bateria: $battery%, Carga: $isCharging)",
            type = EventType.SMS_COMMAND_RECEIVED,
            senderNumber = senderNumber
        )

        onResult?.invoke(
            CommandResult(
                success = true,
                commandName = "STATUS",
                responseMessage = response,
                details = "Relatório de status enviado",
                batteryPct = battery,
                isCharging = isCharging,
                deviceModel = deviceModel,
                operatorName = carrier,
                ownerName = ownerName,
                ownerPhone = ownerPhone
            )
        )
    }

    private fun handleAjudaCommand(
        context: Context,
        senderNumber: String,
        onResult: ((CommandResult) -> Unit)?,
        battery: Int,
        isCharging: Boolean,
        deviceModel: String,
        operatorName: String,
        ownerName: String,
        ownerPhone: String
    ) {
        val response = "ANTI-ROUBO MZ PRO: Comandos SMS: LOCALIZAR [pin], ALARME [pin], BLOQUEAR [pin], FOTO [pin], STATUS [pin]."
        sendSms(context, senderNumber, response)

        onResult?.invoke(
            CommandResult(
                success = true,
                commandName = "AJUDA",
                responseMessage = response,
                details = "Lista de comandos enviada",
                batteryPct = battery,
                isCharging = isCharging,
                deviceModel = deviceModel,
                operatorName = operatorName,
                ownerName = ownerName,
                ownerPhone = ownerPhone
            )
        )
    }

    fun sendSms(context: Context, destinationNumber: String, text: String) {
        try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val parts = smsManager.divideMessage(text)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(destinationNumber, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(destinationNumber, null, text, null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
