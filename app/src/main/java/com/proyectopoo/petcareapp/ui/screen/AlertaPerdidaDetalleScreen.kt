package com.proyectopoo.petcareapp.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.proyectopoo.petcareapp.data.network.AlertaPerdidaEstado
import com.proyectopoo.petcareapp.data.network.AvistamientoDto
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import com.proyectopoo.petcareapp.location.getOneShotLocation
import com.proyectopoo.petcareapp.ui.components.FullScreenImageViewer
import com.proyectopoo.petcareapp.util.abrirNavegacion
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * Detalle de una alerta de mascota perdida (Bloque 12): descripción, ubicación (mapa +
 * "Cómo llegar", reutilizando el mismo patrón de navegación externa que ya usa OwnerHomeScreen)
 * y la lista de avistamientos reportados (GET /api/alertas-perdida/{id}/avistamientos).
 * Cualquier usuario puede reportar un avistamiento (comentario + ubicación opcional); solo quien
 * reportó la alerta ve el botón "¡La encontré!".
 *
 * No existe un endpoint para "obtener una alerta por id" en el backend -- solo se devuelve el
 * objeto completo al crearla o al listarlas por cercanía -- así que esta pantalla recibe todos
 * los campos de la alerta como argumentos de navegación (ver [com.proyectopoo.petcareapp.navigation.AlertaPerdidaDetalle])
 * en vez de volver a pedirlos por id.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertaPerdidaDetalleScreen(
    alertaId: Int,
    descripcion: String?,
    latitud: Double,
    longitud: Double,
    direccionTexto: String?,
    estadoInicial: String,
    isReporter: Boolean,
    currentUserId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var estado by remember { mutableStateOf(estadoInicial) }
    var avistamientos by remember { mutableStateOf<List<AvistamientoDto>>(emptyList()) }
    var isLoadingAvistamientos by remember { mutableStateOf(true) }
    var isMarkingFound by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }

    suspend fun reloadAvistamientos() {
        isLoadingAvistamientos = true
        val response = runCatching { RetrofitClient.apiService.getAvistamientos(alertaId) }.getOrNull()
        if (response?.isSuccessful == true) {
            avistamientos = response.body().orEmpty()
        }
        isLoadingAvistamientos = false
    }

    LaunchedEffect(alertaId) { reloadAvistamientos() }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(latitud, longitud), 15f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mascota perdida") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showReportDialog = true },
                icon = { Icon(Icons.Default.AddComment, contentDescription = null) },
                text = { Text("Reportar avistamiento") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    AssistChip(onClick = {}, enabled = false, label = { Text(estadoLabelFor(estado)) })
                    Spacer(Modifier.height(8.dp))
                    Text(
                        descripcion?.takeIf { it.isNotBlank() } ?: "Sin descripción adicional",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            direccionTexto?.takeIf { it.isNotBlank() } ?: "%.5f, %.5f".format(latitud, longitud),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        Marker(state = MarkerState(position = LatLng(latitud, longitud)), title = "Última ubicación conocida")
                    }
                    SmallFloatingActionButton(
                        onClick = {
                            abrirNavegacion(context, latitud, longitud) {
                                scope.launch { snackbarHostState.showSnackbar("No tienes una app de navegación instalada.") }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = "Cómo llegar")
                    }
                }

                if (isReporter && estado == AlertaPerdidaEstado.ACTIVA) {
                    Button(
                        onClick = {
                            if (isMarkingFound) return@Button
                            isMarkingFound = true
                            scope.launch {
                                val response = runCatching {
                                    RetrofitClient.apiService.marcarAlertaEncontrada(alertaId, currentUserId)
                                }.getOrNull()
                                isMarkingFound = false
                                if (response?.isSuccessful == true) {
                                    estado = AlertaPerdidaEstado.ENCONTRADA
                                    snackbarHostState.showSnackbar("¡Qué alegría! Marcamos la alerta como encontrada.")
                                } else {
                                    snackbarHostState.showSnackbar("No se pudo actualizar la alerta.")
                                }
                            }
                        },
                        enabled = !isMarkingFound,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(52.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text(if (isMarkingFound) "Actualizando..." else "¡La encontré!", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    "Avistamientos reportados",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            if (isLoadingAvistamientos) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (avistamientos.isEmpty()) {
                item {
                    Text(
                        "Todavía no hay avistamientos reportados.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                items(avistamientos, key = { it.id ?: it.hashCode() }) { avistamiento ->
                    val imageUrl = remember(avistamiento.imagenUrl) { RetrofitClient.resolveImageUrl(avistamiento.imagenUrl) }
                    ListItem(
                        leadingContent = if (imageUrl != null) {
                            {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Foto del avistamiento",
                                    contentScale = ContentScale.Crop,
                                    placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                                    error = ColorPainter(MaterialTheme.colorScheme.errorContainer),
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { fullScreenImageUrl = imageUrl }
                                )
                            }
                        } else null,
                        headlineContent = { Text(avistamiento.comentario?.takeIf { it.isNotBlank() } ?: "Sin comentario") },
                        supportingContent = {
                            val loc = if (avistamiento.latitud != null && avistamiento.longitud != null) {
                                "%.4f, %.4f".format(avistamiento.latitud, avistamiento.longitud)
                            } else null
                            val reporter = "Usuario #${avistamiento.usuarioId ?: "?"}"
                            Text(listOfNotNull(reporter, loc, avistamiento.fechaCreacion).joinToString(" · "))
                        }
                    )
                }
            }
        }
    }

    fullScreenImageUrl?.let { url ->
        FullScreenImageViewer(imageUrl = url, onDismiss = { fullScreenImageUrl = null })
    }

    if (showReportDialog) {
        ReportarAvistamientoDialog(
            alertaId = alertaId,
            currentUserId = currentUserId,
            onDismiss = { showReportDialog = false },
            onReported = {
                showReportDialog = false
                scope.launch {
                    reloadAvistamientos()
                    snackbarHostState.showSnackbar("Avistamiento reportado. ¡Gracias por ayudar!")
                }
            }
        )
    }
}

private fun estadoLabelFor(estado: String?): String = when (estado) {
    AlertaPerdidaEstado.ACTIVA -> "Activa"
    AlertaPerdidaEstado.ENCONTRADA -> "Encontrada"
    AlertaPerdidaEstado.CERRADA -> "Cerrada"
    else -> estado ?: "?"
}

/** Crea un archivo temporal en cache/avistamientos y devuelve su Uri vía FileProvider, para que la app de cámara escriba ahí. */
private fun crearUriParaCamara(context: android.content.Context): Uri {
    val dir = File(context.cacheDir, "avistamientos").apply { mkdirs() }
    val file = File(dir, "avistamiento_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

/**
 * Formulario "Reportar avistamiento": foto obligatoria (cámara o galería), comentario y
 * ubicación opcional (GPS de una sola vez). Sube la foto en multipart/form-data (Bloque 12).
 */
@Composable
private fun ReportarAvistamientoDialog(
    alertaId: Int,
    currentUserId: Int,
    onDismiss: () -> Unit,
    onReported: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var comentario by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf<Double?>(null) }
    var longitud by remember { mutableStateOf<Double?>(null) }
    var isFetchingLocation by remember { mutableStateOf(false) }
    var includeLocation by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> if (uri != null) fotoUri = uri }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success -> if (success) fotoUri = pendingCameraUri }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = crearUriParaCamara(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            errorMessage = "Se necesita permiso de cámara para tomar la foto."
        }
    }

    LaunchedEffect(includeLocation) {
        if (includeLocation && latitud == null) {
            isFetchingLocation = true
            val location = getOneShotLocation(context)
            if (location != null) {
                latitud = location.latitude
                longitud = location.longitude
            } else {
                includeLocation = false
            }
            isFetchingLocation = false
        }
    }

    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Reportar avistamiento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                Text("Foto (obligatoria)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                if (fotoUri != null) {
                    Box {
                        Image(
                            painter = rememberAsyncImagePainter(fotoUri),
                            contentDescription = "Foto del avistamiento seleccionada",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        IconButton(onClick = { fotoUri = null }, modifier = Modifier.align(Alignment.TopEnd)) {
                            Icon(Icons.Default.Close, contentDescription = "Quitar foto")
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Tomar foto")
                        }
                        OutlinedButton(onClick = { galleryLauncher.launch("image/*") }) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Galería")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    label = { Text("¿Dónde la viste? (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = includeLocation, onCheckedChange = { includeLocation = it })
                    Text("Adjuntar mi ubicación actual")
                }
                if (isFetchingLocation) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Obteniendo ubicación...", style = MaterialTheme.typography.bodySmall)
                    }
                } else if (includeLocation && latitud != null) {
                    Text(
                        "Ubicación: %.4f, %.4f".format(latitud, longitud),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                            val uri = fotoUri
                            if (uri == null) {
                                errorMessage = "Adjunta una foto para reportar el avistamiento."
                                return@Button
                            }
                            if (isSaving) return@Button
                            isSaving = true
                            errorMessage = null
                            scope.launch {
                                fun String.part() = toRequestBody("text/plain".toMediaTypeOrNull())
                                val response = runCatching {
                                    val contentResolver = context.contentResolver
                                    val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                                    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                        ?: throw IllegalStateException("No se pudo leer la foto seleccionada.")
                                    val extension = when {
                                        mimeType.contains("png") -> "png"
                                        mimeType.contains("webp") -> "webp"
                                        else -> "jpg"
                                    }
                                    val fileBody: RequestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                                    val fotoPart = MultipartBody.Part.createFormData("foto", "avistamiento.$extension", fileBody)

                                    RetrofitClient.apiService.reportarAvistamiento(
                                        alertaId = alertaId,
                                        usuarioId = currentUserId.toString().part(),
                                        comentario = comentario.trim().takeIf { it.isNotBlank() }?.part(),
                                        latitud = (if (includeLocation) latitud else null)?.toString()?.part(),
                                        longitud = (if (includeLocation) longitud else null)?.toString()?.part(),
                                        foto = fotoPart
                                    )
                                }.getOrNull()
                                isSaving = false
                                if (response?.isSuccessful == true) {
                                    onReported()
                                } else {
                                    errorMessage = "No se pudo enviar el avistamiento. Intenta de nuevo."
                                }
                            }
                        },
                        enabled = !isSaving && fotoUri != null
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Enviar")
                        }
                    }
                }
            }
        }
    }
}
