package com.proyectopoo.petcareapp.ui.screen.caregiver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestStatus
import com.proyectopoo.petcareapp.data.local.relation.ServiceRequestDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverFeedScreen(
    requests: List<ServiceRequestDetails>,
    onGoToOwnerProfile: (Int, Int) -> Unit,
    onApplyToRequest: (Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    val tiposServicio = listOf(
        "Todos", "Alojamiento", "Guardería", "Paseo", "Taxi", "Peluquería", "Visitante"
    )

    var expanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Todos") }
    val hasRealRequests = requests.isNotEmpty()

    val solicitudesBase = remember(requests) {
        requests.ifEmpty {
            listOf(
                ServiceRequestDetails(
                    serviceRequestId = 101, ownerId = 1, petId = 1, serviceTypeId = 3,
                    title = "Paseo vespertino para Max",
                    description = "Max necesita un paseo de 45 minutos por la tarde. Es muy amigable con otros perros, pero se emociona un poco al ver gatos.",
                    requestedDate = "Hoy · 4:30 PM",
                    status = ServiceRequestStatus.PENDING,
                    petName = "Max", petBreed = "Golden Retriever",
                    petSize = "L (20-40 kg)",
                    serviceTypeName = "Paseo", ownerName = "Carlos Mendoza",
                    ownerPhone = "+505 8888-1234", ownerEmail = "carlos.mendoza@email.com"
                ),
                ServiceRequestDetails(
                    serviceRequestId = 102, ownerId = 2, petId = 2, serviceTypeId = 1,
                    title = "Cuidado de fin de semana para Luna",
                    description = "Busco un cuidador responsable para Luna. Necesita alojamiento desde el sábado en la mañana hasta el domingo por la tarde. Es muy tranquila.",
                    requestedDate = "Sábado · 8:00 AM",
                    status = ServiceRequestStatus.PENDING,
                    petName = "Luna", petBreed = "Siberian Husky",
                    petSize = "M (10-20 kg)",
                    serviceTypeName = "Alojamiento", ownerName = "Andrea Espinoza",
                    ownerPhone = "+505 7777-5678", ownerEmail = "andrea.es@email.com"
                ),
                ServiceRequestDetails(
                    serviceRequestId = 103, ownerId = 3, petId = 3, serviceTypeId = 5,
                    title = "Baño y corte de pelo urgente",
                    description = "Grooming completo para mi perrita consentida. Requiere corte de uñas, limpieza de oídos y un corte de pelo estilo cachorro.",
                    requestedDate = "Mañana · 10:00 AM",
                    status = ServiceRequestStatus.PENDING,
                    petName = "Bella", petBreed = "Poodle",
                    petSize = "S (5-10 kg)",
                    serviceTypeName = "Peluquería", ownerName = "Marcela Rostrán",
                    ownerPhone = "+505 8444-9012", ownerEmail = "marce.rostran@email.com"
                )
            )
        }
    }

    val serviciosFiltrados = if (selectedFilter == "Todos") {
        solicitudesBase
    } else {
        solicitudesBase.filter {
            it.serviceTypeName == selectedFilter
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
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(serviciosFiltrados) { servicio ->
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
                                text = "Email: ${servicio.ownerEmail ?: "No disponible"}",
                                color = colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            OutlinedButton(
                                onClick = { onGoToOwnerProfile(servicio.ownerId, servicio.serviceRequestId) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, colorScheme.outline),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = colorScheme.primary
                                )
                            ) {
                                Text("Ver perfil completo", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { onApplyToRequest(servicio.serviceRequestId) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                enabled = hasRealRequests
                            ) {
                                Text("Solicitar trabajo", fontWeight = FontWeight.Bold)
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
