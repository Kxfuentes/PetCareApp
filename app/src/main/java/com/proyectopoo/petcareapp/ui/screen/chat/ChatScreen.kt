package com.proyectopoo.petcareapp.ui.screen.chat

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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.proyectopoo.petcareapp.data.network.ChatMessageDto
import com.proyectopoo.petcareapp.data.network.ChatMessageRequest
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    serviceRequestId: Int,
    currentUserId: Int,
    otherUserId: Int,
    otherUserName: String,
    refreshTick: Int,
    onBack: () -> Unit
) {
    var messages by remember { mutableStateOf<List<ChatMessageDto>>(emptyList()) }
    var draft by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    suspend fun reload() {
        val result = runCatching { RetrofitClient.apiService.getChatMessages(serviceRequestId) }.getOrNull()
        if (result?.isSuccessful == true) {
            messages = result.body().orEmpty()
        }
        runCatching { RetrofitClient.apiService.markChatMessagesRead(serviceRequestId, currentUserId) }
    }

    LaunchedEffect(serviceRequestId) { reload() }
    // Un evento de WebSocket (CHAT_MESSAGE) sube el tick global; si toca esta conversacion, recargamos.
    LaunchedEffect(refreshTick) { if (refreshTick > 0) reload() }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
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
            }.getOrNull()

            if (result?.isSuccessful == true) {
                result.body()?.let { messages = messages + it }
                draft = ""
            } else {
                uploadError = "No se pudo enviar la imagen."
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
                                    response.body()?.let { messages = messages + it }
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
            items(messages, key = { it.id ?: it.createdAt.hashCode() }) { msg ->
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
private fun ChatBubble(message: ChatMessageDto, isMine: Boolean, onImageClick: (String) -> Unit) {
    val resolvedImageUrl = remember(message.imageUrl) { RetrofitClient.resolveImageUrl(message.imageUrl) }

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
                            Icon(
                                imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Done,
                                contentDescription = if (message.isRead) "Visto" else "Enviado",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else if (isMine) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Done,
                            contentDescription = if (message.isRead) "Visto" else "Enviado",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FullScreenImageViewer(imageUrl: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black)
                .clickable(onClick = onDismiss)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Imagen ampliada",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = androidx.compose.ui.graphics.Color.White)
            }
        }
    }
}
