package com.proyectopoo.petcareapp.ui.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.NightShelter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.ui.model.ServiceGiven
import com.proyectopoo.petcareapp.ui.theme.BordeCampo
import com.proyectopoo.petcareapp.ui.theme.CafeClaro
import com.proyectopoo.petcareapp.ui.theme.CafeMedio
import com.proyectopoo.petcareapp.ui.theme.CafeOscuro
import com.proyectopoo.petcareapp.ui.theme.FondoCampo
import com.proyectopoo.petcareapp.ui.theme.FondoClaro
import com.proyectopoo.petcareapp.ui.theme.TextoSuave

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverServiceScreen(
    onBack: () -> Unit
) {

    val tiposServicio = listOf(
        "Alojamiento",
        "Guardería",
        "Paseo",
        "Taxi",
        "Peluquería",
        "Visitante"
    )

    var mostrarFormulario by remember {
        mutableStateOf(false)
    }

    var expanded by remember {
        mutableStateOf(false)
    }

    var tipoServicio by remember {
        mutableStateOf("")
    }

    var precio by remember {
        mutableStateOf("")
    }

    var descripcion by remember {
        mutableStateOf("")
    }

    var activo by remember {
        mutableStateOf(true)
    }

    var servicios by remember {

        mutableStateOf(

            listOf(

                ServiceGiven(
                    id = 1,
                    nombre = "Alojamiento",
                    precio = "$48",
                    descripcion = "Cuidado nocturno y ambiente cómodo.",
                    activo = true
                ),

                ServiceGiven(
                    id = 2,
                    nombre = "Paseo",
                    precio = "$12",
                    descripcion = "Paseos diarios de 30 minutos.",
                    activo = false
                )
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

        containerColor = FondoClaro,

        topBar = {

            TopAppBar(

                title = {

                    Text(
                        text = "Mis servicios ofrecidos",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CafeOscuro
                )
            )
        },

        floatingActionButton = {

            FloatingActionButton(

                onClick = {
                    mostrarFormulario = true
                },

                containerColor = CafeMedio,
                contentColor = Color.White
            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar"
                )
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
                    text = "Agregar servicio",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeOscuro
                )

                Spacer(modifier = Modifier.height(24.dp))

                // TIPO DE SERVICIO
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = {
                        expanded = !expanded
                    }
                ) {

                    OutlinedTextField(
                        value = tipoServicio,
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
                            unfocusedBorderColor = BordeCampo
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
                                    tipoServicio = tipo
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // PRECIO
                OutlinedTextField(
                    value = precio,
                    onValueChange = {
                        precio = it
                    },

                    label = {
                        Text("Precio")
                    },

                    singleLine = true,

                    modifier = Modifier.fillMaxWidth(),

                    shape = RoundedCornerShape(16.dp),

                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = FondoCampo,
                        unfocusedContainerColor = FondoCampo,
                        focusedBorderColor = CafeMedio,
                        unfocusedBorderColor = BordeCampo
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // DESCRIPCIÓN
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = {
                        descripcion = it
                    },

                    label = {
                        Text("Descripción")
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),

                    shape = RoundedCornerShape(16.dp),

                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = FondoCampo,
                        unfocusedContainerColor = FondoCampo,
                        focusedBorderColor = CafeMedio,
                        unfocusedBorderColor = BordeCampo
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))


                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Activo",
                        color = CafeOscuro,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Switch(
                        checked = activo,

                        onCheckedChange = {
                            activo = it
                        },

                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CafeMedio,
                            uncheckedTrackColor = CafeClaro
                        )
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                // GUARDAR
                Button(

                    onClick = {

                        val nuevoServicio = ServiceGiven(
                            id = servicios.size + 1,
                            nombre = tipoServicio,
                            precio = precio,
                            descripcion = descripcion,
                            activo = activo
                        )

                        servicios = servicios + nuevoServicio

                        tipoServicio = ""
                        precio = ""
                        descripcion = ""
                        activo = true

                        mostrarFormulario = false
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),

                    shape = RoundedCornerShape(18.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = CafeMedio
                    )
                ) {

                    Text(
                        text = "Guardar",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // CANCELAR
                OutlinedButton(

                    onClick = {
                        mostrarFormulario = false
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),

                    shape = RoundedCornerShape(18.dp),

                    border = ButtonDefaults.outlinedButtonBorder(
                        enabled = true
                    ),

                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CafeMedio
                    )
                ) {

                    Text(
                        text = "Cancelar",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

        } else {

            // LISTA DE SERVICIOS
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),

                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }

                items(servicios) { servicio ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = CafeClaro,
                                shape = RoundedCornerShape(22.dp)
                            ),

                        shape = RoundedCornerShape(22.dp),

                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Icon(
                                    imageVector = obtenerIcono(servicio.nombre),
                                    contentDescription = servicio.nombre,
                                    tint = CafeMedio
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Text(
                                        text = servicio.nombre,
                                        color = CafeOscuro,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = servicio.precio,
                                        color = CafeMedio,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Switch(
                                    checked = servicio.activo,
                                    onCheckedChange = {},

                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = CafeMedio,
                                        uncheckedTrackColor = CafeClaro
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = servicio.descripcion,
                                color = TextoSuave,
                                fontSize = 15.sp
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            OutlinedButton(

                                onClick = { },

                                shape = RoundedCornerShape(14.dp),

                                border = ButtonDefaults.outlinedButtonBorder(
                                    enabled = true
                                ),

                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = CafeMedio
                                )
                            ) {

                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar"
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Editar",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }
    }
}

