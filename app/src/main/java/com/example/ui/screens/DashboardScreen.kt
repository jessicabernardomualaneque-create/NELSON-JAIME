package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardElevated
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.DarkBg
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VodacomRed
import com.example.ui.viewmodel.MainViewModel
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import android.widget.Toast
import com.example.data.model.MozambiqueOperator
import com.example.security.telephony.DeviceHardwareHelper

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onRequestAdmin: () -> Unit,
    onOpenMap: () -> Unit = {},
    onOpenVault: () -> Unit = {},
    onOpenPayment: () -> Unit = {},
    onOpenAdmin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isArmed by viewModel.isArmed.collectAsState()
    val deviceStatus by viewModel.deviceStatus.collectAsState()
    val liveLoc by viewModel.liveLocationState.collectAsState()
    val totalVaultCount by viewModel.totalVaultCount.collectAsState()
    val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsState()
    val antiBruteForce by viewModel.antiBruteForce.collectAsState()
    val simWatchdog by viewModel.simWatchdog.collectAsState()
    val stealthMode by viewModel.stealthMode.collectAsState()
    val pin by viewModel.securityPin.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val isTrial by viewModel.isTrial.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val isSubAdmin by viewModel.isSubAdmin.collectAsState()
    val diasRestantes by viewModel.diasRestantes.collectAsState()
    val tipoPlano by viewModel.tipoPlano.collectAsState()
    val expiryFormatted by viewModel.expiryDateFormatted.collectAsState()
    val hardwareInfo by viewModel.hardwareInfo.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isImeiRevealed by remember { mutableStateOf(false) }
    var showEditImeiDialog by remember { mutableStateOf(false) }
    var editImeiInput by remember(hardwareInfo.imei1) { mutableStateOf(hardwareInfo.imei1) }

    val infiniteTransition = rememberInfiniteTransition(label = "shield_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isArmed) 1.08f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Header with Mozambique Profile Details
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ANTI-ROUBO MZ PRO",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (userProfile != null) "${userProfile?.fullName} • ${userProfile?.phoneNumber}" else "Segurança Offline Moçambique",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyanAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardDark)
                    .border(1.dp, if (isArmed) EmeraldGreen else CrimsonRed, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isArmed) "PROTEGIDO" else "DESARMADO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isArmed) EmeraldGreen else CrimsonRed
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Subscription & Licensing Status Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(
                    when {
                        isSubAdmin -> VodacomRed.copy(alpha = 0.15f)
                        isPremium -> EmeraldGreen.copy(alpha = 0.12f)
                        isTrial -> CyanAccent.copy(alpha = 0.1f)
                        else -> CrimsonRed.copy(alpha = 0.18f)
                    }
                )
                .border(
                    1.dp,
                    when {
                        isSubAdmin -> VodacomRed.copy(alpha = 0.5f)
                        isPremium -> EmeraldGreen.copy(alpha = 0.4f)
                        isTrial -> CyanAccent.copy(alpha = 0.4f)
                        else -> CrimsonRed.copy(alpha = 0.6f)
                    },
                    RoundedCornerShape(10.dp)
                )
                .clickable {
                    if (isSubAdmin) onOpenAdmin() else onOpenPayment()
                }
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("dashboard_license_banner")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when {
                            isSubAdmin -> Icons.Default.Security
                            isPremium -> Icons.Default.Shield
                            isTrial -> Icons.Default.NotificationsActive
                            else -> Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = when {
                            isSubAdmin -> VodacomRed
                            isPremium -> EmeraldGreen
                            isTrial -> CyanAccent
                            else -> CrimsonRed
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = when {
                                isSubAdmin -> "ADMIN MASTER MZ • Acesso Ilimitado"
                                isPremium -> "Licença MZ Ativa ($tipoPlano)"
                                isTrial -> "Trial Grátis: $diasRestantes dias restantes"
                                else -> "Trial Expirado • Clique para Pagar 869705734"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isSubAdmin -> VodacomRed
                                isPremium -> EmeraldGreen
                                isTrial -> CyanAccent
                                else -> CrimsonRed
                            }
                        )
                        Text(
                            text = when {
                                isSubAdmin -> "Telefone 869705734 • Gestão e Códigos"
                                isPremium -> "Válido até $expiryFormatted"
                                isTrial -> "Planos Diário 7MT | Semanal 25MT | Mensal 80MT"
                                else -> "Pague via M-Pesa / E-mola para desbloquear"
                            },
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(
                            when {
                                isSubAdmin -> VodacomRed
                                isPremium -> EmeraldGreen
                                isTrial -> CyanAccent
                                else -> CrimsonRed
                            },
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isSubAdmin) "PAINEL" else "PLANOS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSubAdmin) Color.White else Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Big Interactive Central Shield
        Box(
            modifier = Modifier
                .size(190.dp)
                .testTag("main_shield_button")
                .clickable { viewModel.toggleArmed() },
            contentAlignment = Alignment.Center
        ) {
            // Pulse outer circle
            if (isArmed) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    CyanAccent.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Inner Shield Container
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(CardDark)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                if (isArmed) CyanAccent else CrimsonRed,
                                if (isArmed) EmeraldGreen else AmberWarning
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (isArmed) Icons.Default.Shield else Icons.Default.PowerSettingsNew,
                        contentDescription = "Estado de Arme",
                        tint = if (isArmed) CyanAccent else CrimsonRed,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isArmed) "SISTEMA ATIVO" else "TOQUE P/ ARMAR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isArmed) "PIN: $pin" else "Desarmado",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Alarm Active Warning Banner
        if (deviceStatus.isSirenPlaying) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CrimsonRed.copy(alpha = 0.2f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonRed)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = CrimsonRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ALARME & FLASH ATIVOS (10s)",
                            fontWeight = FontWeight.Bold,
                            color = CrimsonRed,
                            fontSize = 13.sp
                        )
                    }
                    Button(
                        onClick = { viewModel.stopAlarm() },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("PARAR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Quick Actions Grid (Test siren, Test Flash, Refresh GPS)
        Text(
            text = "TESTE RÁPIDO & AÇÕES DE EMERGÊNCIA",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Test Siren 10s Button
            Button(
                onClick = { viewModel.triggerTestAlarm(10) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("test_siren_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (deviceStatus.isSirenPlaying) CrimsonRed else CardElevated
                )
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sirene 10s", fontSize = 12.sp, color = TextPrimary)
            }

            // Test Flashlight Strobe Button
            Button(
                onClick = { viewModel.triggerTestAlarm(5) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("test_flash_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardElevated)
            ) {
                Icon(Icons.Default.FlashOn, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Lanterna", fontSize = 12.sp, color = TextPrimary)
            }

            // Refresh GPS Coordinates
            Button(
                onClick = { viewModel.refreshDeviceStatus() },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("refresh_gps_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardElevated)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("GPS", fontSize = 12.sp, color = TextPrimary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card de Hardware: Leitura do Cartão SIM, IMEI e Marca por Modelo
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hardware_sim_imei_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, CyanAccent.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(CyanAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Smartphone, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "HARDWARE, SIM & IMEI MZ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Identificação Oficial de Segurança",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            viewModel.refreshDeviceStatus()
                            Toast.makeText(context, "Telemetria e SIM atualizados!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Atualizar", tint = CyanAccent, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // SEÇÃO 1: MARCA & MODELO INTELIGENTE
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CardElevated)
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CyanAccent.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = hardwareInfo.brand.uppercase(java.util.Locale.ROOT),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = CyanAccent,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = hardwareInfo.model,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Text(
                                text = hardwareInfo.rawModel,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Fabricante: ${hardwareInfo.manufacturer} • ${hardwareInfo.androidVersion}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // SEÇÃO 2: CARTÃO SIM & OPERADORA DE MOÇAMBIQUE
                hardwareInfo.simSlots.forEach { simSlot ->
                    val opColor = when (simSlot.operator) {
                        MozambiqueOperator.VODACOM -> VodacomRed
                        MozambiqueOperator.MOVITEL -> AmberWarning
                        MozambiqueOperator.TMCEL -> CyanAccent
                        MozambiqueOperator.UNKNOWN -> TextSecondary
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CardElevated)
                            .border(1.dp, opColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SimCard,
                                        contentDescription = "SIM",
                                        tint = opColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "${simSlot.slotLabel}: ${simSlot.operatorName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Código de Rede: ${simSlot.operatorCode} • ${simSlot.networkType}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextTertiary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(opColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (simSlot.isReady) "ATIVO" else "SIM PRONTO",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = opColor,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "ICCID / Série: ${simSlot.simSerialNumber}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )

                            // Atalhos USSD da Operadora
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                            data = android.net.Uri.parse("tel:${android.net.Uri.encode(simSlot.ussdBalanceCode)}")
                                        }
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = "Saldo (${simSlot.ussdBalanceCode})",
                                        fontSize = 10.sp,
                                        color = opColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                            data = android.net.Uri.parse("tel:${android.net.Uri.encode(simSlot.mobileMoneyUssd)}")
                                        }
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = "${simSlot.mobileMoneyName} (${simSlot.mobileMoneyUssd})",
                                        fontSize = 10.sp,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // SEÇÃO 3: NÚMERO IMEI (15 DÍGITOS)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CardElevated)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "IMEI",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "IMEI 1 (Hardware Principal):",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextTertiary
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { isImeiRevealed = !isImeiRevealed },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isImeiRevealed) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Revelar IMEI",
                                        tint = CyanAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { showEditImeiDialog = true },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar IMEI",
                                        tint = TextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // IMEI Text Box
                        val displayedImei = if (isImeiRevealed) {
                            DeviceHardwareHelper.formatImeiPretty(hardwareInfo.imei1)
                        } else {
                            val raw = hardwareInfo.imei1
                            if (raw.length == 15) {
                                "${raw.take(4)} •••• •••• ${raw.takeLast(3)}"
                            } else {
                                "•••• •••• •••• •••"
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = displayedImei,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = CyanAccent,
                                letterSpacing = 1.sp
                            )

                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(hardwareInfo.imei1))
                                    Toast.makeText(context, "IMEI copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = TextPrimary, modifier = Modifier.size(16.dp))
                            }
                        }

                        if (!hardwareInfo.imei2.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "IMEI 2 (Slot 2): ${if (isImeiRevealed) DeviceHardwareHelper.formatImeiPretty(hardwareInfo.imei2!!) else "•••• •••• ••••"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(hardwareInfo.imei2!!))
                                        Toast.makeText(context, "IMEI 2 copiado!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copiar IMEI 2", tint = TextTertiary, modifier = Modifier.size(14.dp))
                                }
                            }
                        }

                        // Botão Conferir no Teclado *#06#
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        context.startActivity(DeviceHardwareHelper.createCheckImeiDialIntent())
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Disque *#06# no teclado para ver o IMEI de fábrica.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Dialpad, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Conferir no Teclado (*#06#)", fontSize = 11.sp, color = CyanAccent)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Diagnostic Telemetry (Battery, Last GPS, Device Admin)
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Battery
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (deviceStatus.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                            contentDescription = "Bateria",
                            tint = if (deviceStatus.batteryPct > 20) EmeraldGreen else CrimsonRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Bateria", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            Text(
                                text = "${deviceStatus.batteryPct}% ${if (deviceStatus.isCharging) "(⚡ Carga)" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    // Device Admin Status
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Device Admin",
                            tint = if (deviceStatus.isAdminActive) EmeraldGreen else AmberWarning,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Admin Dispositivo", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            Text(
                                text = if (deviceStatus.isAdminActive) "Ativado (Protegido)" else "Toque p/ Ativar",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (deviceStatus.isAdminActive) EmeraldGreen else AmberWarning,
                                modifier = if (!deviceStatus.isAdminActive) Modifier.clickable { onRequestAdmin() } else Modifier
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mozambique Live Map & Real-Time Tracking Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_mozambique_map_card")
                .clickable { onOpenMap() },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardElevated),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, CyanAccent)
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyanAccent.copy(alpha = 0.2f))
                                .border(1.dp, CyanAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Map, contentDescription = "Mapa MZ", tint = CyanAccent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MAPA DE MOÇAMBIQUE EM TEMPO REAL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "${liveLoc.provinceName} • ${liveLoc.nearestCity}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (liveLoc.isOnline) EmeraldGreen.copy(alpha = 0.2f) else AmberWarning.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (liveLoc.isOnline) "GPS AO VIVO" else "OFFLINE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (liveLoc.isOnline) EmeraldGreen else AmberWarning,
                            fontSize = 9.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Endereço: ${liveLoc.detailedAddress}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBg)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Precisão", color = TextTertiary, fontSize = 10.sp)
                        Text("±${String.format(java.util.Locale.ROOT, "%.1f", liveLoc.accuracyMeters)}m", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Velocidade", color = TextTertiary, fontSize = 10.sp)
                        Text("${String.format(java.util.Locale.ROOT, "%.1f", liveLoc.speedKmh)} km/h", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Distância Maputo", color = TextTertiary, fontSize = 10.sp)
                        Text("${String.format(java.util.Locale.ROOT, "%.0f", liveLoc.distanceToCapitalKm)} km", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onOpenMap,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    Icon(Icons.Default.GpsFixed, contentDescription = null, tint = DarkBg, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ABRIR MAPA COMPLETO DE MOÇAMBIQUE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkBg)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // -------------------------------------------------------------
        // COFRE SECRETO MZ PRO (NOVO RECURSO DE SEGURANÇA)
        // -------------------------------------------------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenVault() }
                .testTag("dashboard_vault_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardElevated),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyanAccent.copy(alpha = 0.15f))
                                .border(1.dp, CyanAccent, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isVaultUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = "Cofre Secreto",
                                tint = CyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "COFRE SECRETO MZ PRO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "Ocultar Fotos, SMS, Músicas e Apps",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isVaultUnlocked) EmeraldGreen.copy(alpha = 0.2f) else CrimsonRed.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isVaultUnlocked) "ABERTO" else "$totalVaultCount ITENS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isVaultUnlocked) EmeraldGreen else AmberWarning,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Proteja os seus dados privados com criptografia e código do dispositivo. Suporta modo disfarçado de calculadora secreta.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onOpenVault,
                    colors = ButtonDefaults.buttonColors(containerColor = if (isVaultUnlocked) EmeraldGreen else CardDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                ) {
                    Icon(
                        imageVector = if (isVaultUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isVaultUnlocked) DarkBg else CyanAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isVaultUnlocked) "GERIR COFRE ABERTO" else "DESBLOQUEAR COM CÓDIGO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isVaultUnlocked) DarkBg else CyanAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Core Modules Switches
        Text(
            text = "MÓDULOS DE PROTEÇÃO AUTOMÁTICA",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // 1. Anti-Força Bruta Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Anti-Força Bruta (3x PIN Errado)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "Tira foto do intruso, liga lanterna por 10s e toca sirene após 3 tentativas inválidas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = antiBruteForce,
                        onCheckedChange = { viewModel.setAntiBruteForce(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyanAccent,
                            checkedTrackColor = CyanAccent.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("switch_anti_brute_force")
                    )
                }

                Divider(color = BorderSubtle, modifier = Modifier.padding(vertical = 10.dp))

                // 2. SIM Watchdog Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SimCard, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SIM Watchdog (Troca de Chip)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "Envia SMS silencioso para contatos de emergência se o chip SIM for trocado por outro.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = simWatchdog,
                        onCheckedChange = { viewModel.setSimWatchdog(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyanAccent,
                            checkedTrackColor = CyanAccent.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("switch_sim_watchdog")
                    )
                }

                Divider(color = BorderSubtle, modifier = Modifier.padding(vertical = 10.dp))

                // 3. Modo Furtivo Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Modo Furtivo (Dispositivo Roubado)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "Executa comandos SMS sem emitir luz ou notificações ao ladrão.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = stealthMode,
                        onCheckedChange = { viewModel.setStealthMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyanAccent,
                            checkedTrackColor = CyanAccent.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("switch_stealth_mode")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showEditImeiDialog) {
        AlertDialog(
            onDismissRequest = { showEditImeiDialog = false },
            title = {
                Text(
                    text = "Confirmar / Editar IMEI Real",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Insira o número IMEI de 15 dígitos que consta na caixa do smartphone, na fatura ou digitando *#06# no teclado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editImeiInput,
                        onValueChange = { input ->
                            editImeiInput = input.filter { it.isDigit() }.take(15)
                        },
                        label = { Text("IMEI (15 Dígitos)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            focusedLabelColor = CyanAccent
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Dígitos digitados: ${editImeiInput.length}/15",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (editImeiInput.length == 15) EmeraldGreen else AmberWarning
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editImeiInput.length == 15) {
                            viewModel.updateDeviceImei(editImeiInput)
                            showEditImeiDialog = false
                            Toast.makeText(context, "IMEI de hardware salvo com sucesso!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "O IMEI deve conter exatamente 15 dígitos.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Salvar IMEI", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditImeiDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = CardElevated
        )
    }
}
