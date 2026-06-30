package com.proyectopoo.petcareapp.ui.util

/*
 * Comentario de modulo PetCare:
 * Utilidad compartida. Evita repetir reglas pequenas en varias partes del proyecto.
 */

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
