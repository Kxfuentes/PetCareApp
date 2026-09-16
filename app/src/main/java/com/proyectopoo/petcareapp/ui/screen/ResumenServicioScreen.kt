package com.proyectopoo.petcareapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.proyectopoo.petcareapp.data.network.EvidenciaDto
import com.proyectopoo.petcareapp.data.network.EvidenciaTipo
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import com.proyectopoo.petcareapp.util.compartirSolicitud
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Resumen visual de un servicio ya completado (Parte 3.2): fotos antes/después lado a lado,
 * duración total (calculada entre la primera evidencia ANTES y la última DESPUÉS) y un botón
 * para compartir. Se abre desde una tarjeta de [HistorialScreen] con status COMPLETED.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenServicioScreen(
    serviceRequestId: Int,
    requestTitle: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var evidencias by remember(serviceRequestId) { mutableStateOf<List<EvidenciaDto>>(emptyList()) }
    var isLoading by remember(serviceRequestId) { mutableStateOf(true) }
    var loadError by remember(serviceRequestId) { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(serviceRequestId) {
        isLoading = true
        val response = runCatching { RetrofitClient.apiService.getEvidencias(serviceRequestId) }.getOrNull()
        if (response?.isSuccessful == true) {
            evidencias = response.body().orEmpty()
            loadError = false
        } else {
            loadError = true
        }
        isLoading = false
    }

    val antes = remember(evidencias) { evidencias.filter { it.tipo == EvidenciaTipo.ANTES } }
    val despues = remember(evidencias) { evidencias.filter { it.tipo == EvidenciaTipo.DESPUES } }
    val duracion = remember(evidencias) { calcularDuracion(antes, despues) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(requestTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                isLoading -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                loadError -> Text(
                    "No se pudo cargar el resumen de este servicio.",
                    color = MaterialTheme.colorScheme.error
                )
                else -> {
                    duracion?.let {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Duración total", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(it, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Text("Fotos del servicio", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    if (antes.isEmpty() && despues.isEmpty()) {
                        Text(
                            "Este servicio no tiene fotos de evidencia registradas.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            EvidenciaColumn(modifier = Modifier.weight(1f), label = "Antes", entries = antes)
                            EvidenciaColumn(modifier = Modifier.weight(1f), label = "Después", entries = despues)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (!compartirSolicitud(context, serviceRequestId)) {
                                // No hay app instalada capaz de manejar el share intent.
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Compartir experiencia", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EvidenciaColumn(modifier: Modifier = Modifier, label: String, entries: List<EvidenciaDto>) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        val imageUrl = entries.firstOrNull()?.imagenUrl?.let { RetrofitClient.resolveImageUrl(it) }
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = label,
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                error = ColorPainter(MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Sin foto", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private val evidenciaFechaFormats = listOf(
    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
    "yyyy-MM-dd'T'HH:mm:ssXXX",
    "yyyy-MM-dd'T'HH:mm:ss.SSS",
    "yyyy-MM-dd'T'HH:mm:ss",
    "yyyy-MM-dd HH:mm:ss"
)

private fun parseEvidenciaFecha(fecha: String?): Long? {
    if (fecha.isNullOrBlank()) return null
    for (pattern in evidenciaFechaFormats) {
        runCatching { SimpleDateFormat(pattern, Locale.US).parse(fecha)?.time }.getOrNull()?.let { return it }
    }
    return null
}

/** Diferencia entre la primera foto ANTES y la última DESPUÉS, en un formato legible ("1h 20min"). */
private fun calcularDuracion(antes: List<EvidenciaDto>, despues: List<EvidenciaDto>): String? {
    val inicio = antes.mapNotNull { parseEvidenciaFecha(it.fecha) }.minOrNull() ?: return null
    val fin = despues.mapNotNull { parseEvidenciaFecha(it.fecha) }.maxOrNull() ?: return null
    if (fin <= inicio) return null
    val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(fin - inicio)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours <= 0 -> "$minutes min"
        minutes <= 0 -> "$hours h"
        else -> "$hours h $minutes min"
    }
}
