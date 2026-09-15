package com.proyectopoo.petcareapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.data.network.AlertaCercanaDto
import com.proyectopoo.petcareapp.data.network.AlertaPerdidaEstado
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import com.proyectopoo.petcareapp.location.getOneShotLocation
import kotlinx.coroutines.launch

/**
 * Feed de alertas de mascota perdida activas cerca del usuario (Bloque 12), vía
 * GET /api/alertas-perdida/cercanas?lat=&lng=&radio=. Pantalla dedicada (en vez de una sección
 * dentro de OwnerFeedScreen, que está enfocada en ofertas de cuidadores) porque la aplican tanto
 * dueños como cuidadores por igual -- cualquier usuario puede reportar un avistamiento.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertasPerdidasScreen(
    currentUserId: Int,
    onBack: () -> Unit,
    onOpenAlerta: (AlertaCercanaDto) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var alertas by remember { mutableStateOf<List<AlertaCercanaDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var locationDenied by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf(false) }

    suspend fun reload() {
        isLoading = true
        loadError = false
        locationDenied = false
        val location = getOneShotLocation(context)
        if (location == null) {
            locationDenied = true
            isLoading = false
            return
        }
        val response = runCatching {
            RetrofitClient.apiService.getAlertasCercanas(location.latitude, location.longitude, 10.0)
        }.getOrNull()
        if (response?.isSuccessful == true) {
            alertas = response.body().orEmpty()
        } else {
            loadError = true
        }
        isLoading = false
    }

    LaunchedEffect(Unit) { reload() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mascotas perdidas cerca") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { scope.launch { reload() } }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                locationDenied -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Activa el permiso de ubicación para ver mascotas perdidas cerca de ti.",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { scope.launch { reload() } }) { Text("Reintentar") }
                    }
                }
                loadError -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("No se pudieron cargar las alertas cercanas.", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { scope.launch { reload() } }) { Text("Reintentar") }
                    }
                }
                alertas.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text("No hay mascotas perdidas reportadas cerca de ti ahora.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(alertas, key = { it.alerta.id ?: it.hashCode() }) { item ->
                            AlertaCercanaCard(item = item, onClick = { onOpenAlerta(item) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertaCercanaCard(item: AlertaCercanaDto, onClick: () -> Unit) {
    val alerta = item.alerta
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    alerta.descripcion?.takeIf { it.isNotBlank() } ?: "Mascota perdida",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        alerta.direccionTexto?.takeIf { it.isNotBlank() }
                            ?: "%.4f, %.4f".format(alerta.latitud, alerta.longitud),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item.distanciaKm?.let { km ->
                    Text(
                        "A %.1f km de ti".format(km),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text(estadoLabel(alerta.estado)) }
            )
        }
    }
}

private fun estadoLabel(estado: String?): String = when (estado) {
    AlertaPerdidaEstado.ACTIVA -> "Activa"
    AlertaPerdidaEstado.ENCONTRADA -> "Encontrada"
    AlertaPerdidaEstado.CERRADA -> "Cerrada"
    else -> estado ?: "?"
}
