package com.proyectopoo.petcareapp.ui.screen.caregiver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.model.ServiceGiven

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverServiceScreen(
    onBack: () -> Unit
) {

    val colorScheme = MaterialTheme.colorScheme

    val tiposServicio = listOf(
        "Alojamiento", "Guardería", "Paseo",
        "Taxi", "Peluquería", "Visitante"
    )

    var mostrarFormulario by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    var tipoServicio by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var activo by remember { mutableStateOf(true) }

    var servicios by remember {
        mutableStateOf(
            listOf(
                ServiceGiven(1, "Alojamiento", "$48", "Cuidado nocturno y ambiente cómodo.", true),
                ServiceGiven(2, "Paseo", "$12", "Paseos diarios de 30 minutos.", false)
            )
        )
    }

    fun obtenerIcono(nombre: String) = when (nombre) {
        "Alojamiento" -> Icons.Default.NightShelter
        "Guardería" -> Icons.Default.ChildCare
        "Paseo" -> Icons.Default.DirectionsWalk
        "Taxi" -> Icons.Default.LocalTaxi
        "Peluquería" -> Icons.Default.ContentCut
        else -> Icons.Default.House
    }

    Scaffold(
        containerColor = colorScheme.background,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mis servicios ofrecidos",
                        color = colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.primary
                )
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarFormulario = true },
                containerColor = colorScheme.secondary,
                contentColor = colorScheme.onSecondary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { paddingValues ->

        if (mostrarFormulario) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                Text(
                    "Agregar servicio",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(24.dp))


                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {

                    OutlinedTextField(
                        value = tipoServicio,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de servicio") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
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
                                    tipoServicio = tipo
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colorScheme.surface,
                        unfocusedContainerColor = colorScheme.surface,
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth().height(130.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colorScheme.surface,
                        unfocusedContainerColor = colorScheme.surface,
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text(
                        "Activo",
                        color = colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Switch(
                        checked = activo,
                        onCheckedChange = { activo = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colorScheme.onPrimary,
                            checkedTrackColor = colorScheme.primary,
                            uncheckedTrackColor = colorScheme.surfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                        val nuevo = ServiceGiven(
                            servicios.size + 1,
                            tipoServicio,
                            precio,
                            descripcion,
                            activo
                        )
                        servicios = servicios + nuevo
                        tipoServicio = ""
                        precio = ""
                        descripcion = ""
                        activo = true
                        mostrarFormulario = false
                    },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    )
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { mostrarFormulario = false },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorScheme.primary
                    )
                ) {
                    Text("Cancelar", fontWeight = FontWeight.Bold)
                }
            }

        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                item { Spacer(modifier = Modifier.height(10.dp)) }

                items(servicios) { servicio ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colorScheme.outline, RoundedCornerShape(22.dp)),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.surfaceVariant
                        )
                    ) {

                        Column(Modifier.padding(18.dp)) {

                            Row(verticalAlignment = Alignment.CenterVertically) {

                                Icon(
                                    obtenerIcono(servicio.nombre),
                                    contentDescription = servicio.nombre,
                                    tint = colorScheme.primary
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(Modifier.weight(1f)) {

                                    Text(
                                        servicio.nombre,
                                        color = colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        servicio.precio,
                                        color = colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Switch(
                                    checked = servicio.activo,
                                    onCheckedChange = {},
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = colorScheme.onPrimary,
                                        checkedTrackColor = colorScheme.primary,
                                        uncheckedTrackColor = colorScheme.surfaceVariant
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                servicio.descripcion,
                                color = colorScheme.onSurfaceVariant,
                                fontSize = 15.sp
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            OutlinedButton(
                                onClick = {},
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, colorScheme.outline),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = colorScheme.primary
                                )
                            ) {

                                Icon(Icons.Default.Edit, contentDescription = "Editar")

                                Spacer(modifier = Modifier.width(8.dp))

                                Text("Editar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(90.dp)) }
            }
        }
    }
}