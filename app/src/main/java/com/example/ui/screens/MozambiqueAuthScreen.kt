package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MozambiqueOperator
import com.example.data.model.MozambiquePhoneHelper
import com.example.data.repository.SecurityPreferences
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CardDark
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBg
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun MozambiqueAuthScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { SecurityPreferences(context) }

    // Tab 0 = Entrar (Login), Tab 1 = Criar Conta (Registo)
    var selectedTab by remember { mutableIntStateOf(0) }

    // Login Form State
    var loginPhone by remember { mutableStateOf("") }
    var loginPin by remember { mutableStateOf("") }
    var rememberCredentials by remember { mutableStateOf(true) }

    // Register Form State
    var registerFullName by remember { mutableStateOf("") }
    var registerPhone by remember { mutableStateOf("") }
    var registerPin by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(true) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Auto-fill from remembered memory on startup
    LaunchedEffect(Unit) {
        val rememberedPhone = prefs.getRememberedPhone()
        val rememberedPin = prefs.getRememberedPin()
        if (rememberedPhone.isNotBlank()) {
            val national = MozambiquePhoneHelper.getNationalDigits(rememberedPhone)
            loginPhone = national
            loginPin = rememberedPin
        }
    }

    // Detect operators
    val activePhoneInput = if (selectedTab == 0) loginPhone else registerPhone
    val currentOperator = MozambiquePhoneHelper.detectOperator(activePhoneInput)
    val nationalDigits = MozambiquePhoneHelper.getNationalDigits(activePhoneInput)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Brand Security Shield
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(CyanAccent.copy(alpha = 0.15f))
                .border(1.5.dp, CyanAccent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = CyanAccent,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "ANTI-ROUBO MZ PRO",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            letterSpacing = 1.sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(EmeraldGreen.copy(alpha = 0.2f))
                    .border(1.dp, EmeraldGreen, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "PROTEÇÃO ANTI-FURTO MOÇAMBIQUE (+258)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Two Main Tabs: ENTRAR (LOGIN) vs CRIAR CONTA
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CardDark,
            contentColor = CyanAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = CyanAccent,
                    height = 3.dp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = {
                    selectedTab = 0
                    errorMessage = null
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (selectedTab == 0) CyanAccent else TextSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ENTRAR (LOGIN)",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) CyanAccent else TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                },
                modifier = Modifier.testTag("tab_login")
            )

            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                    errorMessage = null
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (selectedTab == 1) CyanAccent else TextSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CRIAR CONTA",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) CyanAccent else TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                },
                modifier = Modifier.testTag("tab_register")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Auth Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {

                // TAB 0: ENTRAR (LOGIN COM NÚMERO E PIN)
                if (selectedTab == 0) {
                    Text(
                        text = "Entrar com Número e PIN",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Se já possui conta registada, insira o seu número de Moçambique e PIN.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    // Saved Memory Notification
                    if (prefs.getRememberedPhone().isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyanAccent.copy(alpha = 0.1f))
                                .border(1.dp, CyanAccent.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .clickable {
                                    loginPhone = MozambiquePhoneHelper.getNationalDigits(prefs.getRememberedPhone())
                                    loginPin = prefs.getRememberedPin()
                                }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Conta Guardada na Memória:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "${prefs.getRememberedPhone()} (Toque para preencher)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanAccent
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Phone input
                    OutlinedTextField(
                        value = loginPhone,
                        onValueChange = { input ->
                            val clean = input.replace("[^0-9]".toRegex(), "")
                            if (clean.length <= 9) {
                                loginPhone = clean
                                errorMessage = null
                            }
                        },
                        label = { Text("Número de Telemóvel (+258)") },
                        prefix = {
                            Text(
                                text = "+258 ",
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                fontSize = 16.sp
                            )
                        },
                        placeholder = { Text("84 123 4567") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = CyanAccent)
                        },
                        trailingIcon = {
                            if (currentOperator != MozambiqueOperator.UNKNOWN) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Operadora Válida",
                                    tint = EmeraldGreen
                                )
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = CyanAccent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_phone_input")
                    )

                    // Operator detector badge
                    Spacer(modifier = Modifier.height(8.dp))
                    OperatorFeedback(currentOperator, nationalDigits)

                    Spacer(modifier = Modifier.height(14.dp))

                    // PIN input
                    OutlinedTextField(
                        value = loginPin,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                loginPin = it
                                errorMessage = null
                            }
                        },
                        label = { Text("PIN de Segurança (4 a 6 dígitos)") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = CyanAccent)
                        },
                        placeholder = { Text("Ex: 1234") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = CyanAccent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_pin_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Remember credentials checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { rememberCredentials = !rememberCredentials },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberCredentials,
                            onCheckedChange = { rememberCredentials = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = CyanAccent,
                                uncheckedColor = TextSecondary,
                                checkmarkColor = DarkBg
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Guardar número e PIN na memória deste telemóvel",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = CrimsonRed,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Login Button
                    Button(
                        onClick = {
                            val validation = MozambiquePhoneHelper.validate(loginPhone)
                            if (!validation.isValid) {
                                errorMessage = validation.errorMessage ?: "Número inválido para Moçambique."
                            } else if (loginPin.length < 4) {
                                errorMessage = "O PIN de segurança deve ter pelo menos 4 dígitos."
                            } else {
                                isSubmitting = true
                                errorMessage = null
                                viewModel.loginMozambiqueAccount(
                                    rawPhone = loginPhone,
                                    pin = loginPin
                                ) { success, err ->
                                    isSubmitting = false
                                    if (success) {
                                        if (rememberCredentials) {
                                            prefs.saveRememberedCredentials(validation.formattedNumber, loginPin)
                                        }
                                        Toast.makeText(
                                            context,
                                            "Bem-vindo! Proteção Anti-Roubo MZ Ativa.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        errorMessage = err
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_submit_button")
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null, tint = DarkBg)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSubmitting) "ENTRANDO..." else "ENTRAR COM NÚMERO E PIN",
                            color = DarkBg,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Não tem conta ainda? Toque na aba 'CRIAR CONTA' acima.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        modifier = Modifier.clickable { selectedTab = 1 }
                    )
                }

                // TAB 1: CRIAR CONTA (REGISTO)
                if (selectedTab == 1) {
                    Text(
                        text = "Registo de Novo Proprietário",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Crie a sua conta para monitoramento 24h e rastreio por SMS em Moçambique.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanAccent
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Full Name Field
                    OutlinedTextField(
                        value = registerFullName,
                        onValueChange = {
                            registerFullName = it
                            errorMessage = null
                        },
                        label = { Text("Nome Completo do Proprietário") },
                        placeholder = { Text("Ex: Jéssica Bernardo") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = CyanAccent)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = CyanAccent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_name_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Phone Field
                    OutlinedTextField(
                        value = registerPhone,
                        onValueChange = { input ->
                            val clean = input.replace("[^0-9]".toRegex(), "")
                            if (clean.length <= 9) {
                                registerPhone = clean
                                errorMessage = null
                            }
                        },
                        label = { Text("Número de Telemóvel (+258)") },
                        prefix = {
                            Text(
                                text = "+258 ",
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                fontSize = 16.sp
                            )
                        },
                        placeholder = { Text("84 123 4567") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = CyanAccent)
                        },
                        trailingIcon = {
                            if (currentOperator != MozambiqueOperator.UNKNOWN) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Operadora Válida",
                                    tint = EmeraldGreen
                                )
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = CyanAccent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_phone_input")
                    )

                    // Operator badge
                    Spacer(modifier = Modifier.height(8.dp))
                    OperatorFeedback(currentOperator, nationalDigits)

                    Spacer(modifier = Modifier.height(14.dp))

                    // PIN Field
                    OutlinedTextField(
                        value = registerPin,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                registerPin = it
                                errorMessage = null
                            }
                        },
                        label = { Text("Criar PIN de Segurança SMS (4 a 6 dígitos)") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = CyanAccent)
                        },
                        placeholder = { Text("Ex: 1234") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = CyanAccent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_pin_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Legal Terms Acceptance Checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { termsAccepted = !termsAccepted },
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = { termsAccepted = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = CyanAccent,
                                uncheckedColor = TextSecondary,
                                checkmarkColor = DarkBg
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Declaro ser o legítimo proprietário deste dispositivo em Moçambique e autorizo o rastreio remoto por comandos SMS.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = CrimsonRed,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Register Button
                    Button(
                        onClick = {
                            val validation = MozambiquePhoneHelper.validate(registerPhone)
                            if (registerFullName.isBlank() || registerFullName.trim().length < 3) {
                                errorMessage = "Por favor, insira o seu nome completo."
                            } else if (!validation.isValid) {
                                errorMessage = validation.errorMessage ?: "Número inválido para Moçambique."
                            } else if (registerPin.length < 4) {
                                errorMessage = "O PIN de segurança deve ter pelo menos 4 dígitos."
                            } else if (!termsAccepted) {
                                errorMessage = "Você deve concordar com os termos para ativar a proteção."
                            } else {
                                isSubmitting = true
                                errorMessage = null
                                viewModel.registerMozambiqueAccount(
                                    fullName = registerFullName,
                                    rawPhone = registerPhone,
                                    pin = registerPin
                                ) { success, err ->
                                    isSubmitting = false
                                    if (success) {
                                        prefs.saveRememberedCredentials(validation.formattedNumber, registerPin, registerFullName)
                                        Toast.makeText(
                                            context,
                                            "Conta Moçambique criada e ativada automaticamente!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        errorMessage = err
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("register_submit_button")
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = DarkBg)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSubmitting) "REGISTANDO..." else "CRIAR CONTA & ATIVAR",
                            color = DarkBg,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Já possui uma conta? Toque na aba 'ENTRAR (LOGIN)' acima.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        modifier = Modifier.clickable { selectedTab = 0 }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Security Notice Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Os dados do utilizador e PIN são guardados na memória segura do telemóvel e na base de dados encriptada Room.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun OperatorFeedback(currentOperator: MozambiqueOperator, nationalDigits: String) {
    when (currentOperator) {
        MozambiqueOperator.VODACOM -> {
            OperatorBadge(
                name = "Vodacom Moçambique (84 / 85)",
                color = Color(0xFFE60000),
                icon = Icons.Default.CellTower
            )
        }
        MozambiqueOperator.MOVITEL -> {
            OperatorBadge(
                name = "Movitel Moçambique (86 / 87)",
                color = Color(0xFFFF6F00),
                icon = Icons.Default.CellTower
            )
        }
        MozambiqueOperator.TMCEL -> {
            OperatorBadge(
                name = "Tmcel Moçambique (82 / 83)",
                color = Color(0xFF007ACC),
                icon = Icons.Default.CellTower
            )
        }
        MozambiqueOperator.UNKNOWN -> {
            if (nationalDigits.length >= 2) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Apenas operadoras MZ: Vodacom (84/85), Movitel (86/87), Tmcel (82/83)",
                        style = MaterialTheme.typography.labelSmall,
                        color = CrimsonRed
                    )
                }
            } else {
                Text(
                    text = "Prefixos válidos: 84, 85 (Vodacom) • 86, 87 (Movitel) • 82, 83 (Tmcel)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }
    }
}

@Composable
fun OperatorBadge(
    name: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Operadora: $name",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
