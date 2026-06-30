package com.proyectopoo.petcareapp.ui.screen.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import com.proyectopoo.petcareapp.data.local.relation.OfferedServiceDetails
import com.proyectopoo.petcareapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerFeedScreen(
    services: List<OfferedServiceDetails>,
    onGoToCaregiverProfile: (Int) -> Unit,
    onRequestService: (Int, Int) -> Unit
) {
    val tiposServicio = listOf(
        "Todos", "Alojamiento", "Guardería", "Paseo", "Taxi", "Peluquería", "Visitante"
    )

    var expanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Todos") }

    val filteredServices = if (selectedFilter == "Todos") {
        services
    } else {
        services.filter { it.serviceTypeName == selectedFilter }
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
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = CafeMedio
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Cuidadores disponibles",
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
            // Filtro blanco
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
                            text = { Text(tipo) },
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
                if (filteredServices.isEmpty()) {
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
                                    text = "No hay servicios disponibles",
                                    color = CafeOscuro,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Cuando un cuidador publique servicios activos, aparecerán aquí.",
                                    color = TextoSuave,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                items(filteredServices) { service ->
                    val caregiverName = service.caregiverName ?: "Cuidador"
                    val rating = service.caregiverRating?.takeIf { it > 0.0 } ?: 5.0
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(24.dp))
                            .border(1.dp, CafeClaro.copy(alpha = 0.75f), RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {  // Padding reducido como en la otra pantalla

                            // Avatar + Nombre + Rating + Disponible
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = CafeOscuro,
                                        modifier = Modifier.size(58.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = caregiverName.firstOrNull()?.toString() ?: "C",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 26.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column {
                                        Text(
                                            text = caregiverName,
                                            color = CafeOscuro,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 19.sp
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = null,
                                                tint = CafeMedio,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "%.1f".format(rating),
                                                color = CafeOscuro,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = " (${service.caregiverRatingCount ?: 0})",
                                                color = TextoSuave
                                            )
                                        }
                                    }
                                }

                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Contacto (Correo)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.MailOutline,
                                    contentDescription = null,
                                    tint = CafeMedio,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = service.caregiverEmail ?: service.caregiverPhone ?: "Contacto no disponible",
                                    color = CafeOscuro
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Precio
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = "Precio",
                                    tint = CafeMedio,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Precio: C$${ "%.2f".format(service.price) }",
                                    color = CafeOscuro,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Chip sin icono
                            val serviceType = service.serviceTypeName ?: service.title ?: ""
                            if (serviceType.isNotBlank()) {
                                AssistChip(
                                    onClick = { },
                                    label = { Text(serviceType) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = CafeClaro.copy(alpha = 0.28f),
                                        labelColor = CafeOscuro
                                    ),
                                    border = null
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Descripción
                            Text(
                                text = service.description?.ifBlank { null } ?: service.title ?: "",
                                color = TextoSuave,
                                fontStyle = FontStyle.Italic,
                                fontSize = 15.sp
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Botones
                            OutlinedButton(
                                onClick = { onGoToCaregiverProfile(service.caregiverId) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CafeMedio)
                            ) {
                                Text("Ver perfil completo", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { onRequestService(service.caregiverId, service.offeredServiceId) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D5524))
                            ) {
                                Text("Solicitar este servicio", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}