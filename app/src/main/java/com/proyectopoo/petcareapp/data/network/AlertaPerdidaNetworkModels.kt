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
 * Reporte de avistamiento. Se envía con `ApiService.reportarAvistamiento` (multipart/form-data,
 * foto obligatoria) y se recibe de vuelta con este mismo shape, incluyendo `imagenUrl` ya
 * apuntando al endpoint de descarga (`/api/alertas-perdida/avistamiento/foto/{filename}`).
 */
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
