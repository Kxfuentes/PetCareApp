package com.proyectopoo.petcareapp.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Respuesta de `GET /api/calendario?usuario_id={id}&mes={1-12}&anio={year}` (Bloque 9).
 *
 * `disponibilidad` es deliberadamente siempre `[]` hoy: el backend todavía no tiene el concepto
 * de ventanas de disponibilidad del cuidador (fuera de alcance, una funcionalidad futura
 * separada). El modelo queda completo para cuando esa funcionalidad exista, pero la UI
 * (`CalendarioScreen.kt`) solo renderiza `servicios` por ahora.
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

/**
 * Ventana de disponibilidad de un cuidador. El backend no expone esta funcionalidad todavía
 * (siempre llega `disponibilidad: []`), así que estos campos son una mejor suposición del
 * shape que tendrá cuando se implemente, no un contrato confirmado.
 */
@Serializable
data class CalendarioDisponibilidadDto(
    val id: Int? = null,
    @SerialName("cuidador_id") val cuidadorId: Int? = null,
    val fecha: String? = null,
    @SerialName("hora_inicio") val horaInicio: String? = null,
    @SerialName("hora_fin") val horaFin: String? = null
)
