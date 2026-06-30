package com.proyectopoo.petcareapp.ui.screen.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

    val scrollState = rememberScrollState()
    var selectedDogIndex by remember { mutableStateOf(0) }
    var petToDelete by remember { mutableStateOf<PetEntity?>(null) }
    var showHeader by remember { mutableStateOf(true) }
    var applicationToRate by remember { mutableStateOf<ServiceApplicationDetails?>(null) }
    var ratingScore by remember { mutableStateOf(5f) }
    var ratingComment by remember { mutableStateOf("") }


    val safeIndex = if (dogs.isEmpty()) 0 else selectedDogIndex.coerceIn(0, dogs.lastIndex)
    val currentDog = dogs.getOrNull(safeIndex)

    val services = listOf(
        "Alojamiento" to Icons.Default.Home,
        "Guardería" to Icons.Default.WbSunny,
        "Paseo" to Icons.Default.DirectionsWalk,
        "Taxi" to Icons.Default.LocalTaxi,
        "Peluquería" to Icons.Default.ContentCut,
        "Visitante" to Icons.Default.HomeRepairService
    )

    val serviceDescriptions = mapOf(
        "Alojamiento" to "Estancia 24h o más en casa del cuidador",
        "Guardería" to "Cuidado de 8am a 8pm en casa del cuidador",
        "Paseo" to "El cuidador saca a pasear a tu perro",
        "Taxi" to "Traslado entre ubicaciones",
        "Peluquería" to "Servicio de grooming especializado",
        "Visitante" to "El cuidador va a tu casa a atenderlo"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {

            // Header Sección (dismissible)
            if (showHeader) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sección de dueño",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = "¿Qué necesita tu mascota hoy?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
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

                // Tarjeta de Información de Mascota Activa
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(68.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Pets,
                                        contentDescription = "Mascota",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentDog?.name ?: "Agrega tu mascota",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = currentDog?.let { "${it.breed} · ${it.size}" } ?: "Sin información",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(onClick = { currentDog?.let(onEditPets) }, enabled = currentDog != null) {
                                Icon(Icons.Default.Edit, "Editar")
                            }

                            IconButton(onClick = { currentDog?.let { petToDelete = it } }, enabled = currentDog != null) {
                                Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        if (dogs.size > 1) {
                            Spacer(Modifier.height(12.dp))
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
                        containerColor = Color.White,
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

                Text(
                    "Últimos servicios solicitados",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

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
                        recentRequests.forEach { request ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(2.dp, RoundedCornerShape(24.dp))
                                    .clip(RoundedCornerShape(24.dp)),
                                shape = RoundedCornerShape(24.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            val serviceIcon = when (request.serviceTypeName?.lowercase()) {
                                                "paseo" -> Icons.Default.DirectionsWalk
                                                "alojamiento" -> Icons.Default.Home
                                                "guardería" -> Icons.Default.WbSunny
                                                "taxi" -> Icons.Default.LocalTaxi
                                                "peluquería" -> Icons.Default.ContentCut
                                                "visitante" -> Icons.Default.HomeRepairService
                                                else -> Icons.Default.Assignment
                                            }
                                            Icon(
                                                serviceIcon,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(12.dp)
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                request.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                listOfNotNull(
                                                    request.petNames?.takeIf { it.isNotBlank() } ?: request.petName,
                                                    request.serviceTypeName
                                                ).joinToString(" • "),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        StatusChip(status = request.status)
                                    }

                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )

                                    Text(
                                        text = " ${request.requestedDate ?: "Sin fecha"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    "Cuidadores interesados",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                val pendingApplications = caregiverApplications.filter {
                    it.applicationStatus == ApplicationStatus.PENDING
                }
                val acceptedApplications = caregiverApplications.filter {
                    it.applicationStatus == ApplicationStatus.ACCEPTED
                }
                val doneByCaregiverApplications = caregiverApplications.filter {
                    it.applicationStatus == ApplicationStatus.DONE_BY_CAREGIVER
                }
                val completedApplications = caregiverApplications.filter {
                    it.applicationStatus == ApplicationStatus.COMPLETED
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
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(2.dp, RoundedCornerShape(24.dp))
                                    .clip(RoundedCornerShape(24.dp)),
                                shape = RoundedCornerShape(24.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Person,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(12.dp)
                                            )
                                        }
                                        Spacer(Modifier.width(16.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                application.caregiverName ?: "Cuidador desconocido",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                "Interesado en: ${application.requestTitle}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        StatusChip(status = application.applicationStatus)
                                    }

                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Button(
                                            onClick = { onAcceptApplication(application.applicationId) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Text("Aceptar", fontWeight = FontWeight.Medium)
                                        }
                                        OutlinedButton(
                                            onClick = { onRejectApplication(application.applicationId) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text("Rechazar", fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (acceptedApplications.isNotEmpty()) {
                    Text(
                        "Servicios activos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        acceptedApplications.forEach { application ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(application.caregiverName ?: "Cuidador", fontWeight = FontWeight.SemiBold)
                                        Text(
                                            application.serviceTypeName ?: application.requestTitle,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { onCancelService(application) },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Text("Cancelar")
                                    }
                                }
                            }
                        }
                    }
                }

                if (doneByCaregiverApplications.isNotEmpty()) {
                    Text(
                        "Pendientes de confirmar",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        doneByCaregiverApplications.forEach { application ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(application.caregiverName ?: "Cuidador", fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "El cuidador marcó este servicio como realizado.",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Button(onClick = {
                                        applicationToRate = application
                                        ratingScore = 5f
                                        ratingComment = ""
                                    }) {
                                        Text("Confirmar y calificar")
                                    }
                                }
                            }
                        }
                    }
                }

                if (completedApplications.isNotEmpty()) {
                    Text(
                        "Historial de servicios",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        completedApplications.forEach { application ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(application.requestTitle, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "Cuidador: ${application.caregiverName ?: "Cuidador"}",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "Finalizado",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
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
                                        .height(150.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            icon,
                                            null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = serviceDescriptions[name] ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Start
                                        )
                                    }
                                }
                            }
                            if (row.size == 1) {
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

    applicationToRate?.let { application ->
        AlertDialog(
            onDismissRequest = { applicationToRate = null },
            title = { Text("Calificar cuidador") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StarRatingInput(
                        value = ratingScore,
                        onValueChange = { ratingScore = it }
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
private fun StatusChip(status: Enum<*>) {
    val (text, color) = when (status.name.uppercase()) {
        "PENDING" -> "Pendiente" to Color(0xFFFF9800)
        "ACCEPTED" -> "Aceptado" to Color(0xFF4CAF50)
        "REJECTED" -> "Rechazado" to Color(0xFFF44336)
        else -> status.name.replaceFirstChar { it.uppercase() } to MaterialTheme.colorScheme.outline
    }

    AssistChip(
        onClick = {},
        label = {
            Text(
                text,
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
