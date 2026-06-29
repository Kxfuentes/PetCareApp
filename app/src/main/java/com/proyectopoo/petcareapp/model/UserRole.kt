package com.proyectopoo.petcareapp.model

enum class UserRole(val backendValue: String) {
    OWNER("propietario"),
    CAREGIVER("gestor");

    companion object {
        fun fromBackendValue(value: String): UserRole? {
            return entries.find { it.backendValue.equals(value, ignoreCase = true) }
        }
    }
}
