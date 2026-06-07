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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.data.local.entity.ApplicationStatus
import com.proyectopoo.petcareapp.data.local.entity.PetEntity
import com.proyectopoo.petcareapp.data.local.relation.ServiceApplicationDetails
import com.proyectopoo.petcareapp.data.local.relation.ServiceRequestDetails

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
    ownerId: Int
) {

    val scrollState = rememberScrollState()
    var selectedDogIndex by remember { mutableStateOf(0) }
    var petToDelete by remember { mutableStateOf<PetEntity?>(null) }
    var showWelcome by remember { mutableStateOf(true) }   // Banner dismissible

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

            if (showWelcome) {
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
                                "Bienvenido",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                "¿Qué necesita tu mascota hoy?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            )
                        }
                        IconButton(onClick = { showWelcome = false }) {
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

                // ================= PET CARD (SIN FOTO, SOLO ÍCONO) =================
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Row(verticalAlignment = Alignment.CenterVertically) {

                            // Ícono de mascota (estático)
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

                // ================= DIÁLOGO ELIMINAR =================
                if (petToDelete != null) {
                    AlertDialog(
                        onDismissRequest = { petToDelete = null },
                        title = { Text("Eliminar mascota") },
                        text = { Text("¿Estás seguro de que deseas eliminar a ${petToDelete?.name}?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    petToDelete?.let(onDeletePet)
                                    petToDelete = null
                                    selectedDogIndex = selectedDogIndex.coerceAtMost((dogs.size - 2).coerceAtLeast(0))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) { Text("Eliminar") }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { petToDelete = null }) { Text("Cancelar") }
                        }
                    )
                }

                // ================= ÚLTIMOS SERVICIOS SOLICITADOS =================
                Text("Últimos servicios solicitados", style = MaterialTheme.typography.titleLarge)

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
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        recentRequests.forEach { request ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Assignment, null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.width(12.dp))
                                        Text(request.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                        AssistChip(onClick = {}, label = { Text(request.status.name) })
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        listOfNotNull(request.petName, request.serviceTypeName, request.requestedDate)
                                            .joinToString(" • "),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                // ================= CUIDADORES INTERESADOS =================
                Text("Cuidadores interesados", style = MaterialTheme.typography.titleLarge)

                val pendingApplications = caregiverApplications.filter { it.applicationStatus == ApplicationStatus.PENDING }

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
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        pendingApplications.forEach { application ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(application.caregiverName ?: "Cuidador", style = MaterialTheme.typography.titleMedium)
                                            Text(
                                                "Quiere trabajar en: ${application.requestTitle}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Button(
                                            onClick = { onAcceptApplication(application.applicationId) },
                                            modifier = Modifier.weight(1f)
                                        ) { Text("Aceptar") }
                                        OutlinedButton(
                                            onClick = { onRejectApplication(application.applicationId) },
                                            modifier = Modifier.weight(1f)
                                        ) { Text("Rechazar") }
                                    }
                                }
                            }
                        }
                    }
                }


                Text("Servicios disponibles", style = MaterialTheme.typography.titleLarge)

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
                                            style = MaterialTheme.typography.titleSmall
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
}