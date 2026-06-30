package com.proyectopoo.petcareapp.ui.screen.caregiver

/*
 * Comentario de modulo PetCare:
 * Pantalla de la app. Contiene la estructura visual y conecta acciones del usuario con el ViewModel.
 */

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.proyectopoo.petcareapp.data.local.entity.ApplicationStatus
import com.proyectopoo.petcareapp.data.local.relation.ServiceApplicationDetails
import com.proyectopoo.petcareapp.ui.components.StarRatingInput

@Composable
fun CaregiverHomeScreen(
    onGoToServices: () -> Unit,
    ownerRequests: List<ServiceApplicationDetails>,
    onAcceptApplication: (Int) -> Unit,
    onRejectApplication: (Int) -> Unit,
    onCompleteAndRate: (ServiceApplicationDetails, Double, String) -> Unit,
    onCancelService: (ServiceApplicationDetails) -> Unit = {},
    caregiverId: Int
) {
    // Estados propios de UI: disponibilidad, dialogos y valoracion temporal.
    var available by remember { mutableStateOf(true) }
    var showHeader by remember { mutableStateOf(true) }
    var requestToRate by remember { mutableStateOf<ServiceApplicationDetails?>(null) }
    var requestToDetails by remember { mutableStateOf<ServiceApplicationDetails?>(null) }
    var ratingScore by remember { mutableStateOf(5f) }
    var ratingComment by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()
    val pendingRequests = ownerRequests.filter { it.applicationStatus == ApplicationStatus.PENDING }
    val acceptedRequests = ownerRequests.filter { it.applicationStatus == ApplicationStatus.ACCEPTED }
    val waitingOwnerConfirmation = ownerRequests.filter { it.applicationStatus == ApplicationStatus.DONE_BY_CAREGIVER }
    val nextCommitment = acceptedRequests.minByOrNull { it.requestedDate ?: "" }

    // La pantalla separa solicitudes pendientes, servicios activos y cierres por confirmar.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp)
    ) {
        if (showHeader) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sección de cuidador", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Gestiona tus servicios y solicitudes", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                    }
                    IconButton(onClick = { showHeader = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            AvailabilityCard(available = available, onAvailableChange = { available = it })

            SectionTitle("Próximo compromiso")
            CommitmentCard(commitment = nextCommitment, onClick = { nextCommitment?.let { requestToDetails = it } })

            Card(
                onClick = onGoToServices,
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
            ) {
                Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconBubble(Icons.Default.List)
                    Spacer(Modifier.width(14.dp))
                    Text("Mis servicios", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            SectionTitle("Dueños que solicitaron tus servicios")
            if (pendingRequests.isEmpty()) {
                EmptyStateCard("Aún no tienes solicitudes pendientes.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    pendingRequests.forEach { request ->
                        OwnerRequestCard(
                            request = request,
                            onDetails = { requestToDetails = request },
                            onAccept = { onAcceptApplication(request.applicationId) },
                            onReject = { onRejectApplication(request.applicationId) }
                        )
                    }
                }
            }

            if (acceptedRequests.isNotEmpty()) {
                SectionTitle("Servicios activos")
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    acceptedRequests.forEach { request ->
                        ActiveServiceCard(
                            request = request,
                            onDetails = { requestToDetails = request },
                            onCancel = { onCancelService(request) },
                            onFinish = {
                                requestToRate = request
                                ratingScore = 5f
                                ratingComment = ""
                            }
                        )
                    }
                }
            }

            if (waitingOwnerConfirmation.isNotEmpty()) {
                SectionTitle("Pendientes de calificación")
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    waitingOwnerConfirmation.forEach { request ->
                        WaitingConfirmationCard(request = request, onClick = { requestToDetails = request })
                    }
                }
            }

        }
    }

    requestToDetails?.let { request ->
        CaregiverServiceDetailsDialog(request = request, onDismiss = { requestToDetails = null })
    }

    requestToRate?.let { request ->
        AlertDialog(
            onDismissRequest = { requestToRate = null },
            title = { Text("Calificar dueño") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StarRatingInput(value = ratingScore, onValueChange = { ratingScore = it })
                    OutlinedTextField(
                        value = ratingComment,
                        onValueChange = { ratingComment = it },
                        label = { Text("Comentario opcional") },
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCompleteAndRate(request, ratingScore.toDouble(), ratingComment)
                        requestToRate = null
                    }
                ) { Text("Guardar y finalizar") }
            },
            dismissButton = {
                TextButton(onClick = { requestToRate = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun AvailabilityCard(available: Boolean, onAvailableChange: (Boolean) -> Unit) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Estado actual", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Text(if (available) "Disponible para trabajar" else "En descanso", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Switch(checked = available, onCheckedChange = onAvailableChange)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
}

@Composable
private fun EmptyStateCard(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Text(text, modifier = Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CommitmentCard(commitment: ServiceApplicationDetails?, onClick: () -> Unit) {
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(64.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.EventAvailable, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    commitment?.let { "${it.serviceTypeName ?: it.requestTitle} con ${it.petNames ?: it.petName ?: "mascota"}" }
                        ?: "Sin compromisos próximos",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    commitment?.requestedDate ?: "Cuando aceptes una solicitud aparecerá aquí",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun OwnerRequestCard(
    request: ServiceApplicationDetails,
    onDetails: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(onClick = onDetails, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBubble(Icons.Default.Person)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(request.ownerName ?: "Dueño", fontWeight = FontWeight.Bold)
                    Text("${request.serviceTypeName ?: request.requestTitle} · ${request.petNames ?: request.petName ?: "Mascota"}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Text("Fecha: ${request.requestedDate ?: "--"} · Hora: ${request.startTime ?: "--"}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                StatusChip(request.applicationStatus)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onAccept, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text("Aceptar") }
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text("Rechazar") }
            }
        }
    }
}

@Composable
private fun ActiveServiceCard(
    request: ServiceApplicationDetails,
    onDetails: () -> Unit,
    onCancel: () -> Unit,
    onFinish: () -> Unit
) {
    Card(onClick = onDetails, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBubble(serviceIcon(request.serviceTypeName ?: request.requestTitle))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(request.serviceTypeName ?: request.requestTitle, fontWeight = FontWeight.Bold)
                    Text("Con ${request.ownerName ?: "dueño"} · ${request.petNames ?: request.petName ?: "Mascota"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Cancelar") }
                Button(onClick = onFinish, modifier = Modifier.weight(1f)) { Text("Finalizar") }
            }
        }
    }
}

@Composable
private fun WaitingConfirmationCard(request: ServiceApplicationDetails, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBubble(Icons.Default.HourglassTop)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(request.serviceTypeName ?: request.requestTitle, fontWeight = FontWeight.Bold)
                Text("Esperando confirmación del dueño.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatusChip(request.applicationStatus)
        }
    }
}

@Composable
private fun CompletedServiceCard(request: ServiceApplicationDetails, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBubble(Icons.Default.CheckCircle)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(request.serviceTypeName ?: request.requestTitle, fontWeight = FontWeight.Bold)
                Text("Finalizado con ${request.ownerName ?: "dueño"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatusChip(request.applicationStatus)
        }
    }
}

@Composable
private fun CaregiverServiceDetailsDialog(request: ServiceApplicationDetails, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(request.serviceTypeName ?: request.requestTitle, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Cerrar") }
                }
                DetailRow("Dueño", request.ownerName)
                DetailRow("Mascota", request.petNames ?: request.petName)
                DetailRow("Fecha", request.requestedDate)
                DetailRow("Hora", listOfNotNull(request.startTime, request.endTime).joinToString(" - ").ifBlank { null })
                DetailRow("Estado", statusText(request.applicationStatus.name))
                DetailRow("Descripción", request.requestDescription)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun IconBubble(icon: ImageVector) {
    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(46.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun StatusChip(status: Enum<*>) {
    val (text, color) = when (status.name.uppercase()) {
        "PENDING" -> "Pendiente" to Color(0xFFFF9800)
        "ACCEPTED" -> "Aceptado" to Color(0xFF4CAF50)
        "DONE_BY_CAREGIVER" -> "Por confirmar" to Color(0xFF2196F3)
        "COMPLETED" -> "Completado" to Color(0xFF607D8B)
        "REJECTED" -> "Rechazado" to Color(0xFFF44336)
        "CANCELLED" -> "Cancelado" to Color(0xFFF44336)
        else -> status.name.replaceFirstChar { it.uppercase() } to MaterialTheme.colorScheme.outline
    }

    AssistChip(
        onClick = {},
        label = { Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = Color.Black) },
        colors = AssistChipDefaults.assistChipColors(containerColor = color.copy(alpha = 0.1f)),
        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = color.copy(alpha = 0.8f), borderWidth = 1.dp),
        shape = RoundedCornerShape(20.dp)
    )
}

private fun serviceIcon(serviceName: String): ImageVector {
    return when (serviceName.lowercase()) {
        "paseo" -> Icons.Default.DirectionsWalk
        "alojamiento" -> Icons.Default.Home
        "guardería", "guarderia" -> Icons.Default.WbSunny
        "taxi" -> Icons.Default.LocalTaxi
        "peluquería", "peluqueria" -> Icons.Default.ContentCut
        "visitante" -> Icons.Default.HomeRepairService
        else -> Icons.Default.Assignment
    }
}

private fun statusText(status: String): String {
    return when (status.uppercase()) {
        "PENDING" -> "Pendiente"
        "ACCEPTED" -> "Aceptado"
        "DONE_BY_CAREGIVER" -> "Pendiente de confirmar"
        "COMPLETED" -> "Completado"
        "REJECTED" -> "Rechazado"
        "CANCELLED" -> "Cancelado"
        else -> status
    }
}
