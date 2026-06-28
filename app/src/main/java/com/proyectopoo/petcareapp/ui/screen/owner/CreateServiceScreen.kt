package com.proyectopoo.petcareapp.ui.screen.owner

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyectopoo.petcareapp.data.local.entity.PetEntity
import com.proyectopoo.petcareapp.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateServiceScreen(
    petName: String = "",
    serviceType: String = "",
    dogs: List<PetEntity> = emptyList(),
    onBack: () -> Unit,
    onPublish: (
        petName: String,
        serviceType: String,
        description: String,
        location: String,
        price: String,
        date: String
    ) -> Unit,
    existingServices: List<String> = emptyList(),
    viewModel: MainViewModel = viewModel()
) {

    var nombreMascota by remember(petName) { mutableStateOf(petName) }
    var tipoServicio by remember(serviceType) { mutableStateOf(serviceType) }
    var descripcion by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var expandedPet by remember { mutableStateOf(false) }
    var expandedService by remember { mutableStateOf(false) }

    // NUEVO: Estado para la búsqueda de ubicación
    val locationState by viewModel.locationSearchState.collectAsState()
    var showLocationResults by remember { mutableStateOf(false) }

    val serviceOptions = listOf(
        "Alojamiento", "Guardería", "Paseo", "Taxi", "Peluquería", "Visitante"
    )

    val isDuplicate = tipoServicio.isNotBlank() &&
            existingServices.any { it.equals(tipoServicio, ignoreCase = true) }

    val scrollState = rememberScrollState()

    // NUEVO: Efecto para ocultar resultados cuando se selecciona una ubicación
    LaunchedEffect(locationState.selectedLocation) {
        if (locationState.selectedLocation != null) {
            showLocationResults = false
            ubicacion = locationState.selectedLocation!!.display_name
        }
    }

    // Efecto para limpiar errores cuando el usuario escribe
    LaunchedEffect(nombreMascota, tipoServicio, descripcion, ubicacion, precio, fecha) {
        if (showError) showError = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Cada solicitud puede ser solo para un perro.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Crear Solicitud de Servicio",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Cuéntanos qué necesita tu mascota",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Selector de Mascota
        ExposedDropdownMenuBox(
            expanded = expandedPet,
            onExpandedChange = { expandedPet = it }
        ) {
            OutlinedTextField(
                value = nombreMascota,
                onValueChange = {},
                readOnly = true,
                label = { Text("Nombre de la Mascota") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                trailingIcon = {
                    IconButton(onClick = { expandedPet = !expandedPet }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                },
                isError = showError && nombreMascota.isBlank()
            )

            ExposedDropdownMenu(
                expanded = expandedPet,
                onDismissRequest = { expandedPet = false }
            ) {
                if (dogs.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No tienes mascotas registradas") },
                        onClick = { }
                    )
                } else {
                    dogs.forEach { dog ->
                        DropdownMenuItem(
                            text = { Text(dog.name) },
                            onClick = {
                                nombreMascota = dog.name
                                expandedPet = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selector de Tipo de Servicio
        ExposedDropdownMenuBox(
            expanded = expandedService,
            onExpandedChange = { expandedService = it }
        ) {
            OutlinedTextField(
                value = tipoServicio,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de Servicio") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                trailingIcon = {
                    IconButton(onClick = { expandedService = !expandedService }) {
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                },
                isError = (showError && tipoServicio.isBlank()) || isDuplicate,
                supportingText = if (isDuplicate) {
                    { Text("Ya tienes este servicio creado", color = MaterialTheme.colorScheme.error) }
                } else null
            )

            ExposedDropdownMenu(
                expanded = expandedService,
                onDismissRequest = { expandedService = false }
            ) {
                serviceOptions.forEach { service ->
                    DropdownMenuItem(
                        text = { Text(service) },
                        onClick = {
                            tipoServicio = service
                            expandedService = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Descripción
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            isError = showError && descripcion.isBlank()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ----- NUEVO: CAMPO DE UBICACIÓN CON BÚSQUEDA -----
        Text(
            text = "📍 Ubicación del servicio",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Campo de búsqueda de ubicación
        OutlinedTextField(
            value = locationState.query,
            onValueChange = {
                viewModel.updateSearchQuery(it)
                showLocationResults = it.isNotEmpty()
            },
            label = { Text("Buscar dirección") },
            placeholder = { Text("Ej: Plaza Mayor, Managua") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                if (locationState.query.isNotEmpty()) {
                    IconButton(onClick = {
                        viewModel.clearSearchResults()
                        showLocationResults = false
                        ubicacion = ""
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpiar")
                    }
                } else {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                }
            },
            isError = showError && ubicacion.isBlank()
        )

        // Mostrar resultados de búsqueda
        if (showLocationResults && locationState.results.isNotEmpty() && !locationState.isLoading) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(locationState.results) { result ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = result.display_name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = "📍 ${result.lat}, ${result.lon}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.clickable {
                                viewModel.selectLocation(result)
                                showLocationResults = false
                                ubicacion = result.display_name
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }

        // Mostrar estado de carga
        if (locationState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Mostrar error de búsqueda
        if (locationState.error != null && !locationState.isLoading && showLocationResults) {
            Text(
                text = locationState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Mostrar ubicación seleccionada (si no está en el campo de búsqueda)
        if (locationState.selectedLocation != null && ubicacion.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ubicacion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Si no hay resultados y el usuario busca, mostrar mensaje
        if (showLocationResults && locationState.query.isNotEmpty() &&
            locationState.results.isEmpty() && !locationState.isLoading) {
            Text(
                text = "No se encontraron ubicaciones. Intenta con otra dirección.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Precio
        OutlinedTextField(
            value = precio,
            onValueChange = { value ->
                if (value.all { it.isDigit() }) precio = value
            },
            label = { Text("Precio (C$)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = showError && precio.isBlank()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Fecha
        OutlinedTextField(
            value = fecha,
            onValueChange = {},
            label = { Text("Fecha del servicio") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            singleLine = true,
            isError = showError && fecha.isBlank(),
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Mensaje de error
        if (showError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Botón Publicar
        Button(
            onClick = {
                showError = true
                if (nombreMascota.isBlank() || tipoServicio.isBlank() ||
                    descripcion.isBlank() || ubicacion.isBlank() ||
                    precio.isBlank() || fecha.isBlank()
                ) {
                    errorMessage = "Todos los campos son obligatorios"
                    return@Button
                }

                if (isDuplicate) {
                    errorMessage = "Ya tienes este servicio creado"
                    return@Button
                }

                showError = false
                onPublish(nombreMascota, tipoServicio, descripcion, ubicacion, precio, fecha)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isDuplicate
        ) {
            Text("Publicar Solicitud")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // DatePickerDialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        fecha = datePickerState.selectedDateMillis?.let { millis ->
                            formatDate(millis)
                        } ?: ""
                        showDatePicker = false
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun formatDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}