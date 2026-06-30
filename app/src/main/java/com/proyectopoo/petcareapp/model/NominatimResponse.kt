package com.proyectopoo.petcareapp.model

/*
 * Comentario de modulo PetCare:
 * Entidad de dominio. Representa una tabla o concepto principal usado por la API.
 */

import kotlinx.serialization.Serializable

@Serializable
data class NominatimResponse(
    val lat: String,            // Latitud en formato texto
    val lon: String,            // Longitud en formato texto
    val display_name: String    // La dirección completa y legible
)