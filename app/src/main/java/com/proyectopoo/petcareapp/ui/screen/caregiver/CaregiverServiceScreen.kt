package com.proyectopoo.petcareapp.ui.screen.caregiver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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

    val detallesServicios = listOf(
        "Alojamiento" to "Recibe y hospeda a la mascota en tu casa por 24h o más",
        "Guardería" to "Cuida a la mascota en tu casa durante el día (8am a 8pm)",
        "Paseo" to "Saca a pasear a la mascota para que se ejercite y recree",
        "Taxi" to "Transporta de forma segura a la mascota hacia su destino",
        "Peluquería" to "Ofrece cortes, baño y estética canina/felina especializada",
        "Visitante" to "Desplázate a la casa del dueño para atender a la mascota"
    )

    var mostrarFormulario by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    var tipoServicio by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var activo by remember { mutableStateOf(true) }
    var servicioEditando by remember { mutableStateOf<ServiceGiven?>(null) }
    var precioEditado by remember { mutableStateOf("") }
    var descripcionEditada by remember { mutableStateOf("") }
    var activoEditado by remember { mutableStateOf(true) }

    var servicios by remember { mutableStateOf(listOf<ServiceGiven>()) }

    fun obtenerIcono(nombre: String) = when (nombre) {
        "Alojamiento" -> Icons.Default.NightShelter
        "Guardería" -> Icons.Default.WbSunny
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
            if (!mostrarFormulario) {
                FloatingActionButton(
                    onClick = { mostrarFormulario = true },
                    containerColor = colorScheme.secondary,
                    contentColor = colorScheme.onSecondary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar")
                }
            }
        }
    ) { paddingValues ->

        // --- DIÁLOGO DE EDICIÓN ---
        servicioEditando?.let { servicio ->
            AlertDialog(
                onDismissRequest = { servicioEditando = null },
                containerColor = Color.White,
                title = {
                    Text(
                        "Editar ${servicio.nombre}",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        OutlinedTextField(
                            value = precioEditado,
                            onValueChange = { precioEditado = it },
                            label = { Text("Precio (C$)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )

                        OutlinedTextField(
                            value = descripcionEditada,
                            onValueChange = { descripcionEditada = it },
                            label = { Text("Descripción (Opcional)") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Activo",
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Switch(
                                checked = activoEditado,
                                onCheckedChange = { activoEditado = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colorScheme.onPrimary,
                                    checkedTrackColor = colorScheme.primary,
                                    uncheckedTrackColor = colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val precioFormateado = if (precioEditado.startsWith("C$")) precioEditado else "C$ $precioEditado"

                            servicios = servicios.map {
                                if (it.id == servicio.id) {
                                    it.copy(
                                        precio = precioFormateado,
                                        descripcion = descripcionEditada,
                                        activo = activoEditado
                                    )
                                } else {
                                    it
                                }
                            }
                            servicioEditando = null
                        },
                        enabled = precioEditado.isNotBlank()
                    ) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { servicioEditando = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // --- FORMULARIO PARA AGREGAR NUEVO SERVICIO ---
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
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        tiposServicio.forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text(tipo, color = Color.Black) },
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
                    label = { Text("Precio(C$)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    label = { Text("Descripción (Opcional)") },
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

                val formularioValido = tipoServicio.isNotBlank() && precio.isNotBlank()

                Button(
                    onClick = {
                        val nuevo = ServiceGiven(
                            servicios.size + 1,
                            tipoServicio,
                            "C$ $precio",
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
                    enabled = formularioValido,
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

            // --- VISTA PRINCIPAL DE SERVICIOS OFRECIDOS ---
            if (servicios.isEmpty()) {
                // Modificado a LazyColumn para soportar cómodamente la lista explicativa con scroll
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item { Spacer(modifier = Modifier.height(20.dp)) }

                    // Bloque del mensaje informativo principal
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Información",
                                modifier = Modifier.size(44.dp),
                                tint = colorScheme.primary
                            )
                            Text(
                                text = "No hay servicios ofrecidos registrados aún.\nPresiona el botón \"+\" para agregar el primero.",
                                color = colorScheme.onSurfaceVariant,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 22.sp
                            )
                        }
                    }

                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = colorScheme.outlineVariant
                        )
                    }

                    // Título de la guía de servicios
                    item {
                        Text(
                            text = "Guía de servicios de la plataforma",
                            color = colorScheme.onBackground,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }

                    // Renderizado de las 6 explicaciones mapeadas dinámicamente
                    items(detallesServicios) { (nombre, explicacion) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = obtenerIcono(nombre),
                                    contentDescription = nombre,
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = nombre,
                                        color = colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = explicacion,
                                        color = colorScheme.onSurfaceVariant,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(90.dp)) }
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
                                        onCheckedChange = { nuevoEstado ->
                                            servicios = servicios.map {
                                                if (it.id == servicio.id) it.copy(activo = nuevoEstado) else it
                                            }
                                        },
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
                                    onClick = {
                                        servicioEditando = servicio
                                        precioEditado = servicio.precio.replace("C$", "").trim()
                                        descripcionEditada = servicio.descripcion
                                        activoEditado = servicio.activo
                                    },
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
}