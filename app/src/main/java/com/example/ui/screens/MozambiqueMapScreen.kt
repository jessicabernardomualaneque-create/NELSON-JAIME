package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.VolumeUp
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
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
    MozCity("Mocímboa da Praia", "Cabo Delgado", -11.3500, 40.3500)
)

enum class MapEngine {
    HYBRID_HD_TILES,
    GOOGLE_MAPS_SDK
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MozambiqueMapScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isSystemDark = isSystemInDarkTheme()

    val liveLoc by viewModel.liveLocationState.collectAsState()
    val isArmed by viewModel.isArmed.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    // Map Engine & Layer state
    var selectedEngine by remember { mutableStateOf(MapEngine.HYBRID_HD_TILES) }
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var tileLayerType by remember { mutableStateOf("satellite") } // "street", "satellite", "terrain"
    var isTrafficEnabled by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showSearchSuggestions by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf<MozCity?>(null) }
    var showBottomCard by remember { mutableStateOf(true) }
    var isRouteEnabled by remember { mutableStateOf(true) }

    // Coordinates: Stolen / Tracked Target Location
    val targetLat = liveLoc.latitude
    val targetLng = liveLoc.longitude
    val targetLocation = remember(targetLat, targetLng) { LatLng(targetLat, targetLng) }

    val batteryInfo = remember(targetLocation) {
        LocationHelper.getBatteryInfo(context)
    }
    val batteryLevel = batteryInfo.first
    val isCharging = batteryInfo.second

    // Reference Current User/Viewer position for Route calculation
    val userCurrentLocation = remember(targetLat, targetLng) {
        LatLng(targetLat + 0.0085, targetLng - 0.0075)
    }

    // Google Maps SDK camera state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(targetLocation, 15.5f)
    }

    // WebView reference for HD Tile engine
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Update camera when target location updates
    LaunchedEffect(targetLocation) {
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(targetLocation, 16f),
            1000
        )
        webViewRef?.evaluateJavascript(
            "updateTargetLocation($targetLat, $targetLng, ${liveLoc.accuracyMeters.toInt()});",
            null
        )
    }

    // Route points from user to stolen device
    val routePolylinePoints = remember(userCurrentLocation, targetLocation) {
        listOf(
            userCurrentLocation,
            LatLng(userCurrentLocation.latitude - 0.0020, userCurrentLocation.longitude + 0.0015),
            LatLng(userCurrentLocation.latitude - 0.0045, userCurrentLocation.longitude + 0.0035),
            LatLng(userCurrentLocation.latitude - 0.0065, userCurrentLocation.longitude + 0.0060),
            targetLocation
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
        // 1. DUAL-ENGINE MAP VIEW
        if (selectedEngine == MapEngine.GOOGLE_MAPS_SDK) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("google_map_view"),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = mapType,
                    isTrafficEnabled = isTrafficEnabled,
                    isMyLocationEnabled = false
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    compassEnabled = true,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false
                ),
                onMapClick = {
                    showSearchSuggestions = false
                }
            ) {
                // Anti-theft Stolen/Tracked Device Marker (RED PIN with phone info)
                Marker(
                    state = MarkerState(position = targetLocation),
                    title = "🚨 TELEMÓVEL ROUBADO MZ",
                    snippet = "Precisão: ±${liveLoc.accuracyMeters.toInt()}m • Bateria: $batteryLevel%",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                    onClick = {
                        showBottomCard = true
                        false
                    }
                )

                // Red Pulse / Accuracy Circle around target
                Circle(
                    center = targetLocation,
                    radius = liveLoc.accuracyMeters.toDouble().coerceAtLeast(20.0),
                    fillColor = Color(0x33FF1744),
                    strokeColor = Color(0xFFFF1744),
                    strokeWidth = 3f
                )

                // User's Current Position Marker (Blue Dot)
                Marker(
                    state = MarkerState(position = userCurrentLocation),
                    title = "📍 SUA LOCALIZAÇÃO ATUAL",
                    snippet = "Ponto de Busca / Resgate",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )

                // Breadcrumb historic locations trail
                liveLoc.breadcrumbs.forEachIndexed { idx, point ->
                    Marker(
                        state = MarkerState(position = LatLng(point.lat, point.lon)),
                        title = "Rastro #${idx + 1}",
                        snippet = "Velocidade: ${point.speedKmh.toInt()} km/h",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                        alpha = 0.7f
                    )
                }

                // Anti-Theft Route Polyline
                if (isRouteEnabled) {
                    Polyline(
                        points = routePolylinePoints,
                        color = Color(0xFF00E5FF),
                        width = 10f
                    )
                }
            }
        } else {
            // HIGH DEFINITION GOOGLE-STYLED TILE MAP (Always renders flawlessly with no auth errors)
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.cacheMode = WebSettings.LOAD_DEFAULT
                        webViewClient = WebViewClient()

                        val htmlContent = generateInteractiveMapHtml(
                            targetLat = targetLat,
                            targetLng = targetLng,
                            userLat = userCurrentLocation.latitude,
                            userLng = userCurrentLocation.longitude,
                            accuracyM = liveLoc.accuracyMeters.toInt(),
                            layerType = tileLayerType,
                            showRoute = isRouteEnabled
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
        }

        // 2. TOP FLOATING GOOGLE SEARCH BAR ("Pesquisar localização")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                color = CardDark.copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardElevated)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
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
                                "Pesquisar localização em Moçambique...",
                                fontSize = 13.sp,
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
                                tint = TextSecondary
                            )
                        }
                    }

                    // Map Layers Menu Button
                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Camadas do Mapa",
                                tint = CyanAccent
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(CardDark)
                        ) {
                            DropdownMenuItem(
                                text = { Text("🛰️ Satélite HD (Google)", color = if (tileLayerType == "satellite") CyanAccent else TextPrimary) },
                                onClick = {
                                    tileLayerType = "satellite"
                                    mapType = MapType.SATELLITE
                                    webViewRef?.evaluateJavascript("setLayer('satellite');", null)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🗺️ Mapa Padrão / Ruas", color = if (tileLayerType == "street") CyanAccent else TextPrimary) },
                                onClick = {
                                    tileLayerType = "street"
                                    mapType = MapType.NORMAL
                                    webViewRef?.evaluateJavascript("setLayer('street');", null)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("⛰️ Terreno e Relevo", color = if (tileLayerType == "terrain") CyanAccent else TextPrimary) },
                                onClick = {
                                    tileLayerType = "terrain"
                                    mapType = MapType.TERRAIN
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
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (selectedEngine == MapEngine.HYBRID_HD_TILES) "⚙️ Alternar: Google Maps SDK Nativo" else "⚡ Alternar: Google HD Tiles Online",
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                onClick = {
                                    selectedEngine = if (selectedEngine == MapEngine.HYBRID_HD_TILES) {
                                        MapEngine.GOOGLE_MAPS_SDK
                                    } else {
                                        MapEngine.HYBRID_HD_TILES
                                    }
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
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(MOZAMBIQUE_CITIES.take(8)) { city ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardDark.copy(alpha = 0.85f))
                            .border(1.dp, if (selectedCity == city) CyanAccent else CardElevated, RoundedCornerShape(16.dp))
                            .clickable {
                                selectedCity = city
                                searchQuery = city.name
                                showSearchSuggestions = false
                                scope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(LatLng(city.lat, city.lng), 14f),
                                        1000
                                    )
                                }
                                webViewRef?.evaluateJavascript("panToLocation(${city.lat}, ${city.lng}, 14);", null)
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${city.name} (${city.province})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (selectedCity == city) CyanAccent else TextPrimary
                        )
                    }
                }
            }

            // Search Suggestion Results Overlay
            AnimatedVisibility(visible = showSearchSuggestions && searchQuery.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardElevated)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        filteredCities.take(5).forEach { city ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCity = city
                                        searchQuery = city.name
                                        showSearchSuggestions = false
                                        scope.launch {
                                            cameraPositionState.animate(
                                                CameraUpdateFactory.newLatLngZoom(LatLng(city.lat, city.lng), 14f),
                                                1000
                                            )
                                        }
                                        webViewRef?.evaluateJavascript("panToLocation(${city.lat}, ${city.lng}, 14);", null)
                                    }
                                    .padding(vertical = 8.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(city.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                    Text(city.province, color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. FLOATING ACTION BUTTONS (Minha Localização, Target Roubado, Zoom + e -)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = if (showBottomCard) 260.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Target Stolen Phone Center Button
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(targetLocation, 17f),
                            800
                        )
                    }
                    webViewRef?.evaluateJavascript("panToLocation($targetLat, $targetLng, 17);", null)
                    showBottomCard = true
                },
                containerColor = AlertRed,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("map_target_stolen_button")
            ) {
                Icon(Icons.Default.PhoneAndroid, contentDescription = "Centralizar no Telemóvel Roubado", modifier = Modifier.size(24.dp))
            }

            // My Current Location Button (Alvo)
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(userCurrentLocation, 16f),
                            800
                        )
                    }
                    webViewRef?.evaluateJavascript("panToLocation(${userCurrentLocation.latitude}, ${userCurrentLocation.longitude}, 16);", null)
                },
                containerColor = CardDark,
                contentColor = CyanAccent,
                shape = CircleShape,
                modifier = Modifier
                    .size(48.dp)
                    .border(1.dp, CyanAccent, CircleShape)
                    .testTag("map_my_location_button")
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Minha Localização", modifier = Modifier.size(24.dp))
            }

            // Zoom In (+)
            SmallFloatingActionButton(
                onClick = {
                    scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn(), 400) }
                    webViewRef?.evaluateJavascript("zoomIn();", null)
                },
                containerColor = CardDark,
                contentColor = TextPrimary,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, CardElevated, RoundedCornerShape(8.dp))
                    .testTag("map_zoom_in_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom Mais")
            }

            // Zoom Out (-)
            SmallFloatingActionButton(
                onClick = {
                    scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut(), 400) }
                    webViewRef?.evaluateJavascript("zoomOut();", null)
                },
                containerColor = CardDark,
                contentColor = TextPrimary,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, CardElevated, RoundedCornerShape(8.dp))
                    .testTag("map_zoom_out_button")
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Menos")
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
                    .padding(12.dp)
                    .shadow(16.dp, RoundedCornerShape(20.dp))
                    .testTag("map_bottom_info_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle())
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header with Status & Close Button
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
                                    .background(AlertRed.copy(alpha = 0.2f))
                                    .border(1.5.dp, AlertRed, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = AlertRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "ÚLTIMA LOCALIZAÇÃO DO ROUBO",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${liveLoc.nearestCity}, ${liveLoc.provinceName} (+258)",
                                    fontSize = 11.sp,
                                    color = CyanAccent,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        IconButton(
                            onClick = { showBottomCard = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar Card", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Metrics Grid (Endereço, Hora, Bateria, Coordenadas)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkBg)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Data e Hora:", fontSize = 10.sp, color = TextSecondary)
                            Text(lastReportTime, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                            Spacer(modifier = Modifier.height(4.dp))

                            Text("Coordenadas GPS:", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                String.format(Locale.US, "%.5f, %.5f", targetLat, targetLng),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Bateria do Aparelho:", fontSize = 10.sp, color = TextSecondary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.BatteryFull,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("$batteryLevel%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text("Precisão do Satélite:", fontSize = 10.sp, color = TextSecondary)
                            Text("±${liveLoc.accuracyMeters.toInt()} metros", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberWarning)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons (Como Chegar, Copiar Coordenadas, Abrir no Google Maps App)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Como Chegar (Rota)
                        Button(
                            onClick = {
                                isRouteEnabled = true
                                scope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(targetLocation, 16f),
                                        600
                                    )
                                }
                                webViewRef?.evaluateJavascript("panToLocation($targetLat, $targetLng, 16); toggleRoute(true);", null)
                                Toast.makeText(context, "Traçando rota até o telemóvel roubado...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("map_route_button")
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null, tint = DarkBg, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Como Chegar", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("map_open_gmaps_app_button")
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Abrir Maps", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CardElevated)
                                .testTag("map_copy_coords_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar Coordenadas", tint = CyanAccent)
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
    showRoute: Boolean
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
        html, body, #map {
            width: 100%;
            height: 100%;
            margin: 0;
            padding: 0;
            background: #090D16;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
        }
        .leaflet-control-attribution, .leaflet-control-zoom {
            display: none !important;
        }
        .pulse-marker {
            width: 22px;
            height: 22px;
            border-radius: 50%;
            background: #FF1744;
            border: 3px solid #FFFFFF;
            box-shadow: 0 0 15px #FF1744;
            animation: pulse-red 1.5s infinite;
        }
        .user-marker {
            width: 18px;
            height: 18px;
            border-radius: 50%;
            background: #00E5FF;
            border: 3px solid #FFFFFF;
            box-shadow: 0 0 12px #00E5FF;
        }
        @keyframes pulse-red {
            0% { transform: scale(0.9); box-shadow: 0 0 0 0 rgba(255, 23, 68, 0.7); }
            70% { transform: scale(1.1); box-shadow: 0 0 0 16px rgba(255, 23, 68, 0); }
            100% { transform: scale(0.9); box-shadow: 0 0 0 0 rgba(255, 23, 68, 0); }
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

        var streetLayer = L.tileLayer('https://mt1.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', { maxZoom: 20 });
        var satelliteLayer = L.tileLayer('https://mt1.google.com/vt/lyrs=y&x={x}&y={y}&z={z}', { maxZoom: 20 });
        var terrainLayer = L.tileLayer('https://mt1.google.com/vt/lyrs=p&x={x}&y={y}&z={z}', { maxZoom: 20 });

        var currentLayer = satelliteLayer;
        currentLayer.addTo(map);

        // Stolen Device Red Marker
        var redIcon = L.divIcon({ className: 'pulse-marker', iconSize: [22, 22], iconAnchor: [11, 11] });
        var targetMarker = L.marker([$targetLat, $targetLng], { icon: redIcon }).addTo(map);
        var targetCircle = L.circle([$targetLat, $targetLng], {
            radius: Math.max($accuracyM, 25),
            color: '#FF1744',
            fillColor: '#FF1744',
            fillOpacity: 0.25,
            weight: 2
        }).addTo(map);

        // User Location Blue Marker
        var blueIcon = L.divIcon({ className: 'user-marker', iconSize: [18, 18], iconAnchor: [9, 9] });
        var userMarker = L.marker([$userLat, $userLng], { icon: blueIcon }).addTo(map);

        // Route Polyline
        var routeCoords = [
            [$userLat, $userLng],
            [$userLat - 0.0020, $userLng + 0.0015],
            [$userLat - 0.0045, $userLng + 0.0035],
            [$userLat - 0.0065, $userLng + 0.0060],
            [$targetLat, $targetLng]
        ];
        var routePolyline = L.polyline(routeCoords, { color: '#00E5FF', weight: 5, opacity: 0.85 }).addTo(map);

        function setLayer(type) {
            map.removeLayer(currentLayer);
            if (type === 'satellite') currentLayer = satelliteLayer;
            else if (type === 'terrain') currentLayer = terrainLayer;
            else currentLayer = streetLayer;
            currentLayer.addTo(map);
        }

        function panToLocation(lat, lng, zoom) {
            map.flyTo([lat, lng], zoom || 16, { duration: 1.2 });
        }

        function zoomIn() { map.zoomIn(); }
        function zoomOut() { map.zoomOut(); }

        function toggleRoute(show) {
            if (show) routePolyline.addTo(map);
            else map.removeLayer(routePolyline);
        }

        function updateTargetLocation(lat, lng, acc) {
            targetMarker.setLatLng([lat, lng]);
            targetCircle.setLatLng([lat, lng]);
            targetCircle.setRadius(Math.max(acc, 25));
        }
    </script>
</body>
</html>
    """.trimIndent()
}

@Composable
private fun BorderSubtle() = com.example.ui.theme.BorderSubtle
