package com.proyectopoo.petcareapp.ui.util

fun statusLabel(statusName: String): String {
    return when (statusName.uppercase()) {
        "PENDING" -> "Pendiente"
        "ACCEPTED" -> "Aceptada"
        "DONE_BY_CAREGIVER" -> "Pendiente de confirmar"
        "REJECTED" -> "Rechazada"
        "CANCELLED" -> "Cancelada"
        "COMPLETED" -> "Finalizada"
        "ACTIVE" -> "Activa"
        else -> statusName.replace('_', ' ').lowercase()
            .replaceFirstChar { it.uppercase() }
    }
}
