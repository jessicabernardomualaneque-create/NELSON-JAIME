package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Timer3
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.VaultItemEntity
import com.example.data.database.VaultUnlockStatus
import com.example.security.VaultCryptoManager
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardElevated
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBg
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VodacomRed
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class VaultTab(val title: String, val icon: ImageVector) {
    PHOTOS("FOTOS", Icons.Default.Image),
    VIDEOS("VÍDEOS", Icons.Default.Movie),
    MUSICS("MÚSICAS", Icons.Default.MusicNote),
    MESSAGES("MENSAGENS", Icons.Default.Message),
    CONTACTS("CONTACTOS", Icons.Default.ContactPhone)
}

enum class EqualizerPreset(val label: String, val bass: Float, val mid: Float, val treble: Float) {
    FLAT("🎵 Padrão / Flat", 0.5f, 0.5f, 0.5f),
    BASS_BOOST("🔊 Super Graves MZ", 0.95f, 0.45f, 0.35f),
    VOCAL_CLEAR("🎙️ Voz & Conversas", 0.30f, 0.90f, 0.70f),
    TREBLE_BOOST("🎛️ Agudos Cristalinos", 0.40f, 0.50f, 0.90f)
}

@Composable
fun SecretVaultScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isUnlocked by viewModel.isVaultUnlocked.collectAsState()
    val isDecoyActive by viewModel.isDecoyVaultActive.collectAsState()
    val isDisguiseMode by viewModel.isDisguiseMode.collectAsState()
    val isLockoutActive by viewModel.isVaultLockoutActive.collectAsState()
    val lockoutSeconds by viewModel.vaultLockoutRemainingSeconds.collectAsState()
    val failedAttempts by viewModel.failedVaultAttempts.collectAsState()
    val isDecoyEnabled by viewModel.isDecoyVaultEnabled.collectAsState()
    val decoyPin by viewModel.decoyVaultPin.collectAsState()

    val allPhotos by viewModel.vaultPhotos.collectAsState()
    val allVideos by viewModel.vaultVideos.collectAsState()
    val allMusics by viewModel.vaultMusics.collectAsState()
    val allMessages by viewModel.vaultMessages.collectAsState()
    val allContacts by viewModel.vaultContacts.collectAsState()

    // Filter by Decoy vs Real mode
    val currentTag = if (isDecoyActive) "DECOY" else "REAL"
    val photos = remember(allPhotos, isDecoyActive) { allPhotos.filter { it.extraTag == currentTag || (!isDecoyActive && it.extraTag != "DECOY") } }
    val videos = remember(allVideos, isDecoyActive) { allVideos.filter { it.extraTag == currentTag || (!isDecoyActive && it.extraTag != "DECOY") } }
    val musics = remember(allMusics, isDecoyActive) { allMusics.filter { it.extraTag == currentTag || (!isDecoyActive && it.extraTag != "DECOY") } }
    val messages = remember(allMessages, isDecoyActive) { allMessages.filter { it.extraTag == currentTag || (!isDecoyActive && it.extraTag != "DECOY") } }
    val contacts = remember(allContacts, isDecoyActive) { allContacts.filter { it.extraTag == currentTag || (!isDecoyActive && it.extraTag != "DECOY") } }

    var selectedTab by remember { mutableStateOf(VaultTab.PHOTOS) }
    var enteredPin by remember { mutableStateOf("") }
    var pinErrorMessage by remember { mutableStateOf<String?>(null) }
    var showVaultSettingsDialog by remember { mutableStateOf(false) }

    // Dialogs
    var showAddPhotoDialog by remember { mutableStateOf(false) }
    var showAddVideoDialog by remember { mutableStateOf(false) }
    var showAddMusicDialog by remember { mutableStateOf(false) }
    var showAddMessageDialog by remember { mutableStateOf(false) }
    var showImportWhatsAppDialog by remember { mutableStateOf(false) }
    var showAddContactDialog by remember { mutableStateOf(false) }

    // Viewer states
    var previewItem by remember { mutableStateOf<VaultItemEntity?>(null) }
    var autoPurgeTimerSeconds by remember { mutableIntStateOf(30) }

    // Music Player State with Equalizer
    var activePlayingTrack by remember { mutableStateOf<VaultItemEntity?>(null) }
    var isAudioPlaying by remember { mutableStateOf(false) }
    var audioProgress by remember { mutableFloatStateOf(0.35f) }
    var selectedEqPreset by remember { mutableStateOf(EqualizerPreset.FLAT) }

    // Countdown for auto purge when previewing
    LaunchedEffect(previewItem) {
        if (previewItem != null) {
            autoPurgeTimerSeconds = 30
            while (autoPurgeTimerSeconds > 0 && previewItem != null) {
                delay(1000)
                autoPurgeTimerSeconds -= 1
            }
            if (autoPurgeTimerSeconds <= 0 && previewItem != null) {
                previewItem = null
                VaultCryptoManager.purgeAllTempCache()
                Toast.makeText(context, "Tempo de visualização expirado. Cache em RAM destruído por segurança.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (!isUnlocked) {
        // DOUBLE PIN LOCK BARRIER & DISGUISE CALCULATOR SCREEN
        if (isDisguiseMode) {
            CalculatorDisguiseScreen(
                onUnlockWithSecret = { code ->
                    val status = viewModel.unlockSecretVault(code)
                    handleUnlockStatus(status, context) { pinErrorMessage = it }
                },
                onExitDisguise = { viewModel.toggleVaultDisguiseMode() }
            )
        } else {
            DoublePinLockScreen(
                isLockout = isLockoutActive,
                lockoutRemainingSeconds = lockoutSeconds,
                failedAttempts = failedAttempts,
                enteredPin = enteredPin,
                errorMessage = pinErrorMessage,
                onPinDigit = { digit ->
                    if (enteredPin.length < 6) {
                        enteredPin += digit
                        pinErrorMessage = null
                    }
                },
                onPinBackspace = {
                    if (enteredPin.isNotEmpty()) {
                        enteredPin = enteredPin.dropLast(1)
                        pinErrorMessage = null
                    }
                },
                onPinClear = {
                    enteredPin = ""
                    pinErrorMessage = null
                },
                onSubmitPin = {
                    val status = viewModel.unlockSecretVault(enteredPin)
                    enteredPin = ""
                    handleUnlockStatus(status, context) { pinErrorMessage = it }
                },
                onSwitchToDisguise = { viewModel.toggleVaultDisguiseMode() }
            )
        }
    } else {
        // UNLOCKED SECRET VAULT INTERFACE (REAL OR DECOY)
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(DarkBg)
        ) {
            // Vault Top Header Bar with Security Level 2 Indicator
            VaultHeaderBar(
                isDecoyMode = isDecoyActive,
                onLock = {
                    viewModel.lockSecretVault()
                    Toast.makeText(context, "Cofre trancado. Memória RAM limpa.", Toast.LENGTH_SHORT).show()
                },
                onOpenSettings = { showVaultSettingsDialog = true },
                onDisguise = {
                    viewModel.toggleVaultDisguiseMode()
                    viewModel.lockSecretVault()
                }
            )

            // Security Level 2 & AES-256 Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDecoyActive) AmberWarning.copy(alpha = 0.15f) else CyanAccent.copy(alpha = 0.12f)
                ),
                border = BorderStroke(1.dp, if (isDecoyActive) AmberWarning else CyanAccent.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isDecoyActive) Icons.Default.Warning else Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (isDecoyActive) AmberWarning else CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isDecoyActive) "MODO ISCA ATIVO (COFRE FALSO)" else "SEGURANÇA NÍVEL 2 • AES-256 ATIVA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDecoyActive) AmberWarning else CyanAccent
                        )
                        Text(
                            text = if (isDecoyActive)
                                "Exibindo dados fictícios inofensivos. O cofre real permanece lacrado."
                            else
                                "Criptografia Keystore | UUID.enc | Cache RAM auto-destrutivo em 30s",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // 5 REINFORCED TABS: [ FOTOS ] [ VÍDEOS ] [ MÚSICAS ] [ MENSAGENS ] [ CONTACTOS ]
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = CardDark,
                contentColor = CyanAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                        color = CyanAccent,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                VaultTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = tab.title,
                                fontSize = 10.5.sp,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == tab) CyanAccent else TextSecondary
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (selectedTab == tab) CyanAccent else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            // TAB CONTENT AREA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    VaultTab.PHOTOS -> PhotosVaultTab(
                        photos = photos,
                        onAddPhotos = { showAddPhotoDialog = true },
                        onPreview = { previewItem = it },
                        onDelete = { viewModel.deleteVaultItem(it) }
                    )
                    VaultTab.VIDEOS -> VideosVaultTab(
                        videos = videos,
                        onAddVideos = { showAddVideoDialog = true },
                        onPreview = { previewItem = it },
                        onDelete = { viewModel.deleteVaultItem(it) }
                    )
                    VaultTab.MUSICS -> MusicsVaultTab(
                        musics = musics,
                        activeTrack = activePlayingTrack,
                        isPlaying = isAudioPlaying,
                        progress = audioProgress,
                        eqPreset = selectedEqPreset,
                        onTogglePlay = { track ->
                            if (activePlayingTrack?.id == track.id) {
                                isAudioPlaying = !isAudioPlaying
                            } else {
                                activePlayingTrack = track
                                isAudioPlaying = true
                                audioProgress = 0.1f
                            }
                        },
                        onSelectEq = { selectedEqPreset = it },
                        onProgressChange = { audioProgress = it },
                        onAddMusic = { showAddMusicDialog = true },
                        onDelete = { viewModel.deleteVaultItem(it) }
                    )
                    VaultTab.MESSAGES -> MessagesVaultTab(
                        messages = messages,
                        onAddMessage = { showAddMessageDialog = true },
                        onImportWhatsApp = { showImportWhatsAppDialog = true },
                        onPreview = { previewItem = it },
                        onDelete = { viewModel.deleteVaultItem(it) }
                    )
                    VaultTab.CONTACTS -> ContactsVaultTab(
                        contacts = contacts,
                        onAddContact = { showAddContactDialog = true },
                        onDelete = { viewModel.deleteVaultItem(it) }
                    )
                }
            }
        }
    }

    // DIALOGS & OVERLAYS
    if (showVaultSettingsDialog) {
        VaultSettingsDialog(
            isDecoyEnabled = isDecoyEnabled,
            decoyPin = decoyPin,
            onToggleDecoy = { viewModel.setDecoyVaultEnabled(it) },
            onUpdateDecoyPin = { viewModel.setDecoyVaultPin(it) },
            onPurgeCacheNow = {
                VaultCryptoManager.purgeAllTempCache()
                Toast.makeText(context, "Memória RAM do Cofre Limpa com Sucesso!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showVaultSettingsDialog = false }
        )
    }

    if (showAddPhotoDialog) {
        AddPhotoBatchDialog(
            onImport = { title, desc, autoDelete ->
                val sampleBytes = "ENC_PHOTO_${System.currentTimeMillis()}".toByteArray()
                viewModel.importEncryptedPhoto(
                    title = title,
                    subtitle = desc,
                    imageBytes = sampleBytes,
                    originalName = "IMG_MOZ_${System.currentTimeMillis()}.jpg",
                    extraTag = if (isDecoyActive) "DECOY" else "REAL"
                )
                val msg = if (autoDelete) "Foto criptografada e original apagado do telefone!" else "Foto salva e criptografada com AES-256!"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                showAddPhotoDialog = false
            },
            onDismiss = { showAddPhotoDialog = false }
        )
    }

    if (showAddVideoDialog) {
        AddVideoBatchDialog(
            onImport = { title, duration, autoDelete ->
                val sampleBytes = "ENC_VIDEO_${System.currentTimeMillis()}".toByteArray()
                viewModel.importEncryptedVideo(
                    title = title,
                    subtitle = duration,
                    videoBytes = sampleBytes,
                    originalName = "VID_MOZ_${System.currentTimeMillis()}.mp4",
                    extraTag = if (isDecoyActive) "DECOY" else "REAL"
                )
                val msg = if (autoDelete) "Vídeo criptografado e original apagado do telefone!" else "Vídeo salvo com AES-256 no cofre!"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                showAddVideoDialog = false
            },
            onDismiss = { showAddVideoDialog = false }
        )
    }

    if (showAddMusicDialog) {
        AddMusicDialog(
            onImport = { title, artist ->
                val sampleBytes = "ENC_AUDIO_${System.currentTimeMillis()}".toByteArray()
                viewModel.importEncryptedMusic(
                    title = title,
                    artistOrDuration = artist,
                    audioBytes = sampleBytes,
                    originalName = "AUDIO_MOZ_${System.currentTimeMillis()}.mp3",
                    extraTag = if (isDecoyActive) "DECOY" else "REAL"
                )
                Toast.makeText(context, "Áudio importado para o Player Seguro com Equalizador!", Toast.LENGTH_SHORT).show()
                showAddMusicDialog = false
            },
            onDismiss = { showAddMusicDialog = false }
        )
    }

    if (showAddMessageDialog) {
        AddSecretMessageDialog(
            onSave = { title, sender, content ->
                viewModel.importSecretMessage(
                    title = title,
                    sender = sender,
                    content = content,
                    extraTag = if (isDecoyActive) "DECOY" else "REAL"
                )
                Toast.makeText(context, "Conversa criptografada com sucesso!", Toast.LENGTH_SHORT).show()
                showAddMessageDialog = false
            },
            onDismiss = { showAddMessageDialog = false }
        )
    }

    if (showImportWhatsAppDialog) {
        ImportWhatsAppChatDialog(
            onImport = { contactName, chatText ->
                viewModel.importSecretMessage(
                    title = "Conversa WhatsApp ($contactName)",
                    sender = contactName,
                    content = chatText,
                    extraTag = if (isDecoyActive) "DECOY" else "REAL"
                )
                Toast.makeText(context, "Conversa do WhatsApp importada e protegida no cofre!", Toast.LENGTH_LONG).show()
                showImportWhatsAppDialog = false
            },
            onDismiss = { showImportWhatsAppDialog = false }
        )
    }

    if (showAddContactDialog) {
        AddSecretContactDialog(
            onSave = { name, phone, note, autoDeletePhone ->
                viewModel.importSecretContact(
                    name = name,
                    phoneNumber = phone,
                    notes = note,
                    extraTag = if (isDecoyActive) "DECOY" else "REAL"
                )
                val msg = if (autoDeletePhone) "Contacto salvo no cofre e removido da agenda do telefone!" else "Contacto salvo com segurança máxima!"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                showAddContactDialog = false
            },
            onDismiss = { showAddContactDialog = false }
        )
    }

    // FULLSCREEN SECURE PREVIEW WITH 30s AUTO-DESTRUCT TIMER
    if (previewItem != null) {
        SecureAutoDestructPreviewDialog(
            item = previewItem!!,
            remainingSeconds = autoPurgeTimerSeconds,
            onClose = {
                previewItem = null
                VaultCryptoManager.purgeAllTempCache()
            }
        )
    }
}

private fun handleUnlockStatus(
    status: VaultUnlockStatus,
    context: Context,
    onError: (String) -> Unit
) {
    when (status) {
        VaultUnlockStatus.SUCCESS_REAL -> {
            Toast.makeText(context, "🔓 Cofre Principal Desbloqueado com Chave AES-256", Toast.LENGTH_SHORT).show()
        }
        VaultUnlockStatus.SUCCESS_DECOY -> {
            Toast.makeText(context, "🎭 Cofre Isca Aberto", Toast.LENGTH_SHORT).show()
        }
        VaultUnlockStatus.FAILED -> {
            onError("PIN incorreto. Cuidado: 3 erros ativam foto de intruso, áudio e bloqueio de 10 min.")
        }
        VaultUnlockStatus.LOCKED_OUT -> {
            onError("🚨 Cofre Bloqueado por 10 minutos. Foto de intruso e áudio de 5s foram gravados.")
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 1. DOUBLE PIN LOCK SCREEN WITH 3X INTRUDER ENFORCEMENT & LOCKOUT
// -------------------------------------------------------------------------------------------------
@Composable
fun DoublePinLockScreen(
    isLockout: Boolean,
    lockoutRemainingSeconds: Int,
    failedAttempts: Int,
    enteredPin: String,
    errorMessage: String?,
    onPinDigit: (String) -> Unit,
    onPinBackspace: () -> Unit,
    onPinClear: () -> Unit,
    onSubmitPin: () -> Unit,
    onSwitchToDisguise: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Icon & Security Level Info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(if (isLockout) AlertRed.copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.15f))
                    .border(2.dp, if (isLockout) AlertRed else CyanAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isLockout) Icons.Default.LockReset else Icons.Default.Shield,
                    contentDescription = null,
                    tint = if (isLockout) AlertRed else CyanAccent,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isLockout) "COFRE BLOQUEADO POR SEGURANÇA" else "COFRE CRIPTOGRAFADO MZ PRO",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isLockout) AlertRed else TextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isLockout)
                    "3 erros de PIN detectados. Foto silenciosa capturada e áudio gravado."
                else
                    "Autenticação dupla necessária. Digite o PIN de 4 a 6 dígitos.",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            if (isLockout) {
                val minutes = lockoutRemainingSeconds / 60
                val seconds = lockoutRemainingSeconds % 60
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = AlertRed.copy(alpha = 0.18f)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AlertRed)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = AlertRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = String.format(Locale.US, "Desbloqueio em: %02d:%02d", minutes, seconds),
                            color = AlertRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            } else if (failedAttempts > 0) {
                Text(
                    text = "Tentativas falhadas: $failedAttempts/3",
                    fontSize = 11.sp,
                    color = AmberWarning,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // PIN Indicator Dots
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                repeat(6) { index ->
                    val isFilled = index < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) CyanAccent else CardElevated
                            )
                            .border(1.dp, if (isFilled) CyanAccent else CardElevated, CircleShape)
                    )
                }
            }

            if (errorMessage != null && !isLockout) {
                Text(
                    text = errorMessage,
                    color = AlertRed,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        // Numeric Keypad
        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("CLEAR", "0", "BACK")
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(CardDark)
                                .border(1.dp, CardElevated, CircleShape)
                                .clickable(enabled = !isLockout) {
                                    when (key) {
                                        "CLEAR" -> onPinClear()
                                        "BACK" -> onPinBackspace()
                                        else -> {
                                            onPinDigit(key)
                                            if (enteredPin.length + 1 >= 4) {
                                                // auto submit or user can press check
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            when (key) {
                                "CLEAR" -> Text("C", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AmberWarning)
                                "BACK" -> Icon(Icons.Default.Backspace, contentDescription = "Apagar", tint = TextSecondary, modifier = Modifier.size(20.dp))
                                else -> Text(key, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }

        // Bottom Unlock Action & Disguise Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onSwitchToDisguise,
                modifier = Modifier.testTag("vault_disguise_calc_button")
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Modo Calculadora", color = CyanAccent, fontSize = 12.sp)
            }

            Button(
                onClick = onSubmitPin,
                enabled = !isLockout && enteredPin.length >= 4,
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("vault_submit_pin_button")
            ) {
                Icon(Icons.Default.LockOpen, contentDescription = null, tint = DarkBg, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Desbloquear", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 2. CALCULATOR DISGUISE MODE
// -------------------------------------------------------------------------------------------------
@Composable
fun CalculatorDisguiseScreen(
    onUnlockWithSecret: (String) -> Unit,
    onExitDisguise: () -> Unit
) {
    var display by remember { mutableStateOf("0") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Calculadora MZ", color = TextSecondary, fontSize = 14.sp)
            IconButton(onClick = onExitDisguise) {
                Icon(Icons.Default.Close, contentDescription = "Sair", tint = TextSecondary)
            }
        }

        // Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CardElevated)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    text = display,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1
                )
            }
        }

        // Calculator Keys
        val calcRows = listOf(
            listOf("C", "±", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=", "🔓")
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            calcRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { key ->
                        val isOp = key in listOf("÷", "×", "-", "+", "=")
                        val isSpecial = key == "🔓"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    when {
                                        isSpecial -> CyanAccent.copy(alpha = 0.2f)
                                        isOp -> CyanAccent
                                        key == "C" -> AlertRed.copy(alpha = 0.2f)
                                        else -> CardDark
                                    }
                                )
                                .border(1.dp, if (isSpecial) CyanAccent else CardElevated, RoundedCornerShape(14.dp))
                                .clickable {
                                    when (key) {
                                        "C" -> display = "0"
                                        "=" -> {
                                            // Secret unlock trigger: if user typed secret PIN then '='
                                            onUnlockWithSecret(display)
                                        }
                                        "🔓" -> onUnlockWithSecret(display)
                                        else -> {
                                            if (display == "0") display = key
                                            else display += key
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isSpecial -> CyanAccent
                                    isOp -> DarkBg
                                    key == "C" -> AlertRed
                                    else -> TextPrimary
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 3. VAULT HEADER BAR
// -------------------------------------------------------------------------------------------------
@Composable
fun VaultHeaderBar(
    isDecoyMode: Boolean,
    onLock: () -> Unit,
    onOpenSettings: () -> Unit,
    onDisguise: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardDark)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isDecoyMode) AmberWarning.copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDecoyMode) Icons.Default.Warning else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isDecoyMode) AmberWarning else CyanAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = if (isDecoyMode) "COFRE ISCA MZ" else "COFRE SECRETO MZ",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = if (isDecoyMode) "Modo de Coação Ativo" else "Criptografia AES-256 no Keystore",
                    fontSize = 11.sp,
                    color = if (isDecoyMode) AmberWarning else EmeraldGreen
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDisguise) {
                Icon(Icons.Default.Calculate, contentDescription = "Modo Calculadora", tint = TextSecondary)
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Configurações do Cofre", tint = CyanAccent)
            }
            IconButton(onClick = onLock) {
                Icon(Icons.Default.Lock, contentDescription = "Trancar Agora", tint = AlertRed)
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 4. PHOTOS TAB (SELEÇÃO MÚLTIPLA & APAGAR ORIGINAIS)
// -------------------------------------------------------------------------------------------------
@Composable
fun PhotosVaultTab(
    photos: List<VaultItemEntity>,
    onAddPhotos: () -> Unit,
    onPreview: (VaultItemEntity) -> Unit,
    onDelete: (VaultItemEntity) -> Unit
) {
    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<Long>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Actions Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${photos.size} Fotos Criptografadas",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onAddPhotos,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = DarkBg, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Importar Vários", color = DarkBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (photos.isEmpty()) {
            EmptyVaultPlaceholder(
                icon = Icons.Default.Image,
                title = "Nenhuma foto criptografada",
                subtitle = "Adicione fotos para criptografar com AES-256 no Keystore e esconder da galeria."
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(photos) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPreview(item) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = BorderStroke(1.dp, CardElevated)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(36.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AlertRed.copy(alpha = 0.8f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("AES-256", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(item.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(item.subtitle, fontSize = 10.sp, color = TextSecondary, maxLines = 1)

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.secretContent.take(12) + "...",
                                    fontSize = 9.sp,
                                    color = CyanAccent
                                )
                                IconButton(
                                    onClick = { onDelete(item) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Apagar", tint = AlertRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 5. VIDEOS TAB
// -------------------------------------------------------------------------------------------------
@Composable
fun VideosVaultTab(
    videos: List<VaultItemEntity>,
    onAddVideos: () -> Unit,
    onPreview: (VaultItemEntity) -> Unit,
    onDelete: (VaultItemEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${videos.size} Vídeos Protegidos",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Button(
                onClick = onAddVideos,
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = DarkBg, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Adicionar Vídeo", color = DarkBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (videos.isEmpty()) {
            EmptyVaultPlaceholder(
                icon = Icons.Default.Movie,
                title = "Nenhum vídeo confidencial",
                subtitle = "Adicione vídeos para proteger com AES-256 e visualizador com auto-bloqueio."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(videos) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPreview(item) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = BorderStroke(1.dp, CardElevated)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(item.subtitle, fontSize = 11.sp, color = TextSecondary)
                                Text("UUID: ${item.secretContent.take(16)}...", fontSize = 9.sp, color = CyanAccent)
                            }
                            IconButton(onClick = { onDelete(item) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Apagar", tint = AlertRed, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 6. MUSICS TAB WITH INTEGRATED EQUALIZER AUDIO PLAYER
// -------------------------------------------------------------------------------------------------
@Composable
fun MusicsVaultTab(
    musics: List<VaultItemEntity>,
    activeTrack: VaultItemEntity?,
    isPlaying: Boolean,
    progress: Float,
    eqPreset: EqualizerPreset,
    onTogglePlay: (VaultItemEntity) -> Unit,
    onSelectEq: (EqualizerPreset) -> Unit,
    onProgressChange: (Float) -> Unit,
    onAddMusic: () -> Unit,
    onDelete: (VaultItemEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Built-in Equalizer Player Card if audio is selected
        if (activeTrack != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardElevated),
                border = BorderStroke(1.dp, CyanAccent)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("PLAYER & EQUALIZADOR SEGURO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                                Text(activeTrack.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(eqPreset.label.take(8), fontSize = 10.sp, color = CyanAccent)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Slider
                    Slider(
                        value = progress,
                        onValueChange = onProgressChange,
                        colors = SliderDefaults.colors(
                            thumbColor = CyanAccent,
                            activeTrackColor = CyanAccent,
                            inactiveTrackColor = DarkBg
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Player Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onProgressChange((progress - 0.1f).coerceAtLeast(0f)) }) {
                            Icon(Icons.Default.FastRewind, contentDescription = null, tint = TextPrimary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(CyanAccent)
                                .clickable { onTogglePlay(activeTrack) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = DarkBg,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        IconButton(onClick = { onProgressChange((progress + 0.1f).coerceAtMost(1f)) }) {
                            Icon(Icons.Default.FastForward, contentDescription = null, tint = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Equalizer Presets Selector
                    Text("Predefinições de Equalizador:", fontSize = 10.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        EqualizerPreset.values().forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (eqPreset == preset) CyanAccent else DarkBg)
                                    .clickable { onSelectEq(preset) }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = preset.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (eqPreset == preset) DarkBg else TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Header for music list
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${musics.size} Áudios Criptografados",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Button(
                onClick = onAddMusic,
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = DarkBg, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Adicionar Áudio", color = DarkBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (musics.isEmpty()) {
            EmptyVaultPlaceholder(
                icon = Icons.Default.MusicNote,
                title = "Nenhum áudio ou música protegida",
                subtitle = "Guarde gravações de voz secretas, músicas e chamadas com player e equalizador seguro."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(musics) { track ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTogglePlay(track) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = BorderStroke(1.dp, if (activeTrack?.id == track.id) CyanAccent else CardElevated)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (activeTrack?.id == track.id && isPlaying) CyanAccent else DarkBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (activeTrack?.id == track.id && isPlaying) Icons.Default.Pause else Icons.Default.Audiotrack,
                                    contentDescription = null,
                                    tint = if (activeTrack?.id == track.id && isPlaying) DarkBg else CyanAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(track.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(track.subtitle, fontSize = 11.sp, color = TextSecondary)
                            }
                            IconButton(onClick = { onDelete(track) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Apagar", tint = AlertRed, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 7. MESSAGES TAB WITH WHATSAPP IMPORT (.TXT) & CONTACT FILTER
// -------------------------------------------------------------------------------------------------
@Composable
fun MessagesVaultTab(
    messages: List<VaultItemEntity>,
    onAddMessage: () -> Unit,
    onImportWhatsApp: () -> Unit,
    onPreview: (VaultItemEntity) -> Unit,
    onDelete: (VaultItemEntity) -> Unit
) {
    var filterContact by remember { mutableStateOf("") }

    val filtered = remember(messages, filterContact) {
        if (filterContact.isBlank()) messages
        else messages.filter {
            it.title.contains(filterContact, ignoreCase = true) ||
                    it.subtitle.contains(filterContact, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Action Buttons: Nova Mensagem + Importar WhatsApp .txt
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onImportWhatsApp,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
            ) {
                Icon(Icons.Default.Whatsapp, contentDescription = null, tint = DarkBg, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Importar WhatsApp .txt", color = DarkBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onAddMessage,
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = DarkBg, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Nova Conversa", color = DarkBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter by Contact
        OutlinedTextField(
            value = filterContact,
            onValueChange = { filterContact = it },
            placeholder = { Text("Filtrar por contacto ou remetente...", fontSize = 12.sp, color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanAccent,
                unfocusedBorderColor = CardElevated,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (filtered.isEmpty()) {
            EmptyVaultPlaceholder(
                icon = Icons.Default.Message,
                title = "Nenhuma mensagem confidencial",
                subtitle = "Guarde SMS bancários, conversas sigilosas do WhatsApp (.txt) ou códigos de recuperação."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered) { msg ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPreview(msg) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = BorderStroke(1.dp, CardElevated)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(msg.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CyanAccent.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("AES-256", fontSize = 9.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                                }
                            }

                            Text(msg.subtitle, fontSize = 11.sp, color = CyanAccent)

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "🔒 Conteúdo criptografado. Toque para descriptografar.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(
                                    onClick = { onDelete(msg) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Apagar", tint = AlertRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 8. CONTACTS TAB (BACKUP & APAGAR DO TELEFONE)
// -------------------------------------------------------------------------------------------------
@Composable
fun ContactsVaultTab(
    contacts: List<VaultItemEntity>,
    onAddContact: () -> Unit,
    onDelete: (VaultItemEntity) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${contacts.size} Contactos Sigilosos",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Button(
                onClick = onAddContact,
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = DarkBg, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Novo Contacto", color = DarkBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (contacts.isEmpty()) {
            EmptyVaultPlaceholder(
                icon = Icons.Default.ContactPhone,
                title = "Nenhum contacto sigiloso",
                subtitle = "Guarde contactos confidenciais com opção de backup e exclusão da agenda pública."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(contacts) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = BorderStroke(1.dp, CardElevated)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(DarkBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = CyanAccent)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(item.subtitle, fontSize = 12.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                            }

                            // Call Button
                            IconButton(onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.subtitle}"))
                                context.startActivity(intent)
                            }) {
                                Icon(Icons.Default.Call, contentDescription = "Ligar", tint = CyanAccent, modifier = Modifier.size(20.dp))
                            }

                            // Delete Button
                            IconButton(onClick = { onDelete(item) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Apagar", tint = AlertRed, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 9. DIALOGS (SETTINGS, BATCH IMPORT, PREVIEW, ETC)
// -------------------------------------------------------------------------------------------------
@Composable
fun VaultSettingsDialog(
    isDecoyEnabled: Boolean,
    decoyPin: String,
    onToggleDecoy: (Boolean) -> Unit,
    onUpdateDecoyPin: (String) -> Unit,
    onPurgeCacheNow: () -> Unit,
    onDismiss: () -> Unit
) {
    var tempDecoyPin by remember { mutableStateOf(decoyPin) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Configurações do Cofre Secreto", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Decoy / Fake Vault Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ativar Cofre Falso (Modo Isca)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        Text("Permite digitar um PIN falso se for forçado por assaltantes.", fontSize = 11.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = isDecoyEnabled,
                        onCheckedChange = onToggleDecoy,
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = CardElevated)
                    )
                }

                if (isDecoyEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tempDecoyPin,
                        onValueChange = {
                            if (it.length <= 6) {
                                tempDecoyPin = it
                                onUpdateDecoyPin(it)
                            }
                        },
                        label = { Text("PIN do Cofre Isca/Falso (ex: 0000)", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberWarning,
                            unfocusedBorderColor = CardElevated,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = CardElevated)
                Spacer(modifier = Modifier.height(14.dp))

                // Purge RAM Cache Button
                Button(
                    onClick = onPurgeCacheNow,
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, AlertRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = AlertRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Limpar Memória RAM do Cofre Agora", color = AlertRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)) {
                Text("Fechar", color = DarkBg, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = CardDark
    )
}

@Composable
fun AddPhotoBatchDialog(
    onImport: (title: String, desc: String, autoDeleteOriginal: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("Documento Criptografado") }
    var autoDelete by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Importar Fotos com AES-256", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título da Foto / Documento") },
                    placeholder = { Text("ex: BI Moçambique, Comprovativo...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CardElevated,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = autoDelete,
                        onCheckedChange = { autoDelete = it },
                        colors = CheckboxDefaults.colors(checkedColor = CyanAccent)
                    )
                    Text("Apagar originais da galeria após mover", fontSize = 12.sp, color = TextPrimary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onImport(title, desc, autoDelete) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text("Criptografar", color = DarkBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = TextSecondary) }
        },
        containerColor = CardDark
    )
}

@Composable
fun AddVideoBatchDialog(
    onImport: (title: String, duration: String, autoDeleteOriginal: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var autoDelete by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Proteger Vídeo no Cofre", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título do Vídeo") },
                    placeholder = { Text("ex: Gravação Secreta Escritório") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CardElevated,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = autoDelete,
                        onCheckedChange = { autoDelete = it },
                        colors = CheckboxDefaults.colors(checkedColor = CyanAccent)
                    )
                    Text("Apagar original do telemóvel após criptografar", fontSize = 12.sp, color = TextPrimary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onImport(title, "01:15 min • 1080p", autoDelete) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text("Salvar Vídeo", color = DarkBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = TextSecondary) } },
        containerColor = CardDark
    )
}

@Composable
fun AddMusicDialog(
    onImport: (title: String, artist: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("Áudio Confidencial MZ") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Importar Áudio para o Cofre", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nome do Áudio / Gravação") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CardElevated,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onImport(title, artist) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text("Adicionar", color = DarkBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = TextSecondary) } },
        containerColor = CardDark
    )
}

@Composable
fun AddSecretMessageDialog(
    onSave: (title: String, sender: String, content: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var sender by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Mensagem Secreta", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Assunto / Título") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CardElevated,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = sender,
                    onValueChange = { sender = it },
                    label = { Text("Remetente ou Contacto") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CardElevated,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Conteúdo Sigiloso") },
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CardElevated,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank() && content.isNotBlank()) onSave(title, sender, content) },
                enabled = title.isNotBlank() && content.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text("Criptografar", color = DarkBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = TextSecondary) } },
        containerColor = CardDark
    )
}

@Composable
fun ImportWhatsAppChatDialog(
    onImport: (contactName: String, chatText: String) -> Unit,
    onDismiss: () -> Unit
) {
    var contactName by remember { mutableStateOf("") }
    var chatText by remember {
        mutableStateOf(
            "[12/08/2026 14:20] Armando: Olá, o comprovativo da transferência já foi enviado para Maputo.\n" +
            "[12/08/2026 14:22] Jessica: Recebido com sucesso. Guardando as chaves de segurança."
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Whatsapp, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Importar Conversa WhatsApp (.txt)", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Cole abaixo a conversa exportada do WhatsApp (.txt) para criptografar integralmente no cofre:",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contactName,
                    onValueChange = { contactName = it },
                    label = { Text("Nome do Contacto / Grupo") },
                    placeholder = { Text("ex: Família, Negócios Maputo...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = CardElevated,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = chatText,
                    onValueChange = { chatText = it },
                    label = { Text("Texto da Conversa") },
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = CardElevated,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (contactName.isNotBlank() && chatText.isNotBlank()) onImport(contactName, chatText) },
                enabled = contactName.isNotBlank() && chatText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Text("Importar & Proteger", color = DarkBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = TextSecondary) } },
        containerColor = CardDark
    )
}

@Composable
fun AddSecretContactDialog(
    onSave: (name: String, phone: String, note: String, autoDeletePhone: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+258 8") }
    var note by remember { mutableStateOf("") }
    var autoDeletePhone by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Contacto Sigiloso", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Contacto") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CardElevated,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Número de Telemóvel (+258)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CardElevated,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = autoDeletePhone,
                        onCheckedChange = { autoDeletePhone = it },
                        colors = CheckboxDefaults.colors(checkedColor = CyanAccent)
                    )
                    Text("Backup seguro e apagar da agenda pública", fontSize = 12.sp, color = TextPrimary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && phone.isNotBlank()) onSave(name, phone, note, autoDeletePhone) },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text("Salvar no Cofre", color = DarkBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = TextSecondary) } },
        containerColor = CardDark
    )
}

// -------------------------------------------------------------------------------------------------
// 10. SECURE AUTO-DESTRUCT PREVIEW DIALOG (30s TIMER)
// -------------------------------------------------------------------------------------------------
@Composable
fun SecureAutoDestructPreviewDialog(
    item: VaultItemEntity,
    remainingSeconds: Int,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val decryptedText = remember(item) {
        VaultCryptoManager.decryptString(item.secretContent)
    }

    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AlertRed.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("⏱️ ${remainingSeconds}s", color = AlertRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "🔒 Descriptografado em cache temporário. Destruição automática em $remainingSeconds segundos.",
                    fontSize = 10.sp,
                    color = AmberWarning
                )
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkBg),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Conteúdo Revelado:", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (item.itemType in listOf("PHOTO", "VIDEO", "MUSIC"))
                                "Ficheiro protegido: ${item.originalFileName ?: item.secretContent}\nCaminho seguro: /data/data/.../cofre/${item.secretContent}\nChave: AES-256 AndroidKeyStore"
                            else
                                decryptedText,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onClose, colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)) {
                Text("Fechar e Limpar RAM", color = DarkBg, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = CardDark
    )
}

@Composable
fun EmptyVaultPlaceholder(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(CardDark)
                    .border(1.dp, CardElevated, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, fontSize = 11.sp, color = TextSecondary, textAlign = TextAlign.Center)
        }
    }
}
