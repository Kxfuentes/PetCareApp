package com.proyectopoo.petcareapp.data.network

/*
 * Comentario de modulo PetCare:
 * Contrato de red. Define datos y endpoints usados para comunicarse con la API de PetCare.
 */

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServiceApplicationRequest(
    @SerialName("service_request_id")
    val serviceRequestId: Int,

    @SerialName("caregiver_id")
    val caregiverId: Int,

    @SerialName("offered_service_id")
    val offeredServiceId: Int? = null,

    @SerialName("initiated_by")
    val initiatedBy: String = "CAREGIVER",

    val status: String = "PENDING"
)

@Serializable
data class ServiceApplicationStatusRequest(
    val status: String
)

@Serializable
data class ServiceApplicationDto(
    val id: Int,

    @SerialName("service_request_id")
    val serviceRequestId: Int,

    @SerialName("caregiver_id")
    val caregiverId: Int,

    @SerialName("offered_service_id")
    val offeredServiceId: Int? = null,

    @SerialName("initiated_by")
    val initiatedBy: String,

    val status: String,

    @SerialName("owner_name")
    val ownerName: String? = null,

    @SerialName("caregiver_name")
    val caregiverName: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)
