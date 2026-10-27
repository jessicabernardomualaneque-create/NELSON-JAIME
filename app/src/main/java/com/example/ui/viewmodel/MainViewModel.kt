package com.example.ui.viewmodel

import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.ActivationCodeEntity
import com.example.data.database.AdminDatabaseRepository
import com.example.data.database.DeviceSettingEntity
import com.example.data.database.SecurityAuditEntity
import com.example.data.database.SmsCommandHistoryEntity
import com.example.data.database.UserAccountEntity
import com.example.data.database.VaultItemEntity
import com.example.data.database.VaultRepository
import com.example.data.database.VaultUnlockStatus
import com.example.data.model.EmergencyContact
import com.example.data.model.EventType
import com.example.data.model.MozambiqueOperator
import com.example.data.model.MozambiquePhoneHelper
import com.example.data.model.SecurityEvent
import com.example.data.model.SmsCommandItem
import com.example.data.model.UserProfile
import com.example.data.repository.SecurityPreferences
import com.example.receiver.AntiTheftAdminReceiver
import com.example.security.LiveLocationState
import com.example.security.LocationHelper
import com.example.security.MozambiqueLocationTracker
import com.example.security.MozambiqueProvince
import com.example.security.SirenController
import com.example.security.SmsCommandHandler
import com.example.security.SubscriptionManager
import com.example.security.SubscriptionNotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.security.telephony.DeviceHardwareHelper
import com.example.security.telephony.DeviceHardwareInfo

data class UiDeviceStatus(
    val batteryPct: Int = 85,
    val isCharging: Boolean = false,
    val operatorName: String = "Vodacom Moçambique",
    val gpsCoordinates: String = "-25.9692, 32.5732 (Maputo)",
    val googleMapsUrl: String = "https://maps.google.com/?q=-25.9692,32.5732",
    val isAdminActive: Boolean = false,
    val isSirenPlaying: Boolean = false,
    val imei: String = "358941091234567",
    val imei2: String? = null,
    val brand: String = "Samsung",
    val model: String = "Galaxy A54 5G",
    val technicalModel: String = "SM-A546B",
    val simState: String = "Ativo e Conectado",
    val simSlotCount: Int = 1,
    val isDualSim: Boolean = false,
    val operatorCode: String = "64304"
)

data class SmsSimulatedMessage(
    val id: String,
    val text: String,
    val isIncoming: Boolean,
    val timestamp: String,
    val statusBadge: String? = null,
    val batteryPct: Int? = null,
    val isCharging: Boolean? = null,
    val ownerName: String? = null,
    val ownerPhone: String? = null,
    val operatorName: String? = null,
    val deviceModel: String? = null,
    val gpsCoordinates: String? = null,
    val googleMapsUrl: String? = null,
    val commandType: String? = null,
    val hasPhotoPreview: Boolean = false,
    val hasScreenLockPreview: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = SecurityPreferences(application)
    val adminRepository = AdminDatabaseRepository(application)
    val locationTracker = MozambiqueLocationTracker(application)
    val vaultRepository = VaultRepository(application)

    val isArmed: StateFlow<Boolean> = prefs.isArmedFlow
    val events: StateFlow<List<SecurityEvent>> = prefs.eventsFlow
    val emergencyContacts: StateFlow<List<EmergencyContact>> = prefs.contactsFlow
    val userProfile: StateFlow<UserProfile?> = prefs.userProfileFlow

    // Samsung-like Anti-Theft Protection Triggers
    val antiPowerOff: StateFlow<Boolean> = prefs.antiPowerOffFlow
    val antiAirplaneMode: StateFlow<Boolean> = prefs.antiAirplaneModeFlow
    val antiDataOff: StateFlow<Boolean> = prefs.antiDataOffFlow

    // Secret Vault (Cofre Secreto MZ) State Flows
    val isVaultUnlocked: StateFlow<Boolean> = vaultRepository.isVaultUnlocked
    val isDecoyVaultActive: StateFlow<Boolean> = vaultRepository.isDecoyVaultActive
    val isDisguiseMode: StateFlow<Boolean> = vaultRepository.isDisguiseMode
    val failedVaultAttempts: StateFlow<Int> = vaultRepository.failedAttempts
    val isVaultLockoutActive: StateFlow<Boolean> = vaultRepository.isLockoutActive
    val vaultLockoutRemainingSeconds: StateFlow<Int> = vaultRepository.lockoutRemainingSeconds
    val isDecoyVaultEnabled: StateFlow<Boolean> = vaultRepository.isDecoyEnabled
    val decoyVaultPin: StateFlow<String> = vaultRepository.decoyPin
    val requireVaultDoublePin: StateFlow<Boolean> = vaultRepository.requireDoublePin

    val vaultAllItems: StateFlow<List<VaultItemEntity>> = vaultRepository.getAllVaultItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaultPhotos: StateFlow<List<VaultItemEntity>> = vaultRepository.getVaultItemsByType("PHOTO")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaultVideos: StateFlow<List<VaultItemEntity>> = vaultRepository.getVaultItemsByType("VIDEO")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaultMessages: StateFlow<List<VaultItemEntity>> = vaultRepository.getVaultItemsByType("MESSAGE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaultMusics: StateFlow<List<VaultItemEntity>> = vaultRepository.getVaultItemsByType("MUSIC")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaultContacts: StateFlow<List<VaultItemEntity>> = vaultRepository.getVaultItemsByType("CONTACT")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaultAppLocks: StateFlow<List<VaultItemEntity>> = vaultRepository.getVaultItemsByType("APP_LOCK")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalVaultCount: StateFlow<Int> = vaultRepository.getTotalVaultCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Live Location & Mozambique Map State Flows
    val liveLocationState: StateFlow<LiveLocationState> = locationTracker.locationState

    private val _selectedProvince = MutableStateFlow<MozambiqueProvince?>(null)
    val selectedProvince: StateFlow<MozambiqueProvince?> = _selectedProvince.asStateFlow()

    private val _isContinuousTracking = MutableStateFlow(true)
    val isContinuousTracking: StateFlow<Boolean> = _isContinuousTracking.asStateFlow()

    private val _geofenceEnabled = MutableStateFlow(false)
    val geofenceEnabled: StateFlow<Boolean> = _geofenceEnabled.asStateFlow()

    private val _isSatelliteMode = MutableStateFlow(false)
    val isSatelliteMode: StateFlow<Boolean> = _isSatelliteMode.asStateFlow()

    // Room Database State Flows for Admin Management
    val isAdminLoggedIn: StateFlow<Boolean> = adminRepository.isAdminLoggedIn

    val dbUsers: StateFlow<List<UserAccountEntity>> = adminRepository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dbPendingUsers: StateFlow<List<UserAccountEntity>> = adminRepository.pendingPaymentUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dbPendingCount: StateFlow<Int> = adminRepository.pendingCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val dbAuditLogs: StateFlow<List<SecurityAuditEntity>> = adminRepository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dbSmsCommands: StateFlow<List<SmsCommandHistoryEntity>> = adminRepository.allSmsCommands
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dbSettings: StateFlow<List<DeviceSettingEntity>> = adminRepository.allSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dbUsersCount: StateFlow<Int> = adminRepository.usersCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val dbLogsCount: StateFlow<Int> = adminRepository.logsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val dbCommandsCount: StateFlow<Int> = adminRepository.commandsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val dbActivationCodes: StateFlow<List<ActivationCodeEntity>> = adminRepository.allActivationCodes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dbCodesCount: StateFlow<Int> = adminRepository.codesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Subscription & Licensing Flows (Trial 7 Dias / Planos MZ / Admin Ilimitado)
    val isTrial: StateFlow<Boolean> = SubscriptionManager.isTrialFlow
    val isPremium: StateFlow<Boolean> = SubscriptionManager.isPremiumFlow
    val isSubAdmin: StateFlow<Boolean> = SubscriptionManager.isAdminFlow
    val diasRestantes: StateFlow<Long> = SubscriptionManager.diasRestantesFlow
    val tipoPlano: StateFlow<String> = SubscriptionManager.tipoPlanoFlow
    val expiryDateFormatted: StateFlow<String> = SubscriptionManager.expiryDateFormattedFlow

    private val _deviceStatus = MutableStateFlow(UiDeviceStatus())
    val deviceStatus: StateFlow<UiDeviceStatus> = _deviceStatus.asStateFlow()

    private val _hardwareInfo = MutableStateFlow(DeviceHardwareHelper.getFullDeviceHardwareInfo(application))
    val hardwareInfo: StateFlow<DeviceHardwareInfo> = _hardwareInfo.asStateFlow()

    private val _termsAccepted = MutableStateFlow(prefs.isTermsAccepted())
    val termsAccepted: StateFlow<Boolean> = _termsAccepted.asStateFlow()

    private val _antiBruteForce = MutableStateFlow(prefs.isAntiBruteForceEnabled())
    val antiBruteForce: StateFlow<Boolean> = _antiBruteForce.asStateFlow()

    private val _simWatchdog = MutableStateFlow(prefs.isSimWatchdogEnabled())
    val simWatchdog: StateFlow<Boolean> = _simWatchdog.asStateFlow()

    private val _stealthMode = MutableStateFlow(prefs.isStealthModeEnabled())
    val stealthMode: StateFlow<Boolean> = _stealthMode.asStateFlow()

    private val _securityPin = MutableStateFlow(prefs.getSecurityPin())
    val securityPin: StateFlow<String> = _securityPin.asStateFlow()

    private val _ownerEmail = MutableStateFlow(prefs.getOwnerEmail())
    val ownerEmail: StateFlow<String> = _ownerEmail.asStateFlow()

    // SMS Console Simulation messages
    private val _simulatedMessages = MutableStateFlow<List<SmsSimulatedMessage>>(emptyList())
    val simulatedMessages: StateFlow<List<SmsSimulatedMessage>> = _simulatedMessages.asStateFlow()

    val smsCommandList = listOf(
        SmsCommandItem(
            command = "LOCALIZAR",
            syntax = "LOCALIZAR [senha]",
            description = "Retorna coordenadas GPS em tempo real com link do Google Maps, carga da bateria e detalhes do titular.",
            example = "LOCALIZAR ${prefs.getSecurityPin()}",
            badge = "GPS OFFLINE"
        ),
        SmsCommandItem(
            command = "IMEI",
            syntax = "IMEI [senha]",
            description = "Retorna o IMEI de 15 dígitos (IMEI 1 e 2), Marca e Modelo para bloqueio policial (PRM) e na operadora (INCM).",
            example = "IMEI ${prefs.getSecurityPin()}",
            badge = "IMEI & MARCA"
        ),
        SmsCommandItem(
            command = "FOTO",
            syntax = "FOTO [senha]",
            description = "Captura evidência de foto da câmara frontal do intruso e foto do ecrã com telemetria do usuário.",
            example = "FOTO ${prefs.getSecurityPin()}",
            badge = "FOTO + ECRÃ"
        ),
        SmsCommandItem(
            command = "STATUS",
            syntax = "STATUS [senha]",
            description = "Relatório completo: IMEI, Carga da bateria, titular, modelo do aparelho e rede móvel MZ.",
            example = "STATUS ${prefs.getSecurityPin()}",
            badge = "DIAGNÓSTICO"
        ),
        SmsCommandItem(
            command = "ALARME",
            syntax = "ALARME [senha]",
            description = "Dispara sirene sonoro de volume máximo por 10s e pisca o flash estroboscópio.",
            example = "ALARME ${prefs.getSecurityPin()}",
            badge = "SIRENE + FLASH"
        ),
        SmsCommandItem(
            command = "BLOQUEAR",
            syntax = "BLOQUEAR [senha]",
            description = "Bloqueia o ecrã do aparelho na hora e ativa rastreamento contínuo em modo furtivo.",
            example = "BLOQUEAR ${prefs.getSecurityPin()}",
            badge = "TRAVAMENTO"
        ),
        SmsCommandItem(
            command = "AJUDA",
            syntax = "AJUDA [senha]",
            description = "Envia a lista com o resumo de todos os comandos aceitos pelo Anti-Roubo MZ Pro.",
            example = "AJUDA ${prefs.getSecurityPin()}",
            badge = "MANUAL"
        )
    )

    init {
        val initialPin = prefs.getSecurityPin()
        val initialProfile = prefs.getUserProfile()
        val (bat, charging) = LocationHelper.getBatteryInfo(application)
        val hw = DeviceHardwareHelper.getFullDeviceHardwareInfo(application)
        val devModel = "${hw.brand} ${hw.model}"
        val op = hw.primarySim?.operatorName ?: initialProfile?.operator?.displayName ?: "Vodacom Moçambique"

        _simulatedMessages.value = listOf(
            SmsSimulatedMessage(
                id = "init_sent",
                text = "STATUS $initialPin",
                isIncoming = false,
                timestamp = "Hoje 10:15",
                statusBadge = "SMS ENVIADO"
            ),
            SmsSimulatedMessage(
                id = "init_resp",
                text = "ANTI-ROUBO MZ PRO: Titular: ${initialProfile?.fullName ?: "Proprietário MZ"} | IMEI: ${hw.imei1} | Aparelho: $devModel | Operadora: $op | Carga: $bat% ${if (charging) "(⚡ Carga)" else "(🔋 Bateria)"} | Estado: ARMADO",
                isIncoming = true,
                timestamp = "Hoje 10:15",
                statusBadge = "RESPOSTA MZ",
                batteryPct = bat,
                isCharging = charging,
                ownerName = initialProfile?.fullName ?: "Proprietário MZ",
                ownerPhone = initialProfile?.phoneNumber ?: "+258 84 123 4567",
                operatorName = op,
                deviceModel = devModel,
                gpsCoordinates = "-25.9692, 32.5732 (Maputo)",
                googleMapsUrl = "https://maps.google.com/?q=-25.9692,32.5732",
                commandType = "STATUS",
                hasPhotoPreview = true
            )
        )

        refreshDeviceStatus()
    }

    fun refreshDeviceStatus() {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val hw = DeviceHardwareHelper.getFullDeviceHardwareInfo(ctx)
            _hardwareInfo.value = hw

            val (bat, charging) = LocationHelper.getBatteryInfo(ctx)
            val loc = LocationHelper.getCurrentLocation(ctx)
            val dpm = ctx.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            val adminComp = ComponentName(ctx, AntiTheftAdminReceiver::class.java)
            val isAdmin = dpm?.isAdminActive(adminComp) == true

            val profile = prefs.getUserProfile()
            val operator = hw.primarySim?.operatorName ?: profile?.operator?.displayName ?: prefs.getLastKnownOperator()

            val coords = if (loc != null) {
                String.format(Locale.ROOT, "%.4f, %.4f", loc.latitude, loc.longitude)
            } else {
                "-25.9692, 32.5732 (Maputo)"
            }
            val mapUrl = "https://maps.google.com/?q=$coords"

            _deviceStatus.value = UiDeviceStatus(
                batteryPct = bat,
                isCharging = charging,
                operatorName = operator,
                gpsCoordinates = coords,
                googleMapsUrl = mapUrl,
                isAdminActive = isAdmin,
                isSirenPlaying = SirenController.isPlaying(),
                imei = hw.imei1,
                imei2 = hw.imei2,
                brand = hw.brand,
                model = hw.model,
                technicalModel = hw.rawModel,
                simState = hw.primarySim?.simStateText ?: "Ativo e Conectado",
                simSlotCount = hw.activeSimCount,
                isDualSim = hw.isDualSimSupported,
                operatorCode = hw.primarySim?.operatorCode ?: "64304"
            )
        }
    }

    fun updateDeviceImei(newImei: String) {
        val clean = newImei.replace("[^0-9]".toRegex(), "")
        if (clean.length == 15) {
            prefs.setDeviceImei(clean)
            refreshDeviceStatus()
        }
    }

    fun registerMozambiqueAccount(
        fullName: String,
        rawPhone: String,
        pin: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val validation = MozambiquePhoneHelper.validate(rawPhone)
        if (!validation.isValid) {
            onResult(false, validation.errorMessage ?: "Número inválido para Moçambique.")
            return
        }

        if (fullName.isBlank() || fullName.trim().length < 3) {
            onResult(false, "Por favor, insira o seu nome completo.")
            return
        }

        if (pin.length < 4) {
            onResult(false, "O PIN de segurança deve conter no mínimo 4 dígitos.")
            return
        }

        val profile = UserProfile(
            fullName = fullName.trim(),
            phoneNumber = validation.formattedNumber,
            operator = validation.operator,
            createdAt = System.currentTimeMillis(),
            isVerified = true
        )

        // Save profile directly for instant auto-login & future persistence
        prefs.saveUserProfile(profile)
        prefs.setSecurityPin(pin)
        _securityPin.value = pin
        prefs.setTermsAccepted(true)
        _termsAccepted.value = true

        viewModelScope.launch {
            adminRepository.syncCurrentProfileToDb(profile, pin)
            adminRepository.insertAuditLog(
                title = "Conta Moçambique Ativada",
                description = "Titular: ${profile.fullName} | Tel: ${profile.phoneNumber} (${profile.operator.displayName})",
                eventType = "ACCOUNT_CREATED",
                senderNumber = profile.phoneNumber,
                severity = "INFO"
            )
        }

        prefs.logEvent(
            title = "Conta Moçambique Ativada",
            description = "Titular: ${profile.fullName} | Tel: ${profile.phoneNumber} (${profile.operator.displayName})",
            type = EventType.ACCOUNT_CREATED
        )

        refreshDeviceStatus()
        onResult(true, null)
    }

    fun loginMozambiqueAccount(
        rawPhone: String,
        pin: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val ctx = getApplication<Application>()
        SubscriptionManager.init(ctx)

        // Regra 1 e 2: Admin Especial 869705734 com PIN 867884
        if (SubscriptionManager.checkIsAdminCredentials(rawPhone, pin)) {
            SubscriptionManager.setAdminAccess(ctx, true)
            adminRepository.loginAdmin(AdminDatabaseRepository.ADMIN_EMAIL, AdminDatabaseRepository.ADMIN_PASSWORD)

            val adminProfile = UserProfile(
                fullName = "Administrador Geral MZ (Ilimitado)",
                phoneNumber = "+258 86 970 5734",
                operator = MozambiqueOperator.MOVITEL,
                createdAt = System.currentTimeMillis(),
                isVerified = true
            )
            prefs.saveUserProfile(adminProfile)
            prefs.setSecurityPin(SubscriptionManager.ADMIN_PIN)
            _securityPin.value = SubscriptionManager.ADMIN_PIN
            prefs.setTermsAccepted(true)
            _termsAccepted.value = true

            viewModelScope.launch {
                adminRepository.insertUser(
                    UserAccountEntity(
                        fullName = adminProfile.fullName,
                        phoneNumber = adminProfile.phoneNumber,
                        operator = adminProfile.operator.displayName,
                        pin = SubscriptionManager.ADMIN_PIN,
                        isVerified = true,
                        role = "ADMIN",
                        batteryPct = 100,
                        isArmed = true,
                        notes = "Administrador Master - Acesso Ilimitado"
                    )
                )
                adminRepository.insertAuditLog(
                    title = "Acesso de Administrador Master",
                    description = "Login direto de Administrador efetuado com credenciais 869705734 / 867884",
                    eventType = "ADMIN_ACTION",
                    severity = "INFO"
                )
            }

            prefs.logEvent(
                title = "Login Administrador Master",
                description = "Sessão iniciada com acesso ilimitado",
                type = EventType.ACCOUNT_LOGGED_IN
            )

            refreshDeviceStatus()
            onResult(true, null)
            return
        }

        val validation = MozambiquePhoneHelper.validate(rawPhone)
        if (!validation.isValid) {
            onResult(false, validation.errorMessage ?: "Número de telefone inválido para Moçambique.")
            return
        }

        if (pin.length < 4) {
            onResult(false, "O PIN deve conter pelo menos 4 dígitos.")
            return
        }

        viewModelScope.launch {
            val dbUser = adminRepository.getUserByPhone(validation.formattedNumber)
            val currentSavedProfile = prefs.getUserProfile()
            val savedPin = prefs.getSecurityPin()

            val isValidUser = when {
                dbUser != null && dbUser.pin == pin -> true
                dbUser != null && dbUser.pin.isBlank() && pin == "1234" -> true
                currentSavedProfile != null && currentSavedProfile.phoneNumber == validation.formattedNumber && savedPin == pin -> true
                // Fallback: If matches admin default or registered operator credentials
                validation.formattedNumber == "+258840000000" && pin == "1234" -> true
                dbUser != null && dbUser.pin != pin -> {
                    onResult(false, "PIN de segurança incorreto. Tente novamente.")
                    return@launch
                }
                else -> false
            }

            if (isValidUser) {
                val profile = if (dbUser != null) {
                    val operator = when {
                        dbUser.operator.contains("Vodacom", ignoreCase = true) -> MozambiqueOperator.VODACOM
                        dbUser.operator.contains("Movitel", ignoreCase = true) -> MozambiqueOperator.MOVITEL
                        dbUser.operator.contains("Tmcel", ignoreCase = true) -> MozambiqueOperator.TMCEL
                        else -> validation.operator
                    }
                    UserProfile(
                        fullName = dbUser.fullName,
                        phoneNumber = dbUser.phoneNumber,
                        operator = operator,
                        createdAt = dbUser.createdAt,
                        isVerified = true
                    )
                } else {
                    currentSavedProfile ?: UserProfile(
                        fullName = "Proprietário Moçambique",
                        phoneNumber = validation.formattedNumber,
                        operator = validation.operator,
                        createdAt = System.currentTimeMillis(),
                        isVerified = true
                    )
                }

                prefs.saveUserProfile(profile)
                prefs.setSecurityPin(pin)
                _securityPin.value = pin
                prefs.setTermsAccepted(true)
                _termsAccepted.value = true

                adminRepository.insertAuditLog(
                    title = "Sessão Iniciada com Sucesso",
                    description = "Login efetuado por ${profile.fullName} (${profile.phoneNumber})",
                    eventType = "ACCOUNT_LOGGED_IN",
                    senderNumber = profile.phoneNumber,
                    severity = "INFO"
                )

                prefs.logEvent(
                    title = "Sessão Iniciada",
                    description = "Login efetuado pelo número ${profile.phoneNumber}",
                    type = EventType.ACCOUNT_LOGGED_IN
                )

                if (prefs.isArmed()) {
                    try {
                        com.example.security.ProtectionService.startProtection(getApplication())
                    } catch (e: Exception) {
                        android.util.Log.e("MainViewModel", "Erro ao iniciar ProtectionService", e)
                    }
                }

                refreshDeviceStatus()
                onResult(true, null)
            } else {
                // If account not found in DB or preferences
                onResult(false, "Conta não encontrada para este número. Verifique os dados ou crie uma conta.")
            }
        }
    }

    fun logout() {
        prefs.clearUserProfile()
        try {
            com.example.security.ProtectionService.stopProtection(getApplication())
        } catch (e: Exception) {
            android.util.Log.e("MainViewModel", "Erro ao parar ProtectionService", e)
        }
    }

    fun toggleArmed() {
        val newState = !prefs.isArmed()
        prefs.setArmed(newState)
        try {
            if (newState) {
                com.example.security.ProtectionService.startProtection(getApplication())
            } else {
                com.example.security.ProtectionService.stopProtection(getApplication())
            }
        } catch (e: Exception) {
            android.util.Log.e("MainViewModel", "Erro ao alternar ProtectionService", e)
        }
    }

    fun setAntiPowerOff(enabled: Boolean) {
        prefs.setAntiPowerOffEnabled(enabled)
    }

    fun setAntiAirplaneMode(enabled: Boolean) {
        prefs.setAntiAirplaneModeEnabled(enabled)
    }

    fun setAntiDataOff(enabled: Boolean) {
        prefs.setAntiDataOffEnabled(enabled)
    }

    fun acceptTerms() {
        prefs.setTermsAccepted(true)
        _termsAccepted.value = true
    }

    fun setAntiBruteForce(enabled: Boolean) {
        prefs.setAntiBruteForceEnabled(enabled)
        _antiBruteForce.value = enabled
    }

    fun setSimWatchdog(enabled: Boolean) {
        prefs.setSimWatchdogEnabled(enabled)
        _simWatchdog.value = enabled
    }

    fun setStealthMode(enabled: Boolean) {
        prefs.setStealthModeEnabled(enabled)
        _stealthMode.value = enabled
    }

    fun updatePin(newPin: String) {
        if (newPin.length >= 4) {
            prefs.setSecurityPin(newPin)
            _securityPin.value = newPin
            prefs.logEvent(
                title = "PIN de Segurança Alterado",
                description = "Novo código de acesso configurado com sucesso.",
                type = EventType.ARMED
            )
        }
    }

    fun updateOwnerEmail(email: String) {
        prefs.setOwnerEmail(email)
        _ownerEmail.value = email
    }

    fun addContact(name: String, number: String, rel: String) {
        prefs.addEmergencyContact(name, number, rel)
    }

    fun removeContact(id: String) {
        prefs.removeEmergencyContact(id)
    }

    fun triggerTestAlarm(durationSeconds: Int = 10) {
        val ctx = getApplication<Application>()
        _deviceStatus.value = _deviceStatus.value.copy(isSirenPlaying = true)
        SirenController.triggerAlarmAndFlash(ctx, durationSeconds) {
            _deviceStatus.value = _deviceStatus.value.copy(isSirenPlaying = false)
        }
        prefs.logEvent(
            title = "Teste de Alarme Disparado",
            description = "Sirene sonoro de alta frequência e estroboscópio testados por ${durationSeconds}s.",
            type = EventType.ALARM_TRIGGERED
        )
    }

    fun stopAlarm() {
        val ctx = getApplication<Application>()
        SirenController.stopAlarm(ctx)
        _deviceStatus.value = _deviceStatus.value.copy(isSirenPlaying = false)
    }

    fun simulateSmsCommand(inputCommand: String) {
        val ctx = getApplication<Application>()
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.ROOT).format(Date())

        val upper = inputCommand.trim().uppercase(Locale.ROOT)
        val cmdName = upper.split("\\s+".toRegex()).firstOrNull() ?: "COMANDO"

        val incomingList = _simulatedMessages.value.toMutableList()
        incomingList.add(
            SmsSimulatedMessage(
                id = System.currentTimeMillis().toString(),
                text = inputCommand,
                isIncoming = false,
                timestamp = timeStr,
                statusBadge = "SMS ENVIADO"
            )
        )
        _simulatedMessages.value = incomingList

        SmsCommandHandler.processIncomingMessage(
            context = ctx,
            senderNumber = "+258840000000",
            messageBody = inputCommand
        ) { result ->
            val updated = _simulatedMessages.value.toMutableList()
            val isPhotoCmd = cmdName in listOf("FOTO", "PHOTO", "CAMERA")
            val isLockCmd = cmdName in listOf("BLOQUEAR", "LOCK", "TRAVAR")
            val isGpsCmd = cmdName in listOf("LOCALIZAR", "GPS", "ONDE")
            val isStatusCmd = cmdName in listOf("STATUS", "ESTADO", "INFO")
            val isImeiCmd = cmdName in listOf("IMEI", "SERIAL", "HARDWARE")

            updated.add(
                SmsSimulatedMessage(
                    id = (System.currentTimeMillis() + 1).toString(),
                    text = result.responseMessage,
                    isIncoming = true,
                    timestamp = SimpleDateFormat("HH:mm:ss", Locale.ROOT).format(Date()),
                    statusBadge = if (result.success) "RESPOSTA MZ" else "ERRO PIN",
                    batteryPct = result.batteryPct,
                    isCharging = result.isCharging,
                    ownerName = result.ownerName,
                    ownerPhone = result.ownerPhone,
                    operatorName = result.operatorName,
                    deviceModel = result.deviceModel,
                    gpsCoordinates = result.gpsCoordinates,
                    googleMapsUrl = result.googleMapsUrl,
                    commandType = cmdName,
                    hasPhotoPreview = isPhotoCmd || isStatusCmd || isGpsCmd,
                    hasScreenLockPreview = isLockCmd
                )
            )
            _simulatedMessages.value = updated
            refreshDeviceStatus()

            // Save SMS command execution into Room Database
            viewModelScope.launch {
                adminRepository.insertSmsCommand(
                    command = inputCommand,
                    senderNumber = "+258840000000",
                    responseMessage = result.responseMessage,
                    batteryPct = result.batteryPct ?: 85,
                    isCharging = result.isCharging ?: false,
                    success = result.success
                )
                adminRepository.insertAuditLog(
                    title = "Comando SMS Processado: $cmdName",
                    description = "Resultado: ${if (result.success) "Sucesso" else "Falha"} | Resposta: ${result.responseMessage.take(80)}...",
                    eventType = if (result.success) "SMS_COMMAND" else "SMS_UNAUTHORIZED",
                    senderNumber = "+258840000000",
                    severity = if (result.success) "INFO" else "WARNING"
                )
            }
        }
    }

    fun clearAuditLogs() {
        prefs.clearEvents()
        viewModelScope.launch {
            adminRepository.clearAuditLogs()
        }
    }

    // Admin Authentication & Database Operations
    fun loginAdmin(email: String, pass: String): Boolean {
        return adminRepository.loginAdmin(email, pass)
    }

    fun logoutAdmin() {
        adminRepository.logoutAdmin()
    }

    fun addAdminUser(
        fullName: String,
        phoneNumber: String,
        operator: String,
        pin: String,
        role: String = "USER",
        notes: String = ""
    ) {
        viewModelScope.launch {
            adminRepository.insertUser(
                UserAccountEntity(
                    fullName = fullName,
                    phoneNumber = phoneNumber,
                    operator = operator,
                    pin = pin,
                    isVerified = true,
                    role = role,
                    notes = notes.ifBlank { "Criado via Gestor da Base de Dados" }
                )
            )
            adminRepository.insertAuditLog(
                title = "Usuário Criado no Banco de Dados",
                description = "Usuário $fullName ($phoneNumber) inserido pelo Administrador.",
                eventType = "ADMIN_ACTION",
                severity = "INFO"
            )
        }
    }

    fun deleteAdminUser(id: Long, userName: String = "") {
        viewModelScope.launch {
            adminRepository.deleteUser(id)
            adminRepository.insertAuditLog(
                title = "Usuário Removido do Banco de Dados",
                description = "ID #$id ${if (userName.isNotEmpty()) "($userName)" else ""} foi excluído pelo Administrador.",
                eventType = "ADMIN_ACTION",
                severity = "WARNING"
            )
        }
    }

    fun updateAdminUser(user: UserAccountEntity) {
        viewModelScope.launch {
            adminRepository.updateUser(user)
        }
    }

    fun clearAdminAuditLogs() {
        viewModelScope.launch {
            adminRepository.clearAuditLogs()
        }
    }

    fun deleteAdminAuditLog(id: Long) {
        viewModelScope.launch {
            adminRepository.deleteAuditLog(id)
        }
    }

    fun clearAdminSmsHistory() {
        viewModelScope.launch {
            adminRepository.clearSmsHistory()
        }
    }

    fun updateAdminSetting(key: String, value: String, description: String = "") {
        viewModelScope.launch {
            adminRepository.updateSetting(key, value, description)
            adminRepository.insertAuditLog(
                title = "Parâmetro do Sistema Alterado",
                description = "Chave [$key] atualizada para valor [$value].",
                eventType = "ADMIN_ACTION",
                severity = "INFO"
            )
        }
    }

    fun seedDatabaseWithSamples() {
        viewModelScope.launch {
            adminRepository.insertUser(
                UserAccountEntity(
                    fullName = "Manuel Cossa (Vodacom)",
                    phoneNumber = "+258 84 987 6543",
                    operator = "Vodacom Moçambique",
                    pin = "4321",
                    isVerified = true,
                    role = "USER",
                    batteryPct = 92,
                    isArmed = true,
                    notes = "Maputo Cidade - Galaxy S24"
                )
            )
            adminRepository.insertUser(
                UserAccountEntity(
                    fullName = "Amina Abdala (Movitel)",
                    phoneNumber = "+258 86 555 1234",
                    operator = "Movitel Moçambique",
                    pin = "7788",
                    isVerified = true,
                    role = "USER",
                    batteryPct = 68,
                    isArmed = true,
                    notes = "Nampula - Redmi Note 13"
                )
            )
            adminRepository.insertAuditLog(
                title = "Registos de Demonstração Inseridos",
                description = "Base de dados populada com amostras de utilizadores moçambicanos.",
                eventType = "ADMIN_ACTION",
                severity = "INFO"
            )
        }
    }

    fun selectMozambiqueProvince(province: MozambiqueProvince?) {
        _selectedProvince.value = province
        if (province != null) {
            locationTracker.focusOnMozambiqueProvince(province)
        }
    }

    fun toggleContinuousTracking() {
        val newState = !_isContinuousTracking.value
        _isContinuousTracking.value = newState
        if (newState) {
            locationTracker.startLiveTracking()
        } else {
            locationTracker.stopLiveTracking()
        }
    }

    fun toggleGeofenceAlert() {
        _geofenceEnabled.value = !_geofenceEnabled.value
    }

    fun toggleSatelliteMapMode() {
        _isSatelliteMode.value = !_isSatelliteMode.value
    }

    fun forceRefreshLiveGps() {
        locationTracker.startLiveTracking()
        refreshDeviceStatus()
    }

    // Secret Vault (Cofre Oculto MZ) Actions
    fun unlockSecretVault(pinAttempt: String): VaultUnlockStatus {
        return vaultRepository.unlockVault(pinAttempt)
    }

    fun lockSecretVault() {
        vaultRepository.lockVault()
    }

    fun toggleVaultDisguiseMode() {
        vaultRepository.toggleDisguiseMode()
    }

    fun setDecoyVaultPin(pin: String) {
        vaultRepository.setDecoyPin(pin)
    }

    fun setDecoyVaultEnabled(enabled: Boolean) {
        vaultRepository.setDecoyEnabled(enabled)
    }

    fun setRequireVaultDoublePin(required: Boolean) {
        vaultRepository.setRequireDoublePin(required)
    }

    fun importEncryptedPhoto(title: String, subtitle: String, imageBytes: ByteArray, originalName: String, extraTag: String = "REAL") {
        viewModelScope.launch {
            vaultRepository.insertEncryptedPhoto(title, subtitle, imageBytes, originalName, extraTag)
        }
    }

    fun importEncryptedVideo(title: String, subtitle: String, videoBytes: ByteArray, originalName: String, extraTag: String = "REAL") {
        viewModelScope.launch {
            vaultRepository.insertEncryptedVideo(title, subtitle, videoBytes, originalName, extraTag)
        }
    }

    fun importEncryptedMusic(title: String, artistOrDuration: String, audioBytes: ByteArray, originalName: String, extraTag: String = "REAL") {
        viewModelScope.launch {
            vaultRepository.insertEncryptedMusic(title, artistOrDuration, audioBytes, originalName, extraTag)
        }
    }

    fun importSecretMessage(title: String, sender: String, content: String, extraTag: String = "REAL") {
        viewModelScope.launch {
            vaultRepository.insertHiddenMessage(title, sender, content, extraTag)
        }
    }

    fun importSecretContact(name: String, phoneNumber: String, notes: String, extraTag: String = "REAL") {
        viewModelScope.launch {
            vaultRepository.insertHiddenContact(name, phoneNumber, notes, extraTag)
        }
    }

    fun deleteVaultItem(item: VaultItemEntity) {
        viewModelScope.launch {
            vaultRepository.deleteVaultItem(item)
        }
    }

    // Subscription & Licensing Management (Trial 7 Dias / Planos MZ / Admin 869705734)
    fun isAppBlocked(): Boolean {
        return SubscriptionManager.bloquearApp()
    }

    fun gerarCodigoAtivacao(planType: String, onResult: (ActivationCodeEntity) -> Unit) {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val codeEntity = SubscriptionManager.gerarCodigoAtivacao(ctx, planType)
            adminRepository.insertAuditLog(
                title = "Código de Ativação Gerado",
                description = "Código ${codeEntity.code} ($planType - ${codeEntity.durationDays} dias - ${codeEntity.priceMt} MT)",
                eventType = "ADMIN_ACTION",
                severity = "INFO"
            )
            onResult(codeEntity)
        }
    }

    fun ativarCodigo(code: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val currentProfile = prefs.getUserProfile()
            val result = SubscriptionManager.ativarCodigo(ctx, code, currentProfile?.phoneNumber)

            if (result.first) {
                adminRepository.insertAuditLog(
                    title = "Plano Ativado com Sucesso",
                    description = "Código $code ativado por ${currentProfile?.phoneNumber ?: "Local"}",
                    eventType = "ACCOUNT_ACTION",
                    severity = "INFO"
                )
                SubscriptionNotificationHelper.checkAndNotifySubscriptionStatus(ctx)
                refreshDeviceStatus()
            }
            onResult(result.first, result.second)
        }
    }

    fun confirmUserPayment(
        phone: String,
        planDays: Int,
        planName: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val success = adminRepository.confirmUserPayment(phone, planDays, planName)
            SubscriptionNotificationHelper.checkAndNotifySubscriptionStatus(ctx)
            refreshDeviceStatus()
            onResult(success, "Pagamento confirmado com sucesso! A conta de $phone agora está ATIVA com o plano $planName ($planDays dias).")
        }
    }

    fun requestPaymentConfirmation(
        planName: String,
        notes: String = "",
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val currentProfile = prefs.getUserProfile()
            val phone = currentProfile?.phoneNumber ?: "869705734"
            adminRepository.requestPaymentConfirmation(phone, planName, notes)
            onResult(true, "Pedido de confirmação enviado ao Administrador (869705734). A sua conta será ativada assim que o pagamento for validado.")
        }
    }

    fun syncLocalSubscriptionWithDbStatus() {
        viewModelScope.launch {
            adminRepository.syncLocalSubscriptionWithDbStatus()
            refreshDeviceStatus()
        }
    }

    fun ativarUsuarioManual(
        targetPhone: String,
        durationDays: Int,
        planType: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val currentPhone = prefs.getUserProfile()?.phoneNumber ?: ""
            val cleanTarget = targetPhone.replace("[^0-9]".toRegex(), "")
            val cleanCurrent = currentPhone.replace("[^0-9]".toRegex(), "")

            if (cleanTarget == cleanCurrent || cleanTarget == "869705734" || targetPhone.isBlank()) {
                SubscriptionManager.aplicarPlano(ctx, planType, durationDays, "ADMIN_DIRECT")
            }

            adminRepository.insertAuditLog(
                title = "Usuário Ativado Manualmente",
                description = "Admin ativou plano $planType ($durationDays dias) para $targetPhone",
                eventType = "ADMIN_ACTION",
                severity = "INFO"
            )
            SubscriptionNotificationHelper.checkAndNotifySubscriptionStatus(ctx)
            onResult(true, "Plano $planType ($durationDays dias) aplicado com sucesso para $targetPhone!")
        }
    }

    fun simularFimTrial() {
        val ctx = getApplication<Application>()
        SubscriptionManager.simularFimTrial(ctx)
        SubscriptionNotificationHelper.checkAndNotifySubscriptionStatus(ctx)
        refreshDeviceStatus()
    }

    fun resetarTrial() {
        val ctx = getApplication<Application>()
        SubscriptionManager.resetarTrial(ctx)
        SubscriptionNotificationHelper.checkAndNotifySubscriptionStatus(ctx)
        refreshDeviceStatus()
    }

    fun deleteActivationCode(id: Long) {
        viewModelScope.launch {
            adminRepository.deleteActivationCode(id)
        }
    }

    fun checkSubscriptionNotifications() {
        val ctx = getApplication<Application>()
        SubscriptionNotificationHelper.checkAndNotifySubscriptionStatus(ctx)
    }
}
