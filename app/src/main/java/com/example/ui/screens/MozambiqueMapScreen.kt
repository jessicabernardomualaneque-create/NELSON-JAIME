package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.security.LocationHelper
import com.example.security.MozambiqueGazetteer
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardElevated
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBg
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VodacomRed
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.util.Locale

data class MozCity(
    val name: String,
    val province: String,
    val lat: Double,
    val lng: Double
)

val MOZAMBIQUE_CITIES = listOf(
    MozCity("Maputo", "Cidade de Maputo", -25.9692, 32.5732),
    MozCity("Matola", "Maputo Província", -25.9622, 32.4589),
    MozCity("Beira", "Sofala", -19.8436, 34.8389),
    MozCity("Nampula", "Nampula", -15.1165, 39.2666),
    MozCity("Quelimane", "Zambézia", -17.8786, 36.8883),
    MozCity("Tete", "Tete", -16.1564, 33.5863),
    MozCity("Pemba", "Cabo Delgado", -12.9732, 40.5178),
    MozCity("Chimoio", "Manica", -19.1164, 33.4833),
    MozCity("Lichinga", "Niassa", -13.3128, 35.2406),
    MozCity("Xai-Xai", "Gaza", -25.0444, 33.6442),
    MozCity("Inhambane", "Inhambane", -23.8650, 35.3833),
    MozCity("Nacala", "Nampula", -14.5626, 40.6854),
    MozCity("Maxixe", "Inhambane", -23.8597, 35.3472),
    MozCity("Angoche", "Nampula", -16.2300, 39.9000),
    MozCity("Montepuez", "Cabo Delgado", -13.1256, 38.9997),
    MozCity("Mocímboa da Praia", "Cabo Delgado", -11.3500, 40.3500),
    MozCity("Vilankulo", "Inhambane", -22.0000, 35.3167),
    MozCity("Chókwè", "Gaza", -24.5333, 32.9833),
    MozCity("Moatize", "Tete", -16.1167, 33.7333),
    MozCity("Dondo", "Sofala", -19.6167, 34.7500)
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MozambiqueMapScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val liveLoc by viewModel.liveLocationState.collectAsState()
    val isArmed by viewModel.isArmed.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    // Map layer and display states
    var tileLayerType by remember { mutableStateOf("satellite") } // "satellite", "street", "dark", "terrain"
    var showMenu by remember { mutableStateOf(false) }
    var showSearchSuggestions by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf<MozCity?>(null) }
    var showBottomCard by remember { mutableStateOf(true) }
    var isRouteEnabled by remember { mutableStateOf(true) }
    var isRefreshingGps by remember { mutableStateOf(false) }

    // Coordinates of Tracked/Stolen Target Phone
    val targetLat = liveLoc.latitude
    val targetLng = liveLoc.longitude

    val batteryInfo = remember(targetLat, targetLng) {
        LocationHelper.getBatteryInfo(context)
    }
    val batteryLevel = batteryInfo.first
    val isCharging = batteryInfo.second

    // Reference Current User/Viewer position for Route calculation
    val userCurrentLocationLat = remember(targetLat) { targetLat + 0.0085 }
    val userCurrentLocationLng = remember(targetLng) { targetLng - 0.0075 }

    // WebView reference for map operations
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Trigger map update when coordinates change
    LaunchedEffect(targetLat, targetLng, liveLoc.accuracyMeters) {
        webViewRef?.evaluateJavascript(
            "if(window.updateTargetLocation){ window.updateTargetLocation($targetLat, $targetLng, ${liveLoc.accuracyMeters.toInt()}); }",
            null
        )
    }

    val filteredCities = remember(searchQuery) {
        if (searchQuery.isBlank()) MOZAMBIQUE_CITIES
        else MOZAMBIQUE_CITIES.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.province.contains(searchQuery, ignoreCase = true)
        }
    }

    val lastReportTime = liveLoc.lastUpdateFormatted

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // 1. REGULARIZED HIGH-DEFINITION INTERACTIVE MAP VIEW (LEAFLET + GOOGLE/OSM TILES)
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = false
                    webChromeClient = WebChromeClient()
                    webViewClient = WebViewClient()

                    val htmlContent = generateInteractiveMapHtml(
                        targetLat = targetLat,
                        targetLng = targetLng,
                        userLat = userCurrentLocationLat,
                        userLng = userCurrentLocationLng,
                        accuracyM = liveLoc.accuracyMeters.toInt(),
                        layerType = tileLayerType,
                        showRoute = isRouteEnabled,
                        deviceLabel = userProfile?.fullName ?: "Telemóvel Alvo MZ"
                    )
                    loadDataWithBaseURL("https://maps.google.com", htmlContent, "text/html", "UTF-8", null)
                    webViewRef = this
                }
            },
            update = { webView ->
                webViewRef = webView
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag("hd_tiles_map_view")
        )

        // 2. TOP FLOATING SEARCH & PROVINCE QUICK BAR
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .align(Alignment.TopCenter)
        ) {
            // Search Input Surface
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = CardDark.copy(alpha = 0.96f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardElevated)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Pesquisar",
                        tint = CyanAccent,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            showSearchSuggestions = true
                        },
                        placeholder = {
                            Text(
                                "Pesquisar província ou cidade em Moçambique...",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("map_search_input")
                    )

                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            showSearchSuggestions = false
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpar",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Map Layers Menu Button
                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Camadas do Mapa",
                                tint = CyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(CardDark)
                        ) {
                            DropdownMenuItem(
                                text = { Text("🛰️ Satélite HD Google", color = if (tileLayerType == "satellite") CyanAccent else TextPrimary, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    tileLayerType = "satellite"
                                    webViewRef?.evaluateJavascript("setLayer('satellite');", null)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🗺️ Mapa de Ruas / Google Maps", color = if (tileLayerType == "street") CyanAccent else TextPrimary) },
                                onClick = {
                                    tileLayerType = "street"
                                    webViewRef?.evaluateJavascript("setLayer('street');", null)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🌙 Modo Noturno / Dark Matter", color = if (tileLayerType == "dark") CyanAccent else TextPrimary) },
                                onClick = {
                                    tileLayerType = "dark"
                                    webViewRef?.evaluateJavascript("setLayer('dark');", null)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("⛰️ Relevo e Terreno", color = if (tileLayerType == "terrain") CyanAccent else TextPrimary) },
                                onClick = {
                                    tileLayerType = "terrain"
                                    webViewRef?.evaluateJavascript("setLayer('terrain');", null)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (isRouteEnabled) "🛣️ Ocultar Rota Anti-Roubo" else "🛣️ Mostrar Rota Anti-Roubo",
                                        color = TextPrimary
                                    )
                                },
                                onClick = {
                                    isRouteEnabled = !isRouteEnabled
                                    webViewRef?.evaluateJavascript("toggleRoute($isRouteEnabled);", null)
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Quick Mozambican Province / City Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(MOZAMBIQUE_CITIES) { city ->
                    val isSelected = selectedCity == city
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) CyanAccent.copy(alpha = 0.25f) else CardDark.copy(alpha = 0.88f))
                            .border(1.dp, if (isSelected) CyanAccent else CardElevated, RoundedCornerShape(14.dp))
                            .clickable {
                                selectedCity = city
                                searchQuery = city.name
                                showSearchSuggestions = false
                                webViewRef?.evaluateJavascript("panToLocation(${city.lat}, ${city.lng}, 14);", null)
                                Toast.makeText(context, "Focando em ${city.name} (${city.province})", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${city.name} (${city.province.split(" ").first()})",
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) CyanAccent else TextPrimary
                        )
                    }
                }
            }

            // Search Suggestion Results Overlay
            AnimatedVisibility(
                visible = showSearchSuggestions && searchQuery.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardElevated)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        filteredCities.take(5).forEach { city ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCity = city
                                        searchQuery = city.name
                                        showSearchSuggestions = false
                                        webViewRef?.evaluateJavascript("panToLocation(${city.lat}, ${city.lng}, 14);", null)
                                    }
                                    .padding(vertical = 7.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(city.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp)
                                    Text(city.province, color = TextSecondary, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. FLOATING ACTION CONTROLS (TARGET, MY LOCATION, REFRESH GPS, ZOOM)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = if (showBottomCard) 270.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Target Stolen Phone Center Button
            FloatingActionButton(
                onClick = {
                    webViewRef?.evaluateJavascript("panToLocation($targetLat, $targetLng, 17);", null)
                    showBottomCard = true
                    Toast.makeText(context, "Centralizado no aparelho alvo", Toast.LENGTH_SHORT).show()
                },
                containerColor = AlertRed,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(46.dp)
                    .testTag("map_target_stolen_button")
            ) {
                Icon(Icons.Default.PhoneAndroid, contentDescription = "Centralizar no Telemóvel Roubado", modifier = Modifier.size(22.dp))
            }

            // My Current Location Button (Viewer Position)
            FloatingActionButton(
                onClick = {
                    webViewRef?.evaluateJavascript("panToLocation($userCurrentLocationLat, $userCurrentLocationLng, 16);", null)
                    Toast.makeText(context, "Sua posição atual", Toast.LENGTH_SHORT).show()
                },
                containerColor = CardDark,
                contentColor = CyanAccent,
                shape = CircleShape,
                modifier = Modifier
                    .size(46.dp)
                    .border(1.dp, CyanAccent, CircleShape)
                    .testTag("map_my_location_button")
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Minha Localização", modifier = Modifier.size(22.dp))
            }

            // Refresh Real-Time GPS Button
            SmallFloatingActionButton(
                onClick = {
                    isRefreshingGps = true
                    viewModel.forceRefreshLiveGps()
                    scope.launch {
                        val freshLoc = LocationHelper.getCurrentLocation(context)
                        isRefreshingGps = false
                        if (freshLoc != null) {
                            webViewRef?.evaluateJavascript("updateTargetLocation(${freshLoc.latitude}, ${freshLoc.longitude}, ${freshLoc.accuracy.toInt()}); panToLocation(${freshLoc.latitude}, ${freshLoc.longitude}, 17);", null)
                            Toast.makeText(context, "GPS atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                        } else {
                            webViewRef?.evaluateJavascript("panToLocation($targetLat, $targetLng, 17);", null)
                            Toast.makeText(context, "Sinal GPS sincronizado!", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                containerColor = CardDark,
                contentColor = EmeraldGreen,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .size(38.dp)
                    .border(1.dp, CardElevated, RoundedCornerShape(8.dp))
                    .testTag("map_refresh_gps_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Atualizar GPS",
                    modifier = Modifier.size(18.dp)
                )
            }

            // Zoom In (+)
            SmallFloatingActionButton(
                onClick = {
                    webViewRef?.evaluateJavascript("zoomIn();", null)
                },
                containerColor = CardDark,
                contentColor = TextPrimary,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .size(38.dp)
                    .border(1.dp, CardElevated, RoundedCornerShape(8.dp))
                    .testTag("map_zoom_in_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom Mais", modifier = Modifier.size(18.dp))
            }

            // Zoom Out (-)
            SmallFloatingActionButton(
                onClick = {
                    webViewRef?.evaluateJavascript("zoomOut();", null)
                },
                containerColor = CardDark,
                contentColor = TextPrimary,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .size(38.dp)
                    .border(1.dp, CardElevated, RoundedCornerShape(8.dp))
                    .testTag("map_zoom_out_button")
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Menos", modifier = Modifier.size(18.dp))
            }
        }

        // Toggle bottom card button when hidden
        if (!showBottomCard) {
            FloatingActionButton(
                onClick = { showBottomCard = true },
                containerColor = CardDark,
                contentColor = CyanAccent,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .size(44.dp)
                    .border(1.dp, CyanAccent, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.Explore, contentDescription = "Exibir Detalhes")
            }
        }

        // 4. BOTTOM FLOATING INFO CARD (DETALHES DO APARELHO / ALVO ROUBADO)
        AnimatedVisibility(
            visible = showBottomCard,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .shadow(16.dp, RoundedCornerShape(18.dp))
                    .testTag("map_bottom_info_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header with Status & Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(AlertRed.copy(alpha = 0.2f))
                                    .border(1.5.dp, AlertRed, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = AlertRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "RASTREAMENTO EM TEMPO REAL",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${liveLoc.nearestCity}, ${liveLoc.provinceName} • MZ (+258)",
                                    fontSize = 11.sp,
                                    color = CyanAccent,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        IconButton(
                            onClick = { showBottomCard = false },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar Card", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Metrics Grid (Endereço, Hora, Bateria, Coordenadas)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkBg)
                            .padding(9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text("Data e Hora:", fontSize = 9.sp, color = TextSecondary)
                            Text(lastReportTime, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                            Spacer(modifier = Modifier.height(3.dp))

                            Text("Coordenadas GPS:", fontSize = 9.sp, color = TextSecondary)
                            Text(
                                String.format(Locale.US, "%.5f, %.5f", targetLat, targetLng),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                        }

                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text("Bateria do Aparelho:", fontSize = 9.sp, color = TextSecondary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.BatteryFull,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "$batteryLevel% ${if (isCharging) "(⚡)" else ""}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text("Precisão do Satélite:", fontSize = 9.sp, color = TextSecondary)
                            Text(
                                "±${liveLoc.accuracyMeters.toInt()} metros",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberWarning
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action Buttons (Como Chegar, Abrir no Google Maps, Partilhar e Copiar)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Como Chegar (Rota)
                        Button(
                            onClick = {
                                isRouteEnabled = true
                                webViewRef?.evaluateJavascript("panToLocation($targetLat, $targetLng, 16); toggleRoute(true);", null)
                                Toast.makeText(context, "Traçando rota até o telemóvel roubado...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("map_route_button")
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null, tint = DarkBg, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rota", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        // Abrir no Google Maps Oficial
                        Button(
                            onClick = {
                                val gmmIntentUri = Uri.parse("google.navigation:q=$targetLat,$targetLng")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                try {
                                    context.startActivity(mapIntent)
                                } catch (e: Exception) {
                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=$targetLat,$targetLng"))
                                    context.startActivity(webIntent)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CardElevated),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("map_open_gmaps_app_button")
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Maps", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        // Partilhar Localização (SMS / WhatsApp)
                        IconButton(
                            onClick = {
                                val shareText = "🚨 ALERTA ANTI-ROUBO MZ: Localização em tempo real do telemóvel: https://maps.google.com/?q=$targetLat,$targetLng (Precisão: ±${liveLoc.accuracyMeters.toInt()}m, Bateria: $batteryLevel%)"
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Partilhar Localização Anti-Roubo"))
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CardElevated)
                                .testTag("map_share_coords_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Partilhar Alerta", tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                        }

                        // Copiar Coordenadas
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("GPS Coords", "$targetLat,$targetLng")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Coordenadas copiadas: $targetLat, $targetLng", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CardElevated)
                                .testTag("map_copy_coords_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar Coordenadas", tint = CyanAccent, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun generateInteractiveMapHtml(
    targetLat: Double,
    targetLng: Double,
    userLat: Double,
    userLng: Double,
    accuracyM: Int,
    layerType: String,
    showRoute: Boolean,
    deviceLabel: String
): String {
    return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <style>
        * { box-sizing: border-box; }
        html, body, #map {
            width: 100%;
            height: 100%;
            margin: 0;
            padding: 0;
            background: #090D16;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            overflow: hidden;
        }
        .leaflet-control-attribution, .leaflet-control-zoom {
            display: none !important;
        }
        .pulse-marker {
            width: 26px;
            height: 26px;
            border-radius: 50%;
            background: #FF1744;
            border: 3px solid #FFFFFF;
            box-shadow: 0 0 16px #FF1744;
            animation: pulse-red 1.6s infinite;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #FFFFFF;
            font-size: 11px;
            font-weight: bold;
        }
        .user-marker {
            width: 22px;
            height: 22px;
            border-radius: 50%;
            background: #00E5FF;
            border: 3px solid #FFFFFF;
            box-shadow: 0 0 14px #00E5FF;
            animation: pulse-blue 2s infinite;
        }
        .city-pin {
            background: rgba(13, 20, 36, 0.85);
            border: 1px solid #00E5FF;
            color: #FFFFFF;
            border-radius: 6px;
            padding: 2px 6px;
            font-size: 10px;
            font-weight: bold;
            white-space: nowrap;
        }
        @keyframes pulse-red {
            0% { transform: scale(0.92); box-shadow: 0 0 0 0 rgba(255, 23, 68, 0.8); }
            70% { transform: scale(1.15); box-shadow: 0 0 0 18px rgba(255, 23, 68, 0); }
            100% { transform: scale(0.92); box-shadow: 0 0 0 0 rgba(255, 23, 68, 0); }
        }
        @keyframes pulse-blue {
            0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(0, 229, 255, 0.7); }
            70% { transform: scale(1.1); box-shadow: 0 0 0 12px rgba(0, 229, 255, 0); }
            100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(0, 229, 255, 0); }
        }
    </style>
</head>
<body>
    <div id="map"></div>
    <script>
        var map = L.map('map', {
            center: [$targetLat, $targetLng],
            zoom: 16,
            zoomControl: false
        });

        // Robust map tile sources
        var satelliteLayer = L.tileLayer('https://mt1.google.com/vt/lyrs=y&x={x}&y={y}&z={z}', { maxZoom: 20 });
        var streetLayer = L.tileLayer('https://mt1.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', { maxZoom: 20 });
        var darkLayer = L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', { maxZoom: 19, subdomains: 'abcd' });
        var terrainLayer = L.tileLayer('https://mt1.google.com/vt/lyrs=p&x={x}&y={y}&z={z}', { maxZoom: 20 });

        var currentLayer = satelliteLayer;
        currentLayer.addTo(map);

        // Target Stolen Device Marker
        var redIcon = L.divIcon({ className: 'pulse-marker', iconSize: [26, 26], iconAnchor: [13, 13], html: '🚨' });
        var targetMarker = L.marker([$targetLat, $targetLng], { icon: redIcon }).addTo(map);
        targetMarker.bindPopup("<b>🚨 TELEMÓVEL ROUBADO MZ</b><br/>Titular: $deviceLabel<br/>Precisão: ±${accuracyM}m").openPopup();

        var targetCircle = L.circle([$targetLat, $targetLng], {
            radius: Math.max($accuracyM, 20),
            color: '#FF1744',
            fillColor: '#FF1744',
            fillOpacity: 0.22,
            weight: 2
        }).addTo(map);

        // User Current Location Marker
        var blueIcon = L.divIcon({ className: 'user-marker', iconSize: [22, 22], iconAnchor: [11, 11] });
        var userMarker = L.marker([$userLat, $userLng], { icon: blueIcon }).addTo(map);
        userMarker.bindPopup("<b>📍 SUA LOCALIZAÇÃO</b><br/>Ponto de Busca / Resgate");

        // Route Polyline
        var routeCoords = [
            [$userLat, $userLng],
            [$userLat - 0.0020, $userLng + 0.0015],
            [$userLat - 0.0045, $userLng + 0.0035],
            [$userLat - 0.0065, $userLng + 0.0060],
            [$targetLat, $targetLng]
        ];
        var routePolyline = L.polyline(routeCoords, { color: '#00E5FF', weight: 5, opacity: 0.9, dashArray: '8, 8' }).addTo(map);

        // Global functions for Kotlin Jetpack Compose bridge
        window.setLayer = function(type) {
            map.removeLayer(currentLayer);
            if (type === 'satellite') currentLayer = satelliteLayer;
            else if (type === 'dark') currentLayer = darkLayer;
            else if (type === 'terrain') currentLayer = terrainLayer;
            else currentLayer = streetLayer;
            currentLayer.addTo(map);
        };

        window.panToLocation = function(lat, lng, zoom) {
            map.flyTo([lat, lng], zoom || 16, { duration: 1.2 });
        };

        window.zoomIn = function() { map.zoomIn(); };
        window.zoomOut = function() { map.zoomOut(); };

        window.toggleRoute = function(show) {
            if (show) {
                if (!map.hasLayer(routePolyline)) routePolyline.addTo(map);
            } else {
                if (map.hasLayer(routePolyline)) map.removeLayer(routePolyline);
            }
        };

        window.updateTargetLocation = function(lat, lng, acc) {
            targetMarker.setLatLng([lat, lng]);
            targetCircle.setLatLng([lat, lng]);
            targetCircle.setRadius(Math.max(acc, 20));
        };
    </script>
</body>
</html>
    """.trimIndent()
}
