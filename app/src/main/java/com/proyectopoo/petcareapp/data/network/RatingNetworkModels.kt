package com.proyectopoo.petcareapp.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RatingRequest(
    @SerialName("service_request_id")
    val serviceRequestId: Int,
    @SerialName("caregiver_id")
    val caregiverId: Int,
    @SerialName("owner_id")
    val ownerId: Int,
    @SerialName("rated_by_role")
    val ratedByRole: String,
    val score: Double,
    val comment: String? = null
)

@Serializable
data class RatingDto(
    val id: Int? = null,
    @SerialName("service_request_id")
    val serviceRequestId: Int,
    @SerialName("caregiver_id")
    val caregiverId: Int,
    @SerialName("owner_id")
    val ownerId: Int,
    @SerialName("rated_by_role")
    val ratedByRole: String,
    val score: Double,
    val comment: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class RatingSummaryDto(
    val average: Double = 5.0,
    val count: Int = 0
)
