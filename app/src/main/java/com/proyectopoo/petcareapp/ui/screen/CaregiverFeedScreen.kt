package com.proyectopoo.petcareapp.ui.screen

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.ui.data.listaServicios
import com.proyectopoo.petcareapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverFeedScreen(
    onGoToCreate: () -> Unit,
    onGoToProfile: () -> Unit
) {

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
            .background(Color.White)
    ) {

        TopAppBar(

            title = {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = Color.White
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Solicitudes disponibles",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },

            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = CafeOscuro
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    expanded = !expanded
                }
            ) {

                OutlinedTextField(
                    value = selectedFilter,
                    onValueChange = {},
                    readOnly = true,
                    label = {
                        Text("Tipo de servicio")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = FondoCampo,
                        unfocusedContainerColor = FondoCampo,
                        focusedBorderColor = CafeMedio,
                        unfocusedBorderColor = CafeClaro
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    }
                ) {

                    tiposServicio.forEach { tipo ->

                        DropdownMenuItem(
                            text = {
                                Text(tipo)
                            },

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
                                color = CafeClaro,
                                shape = RoundedCornerShape(22.dp)
                            ),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = FondoClaro
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {


                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Icon(
                                    imageVector = Icons.Default.Pets,
                                    contentDescription = "Mascota",
                                    tint = CafeMedio
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = "🐶 ${servicio.nombreMascota}, Golden Retriever · Tamaño M",
                                    color = CafeOscuro,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))


                            Text(
                                text = "Dueño: Carlos Martínez",
                                color = CafeOscuro,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(12.dp))


                            AssistChip(
                                onClick = { },

                                label = {
                                    Text(
                                        text = servicio.tipoServicio
                                    )
                                },

                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = CafeClaro,
                                    labelColor = CafeOscuro
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))


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
                                    text = "Managua, Nicaragua",
                                    color = CafeOscuro
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))


                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "Hora",
                                    tint = CafeMedio,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = servicio.hora,
                                    color = CafeOscuro
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))


                            Text(
                                text = servicio.descripcion,
                                color = TextoSuave,
                                fontSize = 15.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))


                            Text(
                                text = "Contacto: +505 8888-8888",
                                color = CafeOscuro,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "Email: dueño@email.com",
                                color = CafeOscuro
                            )

                            Spacer(modifier = Modifier.height(18.dp))


                            OutlinedButton(
                                onClick = { },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                border = ButtonDefaults.outlinedButtonBorder,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = CafeMedio
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