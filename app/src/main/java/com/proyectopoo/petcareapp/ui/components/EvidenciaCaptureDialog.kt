package com.proyectopoo.petcareapp.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.proyectopoo.petcareapp.data.local.database.PetCareDatabase
import com.proyectopoo.petcareapp.data.local.entity.EvidenciaLocalEntity
import com.proyectopoo.petcareapp.data.network.EvidenciaDto
import com.proyectopoo.petcareapp.data.network.EvidenciaTipo
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Diálogo prominente para capturar la foto de evidencia antes/después de un servicio (Bloque 8).
 *
 * Se muestra en dos puntos del flujo del cuidador (ver `CaregiverHomeScreen.kt`):
 *  - [EvidenciaTipo.ANTES]: justo después de aceptar una solicitud (transición a ACCEPTED, el
 *    punto en este proyecto que representa "el cuidador empieza el servicio").
 *  - [EvidenciaTipo.DESPUES]: al tocar "Finalizar" en un servicio activo, antes de abrir el
 *    diálogo de calificación.
 *
 * El backend NUNCA bloquea la transición de estado por falta de evidencia ("excepción
 * flexible": ver contrato de Bloque 8), así que este diálogo tampoco lo hace: [onFinished] se
 * invoca siempre (foto subida con éxito, foto guardada en la cola local para reintentar, u
 * "omitir por ahora"), dejando que la acción subyacente (aceptar/finalizar) siga su curso.
 */
@Composable
fun EvidenciaCaptureDialog(
    serviceRequestId: Int,
    tipo: String,
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = remember { PetCareDatabase.getDatabase(context).evidenciaLocalDao() }

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var nota by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> if (uri != null) selectedUri = uri }

    val isAntes = tipo == EvidenciaTipo.ANTES
    val title = if (isAntes) "Foto antes de comenzar" else "Foto al finalizar"
    val subtitle = if (isAntes) {
        "Sube una foto de la mascota antes de comenzar el servicio."
    } else {
        "Sube una foto de la mascota al terminar el servicio."
    }
    val notaPlaceholder = if (isAntes) {
        "Ej. Estado en el que se recibe la mascota"
    } else {
        "Ej. Dueño no presente, mascota entregada a..."
    }

    Dialog(onDismissRequest = { /* interacción explícita requerida: guardar u omitir */ }) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                val currentUri = selectedUri
                if (currentUri != null) {
                    AsyncImage(
                        model = currentUri,
                        contentDescription = "Foto seleccionada",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cambiar foto")
                    }
                } else {
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Elegir foto")
                    }
                }

                OutlinedTextField(
                    value = nota,
                    onValueChange = { nota = it },
                    label = { Text("Nota (opcional)") },
                    placeholder = { Text(notaPlaceholder) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                statusMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = {
                        val uri = currentUri ?: return@Button
                        scope.launch {
                            isSaving = true
                            statusMessage = null
                            val uploaded = uploadEvidencia(context, serviceRequestId, tipo, uri, nota)
                            if (!uploaded) {
                                dao.insertar(
                                    EvidenciaLocalEntity(
                                        id = EvidenciaLocalEntity.nuevoIdLocal(),
                                        solicitudId = serviceRequestId,
                                        tipo = tipo,
                                        imagenUrl = uri.toString(),
                                        nota = nota.trim().ifBlank { null },
                                        latitud = null,
                                        longitud = null,
                                        fecha = System.currentTimeMillis(),
                                        enviado = false
                                    )
                                )
                            }
                            isSaving = false
                            onFinished()
                        }
                    },
                    enabled = currentUri != null && !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.height(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Guardar evidencia", fontWeight = FontWeight.Bold)
                    }
                }

                TextButton(
                    onClick = onFinished,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text("Omitir por ahora")
                }
            }
        }
    }
}

/**
 * Intenta subir la evidencia de inmediato. Devuelve `false` (sin lanzar) ante cualquier fallo de
 * red/servidor, para que el llamador la encole localmente en vez de perderla.
 */
private suspend fun uploadEvidencia(
    context: Context,
    serviceRequestId: Int,
    tipo: String,
    uri: Uri,
    nota: String
): Boolean {
    return runCatching {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return false
        val extension = when {
            mimeType.contains("png") -> "png"
            mimeType.contains("gif") -> "gif"
            mimeType.contains("webp") -> "webp"
            else -> "jpg"
        }
        val fileBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", "evidencia_$tipo.$extension", fileBody)
        val tipoBody = tipo.toRequestBody("text/plain".toMediaTypeOrNull())
        val notaBody: RequestBody? = nota.trim().ifBlank { null }?.toRequestBody("text/plain".toMediaTypeOrNull())
        val response = RetrofitClient.apiService.subirEvidencia(
            id = serviceRequestId,
            tipo = tipoBody,
            file = filePart,
            nota = notaBody
        )
        response.isSuccessful
    }.getOrDefault(false)
}

/**
 * Reintenta toda la evidencia pendiente (encolada por falta de red en [EvidenciaCaptureDialog])
 * en cuanto haya conexión disponible. Análogo a `retryPendingMessages()` en `ChatScreen.kt`
 * (Bloque 5), pero global (no está atado a una sola solicitud abierta en pantalla) porque no hay
 * una pantalla de evidencia persistente: se llama desde el ciclo de refresco de
 * `CaregiverHomeScreen.kt`.
 */
suspend fun retryPendingEvidencias(context: Context) {
    val dao = PetCareDatabase.getDatabase(context).evidenciaLocalDao()
    val pending = dao.obtenerTodosPendientes()
    for (entity in pending) {
        val localUri = entity.imagenUrl?.takeIf {
            it.startsWith("content://") || it.startsWith("file://")
        } ?: continue
        val uploaded = uploadEvidencia(
            context = context,
            serviceRequestId = entity.solicitudId,
            tipo = entity.tipo,
            uri = Uri.parse(localUri),
            nota = entity.nota.orEmpty()
        )
        if (uploaded) dao.eliminarPorId(entity.id)
    }
}

/**
 * Combina la evidencia remota ([EvidenciaDto], ya sincronizada) con la cola local pendiente
 * ([EvidenciaLocalEntity], aún no subida) de una solicitud, para mostrarla de inmediato aunque
 * la subida todavía no haya terminado. Devuelve la más reciente de cada [EvidenciaTipo].
 */
suspend fun cargarEvidenciaPorTipo(context: Context, serviceRequestId: Int): Map<String, EvidenciaThumbnail> {
    val dao = PetCareDatabase.getDatabase(context).evidenciaLocalDao()
    val local = dao.obtenerPorSolicitud(serviceRequestId)
    val remote = runCatching {
        RetrofitClient.apiService.getEvidencias(serviceRequestId).takeIf { it.isSuccessful }?.body()
    }.getOrNull().orEmpty()

    val result = linkedMapOf<String, EvidenciaThumbnail>()
    remote.forEach { dto ->
        val url = RetrofitClient.resolveImageUrl(dto.imagenUrl)
        if (url != null) {
            result[dto.tipo] = EvidenciaThumbnail(imageUrl = url, nota = dto.nota, pending = false)
        }
    }
    // La cola local pendiente (sin subir aún) tiene prioridad visual: si existe, es la más
    // reciente para ese tipo (recién capturada, todavía no reflejada por el servidor).
    local.filter { !it.enviado }.forEach { entity ->
        val url = entity.imagenUrl
        if (url != null) {
            result[entity.tipo] = EvidenciaThumbnail(imageUrl = url, nota = entity.nota, pending = true)
        }
    }
    return result
}

data class EvidenciaThumbnail(
    val imageUrl: String,
    val nota: String?,
    val pending: Boolean
)
