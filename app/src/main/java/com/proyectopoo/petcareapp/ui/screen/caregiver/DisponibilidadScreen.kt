package com.proyectopoo.petcareapp.ui.screen.caregiver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.data.network.DisponibilidadCuidadorDto
import com.proyectopoo.petcareapp.data.network.DisponibilidadCuidadorRequest
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import kotlinx.coroutines.launch

private val DIAS_SEMANA = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

/**
 * Pantalla de disponibilidad semanal recurrente del cuidador (Bloque 9): una grilla Lun-Dom,
 * cada día con sus bloques de horario ya publicados y un botón para agregar uno nuevo. Se
 * expande a fechas concretas en `GET /api/calendario` (ver CalendarioScreen.kt).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisponibilidadScreen(
    cuidadorId: Int,
    onBack: () -> Unit
) {
    var bloques by remember { mutableStateOf<List<DisponibilidadCuidadorDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var diaParaAgregar by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    suspend fun reload() {
        isLoading = true
        val response = runCatching { RetrofitClient.apiService.getDisponibilidad(cuidadorId) }.getOrNull()
        bloques = response?.takeIf { it.isSuccessful }?.body().orEmpty()
        isLoading = false
    }

    LaunchedEffect(cuidadorId) { reload() }

    val bloquesPorDia = remember(bloques) { bloques.groupBy { it.diaSemana } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi disponibilidad", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Define los horarios en los que sueles estar disponible cada semana. Se mostrarán en tu calendario.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(DIAS_SEMANA.size) { diaIndex ->
                DiaDisponibilidadCard(
                    nombreDia = DIAS_SEMANA[diaIndex],
                    bloques = bloquesPorDia[diaIndex].orEmpty(),
                    onAgregar = { diaParaAgregar = diaIndex },
                    onEliminar = { bloque ->
                        scope.launch {
                            val id = bloque.id ?: return@launch
                            val response = runCatching {
                                RetrofitClient.apiService.eliminarDisponibilidad(id, cuidadorId)
                            }.getOrNull()
                            if (response?.isSuccessful == true) {
                                reload()
                            } else {
                                snackbarHostState.showSnackbar("No se pudo eliminar el horario")
                            }
                        }
                    }
                )
            }
            item { Spacer(Modifier.height(60.dp)) }
        }
    }

    diaParaAgregar?.let { diaIndex ->
        AgregarBloqueDialog(
            nombreDia = DIAS_SEMANA[diaIndex],
            onDismiss = { diaParaAgregar = null },
            onConfirm = { horaInicio, horaFin ->
                scope.launch {
                    val response = runCatching {
                        RetrofitClient.apiService.crearDisponibilidad(
                            DisponibilidadCuidadorRequest(
                                usuarioId = cuidadorId,
                                diaSemana = diaIndex,
                                horaInicio = horaInicio,
                                horaFin = horaFin
                            )
                        )
                    }.getOrNull()
                    diaParaAgregar = null
                    if (response?.isSuccessful == true) {
                        reload()
                    } else {
                        val error = response?.errorBody()?.string()
                        snackbarHostState.showSnackbar(
                            if (error?.contains("solapa") == true) "Ese horario se solapa con uno existente"
                            else "No se pudo guardar el horario"
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun DiaDisponibilidadCard(
    nombreDia: String,
    bloques: List<DisponibilidadCuidadorDto>,
    onAgregar: () -> Unit,
    onEliminar: (DisponibilidadCuidadorDto) -> Unit
) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(nombreDia, fontWeight = FontWeight.Bold)
                IconButton(onClick = onAgregar) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar horario para $nombreDia")
                }
            }
            if (bloques.isEmpty()) {
                Text(
                    "Sin horarios",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                bloques.forEach { bloque ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${bloque.horaInicio} - ${bloque.horaFin}", style = MaterialTheme.typography.bodyMedium)
                        IconButton(onClick = { onEliminar(bloque) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar horario", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AgregarBloqueDialog(
    nombreDia: String,
    onDismiss: () -> Unit,
    onConfirm: (horaInicio: String, horaFin: String) -> Unit
) {
    val inicioState = rememberTimePickerState(initialHour = 9, initialMinute = 0)
    val finState = rememberTimePickerState(initialHour = 12, initialMinute = 0)
    var mostrarPickerFin by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Disponibilidad — $nombreDia") },
        text = {
            Column {
                Text(if (mostrarPickerFin) "Hora de fin" else "Hora de inicio", fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                if (mostrarPickerFin) {
                    TimePicker(state = finState)
                } else {
                    TimePicker(state = inicioState)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (!mostrarPickerFin) {
                    mostrarPickerFin = true
                } else {
                    val horaInicio = "%02d:%02d".format(inicioState.hour, inicioState.minute)
                    val horaFin = "%02d:%02d".format(finState.hour, finState.minute)
                    onConfirm(horaInicio, horaFin)
                }
            }) {
                Text(if (mostrarPickerFin) "Guardar" else "Siguiente")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
