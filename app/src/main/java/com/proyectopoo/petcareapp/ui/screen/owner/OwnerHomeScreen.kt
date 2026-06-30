package com.proyectopoo.petcareapp.ui.screen.owner

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.proyectopoo.petcareapp.data.local.entity.ApplicationStatus
import com.proyectopoo.petcareapp.data.local.entity.PetEntity
import com.proyectopoo.petcareapp.data.local.relation.ServiceApplicationDetails
import com.proyectopoo.petcareapp.data.local.relation.ServiceRequestDetails
import com.proyectopoo.petcareapp.ui.components.StarRatingInput

@Composable
fun OwnerHomeScreen(
    dogs: List<PetEntity>,
    recentRequests: List<ServiceRequestDetails>,
    caregiverApplications: List<ServiceApplicationDetails>,
    onGoToCreate: (String) -> Unit,
    onEditPets: (PetEntity) -> Unit,
    onDeletePet: (PetEntity) -> Unit,
    onAddDog: () -> Unit,
    onGoToFeed: () -> Unit,
    onGoToOwnerProfile: () -> Unit,
    onAcceptApplication: (Int) -> Unit,
    onRejectApplication: (Int) -> Unit,
    onCompleteAndRate: (ServiceApplicationDetails, Double, String) -> Unit,
    onCancelService: (ServiceApplicationDetails) -> Unit = {},
    ownerId: Int
) {
    // Estados locales de la pantalla: dialogos, mascota seleccionada y datos de calificacion.
    val scrollState = rememberScrollState()
    var selectedDogIndex by remember { mutableStateOf(0) }
    var petToDelete by remember { mutableStateOf<PetEntity?>(null) }
    var showHeader by remember { mutableStateOf(true) }
    var applicationToRate by remember { mutableStateOf<ServiceApplicationDetails?>(null) }
    var requestToDetail by remember { mutableStateOf<ServiceRequestDetails?>(null) }
    var applicationToDetail by remember { mutableStateOf<ServiceApplicationDetails?>(null) }
    var ratingScore by remember { mutableStateOf(5f) }
    var ratingComment by remember { mutableStateOf("") }

    val safeIndex = if (dogs.isEmpty()) 0 else selectedDogIndex.coerceIn(0, dogs.lastIndex)
    val currentDog = dogs.getOrNull(safeIndex)
    val visibleRecentRequests = recentRequests.take(3)

    val pendingApplications = caregiverApplications.filter { it.applicationStatus == ApplicationStatus.PENDING }
    val acceptedApplications = caregiverApplications.filter { it.applicationStatus == ApplicationStatus.ACCEPTED }
    val doneByCaregiverApplications = caregiverApplications.filter { it.applicationStatus == ApplicationStatus.DONE_BY_CAREGIVER }

    // Servicios que el dueno puede solicitar desde el inicio.
    val services = listOf(
        "Alojamiento" to Icons.Default.Home,
        "Guardería" to Icons.Default.WbSunny,
        "Paseo" to Icons.Default.DirectionsWalk,
        "Taxi" to Icons.Default.LocalTaxi,
        "Peluquería" to Icons.Default.ContentCut,
        "Visitante" to Icons.Default.HomeRepairService
    )

    val serviceDescriptions = mapOf(
        "Alojamiento" to "Espacios 24/7",
        "Guardería" to "Cuidado de día",
        "Paseo" to "Saca a tu mascota",
        "Taxi" to "Traslados seguros",
        "Peluquería" to "Grooming",
        "Visitante" to "Visitas a domicilio"
    )

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
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
                            Text(
                                text = "Sección de dueño",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = "¿Qué necesita tu mascota hoy?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                            )
                        }
                        IconButton(onClick = { showHeader = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // La pantalla prioriza mascotas, ultimos servicios y acciones pendientes.
                PetCarouselCard(
                    currentDog = currentDog,
                    safeIndex = safeIndex,
                    dogCount = dogs.size,
                    onPrevious = { if (selectedDogIndex > 0) selectedDogIndex-- },
                    onNext = { if (selectedDogIndex < dogs.lastIndex) selectedDogIndex++ },
                    onEdit = { currentDog?.let(onEditPets) },
                    onDelete = { currentDog?.let { petToDelete = it } }
                )

                OutlinedButton(onClick = onAddDog, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Agregar mascota")
                }

                SectionTitle(
                    title = "Últimos servicios solicitados",
                    actionText = if (recentRequests.size > 3) "Mostrando 3" else null
                )
                if (recentRequests.isEmpty()) {
                    EmptyStateCard("Aún no has solicitado servicios.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        visibleRecentRequests.forEach { request ->
                            CompactRequestCard(request = request, onClick = { requestToDetail = request })
                        }
                    }
                }

                SectionTitle("Cuidadores interesados")
                if (pendingApplications.isEmpty()) {
                    EmptyStateCard("Aún no hay cuidadores interesados.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        pendingApplications.forEach { application ->
                            InterestedCaregiverCard(
                                application = application,
                                onDetails = { applicationToDetail = application },
                                onAccept = { onAcceptApplication(application.applicationId) },
                                onReject = { onRejectApplication(application.applicationId) }
                            )
                        }
                    }
                }

                if (acceptedApplications.isNotEmpty()) {
                    SectionTitle("Servicios activos")
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        acceptedApplications.forEach { application ->
                            ServiceApplicationCard(
                                application = application,
                                message = "Servicio aceptado y en curso.",
                                actionText = "Cancelar",
                                onAction = { onCancelService(application) },
                                onDetails = { applicationToDetail = application },
                                dangerAction = true
                            )
                        }
                    }
                }

                if (doneByCaregiverApplications.isNotEmpty()) {
                    SectionTitle("Pendientes de calificación")
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        doneByCaregiverApplications.forEach { application ->
                            ServiceApplicationCard(
                                application = application,
                                message = "El cuidador marcó este servicio como realizado.",
                                actionText = "Confirmar y calificar",
                                onAction = {
                                    applicationToRate = application
                                    ratingScore = 5f
                                    ratingComment = ""
                                },
                                onDetails = { applicationToDetail = application }
                            )
                        }
                    }
                }

                SectionTitle("Servicios disponibles")
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    services.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { (name, icon) ->
                                ServiceTypeCard(
                                    name = name,
                                    description = serviceDescriptions[name].orEmpty(),
                                    icon = icon,
                                    onClick = { onGoToCreate(name) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "¿Buscas cuidadores?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                "Encuentra servicios disponibles cerca de ti",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            )
                        }
                        Button(
                            onClick = onGoToFeed,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimary)
                        ) {
                            Text("Explorar", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    petToDelete?.let { pet ->
        AlertDialog(
            onDismissRequest = { petToDelete = null },
            title = { Text("Eliminar mascota") },
            text = { Text("¿Estás seguro de que deseas eliminar a ${pet.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePet(pet)
                        petToDelete = null
                        selectedDogIndex = 0.coerceAtMost((dogs.size - 1).coerceAtLeast(0))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { petToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    requestToDetail?.let { request ->
        RequestDetailsDialog(request = request, onDismiss = { requestToDetail = null })
    }

    applicationToDetail?.let { application ->
        ApplicationDetailsDialog(application = application, onDismiss = { applicationToDetail = null })
    }

    applicationToRate?.let { application ->
        AlertDialog(
            onDismissRequest = { applicationToRate = null },
            title = { Text("Calificar cuidador") },
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
                        onCompleteAndRate(application, ratingScore.toDouble(), ratingComment)
                        applicationToRate = null
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { applicationToRate = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun PetCarouselCard(
    currentDog: PetEntity?,
    safeIndex: Int,
    dogCount: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(22.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(22.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(62.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(currentDog?.name ?: "Agrega tu mascota", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        currentDog?.let { listOfNotNull(it.breed, it.size).joinToString(" · ") }.orEmpty().ifBlank { "Sin información" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onEdit, enabled = currentDog != null) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                IconButton(onClick = onDelete, enabled = currentDog != null) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error) }
            }
            if (dogCount > 1) {
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrevious) { Icon(Icons.Default.ArrowBack, contentDescription = "Anterior") }
                    Text("${safeIndex + 1}/$dogCount", style = MaterialTheme.typography.labelLarge)
                    IconButton(onClick = onNext) { Icon(Icons.Default.ArrowForward, contentDescription = "Siguiente") }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, actionText: String? = null) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        actionText?.let {
            Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun EmptyStateCard(text: String) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(16.dp)) {
        Text(text, modifier = Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CompactRequestCard(request: ServiceRequestDetails, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBubble(icon = serviceIcon(request.serviceTypeName ?: request.title))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(request.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(
                    listOfNotNull(request.petNames?.takeIf { it.isNotBlank() } ?: request.petName, request.requestedDate).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(status = request.status)
        }
    }
}

@Composable
private fun InterestedCaregiverCard(
    application: ServiceApplicationDetails,
    onDetails: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        onClick = onDetails,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBubble(Icons.Default.Person)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(application.caregiverName ?: "Cuidador", fontWeight = FontWeight.Bold)
                    Text("Interesado en: ${application.serviceTypeName ?: application.requestTitle}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusChip(status = application.applicationStatus)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onAccept, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text("Aceptar") }
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text("Rechazar") }
            }
        }
    }
}

@Composable
private fun ServiceApplicationCard(
    application: ServiceApplicationDetails,
    message: String,
    actionText: String,
    onAction: () -> Unit,
    onDetails: () -> Unit,
    dangerAction: Boolean = false
) {
    Card(onClick = onDetails, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(application.caregiverName ?: "Cuidador", fontWeight = FontWeight.Bold)
                Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            if (dangerAction) {
                OutlinedButton(onClick = onAction, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text(actionText) }
            } else {
                Button(onClick = onAction) { Text(actionText) }
            }
        }
    }
}

@Composable
private fun CompletedServiceCard(application: ServiceApplicationDetails, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBubble(Icons.Default.CheckCircle)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(application.serviceTypeName ?: application.requestTitle, fontWeight = FontWeight.Bold)
                Text("Cuidador: ${application.caregiverName ?: "Cuidador"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatusChip(status = application.applicationStatus)
        }
    }
}

@Composable
private fun ServiceTypeCard(name: String, description: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.height(132.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Start)
        }
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
private fun RequestDetailsDialog(request: ServiceRequestDetails, onDismiss: () -> Unit) {
    DetailsDialog(title = request.title, onDismiss = onDismiss) {
        DetailRow("Mascota", request.petNames ?: request.petName)
        DetailRow("Servicio", request.serviceTypeName)
        DetailRow("Fecha", request.requestedDate)
        DetailRow("Hora", listOfNotNull(request.startTime, request.endTime).joinToString(" - ").ifBlank { null })
        DetailRow("Estado", statusText(request.status.name))
        DetailRow("Descripción", request.description)
    }
}

@Composable
private fun ApplicationDetailsDialog(application: ServiceApplicationDetails, onDismiss: () -> Unit) {
    DetailsDialog(title = application.serviceTypeName ?: application.requestTitle, onDismiss = onDismiss) {
        DetailRow("Cuidador", application.caregiverName)
        DetailRow("Mascota", application.petNames ?: application.petName)
        DetailRow("Fecha", application.requestedDate)
        DetailRow("Hora", listOfNotNull(application.startTime, application.endTime).joinToString(" - ").ifBlank { null })
        DetailRow("Estado", statusText(application.applicationStatus.name))
        DetailRow("Descripción", application.requestDescription)
    }
}

@Composable
private fun DetailsDialog(title: String, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Cerrar") }
                }
                content()
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
