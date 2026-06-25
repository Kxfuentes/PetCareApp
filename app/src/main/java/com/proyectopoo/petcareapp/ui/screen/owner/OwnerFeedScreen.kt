package com.proyectopoo.petcareapp.ui.screen.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.model.Cuidador
import com.proyectopoo.petcareapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoClaro)
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, null, tint = CafeMedio)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Cuidadores disponibles", color = CafeOscuro, fontWeight = FontWeight.Bold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
                    label = { Text("Filtrar por servicio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = FondoCampo,
                        unfocusedContainerColor = FondoCampo,
                        focusedBorderColor = CafeMedio,
                        unfocusedBorderColor = BordeCampo
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
                            .shadow(6.dp, RoundedCornerShape(24.dp))
                            .border(1.dp, CafeClaro, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = CafeOscuro,
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cuidador.nombre.first().toString(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = cuidador.nombre,
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
                                            text = cuidador.rating.toString(),
                                            color = CafeOscuro,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = " (${cuidador.reviews})",
                                            color = TextoSuave
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Ubicación",
                                    tint = CafeMedio,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = cuidador.ubicacion,
                                    color = CafeOscuro
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Precio: ${cuidador.precio}",
                                color = CafeOscuro,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                cuidador.servicios.forEach { servicio ->
                                    AssistChip(
                                        onClick = { },
                                        label = { Text(servicio) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = CafeClaro,
                                            labelColor = CafeOscuro
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "\"${cuidador.review}\"",
                                color = TextoSuave,
                                fontStyle = FontStyle.Italic,
                                fontSize = 15.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedButton(
                                onClick = { onGoToCaregiverProfile(cuidador.id) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CafeMedio)
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