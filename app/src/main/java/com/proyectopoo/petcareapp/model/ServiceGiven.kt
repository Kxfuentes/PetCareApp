package com.proyectopoo.petcareapp.model

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

data class ServiceGiven(
    val id: Int,
    val nombre: String,
    val precio: String,
    val descripcion: String,
    val activo: Boolean,
    val ubicacion: String? = null
)
