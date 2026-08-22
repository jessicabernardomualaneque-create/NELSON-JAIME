package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ActivationCodeEntity
import com.example.data.database.UserAccountEntity
import com.example.security.SubscriptionManager
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CardDark
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBg
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MovitelOrange
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VodacomRed
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Painel do Administrador Oficial (869705734) - AdminDashboardScreen
 *
 * Exclusivo para o telefone 869705734 com PIN 867884.
 * Funções:
 * 1. Gerar Códigos de Ativação (Diário 7MT | Semanal 25MT | Mensal 80MT)
 * 2. Ativação Manual de Usuários (Telefone + Plano)
 * 3. Lista de Códigos Gerados (Pendente / Usado)
 * 4. Ver Usuários Ativos na Base de Dados SQLite
 */
@Composable
fun AdminDashboardScreen(
    viewModel: MainViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val isAdmin by viewModel.isSubAdmin.collectAsState()
    val isAppAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()

    val users by viewModel.dbUsers.collectAsState()
    val pendingUsers by viewModel.dbPendingUsers.collectAsState()
    val pendingCount by viewModel.dbPendingCount.collectAsState()
    val activationCodes by viewModel.dbActivationCodes.collectAsState()
    val codesCount by viewModel.dbCodesCount.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog state for Generated Code
    var showGeneratedCodeDialog by remember { mutableStateOf(false) }
    var lastGeneratedCode by remember { mutableStateOf<ActivationCodeEntity?>(null) }

    // Dialog state for Manual User Activation
    var showManualActivationDialog by remember { mutableStateOf(false) }
    var targetPhoneInput by remember { mutableStateOf("") }
    var selectedPlanDays by remember { mutableIntStateOf(7) }
    var selectedPlanName by remember { mutableStateOf("SEMANAL") }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .testTag("admin_dashboard_screen")
    ) {
        // Top Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(VodacomRed.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = VodacomRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Painel do Administrador",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "869705734 • Acesso Ilimitado Vitalício",
                                fontSize = 11.sp,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(EmeraldGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("ILIMITADO", fontSize = 9.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons Row: [Gerar Código] [Ativar Manual] [Ver Usuários]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.gerarCodigoAtivacao("SEMANAL") { codeEntity ->
                                lastGeneratedCode = codeEntity
                                showGeneratedCodeDialog = true
                                copyToClipboard(codeEntity.code, "Código de Ativação")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("admin_btn_generate_code"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gerar Código", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showManualActivationDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("admin_btn_manual_activation"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VodacomRed,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ativar Manual", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Tabs Row: [Códigos Gerados (X)] [Usuários Registados (Y)] [Pendentes (Z)] [Ferramentas de Teste]
        val tabs = listOf(
            "Códigos (${activationCodes.size})",
            "Utilizadores (${users.size})",
            "Pendentes Pagamento (${pendingUsers.size})",
            "Testes de Trial"
        )

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceDark,
            contentColor = CyanAccent,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = CyanAccent,
                    height = 2.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) CyanAccent else TextTertiary
                        )
                    }
                )
            }
        }

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> {
                    // TAB 0: Lista de Códigos de Ativação
                    if (activationCodes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.VpnKey,
                                    contentDescription = null,
                                    tint = TextTertiary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Nenhum código gerado ainda",
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Clique em 'Gerar Código' acima para criar códigos tipo MZ-XXXX-XXXX para os utilizadores pagantes.",
                                    fontSize = 11.sp,
                                    color = TextTertiary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ÚLTIMOS CÓDIGOS GERADOS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        CodeQuickGenBadge("7 MT (1d)") {
                                            viewModel.gerarCodigoAtivacao("DIARIO") {
                                                lastGeneratedCode = it
                                                showGeneratedCodeDialog = true
                                                copyToClipboard(it.code, "Código Diário")
                                            }
                                        }
                                        CodeQuickGenBadge("25 MT (7d)") {
                                            viewModel.gerarCodigoAtivacao("SEMANAL") {
                                                lastGeneratedCode = it
                                                showGeneratedCodeDialog = true
                                                copyToClipboard(it.code, "Código Semanal")
                                            }
                                        }
                                        CodeQuickGenBadge("80 MT (30d)") {
                                            viewModel.gerarCodigoAtivacao("MENSAL") {
                                                lastGeneratedCode = it
                                                showGeneratedCodeDialog = true
                                                copyToClipboard(it.code, "Código Mensal")
                                            }
                                        }
                                    }
                                }
                            }

                            items(activationCodes, key = { it.id }) { codeEntity ->
                                ActivationCodeItemCard(
                                    codeEntity = codeEntity,
                                    onCopy = { copyToClipboard(codeEntity.code, "Código de Ativação") },
                                    onDelete = { viewModel.deleteActivationCode(codeEntity.id) }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: Lista de Todos os Utilizadores
                    if (users.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = null,
                                    tint = TextTertiary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Nenhum usuário registrado na base de dados",
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Text(
                                    text = "UTILIZADORES REGISTADOS NO SISTEMA MZ (${users.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                            }

                            items(users, key = { it.id }) { user ->
                                AdminUserCard(
                                    user = user,
                                    onActivate = {
                                        targetPhoneInput = user.phoneNumber
                                        showManualActivationDialog = true
                                    }
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: Pagamentos Pendentes de Confirmação
                    if (pendingUsers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Nenhum pagamento pendente!",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Todos os pagamentos foram confirmados e as contas dos utilizadores estão ATIVAS.",
                                    fontSize = 12.sp,
                                    color = TextTertiary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Text(
                                    text = "AGUARDANDO CONFIRMAÇÃO DO ADMIN (${pendingUsers.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MovitelOrange
                                )
                            }

                            items(pendingUsers, key = { it.id }) { user ->
                                AdminUserCard(
                                    user = user,
                                    onActivate = {
                                        targetPhoneInput = user.phoneNumber
                                        selectedPlanName = user.planType
                                        selectedPlanDays = when (user.planType) {
                                            "DIARIO" -> 1
                                            "MENSAL" -> 30
                                            else -> 7
                                        }
                                        showManualActivationDialog = true
                                    }
                                )
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: Ferramentas de Teste e Validação
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            border = BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "SIMULAÇÃO & TESTES DE SEGURANÇA",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Use os controlos abaixo para testar o comportamento do aplicativo no final do período de 7 dias grátis:",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        viewModel.simularFimTrial()
                                        Toast.makeText(context, "Trial expirado simulado! O app agora exigirá pagamento.", Toast.LENGTH_LONG).show()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CrimsonRed.copy(alpha = 0.25f),
                                        contentColor = CrimsonRed
                                    )
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Simular Fim do Trial (7 Dias Expirados)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        viewModel.resetarTrial()
                                        Toast.makeText(context, "Trial restaurado para 7 dias grátis!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldGreen.copy(alpha = 0.25f),
                                        contentColor = EmeraldGreen
                                    )
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Restaurar Trial 7 Dias Grátis", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        viewModel.ativarCodigo("MZ-PRO-2026") { _, msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CyanAccent.copy(alpha = 0.25f),
                                        contentColor = CyanAccent
                                    )
                                ) {
                                    Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Ativar Licença Mensal de Demonstração (30d)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Generated Code Result
    if (showGeneratedCodeDialog && lastGeneratedCode != null) {
        val code = lastGeneratedCode!!
        AlertDialog(
            onDismissRequest = { showGeneratedCodeDialog = false },
            containerColor = CardDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Código Gerado com Sucesso", fontSize = 16.sp, color = TextPrimary)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Envie este código ao cliente após confirmação de pagamento para 869705734:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceDark, RoundedCornerShape(8.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = code.code,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CyanAccent,
                            letterSpacing = 2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Plano: ${code.planType} (${code.durationDays} dias) • Valor: ${code.priceMt} MT",
                        fontSize = 11.sp,
                        color = TextTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        copyToClipboard(code.code, "Código de Ativação")
                        showGeneratedCodeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar e Fechar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Dialog: Confirm Payment & User Activation
    if (showManualActivationDialog) {
        AlertDialog(
            onDismissRequest = { showManualActivationDialog = false },
            containerColor = CardDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirmar Pagamento & Ativar", fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Ao confirmar o pagamento, a conta do utilizador ficará imediatamente ATIVA no sistema:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = targetPhoneInput,
                        onValueChange = { targetPhoneInput = it },
                        label = { Text("Número do Utilizador (+258)", color = TextTertiary) },
                        placeholder = { Text("84/85/86/87/82...", color = TextTertiary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Selecione o Plano Confirmado:", fontSize = 11.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PlanSelectButton("Diário (7 MT)", selectedPlanDays == 1) {
                            selectedPlanDays = 1
                            selectedPlanName = "DIARIO"
                        }
                        PlanSelectButton("Semanal (25 MT)", selectedPlanDays == 7) {
                            selectedPlanDays = 7
                            selectedPlanName = "SEMANAL"
                        }
                        PlanSelectButton("Mensal (80 MT)", selectedPlanDays == 30) {
                            selectedPlanDays = 30
                            selectedPlanName = "MENSAL"
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PlanSelectButton("Vitalício / Ilimitado", selectedPlanDays == 3650) {
                            selectedPlanDays = 3650
                            selectedPlanName = "ILIMITADO"
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (targetPhoneInput.isNotBlank()) {
                            viewModel.confirmUserPayment(targetPhoneInput, selectedPlanDays, selectedPlanName) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                showManualActivationDialog = false
                                targetPhoneInput = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Confirmar & Ativar Conta", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showManualActivationDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Text("Cancelar", fontSize = 12.sp)
                }
            }
        )
    }
}

@Composable
private fun CodeQuickGenBadge(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(SurfaceDark, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(label, fontSize = 10.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PlanSelectButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) CyanAccent else SurfaceDark,
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.Black else TextSecondary
        )
    }
}

@Composable
private fun ActivationCodeItemCard(
    codeEntity: ActivationCodeEntity,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    val isUsed = codeEntity.status == "USADO"
    val dateStr = SimpleDateFormat("dd/MM HH:mm", Locale("pt", "MZ")).format(Date(codeEntity.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("activation_code_item_${codeEntity.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, if (isUsed) BorderSubtle else CyanAccent.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = codeEntity.code,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (isUsed) TextTertiary else CyanAccent
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .background(
                                if (isUsed) SurfaceDark else EmeraldGreen.copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isUsed) "USADO" else "PENDENTE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUsed) TextTertiary else EmeraldGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Plano: ${codeEntity.planType} (${codeEntity.durationDays}d) • ${codeEntity.priceMt} MT • Gerado: $dateStr",
                    fontSize = 10.sp,
                    color = TextSecondary
                )

                if (isUsed && codeEntity.usedByPhone != null) {
                    Text(
                        text = "Utilizado por: ${codeEntity.usedByPhone}",
                        fontSize = 10.sp,
                        color = TextTertiary
                    )
                }
            }

            Row {
                IconButton(onClick = onCopy, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar Código",
                        tint = CyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remover Código",
                        tint = CrimsonRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminUserCard(
    user: UserAccountEntity,
    onActivate: () -> Unit
) {
    val isUserActive = user.accountStatus == "ATIVO" || user.isPaymentConfirmed
    val isPending = user.accountStatus == "PENDENTE_CONFIRMACAO"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(
            1.dp,
            if (isUserActive) EmeraldGreen.copy(alpha = 0.5f) else if (isPending) MovitelOrange.copy(alpha = 0.6f) else BorderSubtle
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.fullName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        if (user.role == "ADMIN") {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(VodacomRed.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text("ADMIN", fontSize = 8.sp, color = VodacomRed, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Status Badge
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isUserActive) EmeraldGreen.copy(alpha = 0.2f)
                                    else if (isPending) MovitelOrange.copy(alpha = 0.2f)
                                    else TextTertiary.copy(alpha = 0.2f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isUserActive) "CONTA ATIVA" else if (isPending) "PAGAMENTO PENDENTE" else "TRIAL / INATIVO",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUserActive) EmeraldGreen else if (isPending) MovitelOrange else TextTertiary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${user.phoneNumber} • ${user.operator}",
                        fontSize = 11.sp,
                        color = CyanAccent
                    )

                    Text(
                        text = "Plano: ${user.planType} • PIN: ${user.pin} • Bateria: ${user.batteryPct}%",
                        fontSize = 10.sp,
                        color = TextTertiary
                    )

                    if (user.isPaymentConfirmed && user.paymentConfirmedBy != null) {
                        Text(
                            text = "✅ Pagamento validado pelo Admin (${user.paymentConfirmedBy})",
                            fontSize = 10.sp,
                            color = EmeraldGreen
                        )
                    }
                }

                Button(
                    onClick = onActivate,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPending) EmeraldGreen else SurfaceDark,
                        contentColor = if (isPending) Color.Black else CyanAccent
                    ),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = if (isPending) "Confirmar" else if (isUserActive) "Renovar" else "Ativar",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
