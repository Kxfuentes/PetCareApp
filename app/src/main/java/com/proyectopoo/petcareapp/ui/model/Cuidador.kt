package com.proyectopoo.petcareapp.ui.model

data class Cuidador(

    val nombre: String,

    val ubicacion: String,

    val precio: String,

    val rating: Double,

    val reviews: Int,

    val servicios: List<String>,

    val resena: String
)