package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

/**
 * Tela de Pagamento & Ativação de Licença Anti-Roubo MZ Pro (PagamentoActivity / PaymentScreen)
 *
 * Exibida quando o período de trial de 7 dias acaba ou quando o usuário desejar renovar o plano.
 * Pagamento manual via M-Pesa / E-mola para o número do Administrador: 869705734
 */
@Composable
fun PaymentScreen(
    viewModel: MainViewModel,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val isTrial by viewModel.isTrial.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val isAdmin by viewModel.isSubAdmin.collectAsState()
    val diasRestantes by viewModel.diasRestantes.collectAsState()
    val tipoPlano by viewModel.tipoPlano.collectAsState()
    val expiryFormatted by viewModel.expiryDateFormatted.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.syncLocalSubscriptionWithDbStatus()
    }

    var activationCodeInput by remember { mutableStateOf("") }
    var selectedPlanForInfo by remember { mutableStateOf("SEMANAL") }
    var isSubmitting by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessFeedback by remember { mutableStateOf(false) }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copiado!", Toast.LENGTH_SHORT).show()
    }

    fun openWhatsAppOrSms() {
        viewModel.requestPaymentConfirmation(selectedPlanForInfo, "Utilizador notificou pagamento") { _, infoMsg ->
            Toast.makeText(context, infoMsg, Toast.LENGTH_LONG).show()
        }
        try {
            val msg = "Olá Admin (869705734), confirmei o pagamento do Anti-Roubo MZ Pro. Plano: $selectedPlanForInfo. Por favor ative a minha conta."
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/258869705734?text=${Uri.encode(msg)}")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to SMS
            try {
                val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:869705734")
                    putExtra("sms_body", "Olá Admin, confirmei o pagamento do plano $selectedPlanForInfo. Ative minha conta Anti-Roubo MZ Pro.")
                }
                context.startActivity(smsIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Contacte o número 869705734", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .testTag("payment_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Header Icon Badge
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        if (isPremium) EmeraldGreen.copy(alpha = 0.15f) else CrimsonRed.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPremium) Icons.Default.Shield else Icons.Default.Lock,
                    contentDescription = "Estado da Licença",
                    tint = if (isPremium) EmeraldGreen else CrimsonRed,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isPremium) "Assinatura Ativa" else if (isTrial) "Trial Grátis em Andamento" else "Seu trial de 7 dias acabou",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPremium) EmeraldGreen else if (isTrial) CyanAccent else CrimsonRed,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isPremium) {
                    "Proteção 24h ativa até: $expiryFormatted"
                } else if (isTrial) {
                    "Restam $diasRestantes dias grátis. Pague com antecedência para não interromper a vigilância."
                } else {
                    "Pague para 869705734 via M-Pesa / E-mola para desbloquear a proteção."
                },
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Plans Selection Cards (Diário 7 MT | Semanal 25 MT | Mensal 80 MT)
            Text(
                text = "PLANOS DISPONÍVEIS (MOÇAMBIQUE)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyanAccent,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Plano Diário
                PlanOptionCard(
                    title = "Diário",
                    price = "7 MT",
                    duration = "1 Dia",
                    isSelected = selectedPlanForInfo == "DIARIO",
                    modifier = Modifier.weight(1f),
                    onClick = { selectedPlanForInfo = "DIARIO" }
                )

                // Plano Semanal
                PlanOptionCard(
                    title = "Semanal",
                    price = "25 MT",
                    duration = "7 Dias",
                    isPopular = true,
                    isSelected = selectedPlanForInfo == "SEMANAL",
                    modifier = Modifier.weight(1f),
                    onClick = { selectedPlanForInfo = "SEMANAL" }
                )

                // Plano Mensal
                PlanOptionCard(
                    title = "Mensal",
                    price = "80 MT",
                    duration = "30 Dias",
                    isSelected = selectedPlanForInfo == "MENSAL",
                    modifier = Modifier.weight(1f),
                    onClick = { selectedPlanForInfo = "MENSAL" }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Payment Instructions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Instruções de Pagamento",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(MovitelOrange.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("M-Pesa / E-mola", fontSize = 10.sp, color = MovitelOrange, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "1. Envie o valor do plano para o número:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Phone Number Copy Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceDark, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "869705734",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Titular: Jéssica Bernardo (Suporte MZ)",
                                fontSize = 11.sp,
                                color = TextTertiary
                            )
                        }

                        IconButton(
                            onClick = { copyToClipboard("869705734", "Número de Pagamento") },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar Número",
                                tint = CyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "2. Envie o comprovativo via WhatsApp/SMS para o 869705734 para receber seu código de ativação instantâneo.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { openWhatsAppOrSms() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreen,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pedir Código ao Admin (869705734)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Activation Code Input Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DIGITE O CÓDIGO RECEBIDO DO ADMIN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = activationCodeInput,
                        onValueChange = {
                            activationCodeInput = it.uppercase()
                            feedbackMessage = null
                        },
                        placeholder = { Text("Ex: MZ-84A7-9K2P", color = TextTertiary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("activation_code_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (activationCodeInput.isNotBlank()) {
                                    isSubmitting = true
                                    viewModel.ativarCodigo(activationCodeInput) { success, msg ->
                                        isSubmitting = false
                                        isSuccessFeedback = success
                                        feedbackMessage = msg
                                        if (success) {
                                            activationCodeInput = ""
                                        }
                                    }
                                }
                            }
                        )
                    )

                    feedbackMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSuccessFeedback) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isSuccessFeedback) EmeraldGreen else CrimsonRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = msg,
                                fontSize = 12.sp,
                                color = if (isSuccessFeedback) EmeraldGreen else CrimsonRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            isSubmitting = true
                            viewModel.ativarCodigo(activationCodeInput) { success, msg ->
                                isSubmitting = false
                                isSuccessFeedback = success
                                feedbackMessage = msg
                                if (success) {
                                    activationCodeInput = ""
                                }
                            }
                        },
                        enabled = activationCodeInput.isNotBlank() && !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("activate_code_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = Color.Black,
                            disabledContainerColor = SurfaceDark,
                            disabledContentColor = TextTertiary
                        )
                    ) {
                        Text(
                            text = if (isSubmitting) "A VALIDAR CÓDIGO..." else "ATIVAR COM CÓDIGO",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.syncLocalSubscriptionWithDbStatus()
                            Toast.makeText(context, "Estado da conta atualizado com a base de dados!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldGreen),
                        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verificar Confirmação do Admin", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Testing / Simulation Controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "FERRAMENTAS DE TESTE & DEMO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextTertiary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.simularFimTrial() },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("btn_simulate_trial_end"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrimsonRed.copy(alpha = 0.2f),
                                contentColor = CrimsonRed
                            )
                        ) {
                            Text("Simular Fim do Trial", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { viewModel.resetarTrial() },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("btn_reset_trial"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanAccent.copy(alpha = 0.2f),
                                contentColor = CyanAccent
                            )
                        ) {
                            Text("Restaurar Trial (7d)", fontSize = 11.sp)
                        }
                    }
                }
            }

            if (onBack != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = TextSecondary)
                ) {
                    Text("Voltar ao Painel", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PlanOptionCard(
    title: String,
    price: String,
    duration: String,
    isSelected: Boolean,
    isPopular: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("plan_card_$title"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CyanAccent.copy(alpha = 0.12f) else CardDark
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) CyanAccent else if (isPopular) VodacomRed.copy(alpha = 0.6f) else BorderSubtle
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isPopular) {
                Box(
                    modifier = Modifier
                        .background(VodacomRed, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("POPULAR", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = title,
                fontSize = 12.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = price,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) CyanAccent else TextPrimary
            )

            Text(
                text = duration,
                fontSize = 10.sp,
                color = TextTertiary
            )
        }
    }
}
