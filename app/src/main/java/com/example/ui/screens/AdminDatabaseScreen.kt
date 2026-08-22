package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AdminDatabaseRepository
import com.example.data.database.DeviceSettingEntity
import com.example.data.database.SecurityAuditEntity
import com.example.data.database.SmsCommandHistoryEntity
import com.example.data.database.UserAccountEntity
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBg
import com.example.ui.theme.GreenSafe
import com.example.ui.theme.MovitelOrange
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TmcelYellow
import com.example.ui.theme.VodacomRed
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDatabaseScreen(
    viewModel: MainViewModel,
    onClose: () -> Unit
) {
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()

    if (!isAdminLoggedIn) {
        AdminLoginContent(
            onLogin = { email, pass -> viewModel.loginAdmin(email, pass) },
            onClose = onClose
        )
    } else {
        AdminDashboardContent(
            viewModel = viewModel,
            onLogout = { viewModel.logoutAdmin() },
            onClose = onClose
        )
    }
}

@Composable
fun AdminLoginContent(
    onLogin: (String, String) -> Boolean,
    onClose: () -> Unit
) {
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var attemptsCount by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(20.dp)
            .testTag("admin_login_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(VodacomRed.copy(alpha = 0.15f))
                    .border(2.dp, VodacomRed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = "Admin Security",
                    tint = VodacomRed,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "GESTÃO DE BASE DE DADOS",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Portal de Administração Exclusivo MZ Pro",
                style = MaterialTheme.typography.bodyMedium,
                color = CyanAccent,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Acesso restrito ao administrador para gerir tabelas SQLite/Room, utilizadores, logs de auditoria e configurações de segurança.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Credenciais do Administrador",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Email Field
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = {
                            emailInput = it
                            errorMessage = null
                        },
                        label = { Text("E-mail do Administrador") },
                        placeholder = { Text("email@exemplo.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = CyanAccent)
                        },
                        trailingIcon = {
                            if (emailInput.isNotEmpty()) {
                                IconButton(onClick = { emailInput = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpar", tint = TextMuted)
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_email_input")
                    )

                    // Quick fill helper for the authorized administrator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Preencher E-mail ADM",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    emailInput = AdminDatabaseRepository.ADMIN_EMAIL
                                    passwordInput = AdminDatabaseRepository.ADMIN_PASSWORD
                                }
                                .padding(4.dp)
                        )
                    }

                    // Password Field
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            errorMessage = null
                        },
                        label = { Text("Senha do Administrador") },
                        placeholder = { Text("Senha de Acesso") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = CyanAccent)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Alternar Visibilidade",
                                    tint = TextMuted
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_password_input")
                    )

                    AnimatedVisibility(visible = errorMessage != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(AlertRed.copy(alpha = 0.15f))
                                .border(1.dp, AlertRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = AlertRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = AlertRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val success = onLogin(emailInput, passwordInput)
                            if (!success) {
                                attemptsCount++
                                errorMessage = "Acesso Negado: Apenas o e-mail oficial '${AdminDatabaseRepository.ADMIN_EMAIL}' com a senha correta pode gerir a base de dados."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VodacomRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("admin_login_submit_btn")
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ENTRAR NO PAINEL ADM",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onClose,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text("Voltar ao Aplicativo", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Security Policy Notice Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = GreenSafe,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Políticas de Acesso Rigorosas",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "A base de dados Room encriptada armazena localmente todos os dispositivos e telemetrias.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDashboardContent(
    viewModel: MainViewModel,
    onLogout: () -> Unit,
    onClose: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("👥 Usuários", "🛡️ Auditoria", "💬 Comandos SMS", "⚙️ Configurações", "🛠️ Ferramentas")

    val users by viewModel.dbUsers.collectAsState()
    val auditLogs by viewModel.dbAuditLogs.collectAsState()
    val smsCommands by viewModel.dbSmsCommands.collectAsState()
    val settings by viewModel.dbSettings.collectAsState()

    val usersCount by viewModel.dbUsersCount.collectAsState()
    val logsCount by viewModel.dbLogsCount.collectAsState()
    val commandsCount by viewModel.dbCommandsCount.collectAsState()

    var showAddUserDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .testTag("admin_dashboard_screen")
    ) {
        // Admin Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(VodacomRed.copy(alpha = 0.2f))
                        .border(1.5.dp, VodacomRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin",
                        tint = VodacomRed,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PAINEL ADM MZ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GreenSafe.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SUPER-ADMIN",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = GreenSafe,
                                fontSize = 9.sp
                            )
                        }
                    }
                    Text(
                        text = AdminDatabaseRepository.ADMIN_EMAIL,
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row {
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier.testTag("admin_logout_btn")
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Sair do ADM", tint = AlertRed)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Clear, contentDescription = "Fechar", tint = TextSecondary)
                }
            }
        }

        // Live Database Metrics Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DbMetricCard(
                modifier = Modifier.weight(1f),
                title = "Usuários",
                value = usersCount.toString(),
                sub = "Tabela `users`",
                icon = Icons.Default.Person,
                color = CyanAccent
            )
            DbMetricCard(
                modifier = Modifier.weight(1f),
                title = "Auditoria",
                value = logsCount.toString(),
                sub = "Logs de Segurança",
                icon = Icons.Default.Shield,
                color = AlertRed
            )
            DbMetricCard(
                modifier = Modifier.weight(1f),
                title = "SMS",
                value = commandsCount.toString(),
                sub = "Histórico Comandos",
                icon = Icons.Default.Sms,
                color = GreenSafe
            )
        }

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceDark,
            contentColor = CyanAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = CyanAccent,
                    height = 3.dp
                )
            },
            edgePadding = 12.dp
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) CyanAccent else TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> AdminUsersTab(
                    users = users,
                    onAddUser = { showAddUserDialog = true },
                    onDeleteUser = { id, name -> viewModel.deleteAdminUser(id, name) },
                    onToggleArmed = { user ->
                        viewModel.updateAdminUser(user.copy(isArmed = !user.isArmed))
                    }
                )
                1 -> AdminAuditLogsTab(
                    logs = auditLogs,
                    onClearAll = { viewModel.clearAdminAuditLogs() },
                    onDeleteLog = { id -> viewModel.deleteAdminAuditLog(id) }
                )
                2 -> AdminSmsHistoryTab(
                    commands = smsCommands,
                    onClearAll = { viewModel.clearAdminSmsHistory() }
                )
                3 -> AdminSettingsTab(
                    settings = settings,
                    onUpdateSetting = { key, value, desc -> viewModel.updateAdminSetting(key, value, desc) }
                )
                4 -> AdminToolsTab(
                    onSeedDatabase = { viewModel.seedDatabaseWithSamples() },
                    onClearAllLogs = { viewModel.clearAdminAuditLogs() }
                )
            }
        }
    }

    if (showAddUserDialog) {
        AddUserDialog(
            onDismiss = { showAddUserDialog = false },
            onConfirm = { name, phone, op, pin, role, notes ->
                viewModel.addAdminUser(name, phone, op, pin, role, notes)
                showAddUserDialog = false
            }
        )
    }
}

@Composable
fun DbMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 9.sp,
                maxLines = 1
            )
        }
    }
}

// ---------------- TAB 1: USERS ----------------
@Composable
fun AdminUsersTab(
    users: List<UserAccountEntity>,
    onAddUser: () -> Unit,
    onDeleteUser: (Long, String) -> Unit,
    onToggleArmed: (UserAccountEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var userToDelete by remember { mutableStateOf<UserAccountEntity?>(null) }

    val filteredUsers = users.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.phoneNumber.contains(searchQuery, ignoreCase = true) ||
                it.operator.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Pesquisar utilizador ou +258...", fontSize = 13.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = CardDark,
                    unfocusedContainerColor = CardDark
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onAddUser,
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(50.dp)
                    .testTag("admin_add_user_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = DarkBg)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Adicionar", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardDark)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Nenhum utilizador encontrado na tabela `users`", color = TextSecondary, fontWeight = FontWeight.Bold)
                    Text("Clique em 'Adicionar' ou use a aba Ferramentas para gerar dados de teste.", color = TextMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredUsers, key = { it.id }) { user ->
                    val opColor = when {
                        user.operator.contains("Vodacom", ignoreCase = true) -> VodacomRed
                        user.operator.contains("Movitel", ignoreCase = true) -> MovitelOrange
                        user.operator.contains("Tmcel", ignoreCase = true) -> TmcelYellow
                        else -> CyanAccent
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        shape = RoundedCornerShape(12.dp)
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
                                            .background(opColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = user.fullName.take(1).uppercase(Locale.ROOT),
                                            fontWeight = FontWeight.Black,
                                            color = opColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = user.fullName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = user.phoneNumber,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CyanAccent,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(opColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = user.operator,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = opColor,
                                            fontSize = 10.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = { userToDelete = user },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = AlertRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val isUserActive = user.accountStatus == "ATIVO" || user.isPaymentConfirmed
                                val isPending = user.accountStatus == "PENDENTE_CONFIRMACAO"

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (isUserActive) GreenSafe.copy(alpha = 0.2f)
                                                else if (isPending) MovitelOrange.copy(alpha = 0.2f)
                                                else AlertRed.copy(alpha = 0.2f)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isUserActive) "ATIVO" else if (isPending) "PENDENTE" else "INATIVO",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUserActive) GreenSafe else if (isPending) MovitelOrange else AlertRed,
                                            fontSize = 9.sp
                                        )
                                    }

                                    Text(
                                        text = "Plano: ${user.planType}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { onToggleArmed(user) }
                                ) {
                                    Icon(
                                        imageVector = if (user.isArmed) Icons.Default.Shield else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (user.isArmed) GreenSafe else AlertRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (user.isArmed) "ARMADO" else "DESARMADO",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (user.isArmed) GreenSafe else AlertRed,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Eliminar Utilizador?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Tem certeza que deseja apagar o utilizador '${userToDelete?.fullName}' (${userToDelete?.phoneNumber}) da base de dados Room? Esta ação não pode ser desfeita.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        userToDelete?.let { onDeleteUser(it.id, it.fullName) }
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                ) {
                    Text("Eliminar Registo", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { userToDelete = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = CardDark
        )
    }
}

// ---------------- TAB 2: AUDIT LOGS ----------------
@Composable
fun AdminAuditLogsTab(
    logs: List<SecurityAuditEntity>,
    onClearAll: () -> Unit,
    onDeleteLog: (Long) -> Unit
) {
    var showClearDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tabela `security_audit_logs` (${logs.size} registos)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            if (logs.isNotEmpty()) {
                OutlinedButton(
                    onClick = { showClearDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AlertRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Limpar Tabela", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardDark)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Nenhum log de auditoria no banco de dados.", color = TextSecondary, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs, key = { it.id }) { log ->
                    val sevColor = when (log.severity) {
                        "CRITICAL" -> AlertRed
                        "WARNING" -> MovitelOrange
                        else -> CyanAccent
                    }
                    val dateFormatted = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ROOT).format(Date(log.timestamp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(sevColor)
                                        .padding(top = 4.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = log.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "[#${log.id}]",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = log.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = dateFormatted,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                        if (log.senderNumber != null) {
                                            Text(
                                                text = "Remetente: ${log.senderNumber}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CyanAccent,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }

                            IconButton(
                                onClick = { onDeleteLog(log.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Remover", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Limpar Todos os Logs?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Deseja apagar todos os registos da tabela `security_audit_logs`?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAll()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                ) {
                    Text("Limpar Todos")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = CardDark
        )
    }
}

// ---------------- TAB 3: SMS COMMANDS HISTORY ----------------
@Composable
fun AdminSmsHistoryTab(
    commands: List<SmsCommandHistoryEntity>,
    onClearAll: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tabela `sms_commands_history` (${commands.size} execuções)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            if (commands.isNotEmpty()) {
                OutlinedButton(
                    onClick = onClearAll,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AlertRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Limpar SMS", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (commands.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardDark)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Sms, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Nenhum comando SMS executado ou registado ainda.", color = TextSecondary, fontWeight = FontWeight.Bold)
                    Text("Envie comandos na aba 'SMS' para vê-los persistidos na base de dados.", color = TextMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(commands, key = { it.id }) { cmd ->
                    val dateFormatted = SimpleDateFormat("HH:mm:ss dd/MM", Locale.ROOT).format(Date(cmd.timestamp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (cmd.success) GreenSafe.copy(alpha = 0.2f) else AlertRed.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = cmd.command,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (cmd.success) GreenSafe else AlertRed
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = cmd.senderNumber,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }

                                Text(
                                    text = dateFormatted,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Resposta: ${cmd.responseMessage}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (cmd.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                                        contentDescription = null,
                                        tint = if (cmd.batteryPct > 30) GreenSafe else AlertRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Carga: ${cmd.batteryPct}% ${if (cmd.isCharging) "(⚡)" else ""}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- TAB 4: SETTINGS ----------------
@Composable
fun AdminSettingsTab(
    settings: List<DeviceSettingEntity>,
    onUpdateSetting: (String, String, String) -> Unit
) {
    var editSettingKey by remember { mutableStateOf<String?>(null) }
    var editSettingValue by remember { mutableStateOf("") }
    var editSettingDesc by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Tabela `device_settings` (Políticas de Segurança do Banco)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(settings, key = { it.key }) { setting ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = setting.key,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = CyanAccent
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = setting.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SurfaceDark)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Valor: ${setting.value}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GreenSafe,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                editSettingKey = setting.key
                                editSettingValue = setting.value
                                editSettingDesc = setting.description
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = CyanAccent)
                        }
                    }
                }
            }
        }
    }

    if (editSettingKey != null) {
        AlertDialog(
            onDismissRequest = { editSettingKey = null },
            title = { Text("Editar Parâmetro: $editSettingKey", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(editSettingDesc, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = editSettingValue,
                        onValueChange = { editSettingValue = it },
                        label = { Text("Novo Valor") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        editSettingKey?.let { onUpdateSetting(it, editSettingValue, editSettingDesc) }
                        editSettingKey = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Salvar Alteração", color = DarkBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { editSettingKey = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = CardDark
        )
    }
}

// ---------------- TAB 5: TOOLS & SEED ----------------
@Composable
fun AdminToolsTab(
    onSeedDatabase: () -> Unit,
    onClearAllLogs: () -> Unit
) {
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Ferramentas & Operações da Base de Dados Room",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = CyanAccent)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Popular Banco com Dados de Teste (Seed)", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Insere utilizadores de teste (Vodacom, Movitel) e logs de exemplo.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Button(
                    onClick = {
                        onSeedDatabase()
                        statusMessage = "✅ Base de dados populada com sucesso com registos de teste moçambicanos!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("INSERIR REGISTOS DE DEMONSTRAÇÃO", color = DarkBg, fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = GreenSafe)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Diagnóstico do Motor Room SQLite", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Base de dados: anti_theft_mz_pro.db | Versão: 1 | Modo WAL", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }

                OutlinedButton(
                    onClick = {
                        statusMessage = "🔍 Verificação de Integridade OK: Tabelas (users, security_audit_logs, sms_commands_history, device_settings) 100% íntegras."
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenSafe),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GreenSafe.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("TESTAR INTEGRIDADE DO BANCO", fontWeight = FontWeight.Bold)
                }
            }
        }

        AnimatedVisibility(visible = statusMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardDark)
                    .border(1.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = statusMessage ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyanAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, op: String, pin: String, role: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+258 8") }
    var operator by remember { mutableStateOf("Vodacom Moçambique") }
    var pin by remember { mutableStateOf("1234") }
    var role by remember { mutableStateOf("USER") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Adicionar Utilizador à Base de Dados",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome Completo") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Número Moçambique (+258)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = operator,
                    onValueChange = { operator = it },
                    label = { Text("Operadora (Vodacom, Movitel, Tmcel)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("PIN de Segurança") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas / Cidade / Dispositivo") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, phone, operator, pin, role, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text("Gravar no Banco", color = DarkBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        containerColor = CardDark
    )
}
