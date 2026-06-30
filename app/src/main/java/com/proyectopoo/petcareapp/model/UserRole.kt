package com.proyectopoo.petcareapp.model

/*
 * Comentario de modulo PetCare:
 * Entidad de dominio. Representa una tabla o concepto principal usado por la API.
 */

enum class UserRole(val backendValue: String) {
    OWNER("propietario"),
    CAREGIVER("gestor");

    companion object {
        fun fromBackendValue(value: String): UserRole? {
            return entries.find { it.backendValue.equals(value, ignoreCase = true) }
        }
    }
}
