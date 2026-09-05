package com.proyectopoo.petcareapp.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Cuerpo para PUT /api/solicitudes/{id}: edición parcial de una solicitud PENDING.
 * Todos los campos son opcionales; el backend solo actualiza los que vienen presentes.
 */
@Serializable
data class SolicitudEditRequest(
    val title: String? = null,
    val description: String? = null,
    @SerialName("requested_date") val requestedDate: String? = null,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    @SerialName("pet_id") val petId: Int? = null,
    @SerialName("pet_ids") val petIds: List<Int>? = null,
    @SerialName("service_type_id") val serviceTypeId: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
