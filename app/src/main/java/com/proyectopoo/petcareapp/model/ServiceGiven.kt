package com.proyectopoo.petcareapp.model

data class ServiceGiven(
    val id: Int,
    val nombre: String,
    val precio: String,
    val descripcion: String,
    val activo: Boolean,
    val ubicacion: String? = null
)
