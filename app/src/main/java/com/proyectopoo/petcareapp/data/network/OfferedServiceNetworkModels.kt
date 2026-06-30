package com.proyectopoo.petcareapp.data.network

/*
 * Comentario de modulo PetCare:
 * Contrato de red. Define datos y endpoints usados para comunicarse con la API de PetCare.
 */

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OfferedServiceRequest(
    @SerialName("caregiver_id")
    val caregiverId: Int,
    @SerialName("service_type_id")
    val serviceTypeId: Int,
    val title: String,
    val description: String? = null,
    val price: Double,
    @SerialName("is_available")
    val isAvailable: Boolean = true,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class OfferedServiceDto(
    val id: Int,
    @SerialName("caregiver_id")
    val caregiverId: Int,
    @SerialName("service_type_id")
    val serviceTypeId: Int,
    val title: String,
    val description: String? = null,
    val price: Double = 0.0,
    @SerialName("is_available")
    val isAvailable: Boolean = true,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)
