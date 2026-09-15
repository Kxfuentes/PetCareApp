package com.proyectopoo.petcareapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.proyectopoo.petcareapp.data.network.EvidenciaTipo

/**
 * Muestra las fotos de evidencia (antes/después) de un servicio dentro de los diálogos de
 * detalle existentes (`CaregiverServiceDetailsDialog` en `CaregiverHomeScreen.kt`,
 * `ServiceApplicationDetailsDialog` en `OwnerHomeScreen.kt`, Bloque 8). Miniaturas lado a lado,
 * tocables para ampliar a pantalla completa (mismo patrón que las imágenes del chat, ver
 * [FullScreenImageViewer]). Si `disponibilidad`/evidencia aún no existe para un tipo, se muestra
 * un placeholder "Sin foto aún" en vez de omitir la sección entera.
 */
@Composable
fun EvidenciaThumbnails(serviceRequestId: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var evidencia by remember(serviceRequestId) { mutableStateOf<Map<String, EvidenciaThumbnail>>(emptyMap()) }
    var fullScreenUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(serviceRequestId) {
        evidencia = cargarEvidenciaPorTipo(context, serviceRequestId)
    }

    if (evidencia.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                "Evidencia fotográfica",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EvidenciaSlot(
                    label = "Antes",
                    thumbnail = evidencia[EvidenciaTipo.ANTES],
                    modifier = Modifier.weight(1f),
                    onClick = { evidencia[EvidenciaTipo.ANTES]?.let { fullScreenUrl = it.imageUrl } }
                )
                EvidenciaSlot(
                    label = "Después",
                    thumbnail = evidencia[EvidenciaTipo.DESPUES],
                    modifier = Modifier.weight(1f),
                    onClick = { evidencia[EvidenciaTipo.DESPUES]?.let { fullScreenUrl = it.imageUrl } }
                )
            }
        }
    }

    fullScreenUrl?.let { url ->
        FullScreenImageViewer(imageUrl = url, onDismiss = { fullScreenUrl = null })
    }
}

@Composable
private fun EvidenciaSlot(
    label: String,
    thumbnail: EvidenciaThumbnail?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .then(if (thumbnail != null) Modifier.clickable(onClick = onClick) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            if (thumbnail != null) {
                AsyncImage(
                    model = thumbnail.imageUrl,
                    contentDescription = "Foto $label",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                )
                if (thumbnail.pending) {
                    Icon(
                        Icons.Default.CloudUpload,
                        contentDescription = "Pendiente de subir",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                    )
                }
            } else {
                Icon(
                    Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        if (thumbnail == null) {
            Text(
                text = "Sin foto aún",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
