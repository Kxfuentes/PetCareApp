package com.proyectopoo.petcareapp.model

/*
 * Comentario de modulo PetCare:
 * Entidad de dominio. Representa una tabla o concepto principal usado por la API.
 */

data class Cuidador(
    val id: String,
    val nombre: String,
    val ubicacion: String,
    val precio: String,
    val rating: Double,
    val reviews: Int,
    val servicios: List<String>,
    val review: String
)

