package com.proyectopoo.petcareapp.data.network

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

/** Body opcional para POST /api/ofertas/{id}/rechazar y /cancelar. */
@Serializable
data class OfertaMotivoRequest(
    val motivo: String? = null
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

    @SerialName("owner_phone")
    val ownerPhone: String? = null,

    @SerialName("owner_email")
    val ownerEmail: String? = null,

    @SerialName("caregiver_phone")
    val caregiverPhone: String? = null,

    @SerialName("caregiver_email")
    val caregiverEmail: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)
