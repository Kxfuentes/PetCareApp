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

/**
 * Cuerpo para POST /api/solicitudes/{id}/ubicacion: el cuidador reporta su posición GPS
 * actual mientras un servicio de Taxi/Paseo está en curso. Nota: a diferencia del resto de
 * este archivo (que usa latitude/longitude en inglés), este endpoint y su respuesta usan
 * latitud/longitud en español para coincidir con la convención existente del controlador del
 * backend — es intencional, no una inconsistencia.
 */
@Serializable
data class UbicacionRequest(
    val latitud: Double,
    val longitud: Double
)

/** Respuesta 200 de POST .../ubicacion. */
@Serializable
data class UbicacionResponse(
    val latitud: Double,
    val longitud: Double
)

/** Respuesta 200 de GET /api/solicitudes/{id}/ubicacion-actual. 404 significa que el cuidador
 *  aún no ha reportado ninguna ubicación para esta solicitud (no es un error). */
@Serializable
data class UbicacionActualResponse(
    val latitud: Double,
    val longitud: Double,
    val actualizadoEn: String
)
