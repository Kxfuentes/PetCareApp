package com.proyectopoo.petcareapp.ui.screen.chat

import android.net.ConnectivityManager
import android.net.Network
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.proyectopoo.petcareapp.data.local.database.PetCareDatabase
import com.proyectopoo.petcareapp.data.local.entity.MensajeLocalEntity
import com.proyectopoo.petcareapp.data.network.ChatMessageDto
import com.proyectopoo.petcareapp.data.network.ChatMessageRequest
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import com.proyectopoo.petcareapp.ui.components.FullScreenImageViewer
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Modelo de UI que unifica los mensajes que vienen del servidor ([ChatMessageDto]) con los
 * mensajes cacheados/pendientes en Room ([MensajeLocalEntity]), para poder pintar la lista de
 * inmediato desde la cache offline y luego reconciliarla con la respuesta del servidor.
 */
private data class ChatDisplayMessage(
    val localId: Long?,
    val serverId: Int?,
    val senderId: Int,
    val receiverId: Int,
    val message: String,
    val imageUrl: String?,
    val isRead: Boolean,
    val enviado: Boolean,
    val fecha: Long
)

private fun ChatMessageDto.toDisplay(): ChatDisplayMessage = ChatDisplayMessage(
    localId = null,
    serverId = id,
    senderId = senderId,
    receiverId = receiverId,
    message = message,
    imageUrl = imageUrl,
    isRead = isRead,
    enviado = true,
    fecha = parseChatDate(createdAt)
)

private fun MensajeLocalEntity.toDisplay(): ChatDisplayMessage = ChatDisplayMessage(
    localId = id,
    serverId = if (id > 0) id.toInt() else null,
    senderId = emisorId,
    receiverId = receptorId,
    message = mensaje,
    imageUrl = imagenUrl,
    isRead = leido,
    enviado = enviado,
    fecha = fecha
)

/** Fila de cache para un mensaje ya confirmado por el servidor (id positivo = id del backend). */
private fun ChatMessageDto.toEntity(): MensajeLocalEntity? {
    val serverId = id ?: return null
    return MensajeLocalEntity(
        id = serverId.toLong(),
        solicitudId = serviceRequestId,
        emisorId = senderId,
        receptorId = receiverId,
        mensaje = message,
        imagenUrl = imageUrl,
        fecha = parseChatDate(createdAt),
        enviado = true,
        leido = isRead
    )
}

/** Intenta parsear la fecha ISO-8601 que manda el backend; si falla, usa la hora actual. */
private fun parseChatDate(createdAt: String?): Long {
    if (createdAt.isNullOrBlank()) return System.currentTimeMillis()
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss"
    )
    for (pattern in patterns) {
        runCatching {
            java.text.SimpleDateFormat(pattern, java.util.Locale.US).parse(createdAt)?.time
        }.getOrNull()?.let { return it }
    }
    return System.currentTimeMillis()
}

/**
 * Combina la cache local (mensajes pendientes + confirmados) con la ultima respuesta del
 * servidor. Ante un id en comun, gana el servidor (esta mas actualizado, por ejemplo el estado
 * de "leido"). Los mensajes locales sin id de servidor (pendientes de envio, id negativo) se
 * conservan tal cual.
 */
private fun mergeMessages(
    local: List<MensajeLocalEntity>,
    server: List<ChatMessageDto>
): List<ChatDisplayMessage> {
    val merged = LinkedHashMap<String, ChatDisplayMessage>()
    for (entity in local) {
        val key = if (entity.id > 0) "s:${entity.id}" else "l:${entity.id}"
        merged[key] = entity.toDisplay()
    }
    for (dto in server) {
        val key = if (dto.id != null) "s:${dto.id}" else "l:${dto.hashCode()}"
        merged[key] = dto.toDisplay()
    }
    return merged.values.sortedWith(compareBy({ it.fecha }, { it.serverId ?: Int.MAX_VALUE }))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    serviceRequestId: Int,
    currentUserId: Int,
    otherUserId: Int,
    otherUserName: String,
    refreshTick: Int,
    onBack: () -> Unit,
    petId: Int = -1,
    onViewExpediente: (Int) -> Unit = {}
) {
    var displayMessages by remember { mutableStateOf<List<ChatDisplayMessage>>(emptyList()) }
    var draft by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dao = remember { PetCareDatabase.getDatabase(context).mensajeLocalDao() }

    suspend fun refreshFromCache() {
        displayMessages = mergeMessages(dao.obtenerPorSolicitud(serviceRequestId), emptyList())
    }

    suspend fun reload() {
        // Mostrar de inmediato lo que ya tenemos en cache, sin esperar a la red.
        val cachedLocal = dao.obtenerPorSolicitud(serviceRequestId)
        if (displayMessages.isEmpty() && cachedLocal.isNotEmpty()) {
            displayMessages = mergeMessages(cachedLocal, emptyList())
        }

        val result = runCatching { RetrofitClient.apiService.getChatMessages(serviceRequestId) }.getOrNull()
        if (result?.isSuccessful == true) {
            val serverMessages = result.body().orEmpty()
            // Upsert de los mensajes del servidor en Room para mantener la cache al dia.
            val entities = serverMessages.mapNotNull { it.toEntity() }
            if (entities.isNotEmpty()) dao.insertarTodos(entities)
            val refreshedLocal = dao.obtenerPorSolicitud(serviceRequestId)
            displayMessages = mergeMessages(refreshedLocal, serverMessages)
        }
        runCatching { RetrofitClient.apiService.markChatMessagesRead(serviceRequestId, currentUserId) }
    }

    suspend fun resendText(entity: MensajeLocalEntity): Boolean {
        val response = runCatching {
            RetrofitClient.apiService.sendChatMessage(
                ChatMessageRequest(
                    serviceRequestId = entity.solicitudId,
                    senderId = entity.emisorId,
                    receiverId = entity.receptorId,
                    message = entity.mensaje
                )
            )
        }.getOrNull()
        if (response?.isSuccessful == true) {
            response.body()?.toEntity()?.let { dao.insertar(it) }
            return true
        }
        return false
    }

    suspend fun resendImage(entity: MensajeLocalEntity, localUri: String): Boolean {
        return runCatching {
            val uri = Uri.parse(localUri)
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
            val filePart = MultipartBody.Part.createFormData("file", "chat_image.$extension", fileBody)
            val response = RetrofitClient.apiService.sendChatImage(
                serviceRequestId = entity.solicitudId,
                senderId = entity.emisorId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                receiverId = entity.receptorId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                message = entity.mensaje.toRequestBody("text/plain".toMediaTypeOrNull()),
                file = filePart
            )
            if (response.isSuccessful) {
                response.body()?.toEntity()?.let { dao.insertar(it) }
                true
            } else {
                false
            }
        }.getOrDefault(false)
    }

    // "Cola de reintento": reenvia los mensajes que quedaron marcados como no-enviados
    // (enviado = false) para esta conversacion, por ejemplo tras recuperar la conexion.
    suspend fun retryPendingMessages() {
        val pending = dao.obtenerPendientesPorSolicitud(serviceRequestId)
        if (pending.isEmpty()) return
        var anySucceeded = false
        for (entity in pending) {
            val localImageUri = entity.imagenUrl?.takeIf {
                it.startsWith("content://") || it.startsWith("file://")
            }
            val success = if (localImageUri != null) {
                resendImage(entity, localImageUri)
            } else {
                resendText(entity)
            }
            if (success) {
                dao.eliminarPorId(entity.id)
                anySucceeded = true
            }
        }
        if (anySucceeded) reload() else refreshFromCache()
    }

    LaunchedEffect(serviceRequestId) {
        reload()
        retryPendingMessages()
    }
    // Un evento de WebSocket (CHAT_MESSAGE) sube el tick global; si toca esta conversacion, recargamos.
    LaunchedEffect(refreshTick) { if (refreshTick > 0) reload() }

    LaunchedEffect(displayMessages.size) {
        if (displayMessages.isNotEmpty()) listState.animateScrollToItem(displayMessages.lastIndex)
    }

    // Reintenta la cola de mensajes pendientes en cuanto el dispositivo recupera conectividad.
    DisposableEffect(serviceRequestId) {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                scope.launch { retryPendingMessages() }
            }
        }
        runCatching { connectivityManager?.registerDefaultNetworkCallback(callback) }
        onDispose {
            runCatching { connectivityManager?.unregisterNetworkCallback(callback) }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isUploadingImage = true
            uploadError = null
            val caption = draft.trim()
            val result = runCatching {
                val contentResolver = context.contentResolver
                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IllegalStateException("No se pudo leer la imagen seleccionada.")
                val extension = when {
                    mimeType.contains("png") -> "png"
                    mimeType.contains("gif") -> "gif"
                    mimeType.contains("webp") -> "webp"
                    else -> "jpg"
                }
                val fileBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", "chat_image.$extension", fileBody)
                RetrofitClient.apiService.sendChatImage(
                    serviceRequestId = serviceRequestId,
                    senderId = currentUserId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    receiverId = otherUserId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    message = caption.toRequestBody("text/plain".toMediaTypeOrNull()),
                    file = filePart
                )
            }

            val response = result.getOrNull()
            if (response?.isSuccessful == true) {
                response.body()?.let { dto ->
                    dto.toEntity()?.let { dao.insertar(it) }
                    displayMessages = mergeMessages(dao.obtenerPorSolicitud(serviceRequestId), listOf(dto))
                }
                draft = ""
            } else {
                // Sin red o fallo del servidor: guardamos el mensaje como pendiente (cola de
                // reintento) en vez de perderlo, y lo mostramos de inmediato (UI optimista).
                val pendingEntity = MensajeLocalEntity(
                    id = MensajeLocalEntity.nuevoIdLocal(),
                    solicitudId = serviceRequestId,
                    emisorId = currentUserId,
                    receptorId = otherUserId,
                    mensaje = caption,
                    imagenUrl = uri.toString(),
                    fecha = System.currentTimeMillis(),
                    enviado = false,
                    leido = false
                )
                dao.insertar(pendingEntity)
                displayMessages = displayMessages + pendingEntity.toDisplay()
                uploadError = "No se pudo enviar la imagen. Se reintentara cuando haya conexion."
                draft = ""
            }
            isUploadingImage = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(otherUserName.ifBlank { "Chat" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (petId > 0) {
                        IconButton(onClick = { onViewExpediente(petId) }) {
                            Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = "Ver expediente médico")
                        }
                    }
                }
            )
        },
        bottomBar = {
            Column {
                uploadError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        enabled = !isSending && !isUploadingImage,
                        onClick = { imagePickerLauncher.launch("image/*") }
                    ) {
                        if (isUploadingImage) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.AttachFile, contentDescription = "Adjuntar imagen")
                        }
                    }
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe un mensaje...") },
                        shape = RoundedCornerShape(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        enabled = draft.isNotBlank() && !isSending,
                        onClick = {
                            val text = draft.trim()
                            if (text.isEmpty()) return@IconButton
                            draft = ""
                            scope.launch {
                                isSending = true
                                val response = runCatching {
                                    RetrofitClient.apiService.sendChatMessage(
                                        ChatMessageRequest(
                                            serviceRequestId = serviceRequestId,
                                            senderId = currentUserId,
                                            receiverId = otherUserId,
                                            message = text
                                        )
                                    )
                                }.getOrNull()
                                if (response?.isSuccessful == true) {
                                    response.body()?.let { dto ->
                                        dto.toEntity()?.let { dao.insertar(it) }
                                        displayMessages = mergeMessages(dao.obtenerPorSolicitud(serviceRequestId), listOf(dto))
                                    }
                                } else {
                                    // Sin red o fallo del servidor: no se pierde el mensaje, se
                                    // guarda como pendiente y se muestra de inmediato.
                                    val pendingEntity = MensajeLocalEntity(
                                        id = MensajeLocalEntity.nuevoIdLocal(),
                                        solicitudId = serviceRequestId,
                                        emisorId = currentUserId,
                                        receptorId = otherUserId,
                                        mensaje = text,
                                        imagenUrl = null,
                                        fecha = System.currentTimeMillis(),
                                        enviado = false,
                                        leido = false
                                    )
                                    dao.insertar(pendingEntity)
                                    displayMessages = displayMessages + pendingEntity.toDisplay()
                                }
                                isSending = false
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(
                displayMessages,
                key = { it.serverId?.toLong() ?: it.localId ?: it.fecha }
            ) { msg ->
                ChatBubble(
                    message = msg,
                    isMine = msg.senderId == currentUserId,
                    onImageClick = { url -> fullScreenImageUrl = url }
                )
            }
        }
    }

    fullScreenImageUrl?.let { url ->
        FullScreenImageViewer(imageUrl = url, onDismiss = { fullScreenImageUrl = null })
    }
}

@Composable
private fun ChatBubble(message: ChatDisplayMessage, isMine: Boolean, onImageClick: (String) -> Unit) {
    val resolvedImageUrl = remember(message.imageUrl) {
        if (message.imageUrl?.startsWith("content://") == true || message.imageUrl?.startsWith("file://") == true) {
            // Imagen aun no subida (pendiente de envio): se muestra desde el Uri local.
            message.imageUrl
        } else {
            RetrofitClient.resolveImageUrl(message.imageUrl)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 10.dp
                )
            ) {
                if (resolvedImageUrl != null) {
                    AsyncImage(
                        model = resolvedImageUrl,
                        contentDescription = "Imagen adjunta",
                        contentScale = ContentScale.Crop,
                        placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                        error = ColorPainter(MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onImageClick(resolvedImageUrl) }
                    )
                    if (message.message.isNotBlank()) Spacer(Modifier.height(6.dp))
                }

                if (message.message.isNotBlank()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = message.message,
                            color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Normal
                        )
                        if (isMine) {
                            Spacer(Modifier.width(6.dp))
                            MessageStatusIcon(message)
                        }
                    }
                } else if (isMine) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 2.dp)
                    ) {
                        MessageStatusIcon(message)
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageStatusIcon(message: ChatDisplayMessage) {
    when {
        !message.enviado -> Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = "Pendiente de envio",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(16.dp)
        )
        else -> Icon(
            imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Done,
            contentDescription = if (message.isRead) "Visto" else "Enviado",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(16.dp)
        )
    }
}

