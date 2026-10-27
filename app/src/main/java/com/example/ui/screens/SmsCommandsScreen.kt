package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SmsCommandItem
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
import com.example.ui.viewmodel.SmsSimulatedMessage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmsCommandsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pin by viewModel.securityPin.collectAsState()
    val simulatedMessages by viewModel.simulatedMessages.collectAsState()
    var selectedTab by remember { mutableIntStateOf(1) } // Default directly to interactive SMS Simulator
    var customCommandInput by remember { mutableStateOf("FOTO $pin") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Header
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "COMANDOS & SIMULADOR SMS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Telemetria da Bateria, Foto de Ecrã & Detalhes MZ",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanAccent
                    )
                }

                IconButton(
                    onClick = {
                        val shareText = buildString {
                            appendLine("🚨 *COMANDOS ANTI-ROUBO MZ PRO* 🚨")
                            appendLine("Envie estes SMS do seu número para rastrear/proteger o meu celular (PIN: $pin):")
                            appendLine()
                            viewModel.smsCommandList.forEach {
                                appendLine("📱 *${it.syntax.replace("[senha]", pin)}* -> ${it.description}")
                            }
                            appendLine()
                            appendLine("Anti-Roubo MZ Pro - Segurança Offline Moçambique")
                        }
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Partilhar Comandos SMS com Familiares"))
                    },
                    modifier = Modifier.testTag("share_commands_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Partilhar Comandos",
                        tint = CyanAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-tabs: Simulador Interativo vs Guia de Comandos
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = CardDark,
                contentColor = CyanAccent,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CyanAccent
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Simulador SMS + Foto & Bateria", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Guia de Comandos MZ", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (selectedTab == 0) {
            // Tab 0: Commands Catalog
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    // Instruction Card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "COMO USAR QUANDO O TELEFONE FOR ROUBADO:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "1. Pegue o celular de qualquer amigo ou familiar (Vodacom, Movitel, Tmcel).\n2. Envie um SMS normal com o comando e o seu PIN (ex: LOCALIZAR $pin ou FOTO $pin).\n3. O Anti-Roubo MZ Pro responde com a carga da bateria, foto da câmara frontal, mapa GPS e detalhes do dispositivo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                items(viewModel.smsCommandList) { cmd ->
                    SmsCommandCard(
                        item = cmd,
                        currentPin = pin,
                        onCopy = { syntax ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("SMS Command", syntax)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copiado: $syntax", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        } else {
            // Tab 1: Interactive SMS Simulator with Live Battery, Photo Preview & Telemetry
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "CONSOLE DE TESTE DE SMS EM TEMPO REAL",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Text(
                    text = "Simule comandos SMS e visualize a carga atual, foto do ecrã e telemetria do usuário:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Command Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val chips = listOf(
                        "FOTO $pin",
                        "LOCALIZAR $pin",
                        "STATUS $pin",
                        "BLOQUEAR $pin",
                        "ALARME $pin",
                        "AJUDA $pin",
                        "FOTO 0000" // wrong PIN test
                    )
                    chips.forEach { chipText ->
                        val isWrongPin = chipText.contains("0000")
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(CardElevated)
                                .border(
                                    1.dp,
                                    if (isWrongPin) CrimsonRed else CyanAccent.copy(alpha = 0.4f),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    customCommandInput = chipText
                                    viewModel.simulateSmsCommand(chipText)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = chipText,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isWrongPin) CrimsonRed else TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Messages Feed with Telemetry & Photo Evidence
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardDark)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(simulatedMessages) { msg ->
                        SmsMessageBubble(msg = msg, currentPin = pin)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customCommandInput,
                        onValueChange = { customCommandInput = it },
                        placeholder = { Text("Digite o comando (ex: FOTO $pin)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (customCommandInput.isNotBlank()) {
                                viewModel.simulateSmsCommand(customCommandInput)
                            }
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sms_input_field")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (customCommandInput.isNotBlank()) {
                                viewModel.simulateSmsCommand(customCommandInput)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("send_sms_sim_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Enviar", tint = DarkBg)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun SmsCommandCard(
    item: SmsCommandItem,
    currentPin: String,
    onCopy: (String) -> Unit
) {
    val formattedSyntax = item.syntax.replace("[senha]", currentPin)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("command_card_${item.command.lowercase()}"),
        shape = RoundedCornerShape(14.dp),
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
                    val icon = when (item.command) {
                        "LOCALIZAR" -> Icons.Default.MyLocation
                        "ALARME" -> Icons.Default.VolumeUp
                        "BLOQUEAR" -> Icons.Default.Lock
                        "FOTO" -> Icons.Default.CameraAlt
                        "STATUS" -> Icons.Default.Info
                        else -> Icons.Default.HelpOutline
                    }
                    val iconColor = when (item.command) {
                        "LOCALIZAR" -> CyanAccent
                        "ALARME" -> CrimsonRed
                        "BLOQUEAR" -> AmberWarning
                        "FOTO" -> EmeraldGreen
                        else -> CyanAccent
                    }
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.command,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CardElevated)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.badge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Code Syntax Snippet Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkBg)
                    .border(1.dp, CyanAccent.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Texto do SMS:", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 10.sp)
                    Text(
                        text = formattedSyntax,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        fontSize = 14.sp
                    )
                }

                IconButton(
                    onClick = { onCopy(formattedSyntax) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar Comando",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SmsMessageBubble(
    msg: SmsSimulatedMessage,
    currentPin: String
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (msg.isIncoming) Alignment.Start else Alignment.End
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (msg.isIncoming) Arrangement.Start else Arrangement.End
        ) {
            Text(
                text = msg.statusBadge ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = if (msg.isIncoming) {
                    if (msg.statusBadge?.contains("ERRO") == true) CrimsonRed else EmeraldGreen
                } else AmberWarning,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = msg.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // SMS Bubble
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (msg.isIncoming) 0.dp else 12.dp,
                        bottomEnd = if (msg.isIncoming) 12.dp else 0.dp
                    )
                )
                .background(if (msg.isIncoming) CardElevated else CyanAccent.copy(alpha = 0.2f))
                .border(
                    1.dp,
                    if (msg.isIncoming) {
                        if (msg.statusBadge?.contains("ERRO") == true) CrimsonRed.copy(alpha = 0.5f) else CyanAccent.copy(alpha = 0.4f)
                    } else CyanAccent.copy(alpha = 0.5f),
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (msg.isIncoming) 0.dp else 12.dp,
                        bottomEnd = if (msg.isIncoming) 12.dp else 0.dp
                    )
                )
                .padding(12.dp)
        ) {
            Text(
                text = msg.text,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
                lineHeight = 16.sp
            )
        }

        // If this is an incoming response, display rich telemetry (Battery, User details, Photo/Screen Evidence)
        if (msg.isIncoming && msg.batteryPct != null) {
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header: Live Telemetry
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DETALHES DO USUÁRIO & APARELHO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                fontSize = 11.sp
                            )
                        }

                        // Battery Pill
                        val batColor = when {
                            msg.batteryPct > 50 -> EmeraldGreen
                            msg.batteryPct > 20 -> AmberWarning
                            else -> CrimsonRed
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(batColor.copy(alpha = 0.15f))
                                .border(1.dp, batColor, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (msg.isCharging == true) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                                    contentDescription = null,
                                    tint = batColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${msg.batteryPct}% ${if (msg.isCharging == true) "⚡" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = batColor,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Battery Progress Level Bar
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Carga Atual do Dispositivo:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = if (msg.isCharging == true) "⚡ Em Carregamento" else "🔋 Em Descarga",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (msg.isCharging == true) EmeraldGreen else TextTertiary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (msg.batteryPct.toFloat() / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (msg.batteryPct > 50) EmeraldGreen else if (msg.batteryPct > 20) AmberWarning else CrimsonRed,
                            trackColor = CardElevated,
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Grid of User Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // User Info
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Titular MZ:", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 10.sp)
                            }
                            Text(
                                text = msg.ownerName ?: "Jéssica Bernardo",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = msg.ownerPhone ?: "+258 84 123 4567",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent,
                                fontSize = 11.sp
                            )
                        }

                        // Operator & Device Info
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CellTower, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rede / Modelo:", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 10.sp)
                            }
                            Text(
                                text = msg.operatorName ?: "Vodacom Moçambique",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = msg.deviceModel ?: "Android Security Pro",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            val imeiFound = if (msg.text.contains("IMEI: ")) {
                                msg.text.substringAfter("IMEI: ").substringBefore(" |").trim()
                            } else if (msg.text.contains("IMEI 1: ")) {
                                msg.text.substringAfter("IMEI 1: ").substringBefore(" |").trim()
                            } else null
                            if (imeiFound != null) {
                                Text(
                                    text = "IMEI: $imeiFound",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // GPS Link if available
                    if (msg.gpsCoordinates != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CardElevated)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("GPS:", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 10.sp)
                                    Text(
                                        text = msg.gpsCoordinates,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            if (msg.googleMapsUrl != null) {
                                IconButton(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(msg.googleMapsUrl))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Abrindo mapa...", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.OpenInNew, contentDescription = "Ver no Mapa", tint = CyanAccent, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // Visual Evidence: Foto da Câmara Frontal ou Captura de Ecrã
                    if (msg.hasPhotoPreview || msg.commandType in listOf("FOTO", "PHOTO", "CAMERA", "LOCALIZAR", "STATUS")) {
                        Spacer(modifier = Modifier.height(10.dp))
                        PhotoEvidencePreviewCard(
                            commandType = msg.commandType ?: "FOTO",
                            ownerName = msg.ownerName ?: "Titular MZ",
                            batteryPct = msg.batteryPct,
                            timestamp = msg.timestamp
                        )
                    }

                    // Screen Lock Mockup Preview
                    if (msg.hasScreenLockPreview || msg.commandType in listOf("BLOQUEAR", "LOCK", "TRAVAR")) {
                        Spacer(modifier = Modifier.height(10.dp))
                        ScreenLockPreviewCard(
                            batteryPct = msg.batteryPct,
                            ownerPhone = msg.ownerPhone ?: "+258 84 123 4567"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoEvidencePreviewCard(
    commandType: String,
    ownerName: String,
    batteryPct: Int,
    timestamp: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "EVIDÊNCIA: FOTO FRONTAL DO INTRUSO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(EmeraldGreen.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "1080p FRONT",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Simulated Camera Frame Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, EmeraldGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Background Grid HUD
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "[REC] LIVE CAPTURE",
                            color = CrimsonRed,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "BAT: $batteryPct%",
                            color = EmeraldGreen,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Facial Detection Target Box
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .align(Alignment.CenterHorizontally)
                            .border(1.5.dp, EmeraldGreen, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "Rosto Detectado",
                            tint = EmeraldGreen.copy(alpha = 0.8f),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "GEO: MAPUTO, MZ",
                            color = CyanAccent,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = timestamp,
                            color = TextTertiary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "✓ Fotografia tirada silenciosamente pela câmara frontal e armazenada de forma segura na memória interna do telemóvel.",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun ScreenLockPreviewCard(
    batteryPct: Int,
    ownerPhone: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = AmberWarning,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FOTO DE ECRÃ: APARELHO BLOQUEADO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AmberWarning,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AmberWarning.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "LOCK ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberWarning,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Simulated Lock Screen Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E1B4B))
                    .border(1.dp, AmberWarning.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "DISPOSITIVO PROTEGIDO POR ANTI-ROUBO MZ",
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Contacto de Devolução: $ownerPhone",
                        color = CyanAccent,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}
