package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.example.data.repository.SecurityPreferences
import com.example.security.SmsCommandHandler

class SmsCommandReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SmsCommandReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        val prefs = SecurityPreferences(context)
        if (!prefs.isArmed()) {
            Log.d(TAG, "Anti-Theft is disarmed, ignoring incoming SMS.")
            return
        }

        val messages: Array<SmsMessage>? = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Telephony.Sms.Intents.getMessagesFromIntent(intent)
            } else {
                @Suppress("DEPRECATION")
                val bundle = intent.extras
                val pdus = bundle?.get("pdus") as? Array<*>
                pdus?.mapNotNull {
                    @Suppress("DEPRECATION")
                    SmsMessage.createFromPdu(it as ByteArray)
                }?.toTypedArray()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing SMS: ${e.message}", e)
            null
        }

        if (messages.isNullOrEmpty()) return

        // Group messages by originating address
        val senderMap = mutableMapOf<String, StringBuilder>()
        for (message in messages) {
            val sender = message.displayOriginatingAddress ?: message.originatingAddress ?: continue
            val body = message.displayMessageBody ?: message.messageBody ?: ""
            senderMap.getOrPut(sender) { StringBuilder() }.append(body)
        }

        for ((sender, bodyBuilder) in senderMap) {
            val fullBody = bodyBuilder.toString().trim()
            Log.d(TAG, "Received SMS from $sender: $fullBody")

            // Check if this message starts with any of our known commands
            val upper = fullBody.uppercase()
            val isAntiTheftCommand = upper.startsWith("LOCALIZAR") ||
                    upper.startsWith("ALARME") ||
                    upper.startsWith("BLOQUEAR") ||
                    upper.startsWith("FOTO") ||
                    upper.startsWith("STATUS") ||
                    upper.startsWith("AJUDA") ||
                    upper.startsWith("GPS") ||
                    upper.startsWith("SIRENE") ||
                    upper.startsWith("LOCK")

            if (isAntiTheftCommand) {
                // Abort broadcast so user/thief doesn't see notification if possible on older APIs
                try {
                    abortBroadcast()
                } catch (e: Exception) {
                    // ignore if not ordered broadcast
                }

                // Process command
                SmsCommandHandler.processIncomingMessage(context, sender, fullBody)
            }
        }
    }
}
