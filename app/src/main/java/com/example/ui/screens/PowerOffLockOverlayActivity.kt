package com.example.ui.screens

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EventType
import com.example.data.repository.SecurityPreferences
import com.example.security.LocationHelper
import com.example.security.SilentCameraHelper
import com.example.security.SirenController
import com.example.security.SmsCommandHandler
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CardDark
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBg
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Tela de Bloqueio Sobreposta Anti-Desligamento (Estilo Samsung Knox)
 * Exigida quando o usuário ou ladrão tenta desligar ou alterar configurações críticas.
 */
class PowerOffLockOverlayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show over lockscreen and turn screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            km?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val triggerReason = intent.getStringExtra("EXTRA_REASON") ?: "Tentativa de Desligamento Detectada"

        setContent {
            MyApplicationTheme {
                LockOverlayScreen(
                    reason = triggerReason,
                    onUnlocked = {
                        SirenController.stopAlarm(this)
                        finish()
                    },
                    onWrongPinAlert = { attempts ->
                        handleWrongPinAction(triggerReason, attempts)
                    }
                )
            }
        }
    }

    private fun handleWrongPinAction(reason: String, failedAttempts: Int) {
        val prefs = SecurityPreferences(this)
        SilentCameraHelper.captureFrontCameraSilent(this, "PIN Incorreto no Bloqueio ($reason)")
        SirenController.triggerAlarmAndFlash(this, durationSeconds = 10)

        val contacts = prefs.getEmergencyContacts()
        CoroutineScope(Dispatchers.IO).launch {
            val status = LocationHelper.getFullDeviceStatus(this@PowerOffLockOverlayActivity)
            val mapUrl = status.googleMapsUrl ?: "https://maps.google.com/?q=-25.9692,32.5732"
            val msg = "ALERTA ANTI-ROUBO MZ: PIN incorreto na tela anti-desligamento ($failedAttempts/3)! Localização: $mapUrl - Carga: ${status.batteryPercentage}%"

            contacts.forEach { contact ->
                SmsCommandHandler.sendSms(this@PowerOffLockOverlayActivity, contact.phoneNumber, msg)
            }
        }
    }
}

@Composable
fun LockOverlayScreen(
    reason: String,
    onUnlocked: () -> Unit,
    onWrongPinAlert: (Int) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { SecurityPreferences(context) }
    val correctPin = remember { prefs.getSecurityPin() }

    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var failedCount by remember { mutableStateOf(0) }
    var secondsLeft by remember { mutableStateOf(15) }

    // Automatic countdown security timer
    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
        }
        if (enteredPin != correctPin) {
            errorMessage = "Tempo expirado! Alarme e foto de segurança acionados."
            onWrongPinAlert(failedCount + 1)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("power_off_lock_overlay"),
        color = DarkBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Header: Samsung-like Anti-Tamper Security
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(CrimsonRed.copy(alpha = 0.15f))
                        .border(2.dp, CrimsonRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = CrimsonRed,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "BLOQUEIO DE SEGURANÇA",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonRed,
                    letterSpacing = 1.2.sp
                )

                Text(
                    text = "PROTEÇÃO ANTI-ROUBO MZ PRO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                )

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = CrimsonRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Center: PIN dots indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 10.dp)
            ) {
                Text(
                    text = "Insira o PIN do Proprietário para continuar ($secondsLeft s):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // PIN digit indicator circles
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val maxDots = maxOf(4, correctPin.length)
                    for (i in 0 until maxDots) {
                        val isFilled = i < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) CyanAccent else CardDark)
                                .border(
                                    1.5.dp,
                                    if (isFilled) CyanAccent else BorderSubtle,
                                    CircleShape
                                )
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = CrimsonRed,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Bottom: Numeric Keypad
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "OK")
                )

                for (row in keys) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (key in row) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (key) {
                                            "OK" -> EmeraldGreen.copy(alpha = 0.25f)
                                            "C" -> CrimsonRed.copy(alpha = 0.2f)
                                            else -> CardDark
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        when (key) {
                                            "OK" -> EmeraldGreen
                                            "C" -> CrimsonRed
                                            else -> BorderSubtle
                                        },
                                        CircleShape
                                    )
                                    .clickable {
                                        when (key) {
                                            "C" -> {
                                                enteredPin = ""
                                                errorMessage = null
                                            }
                                            "OK" -> {
                                                if (enteredPin == correctPin) {
                                                    Toast.makeText(context, "Acesso Autorizado", Toast.LENGTH_SHORT).show()
                                                    onUnlocked()
                                                } else {
                                                    failedCount += 1
                                                    errorMessage = "PIN Incorreto! Tentativa $failedCount/3 registrada."
                                                    enteredPin = ""
                                                    onWrongPinAlert(failedCount)
                                                }
                                            }
                                            else -> {
                                                if (enteredPin.length < 8) {
                                                    enteredPin += key
                                                    errorMessage = null
                                                    if (enteredPin == correctPin) {
                                                        Toast.makeText(context, "Acesso Autorizado", Toast.LENGTH_SHORT).show()
                                                        onUnlocked()
                                                    }
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (key == "C") {
                                    Icon(
                                        imageVector = Icons.Default.Backspace,
                                        contentDescription = "Limpar",
                                        tint = CrimsonRed
                                    )
                                } else {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (key == "OK") EmeraldGreen else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Anti-Roubo MZ Pro • Moçambique (+258)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }
    }
}
