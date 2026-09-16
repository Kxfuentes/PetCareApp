package com.proyectopoo.petcareapp.ui.screen

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

/**
 * Estadísticas personales del propietario (Parte 3.5): servicios contratados (total/este mes),
 * gasto (cuando la solicitud incluye una línea "Precio:" en su descripción — no todas la tienen,
 * así que se muestra como estimado), cuidadores favoritos guardados y actividad de 6 meses.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasPropietarioScreen(
    ownerId: Int,
    onBack: () -> Unit,
    onVerFavoritos: () -> Unit = {}
) {
    var isLoading by remember { mutableStateOf(true) }
    var totalContratados by remember { mutableIntStateOf(0) }
    var contratadosEsteMes by remember { mutableIntStateOf(0) }
    var gastoTotal by remember { mutableStateOf<Double?>(null) }
    var gastoPromedio by remember { mutableStateOf<Double?>(null) }
    var favoritosCount by remember { mutableIntStateOf(0) }
    var actividadMensual by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }

    LaunchedEffect(ownerId) {
        isLoading = true

        val historial = runCatching { RetrofitClient.apiService.getHistorialSolicitudes(ownerId, "OWNER") }
            .getOrNull()?.takeIf { it.isSuccessful }?.body().orEmpty()
        val completados = historial.filter { it.status == "COMPLETED" }
        totalContratados = completados.size

        val ahora = Calendar.getInstance()
        contratadosEsteMes = completados.count { esMesActual(it.requestedDate, ahora) }
        actividadMensual = actividadPorMes(completados.mapNotNull { it.requestedDate })

        val precios = completados.mapNotNull { extraerPrecio(it.description) }
        if (precios.isNotEmpty()) {
            gastoTotal = precios.sum()
            gastoPromedio = precios.average()
        }

        runCatching { RetrofitClient.apiService.getFavoritos(ownerId) }
            .getOrNull()?.takeIf { it.isSuccessful }?.body()?.let { favoritosCount = it.size }

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
                EstadisticaTilePropietario(modifier = Modifier.weight(1f), titulo = "Contratados (total)", valor = totalContratados.toString())
                EstadisticaTilePropietario(modifier = Modifier.weight(1f), titulo = "Este mes", valor = contratadosEsteMes.toString())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EstadisticaTilePropietario(
                    modifier = Modifier.weight(1f),
                    titulo = "Gasto total (estimado)",
                    valor = gastoTotal?.let { "C$ %.0f".format(it) } ?: "Sin datos"
                )
                EstadisticaTilePropietario(
                    modifier = Modifier.weight(1f),
                    titulo = "Gasto promedio",
                    valor = gastoPromedio?.let { "C$ %.0f".format(it) } ?: "Sin datos"
                )
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                onClick = onVerFavoritos
            ) {
                Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Cuidadores favoritos", fontWeight = FontWeight.Medium)
                        Text("$favoritosCount guardados", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("Ver →", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            Text("Actividad de los últimos 6 meses", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            ActividadMensualChart(actividadMensual)
        }
    }
}

@Composable
private fun EstadisticaTilePropietario(modifier: Modifier = Modifier, titulo: String, valor: String) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

/** Busca una línea "Precio: C$123" (o "Precio: 123") en la descripción de la solicitud, si existe. */
private fun extraerPrecio(description: String?): Double? {
    val linea = description
        ?.lineSequence()
        ?.map { it.trim() }
        ?.firstOrNull { it.startsWith("Precio:", ignoreCase = true) }
        ?: return null
    val numero = linea.substringAfter(":").trim().filter { it.isDigit() || it == '.' }
    return numero.toDoubleOrNull()
}
