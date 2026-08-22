package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SimCardAlert
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EventType
import com.example.data.model.SecurityEvent
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardElevated
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBg
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EvidenceLogsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val events by viewModel.events.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "HISTÓRICO DE EVIDÊNCIAS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Registro de comandos, alarmes e alertas de chip",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyanAccent
                )
            }

            if (events.isNotEmpty()) {
                IconButton(
                    onClick = {
                        viewModel.clearAuditLogs()
                        Toast.makeText(context, "Histórico limpo", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("clear_logs_button")
                ) {
                    Icon(
                        Icons.Default.DeleteSweep,
                        contentDescription = "Limpar Histórico",
                        tint = TextTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nenhum evento registrado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(events, key = { it.id }) { ev ->
                    SecurityEventItemCard(
                        event = ev,
                        onCopyLocation = { loc ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Google Maps Link", loc)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Link do mapa copiado!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SecurityEventItemCard(
    event: SecurityEvent,
    onCopyLocation: (String) -> Unit
) {
    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "MZ")).format(Date(event.timestamp))

    val (icon, iconTint, badgeText) = when (event.type) {
        EventType.ARMED -> Triple(Icons.Default.Shield, EmeraldGreen, "ARMADO")
        EventType.DISARMED -> Triple(Icons.Default.Shield, CrimsonRed, "DESARMADO")
        EventType.SMS_COMMAND_RECEIVED -> Triple(Icons.Default.Sms, CyanAccent, "SMS COMANDO")
        EventType.SMS_LOCATION_DISPATCHED -> Triple(Icons.Default.MyLocation, CyanAccent, "GPS ENVIADO")
        EventType.ALARM_TRIGGERED -> Triple(Icons.Default.NotificationsActive, CrimsonRed, "ALARME 10S")
        EventType.FLASH_TRIGGERED -> Triple(Icons.Default.Warning, AmberWarning, "LANTERNA")
        EventType.WRONG_PIN_ATTEMPT -> Triple(Icons.Default.Warning, CrimsonRed, "PIN ERRADO")
        EventType.SIM_CHANGED_ALERT -> Triple(Icons.Default.SimCardAlert, CrimsonRed, "TROCA SIM")
        EventType.STEALTH_MODE_ACTIVE -> Triple(Icons.Default.Lock, CyanAccent, "FURTIVO")
        EventType.DEVICE_LOCKED -> Triple(Icons.Default.Lock, AmberWarning, "BLOQUEADO")
        EventType.ACCOUNT_CREATED -> Triple(Icons.Default.Shield, EmeraldGreen, "CONTA MZ")
        EventType.ACCOUNT_LOGGED_IN -> Triple(Icons.Default.Shield, CyanAccent, "LOGIN MZ")
        EventType.ANTI_POWER_OFF_ALERT -> Triple(Icons.Default.PowerSettingsNew, CrimsonRed, "ANTI-DESLIGAR")
        EventType.ANTI_AIRPLANE_MODE_ALERT -> Triple(Icons.Default.Warning, CrimsonRed, "ANTI-AVIÃO")
        EventType.ANTI_DATA_OFF_ALERT -> Triple(Icons.Default.CellTower, AmberWarning, "ANTI-DADOS")
        EventType.CAMERA_CAPTURE_ALERT -> Triple(Icons.Default.PhotoCamera, CyanAccent, "FOTO INTRUSO")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("event_card_${event.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(iconTint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CardElevated)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = iconTint,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = event.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            if (!event.locationInfo.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBg)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.locationInfo,
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanAccent,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onCopyLocation(event.locationInfo) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copiar Localização",
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
