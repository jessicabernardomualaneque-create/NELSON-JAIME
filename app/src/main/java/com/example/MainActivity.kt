package com.example

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.receiver.AntiTheftAdminReceiver
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AdminDatabaseScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EmergencyContactsScreen
import com.example.ui.screens.EvidenceLogsScreen
import com.example.ui.screens.MozambiqueAuthScreen
import com.example.ui.screens.MozambiqueMapScreen
import com.example.ui.screens.PaymentScreen
import com.example.ui.screens.SecretVaultScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SmsCommandsScreen
import com.example.ui.theme.CardDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBg
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VodacomRed
import com.example.ui.viewmodel.MainViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestAdminLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshDeviceStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainApp(
                    viewModel = viewModel,
                    onRequestAdmin = { requestDeviceAdmin() }
                )
            }
        }
    }

    private fun requestDeviceAdmin() {
        val adminComponent = ComponentName(this, AntiTheftAdminReceiver::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "O Anti-Roubo MZ Pro necessita desta permissão para travar a tela remotamente via SMS e alertar sobre tentativas de senha incorreta."
            )
        }
        requestAdminLauncher.launch(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDeviceStatus()
        viewModel.checkSubscriptionNotifications()
    }
}

enum class ScreenTab(val title: String) {
    DASHBOARD("Painel"),
    VAULT("Cofre"),
    MAP_MZ("Mapa MZ"),
    SMS_COMMANDS("SMS"),
    PAYMENT("Planos"),
    CONTACTS("Contactos"),
    EVIDENCE("Histórico"),
    SETTINGS("Ajustes"),
    ADMIN("ADM")
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainApp(
    viewModel: MainViewModel,
    onRequestAdmin: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val isSubAdmin by viewModel.isSubAdmin.collectAsState()
    val isTrial by viewModel.isTrial.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()

    var currentTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }

    // Request vital runtime permissions
    val permissionsList = mutableListOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionsList.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    val permissionsState = rememberMultiplePermissionsState(permissionsList)

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    if (userProfile == null) {
        // Mozambique Restricted Registration & Account Creation Flow
        MozambiqueAuthScreen(viewModel = viewModel)
    } else if (viewModel.isAppBlocked() && !isSubAdmin) {
        // REQUISITO BLOQUEAR APP: Trial expirado e sem plano ativo -> Redireciona para Pagamento
        PaymentScreen(
            viewModel = viewModel,
            onBack = null
        )
    } else {
        // Authenticated Anti-Theft Protection Suite
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = DarkBg,
            bottomBar = {
                NavigationBar(
                    containerColor = CardDark,
                    contentColor = TextPrimary,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("main_navigation_bar")
                ) {
                    NavigationBarItem(
                        selected = currentTab == ScreenTab.DASHBOARD,
                        onClick = { currentTab = ScreenTab.DASHBOARD },
                        icon = { Icon(Icons.Default.Shield, contentDescription = "Painel") },
                        label = { Text("Painel", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBg,
                            selectedTextColor = CyanAccent,
                            indicatorColor = CyanAccent,
                            unselectedIconColor = TextTertiary,
                            unselectedTextColor = TextTertiary
                        ),
                        modifier = Modifier.testTag("nav_item_dashboard")
                    )

                    NavigationBarItem(
                        selected = currentTab == ScreenTab.VAULT,
                        onClick = { currentTab = ScreenTab.VAULT },
                        icon = { Icon(Icons.Default.Lock, contentDescription = "Cofre Secreto") },
                        label = { Text("Cofre", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBg,
                            selectedTextColor = CyanAccent,
                            indicatorColor = CyanAccent,
                            unselectedIconColor = TextTertiary,
                            unselectedTextColor = TextTertiary
                        ),
                        modifier = Modifier.testTag("nav_item_vault")
                    )

                    NavigationBarItem(
                        selected = currentTab == ScreenTab.MAP_MZ,
                        onClick = { currentTab = ScreenTab.MAP_MZ },
                        icon = { Icon(Icons.Default.Map, contentDescription = "Mapa de Moçambique") },
                        label = { Text("Mapa MZ", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBg,
                            selectedTextColor = CyanAccent,
                            indicatorColor = CyanAccent,
                            unselectedIconColor = TextTertiary,
                            unselectedTextColor = TextTertiary
                        ),
                        modifier = Modifier.testTag("nav_item_map")
                    )

                    NavigationBarItem(
                        selected = currentTab == ScreenTab.SMS_COMMANDS,
                        onClick = { currentTab = ScreenTab.SMS_COMMANDS },
                        icon = { Icon(Icons.Default.Sms, contentDescription = "Comandos SMS") },
                        label = { Text("SMS", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBg,
                            selectedTextColor = CyanAccent,
                            indicatorColor = CyanAccent,
                            unselectedIconColor = TextTertiary,
                            unselectedTextColor = TextTertiary
                        ),
                        modifier = Modifier.testTag("nav_item_sms")
                    )

                    NavigationBarItem(
                        selected = currentTab == ScreenTab.PAYMENT,
                        onClick = { currentTab = ScreenTab.PAYMENT },
                        icon = { Icon(Icons.Default.Phone, contentDescription = "Planos & Pagamentos MZ") },
                        label = { Text("Planos", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBg,
                            selectedTextColor = EmeraldGreen,
                            indicatorColor = EmeraldGreen,
                            unselectedIconColor = if (isPremium) EmeraldGreen else TextTertiary,
                            unselectedTextColor = if (isPremium) EmeraldGreen else TextTertiary
                        ),
                        modifier = Modifier.testTag("nav_item_payment")
                    )

                    NavigationBarItem(
                        selected = currentTab == ScreenTab.SETTINGS,
                        onClick = { currentTab = ScreenTab.SETTINGS },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
                        label = { Text("Ajustes", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBg,
                            selectedTextColor = CyanAccent,
                            indicatorColor = CyanAccent,
                            unselectedIconColor = TextTertiary,
                            unselectedTextColor = TextTertiary
                        ),
                        modifier = Modifier.testTag("nav_item_settings")
                    )

                    NavigationBarItem(
                        selected = currentTab == ScreenTab.ADMIN,
                        onClick = { currentTab = ScreenTab.ADMIN },
                        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Gestão ADM Base de Dados") },
                        label = { Text("ADM", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = androidx.compose.ui.graphics.Color.White,
                            selectedTextColor = VodacomRed,
                            indicatorColor = VodacomRed,
                            unselectedIconColor = if (isSubAdmin || isAdminLoggedIn) VodacomRed else TextTertiary,
                            unselectedTextColor = if (isSubAdmin || isAdminLoggedIn) VodacomRed else TextTertiary
                        ),
                        modifier = Modifier.testTag("nav_item_admin")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    ScreenTab.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onRequestAdmin = onRequestAdmin,
                        onOpenMap = { currentTab = ScreenTab.MAP_MZ },
                        onOpenVault = { currentTab = ScreenTab.VAULT },
                        onOpenPayment = { currentTab = ScreenTab.PAYMENT },
                        onOpenAdmin = { currentTab = ScreenTab.ADMIN }
                    )
                    ScreenTab.VAULT -> SecretVaultScreen(
                        viewModel = viewModel
                    )
                    ScreenTab.MAP_MZ -> MozambiqueMapScreen(
                        viewModel = viewModel
                    )
                    ScreenTab.SMS_COMMANDS -> SmsCommandsScreen(
                        viewModel = viewModel
                    )
                    ScreenTab.PAYMENT -> PaymentScreen(
                        viewModel = viewModel,
                        onBack = { currentTab = ScreenTab.DASHBOARD }
                    )
                    ScreenTab.CONTACTS -> EmergencyContactsScreen(
                        viewModel = viewModel
                    )
                    ScreenTab.EVIDENCE -> EvidenceLogsScreen(
                        viewModel = viewModel
                    )
                    ScreenTab.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        onRequestAdmin = onRequestAdmin,
                        onOpenAdminPortal = { currentTab = ScreenTab.ADMIN },
                        onOpenPayment = { currentTab = ScreenTab.PAYMENT }
                    )
                    ScreenTab.ADMIN -> {
                        if (isSubAdmin || isAdminLoggedIn) {
                            AdminDashboardScreen(
                                viewModel = viewModel,
                                onClose = { currentTab = ScreenTab.DASHBOARD }
                            )
                        } else {
                            AdminDatabaseScreen(
                                viewModel = viewModel,
                                onClose = { currentTab = ScreenTab.DASHBOARD }
                            )
                        }
                    }
                }
            }
        }
    }
}
