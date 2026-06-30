package com.proyectopoo.petcareapp.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServiceApplicationRequest(
    @SerialName("caregiver_id")
    val caregiverId: Int,

    @SerialName("initiated_by")
    val initiatedBy: String = "CAREGIVER"
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

    @SerialName("initiated_by")
    val initiatedBy: String,

    val status: String
)