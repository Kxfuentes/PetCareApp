package com.proyectopoo.petcareapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight

/** Tipos de reacción soportados por el backend (POST /api/solicitudes/{id}/valorar-durante). */
private val reactionOptions: List<Pair<String, ImageVector>> = listOf(
    "CORAZON" to Icons.Default.Favorite,
    "ESTRELLA" to Icons.Default.Star,
    "PULGAR" to Icons.Default.ThumbUp
)

/**
 * Fila de 3 botones de reacción rápida (corazón/estrella/pulgar) para enviar mientras un
 * servicio está en curso. [onReact] recibe el valor exacto que espera el backend (CORAZON,
 * ESTRELLA o PULGAR); no requiere estado propio, el llamador decide qué hacer con cada envío
 * (llamar a la API y mostrar la confirmación).
 */
@Composable
fun ReactionButtonsRow(
    enabled: Boolean,
    onReact: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Enviar una reacción",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            reactionOptions.forEach { (value, icon) ->
                IconButton(enabled = enabled, onClick = { onReact(value) }) {
                    Icon(icon, contentDescription = value, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
