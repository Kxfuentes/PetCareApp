package com.proyectopoo.petcareapp.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Alerta de mascota perdida (Bloque 12). Se crea vía `POST /api/alertas-perdida`; el backend
 * notifica automáticamente a los usuarios cercanos (no hay lógica de notificación del lado del
 * cliente). `estado` es uno de "ACTIVA"/"ENCONTRADA"/"CERRADA".
 */
@Serializable
data class AlertaPerdidaDto(
    val id: Int? = null,
    @SerialName("pets_id") val petsId: Int,
    @SerialName("usuario_id") val usuarioId: Int,
    val descripcion: String? = null,
    val latitud: Double,
    val longitud: Double,
    @SerialName("direccion_texto") val direccionTexto: String? = null,
    val estado: String? = null,
    @SerialName("fecha_creacion") val fechaCreacion: String? = null,
    @SerialName("fecha_cierre") val fechaCierre: String? = null
)

@Serializable
data class AlertaPerdidaRequest(
    @SerialName("pets_id") val petsId: Int,
    @SerialName("usuario_id") val usuarioId: Int,
    val descripcion: String? = null,
    val latitud: Double,
    val longitud: Double,
    @SerialName("direccion_texto") val direccionTexto: String? = null
)

/**
 * Reporte de avistamiento (`POST /api/alertas-perdida/{id}/avistamiento`, cualquier usuario).
 * `imagenUrl` queda deliberadamente sin usar en esta primera versión: el endpoint espera una
 * URL de texto plano, no una subida multipart, así que adjuntar foto requeriría subirla primero
 * a algún endpoint de imágenes existente (p.ej. el del chat) y pasar la URL resultante. Se dejó
 * fuera de alcance para esta pasada -- el flujo principal (ubicación + comentario) es lo que
 * importa.
 */
@Serializable
data class AvistamientoRequest(
    @SerialName("alerta_id") val alertaId: Int? = null,
    @SerialName("usuario_id") val usuarioId: Int,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val comentario: String? = null,
    @SerialName("imagen_url") val imagenUrl: String? = null
)

@Serializable
data class AvistamientoDto(
    val id: Int? = null,
    @SerialName("alerta_id") val alertaId: Int? = null,
    @SerialName("usuario_id") val usuarioId: Int? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val comentario: String? = null,
    @SerialName("imagen_url") val imagenUrl: String? = null,
    @SerialName("fecha_creacion") val fechaCreacion: String? = null
)

/** Item de `GET /api/alertas-perdida/cercanas?lat=&lng=&radio=`. */
@Serializable
data class AlertaCercanaDto(
    val alerta: AlertaPerdidaDto,
    @SerialName("distancia_km") val distanciaKm: Double? = null
)

object AlertaPerdidaEstado {
    const val ACTIVA = "ACTIVA"
    const val ENCONTRADA = "ENCONTRADA"
    const val CERRADA = "CERRADA"
}
