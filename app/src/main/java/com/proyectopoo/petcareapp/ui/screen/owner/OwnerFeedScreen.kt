package com.proyectopoo.petcareapp.ui.screen.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.model.Cuidador

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OwnerFeedScreen(
    onGoToCaregiverProfile: (Int) -> Unit,
    onRequestServices: (Int) -> Unit
) {
    val tiposServicio = listOf(
        "Todos", "Alojamiento", "Guardería", "Paseo", "Taxi", "Peluquería", "Visitante"
    )

    val cuidadores = listOf(
        Cuidador(
            id = 1,
            nombre = "Carlos Martínez",
            ubicacion = "Managua, Nicaragua",
            precio = "C$250 por paseo",
            rating = 4.8,
            reviews = 42,
            servicios = listOf("Paseo", "Visitante"),
            review = "Ideal para paseos programados y visitas a domicilio."
        ),
        Cuidador(
            id = 2,
            nombre = "Valeria López",
            ubicacion = "León, Nicaragua",
            precio = "C$900 por alojamiento",
            rating = 4.9,
            reviews = 81,
            servicios = listOf("Alojamiento", "Guardería", "Peluquería"),
            review = "Recibe mascotas para guardería, alojamiento y grooming."
        ),
        Cuidador(
            id = 3,
            nombre = "María Fernanda",
            ubicacion = "Masaya, Nicaragua",
            precio = "C$350 por taxi",
            rating = 4.7,
            reviews = 35,
            servicios = listOf("Taxi", "Peluquería"),
            review = "Apoya con traslados a veterinaria y servicios de peluquería."
        )
    )

    var expanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Todos") }

    val filteredCaregivers = if (selectedFilter == "Todos") {
        cuidadores
    } else {
        cuidadores.filter { it.servicios.contains(selectedFilter) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cuidadores disponibles") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                    label = { Text("Filtrar por servicio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
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
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(filteredCaregivers) { cuidador ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cuidador.nombre.first().toString(),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cuidador.nombre,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Star,
                                            null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "${cuidador.rating} (${cuidador.reviews})",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = "Ubicación",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = cuidador.ubicacion, style = MaterialTheme.typography.bodyMedium)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Precio: ${cuidador.precio}",
                                style = MaterialTheme.typography.titleSmall
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                cuidador.servicios.forEach { servicio ->
                                    AssistChip(
                                        onClick = { },
                                        label = { Text(servicio) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "\"${cuidador.review}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedButton(
                                onClick = { onGoToCaregiverProfile(cuidador.id) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Ver perfil completo", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { onRequestServices(cuidador.id) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Solicitar servicios", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
