package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.VodacomRed
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onRequestAdmin: () -> Unit,
    onOpenAdminPortal: () -> Unit = {},
    onOpenPayment: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentPin by viewModel.securityPin.collectAsState()
    val ownerEmail by viewModel.ownerEmail.collectAsState()
    val deviceStatus by viewModel.deviceStatus.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val isSubAdmin by viewModel.isSubAdmin.collectAsState()
    val isTrial by viewModel.isTrial.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val diasRestantes by viewModel.diasRestantes.collectAsState()
    val tipoPlano by viewModel.tipoPlano.collectAsState()
    val expiryFormatted by viewModel.expiryDateFormatted.collectAsState()
    val usersCount by viewModel.dbUsersCount.collectAsState()
    val logsCount by viewModel.dbLogsCount.collectAsState()

    var newPinInput by remember(currentPin) { mutableStateOf(currentPin) }
    var emailInput by remember(ownerEmail) { mutableStateOf(ownerEmail) }
    var pinMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "CONFIGURAÇÕES DE SEGURANÇA",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            letterSpacing = 1.sp
        )
        Text(
            text = "Parâmetros de autenticação e proteção do dispositivo",
            style = MaterialTheme.typography.bodySmall,
            color = CyanAccent
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dedicated Admin & Database Management Portal Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_portal_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardElevated),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isAdminLoggedIn) EmeraldGreen else CrimsonRed)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(CrimsonRed.copy(alpha = 0.2f))
                                .border(1.5.dp, CrimsonRed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Security",
                                tint = CrimsonRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Gestor da Base de Dados (ADM)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isAdminLoggedIn) EmeraldGreen.copy(alpha = 0.2f) else AmberWarning.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isAdminLoggedIn) "SESSÃO ADM ATIVA" else "RESTRO ADM",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAdminLoggedIn) EmeraldGreen else AmberWarning,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                            Text(
                                text = "Acesso exclusivo: jessicabernardomualaneque@gmail.com",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyanAccent,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Permite ao administrador gerir utilizadores registados, inspecionar logs de auditoria no SQLite/Room, configurar parâmetros de segurança e auditar comandos SMS.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Stats preview
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBg)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Base de Dados", color = TextTertiary, fontSize = 10.sp)
                        Text("Room SQLite", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Usuários DB", color = TextTertiary, fontSize = 10.sp)
                        Text("$usersCount", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Logs Gravados", color = TextTertiary, fontSize = 10.sp)
                        Text("$logsCount", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onOpenAdminPortal,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("open_admin_portal_btn")
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAdminLoggedIn) "ABRIR GESTOR DO BANCO (ADM)" else "AUTENTICAR COMO ADM & GERIR DB",
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mozambique Registered Account Card
        if (userProfile != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_profile_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(CyanAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(26.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = userProfile?.fullName ?: "Proprietário",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = userProfile?.phoneNumber ?: "+258",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CyanAccent
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EmeraldGreen.copy(alpha = 0.2f))
                                .border(1.dp, EmeraldGreen, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "MZ VERIFICADO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBg)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CellTower, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Operadora: ${userProfile?.operator?.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.logout()
                                Toast.makeText(context, "Sessão encerrada", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(32.dp).testTag("logout_button")
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Trocar Conta", fontSize = 11.sp, color = CrimsonRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Subscription & Licensing Management Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                when {
                    isSubAdmin -> VodacomRed
                    isPremium -> EmeraldGreen
                    isTrial -> CyanAccent
                    else -> CrimsonRed
                }
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = when {
                                isSubAdmin -> VodacomRed
                                isPremium -> EmeraldGreen
                                isTrial -> CyanAccent
                                else -> CrimsonRed
                            },
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Licenciamento & Planos MZ",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = when {
                                    isSubAdmin -> "Acesso Admin Ilimitado Vitalício"
                                    isPremium -> "Plano Ativo: $tipoPlano"
                                    isTrial -> "Trial Grátis: $diasRestantes dias restantes"
                                    else -> "Trial Expirado • Bloqueado"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = when {
                                    isSubAdmin -> VodacomRed
                                    isPremium -> EmeraldGreen
                                    isTrial -> CyanAccent
                                    else -> CrimsonRed
                                },
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = when {
                        isSubAdmin -> "Telefone Administrador: 869705734 com permissão master permanente."
                        isPremium -> "Sua proteção anti-roubo está 100% ativa até $expiryFormatted."
                        isTrial -> "Você possui $diasRestantes dias de teste grátis com todas as funcionalidades ativas."
                        else -> "Efetue o pagamento via M-Pesa / E-mola para o número 869705734 para reativar."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isSubAdmin) {
                        Button(
                            onClick = onOpenAdminPortal,
                            colors = ButtonDefaults.buttonColors(containerColor = VodacomRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gerenciar Códigos", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onOpenPayment,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ver Planos e Pagar", color = DarkBg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Device Admin Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (deviceStatus.isAdminActive) EmeraldGreen else AmberWarning
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = if (deviceStatus.isAdminActive) EmeraldGreen else AmberWarning,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Administrador do Dispositivo",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (deviceStatus.isAdminActive) "Ativo - Impede desinstalação sem senha" else "Pendente - Toque para conceder",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (deviceStatus.isAdminActive) EmeraldGreen else AmberWarning
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "A permissão de Administrador do Dispositivo permite travar a tela remotamente via SMS ('BLOQUEAR') e registrar tentativas de invasão por PIN incorreto.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                if (!deviceStatus.isAdminActive) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onRequestAdmin,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberWarning, contentColor = DarkBg),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("activate_admin_button")
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ATIVAR ADMINISTRADOR DO DISPOSITIVO", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Change PIN Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "PIN de Segurança dos Comandos SMS",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Este PIN é exigido em todas as mensagens SMS (ex: LOCALIZAR $currentPin). Guarde-o com segurança.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newPinInput,
                    onValueChange = {
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            newPinInput = it
                        }
                    },
                    label = { Text("Novo PIN (4 a 6 dígitos)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("settings_pin_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (newPinInput.length >= 4) {
                            viewModel.updatePin(newPinInput)
                            pinMessage = "PIN atualizado para $newPinInput com sucesso!"
                            Toast.makeText(context, "PIN alterado com sucesso!", Toast.LENGTH_SHORT).show()
                        } else {
                            pinMessage = "O PIN deve ter no mínimo 4 dígitos."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("save_pin_button")
                ) {
                    Text("Salvar Novo PIN", color = DarkBg, fontWeight = FontWeight.Bold)
                }

                if (pinMessage != null) {
                    Text(
                        text = pinMessage ?: "",
                        color = if (newPinInput.length >= 4) EmeraldGreen else CrimsonRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Owner Email Backup
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Email do Proprietário (Opcional)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Utilizado para envio de relatórios e backup de evidências caso haja conexão com a internet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    placeholder = { Text("seu-email@exemplo.com") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("owner_email_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        viewModel.updateOwnerEmail(emailInput)
                        Toast.makeText(context, "Email salvo!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CardElevated),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Email", color = TextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legal Compliance Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Conformidade Legal e Privacidade",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "O Anti-Roubo MZ Pro foi desenvolvido exclusivamente para a proteção e rastreamento de aparelhos de sua legítima propriedade. Não utilize para fins de espionagem não autorizada. Todos os dados são processados localmente e criptografados.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
