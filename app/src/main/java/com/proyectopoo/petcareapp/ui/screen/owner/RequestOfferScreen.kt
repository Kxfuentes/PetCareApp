package com.proyectopoo.petcareapp.ui.screen.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.data.local.entity.PetEntity
import com.proyectopoo.petcareapp.data.local.relation.OfferedServiceDetails
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestOfferScreen(
    offer: OfferedServiceDetails,
    dogs: List<PetEntity>,
    onBack: () -> Unit,
    onSubmit: (
        petIds: List<Int>,
        date: String,
        startTime: String,
        notes: String
    ) -> Unit
) {
    var selectedPetIds by remember { mutableStateOf(setOf<Int>()) }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val currentLocale = remember { Locale.getDefault() }
    val pickerDateFormatter = remember(currentLocale) {
        SimpleDateFormat("dd/MM/yyyy", currentLocale).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    val todayPickerMillis = remember { Calendar.getInstance(TimeZone.getTimeZone("UTC")).startOfDayMillis() }
    val maxPickerDateMillis = remember {
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { add(Calendar.MONTH, 6) }.startOfDayMillis()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitar este servicio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        offer.serviceTypeName ?: offer.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        "Cuidador: ${offer.caregiverName ?: "Disponible"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                    Text(
                        "Precio publicado: C$${"%.2f".format(offer.price)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }
            }

            OutlinedTextField(
                value = fecha,
                onValueChange = {},
                label = { Text("Fecha deseada") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                isError = showError && fecha.isBlank(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.LightGray
                ),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.DarkGray)
                    }
                }
            )

            OutlinedTextField(
                value = hora,
                onValueChange = {},
                label = { Text("Hora deseada") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                isError = showError && hora.isBlank(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.LightGray
                ),
                trailingIcon = {
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.DarkGray)
                    }
                }
            )

            Text(
                "¿Para cuáles de tus mascotas deseas este servicio?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            if (dogs.isEmpty()) {
                Text(
                    "No tienes mascotas registradas. Agrega una mascota antes de solicitar.",
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                dogs.forEach { dog ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedPetIds.contains(dog.petId),
                            onCheckedChange = { checked ->
                                selectedPetIds = if (checked) {
                                    selectedPetIds + dog.petId
                                } else {
                                    selectedPetIds - dog.petId
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = Color.DarkGray)
                        )
                        Text(dog.name, style = MaterialTheme.typography.bodyLarge, color = Color.Black)
                    }
                }
            }

            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text("Notas adicionales (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            if (showError) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    showError = true
                    when {
                        fecha.isBlank() -> errorMessage = "Selecciona una fecha"
                        hora.isBlank() -> errorMessage = "Selecciona una hora"
                        selectedPetIds.isEmpty() -> errorMessage = "Selecciona al menos una mascota"
                        else -> {
                            showError = false
                            onSubmit(selectedPetIds.toList(), fecha, hora, notas)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = dogs.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037))
            ) {
                Text("Enviar solicitud de reserva", fontWeight = FontWeight.Bold, color = Color.White)
            }
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
                }) { Text("Aceptar", color = Color.Black) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = Color.Black) } },
            colors = DatePickerDefaults.colors(
                containerColor = Color.White,
                titleContentColor = Color.Black,
                headlineContentColor = Color.Black,
                weekdayContentColor = Color.Black,
                subheadContentColor = Color.Black,
                yearContentColor = Color.Black,
                currentYearContentColor = Color.Black,
                selectedYearContentColor = Color.White,
                selectedYearContainerColor = Color(0xFF5D4037),
                todayContentColor = Color(0xFF5D4037),
                todayDateBorderColor = Color(0xFF5D4037),
                dayContentColor = Color.Black,
                selectedDayContentColor = Color.White,
                selectedDayContainerColor = Color(0xFF5D4037),
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
                    selectedDayContainerColor = Color(0xFF5D4037),
                    todayContentColor = Color(0xFF5D4037),
                    todayDateBorderColor = Color(0xFF5D4037)
                )
            )
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { timeState ->
                hora = String.format("%02d:%02d", timeState.hour, timeState.minute)
                showTimePicker = false
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
        confirmButton = { TextButton(onClick = { onConfirm(timeState) }) { Text("Aceptar", color = Color.Black) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Black) } },
        containerColor = Color.White,
        text = {
            TimePicker(
                state = timeState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = Color.White,
                    selectorColor = Color(0xFF5D4037),
                    clockDialSelectedContentColor = Color.White,
                    clockDialUnselectedContentColor = Color.Black
                )
            )
        }
    )
}
