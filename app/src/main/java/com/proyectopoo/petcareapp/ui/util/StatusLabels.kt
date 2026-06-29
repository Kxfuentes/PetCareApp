package com.proyectopoo.petcareapp.ui.util

fun statusLabel(statusName: String): String {
    return when (statusName.uppercase()) {
        "PENDING" -> "Pendiente"
        "ACCEPTED" -> "Aceptada"
        "REJECTED" -> "Rechazada"
        "CANCELLED" -> "Cancelado"
        "COMPLETED" -> "Completado"
        "ACTIVE" -> "Activa"
        else -> statusName.replace('_', ' ').lowercase()
            .replaceFirstChar { it.uppercase() }
    }
}
