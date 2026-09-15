package com.proyectopoo.petcareapp.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.proyectopoo.petcareapp.data.network.AlertaPerdidaDto
import com.proyectopoo.petcareapp.data.network.AlertaPerdidaRequest
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import com.proyectopoo.petcareapp.location.getOneShotLocation
import kotlinx.coroutines.launch

/**
 * Formulario "Mi mascota se perdió" (Bloque 12), alojado en el perfil de la mascota
 * ([DogInfoScreen][com.proyectopoo.petcareapp.ui.screen.owner.DogInfoScreen], solo dueño).
 *
 * Al abrirse pide el permiso de ubicación (si aún no está concedido) y obtiene una posición GPS
 * de una sola vez ([getOneShotLocation], no un reporte continuo como [LocationReporter]).
 * `direccion_texto` es un campo de texto libre opcional -- este proyecto no tiene hoy geocodificación
 * inversa (Nominatim, vía [com.proyectopoo.petcareapp.data.NominatimClient], solo busca
 * dirección→coordenadas, no al revés), así que no se intenta resolver la dirección
 * automáticamente a partir del GPS; queda como una mejora futura.
 */
@Composable
fun ReportarMascotaPerdidaDialog(
    petName: String,
    petId: Int,
    currentUserId: Int,
    onDismiss: () -> Unit,
    onReported: (AlertaPerdidaDto) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var descripcion by remember { mutableStateOf("") }
    var direccionTexto by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf<Double?>(null) }
    var longitud by remember { mutableStateOf<Double?>(null) }
    var isFetchingLocation by remember { mutableStateOf(true) }
    var locationError by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    suspend fun fetchLocation() {
        isFetchingLocation = true
        locationError = false
        val location = getOneShotLocation(context)
        if (location != null) {
            latitud = location.latitude
            longitud = location.longitude
        } else {
            locationError = true
        }
        isFetchingLocation = false
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch { fetchLocation() }
        } else {
            isFetchingLocation = false
            locationError = true
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("🚨 Reportar a $petName como perdida", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Se notificará automáticamente a usuarios cercanos a tu ubicación actual.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    placeholder = { Text("Ej. collar rojo, se asusta con ruidos fuertes...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = direccionTexto,
                    onValueChange = { direccionTexto = it },
                    label = { Text("Referencia de la dirección (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    when {
                        isFetchingLocation -> {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Obteniendo tu ubicación actual...", style = MaterialTheme.typography.bodySmall)
                        }
                        latitud != null && longitud != null -> {
                            Text(
                                "Ubicación obtenida (%.4f, %.4f)".format(latitud, longitud),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        else -> {
                            Column {
                                Text(
                                    "No se pudo obtener tu ubicación. Activa el permiso de ubicación e intenta de nuevo.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                TextButton(onClick = {
                                    scope.launch { fetchLocation() }
                                }) { Text("Reintentar") }
                            }
                        }
                    }
                }

                errorMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val lat = latitud
                            val lng = longitud
                            if (lat == null || lng == null) {
                                errorMessage = "Necesitamos tu ubicación para publicar la alerta."
                                return@Button
                            }
                            if (isSaving) return@Button
                            isSaving = true
                            errorMessage = null
                            scope.launch {
                                val response = runCatching {
                                    RetrofitClient.apiService.crearAlertaPerdida(
                                        AlertaPerdidaRequest(
                                            petsId = petId,
                                            usuarioId = currentUserId,
                                            descripcion = descripcion.trim().takeIf { it.isNotBlank() },
                                            latitud = lat,
                                            longitud = lng,
                                            direccionTexto = direccionTexto.trim().takeIf { it.isNotBlank() }
                                        )
                                    )
                                }.getOrNull()
                                isSaving = false
                                val body = response?.takeIf { it.isSuccessful }?.body()
                                if (body != null) {
                                    onReported(body)
                                } else {
                                    errorMessage = "No se pudo publicar la alerta. Intenta de nuevo."
                                }
                            }
                        },
                        enabled = !isSaving && latitud != null && longitud != null,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Publicar alerta")
                        }
                    }
                }
            }
        }
    }
}
