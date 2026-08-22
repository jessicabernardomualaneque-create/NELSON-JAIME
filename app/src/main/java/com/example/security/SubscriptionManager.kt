package com.example.security

import android.content.Context
import android.content.SharedPreferences
import com.example.data.database.ActivationCodeEntity
import com.example.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gestor Central de Subscrições e Licenciamento Anti-Roubo MZ Pro
 *
 * Regras:
 * 1. ADMIN: Telefone 869705734 com PIN 867884 tem acesso ILIMITADO para sempre.
 * 2. USUÁRIO NORMAL: 7 dias grátis (Trial). Depois pagar: Diário 7 MT | Semanal 25 MT | Mensal 80 MT
 * 3. Pagamento via M-Pesa / E-mola para 869705734
 * 4. Se não pagar: Bloqueia recursos anti-roubo e exibe tela de pagamento/ativação.
 */
object SubscriptionManager {

    const val ADMIN_PHONE = "869705734"
    const val ADMIN_PHONE_E164 = "+258869705734"
    const val ADMIN_PIN = "867884"
    const val PAYMENT_PHONE = "869705734"
    const val PAYMENT_RECIPIENT_NAME = "Jéssica Bernardo"

    const val PRICE_DIARIO = 7
    const val PRICE_SEMANAL = 25
    const val PRICE_MENSAL = 80
    const val TRIAL_DURATION_DAYS = 7

    private const val PREFS_NAME = "subscription_mz_prefs"
    private const val KEY_IS_ADMIN = "sub_is_admin"
    private const val KEY_FIRST_ACCESS = "sub_data_primeiro_acesso"
    private const val KEY_PLAN_TYPE = "sub_tipo_plano"
    private const val KEY_EXPIRY_DATE = "sub_data_expira"
    private const val KEY_ACTIVATION_CODE = "sub_codigo_ativacao"

    private var prefs: SharedPreferences? = null

    private val _isTrialFlow = MutableStateFlow(true)
    val isTrialFlow: StateFlow<Boolean> = _isTrialFlow.asStateFlow()

    private val _isPremiumFlow = MutableStateFlow(false)
    val isPremiumFlow: StateFlow<Boolean> = _isPremiumFlow.asStateFlow()

    private val _isAdminFlow = MutableStateFlow(false)
    val isAdminFlow: StateFlow<Boolean> = _isAdminFlow.asStateFlow()

    private val _diasRestantesFlow = MutableStateFlow(7L)
    val diasRestantesFlow: StateFlow<Long> = _diasRestantesFlow.asStateFlow()

    private val _tipoPlanoFlow = MutableStateFlow("TRIAL")
    val tipoPlanoFlow: StateFlow<String> = _tipoPlanoFlow.asStateFlow()

    private val _expiryDateFormattedFlow = MutableStateFlow("")
    val expiryDateFormattedFlow: StateFlow<String> = _expiryDateFormattedFlow.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val p = prefs ?: return

            // Initialize first access date if first install
            if (!p.contains(KEY_FIRST_ACCESS)) {
                p.edit()
                    .putLong(KEY_FIRST_ACCESS, System.currentTimeMillis())
                    .putString(KEY_PLAN_TYPE, "TRIAL")
                    .putLong(KEY_EXPIRY_DATE, 0L)
                    .putBoolean(KEY_IS_ADMIN, false)
                    .apply()
            }

            refreshState()
        }
    }

    private fun getPrefs(context: Context): SharedPreferences {
        if (prefs == null) {
            init(context.applicationContext)
        }
        return prefs!!
    }

    fun refreshState() {
        val p = prefs ?: return
        val isAdmin = p.getBoolean(KEY_IS_ADMIN, false)
        val firstAccess = p.getLong(KEY_FIRST_ACCESS, System.currentTimeMillis())
        val expiry = p.getLong(KEY_EXPIRY_DATE, 0L)
        val planType = p.getString(KEY_PLAN_TYPE, "TRIAL") ?: "TRIAL"

        val now = System.currentTimeMillis()
        val trialDurationMs = TRIAL_DURATION_DAYS * 24 * 60 * 60 * 1000L
        val trialEndsAt = firstAccess + trialDurationMs

        val isTrial = !isAdmin && (expiry <= now) && (now < trialEndsAt)
        val isPremium = isAdmin || (expiry > now)

        val daysLeft = when {
            isAdmin -> 9999L
            isPremium -> {
                val diff = expiry - now
                if (diff <= 0) 0L else ((diff + (24 * 60 * 60 * 1000L - 1)) / (24 * 60 * 60 * 1000L))
            }
            isTrial -> {
                val diff = trialEndsAt - now
                if (diff <= 0) 0L else ((diff + (24 * 60 * 60 * 1000L - 1)) / (24 * 60 * 60 * 1000L))
            }
            else -> 0L
        }

        val formattedDate = when {
            isAdmin -> "Acesso Ilimitado Vitalício"
            expiry > now -> SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "MZ")).format(Date(expiry))
            isTrial -> SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "MZ")).format(Date(trialEndsAt))
            else -> "Expirado"
        }

        _isAdminFlow.value = isAdmin
        _isTrialFlow.value = isTrial
        _isPremiumFlow.value = isPremium
        _diasRestantesFlow.value = daysLeft
        _tipoPlanoFlow.value = if (isAdmin) "ADMIN_ILIMITADO" else planType
        _expiryDateFormattedFlow.value = formattedDate
    }

    /**
     * Verifica se as credenciais correspondem ao Administrador Oficial (869705734 com PIN 867884)
     */
    fun checkIsAdminCredentials(rawPhone: String?, pin: String?): Boolean {
        if (rawPhone == null || pin == null) return false
        val cleanPhone = rawPhone.replace("[^0-9]".toRegex(), "")
        val isAdminPhone = cleanPhone == ADMIN_PHONE || cleanPhone == "258$ADMIN_PHONE"
        val isAdminPin = pin.trim() == ADMIN_PIN
        return isAdminPhone && isAdminPin
    }

    fun isAdmin(): Boolean {
        return _isAdminFlow.value || (prefs?.getBoolean(KEY_IS_ADMIN, false) ?: false)
    }

    fun isTrial(): Boolean {
        if (isAdmin()) return false
        val p = prefs ?: return true
        val firstAccess = p.getLong(KEY_FIRST_ACCESS, System.currentTimeMillis())
        val expiry = p.getLong(KEY_EXPIRY_DATE, 0L)
        val now = System.currentTimeMillis()
        val trialEndsAt = firstAccess + (TRIAL_DURATION_DAYS * 24 * 60 * 60 * 1000L)
        return expiry <= now && now < trialEndsAt
    }

    fun isPremium(): Boolean {
        if (isAdmin()) return true
        val p = prefs ?: return false
        val expiry = p.getLong(KEY_EXPIRY_DATE, 0L)
        return expiry > System.currentTimeMillis()
    }

    fun diasRestantes(): Long {
        return _diasRestantesFlow.value
    }

    /**
     * Regra 4: Se não pagar e o trial acabou, bloquear recursos anti-roubo.
     */
    fun bloquearApp(): Boolean {
        if (isAdmin()) return false
        return !isPremium() && !isTrial()
    }

    fun setAdminAccess(context: Context, enabled: Boolean) {
        val p = getPrefs(context)
        p.edit()
            .putBoolean(KEY_IS_ADMIN, enabled)
            .putString(KEY_PLAN_TYPE, if (enabled) "ADMIN_ILIMITADO" else "TRIAL")
            .apply()
        refreshState()
    }

    /**
     * Gera um código de ativação aleatório de alta segurança no formato: MZ-XXXX-XXXX
     */
    suspend fun gerarCodigoAtivacao(
        context: Context,
        planType: String = "SEMANAL" // "DIARIO", "SEMANAL", "MENSAL", "ILIMITADO"
    ): ActivationCodeEntity {
        val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        val random = SecureRandom()

        fun randomChunk(length: Int): String {
            return (1..length).map { chars[random.nextInt(chars.length)] }.joinToString("")
        }

        val codeValue = "MZ-${randomChunk(4)}-${randomChunk(4)}"
        val (durationDays, priceMt) = when (planType.uppercase(Locale.ROOT)) {
            "DIARIO", "1_DIA" -> Pair(1, PRICE_DIARIO)
            "SEMANAL", "7_DIAS" -> Pair(7, PRICE_SEMANAL)
            "MENSAL", "30_DIAS" -> Pair(30, PRICE_MENSAL)
            "ILIMITADO" -> Pair(9999, 0)
            else -> Pair(7, PRICE_SEMANAL)
        }

        val entity = ActivationCodeEntity(
            code = codeValue,
            planType = planType.uppercase(Locale.ROOT),
            durationDays = durationDays,
            priceMt = priceMt,
            status = "PENDENTE",
            createdAt = System.currentTimeMillis()
        )

        val db = AppDatabase.getInstance(context)
        val id = db.activationCodeDao().insertCode(entity)
        return entity.copy(id = id)
    }

    /**
     * Ativa o plano através de um código fornecido pelo Admin (869705734)
     */
    suspend fun ativarCodigo(
        context: Context,
        codigoInput: String,
        userPhone: String? = null
    ): Pair<Boolean, String> {
        val cleanCode = codigoInput.trim().uppercase(Locale.ROOT)
        if (cleanCode.isBlank()) {
            return Pair(false, "Por favor, digite o código de ativação.")
        }

        val db = AppDatabase.getInstance(context)
        val codeEntity = db.activationCodeDao().getCodeByValue(cleanCode)

        if (codeEntity == null) {
            // Check fallback for instant verification of demo/universal admin master codes
            if (cleanCode == "MZ-ADMIN-8678" || cleanCode == "MZ-PRO-2026") {
                aplicarPlano(context, "MENSAL", 30, cleanCode)
                return Pair(true, "Código Mestre aceite! Plano Mensal (30 dias) ativado com sucesso.")
            }
            return Pair(false, "Código de ativação inválido. Contacte o administrador no 869705734 via M-Pesa/E-mola.")
        }

        if (codeEntity.status == "USADO") {
            val usedDate = codeEntity.usedAt?.let {
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "MZ")).format(Date(it))
            } ?: "anteriormente"
            return Pair(false, "Este código já foi utilizado em $usedDate.")
        }

        // Mark code as used in Database
        val updatedCode = codeEntity.copy(
            status = "USADO",
            usedAt = System.currentTimeMillis(),
            usedByPhone = userPhone ?: "Dispositivo Local"
        )
        db.activationCodeDao().updateCode(updatedCode)

        // Apply plan duration
        aplicarPlano(context, codeEntity.planType, codeEntity.durationDays, cleanCode)
        return Pair(true, "Sucesso! Plano ${codeEntity.planType} (${codeEntity.durationDays} dias) ativado!")
    }

    /**
     * Ativação manual direta pelo Painel Admin
     */
    fun aplicarPlano(
        context: Context,
        planType: String,
        durationDays: Int,
        activationCode: String = "MANUAL_ADMIN"
    ) {
        val p = getPrefs(context)
        val now = System.currentTimeMillis()
        val currentExpiry = p.getLong(KEY_EXPIRY_DATE, 0L)
        val baseTime = if (currentExpiry > now) currentExpiry else now
        val addedMs = durationDays.toLong() * 24L * 60L * 60L * 1000L
        val newExpiry = baseTime + addedMs

        p.edit()
            .putString(KEY_PLAN_TYPE, planType)
            .putLong(KEY_EXPIRY_DATE, newExpiry)
            .putString(KEY_ACTIVATION_CODE, activationCode)
            .putBoolean(KEY_IS_ADMIN, false)
            .apply()

        refreshState()
    }

    /**
     * Função para testes: Simula o fim do período Trial (7 dias) para verificar o bloqueio
     */
    fun simularFimTrial(context: Context) {
        val p = getPrefs(context)
        val eightDaysAgo = System.currentTimeMillis() - (8L * 24L * 60L * 60L * 1000L)
        p.edit()
            .putLong(KEY_FIRST_ACCESS, eightDaysAgo)
            .putLong(KEY_EXPIRY_DATE, 0L)
            .putBoolean(KEY_IS_ADMIN, false)
            .putString(KEY_PLAN_TYPE, "EXPIRADO")
            .apply()
        refreshState()
    }

    /**
     * Função para testes: Restaura o período Trial para 7 dias grátis
     */
    fun resetarTrial(context: Context) {
        val p = getPrefs(context)
        p.edit()
            .putLong(KEY_FIRST_ACCESS, System.currentTimeMillis())
            .putLong(KEY_EXPIRY_DATE, 0L)
            .putBoolean(KEY_IS_ADMIN, false)
            .putString(KEY_PLAN_TYPE, "TRIAL")
            .apply()
        refreshState()
    }
}
