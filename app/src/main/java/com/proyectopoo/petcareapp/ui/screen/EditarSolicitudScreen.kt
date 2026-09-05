package com.proyectopoo.petcareapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.data.local.entity.PetEntity
import com.proyectopoo.petcareapp.data.local.relation.ServiceRequestDetails
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import com.proyectopoo.petcareapp.data.network.SolicitudEditRequest
import kotlinx.coroutines.launch

/**
 * Edición de una solicitud PENDING: precargada con sus datos actuales.
 * Envía PUT /api/solicitudes/{id} con solo los campos editables.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarSolicitudScreen(
    request: ServiceRequestDetails,
    dogs: List<PetEntity>,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    var title by remember { mutableStateOf(request.title) }
    var description by remember { mutableStateOf(request.description.orEmpty()) }
    var fecha by remember { mutableStateOf(request.requestedDate.orEmpty()) }
    var horaInicio by remember { mutableStateOf(request.startTime.orEmpty()) }
    var horaFin by remember { mutableStateOf(request.endTime.orEmpty()) }
    var selectedPetId by remember { mutableStateOf(request.petId) }
    var tipoServicio by remember { mutableStateOf(request.serviceTypeName ?: "") }

    var expandedService by remember { mutableStateOf(false) }
    var expandedPet by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val serviceOptions = listOf("Alojamiento", "Guardería", "Paseo", "Taxi", "Peluquería", "Visitante")
    val selectedPetName = dogs.find { it.petId == selectedPetId }?.name ?: "Selecciona una mascota"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Editar solicitud") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                minLines = 3,
                maxLines = 5
            )

            if (dogs.isNotEmpty()) {
                ExposedDropdownMenuBox(expanded = expandedPet, onExpandedChange = { expandedPet = !expandedPet }) {
                    OutlinedTextField(
                        value = selectedPetName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mascota") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(14.dp)
                    )
                    ExposedDropdownMenu(expanded = expandedPet, onDismissRequest = { expandedPet = false }) {
                        dogs.forEach { dog ->
                            DropdownMenuItem(
                                text = { Text(dog.name) },
                                onClick = {
                                    selectedPetId = dog.petId
                                    expandedPet = false
                                }
                            )
                        }
                    }
                }
            }

            ExposedDropdownMenuBox(expanded = expandedService, onExpandedChange = { expandedService = !expandedService }) {
                OutlinedTextField(
                    value = tipoServicio,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de servicio") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(14.dp)
                )
                ExposedDropdownMenu(expanded = expandedService, onDismissRequest = { expandedService = false }) {
                    serviceOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                tipoServicio = option
                                expandedService = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text("Fecha (dd/MM/yyyy)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = horaInicio,
                    onValueChange = { horaInicio = it },
                    label = { Text("Hora inicio (HH:mm)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value = horaFin,
                    onValueChange = { horaFin = it },
                    label = { Text("Hora fin (HH:mm)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                )
            }

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    if (isSaving) return@Button
                    scope.launch {
                        isSaving = true
                        errorMessage = null
                        val body = SolicitudEditRequest(
                            title = title.trim().ifBlank { null },
                            description = description.trim().ifBlank { null },
                            requestedDate = fecha.trim().ifBlank { null },
                            startTime = horaInicio.trim().ifBlank { null },
                            endTime = horaFin.trim().ifBlank { null },
                            petId = selectedPetId,
                            serviceTypeId = serviceTypeIdForEdit(tipoServicio)
                        )
                        try {
                            val response = RetrofitClient.apiService.editarSolicitud(request.serviceRequestId, body)
                            if (response.isSuccessful) {
                                onSaved()
                            } else {
                                errorMessage = "No se pudo guardar los cambios."
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error de conexión. Intenta de nuevo."
                        } finally {
                            isSaving = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                enabled = !isSaving
            ) {
                Text(if (isSaving) "Guardando..." else "Guardar cambios", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Cancelar")
            }
        }
    }
}

private fun serviceTypeIdForEdit(serviceTypeName: String): Int? {
    if (serviceTypeName.isBlank()) return null
    return when (serviceTypeName.lowercase()) {
        "alojamiento" -> 1
        "guardería", "guarderia" -> 2
        "paseo" -> 3
        "taxi" -> 4
        "peluquería", "peluqueria" -> 5
        "visitante" -> 6
        else -> null
    }
}
