package com.proyectopoo.petcareapp.ui.screen.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.data.local.relation.ServiceRequestDetails
import com.proyectopoo.petcareapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverFeedScreen(
    requests: List<ServiceRequestDetails>,
    onGoToOwnerProfile: (Int, Int) -> Unit,
    onApplyToRequest: (Int) -> Unit
) {
    val tiposServicio = listOf(
        "Todos", "Alojamiento", "Guardería", "Paseo", "Taxi", "Peluquería", "Visitante"
    )

    var expanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Todos") }

    val serviciosFiltrados = if (selectedFilter == "Todos") {
        requests
    } else {
        requests.filter { it.serviceTypeName == selectedFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoClaro)
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = CafeMedio
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Solicitudes disponibles",
                        color = CafeOscuro,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = FondoClaro)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedFilter,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Filtrar por servicio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = CafeMedio,
                        unfocusedBorderColor = CafeClaro,
                        focusedLabelColor = CafeOscuro,
                        unfocusedLabelColor = CafeOscuro
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    tiposServicio.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo, color = CafeOscuro) },
                            onClick = {
                                selectedFilter = tipo
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                if (serviciosFiltrados.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = CafeClaro)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No hay solicitudes disponibles",
                                    color = CafeOscuro,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Cuando un dueño publique solicitudes pendientes, aparecerán aquí.",
                                    color = TextoSuave,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                items(serviciosFiltrados) { servicio ->
                    val ownerName = servicio.ownerName ?: "Dueño"
                    val petName = servicio.petName ?: servicio.title ?: "Mascota"
                    val serviceType = servicio.serviceTypeName ?: "Servicio"
                    val rawDescription = servicio.description.orEmpty()
                    val location = extractDetailLine(rawDescription, "Ubicación")
                    val price = extractDetailLine(rawDescription, "Precio")
                    val notes = rawDescription
                        .lineSequence()
                        .filterNot { line ->
                            line.trim().startsWith("Ubicación:", ignoreCase = true) ||
                                    line.trim().startsWith("Precio:", ignoreCase = true)
                        }
                        .joinToString("\n")
                        .trim()
                    val timeText = listOfNotNull(servicio.startTime, servicio.endTime)
                        .joinToString(" - ")
                        .ifBlank { null }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(24.dp))
                            .border(1.dp, CafeClaro.copy(alpha = 0.75f), RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {

                            // Avatar con letra del dueño (como en OwnerFeedScreen)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = CafeOscuro,
                                    modifier = Modifier.size(58.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = ownerName.firstOrNull()?.toString() ?: "D",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 26.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = petName,
                                        color = CafeOscuro,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp
                                    )
                                    Text(
                                        text = "Dueño: $ownerName",
                                        color = CafeOscuro,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Chip sin icono
                            AssistChip(
                                onClick = { },
                                label = { Text(serviceType) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = CafeClaro.copy(alpha = 0.28f),
                                    labelColor = CafeOscuro
                                ),
                                border = null
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            DetailRow(
                                icon = Icons.Default.CalendarToday,
                                title = servicio.requestedDate ?: "Fecha por coordinar"
                            )

                            timeText?.let {
                                Spacer(modifier = Modifier.height(14.dp))
                                DetailRow(
                                    icon = Icons.Default.AccessTime,
                                    title = it
                                )
                            }

                            location?.let {
                                Spacer(modifier = Modifier.height(14.dp))
                                DetailRow(
                                    icon = Icons.Default.LocationOn,
                                    title = "Ubicación",
                                    detail = it
                                )
                            }

                            price?.let {
                                Spacer(modifier = Modifier.height(14.dp))
                                DetailRow(
                                    icon = Icons.Default.Payments,
                                    title = "Precio",
                                    detail = it
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            DetailRow(
                                icon = Icons.Default.Email,
                                title = "Email",
                                detail = servicio.ownerEmail ?: "No disponible"
                            )

                            if (notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(18.dp))
                                HorizontalDivider(color = CafeClaro.copy(alpha = 0.45f))
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Notas del dueño",
                                    color = CafeOscuro,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = notes,
                                    color = TextoSuave,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedButton(
                                onClick = { onGoToOwnerProfile(servicio.ownerId, servicio.serviceRequestId) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CafeMedio)
                            ) {
                                Text("Ver perfil completo", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { onApplyToRequest(servicio.serviceRequestId) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D5524))
                            ) {
                                Text("Ofrecer mis servicios", fontWeight = FontWeight.Bold)
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
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    detail: String? = null
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CafeOscuro,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                color = CafeOscuro,
                fontSize = 14.sp
            )
            detail?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    color = TextoSuave,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

private fun extractDetailLine(description: String, label: String): String? =
    description
        .lineSequence()
        .map { it.trim() }
        .firstOrNull { it.startsWith("$label:", ignoreCase = true) }
        ?.substringAfter(":")
        ?.trim()
        ?.takeIf { it.isNotBlank() }