package com.proyectopoo.petcareapp.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Evidencia fotográfica (antes/después) de un servicio, subida vía
 * `POST /api/solicitudes/{id}/evidencia` (multipart) y listada vía
 * `GET /api/solicitudes/{id}/evidencias` (Bloque 8). `imagenUrl` es una ruta root-relative
 * (p.ej. "/api/solicitudes/evidencia/xyz.jpg"); usar [RetrofitClient.resolveImageUrl] antes de
 * cargarla con Coil, igual que con las imágenes del chat.
 */
@Serializable
data class EvidenciaDto(
    val id: Int? = null,
    @SerialName("solicitud_id") val solicitudId: Int,
    val tipo: String,
    @SerialName("imagen_url") val imagenUrl: String? = null,
    val nota: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fecha: String? = null
)

object EvidenciaTipo {
    const val ANTES = "ANTES"
    const val DESPUES = "DESPUES"
}
