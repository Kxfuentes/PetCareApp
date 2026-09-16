package com.proyectopoo.petcareapp.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Respuesta de `GET /api/calendario?usuario_id={id}&mes={1-12}&anio={year}` (Bloque 9).
 *
 * `disponibilidad` expande el horario semanal recurrente del cuidador (configurado via
 * `POST /api/cuidadores/disponibilidad`) en fechas concretas dentro del mes/anio pedido.
 */
@Serializable
data class CalendarioResponseDto(
    val servicios: List<CalendarioServicioDto> = emptyList(),
    val disponibilidad: List<CalendarioDisponibilidadDto> = emptyList()
)

@Serializable
data class CalendarioServicioDto(
    val fecha: String,
    @SerialName("solicitud_id") val solicitudId: Int,
    val titulo: String,
    val estado: String,
    val rol: String
)

/** Disponibilidad del cuidador ya expandida a una fecha concreta, con sus rangos de hora ("09:00-12:00"). */
@Serializable
data class CalendarioDisponibilidadDto(
    val fecha: String,
    val horas: List<String> = emptyList()
)

/**
 * Un bloque de disponibilidad semanal recurrente, tal como lo devuelve
 * `GET/POST /api/cuidadores/{id}/disponibilidad` (no confundir con [CalendarioDisponibilidadDto],
 * que es la versión ya expandida a fechas concretas que consume el calendario).
 * `dia_semana`: 0 = lunes ... 6 = domingo.
 */
@Serializable
data class DisponibilidadCuidadorDto(
    val id: Int? = null,
    @SerialName("cuidador_id") val cuidadorId: Int? = null,
    @SerialName("dia_semana") val diaSemana: Int,
    @SerialName("hora_inicio") val horaInicio: String,
    @SerialName("hora_fin") val horaFin: String,
    val activo: Boolean = true
)

@Serializable
data class DisponibilidadCuidadorRequest(
    @SerialName("usuario_id") val usuarioId: Int,
    @SerialName("dia_semana") val diaSemana: Int,
    @SerialName("hora_inicio") val horaInicio: String,
    @SerialName("hora_fin") val horaFin: String
)
