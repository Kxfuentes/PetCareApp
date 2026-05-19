package com.proyectopoo.petcareapp.model

/**
 * Entidad de Dominio (Model)
 * Representa los datos puros del usuario.
 */
data class User(
    val username: String,
    val email: String,
    val role: String
)
