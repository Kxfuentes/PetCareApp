package com.proyectopoo.petcareapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.data.local.entity.NotificationEntity
import com.proyectopoo.petcareapp.data.local.entity.NotificationType
import com.proyectopoo.petcareapp.data.repository.NotificationRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Centro de notificaciones in-app (Parte 3.3): lista lo que [com.proyectopoo.petcareapp.notifications.AppNotifier]
 * ya persiste en Room cada vez que llega un evento por WebSocket/FCM (ofertas, aceptaciones,
 * mensajes, recordatorios). Marca todo como leído al abrir la pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    usuarioId: Int,
    repository: NotificationRepository,
    onBack: () -> Unit
) {
    var notificaciones by remember { mutableStateOf<List<NotificationEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    suspend fun reload() {
        notificaciones = repository.getNotificationsByUser(usuarioId)
        isLoading = false
    }

    LaunchedEffect(usuarioId) {
        reload()
        // Marcar como leídas al entrar, para que el contador de la campanita se limpie.
        notificaciones.filterNot { it.isRead }.forEach { repository.markAsRead(it.notificationId) }
        if (notificaciones.any { !it.isRead }) reload()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            notificaciones.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Todavía no tienes notificaciones.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notificaciones, key = { it.notificationId }) { notificacion ->
                    NotificacionCard(notificacion)
                }
            }
        }
    }
}

@Composable
private fun NotificacionCard(notificacion: NotificationEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notificacion.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(iconForTipo(notificacion.type), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(notificacion.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(notificacion.message, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    formatearFecha(notificacion.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun iconForTipo(type: NotificationType): ImageVector = when (type) {
    NotificationType.SERVICE_REQUEST -> Icons.AutoMirrored.Filled.DirectionsWalk
    NotificationType.REQUEST_ACCEPTED -> Icons.Default.CheckCircle
    NotificationType.REQUEST_REJECTED -> Icons.Default.Info
    NotificationType.REQUEST_CANCELLED -> Icons.Default.Info
    NotificationType.GENERAL -> Icons.Default.Notifications
}

private fun formatearFecha(millis: Long): String =
    runCatching { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(millis) }.getOrDefault("")
