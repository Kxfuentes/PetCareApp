package com.proyectopoo.petcareapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import kotlinx.coroutines.delay

private const val POLL_INTERVAL_MS = 5_000L
private const val SIGNAL_LOST_THRESHOLD_MS = 60_000L
private const val DEFAULT_ZOOM = 16f

/**
 * Mapa de seguimiento en vivo para servicios de Taxi/Paseo: muestra la última posición GPS
 * reportada por el cuidador (GET /api/solicitudes/{id}/ubicacion-actual), refrescada por
 * polling cada 5s -- mismo intervalo con el que [com.proyectopoo.petcareapp.location.LocationReporter]
 * publica, y el mismo patrón de polling que ya usa el resto de la app (ver AppNavigation:
 * OwnerHome/CaregiverHome refrescan cada 10s con un LaunchedEffect + while(true) + delay).
 *
 * [wsRefreshTick] es opcional: si la pantalla que la aloja ya tiene acceso al tick global del
 * WebSocket (incrementado en MainActivity por cada evento, incluido LOCATION_UPDATE), pasarlo
 * aquí dispara un refresh inmediato sin esperar al siguiente ciclo de polling -- reutiliza el
 * WebSocket que ya existe en vez de abrir una segunda conexión dedicada a esta pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeguimientoMapaScreen(
    serviceRequestId: Int,
    onBack: () -> Unit,
    wsRefreshTick: Int = 0
) {
    var lastLat by remember { mutableStateOf<Double?>(null) }
    var lastLng by remember { mutableStateOf<Double?>(null) }
    var lastUpdatedAtMillis by remember { mutableStateOf<Long?>(null) }
    var hasLoadedOnce by remember { mutableStateOf(false) }
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    val cameraPositionState = rememberCameraPositionState()

    suspend fun refresh() {
        val response = runCatching {
            RetrofitClient.apiService.getUbicacionActual(serviceRequestId)
        }.getOrNull()

        // 404 (el cuidador aún no reportó ninguna ubicación) u otros errores de red no son un
        // estado de error para esta pantalla: simplemente seguimos esperando la primera
        // actualización (lastLat/lastLng se quedan en null y se muestra el texto de espera).
        if (response != null && response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                val isFirstFix = lastLat == null
                lastLat = body.latitud
                lastLng = body.longitud
                lastUpdatedAtMillis = parseIsoInstantMillis(body.actualizadoEn) ?: System.currentTimeMillis()

                if (isFirstFix) {
                    cameraPositionState.position =
                        CameraPosition.fromLatLngZoom(LatLng(body.latitud, body.longitud), DEFAULT_ZOOM)
                }
            }
        }
        hasLoadedOnce = true
    }

    LaunchedEffect(serviceRequestId) {
        while (true) {
            refresh()
            delay(POLL_INTERVAL_MS)
        }
    }

    LaunchedEffect(wsRefreshTick) {
        if (hasLoadedOnce) refresh()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000L)
            nowMillis = System.currentTimeMillis()
        }
    }

    val signalLost = lastUpdatedAtMillis?.let { (nowMillis - it) > SIGNAL_LOST_THRESHOLD_MS } ?: false

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Ubicación en tiempo real") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val lat = lastLat
            val lng = lastLng

            when {
                lat != null && lng != null -> {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        Marker(
                            state = MarkerState(position = LatLng(lat, lng)),
                            title = "Cuidador"
                        )
                    }

                    FloatingActionButton(
                        onClick = {
                            cameraPositionState.position =
                                CameraPosition.fromLatLngZoom(LatLng(lat, lng), DEFAULT_ZOOM)
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Centrar en cuidador")
                    }

                    if (signalLost) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(12.dp)
                        ) {
                            Text(
                                "Se perdió la señal del cuidador.",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                hasLoadedOnce -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Esperando la ubicación del cuidador...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

/**
 * Parsea un Instant ISO-8601 (p. ej. "2026-09-14T21:25:17.123Z") sin depender de java.time
 * (no disponible sin core library desugaring por debajo de API 26, y minSdk de este proyecto
 * es 24) -- mismo enfoque con SimpleDateFormat que ya usa el resto de la app para fechas.
 */
private fun parseIsoInstantMillis(iso: String): Long? {
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'"
    )
    for (pattern in patterns) {
        val millis = runCatching {
            java.text.SimpleDateFormat(pattern, java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
                isLenient = false
            }.parse(iso)?.time
        }.getOrNull()
        if (millis != null) return millis
    }
    return null
}
