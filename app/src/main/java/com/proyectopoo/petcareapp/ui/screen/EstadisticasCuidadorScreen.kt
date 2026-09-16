package com.proyectopoo.petcareapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import java.util.Calendar
import java.util.Locale

/**
 * Estadísticas personales del cuidador (Parte 3.4): servicios completados (total/este mes),
 * calificación promedio, tasa de aceptación y actividad de los últimos 6 meses. Todo calculado
 * en el momento a partir de endpoints que ya existen (historial, calificaciones y postulaciones),
 * sin agregados nuevos en el backend.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasCuidadorScreen(
    caregiverId: Int,
    onBack: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var totalCompletados by remember { mutableIntStateOf(0) }
    var completadosEsteMes by remember { mutableIntStateOf(0) }
    var promedio by remember { mutableStateOf(0.0) }
    var totalCalificaciones by remember { mutableIntStateOf(0) }
    var tasaAceptacion by remember { mutableStateOf<Double?>(null) }
    var actividadMensual by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }

    LaunchedEffect(caregiverId) {
        isLoading = true

        val historial = runCatching { RetrofitClient.apiService.getHistorialSolicitudes(caregiverId, "CAREGIVER") }
            .getOrNull()?.takeIf { it.isSuccessful }?.body().orEmpty()
        val completados = historial.filter { it.status == "COMPLETED" }
        totalCompletados = completados.size

        val ahora = Calendar.getInstance()
        completadosEsteMes = completados.count { esMesActual(it.requestedDate, ahora) }
        actividadMensual = actividadPorMes(completados.mapNotNull { it.requestedDate })

        runCatching { RetrofitClient.apiService.getCaregiverRatingSummary(caregiverId) }
            .getOrNull()?.takeIf { it.isSuccessful }?.body()?.let {
                promedio = it.average
                totalCalificaciones = it.count
            }

        val postulaciones = runCatching { RetrofitClient.apiService.getServiceApplicationsByCaregiver(caregiverId) }
            .getOrNull()?.takeIf { it.isSuccessful }?.body().orEmpty()
        val respondidas = postulaciones.filter { it.status != "PENDING" }
        if (respondidas.isNotEmpty()) {
            val aceptadas = respondidas.count { it.status in setOf("ACCEPTED", "DONE_BY_CAREGIVER", "COMPLETED") }
            tasaAceptacion = aceptadas.toDouble() / respondidas.size * 100.0
        }

        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis estadísticas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EstadisticaTile(modifier = Modifier.weight(1f), titulo = "Completados (total)", valor = totalCompletados.toString())
                EstadisticaTile(modifier = Modifier.weight(1f), titulo = "Este mes", valor = completadosEsteMes.toString())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EstadisticaTile(
                    modifier = Modifier.weight(1f),
                    titulo = "Calificación promedio",
                    valor = if (totalCalificaciones > 0) "%.1f ★".format(promedio) else "Sin datos"
                )
                EstadisticaTile(
                    modifier = Modifier.weight(1f),
                    titulo = "Tasa de aceptación",
                    valor = tasaAceptacion?.let { "%.0f%%".format(it) } ?: "Sin datos"
                )
            }

            Text("Actividad de los últimos 6 meses", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            ActividadMensualChart(actividadMensual)
        }
    }
}

@Composable
private fun EstadisticaTile(modifier: Modifier = Modifier, titulo: String, valor: String) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun ActividadMensualChart(datos: List<Pair<String, Int>>) {
    val maximo = (datos.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(140.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            datos.forEach { (mes, cantidad) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(cantidad.toString(), style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .fillMaxHeight(fraction = (cantidad.toFloat() / maximo).coerceIn(0.03f, 1f))
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(mes, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

/** true si [fecha] (dd/MM/yyyy o yyyy-MM-dd) cae en el mismo mes/año que [ahora]. */
internal fun esMesActual(fecha: String?, ahora: Calendar): Boolean {
    val cal = parseFechaSolicitud(fecha) ?: return false
    return cal.get(Calendar.YEAR) == ahora.get(Calendar.YEAR) && cal.get(Calendar.MONTH) == ahora.get(Calendar.MONTH)
}

/** Agrupa por mes calendario (etiqueta corta) los últimos 6 meses, incluyendo meses en cero. */
internal fun actividadPorMes(fechas: List<String>): List<Pair<String, Int>> {
    val ahora = Calendar.getInstance()
    val meses = (5 downTo 0).map {
        val cal = ahora.clone() as Calendar
        cal.add(Calendar.MONTH, -it)
        cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH)
    }
    val conteos = fechas.mapNotNull { parseFechaSolicitud(it) }
        .groupingBy { it.get(Calendar.YEAR) to it.get(Calendar.MONTH) }
        .eachCount()
    val formatter = java.text.SimpleDateFormat("MMM", Locale("es"))
    return meses.map { (anio, mes) ->
        val cal = Calendar.getInstance().apply { set(anio, mes, 1) }
        formatter.format(cal.time).replaceFirstChar { it.uppercase() } to (conteos[anio to mes] ?: 0)
    }
}

private fun parseFechaSolicitud(fecha: String?): Calendar? {
    if (fecha.isNullOrBlank()) return null
    val patterns = listOf("dd/MM/yyyy", "yyyy-MM-dd")
    for (pattern in patterns) {
        runCatching {
            val date = java.text.SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }.parse(fecha)
            if (date != null) return Calendar.getInstance().apply { time = date }
        }
    }
    return null
}
