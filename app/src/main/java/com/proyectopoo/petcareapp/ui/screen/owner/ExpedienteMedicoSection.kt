package com.proyectopoo.petcareapp.ui.screen.owner

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.proyectopoo.petcareapp.data.network.ExpedienteEntryDto
import com.proyectopoo.petcareapp.data.network.ExpedienteTipo
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import com.proyectopoo.petcareapp.ui.components.FullScreenImageViewer
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private const val ALERTA_DIAS = 15L

/**
 * Sección "Expediente Médico" del perfil de una mascota (Bloque 11), alojada como una de las
 * pestañas de [DogInfoScreen] cuando se está editando una mascota existente.
 *
 * Lista las entradas agrupadas por [ExpedienteTipo] (GET /api/pets/{petId}/expediente, más
 * reciente primero) y, si [isOwner] es true, permite agregar entradas nuevas (POST multipart,
 * con foto opcional del carnet) y eliminarlas (DELETE).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpedienteMedicoSection(
    petId: Int,
    isOwner: Boolean,
    currentUserId: Int
) {
    var entries by remember(petId) { mutableStateOf<List<ExpedienteEntryDto>>(emptyList()) }
    var isLoading by remember(petId) { mutableStateOf(true) }
    var loadError by remember(petId) { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    var entryPendingDelete by remember { mutableStateOf<ExpedienteEntryDto?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    suspend fun reload() {
        isLoading = true
        val response = runCatching { RetrofitClient.apiService.getExpediente(petId) }.getOrNull()
        if (response?.isSuccessful == true) {
            entries = response.body().orEmpty()
            loadError = false
        } else {
            loadError = true
        }
        isLoading = false
    }

    LaunchedEffect(petId) { reload() }

    val grouped = remember(entries) {
        entries.groupBy { it.tipo }.toList().sortedBy { (tipo, _) -> ExpedienteTipo.ALL.indexOf(tipo).let { if (it < 0) Int.MAX_VALUE else it } }
    }
    val today = remember { LocalDate.now() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isOwner) {
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 16.dp, 16.dp, 8.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Agregar entrada")
                }
            }

            when {
                isLoading && entries.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                loadError && entries.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No se pudo cargar el expediente médico.",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { scope.launch { reload() } }) { Text("Reintentar") }
                    }
                }
                entries.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Todavía no hay entradas en el expediente médico.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        grouped.forEach { (tipo, tipoEntries) ->
                            item(key = "header_$tipo") {
                                Text(
                                    text = ExpedienteTipo.label(tipo),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp, start = 4.dp)
                                )
                            }
                            items(tipoEntries, key = { it.id ?: it.hashCode() }) { entry ->
                                ExpedienteEntryCard(
                                    entry = entry,
                                    today = today,
                                    isOwner = isOwner,
                                    onImageClick = { url -> fullScreenImageUrl = url },
                                    onDeleteClick = { entryPendingDelete = entry }
                                )
                            }
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    fullScreenImageUrl?.let { url ->
        FullScreenImageViewer(imageUrl = url, onDismiss = { fullScreenImageUrl = null })
    }

    entryPendingDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryPendingDelete = null },
            title = { Text("Eliminar entrada") },
            text = { Text("¿Eliminar \"${entry.titulo}\" del expediente médico? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    val id = entry.id
                    entryPendingDelete = null
                    if (id != null) {
                        scope.launch {
                            val response = runCatching {
                                RetrofitClient.apiService.deleteExpedienteEntry(id, currentUserId)
                            }.getOrNull()
                            if (response?.isSuccessful == true) {
                                reload()
                                snackbarHostState.showSnackbar("Entrada eliminada")
                            } else {
                                snackbarHostState.showSnackbar("No se pudo eliminar la entrada")
                            }
                        }
                    }
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { entryPendingDelete = null }) { Text("Cancelar") }
            }
        )
    }

    if (showAddDialog) {
        AddExpedienteEntryDialog(
            petId = petId,
            currentUserId = currentUserId,
            onDismiss = { showAddDialog = false },
            onSaved = {
                showAddDialog = false
                scope.launch {
                    reload()
                    snackbarHostState.showSnackbar("Entrada agregada al expediente")
                }
            }
        )
    }
}

/** true si [fechaProxima] (yyyy-MM-dd) cae dentro de los próximos [ALERTA_DIAS] días (o ya venció). */
private fun isDueSoon(fechaProxima: String?, today: LocalDate): Boolean {
    if (fechaProxima.isNullOrBlank()) return false
    val date = runCatching { LocalDate.parse(fechaProxima) }.getOrNull() ?: return false
    return ChronoUnit.DAYS.between(today, date) <= ALERTA_DIAS
}

private fun iconForTipo(tipo: String): ImageVector = when (tipo) {
    ExpedienteTipo.VACUNA -> Icons.Default.Vaccines
    ExpedienteTipo.DESPARASITACION -> Icons.Default.BugReport
    ExpedienteTipo.ALERGIA -> Icons.Default.Warning
    ExpedienteTipo.MEDICAMENTO -> Icons.Default.Medication
    ExpedienteTipo.CIRUGIA -> Icons.Default.LocalHospital
    ExpedienteTipo.PESO -> Icons.Default.MonitorWeight
    ExpedienteTipo.NOTA -> Icons.AutoMirrored.Filled.Notes
    else -> Icons.AutoMirrored.Filled.Notes
}

@Composable
private fun ExpedienteEntryCard(
    entry: ExpedienteEntryDto,
    today: LocalDate,
    isOwner: Boolean,
    onImageClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    val dueSoon = isDueSoon(entry.fechaProxima, today)
    val imageUrl = remember(entry.imagenCarnetUrl) { RetrofitClient.resolveImageUrl(entry.imagenCarnetUrl) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (dueSoon) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = iconForTipo(entry.tipo),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(entry.titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (dueSoon) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Próximo a vencer",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text("Fecha: ${entry.fecha}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!entry.fechaProxima.isNullOrBlank()) {
                    Text(
                        "Próxima: ${entry.fechaProxima}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (dueSoon) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!entry.descripcion.isNullOrBlank()) {
                    Text(entry.descripcion, style = MaterialTheme.typography.bodyMedium)
                }
                if (!entry.veterinarioNombre.isNullOrBlank()) {
                    Text(
                        "Veterinario: ${entry.veterinarioNombre}${entry.veterinarioTelefono?.let { " · $it" } ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (imageUrl != null) {
                    Spacer(Modifier.height(8.dp))
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Foto del carnet",
                        contentScale = ContentScale.Crop,
                        placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                        error = ColorPainter(MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onImageClick(imageUrl) }
                    )
                }
            }
            if (isOwner) {
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpedienteEntryDialog(
    petId: Int,
    currentUserId: Int,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    var tipo by remember { mutableStateOf(ExpedienteTipo.VACUNA) }
    var tipoExpanded by remember { mutableStateOf(false) }
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var fechaProxima by remember { mutableStateOf("") }
    var vetNombre by remember { mutableStateOf("") }
    var vetTelefono by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var showFechaPicker by remember { mutableStateOf(false) }
    var showFechaProximaPicker by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> if (uri != null) imageUri = uri }

    fun submit() {
        if (titulo.isBlank() || fecha.isBlank()) {
            errorMessage = "Completa el título y la fecha."
            return
        }
        isSaving = true
        errorMessage = null
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
                Text("Agregar entrada al expediente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                ExposedDropdownMenuBox(expanded = tipoExpanded, onExpandedChange = { tipoExpanded = it }) {
                    OutlinedTextField(
                        value = ExpedienteTipo.label(tipo),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        shape = RoundedCornerShape(14.dp)
                    )
                    ExposedDropdownMenu(expanded = tipoExpanded, onDismissRequest = { tipoExpanded = false }) {
                        ExpedienteTipo.ALL.forEach { option ->
                            DropdownMenuItem(text = { Text(ExpedienteTipo.label(option)) }, onClick = { tipo = option; tipoExpanded = false })
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    isError = errorMessage != null && titulo.isBlank()
                )

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = fecha,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    isError = errorMessage != null && fecha.isBlank(),
                    trailingIcon = {
                        IconButton(onClick = { showFechaPicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Elegir fecha")
                        }
                    }
                )

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = fechaProxima,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Próxima fecha (opcional, ej. refuerzo)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    trailingIcon = {
                        Row {
                            if (fechaProxima.isNotBlank()) {
                                IconButton(onClick = { fechaProxima = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Quitar")
                                }
                            }
                            IconButton(onClick = { showFechaProximaPicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Elegir próxima fecha")
                            }
                        }
                    }
                )

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = vetNombre,
                    onValueChange = { vetNombre = it },
                    label = { Text("Veterinario (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = vetTelefono,
                    onValueChange = { vetTelefono = it },
                    label = { Text("Teléfono del veterinario (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(Modifier.height(12.dp))
                Text("Foto del carnet (opcional)", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(6.dp))
                if (imageUri != null) {
                    Box {
                        Image(
                            painter = coil.compose.rememberAsyncImagePainter(imageUri),
                            contentDescription = "Foto seleccionada",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        IconButton(onClick = { imageUri = null }, modifier = Modifier.align(Alignment.TopEnd)) {
                            Icon(Icons.Default.Close, contentDescription = "Quitar imagen")
                        }
                    }
                } else {
                    OutlinedButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Adjuntar foto")
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
                    Button(onClick = { submit() }, enabled = !isSaving) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }

    if (showFechaPicker) {
        SimpleDatePickerDialog(
            onDismiss = { showFechaPicker = false },
            onConfirm = { fecha = it; showFechaPicker = false }
        )
    }
    if (showFechaProximaPicker) {
        SimpleDatePickerDialog(
            onDismiss = { showFechaProximaPicker = false },
            onConfirm = { fechaProxima = it; showFechaProximaPicker = false }
        )
    }

    LaunchedEffect(isSaving) {
        if (!isSaving) return@LaunchedEffect
        val response = runCatching {
            fun String.part() = toRequestBody("text/plain".toMediaTypeOrNull())
            val filePart: MultipartBody.Part? = imageUri?.let { uri ->
                val contentResolver = context.contentResolver
                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    val extension = when {
                        mimeType.contains("png") -> "png"
                        mimeType.contains("webp") -> "webp"
                        else -> "jpg"
                    }
                    val body: RequestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("file", "carnet.$extension", body)
                } else null
            }

            RetrofitClient.apiService.createExpedienteEntry(
                petId = petId,
                usuarioId = currentUserId.toString().part(),
                tipo = tipo.part(),
                titulo = titulo.trim().part(),
                descripcion = descripcion.trim().takeIf { it.isNotBlank() }?.part(),
                fecha = fecha.part(),
                fechaProxima = fechaProxima.takeIf { it.isNotBlank() }?.part(),
                veterinarioNombre = vetNombre.trim().takeIf { it.isNotBlank() }?.part(),
                veterinarioTelefono = vetTelefono.trim().takeIf { it.isNotBlank() }?.part(),
                file = filePart
            )
        }.getOrNull()

        isSaving = false
        if (response?.isSuccessful == true) {
            onSaved()
        } else {
            errorMessage = "No se pudo guardar la entrada. Intenta de nuevo."
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleDatePickerDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val state = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = state.selectedDateMillis
                if (millis != null) {
                    val date = java.time.Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    onConfirm(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                } else {
                    onDismiss()
                }
            }) { Text("Aceptar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    ) {
        DatePicker(state = state, showModeToggle = false)
    }
}
