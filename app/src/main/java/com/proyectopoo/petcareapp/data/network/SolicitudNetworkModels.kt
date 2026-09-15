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

/**
 * Cuerpo para POST /api/emergencias: reporta una emergencia durante un servicio en curso
 * (ACCEPTED). `tipo` debe ser exactamente uno de MEDICA, ACCIDENTE, MASCOTA_PERDIDA, OTRO.
 * El backend notifica por FCM al dueño, al cuidador asignado y a los admins.
 */
@Serializable
data class EmergenciaRequest(
    @SerialName("service_request_id") val serviceRequestId: Int,
    @SerialName("reported_by") val reportedBy: Int,
    val tipo: String,
    val descripcion: String? = null
)

/** Respuesta 201 de POST /api/emergencias. */
@Serializable
data class EmergenciaDto(
    val id: Int? = null,
    @SerialName("service_request_id") val serviceRequestId: Int,
    @SerialName("reported_by") val reportedBy: Int,
    val tipo: String,
    val descripcion: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

/**
 * Cuerpo para POST /api/solicitudes/{id}/valorar-durante: reacción rápida enviada mientras un
 * servicio está en curso (ACCEPTED). `tipoReaccion` debe ser exactamente uno de CORAZON,
 * ESTRELLA, PULGAR. El backend notifica al cuidador asignado.
 */
@Serializable
data class ValoracionDuranteRequest(
    @SerialName("usuario_id") val usuarioId: Int,
    @SerialName("tipo_reaccion") val tipoReaccion: String
)

/** Respuesta 201 de POST /api/solicitudes/{id}/valorar-durante. */
@Serializable
data class ValoracionDuranteDto(
    val id: Int? = null,
    @SerialName("service_request_id") val serviceRequestId: Int? = null,
    @SerialName("usuario_id") val usuarioId: Int? = null,
    @SerialName("tipo_reaccion") val tipoReaccion: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
