package com.proyectopoo.petcareapp.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val rol: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterResponse(
    @SerialName("user") val user: UserDto? = null,
    @SerialName("session") val session: SessionDto? = null,
    @SerialName("useer") val useer: UserDto? = null,
    @SerialName("sessions") val sessions: SessionDto? = null
)

@Serializable
data class LoginResponse(
    @SerialName("token") val token: String? = null,
    @SerialName("user") val user: UserDto? = null,
    @SerialName("useer") val useer: UserDto? = null,
    @SerialName("session") val session: SessionDto? = null,
    @SerialName("error") val error: String? = null
)

@Serializable
data class UserDto(
    val id: Int,
    val username: String,
    val email: String,
    @SerialName("rol") val role: String? = null
)

@Serializable
data class SessionDto(
    val id: Int? = null,
    @SerialName("token_sesion") val tokenSesion: String? = null,
    @SerialName("tokenSesion") val tokenSesionCamel: String? = null,
    @SerialName("fecha_inicio") val fechaInicio: String? = null,
    val token: String? = null,
    @SerialName("sessions") val sessionToken: String? = null
)

@Serializable
data class ErrorResponse(
    val error: String? = null
)

@Serializable
data class RoleUpdateRequest(
    val rol: String
)


@Serializable
data class PetDto(
    val id: Int? = null,
    @SerialName("owner_id") val ownerId: Int,
    val name: String,
    val species: String? = null,
    val breed: String? = null,
    val size: String? = null,
    val age: Int? = null,
    val weight: Double? = null,
    val description: String? = null
)

@Serializable
data class PetRequest(
    @SerialName("owner_id") val ownerId: Int,
    val name: String,
    val species: String? = null,
    val breed: String,
    val size: String,
    val age: Int? = null,
    val weight: Double? = null,
    val description: String? = null
)

@Serializable
data class OfferedServiceDto(
    val id: Int? = null,
    @SerialName("caregiver_id") val caregiverId: Int,
    @SerialName("service_type_id") val serviceTypeId: Int,
    val title: String,
    val description: String? = null,
    val price: Double,
    @SerialName("is_available") val isAvailable: Boolean = true,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class ServiceRequestDto(
    val id: Int,
    @SerialName("owner_id") val ownerId: Int,
    @SerialName("pet_id") val petId: Int,
    @SerialName("pet_ids") val petIds: List<Int> = emptyList(),
    @SerialName("service_type_id") val serviceTypeId: Int,
    val title: String,
    val description: String? = null,
    @SerialName("requested_date") val requestedDate: String? = null,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    val status: String = "PENDING",
    @SerialName("offered_service_id") val offeredServiceId: Int? = null,
    @SerialName("source_type") val sourceType: String = "OPEN",
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class ServiceApplicationDto(
    val id: Int? = null,
    @SerialName("service_request_id") val serviceRequestId: Int,
    @SerialName("caregiver_id") val caregiverId: Int,
    @SerialName("offered_service_id") val offeredServiceId: Int? = null,
    @SerialName("initiated_by") val initiatedBy: String = "CAREGIVER",
    val status: String = "PENDING",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class StatusUpdateRequest(
    val status: String
)

@Serializable
data class ScheduleUpdateRequest(
    @SerialName("requested_date") val requestedDate: String? = null,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null
)


@Serializable
data class RatingDto(
    val id: Int? = null,
    @SerialName("service_request_id") val serviceRequestId: Int,
    @SerialName("caregiver_id") val caregiverId: Int,
    @SerialName("owner_id") val ownerId: Int,
    @SerialName("rated_by_role") val ratedByRole: String = "OWNER",
    val score: Double,
    val comment: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class RatingSummaryDto(
    val average: Double,
    val count: Int
)
