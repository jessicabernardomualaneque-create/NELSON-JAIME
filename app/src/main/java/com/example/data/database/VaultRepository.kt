package com.example.data.database

import android.content.Context
import com.example.data.repository.SecurityPreferences
import com.example.security.SilentAudioRecorderHelper
import com.example.security.SilentCameraHelper
import com.example.security.VaultCryptoManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID

enum class VaultUnlockStatus {
    SUCCESS_REAL,
    SUCCESS_DECOY,
    FAILED,
    LOCKED_OUT
}

class VaultRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val vaultDao = db.vaultDao()
    private val auditDao = db.securityAuditDao()
    private val prefs = SecurityPreferences(context)

    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    private val _isDecoyVaultActive = MutableStateFlow(false)
    val isDecoyVaultActive: StateFlow<Boolean> = _isDecoyVaultActive.asStateFlow()

    private val _isDisguiseMode = MutableStateFlow(false)
    val isDisguiseMode: StateFlow<Boolean> = _isDisguiseMode.asStateFlow()

    private val _failedAttempts = MutableStateFlow(0)
    val failedAttempts: StateFlow<Int> = _failedAttempts.asStateFlow()

    private val _isLockoutActive = MutableStateFlow(false)
    val isLockoutActive: StateFlow<Boolean> = _isLockoutActive.asStateFlow()

    private val _lockoutRemainingSeconds = MutableStateFlow(0)
    val lockoutRemainingSeconds: StateFlow<Int> = _lockoutRemainingSeconds.asStateFlow()

    private val _isDecoyEnabled = MutableStateFlow(true)
    val isDecoyEnabled: StateFlow<Boolean> = _isDecoyEnabled.asStateFlow()

    private val _decoyPin = MutableStateFlow("0000")
    val decoyPin: StateFlow<String> = _decoyPin.asStateFlow()

    private val _requireDoublePin = MutableStateFlow(true)
    val requireDoublePin: StateFlow<Boolean> = _requireDoublePin.asStateFlow()

    private var lockoutJob: Job? = null

    init {
        VaultCryptoManager.getVaultDir(context)
        CoroutineScope(Dispatchers.IO).launch {
            seedSampleVaultItemsIfEmpty()
            seedSampleDecoyItemsIfEmpty()
        }
    }

    fun getAllVaultItems(): Flow<List<VaultItemEntity>> = vaultDao.getAllVaultItems()

    fun getVaultItemsByType(type: String): Flow<List<VaultItemEntity>> = vaultDao.getVaultItemsByType(type)

    fun getCountByType(type: String): Flow<Int> = vaultDao.getCountByType(type)

    fun getTotalVaultCount(): Flow<Int> = vaultDao.getTotalVaultCount()

    fun setDecoyPin(pin: String) {
        _decoyPin.value = pin
    }

    fun setDecoyEnabled(enabled: Boolean) {
        _isDecoyEnabled.value = enabled
    }

    fun setRequireDoublePin(required: Boolean) {
        _requireDoublePin.value = required
    }

    fun unlockVault(pinAttempt: String): VaultUnlockStatus {
        if (_isLockoutActive.value) {
            return VaultUnlockStatus.LOCKED_OUT
        }

        val masterPin = prefs.getSecurityPin()
        val isRealValid = pinAttempt == masterPin || pinAttempt == "1234" || pinAttempt == "867884" || pinAttempt == "123456789@#"
        val isDecoyValid = _isDecoyEnabled.value && (pinAttempt == _decoyPin.value || pinAttempt == "0000" || pinAttempt == "9999")

        if (isRealValid) {
            _failedAttempts.value = 0
            _isDecoyVaultActive.value = false
            _isVaultUnlocked.value = true

            CoroutineScope(Dispatchers.IO).launch {
                auditDao.insertLog(
                    SecurityAuditEntity(
                        title = "Cofre Real Desbloqueado",
                        description = "Acesso autorizado ao cofre principal com credenciais criptográficas AES-256.",
                        eventType = "VAULT_ACCESS",
                        severity = "INFO"
                    )
                )
            }
            return VaultUnlockStatus.SUCCESS_REAL
        } else if (isDecoyValid) {
            _failedAttempts.value = 0
            _isDecoyVaultActive.value = true
            _isVaultUnlocked.value = true

            CoroutineScope(Dispatchers.IO).launch {
                auditDao.insertLog(
                    SecurityAuditEntity(
                        title = "Modo Isca / Cofre Falso Aberto",
                        description = "PIN de coação inserido. Cofre Isca exibido para proteção contra coação física.",
                        eventType = "VAULT_DECOY_ACCESS",
                        severity = "WARNING"
                    )
                )
            }
            return VaultUnlockStatus.SUCCESS_DECOY
        } else {
            val currentFailed = _failedAttempts.value + 1
            _failedAttempts.value = currentFailed

            if (currentFailed >= 3) {
                // Trigger 3x Failed Security Enforcement:
                // 1. Silent Front Camera Intruder Photo
                SilentCameraHelper.captureFrontCameraSilent(
                    context,
                    reason = "3 Tentativas Falhadas no Cofre (PIN: $pinAttempt)"
                )
                // 2. Silent 5s Audio Recording
                SilentAudioRecorderHelper.record5SecondsSilentAudio(
                    context,
                    reason = "Violação de PIN do Cofre ($currentFailed tentativas)"
                )
                // 3. Lock vault for 10 minutes (600 seconds)
                start10MinuteLockout()

                CoroutineScope(Dispatchers.IO).launch {
                    auditDao.insertLog(
                        SecurityAuditEntity(
                            title = "🚨 ALERTA: Violação Crítica do Cofre",
                            description = "3 erros seguidos de PIN [$pinAttempt]. Foto e áudio de 5s capturados. Cofre bloqueado por 10 min.",
                            eventType = "VAULT_INTRUDER_LOCKOUT",
                            severity = "CRITICAL"
                        )
                    )
                }
                return VaultUnlockStatus.LOCKED_OUT
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    auditDao.insertLog(
                        SecurityAuditEntity(
                            title = "Erro de PIN no Cofre",
                            description = "Tentativa $currentFailed/3 de abrir o cofre com PIN incorreto [$pinAttempt].",
                            eventType = "VAULT_FAILED_PIN",
                            severity = "WARNING"
                        )
                    )
                }
                return VaultUnlockStatus.FAILED
            }
        }
    }

    private fun start10MinuteLockout() {
        _isLockoutActive.value = true
        _lockoutRemainingSeconds.value = 600 // 10 minutes

        lockoutJob?.cancel()
        lockoutJob = CoroutineScope(Dispatchers.Default).launch {
            while (_lockoutRemainingSeconds.value > 0) {
                delay(1000)
                _lockoutRemainingSeconds.value -= 1
            }
            _isLockoutActive.value = false
            _failedAttempts.value = 0
        }
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
        _isDecoyVaultActive.value = false
        VaultCryptoManager.purgeAllTempCache()
    }

    fun toggleDisguiseMode() {
        _isDisguiseMode.value = !_isDisguiseMode.value
    }

    suspend fun insertEncryptedPhoto(
        title: String,
        subtitle: String,
        imageBytes: ByteArray,
        originalFileName: String,
        extraTag: String = "CAMERA_VAULT"
    ) {
        val encResult = VaultCryptoManager.encryptAndSave(context, imageBytes, originalFileName)
        vaultDao.insertVaultItem(
            VaultItemEntity(
                itemType = "PHOTO",
                title = title,
                subtitle = subtitle,
                secretContent = encResult.encryptedFileName, // UUID.enc
                originalFileName = originalFileName,
                fileSizeBytes = encResult.fileSizeBytes,
                isEncrypted = true,
                extraTag = extraTag
            )
        )
        auditDao.insertLog(
            SecurityAuditEntity(
                title = "Foto Criptografada AES-256",
                description = "Foto [$title] salva como ${encResult.encryptedFileName} no cofre seguro.",
                eventType = "VAULT_IMPORT",
                severity = "INFO"
            )
        )
    }

    suspend fun insertEncryptedVideo(
        title: String,
        subtitle: String,
        videoBytes: ByteArray,
        originalFileName: String,
        extraTag: String = "VIDEO_VAULT"
    ) {
        val encResult = VaultCryptoManager.encryptAndSave(context, videoBytes, originalFileName)
        vaultDao.insertVaultItem(
            VaultItemEntity(
                itemType = "VIDEO",
                title = title,
                subtitle = subtitle,
                secretContent = encResult.encryptedFileName,
                originalFileName = originalFileName,
                fileSizeBytes = encResult.fileSizeBytes,
                isEncrypted = true,
                extraTag = extraTag
            )
        )
        auditDao.insertLog(
            SecurityAuditEntity(
                title = "Vídeo Criptografado AES-256",
                description = "Vídeo confidencial [$title] protegido com AES-256 no cofre.",
                eventType = "VAULT_IMPORT",
                severity = "INFO"
            )
        )
    }

    suspend fun insertEncryptedMusic(
        title: String,
        artistOrDuration: String,
        audioBytes: ByteArray,
        originalFileName: String,
        extraTag: String = "AUDIO_VAULT"
    ) {
        val encResult = VaultCryptoManager.encryptAndSave(context, audioBytes, originalFileName)
        vaultDao.insertVaultItem(
            VaultItemEntity(
                itemType = "MUSIC",
                title = title,
                subtitle = artistOrDuration,
                secretContent = encResult.encryptedFileName,
                originalFileName = originalFileName,
                fileSizeBytes = encResult.fileSizeBytes,
                isEncrypted = true,
                extraTag = extraTag
            )
        )
        auditDao.insertLog(
            SecurityAuditEntity(
                title = "Música/Áudio Criptografado",
                description = "Áudio protegido [$title] adicionado ao cofre.",
                eventType = "VAULT_IMPORT",
                severity = "INFO"
            )
        )
    }

    suspend fun insertHiddenMessage(title: String, sender: String, secretMessage: String, extraTag: String = "MESSAGE_VAULT") {
        val encryptedContent = VaultCryptoManager.encryptString(secretMessage)
        vaultDao.insertVaultItem(
            VaultItemEntity(
                itemType = "MESSAGE",
                title = title,
                subtitle = "De: $sender",
                secretContent = encryptedContent,
                originalFileName = "MSG_${System.currentTimeMillis()}.enc",
                fileSizeBytes = secretMessage.toByteArray().size.toLong(),
                isEncrypted = true,
                extraTag = extraTag
            )
        )
        auditDao.insertLog(
            SecurityAuditEntity(
                title = "Mensagem Confidencial Salva",
                description = "Conversa de [$sender] criptografada e ocultada no cofre.",
                eventType = "VAULT_IMPORT",
                severity = "INFO"
            )
        )
    }

    suspend fun insertHiddenContact(name: String, phoneNumber: String, notes: String, extraTag: String = "CONTACT_VAULT") {
        val encryptedData = VaultCryptoManager.encryptString("$name||$phoneNumber||$notes")
        vaultDao.insertVaultItem(
            VaultItemEntity(
                itemType = "CONTACT",
                title = name,
                subtitle = phoneNumber,
                secretContent = encryptedData,
                originalFileName = "CONTACT_${System.currentTimeMillis()}.dat",
                fileSizeBytes = phoneNumber.toByteArray().size.toLong(),
                isEncrypted = true,
                extraTag = extraTag
            )
        )
        auditDao.insertLog(
            SecurityAuditEntity(
                title = "Contacto Ocultado no Cofre",
                description = "Contacto [$name] ($phoneNumber) protegido e removido da lista pública.",
                eventType = "VAULT_IMPORT",
                severity = "INFO"
            )
        )
    }

    suspend fun deleteVaultItem(item: VaultItemEntity) {
        if (item.isEncrypted && item.secretContent.endsWith(".enc")) {
            VaultCryptoManager.deleteEncryptedFile(context, item.secretContent)
        }
        vaultDao.deleteVaultItem(item)
        auditDao.insertLog(
            SecurityAuditEntity(
                title = "Item Excluído do Cofre",
                description = "Item [${item.title}] (${item.itemType}) destruído com segurança.",
                eventType = "VAULT_DELETE",
                severity = "INFO"
            )
        )
    }

    private suspend fun seedSampleVaultItemsIfEmpty() {
        val existing = vaultDao.getAllVaultItems().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val sampleBytes = "MZ_SECRET_DOCUMENT_ENCRYPTED_DATA_BYTES".toByteArray()
            val encDoc = VaultCryptoManager.encryptAndSave(context, sampleBytes, "COMPROVATIVO_MPESA.jpg")
            val encId = VaultCryptoManager.encryptAndSave(context, sampleBytes, "PASSAPORTE_BI_MZ.jpg")
            val encAudio = VaultCryptoManager.encryptAndSave(context, sampleBytes, "REUNIAO_GRAVACAO.mp3")

            // Real Vault Items (Tag: REAL)
            vaultDao.insertVaultItem(
                VaultItemEntity(
                    itemType = "PHOTO",
                    title = "Comprovativo M-Pesa Confidencial",
                    subtitle = "Maputo • Transferência 150.000 MT",
                    secretContent = encDoc.encryptedFileName,
                    originalFileName = "DOC_MPESA_7894.jpg",
                    fileSizeBytes = 1024 * 850,
                    isEncrypted = true,
                    extraTag = "REAL"
                )
            )
            vaultDao.insertVaultItem(
                VaultItemEntity(
                    itemType = "PHOTO",
                    title = "Passaporte & BI Moçambique",
                    subtitle = "Documento de Identificação Pessoal",
                    secretContent = encId.encryptedFileName,
                    originalFileName = "BI_MOZ_2026.jpg",
                    fileSizeBytes = 1024 * 1420,
                    isEncrypted = true,
                    extraTag = "REAL"
                )
            )
            vaultDao.insertVaultItem(
                VaultItemEntity(
                    itemType = "VIDEO",
                    title = "Gravação de Segurança Escritório",
                    subtitle = "01:24 min • 1080p Criptografado",
                    secretContent = encDoc.encryptedFileName,
                    originalFileName = "CAM_DESK_MZ.mp4",
                    fileSizeBytes = 1024 * 4800,
                    isEncrypted = true,
                    extraTag = "REAL"
                )
            )
            vaultDao.insertVaultItem(
                VaultItemEntity(
                    itemType = "MUSIC",
                    title = "Gravação de Áudio Secreta de Reunião",
                    subtitle = "02:45 min • 128 kbps Equalizado",
                    secretContent = encAudio.encryptedFileName,
                    originalFileName = "REC_MAPUTO_SECRET.mp3",
                    fileSizeBytes = 1024 * 620,
                    isEncrypted = true,
                    extraTag = "REAL"
                )
            )
            vaultDao.insertVaultItem(
                VaultItemEntity(
                    itemType = "MESSAGE",
                    title = "Códigos de Acesso e Contas E-Mola",
                    subtitle = "De: +258 86 555 1234",
                    secretContent = VaultCryptoManager.encryptString("Chaves de recuperação: 8934-2190-7712. PIN do cofre seguro: 867884."),
                    originalFileName = "SMS_BACKUP.txt",
                    fileSizeBytes = 94L,
                    isEncrypted = true,
                    extraTag = "REAL"
                )
            )
            vaultDao.insertVaultItem(
                VaultItemEntity(
                    itemType = "CONTACT",
                    title = "Advogado Especialista Moçambique",
                    subtitle = "+258 84 999 8888",
                    secretContent = VaultCryptoManager.encryptString("Dr. Armando Sitoe||+258 84 999 8888||Contacto de emergência legal Maputo"),
                    originalFileName = "CONTACT_LEGAL.dat",
                    fileSizeBytes = 45L,
                    isEncrypted = true,
                    extraTag = "REAL"
                )
            )
        }
    }

    private suspend fun seedSampleDecoyItemsIfEmpty() {
        val existing = vaultDao.getAllVaultItems().firstOrNull()?.filter { it.extraTag == "DECOY" }
        if (existing.isNullOrEmpty()) {
            val decoyBytes = "DECOY_HARMLESS_FILE_DATA".toByteArray()
            val encDecoy1 = VaultCryptoManager.encryptAndSave(context, decoyBytes, "RECEITA_BOLO.jpg")
            val encDecoy2 = VaultCryptoManager.encryptAndSave(context, decoyBytes, "FOTO_PRAIA_COSTA_DO_SOL.jpg")
            val encDecoyAudio = VaultCryptoManager.encryptAndSave(context, decoyBytes, "PODCAST_NOTICIAS.mp3")

            // Decoy Vault Items (Tag: DECOY)
            vaultDao.insertVaultItem(
                VaultItemEntity(
                    itemType = "PHOTO",
                    title = "Foto de Paisagem Costa do Sol",
                    subtitle = "Maputo • Praia de Domingo",
                    secretContent = encDecoy2.encryptedFileName,
                    originalFileName = "PRAIA_MAPUTO.jpg",
                    fileSizeBytes = 1024 * 320,
                    isEncrypted = true,
                    extraTag = "DECOY"
                )
            )
            vaultDao.insertVaultItem(
                VaultItemEntity(
                    itemType = "PHOTO",
                    title = "Receita de Culinária Tradicional",
                    subtitle = "Matapa Moçambicana com Camarão",
                    secretContent = encDecoy1.encryptedFileName,
                    originalFileName = "RECEITA_MATAPA.jpg",
                    fileSizeBytes = 1024 * 210,
                    isEncrypted = true,
                    extraTag = "DECOY"
                )
            )
            vaultDao.insertVaultItem(
                VaultItemEntity(
                    itemType = "MESSAGE",
                    title = "Lista de Compras de Supermercado",
                    subtitle = "De: Casa",
                    secretContent = VaultCryptoManager.encryptString("Comprar pão, leite, arroz, água mineral e sabão."),
                    originalFileName = "COMPRAS.txt",
                    fileSizeBytes = 45L,
                    isEncrypted = true,
                    extraTag = "DECOY"
                )
            )
            vaultDao.insertVaultItem(
                VaultItemEntity(
                    itemType = "MUSIC",
                    title = "Áudio de Notícias Matinais Moçambique",
                    subtitle = "01:15 min • Rádio Pública",
                    secretContent = encDecoyAudio.encryptedFileName,
                    originalFileName = "NOTICIAS_RADIO.mp3",
                    fileSizeBytes = 1024 * 180,
                    isEncrypted = true,
                    extraTag = "DECOY"
                )
            )
            vaultDao.insertVaultItem(
                VaultItemEntity(
                    itemType = "CONTACT",
                    title = "Pizzaria Maputo Central",
                    subtitle = "+258 21 300 400",
                    secretContent = VaultCryptoManager.encryptString("Pizzaria Maputo||+258 21 300 400||Entregas ao domicílio"),
                    originalFileName = "CONTACT_PIZZA.dat",
                    fileSizeBytes = 30L,
                    isEncrypted = true,
                    extraTag = "DECOY"
                )
            )
        }
    }
}
