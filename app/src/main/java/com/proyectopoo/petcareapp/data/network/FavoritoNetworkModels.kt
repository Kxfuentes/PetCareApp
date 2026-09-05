package com.proyectopoo.petcareapp.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Favorito de un usuario (cuidador o mascota marcados como favoritos).
 * Se usa tanto para leer la lista (GET /api/favoritos) como para crear uno (POST /api/favoritos),
 * igual que en el backend (FavoritoDTO).
 */
@Serializable
data class FavoritoDto(
    val id: Int? = null,
    @SerialName("usuario_id") val usuarioId: Int = 0,
    @SerialName("cuidador_id") val caregiverId: Int? = null,
    @SerialName("mascota_id") val petId: Int? = null,
    @SerialName("fecha_agregado") val addedAt: String? = null
)
