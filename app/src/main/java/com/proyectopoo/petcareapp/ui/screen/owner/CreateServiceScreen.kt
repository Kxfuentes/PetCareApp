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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.data.local.entity.PetEntity
import com.proyectopoo.petcareapp.model.NominatimResponse
import com.proyectopoo.petcareapp.ui.components.LocationAutocompleteField
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
        selectedPetNames: List<String>,
        serviceType: String,
        description: String,
        location: String,
        price: String,
        date: String,
        startTime: String,
        endTime: String,
        latitude: Double?,
        longitude: Double?
    ) -> Unit
) {

    var selectedPetIds by remember { mutableStateOf(setOf<Int>()) }
    var tipoServicio by remember(serviceType) { mutableStateOf(serviceType) }
    var descripcion by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var ubicacionLat by remember { mutableStateOf<Double?>(null) }
    var ubicacionLon by remember { mutableStateOf<Double?>(null) }
    var precio by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var horaInicio by remember { mutableStateOf("") }
    var horaFin by remember { mutableStateOf("") }
    var fechaSalida by remember { mutableStateOf("") }
    var ubicacionDestino by remember { mutableStateOf("") }
    var necesitaTransporte by remember { mutableStateOf(false) }
    var idaYVuelta by remember { mutableStateOf(false) }
    var tipoPeluqueria by remember { mutableStateOf("") }
    var expandedGrooming by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    var showExitDatePicker by remember { mutableStateOf(false) }

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var expandedService by remember { mutableStateOf(false) }

    LaunchedEffect(dogs, petName) {
        if (selectedPetIds.isEmpty() && dogs.isNotEmpty()) {
            val preselected = dogs.find { it.name == petName }
            if (preselected != null) selectedPetIds = setOf(preselected.petId)
        }
    }

    val serviceOptions = listOf("Alojamiento", "Guardería", "Paseo", "Taxi", "Peluquería", "Visitante")

    val parsedPrice = precio.toDoubleOrNull()
    val isInvalidPrice = precio.isNotBlank() && (parsedPrice == null || parsedPrice !in 20.0..6000.0)

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

    val requiresTimeRange = tipoServicio in listOf("Guardería", "Paseo", "Visitante")
    val usesTime = tipoServicio in listOf("Guardería", "Paseo", "Taxi", "Peluquería", "Visitante")
    val timeValidationError = validateServiceTimes(fecha, horaInicio, horaFin.takeIf { requiresTimeRange }, usesTime)

    LaunchedEffect(selectedPetIds, tipoServicio, ubicacion, ubicacionDestino, precio, fecha, fechaSalida, horaInicio, horaFin, tipoPeluqueria) {
        if (showError) showError = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {

        Text("Crear Solicitud de Servicio", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Cuéntanos qué necesita tu mascota", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(24.dp))

        Text(
            "¿Para cuáles de tus mascotas deseas este servicio?",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))

        if (dogs.isEmpty()) {
            Text("No tienes mascotas registradas.", color = MaterialTheme.colorScheme.error)
        } else {
            dogs.forEach { dog ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedPetIds.contains(dog.petId),
                        onCheckedChange = { checked ->
                            selectedPetIds = if (checked) selectedPetIds + dog.petId else selectedPetIds - dog.petId
                        }
                    )
                    Text(dog.name)
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
                isError = showError && tipoServicio.isBlank()
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
            value = precio,
            onValueChange = { if (it.all { char -> char.isDigit() }) precio = it },
            label = { Text("Precio (C$)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = showError && (precio.isBlank() || isInvalidPrice),
            supportingText = if (isInvalidPrice) {{ Text("El precio debe estar entre C$20 y C$6000", color = MaterialTheme.colorScheme.error) }} else null
        )

        Spacer(Modifier.height(12.dp))

        // Handlers compartidos para el campo de ubicación con autocompletado (Nominatim).
        val onUbicacionChange: (String) -> Unit = {
            ubicacion = it
            ubicacionLat = null
            ubicacionLon = null
        }
        val onUbicacionPicked: (NominatimResponse) -> Unit = {
            ubicacion = it.display_name
            ubicacionLat = it.lat.toDoubleOrNull()
            ubicacionLon = it.lon.toDoubleOrNull()
        }

        when (tipoServicio) {
            "Alojamiento" -> {
                Text("Detalles de Alojamiento", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                ServiceDateField(
                    value = fecha,
                    label = "Fecha de entrada",
                    showError = showError && fecha.isBlank(),
                    onClick = { showDatePicker = true }
                )
                Spacer(Modifier.height(12.dp))
                ServiceDateField(
                    value = fechaSalida,
                    label = "Fecha de salida",
                    showError = showError && fechaSalida.isBlank(),
                    onClick = { showExitDatePicker = true }
                )
                Spacer(Modifier.height(12.dp))
                TransportRow(
                    checked = necesitaTransporte,
                    onCheckedChange = { necesitaTransporte = it }
                )
                if (necesitaTransporte) {
                    Spacer(Modifier.height(12.dp))
                    LocationAutocompleteField(
                        query = ubicacion,
                        onQueryChange = onUbicacionChange,
                        onLocationPicked = onUbicacionPicked,
                        label = "Ubicación de recogida",
                        isError = showError && ubicacion.isBlank()
                    )
                }
            }
            "Guardería" -> {
                Text("Detalles de Guardería", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                ServiceDateField(fecha, "Fecha del servicio", showError && fecha.isBlank()) { showDatePicker = true }
                Spacer(Modifier.height(12.dp))
                TimeRangeFields(horaInicio, horaFin, showError, { showStartTimePicker = true }, { showEndTimePicker = true }, "Hora de entrada", "Hora de salida")
                Spacer(Modifier.height(12.dp))
                TransportRow(necesitaTransporte) { necesitaTransporte = it }
                if (necesitaTransporte) {
                    Spacer(Modifier.height(12.dp))
                    LocationAutocompleteField(
                        query = ubicacion,
                        onQueryChange = onUbicacionChange,
                        onLocationPicked = onUbicacionPicked,
                        label = "Ubicación",
                        isError = showError && ubicacion.isBlank()
                    )
                }
            }
            "Paseo" -> {
                Text("Detalles de Paseo", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                LocationAutocompleteField(
                    query = ubicacion,
                    onQueryChange = onUbicacionChange,
                    onLocationPicked = onUbicacionPicked,
                    label = "Ubicación",
                    isError = showError && ubicacion.isBlank()
                )
                Spacer(Modifier.height(12.dp))
                ServiceDateField(fecha, "Fecha", showError && fecha.isBlank()) { showDatePicker = true }
                Spacer(Modifier.height(12.dp))
                TimeRangeFields(horaInicio, horaFin, showError, { showStartTimePicker = true }, { showEndTimePicker = true }, "Hora de inicio", "Hora de fin")
            }
            "Taxi" -> {
                Text("Detalles de Taxi", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                ServiceDateField(fecha, "Fecha", showError && fecha.isBlank()) { showDatePicker = true }
                Spacer(Modifier.height(12.dp))
                LocationAutocompleteField(
                    query = ubicacion,
                    onQueryChange = onUbicacionChange,
                    onLocationPicked = onUbicacionPicked,
                    label = "Dirección de recogida",
                    isError = showError && ubicacion.isBlank()
                )
                Spacer(Modifier.height(12.dp))
                LocationAutocompleteField(
                    query = ubicacionDestino,
                    onQueryChange = { ubicacionDestino = it },
                    onLocationPicked = { ubicacionDestino = it.display_name },
                    label = "Dirección de destino",
                    isError = showError && ubicacionDestino.isBlank()
                )
                Spacer(Modifier.height(12.dp))
                TimeField(horaInicio, "Hora de recogida", showError && horaInicio.isBlank()) { showStartTimePicker = true }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = idaYVuelta, onCheckedChange = { idaYVuelta = it })
                    Text("Ida y vuelta")
                }
            }
            "Peluquería" -> {
                Text("Detalles de Peluquería", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                LocationAutocompleteField(
                    query = ubicacion,
                    onQueryChange = onUbicacionChange,
                    onLocationPicked = onUbicacionPicked,
                    label = "Ubicación",
                    isError = showError && ubicacion.isBlank()
                )
                Spacer(Modifier.height(12.dp))
                ServiceDateField(fecha, "Fecha", showError && fecha.isBlank()) { showDatePicker = true }
                Spacer(Modifier.height(12.dp))
                TimeField(horaInicio, "Hora", showError && horaInicio.isBlank()) { showStartTimePicker = true }
                Spacer(Modifier.height(12.dp))
                ExposedDropdownMenuBox(expanded = expandedGrooming, onExpandedChange = { expandedGrooming = it }) {
                    OutlinedTextField(
                        value = tipoPeluqueria,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de servicio") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        isError = showError && tipoPeluqueria.isBlank()
                    )
                    ExposedDropdownMenu(expanded = expandedGrooming, onDismissRequest = { expandedGrooming = false }, containerColor = Color.White) {
                        listOf("Baño", "Corte", "Ambos").forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = { tipoPeluqueria = option; expandedGrooming = false })
                        }
                    }
                }
            }
            "Visitante" -> {
                Text("Detalles de Visita", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                LocationAutocompleteField(
                    query = ubicacion,
                    onQueryChange = onUbicacionChange,
                    onLocationPicked = onUbicacionPicked,
                    label = "Ubicación",
                    isError = showError && ubicacion.isBlank()
                )
                Spacer(Modifier.height(12.dp))
                ServiceDateField(fecha, "Fecha de inicio", showError && fecha.isBlank()) { showDatePicker = true }
                Spacer(Modifier.height(12.dp))
                TimeRangeFields(horaInicio, horaFin, showError, { showStartTimePicker = true }, { showEndTimePicker = true }, "Hora de inicio", "Hora de fin")
            }
            else -> {
                Text("Selecciona un tipo de servicio para ver sus campos.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
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
                    selectedPetIds.isEmpty() -> errorMessage = "Selecciona al menos una mascota"
                    tipoServicio.isBlank() -> errorMessage = "Selecciona un tipo de servicio"
                    precio.isBlank() -> errorMessage = "Ingresa el precio"
                    isInvalidPrice -> errorMessage = "El precio debe estar entre C$20 y C$6000"
                    !isServiceFormValid(tipoServicio, ubicacion, ubicacionDestino, fecha, fechaSalida, horaInicio, horaFin, necesitaTransporte, tipoPeluqueria) -> errorMessage = "Completa los campos requeridos para $tipoServicio"
                    isPastDate -> errorMessage = "La fecha no puede ser pasada"
                    isBeyondSixMonths -> errorMessage = "La fecha no puede ser mayor a 6 meses"
                    timeValidationError != null -> errorMessage = timeValidationError.orEmpty()
                    else -> {
                        showError = false
                        val selectedNames = dogs.filter { selectedPetIds.contains(it.petId) }.map { it.name }
                        val details = buildServiceDetails(
                            baseDescription = descripcion,
                            serviceType = tipoServicio,
                            pickupLocation = ubicacion,
                            destination = ubicacionDestino,
                            endDate = fechaSalida,
                            needsTransport = necesitaTransporte,
                            roundTrip = idaYVuelta,
                            groomingType = tipoPeluqueria
                        )
                        onPublish(selectedNames, tipoServicio, details, ubicacion, precio, fecha, horaInicio, horaFin, ubicacionLat, ubicacionLon)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isPastDate && !isBeyondSixMonths && !isInvalidPrice && timeValidationError == null
        ) {
            Text("Publicar Solicitud")
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Cancelar")
        }
    }

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

    if (showExitDatePicker) {
        val exitDatePickerState = rememberDatePickerState(
            initialSelectedDateMillis = todayPickerMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis in todayPickerMillis..maxPickerDateMillis
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showExitDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    fechaSalida = exitDatePickerState.selectedDateMillis?.let { millis ->
                        pickerDateFormatter.format(Date(millis))
                    } ?: ""
                    showExitDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showExitDatePicker = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = exitDatePickerState, showModeToggle = false)
        }
    }

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


private fun validateServiceTimes(date: String, startTime: String, endTime: String?, usesTime: Boolean): String? {
    if (!usesTime || date.isBlank() || startTime.isBlank()) return null
    val start = parseServiceDateTime(date, startTime) ?: return null
    if (start <= System.currentTimeMillis()) return "La hora seleccionada ya pasó."
    if (!endTime.isNullOrBlank()) {
        val end = parseServiceDateTime(date, endTime) ?: return null
        if (start >= end) return "La hora de inicio debe ser menor que la hora de salida."
    }
    return null
}

private fun parseServiceDateTime(date: String, time: String): Long? = runCatching {
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).apply { isLenient = false }
        .parse("$date $time")
        ?.time
}.getOrNull()

private fun Calendar.startOfDayMillis(): Long {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
    return timeInMillis
}

@Composable
private fun ServiceDateField(
    value: String,
    label: String,
    showError: Boolean,
    onClick: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        isError = showError,
        trailingIcon = {
            IconButton(onClick = onClick) {
                Icon(Icons.Default.DateRange, contentDescription = null)
            }
        }
    )
}

@Composable
private fun TimeField(
    value: String,
    label: String,
    showError: Boolean,
    onClick: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        isError = showError,
        trailingIcon = {
            IconButton(onClick = onClick) {
                Icon(Icons.Default.AccessTime, null)
            }
        }
    )
}

@Composable
private fun TimeRangeFields(
    start: String,
    end: String,
    showError: Boolean,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
    startLabel: String,
    endLabel: String
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.weight(1f)) {
            TimeField(start, startLabel, showError && start.isBlank(), onStartClick)
        }
        Box(modifier = Modifier.weight(1f)) {
            TimeField(end, endLabel, showError && end.isBlank(), onEndClick)
        }
    }
}

@Composable
private fun TransportRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text("¿Necesita transporte?")
    }
}

private fun isServiceFormValid(
    serviceType: String,
    location: String,
    destination: String,
    date: String,
    endDate: String,
    startTime: String,
    endTime: String,
    needsTransport: Boolean,
    groomingType: String
): Boolean {
    return when (serviceType) {
        "Alojamiento" -> date.isNotBlank() && endDate.isNotBlank() && (!needsTransport || location.isNotBlank())
        "Guardería" -> date.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank() && (!needsTransport || location.isNotBlank())
        "Paseo" -> location.isNotBlank() && date.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank()
        "Taxi" -> location.isNotBlank() && destination.isNotBlank() && date.isNotBlank() && startTime.isNotBlank()
        "Peluquería" -> location.isNotBlank() && date.isNotBlank() && startTime.isNotBlank() && groomingType.isNotBlank()
        "Visitante" -> location.isNotBlank() && date.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank()
        else -> false
    }
}

private fun buildServiceDetails(
    baseDescription: String,
    serviceType: String,
    pickupLocation: String,
    destination: String,
    endDate: String,
    needsTransport: Boolean,
    roundTrip: Boolean,
    groomingType: String
): String {
    val details = mutableListOf<String>()
    if (baseDescription.isNotBlank()) details += baseDescription.trim()
    when (serviceType) {
        "Alojamiento" -> {
            details += "Fecha de salida: $endDate"
            details += "Necesita transporte: ${if (needsTransport) "Sí" else "No"}"
            if (needsTransport && pickupLocation.isNotBlank()) details += "Ubicación de recogida: $pickupLocation"
        }
        "Taxi" -> {
            details += "Dirección de recogida: $pickupLocation"
            details += "Dirección de destino: $destination"
            details += "Ida y vuelta: ${if (roundTrip) "Sí" else "No"}"
        }
        "Peluquería" -> details += "Tipo de peluquería: $groomingType"
        "Guardería" -> {
            details += "Necesita transporte: ${if (needsTransport) "Sí" else "No"}"
            if (needsTransport && pickupLocation.isNotBlank()) details += "Ubicación: $pickupLocation"
        }
    }
    return details.joinToString("\n")
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
