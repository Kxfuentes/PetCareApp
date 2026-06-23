package com.proyectopoo.petcareapp.ui.screen.owner

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.data.local.entity.PetEntity
import java.text.SimpleDateFormat
import java.util.*

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
        date: String,
        startTime: String,
        endTime: String
    ) -> Unit,
    existingServices: List<String> = emptyList()
) {

    var nombreMascota by remember(petName) { mutableStateOf(petName) }
    var tipoServicio by remember(serviceType) { mutableStateOf(serviceType) }
    var descripcion by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var horaInicio by remember { mutableStateOf("") }
    var horaFin by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var expandedPet by remember { mutableStateOf(false) }
    var expandedService by remember { mutableStateOf(false) }

    val serviceOptions = listOf("Alojamiento", "Guardería", "Paseo", "Taxi", "Peluquería", "Visitante")

    val isDuplicate = tipoServicio.isNotBlank() &&
            existingServices.any { it.equals(tipoServicio, ignoreCase = true) }

    val scrollState = rememberScrollState()

    val currentLocale = remember { Locale.getDefault() }
    val dateFormatter = remember(currentLocale) { SimpleDateFormat("dd/MM/yyyy", currentLocale) }
    val pickerDateFormatter = remember(currentLocale) {
        SimpleDateFormat("dd/MM/yyyy", currentLocale).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    val todayMillis = remember { Calendar.getInstance().startOfDayMillis() }
    val maxDateMillis = remember {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, 6)
        }.startOfDayMillis()
    }
    val todayPickerMillis = remember { Calendar.getInstance(TimeZone.getTimeZone("UTC")).startOfDayMillis() }
    val maxPickerDateMillis = remember {
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            add(Calendar.MONTH, 6)
        }.startOfDayMillis()
    }

    val isPastDate by remember(fecha, dateFormatter) {
        mutableStateOf(
            if (fecha.isBlank()) false
            else try {
                val selectedDate = dateFormatter.parse(fecha)
                selectedDate?.let { date ->
                    Calendar.getInstance().apply { time = date }.startOfDayMillis() < todayMillis
                } ?: false
            } catch (e: Exception) {
                false
            }
        )
    }

    val isBeyondSixMonths by remember(fecha, dateFormatter) {
        mutableStateOf(
            if (fecha.isBlank()) false
            else try {
                val selectedDate = dateFormatter.parse(fecha)
                selectedDate?.let { date ->
                    Calendar.getInstance().apply { time = date }.startOfDayMillis() > maxDateMillis
                } ?: false
            } catch (e: Exception) {
                false
            }
        )
    }

    LaunchedEffect(nombreMascota, tipoServicio, ubicacion, precio, fecha, horaInicio, horaFin) {
        if (showError) showError = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Card informativo
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text(
                    "Cada solicitud puede ser solo para un perro.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("Crear Solicitud de Servicio", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Cuéntanos qué necesita tu mascota", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(24.dp))

        // Dropdown Mascota - Fondo Blanco
        ExposedDropdownMenuBox(expanded = expandedPet, onExpandedChange = { expandedPet = it }) {
            OutlinedTextField(
                value = nombreMascota,
                onValueChange = {},
                readOnly = true,
                label = { Text("Nombre de la Mascota") },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                trailingIcon = { IconButton(onClick = { expandedPet = !expandedPet }) { Icon(Icons.Default.ArrowDropDown, null) } },
                isError = showError && nombreMascota.isBlank()
            )
            ExposedDropdownMenu(
                expanded = expandedPet,
                onDismissRequest = { expandedPet = false },
                containerColor = Color.White
            ) {
                dogs.forEach { dog ->
                    DropdownMenuItem(text = { Text(dog.name) }, onClick = { nombreMascota = dog.name; expandedPet = false })
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Dropdown Tipo de Servicio - Fondo Blanco
        ExposedDropdownMenuBox(expanded = expandedService, onExpandedChange = { expandedService = it }) {
            OutlinedTextField(
                value = tipoServicio,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de Servicio") },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                trailingIcon = { IconButton(onClick = { expandedService = !expandedService }) { Icon(Icons.Default.ArrowDropDown, null) } },
                isError = (showError && tipoServicio.isBlank()) || isDuplicate,
                supportingText = if (isDuplicate) {{ Text("Ya tienes este servicio creado", color = MaterialTheme.colorScheme.error) }} else null
            )
            ExposedDropdownMenu(
                expanded = expandedService,
                onDismissRequest = { expandedService = false },
                containerColor = Color.White
            ) {
                serviceOptions.forEach { service ->
                    DropdownMenuItem(text = { Text(service) }, onClick = { tipoServicio = service; expandedService = false })
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = ubicacion,
            onValueChange = { ubicacion = it },
            label = { Text("Ubicación") },
            modifier = Modifier.fillMaxWidth(),
            isError = showError && ubicacion.isBlank()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = precio,
            onValueChange = { if (it.all { char -> char.isDigit() }) precio = it },
            label = { Text("Precio (C$)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = showError && precio.isBlank()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = fecha,
            onValueChange = {},
            label = { Text("Fecha del servicio") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            isError = showError && (fecha.isBlank() || isPastDate || isBeyondSixMonths),
            supportingText = when {
                isPastDate -> {{ Text("La fecha no puede ser pasada", color = MaterialTheme.colorScheme.error) }}
                isBeyondSixMonths -> {{ Text("La fecha no puede ser mayor a 6 meses", color = MaterialTheme.colorScheme.error) }}
                else -> null
            },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                }
            }
        )

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = horaInicio,
                onValueChange = {},
                label = { Text("Hora Inicio") },
                modifier = Modifier.weight(1f),
                readOnly = true,
                isError = showError && horaInicio.isBlank(),
                trailingIcon = {
                    IconButton(onClick = { showStartTimePicker = true }) {
                        Icon(Icons.Default.AccessTime, null)
                    }
                }
            )

            OutlinedTextField(
                value = horaFin,
                onValueChange = {},
                label = { Text("Hora Fin") },
                modifier = Modifier.weight(1f),
                readOnly = true,
                isError = showError && horaFin.isBlank(),
                trailingIcon = {
                    IconButton(onClick = { showEndTimePicker = true }) {
                        Icon(Icons.Default.AccessTime, null)
                    }
                }
            )
        }

        Spacer(Modifier.height(32.dp))

        if (showError) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                showError = true
                when {
                    nombreMascota.isBlank() -> errorMessage = "Selecciona una mascota"
                    tipoServicio.isBlank() -> errorMessage = "Selecciona un tipo de servicio"
                    ubicacion.isBlank() -> errorMessage = "Ingresa la ubicación"
                    precio.isBlank() -> errorMessage = "Ingresa el precio"
                    fecha.isBlank() -> errorMessage = "Selecciona una fecha"
                    horaInicio.isBlank() || horaFin.isBlank() -> errorMessage = "Selecciona hora de inicio y fin"
                    isPastDate -> errorMessage = "La fecha no puede ser pasada"
                    isBeyondSixMonths -> errorMessage = "La fecha no puede ser mayor a 6 meses"
                    isDuplicate -> errorMessage = "Ya tienes este servicio creado"
                    else -> {
                        showError = false
                        onPublish(nombreMascota, tipoServicio, descripcion, ubicacion, precio, fecha, horaInicio, horaFin)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isDuplicate && !isPastDate && !isBeyondSixMonths
        ) {
            Text("Publicar Solicitud")
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Cancelar")
        }
    }

    // ==================== DATE PICKER - TODO BLANCO ====================
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = todayPickerMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis in todayPickerMillis..maxPickerDateMillis
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    fecha = datePickerState.selectedDateMillis?.let { millis ->
                        pickerDateFormatter.format(Date(millis))
                    } ?: ""
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } },
            colors = DatePickerDefaults.colors(
                containerColor = Color.White,
                titleContentColor = Color.Black,
                headlineContentColor = Color.Black,
                weekdayContentColor = Color.Black,
                subheadContentColor = Color.Black,
                yearContentColor = Color.Black,
                currentYearContentColor = Color.Black,
                selectedYearContentColor = Color.White,
                selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                todayContentColor = MaterialTheme.colorScheme.primary,
                todayDateBorderColor = MaterialTheme.colorScheme.primary,
                dayContentColor = Color.Black,
                selectedDayContentColor = Color.White,
                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                dividerColor = Color.LightGray
            )
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White,
                    dayContentColor = Color.Black,
                    selectedDayContentColor = Color.White,
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    todayContentColor = MaterialTheme.colorScheme.primary,
                    todayDateBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }

    // ==================== TIME PICKERS ====================
    if (showStartTimePicker) {
        TimePickerDialog(
            onDismiss = { showStartTimePicker = false },
            onConfirm = { timeState ->
                horaInicio = String.format("%02d:%02d", timeState.hour, timeState.minute)
                showStartTimePicker = false
            }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            onDismiss = { showEndTimePicker = false },
            onConfirm = { timeState ->
                horaFin = String.format("%02d:%02d", timeState.hour, timeState.minute)
                showEndTimePicker = false
            }
        )
    }
}

private fun Calendar.startOfDayMillis(): Long {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
    return timeInMillis
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (TimePickerState) -> Unit
) {
    val timeState = rememberTimePickerState()
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { onConfirm(timeState) }) { Text("Aceptar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        containerColor = Color.White,
        text = {
            TimePicker(
                state = timeState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = Color.White,
                    selectorColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    )
}
