package com.proyectopoo.petcareapp.model

/*
 * Comentario de modulo PetCare:
 * Entidad de dominio. Representa una tabla o concepto principal usado por la API.
 */

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val role: String
)
