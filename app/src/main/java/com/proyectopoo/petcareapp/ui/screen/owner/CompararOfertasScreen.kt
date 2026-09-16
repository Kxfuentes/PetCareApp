package com.proyectopoo.petcareapp.ui.screen.owner

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.data.local.relation.ServiceApplicationDetails
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import com.proyectopoo.petcareapp.ui.components.BadgeChip
import kotlinx.coroutines.launch

/**
 * Compara hasta 3 postulaciones (ofertas de cuidadores) para la misma solicitud, lado a lado
 * (Parte 3.1). Muestra los datos reales disponibles por postulación (cuidador, contacto, fecha
 * propuesta) más la calificación promedio del cuidador (obtenida en vivo, ya que
 * [ServiceApplicationDetails] no la incluye), con un botón "Aceptar" por columna.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompararOfertasScreen(
    ofertas: List<ServiceApplicationDetails>,
    onBack: () -> Unit,
    onAccept: (ServiceApplicationDetails) -> Unit,
    onReject: (ServiceApplicationDetails) -> Unit
) {
    val comparadas = remember(ofertas) { ofertas.take(3) }
    var ratings by remember { mutableStateOf<Map<Int, Pair<Double, Int>>>(emptyMap()) }
    var badges by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(comparadas) {
        val result = mutableMapOf<Int, Pair<Double, Int>>()
        val badgeResult = mutableMapOf<Int, String>()
        comparadas.forEach { oferta ->
            runCatching { RetrofitClient.apiService.getCaregiverRatingSummary(oferta.caregiverId) }
                .getOrNull()
                ?.takeIf { it.isSuccessful }
                ?.body()
                ?.let { result[oferta.caregiverId] = it.average to it.count }
            runCatching { RetrofitClient.apiService.getUsuarioBadge(oferta.caregiverId) }
                .getOrNull()
                ?.takeIf { it.isSuccessful }
                ?.body()
                ?.let { badgeResult[oferta.caregiverId] = it.badge }
        }
        ratings = result
        badges = badgeResult
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comparar ofertas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (comparadas.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hay ofertas para comparar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .horizontalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            comparadas.forEach { oferta ->
                OfertaColumn(
                    oferta = oferta,
                    rating = ratings[oferta.caregiverId],
                    badge = badges[oferta.caregiverId],
                    onAccept = { onAccept(oferta) },
                    onReject = { onReject(oferta) }
                )
            }
        }
    }
}

@Composable
private fun OfertaColumn(
    oferta: ServiceApplicationDetails,
    rating: Pair<Double, Int>?,
    badge: String?,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.width(240.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val caregiverName = oferta.caregiverName ?: "Cuidador #${oferta.caregiverId}"
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        caregiverName.firstOrNull()?.toString() ?: "C",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
            Text(caregiverName, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    rating?.let { (avg, count) -> "%.1f (%d)".format(avg, count) } ?: "Sin calificaciones",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            badge?.let { BadgeChip(it) }

            HorizontalDivider()

            ComparaRow("Servicio", oferta.serviceTypeName ?: oferta.requestTitle)
            ComparaRow("Fecha propuesta", oferta.requestedDate ?: "Por coordinar")
            listOfNotNull(oferta.startTime, oferta.endTime).takeIf { it.isNotEmpty() }?.let {
                ComparaRow("Horario", it.joinToString(" - "))
            }
            oferta.caregiverPhone?.takeIf { it.isNotBlank() }?.let { ComparaRow("Teléfono", it) }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Aceptar", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onReject,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Rechazar")
            }
        }
    }
}

@Composable
private fun ComparaRow(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
