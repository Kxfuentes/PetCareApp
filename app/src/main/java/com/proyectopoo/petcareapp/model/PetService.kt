package com.proyectopoo.petcareapp.model

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

data class PetService(

    val nombreMascota: String,

    val nombreDueno: String,

    val tipoServicio: String,

    val descripcion: String,

    val ubicacion: String,

    val contacto: String,

    val hora: String
)