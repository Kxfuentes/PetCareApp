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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.data.listaServicios

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverFeedScreen(
    onGoToCreate: () -> Unit,
    onGoToCaregiverProfile: () -> Unit
) {

    val colorScheme = MaterialTheme.colorScheme

    val tiposServicio = listOf(
        "Todos",
        "Alojamiento",
        "Guardería",
        "Paseo",
        "Taxi",
        "Peluquería",
        "Visitante"
    )

    var expanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Todos") }

    val serviciosFiltrados = if (selectedFilter == "Todos") {
        listaServicios
    } else {
        listaServicios.filter {
            it.tipoServicio == selectedFilter
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
                    onDismissRequest = { expanded = false }
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

                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {

                            Row(verticalAlignment = Alignment.CenterVertically) {

                                Icon(
                                    imageVector = Icons.Default.Pets,
                                    contentDescription = "Mascota",
                                    tint = colorScheme.primary
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = "${servicio.nombreMascota}, Golden Retriever · Tamaño M",
                                    color = colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Dueño: Carlos Martínez",
                                color = colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(servicio.tipoServicio)
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = colorScheme.secondary,
                                    labelColor = colorScheme.onSecondary
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {

                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Ubicación",
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Managua, Nicaragua",
                                    color = colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {

                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "Hora",
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = servicio.hora,
                                    color = colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = servicio.descripcion,
                                color = colorScheme.onSurfaceVariant,
                                fontSize = 15.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Contacto: +505 8888-8888",
                                color = colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "Email: dueño@email.com",
                                color = colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            OutlinedButton(
                                onClick = { },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, colorScheme.outline),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = "Me interesa",
                                    fontWeight = FontWeight.Bold
                                )
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