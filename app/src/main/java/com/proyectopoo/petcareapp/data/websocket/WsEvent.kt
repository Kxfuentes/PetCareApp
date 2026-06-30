package com.proyectopoo.petcareapp.data.websocket

/*
 * Comentario de modulo PetCare:
 * Comunicacion en tiempo real. Mantiene la conexion WebSocket y traduce los eventos recibidos.
 */

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WsEvent(
    val type: String,
    @SerialName("recipient_user_id")
    val recipientUserId: Int? = null,
    val title: String? = null,
    val message: String? = null,
    @SerialName("service_request_id")
    val serviceRequestId: Int? = null,
    @SerialName("application_id")
    val applicationId: Int? = null,
    @SerialName("user_id")
    val userId: Int? = null
)