package com.proyectopoo.petcareapp.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Etiqueta visual del badge de un cuidador (calculado en el backend por `BadgeService`, ver
 * `GET /api/usuarios/{id}/badge`): NUEVO, EN_CRECIMIENTO, CONFIABLE, EXPERIMENTADO, ELITE o
 * EN_OBSERVACION. Reutilizable en perfiles y tarjetas de ofertas/solicitudes.
 */
@Composable
fun BadgeChip(badge: String) {
    val (label, color) = badgeStyle(badge)
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.15f)) {
        Text(
            text = label,
            modifier = androidx.compose.ui.Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun badgeStyle(badge: String): Pair<String, Color> = when (badge) {
    "ELITE" -> "⭐ Élite" to Color(0xFFB8860B)
    "EXPERIMENTADO" -> "🏅 Experimentado" to Color(0xFF2E7D32)
    "CONFIABLE" -> "✔️ Confiable" to Color(0xFF1565C0)
    "EN_CRECIMIENTO" -> "🌱 En crecimiento" to Color(0xFF6D4C41)
    "EN_OBSERVACION" -> "⚠️ En observación" to Color(0xFFC62828)
    else -> "🆕 Nuevo" to Color(0xFF757575)
}
