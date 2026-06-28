package com.proyectopoo.petcareapp.ui.screen.caregiver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestStatus
import com.proyectopoo.petcareapp.data.local.relation.ServiceRequestDetails
import com.proyectopoo.petcareapp.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverFeedScreen(
    requests: List<ServiceRequestDetails>,
    onGoToOwnerProfile: (Int) -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val colorScheme = MaterialTheme.colorScheme

    val locationState by viewModel.locationSearchState.collectAsState()
    var showLocationResults by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var serviciosCercanos by remember { mutableStateOf<List<ServiceRequestDetails>>(emptyList()) }
    var mostrarCercanos by remember { mutableStateOf(false) }

    val tiposServicio = listOf(
        "Todos", "Alojamiento", "Guardería", "Paseo", "Taxi", "Peluquería", "Visitante"
    )

    var expanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Todos") }

    LaunchedEffect(locationState.selectedLocation) {
        if (locationState.selectedLocation != null) {
            showLocationResults = false
            isLoading = true

            delay(1000) // Simulación de carga

            serviciosCercanos = requests
            mostrarCercanos = true
            isLoading = false
        }
    }

    val solicitudesBase = remember(requests) {
        requests.ifEmpty {
            listOf(
                ServiceRequestDetails(
                    serviceRequestId = 101,
                    ownerId = 1,
                    petId = 1,
                    serviceTypeId = 3,
                    title = "Paseo vespertino para Max",
                    description = "Max necesita un paseo de 45 minutos por la tarde. Es muy amigable con otros perros, pero se emociona un poco al ver gatos.",
                    requestedDate = "Hoy · 4:30 PM",
                    status = ServiceRequestStatus.PENDING,
                    petName = "Max",
                    petBreed = "Golden Retriever",
                    petSize = "L (20-40 kg)",
                    serviceTypeName = "Paseo",
                    ownerName = "Carlos Mendoza",
                    ownerPhone = "+505 8888-1234", // ✅ Ahora es String
                    ownerEmail = "carlos.mendoza@email.com" // ✅ Ahora es String
                ),
                ServiceRequestDetails(
                    serviceRequestId = 102,
                    ownerId = 2,
                    petId = 2,
                    serviceTypeId = 1,
                    title = "Cuidado de fin de semana para Luna",
                    description = "Busco un cuidador responsable para Luna. Necesita alojamiento desde el sábado en la mañana hasta el domingo por la tarde. Es muy tranquila.",
                    requestedDate = "Sábado · 8:00 AM",
                    status = ServiceRequestStatus.PENDING,
                    petName = "Luna",
                    petBreed = "Siberian Husky",
                    petSize = "M (10-20 kg)",
                    serviceTypeName = "Alojamiento",
                    ownerName = "Andrea Espinoza",
                    ownerPhone = "+505 7777-5678", // ✅ Ahora es String
                    ownerEmail = "andrea.es@email.com" // ✅ Ahora es String
                ),
                ServiceRequestDetails(
                    serviceRequestId = 103,
                    ownerId = 3,
                    petId = 3,
                    serviceTypeId = 5,
                    title = "Baño y corte de pelo urgente",
                    description = "Grooming completo para mi perrita consentida. Requiere corte de uñas, limpieza de oídos y un corte de pelo estilo cachorro.",
                    requestedDate = "Mañana · 10:00 AM",
                    status = ServiceRequestStatus.PENDING,
                    petName = "Bella",
                    petBreed = "Poodle",
                    petSize = "S (5-10 kg)",
                    serviceTypeName = "Peluquería",
                    ownerName = "Marcela Rostrán",
                    ownerPhone = "+505 8444-9012",
                    ownerEmail = "marce.rostran@email.com"
                )
            )
        }
    }

    val serviciosAMostrar = if (mostrarCercanos && serviciosCercanos.isNotEmpty()) {
        serviciosCercanos
    } else if (mostrarCercanos && serviciosCercanos.isEmpty()) {
        emptyList()
    } else {
        if (selectedFilter == "Todos") {
            solicitudesBase
        } else {
            solicitudesBase.filter {
                it.serviceTypeName == selectedFilter
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Solicitudes disponibles",
                        color = colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorScheme.primary
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // --- BÚSQUEDA POR UBICACIÓN ---
            Text(
                text = "📍 Buscar servicios cerca de ti",
                style = MaterialTheme.typography.titleSmall,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = locationState.query,
                onValueChange = {
                    viewModel.updateSearchQuery(it)
                    showLocationResults = it.isNotEmpty()
                    if (it.isEmpty()) {
                        mostrarCercanos = false
                        serviciosCercanos = emptyList()
                    }
                },
                label = { Text("Tu ubicación") },
                placeholder = { Text("Ej: Tu dirección actual") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface,
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outline
                ),
                trailingIcon = {
                    if (locationState.query.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.clearSearchResults()
                            showLocationResults = false
                            mostrarCercanos = false
                            serviciosCercanos = emptyList()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    } else {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                }
            )

            // Resultados de búsqueda de ubicación
            if (showLocationResults && locationState.results.isNotEmpty() && !locationState.isLoading) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 150.dp)
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surfaceVariant
                    )
                ) {
                    LazyColumn {
                        items(locationState.results) { result ->
                            ListItem(
                                headlineContent = {
                                    Text(
                                        result.display_name,
                                        maxLines = 2,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        text = "📍 ${result.lat}, ${result.lon}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                },
                                leadingContent = {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = colorScheme.primary
                                    )
                                },
                                modifier = Modifier.clickable {
                                    viewModel.selectLocation(result)
                                    showLocationResults = false
                                }
                            )
                            Divider()
                        }
                    }
                }
            }

            if (locationState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }

            if (locationState.selectedLocation != null && !showLocationResults) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Buscando cerca de: ${locationState.selectedLocation!!.display_name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (mostrarCercanos && serviciosCercanos.isEmpty() && !isLoading) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No hay servicios disponibles cerca de esta ubicación",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = "Prueba con otra ubicación o amplía el radio de búsqueda",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!mostrarCercanos || serviciosCercanos.isEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedFilter,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de servicio") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colorScheme.surface,
                            unfocusedContainerColor = colorScheme.surface,
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        tiposServicio.forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text(tipo, color = Color.Black) },
                                onClick = {
                                    selectedFilter = tipo
                                    expanded = false
                                    mostrarCercanos = false
                                    serviciosCercanos = emptyList()
                                    viewModel.clearSearchResults()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            } else {
                TextButton(
                    onClick = {
                        mostrarCercanos = false
                        serviciosCercanos = emptyList()
                        viewModel.clearSearchResults()
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver todas las solicitudes")
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Buscando servicios cercanos...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (!isLoading) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (mostrarCercanos && serviciosCercanos.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = colorScheme.secondaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Se encontraron ${serviciosCercanos.size} servicios cerca de ti",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    items(serviciosAMostrar) { servicio ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 2.dp,
                                    color = colorScheme.outline,
                                    shape = RoundedCornerShape(22.dp)
                                ),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = listOfNotNull(
                                            servicio.petName,
                                            servicio.petBreed,
                                            servicio.petSize?.let { "Tamaño $it" }
                                        ).joinToString(" · ").ifBlank { servicio.title },
                                        color = colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )

                                    if (mostrarCercanos) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        AssistChip(
                                            onClick = { },
                                            label = {
                                                Text(
                                                    "Cerca",
                                                    fontSize = 10.sp
                                                )
                                            },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = colorScheme.primary,
                                                labelColor = colorScheme.onPrimary
                                            ),
                                            modifier = Modifier.height(24.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Dueño: ${servicio.ownerName ?: "Sin nombre"}",
                                    color = colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                IntentosChip(servicio)

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "Hora",
                                        tint = colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = servicio.requestedDate ?: "Fecha por coordinar",
                                        color = colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = servicio.description ?: servicio.title,
                                    color = colorScheme.onSurfaceVariant,
                                    fontSize = 15.sp
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "Contacto: ${servicio.ownerPhone ?: "No disponible"}",
                                    color = colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    text = "Email: ${servicio.ownerEmail ?: "No disponible"}",
                                    color = colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                OutlinedButton(
                                    onClick = { onGoToOwnerProfile(servicio.ownerId) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, colorScheme.outline),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = colorScheme.primary
                                    )
                                ) {
                                    Text("Ver perfil completo", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun IntentosChip(servicio: ServiceRequestDetails) {
    AssistChip(
        onClick = { },
        label = { Text(servicio.serviceTypeName ?: "Servicio") },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            labelColor = MaterialTheme.colorScheme.onSecondary
        )
    )
}