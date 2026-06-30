package com.proyectopoo.petcareapp.ui.screen.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Column(modifier = Modifier.padding(start = 4.dp)) {
                Text("Crear Solicitud de Servicio", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Cuéntanos qué necesita tu mascota", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(18.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

        Text(
            "¿Para cuáles de tus mascotas deseas este servicio?",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        if (dogs.isEmpty()) {
            Text("No tienes mascotas registradas.", color = MaterialTheme.colorScheme.error)
        } else {
            dogs.forEach { dog ->
                val checked = selectedPetIds.contains(dog.petId)
                Card(
                    onClick = { selectedPetIds = if (checked) selectedPetIds - dog.petId else selectedPetIds + dog.petId },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.width(4.dp))
                        Text(dog.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Normal)
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { value -> selectedPetIds = if (value) selectedPetIds + dog.petId else selectedPetIds - dog.petId },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                    }
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
                leadingIcon = { Icon(serviceIconForCreate(tipoServicio), null) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(14.dp),
                trailingIcon = { IconButton(onClick = { expandedService = !expandedService }) { Icon(Icons.Default.ArrowDropDown, null) } },
                isError = showError && tipoServicio.isBlank()
            )
            ExposedDropdownMenu(
                expanded = expandedService,
                onDismissRequest = { expandedService = false },
                containerColor = MaterialTheme.colorScheme.surface
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
            leadingIcon = { Icon(Icons.Default.Edit, null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            minLines = 3,
            maxLines = 5
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = precio,
            onValueChange = { if (it.all { char -> char.isDigit() }) precio = it },
            label = { Text("Precio (C$)") },
            leadingIcon = { Icon(Icons.Default.Payments, null) },
            shape = RoundedCornerShape(14.dp),
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
                SectionHeaderCreate(Icons.Default.Home, "Detalles de Alojamiento")
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
                SectionHeaderCreate(Icons.Default.WbSunny, "Detalles de Guardería")
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
                SectionHeaderCreate(Icons.Default.DirectionsWalk, "Detalles de Paseo")
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
                SectionHeaderCreate(Icons.Default.LocalTaxi, "Detalles de Taxi")
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
                SectionHeaderCreate(Icons.Default.ContentCut, "Detalles de Peluquería")
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
                    ExposedDropdownMenu(expanded = expandedGrooming, onDismissRequest = { expandedGrooming = false }, containerColor = MaterialTheme.colorScheme.surface) {
                        listOf("Baño", "Corte", "Ambos").forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = { tipoPeluqueria = option; expandedGrooming = false })
                        }
                    }
                }
            }
            "Visitante" -> {
                SectionHeaderCreate(Icons.Default.HomeRepairService, "Detalles de Visita")
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
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = !isPastDate && !isBeyondSixMonths && !isInvalidPrice && timeValidationError == null
        ) {
            Text("Publicar Solicitud", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("Cancelar", fontWeight = FontWeight.Bold)
        }
                }
            }
            Spacer(Modifier.height(20.dp))
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
                containerColor = MaterialTheme.colorScheme.surface,
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
                    containerColor = MaterialTheme.colorScheme.surface,
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


@Composable
private fun SectionHeaderCreate(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

private fun serviceIconForCreate(service: String): androidx.compose.ui.graphics.vector.ImageVector = when (service) {
    "Alojamiento" -> Icons.Default.Home
    "Guardería" -> Icons.Default.WbSunny
    "Paseo" -> Icons.Default.DirectionsWalk
    "Taxi" -> Icons.Default.LocalTaxi
    "Peluquería" -> Icons.Default.ContentCut
    "Visitante" -> Icons.Default.HomeRepairService
    else -> Icons.Default.Pets
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
        shape = RoundedCornerShape(14.dp),
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
        shape = RoundedCornerShape(14.dp),
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
        containerColor = MaterialTheme.colorScheme.surface,
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
