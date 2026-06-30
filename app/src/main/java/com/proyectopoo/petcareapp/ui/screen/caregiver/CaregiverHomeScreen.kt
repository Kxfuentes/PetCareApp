package com.proyectopoo.petcareapp.ui.screen.caregiver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.proyectopoo.petcareapp.data.local.entity.ApplicationStatus
import com.proyectopoo.petcareapp.data.local.relation.ServiceApplicationDetails
import kotlin.math.roundToInt

@Composable
fun CaregiverHomeScreen(
    onGoToServices: () -> Unit,
    ownerRequests: List<ServiceApplicationDetails>,
    scheduledServices: List<ServiceApplicationDetails>,
    onAcceptApplication: (Int) -> Unit,
    onRejectApplication: (Int) -> Unit,
    onCompleteAndRate: (ServiceApplicationDetails, Double, String) -> Unit,
    onCancelService: (ServiceApplicationDetails) -> Unit = {},
    onScheduledClick: (ServiceApplicationDetails) -> Unit = {},
    caregiverId: Int
) {
    var available by remember { mutableStateOf(true) }
    var showHeader by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()
    val nextCommitment = scheduledServices.filter { it.applicationStatus == ApplicationStatus.ACCEPTED }.minByOrNull { it.requestedDate ?: "" }
    val pendingRequests = ownerRequests.filter { it.applicationStatus == ApplicationStatus.PENDING && it.initiatedBy == com.proyectopoo.petcareapp.data.local.entity.ApplicationInitiator.OWNER }
    val acceptedRequests = scheduledServices.filter { it.applicationStatus == ApplicationStatus.ACCEPTED }
    var requestToRate by remember { mutableStateOf<ServiceApplicationDetails?>(null) }
    var requestToDetails by remember { mutableStateOf<ServiceApplicationDetails?>(null) }
    var ratingScore by remember { mutableStateOf(5f) }
    var ratingComment by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp)
    ) {
        if (showHeader) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(Modifier.padding(horizontal = 20.dp, vertical = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Sección de cuidador", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Gestiona tus servicios y solicitudes", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .9f), style = MaterialTheme.typography.bodyMedium)
                    }
                    IconButton(onClick = { showHeader = false }) { Icon(Icons.Default.Close, "Cerrar", tint = MaterialTheme.colorScheme.onPrimary) }
                }
            }
        }

        Column(Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp))) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Estado actual", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if (available) "Disponible para trabajar" else "En descanso", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                    Switch(checked = available, onCheckedChange = { available = it }, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onPrimary, checkedTrackColor = MaterialTheme.colorScheme.primary))
                }
            }

            SectionLabel("Próximo compromiso")
            CommitmentCard(commitment = nextCommitment)

            Card(onClick = onGoToServices, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(52.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.List, null, tint = MaterialTheme.colorScheme.primary) } }
                    Spacer(Modifier.width(16.dp))
                    Text("Mis servicios", Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            SectionLabel("Dueños que solicitaron tus servicios")
            if (pendingRequests.isEmpty()) EmptyCaregiverCard("Aún no tienes solicitudes pendientes.") else Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                pendingRequests.forEach { request ->
                    OwnerRequestCard(
                        request = request,
                        onDetails = { requestToDetails = request },
                        onAccept = { onAcceptApplication(request.applicationId) },
                        onReject = { onRejectApplication(request.applicationId) }
                    )
                }
            }

            if (acceptedRequests.isNotEmpty()) {
                SectionLabel("Servicios agendados")
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    acceptedRequests.forEach { request ->
                        ScheduledCaregiverRow(
                            request = request,
                            onCancel = { onCancelService(request) },
                            onRate = { requestToRate = request; ratingScore = 5f; ratingComment = "" },
                            onClick = { requestToDetails = request }
                        )
                    }
                }
            }
        }
    }

    requestToDetails?.let { request ->
        CaregiverServiceDetailsDialog(
            request = request,
            onDismiss = { requestToDetails = null }
        )
    }

    requestToRate?.let { request ->
        AlertDialog(
            onDismissRequest = { requestToRate = null },
            title = { Text("Calificar dueño") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("${ratingScore.roundToInt()} estrellas"); Slider(value = ratingScore, onValueChange = { ratingScore = it }, valueRange = 1f..5f, steps = 3); OutlinedTextField(value = ratingComment, onValueChange = { ratingComment = it }, label = { Text("Comentario opcional") }, minLines = 2) } },
            confirmButton = { TextButton(onClick = { onCompleteAndRate(request, ratingScore.toDouble(), ratingComment); requestToRate = null }) { Text("Guardar") } },
            dismissButton = { TextButton(onClick = { requestToRate = null }) { Text("Cancelar") } },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable private fun SectionLabel(text: String) { Text(text, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onBackground) }
@Composable private fun EmptyCaregiverCard(text: String) { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(16.dp)) { Text(text, modifier = Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) } }

@Composable
private fun CommitmentCard(commitment: ServiceApplicationDetails?) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(82.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Pets, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp)) } }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(commitment?.let { "${it.serviceTypeName ?: it.requestTitle} con ${it.petName ?: "mascota"}" } ?: "Sin compromisos próximos", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(10.dp)); Text(commitment?.requestedDate ?: "Cuando aceptes una solicitud aparecerá aquí") }
                commitment?.startTime?.let { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(10.dp)); Text(it) } }
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
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(64.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(38.dp)) } }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(request.ownerName ?: "Dueño", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("${request.serviceTypeName ?: request.requestTitle} · ${request.petNames ?: request.petName ?: "Mascota"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        Text("Fecha: ${request.requestedDate ?: "--"}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        Text("Hora: ${request.startTime ?: "--"}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                }
            }
            OutlinedButton(
                onClick = onDetails,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Ver detalles", fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onAccept, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text("Aceptar", fontWeight = FontWeight.Bold) }
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text("Rechazar", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun ScheduledCaregiverRow(
    request: ServiceApplicationDetails,
    onCancel: () -> Unit,
    onRate: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(58.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp)) } }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(request.ownerName ?: "Dueño", fontWeight = FontWeight.Bold)
                    Text(request.petNames ?: request.petName ?: "Mascota", fontWeight = FontWeight.Medium)
                    Text("${request.serviceTypeName ?: request.requestTitle} • ${request.requestedDate ?: "--"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Ver detalles", fontWeight = FontWeight.Bold) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onCancel, enabled = canCancelService(request.requestedDate, request.startTime), modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Default.Close, null); Spacer(Modifier.width(6.dp)); Text("Cancelar") }
                OutlinedButton(onClick = onRate, enabled = canRateService(request.requestedDate, request.startTime, request.endTime), modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Default.StarBorder, null); Spacer(Modifier.width(6.dp)); Text("Calificar") }
            }
        }
    }
}


private data class CaregiverDetailField(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val isError: Boolean = false
)

@Composable
private fun CaregiverServiceDetailsDialog(
    request: ServiceApplicationDetails,
    onDismiss: () -> Unit
) {
    val title = request.serviceTypeName ?: request.requestTitle
    val fields = caregiverDetailFields(request)
    val notes = request.requestDescription
        ?.lineSequence()
        ?.map { it.trim() }
        ?.filter { it.isNotBlank() && !it.contains(":") }
        ?.joinToString("\n")
        .orEmpty()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(78.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                caregiverServiceIcon(title),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(18.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = .12f)
                        ) {
                            Text(
                                text = if (request.applicationStatus == ApplicationStatus.ACCEPTED) "Aceptado" else "Pendiente",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        fields.forEachIndexed { index, field ->
                            CaregiverDetailFieldRow(field)
                            if (index != fields.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 52.dp, top = 10.dp, bottom = 10.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }

                if (notes.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Notes, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Notas", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                                Text(notes, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cerrar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CaregiverDetailFieldRow(field: CaregiverDetailField) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = if (field.isError) MaterialTheme.colorScheme.error.copy(alpha = .12f) else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    field.icon,
                    contentDescription = null,
                    tint = if (field.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(field.label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            Text(field.value, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun caregiverDetailFields(request: ServiceApplicationDetails): List<CaregiverDetailField> {
    val descriptionDetails = parseCaregiverDescriptionDetails(request.requestDescription)
    return buildList {
        add(CaregiverDetailField("Dueño", request.ownerName ?: "Dueño", Icons.Default.Person))
        add(CaregiverDetailField("Mascota", request.petNames?.takeIf { it.isNotBlank() } ?: request.petName ?: "Mascota", Icons.Default.Pets))
        request.petBreed?.takeIf { it.isNotBlank() }?.let { add(CaregiverDetailField("Raza", it, Icons.Default.Badge)) }
        request.petSize?.takeIf { it.isNotBlank() }?.let { add(CaregiverDetailField("Tamaño", it, Icons.Default.Scale)) }
        add(CaregiverDetailField("Fecha", request.requestedDate ?: "Sin fecha", Icons.Default.CalendarToday))
        request.startTime?.takeIf { it.isNotBlank() }?.let { add(CaregiverDetailField("Hora inicio", it, Icons.Default.AccessTime)) }
        request.endTime?.takeIf { it.isNotBlank() }?.let { add(CaregiverDetailField("Hora fin", it, Icons.Default.Schedule)) }
        descriptionDetails["Ubicación"]?.let { add(CaregiverDetailField("Ubicación", it, Icons.Default.LocationOn)) }
        descriptionDetails["Dirección de recogida"]?.let { add(CaregiverDetailField("Recogida", it, Icons.Default.LocationOn)) }
        descriptionDetails["Dirección de destino"]?.let { add(CaregiverDetailField("Destino", it, Icons.Default.Place)) }
        descriptionDetails["Precio"]?.let { add(CaregiverDetailField("Precio", it, Icons.Default.Payments)) }
        request.ownerEmail?.takeIf { it.isNotBlank() }?.let { add(CaregiverDetailField("Email", it, Icons.Default.Email)) }
        request.ownerPhone?.takeIf { it.isNotBlank() }?.let { add(CaregiverDetailField("Teléfono", it, Icons.Default.Phone)) }
    }
}

private fun parseCaregiverDescriptionDetails(description: String?): Map<String, String> =
    description.orEmpty()
        .lineSequence()
        .map { it.trim() }
        .filter { it.contains(":") }
        .mapNotNull { line ->
            val label = line.substringBefore(":").trim()
            val value = line.substringAfter(":").trim()
            if (label.isBlank() || value.isBlank()) null else label to value
        }
        .toMap()

private fun caregiverServiceIcon(serviceName: String): ImageVector = when (serviceName) {
    "Alojamiento" -> Icons.Default.NightShelter
    "Guardería" -> Icons.Default.WbSunny
    "Paseo" -> Icons.Default.DirectionsWalk
    "Taxi" -> Icons.Default.LocalTaxi
    "Peluquería" -> Icons.Default.ContentCut
    else -> Icons.Default.Pets
}

private const val CANCELLATION_WINDOW_MS_UI = 3L * 60L * 60L * 1000L
private const val ONE_HOUR_MS_UI = 60L * 60L * 1000L

private fun canCancelService(date: String?, startTime: String?): Boolean {
    val startMillis = parseServiceDateTime(date, startTime) ?: return true
    return System.currentTimeMillis() <= startMillis - CANCELLATION_WINDOW_MS_UI
}

private fun canRateService(date: String?, startTime: String?, endTime: String?): Boolean {
    val startMillis = parseServiceDateTime(date, startTime) ?: return false
    val endMillis = parseServiceDateTime(date, endTime) ?: (startMillis + ONE_HOUR_MS_UI)
    return System.currentTimeMillis() >= endMillis
}

private fun parseServiceDateTime(date: String?, time: String?): Long? {
    if (date.isNullOrBlank()) return null
    return try {
        val hasTime = !time.isNullOrBlank()
        val pattern = if (hasTime) "dd/MM/yyyy HH:mm" else "dd/MM/yyyy"
        val text = if (hasTime) "$date $time" else date
        java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault()).apply { isLenient = false }.parse(text)?.time
    } catch (e: Exception) {
        null
    }
}