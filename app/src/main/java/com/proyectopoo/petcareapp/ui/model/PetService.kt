package com.proyectopoo.petcareapp.ui.model

/**
 * Modelo que representa una solicitud de servicio para una mascota.
 */
data class PetService(

    // Identificador único del servicio
    val id: Int,

    // Nombre de la mascota
    val nombreMascota: String,

    // Nombre del dueño
    val nombreDueno: String,

    // Tipo de servicio (Paseo, Veterinario, etc.)
    val tipoServicio: String,

    // Descripción del servicio
    val descripcion: String,

    // Ubicación
    val ubicacion: String,

    // Teléfono de contacto
    val contacto: String,

    // Hora del servicio
    val hora: String
)