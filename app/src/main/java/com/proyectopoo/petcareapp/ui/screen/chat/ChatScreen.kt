package com.proyectopoo.petcareapp.ui.screen.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.data.network.ChatMessageDto
import com.proyectopoo.petcareapp.data.network.ChatMessageRequest
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import kotlinx.coroutines.launch

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
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                ChatBubble(message = msg, isMine = msg.senderId == currentUserId)
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessageDto, isMine: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = message.message,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
