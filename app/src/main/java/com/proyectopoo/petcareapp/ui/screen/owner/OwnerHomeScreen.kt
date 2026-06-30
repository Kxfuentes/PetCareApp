package com.proyectopoo.petcareapp.ui.screen.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.Dialog
import com.proyectopoo.petcareapp.data.local.entity.ApplicationStatus
import com.proyectopoo.petcareapp.data.local.entity.PetEntity
import com.proyectopoo.petcareapp.data.local.relation.ServiceApplicationDetails
import com.proyectopoo.petcareapp.data.local.relation.ServiceRequestDetails
import kotlin.math.roundToInt

@Composable
fun OwnerHomeScreen(
    dogs: List<PetEntity>,
    recentRequests: List<ServiceRequestDetails>,
    caregiverApplications: List<ServiceApplicationDetails>,
    scheduledServices: List<ServiceApplicationDetails>,
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

    val scrollState = rememberScrollState()
    var selectedDogIndex by remember { mutableStateOf(0) }
    var petToDelete by remember { mutableStateOf<PetEntity?>(null) }
    var showHeader by remember { mutableStateOf(true) }
    var applicationToRate by remember { mutableStateOf<ServiceApplicationDetails?>(null) }
    var ratingScore by remember { mutableStateOf(5f) }
    var ratingComment by remember { mutableStateOf("") }
    var showAllRequestedScreen by remember { mutableStateOf(false) }
    var requestToDetail by remember { mutableStateOf<ServiceRequestDetails?>(null) }
    var applicationToDetail by remember { mutableStateOf<ServiceApplicationDetails?>(null) }

    val safeIndex = if (dogs.isEmpty()) 0 else selectedDogIndex.coerceIn(0, dogs.lastIndex)
    val currentDog = dogs.getOrNull(safeIndex)
    val recentRequestLimit = 3
    val visibleRecentRequests = recentRequests.take(recentRequestLimit)

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

    if (showAllRequestedScreen) {
        AllRequestedServicesScreen(
            requests = recentRequests,
            onBack = { showAllRequestedScreen = false },
            onOpenDetails = { requestToDetail = it }
        )

        requestToDetail?.let { request ->
            ServiceRequestDetailsDialog(
                request = request,
                onDismiss = { requestToDetail = null }
            )
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

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
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
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
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(62.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Pets,
                                        contentDescription = "Mascota",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentDog?.name ?: "Agrega tu mascota",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (currentDog != null) {
                                    Text(
                                        text = currentDog.breed ?: "Sin raza",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    currentDog.size?.takeIf { it.isNotBlank() }?.let { size ->
                                        Spacer(Modifier.height(5.dp))
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = formatPetSize(size),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "Sin información",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    IconButton(onClick = { currentDog?.let(onEditPets) }, enabled = currentDog != null) {
                                        Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                                ) {
                                    IconButton(onClick = { currentDog?.let { petToDelete = it } }, enabled = currentDog != null) {
                                        Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }

                        if (dogs.size > 1) {
                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { if (selectedDogIndex > 0) selectedDogIndex-- }) {
                                    Icon(Icons.Default.ArrowBack, null)
                                }
                                Text("${safeIndex + 1}/${dogs.size}")
                                IconButton(onClick = { if (selectedDogIndex < dogs.lastIndex) selectedDogIndex++ }) {
                                    Icon(Icons.Default.ArrowForward, null)
                                }
                            }
                        }
                    }
                }

                OutlinedButton(onClick = onAddDog, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Agregar mascota")
                }

                if (petToDelete != null) {
                    AlertDialog(
                        onDismissRequest = { petToDelete = null },
                        title = { Text("Eliminar mascota") },
                        text = { Text("¿Estás seguro de que deseas eliminar a ${petToDelete?.name}?") },
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = Color.Black,
                        textContentColor = Color.Black,
                        confirmButton = {
                            Button(
                                onClick = {
                                    petToDelete?.let(onDeletePet)
                                    petToDelete = null
                                    // Resetea el índice al primer perro (o 0 si la lista queda vacía)
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Últimos servicios solicitados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (recentRequests.size > recentRequestLimit) {
                        OutlinedButton(
                            onClick = { showAllRequestedScreen = true },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.height(34.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Ver más", style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.width(2.dp))
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                if (recentRequests.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            "Aún no has solicitado servicios.",
                            modifier = Modifier.padding(20.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        visibleRecentRequests.forEach { request ->
                            CompactRequestCard(
                                request = request,
                                onOpenDetails = { requestToDetail = request }
                            )
                        }
                    }
                }

                Text(
                    "Cuidadores interesados",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

    val pendingApplications = caregiverApplications.filter {
        it.applicationStatus == ApplicationStatus.PENDING &&
            it.initiatedBy == com.proyectopoo.petcareapp.data.local.entity.ApplicationInitiator.CAREGIVER
    }
    val acceptedApplications = scheduledServices.filter {
        it.applicationStatus == ApplicationStatus.ACCEPTED
    }

                if (pendingApplications.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            "Aún no hay cuidadores interesados.",
                            modifier = Modifier.padding(20.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        pendingApplications.forEach { application ->
                            InterestedCaregiverCard(
                                application = application,
                                onAccept = { onAcceptApplication(application.applicationId) },
                                onReject = { onRejectApplication(application.applicationId) }
                            )
                        }                    }
                }

                if (acceptedApplications.isNotEmpty()) {
                    Text(
                        "Servicios agendados",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        acceptedApplications.forEach { application ->
                            ScheduledServiceCard(
                                application = application,
                                onCancel = { onCancelService(application) },
                                onRate = {
                                    applicationToRate = application
                                    ratingScore = 5f
                                    ratingComment = ""
                                },
                                onOpenDetails = { applicationToDetail = application }
                            )
                        }
                    }
                }

                Text(
                    "Servicios disponibles",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    services.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { (name, icon) ->
                                Card(
                                    onClick = { onGoToCreate(name) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(118.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            icon,
                                            null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = serviceDescriptions[name] ?: "",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                            repeat(2 - row.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "¿Buscas paseadores?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                "Encuentra los mejores cerca de ti",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            )
                        }
                        Button(
                            onClick = onGoToFeed,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("Explorar", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    requestToDetail?.let { request ->
        ServiceRequestDetailsDialog(
            request = request,
            onDismiss = { requestToDetail = null }
        )
    }

    applicationToDetail?.let { application ->
        ServiceApplicationDetailsDialog(
            application = application,
            onDismiss = { applicationToDetail = null }
        )
    }

    applicationToRate?.let { application ->
        AlertDialog(
            onDismissRequest = { applicationToRate = null },
            title = { Text("Calificar cuidador") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${ratingScore.roundToInt()} estrellas")
                    Slider(
                        value = ratingScore,
                        onValueChange = { ratingScore = it },
                        valueRange = 1f..5f,
                        steps = 3
                    )
                    OutlinedTextField(
                        value = ratingComment,
                        onValueChange = { ratingComment = it },
                        label = { Text("Comentario opcional") },
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onCompleteAndRate(application, ratingScore.toDouble(), ratingComment)
                    applicationToRate = null
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { applicationToRate = null }) { Text("Cancelar") }
            }
        )
    }
}


@Composable
private fun InterestedCaregiverCard(
    application: ServiceApplicationDetails,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(22.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            (application.caregiverName ?: "C").firstOrNull()?.uppercase() ?: "C",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        application.caregiverName ?: "Cuidador desconocido",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Quiere ofrecer sus servicios",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiniInfoLine(Icons.Default.Pets, "Mascota", application.petNames?.takeIf { it.isNotBlank() } ?: application.petName ?: "Tu mascota")
                    MiniInfoLine(Icons.Default.RoomService, "Servicio solicitado", application.serviceTypeName ?: application.requestTitle)
                    application.requestedDate?.let { MiniInfoLine(Icons.Default.CalendarToday, "Fecha", it) }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Aceptar", fontWeight = FontWeight.Medium)
                }
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Rechazar", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun MiniInfoLine(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CompactRequestCard(
    request: ServiceRequestDetails,
    onOpenDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(22.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ServiceIconBubble(serviceName = request.serviceTypeName ?: request.title)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    request.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    request.petNames?.takeIf { it.isNotBlank() } ?: request.petName ?: "Sin mascota",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    request.requestedDate ?: "Sin fecha",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                RequestStatusChip(status = request.status)
                IconButton(onClick = onOpenDetails) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Ver detalles")
                }
            }
        }
    }
}

@Composable
private fun ScheduledServiceCard(
    application: ServiceApplicationDetails,
    onCancel: () -> Unit,
    onRate: () -> Unit,
    onOpenDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ServiceIconBubble(serviceName = application.serviceTypeName ?: application.requestTitle)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        application.serviceTypeName ?: application.requestTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        listOfNotNull(
                            application.petNames?.takeIf { it.isNotBlank() } ?: application.petName,
                            application.caregiverName ?: "Cuidador"
                        ).joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${application.requestedDate ?: "Sin fecha"} ${application.startTime.orEmpty()}".trim(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onOpenDetails) {
                    Text("Ver detalles", style = MaterialTheme.typography.labelMedium)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    enabled = canCancelService(application.requestedDate, application.startTime),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cancelar", style = MaterialTheme.typography.labelMedium)
                }
                Button(
                    onClick = onRate,
                    enabled = canRateService(application.requestedDate, application.startTime, application.endTime),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Calificar", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun AllRequestedServicesScreen(
    requests: List<ServiceRequestDetails>,
    onBack: () -> Unit,
    onOpenDetails: (ServiceRequestDetails) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Todos") }
    val filteredRequests = when (selectedFilter) {
        "Pendientes" -> requests.filter { it.status.name == "PENDING" }
        "Completados" -> requests.filter { it.status.name == "COMPLETED" }
        "Cancelados" -> requests.filter { it.status.name == "CANCELLED" }
        else -> requests
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
                Text(
                    "Todos los servicios solicitados",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Todos", "Pendientes", "Completados", "Cancelados").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, maxLines = 1) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 2.dp, bottom = 16.dp)
            ) {
                items(filteredRequests) { request ->
                    CompactRequestCard(
                        request = request,
                        onOpenDetails = { onOpenDetails(request) }
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun ServiceRequestDetailsDialog(
    request: ServiceRequestDetails,
    onDismiss: () -> Unit
) {
    val status = requestStatusLabelAndColor(request.status.name)
    DetailsCardDialog(
        title = request.serviceTypeName ?: request.title,
        serviceName = request.serviceTypeName ?: request.title,
        statusText = status.first,
        statusColor = status.second,
        fields = requestDetailFields(request),
        notes = parseDescriptionDetails(request.description)["Notas"],
        onDismiss = onDismiss
    )
}

@Composable
private fun ServiceApplicationDetailsDialog(
    application: ServiceApplicationDetails,
    onDismiss: () -> Unit
) {
    DetailsCardDialog(
        title = application.serviceTypeName ?: application.requestTitle,
        serviceName = application.serviceTypeName ?: application.requestTitle,
        statusText = null,
        statusColor = MaterialTheme.colorScheme.primary,
        fields = applicationDetailFields(application),
        notes = parseDescriptionDetails(application.requestDescription)["Notas"],
        onDismiss = onDismiss
    )
}

private data class DetailField(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val accentColor: Color? = null
)

@Composable
private fun DetailsCardDialog(
    title: String,
    serviceName: String,
    statusText: String?,
    statusColor: Color,
    fields: List<DetailField>,
    notes: String?,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(76.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                serviceIconFor(serviceName),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(18.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        statusText?.let {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = statusColor.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.28f))
                            ) {
                                Text(
                                    it,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    color = statusColor,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        fields.forEachIndexed { index, field ->
                            DetailFieldRow(field)
                            if (index != fields.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }

                notes?.takeIf { it.isNotBlank() }?.let {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Article,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Notas",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Cerrar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DetailFieldRow(field: DetailField) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = (field.accentColor ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.1f),
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    field.icon,
                    contentDescription = null,
                    tint = field.accentColor ?: MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                field.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                field.value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


@Composable
private fun ServiceIconBubble(serviceName: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                serviceIconFor(serviceName),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(25.dp)
            )
        }
    }
}

private fun serviceIconFor(serviceName: String): ImageVector = when (serviceName.lowercase()) {
    "paseo" -> Icons.Default.DirectionsWalk
    "alojamiento" -> Icons.Default.Home
    "guardería" -> Icons.Default.WbSunny
    "taxi" -> Icons.Default.LocalTaxi
    "peluquería" -> Icons.Default.ContentCut
    "visitante" -> Icons.Default.HomeRepairService
    else -> Icons.Default.Assignment
}

@Composable
private fun RequestStatusChip(status: Enum<*>) {
    if (status.name.uppercase() == "PENDING") {
        StatusChip(status = status, overrideText = "No coordinado")
    } else {
        StatusChip(status = status)
    }
}

@Composable
private fun StatusChip(status: Enum<*>, overrideText: String? = null) {
    val (statusText, color) = when (status.name.uppercase()) {
        "PENDING" -> "Pendiente" to Color(0xFFFF9800)
        "ACCEPTED" -> "Aceptado" to Color(0xFF4CAF50)
        "REJECTED" -> "Rechazado" to Color(0xFFF44336)
        "CANCELLED" -> "Cancelado" to Color(0xFF795548)
        "COMPLETED" -> "Completado" to Color(0xFF607D8B)
        else -> status.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() } to MaterialTheme.colorScheme.outline
    }

    AssistChip(
        onClick = {},
        label = {
            Text(
                overrideText ?: statusText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            labelColor = Color.Black,
            containerColor = color.copy(alpha = 0.1f)
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = color.copy(alpha = 0.8f),
            borderWidth = 1.dp
        ),
        shape = RoundedCornerShape(20.dp)
    )
}


private fun requestDetailFields(request: ServiceRequestDetails): List<DetailField> {
    val extracted = parseDescriptionDetails(request.description)
    val status = requestStatusLabelAndColor(request.status.name)
    return listOfNotNull(
        DetailField(Icons.Default.Pets, "Mascota", request.petNames?.takeIf { it.isNotBlank() } ?: request.petName ?: "Sin mascota"),
        request.petBreed?.let { DetailField(Icons.Default.Badge, "Raza", it) },
        request.petSize?.let { DetailField(Icons.Default.Scale, "Tamaño", it) },
        DetailField(Icons.Default.Info, "Estado", status.first, status.second),
        DetailField(Icons.Default.CalendarToday, "Fecha", request.requestedDate ?: "Sin fecha"),
        request.startTime?.let { DetailField(Icons.Default.AccessTime, "Hora inicio", it) },
        request.endTime?.let { DetailField(Icons.Default.Schedule, "Hora fin", it) },
        extracted["Ubicación"]?.let { DetailField(Icons.Default.LocationOn, "Ubicación", it) },
        extracted["Precio"]?.let { DetailField(Icons.Default.AttachMoney, "Precio", it) },
        extracted["Precio sugerido"]?.let { DetailField(Icons.Default.AttachMoney, "Precio sugerido", it) },
        extracted["Destino"]?.let { DetailField(Icons.Default.NearMe, "Destino", it) },
        extracted["Tipo de peluquería"]?.let { DetailField(Icons.Default.ContentCut, "Tipo de peluquería", it) }
    )
}

private fun applicationDetailFields(application: ServiceApplicationDetails): List<DetailField> {
    val extracted = parseDescriptionDetails(application.requestDescription)
    return listOfNotNull(
        DetailField(Icons.Default.Pets, "Mascota", application.petNames?.takeIf { it.isNotBlank() } ?: application.petName ?: "Sin mascota"),
        application.petBreed?.let { DetailField(Icons.Default.Badge, "Raza", it) },
        application.petSize?.let { DetailField(Icons.Default.Scale, "Tamaño", it) },
        DetailField(serviceIconFor(application.serviceTypeName ?: application.requestTitle), "Servicio", application.serviceTypeName ?: application.requestTitle),
        DetailField(Icons.Default.Person, "Cuidador", application.caregiverName ?: "Cuidador"),
        application.caregiverEmail?.let { DetailField(Icons.Default.Email, "Email del cuidador", it) },
        application.caregiverPhone?.let { DetailField(Icons.Default.Phone, "Teléfono del cuidador", it) },
        DetailField(Icons.Default.CalendarToday, "Fecha", application.requestedDate ?: "Sin fecha"),
        application.startTime?.let { DetailField(Icons.Default.AccessTime, "Hora inicio", it) },
        application.endTime?.let { DetailField(Icons.Default.Schedule, "Hora fin", it) },
        extracted["Ubicación"]?.let { DetailField(Icons.Default.LocationOn, "Ubicación", it) },
        extracted["Precio"]?.let { DetailField(Icons.Default.AttachMoney, "Precio", it) },
        extracted["Precio sugerido"]?.let { DetailField(Icons.Default.AttachMoney, "Precio sugerido", it) }
    )
}

private fun requestStatusLabelAndColor(statusName: String): Pair<String, Color> = when (statusName.uppercase()) {
    "PENDING" -> "No se ha coordinado" to Color(0xFFFF9800)
    "ACCEPTED" -> "Aceptado" to Color(0xFF4CAF50)
    "REJECTED" -> "Rechazado" to Color(0xFFF44336)
    "CANCELLED" -> "Cancelado" to Color(0xFF795548)
    "COMPLETED" -> "Completado" to Color(0xFF607D8B)
    else -> statusName.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() } to Color(0xFF8A6A55)
}


private fun parseDescriptionDetails(description: String?): Map<String, String> {
    if (description.isNullOrBlank()) return emptyMap()
    val knownLabels = listOf("Ubicación", "Precio", "Precio sugerido", "Destino", "Tipo de peluquería")
    val details = linkedMapOf<String, String>()
    val notes = mutableListOf<String>()

    description.lineSequence().map { it.trim() }.filter { it.isNotBlank() }.forEach { line ->
        val label = knownLabels.firstOrNull { line.startsWith("$it:", ignoreCase = true) }
        if (label != null) {
            line.substringAfter(":").trim().takeIf { it.isNotBlank() }?.let { details[label] = it }
        } else {
            notes += line
        }
    }

    if (notes.isNotEmpty()) details["Notas"] = notes.joinToString("\n")
    return details
}

private fun formatPetSize(size: String): String {
    val clean = size.trim()
    return if (clean.contains("kg", ignoreCase = true) || clean.contains("tamaño", ignoreCase = true)) clean else "Tamaño $clean"
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
        java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault())
            .apply { isLenient = false }
            .parse(text)
            ?.time
    } catch (e: Exception) {
        null
    }
}
