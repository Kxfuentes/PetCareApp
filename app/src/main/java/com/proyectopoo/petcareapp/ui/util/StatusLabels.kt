package com.proyectopoo.petcareapp.ui.util

import androidx.compose.ui.graphics.Color

fun statusLabel(statusName: String): String {
    return when (statusName.uppercase()) {
        "PENDING" -> "Pendiente"
        "ACCEPTED" -> "Aceptada"
        "DONE_BY_CAREGIVER" -> "Pendiente de confirmar"
        "REJECTED" -> "Rechazada"
        "CANCELLED" -> "Cancelado"
        "COMPLETED" -> "Completado"
        "ACTIVE" -> "Activa"
        else -> statusName.replace('_', ' ').lowercase()
            .replaceFirstChar { it.uppercase() }
    }
}

/**
 * Mismo mapeo de color por estado que ya usan `StatusChip`/`requestStatusLabelAndColor` en
 * `OwnerHomeScreen.kt`/`CaregiverHomeScreen.kt` (PENDING naranja, ACCEPTED verde,
 * DONE_BY_CAREGIVER azul, COMPLETED gris, REJECTED/CANCELLED rojo/marrón), extraído aquí para
 * que el calendario (Bloque 9) pueda reusarlo sin depender de una función privada de esas
 * pantallas.
 */
fun statusColor(statusName: String): Color = when (statusName.uppercase()) {
    "PENDING" -> Color(0xFFFF9800)
    "ACCEPTED" -> Color(0xFF4CAF50)
    "DONE_BY_CAREGIVER" -> Color(0xFF2196F3)
    "COMPLETED" -> Color(0xFF607D8B)
    "REJECTED" -> Color(0xFFF44336)
    "CANCELLED" -> Color(0xFF795548)
    else -> Color(0xFF8A6A55)
}
