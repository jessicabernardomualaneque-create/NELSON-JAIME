package com.example.data.database

import android.content.Context
import com.example.data.model.UserProfile
import com.example.data.repository.SecurityPreferences
import com.example.security.SubscriptionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminDatabaseRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val userDao = db.userDao()
    private val auditDao = db.securityAuditDao()
    private val smsDao = db.smsCommandHistoryDao()
    private val settingDao = db.deviceSettingDao()
    private val activationCodeDao = db.activationCodeDao()
    private val prefs = SecurityPreferences(context)

    companion object {
        const val ADMIN_EMAIL = "jessicabernardomualaneque@gmail.com"
        const val ADMIN_PASSWORD = "123456789@#"
    }

    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    val allUsers: Flow<List<UserAccountEntity>> = userDao.getAllUsers()
    val pendingPaymentUsers: Flow<List<UserAccountEntity>> = userDao.getPendingPaymentUsers()
    val allAuditLogs: Flow<List<SecurityAuditEntity>> = auditDao.getAllLogs()
    val allSmsCommands: Flow<List<SmsCommandHistoryEntity>> = smsDao.getAllHistory()
    val allSettings: Flow<List<DeviceSettingEntity>> = settingDao.getAllSettings()
    val allActivationCodes: Flow<List<ActivationCodeEntity>> = activationCodeDao.getAllCodes()

    val usersCount: Flow<Int> = userDao.getUsersCount()
    val pendingCount: Flow<Int> = userDao.getPendingPaymentCount()
    val logsCount: Flow<Int> = auditDao.getLogsCount()
    val commandsCount: Flow<Int> = smsDao.getCommandsCount()
    val codesCount: Flow<Int> = activationCodeDao.getCodesCount()

    init {
        // Seed database with current profile and default logs if empty
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDatabase()
        }
    }

    suspend fun seedInitialDatabase() {
        val currentProfile = prefs.getUserProfile()
        if (currentProfile != null) {
            val existing = userDao.getUserByPhone(currentProfile.phoneNumber)
            if (existing == null) {
                userDao.insertUser(
                    UserAccountEntity(
                        fullName = currentProfile.fullName,
                        phoneNumber = currentProfile.phoneNumber,
                        operator = currentProfile.operator.displayName,
                        pin = prefs.getSecurityPin(),
                        isVerified = true,
                        role = "USER",
                        batteryPct = 85,
                        isArmed = prefs.isArmed(),
                        notes = "Titular Registado no Aplicativo"
                    )
                )
            }
        } else {
            // Add default initial administrator record
            val existingAdmin = userDao.getUserByPhone("+258840000000")
            if (existingAdmin == null) {
                userDao.insertUser(
                    UserAccountEntity(
                        fullName = "Administrador Jéssica Bernardo",
                        phoneNumber = "+258840000000",
                        operator = "Vodacom Moçambique",
                        pin = "1234",
                        isVerified = true,
                        role = "ADMIN",
                        batteryPct = 95,
                        isArmed = true,
                        notes = "Conta Super-Admin MZ"
                    )
                )
            }
        }

        // Populate initial settings if not present
        if (settingDao.getSettingValue("MAX_FAILED_ATTEMPTS") == null) {
            settingDao.setSetting(
                DeviceSettingEntity(
                    key = "MAX_FAILED_ATTEMPTS",
                    value = "3",
                    description = "Limite de tentativas de PIN incorreto antes do bloqueio total"
                )
            )
            settingDao.setSetting(
                DeviceSettingEntity(
                    key = "EMERGENCY_SIREN_DURATION_SEC",
                    value = "10",
                    description = "Duração em segundos do alarme sonoro em volume máximo"
                )
            )
            settingDao.setSetting(
                DeviceSettingEntity(
                    key = "DEFAULT_COUNTRY_CODE",
                    value = "+258",
                    description = "Código de discagem internacional exclusivo de Moçambique"
                )
            )
            settingDao.setSetting(
                DeviceSettingEntity(
                    key = "DATABASE_ENGINE",
                    value = "Room SQLite Encrypted",
                    description = "Mecanismo de persistência local da base de dados"
                )
            )
        }
    }

    /**
     * Authenticates the Admin. STRICTLY only allows:
     * Email: jessicabernardomualaneque@gmail.com
     * Senha: 123456789@#
     */
    fun loginAdmin(email: String, password: String): Boolean {
        val trimmedEmail = email.trim()
        val isValid = trimmedEmail.equals(ADMIN_EMAIL, ignoreCase = true) && password == ADMIN_PASSWORD
        if (isValid) {
            _isAdminLoggedIn.value = true
        }
        return isValid
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
    }

    suspend fun insertUser(user: UserAccountEntity): Long {
        return userDao.insertUser(user)
    }

    suspend fun updateUser(user: UserAccountEntity) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(id: Long) {
        userDao.deleteUserById(id)
    }

    suspend fun deleteAllUsers() {
        userDao.deleteAllUsers()
    }

    suspend fun insertAuditLog(
        title: String,
        description: String,
        eventType: String,
        locationInfo: String? = null,
        senderNumber: String? = null,
        severity: String = "INFO"
    ) {
        auditDao.insertLog(
            SecurityAuditEntity(
                title = title,
                description = description,
                timestamp = System.currentTimeMillis(),
                eventType = eventType,
                locationInfo = locationInfo,
                senderNumber = senderNumber,
                severity = severity
            )
        )
    }

    suspend fun deleteAuditLog(id: Long) {
        auditDao.deleteLogById(id)
    }

    suspend fun clearAuditLogs() {
        auditDao.clearAllLogs()
    }

    suspend fun insertSmsCommand(
        command: String,
        senderNumber: String,
        responseMessage: String,
        batteryPct: Int,
        isCharging: Boolean,
        success: Boolean
    ) {
        smsDao.insertCommand(
            SmsCommandHistoryEntity(
                command = command,
                senderNumber = senderNumber,
                responseMessage = responseMessage,
                batteryPct = batteryPct,
                isCharging = isCharging,
                timestamp = System.currentTimeMillis(),
                success = success
            )
        )
    }

    suspend fun clearSmsHistory() {
        smsDao.clearHistory()
    }

    suspend fun updateSetting(key: String, value: String, description: String = "") {
        settingDao.setSetting(
            DeviceSettingEntity(
                key = key,
                value = value,
                description = description,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun syncCurrentProfileToDb(profile: UserProfile, pin: String) {
        val existing = userDao.getUserByPhone(profile.phoneNumber)
        if (existing == null) {
            userDao.insertUser(
                UserAccountEntity(
                    fullName = profile.fullName,
                    phoneNumber = profile.phoneNumber,
                    operator = profile.operator.displayName,
                    pin = pin,
                    isVerified = profile.isVerified,
                    role = "USER",
                    accountStatus = "ATIVO",
                    planType = "TRIAL",
                    batteryPct = 85,
                    isArmed = prefs.isArmed(),
                    notes = "Sincronizado do Dispositivo Local"
                )
            )
        } else {
            userDao.updateUser(
                existing.copy(
                    fullName = profile.fullName,
                    operator = profile.operator.displayName,
                    pin = pin,
                    isVerified = profile.isVerified,
                    lastActive = System.currentTimeMillis(),
                    isArmed = prefs.isArmed()
                )
            )
        }
    }

    suspend fun confirmUserPayment(
        phone: String,
        planDays: Int,
        planName: String,
        adminIdentifier: String = "ADMIN_869705734"
    ): Boolean {
        val cleanPhone = phone.trim()
        val now = System.currentTimeMillis()
        val addedMs = planDays.toLong() * 24L * 60L * 60L * 1000L
        val newExpiry = now + addedMs
        val noteText = "Pagamento Confirmado pelo Admin ($adminIdentifier) • Plano $planName ($planDays dias)"

        val user = userDao.getUserByPhone(cleanPhone)
        if (user != null) {
            userDao.updateUser(
                user.copy(
                    accountStatus = "ATIVO",
                    planType = planName,
                    planExpiry = newExpiry,
                    isPaymentConfirmed = true,
                    paymentConfirmedAt = now,
                    paymentConfirmedBy = adminIdentifier,
                    lastActive = now,
                    notes = noteText
                )
            )
        } else {
            userDao.insertUser(
                UserAccountEntity(
                    fullName = "Cliente Ativado $cleanPhone",
                    phoneNumber = cleanPhone,
                    operator = "Vodacom Moçambique",
                    pin = "1234",
                    isVerified = true,
                    role = "USER",
                    accountStatus = "ATIVO",
                    planType = planName,
                    planExpiry = newExpiry,
                    isPaymentConfirmed = true,
                    paymentConfirmedAt = now,
                    paymentConfirmedBy = adminIdentifier,
                    notes = noteText
                )
            )
        }

        // Also check if local device phone matches or if this is the active device profile
        val currentProfile = prefs.getUserProfile()
        if (currentProfile != null) {
            val pClean = currentProfile.phoneNumber.replace("[^0-9]".toRegex(), "")
            val targetClean = cleanPhone.replace("[^0-9]".toRegex(), "")
            if (pClean.contains(targetClean) || targetClean.contains(pClean) || pClean.endsWith(targetClean) || targetClean.endsWith(pClean)) {
                SubscriptionManager.aplicarPlano(context, planName, planDays, "ADMIN_CONFIRMADO")
            }
        }

        insertAuditLog(
            title = "Pagamento Confirmado pelo Admin",
            description = "Conta do utilizador $cleanPhone confirmada e ativada com sucesso (Plano: $planName - $planDays dias).",
            eventType = "PAYMENT_CONFIRMED",
            senderNumber = cleanPhone,
            severity = "INFO"
        )
        return true
    }

    suspend fun requestPaymentConfirmation(phone: String, planName: String, notes: String = "") {
        val cleanPhone = phone.trim()
        val user = userDao.getUserByPhone(cleanPhone)
        if (user != null) {
            userDao.updateUser(
                user.copy(
                    accountStatus = "PENDENTE_CONFIRMACAO",
                    planType = planName,
                    isPaymentConfirmed = false,
                    notes = "Pagamento Efetuado pelo Usuário • Aguardando Validação Admin: $notes"
                )
            )
        } else {
            val currentProfile = prefs.getUserProfile()
            userDao.insertUser(
                UserAccountEntity(
                    fullName = currentProfile?.fullName ?: "Utilizador $cleanPhone",
                    phoneNumber = cleanPhone,
                    operator = currentProfile?.operator?.displayName ?: "Moçambique",
                    pin = prefs.getSecurityPin(),
                    isVerified = true,
                    role = "USER",
                    accountStatus = "PENDENTE_CONFIRMACAO",
                    planType = planName,
                    isPaymentConfirmed = false,
                    notes = "Pagamento Efetuado pelo Usuário • Aguardando Validação Admin: $notes"
                )
            )
        }

        insertAuditLog(
            title = "Novo Pedido de Confirmação de Pagamento",
            description = "Utilizador $cleanPhone notificou pagamento para o plano $planName. Aguardando aprovação no painel do administrador.",
            eventType = "PAYMENT_PENDING",
            senderNumber = cleanPhone,
            severity = "WARNING"
        )
    }

    suspend fun syncLocalSubscriptionWithDbStatus() {
        val currentProfile = prefs.getUserProfile() ?: return
        val user = userDao.getUserByPhone(currentProfile.phoneNumber) ?: return

        if (user.accountStatus == "ATIVO" && user.planExpiry > System.currentTimeMillis()) {
            val remainingMs = user.planExpiry - System.currentTimeMillis()
            val remainingDays = (remainingMs / (24L * 60L * 60L * 1000L)).toInt().coerceAtLeast(1)
            SubscriptionManager.aplicarPlano(context, user.planType, remainingDays, "DB_SYNC_CONFIRMADO")
        }
    }

    suspend fun getUserByPhone(phone: String): UserAccountEntity? {
        return userDao.getUserByPhone(phone)
    }

    suspend fun insertActivationCode(code: ActivationCodeEntity): Long {
        return activationCodeDao.insertCode(code)
    }

    suspend fun deleteActivationCode(id: Long) {
        activationCodeDao.deleteCodeById(id)
    }

    suspend fun clearAllActivationCodes() {
        activationCodeDao.deleteAllCodes()
    }
}
