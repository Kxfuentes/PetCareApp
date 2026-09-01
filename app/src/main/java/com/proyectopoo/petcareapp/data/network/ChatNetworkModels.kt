package com.proyectopoo.petcareapp.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageRequest(
    @SerialName("service_request_id") val serviceRequestId: Int,
    @SerialName("sender_id") val senderId: Int,
    @SerialName("receiver_id") val receiverId: Int,
    val message: String
)

@Serializable
data class ChatMessageDto(
    val id: Int? = null,
    @SerialName("service_request_id") val serviceRequestId: Int,
    @SerialName("sender_id") val senderId: Int,
    @SerialName("receiver_id") val receiverId: Int,
    val message: String,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class UnreadCountDto(@SerialName("no_leidos") val noLeidos: Int = 0)
