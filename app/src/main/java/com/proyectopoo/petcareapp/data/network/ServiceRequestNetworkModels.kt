package com.proyectopoo.petcareapp.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServiceRequestRequest(
    val id: Int,
    @SerialName("owner_id")
    val ownerId: Int,
    @SerialName("pet_id")
    val petId: Int,
    @SerialName("pet_ids")
    val petIds: List<Int> = emptyList(),
    @SerialName("service_type_id")
    val serviceTypeId: Int,
    val title: String,
    val description: String? = null,
    @SerialName("requested_date")
    val requestedDate: String? = null,
    @SerialName("start_time")
    val startTime: String? = null,
    @SerialName("end_time")
    val endTime: String? = null,
    val status: String = "PENDING",
    @SerialName("offered_service_id")
    val offeredServiceId: Int? = null,
    @SerialName("source_type")
    val sourceType: String = "OPEN",
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class ServiceRequestDto(
    val id: Int,
    @SerialName("owner_id")
    val ownerId: Int,
    @SerialName("pet_id")
    val petId: Int,
    @SerialName("pet_ids")
    val petIds: List<Int> = emptyList(),
    @SerialName("service_type_id")
    val serviceTypeId: Int,
    val title: String,
    val description: String? = null,
    @SerialName("requested_date")
    val requestedDate: String? = null,
    @SerialName("start_time")
    val startTime: String? = null,
    @SerialName("end_time")
    val endTime: String? = null,
    val status: String = "PENDING",
    @SerialName("offered_service_id")
    val offeredServiceId: Int? = null,
    @SerialName("source_type")
    val sourceType: String = "OPEN",
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class StatusUpdateRequest(
    val status: String
)