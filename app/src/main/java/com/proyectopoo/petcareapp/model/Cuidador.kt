package com.proyectopoo.petcareapp.model

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

