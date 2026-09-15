package com.proyectopoo.petcareapp.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Entrada del expediente médico de una mascota (Bloque 11).
 *
 * Se lista vía `GET /api/pets/{id}/expediente` (más reciente primero), se crea vía
 * `POST /api/pets/{id}/expediente` (multipart, ver [ApiService.createExpedienteEntry]) y se
 * edita/elimina vía `PUT`/`DELETE /api/pets/expediente/{entradaId}`.
 *
 * `imagenCarnetUrl` es una ruta root-relative (p.ej. "/api/pets/expediente/carnet/xyz.jpg");
 * usar [RetrofitClient.resolveImageUrl] antes de cargarla con Coil, igual que con las imágenes
 * del chat y la evidencia de servicio.
 */
@Serializable
data class ExpedienteEntryDto(
    val id: Int? = null,
    @SerialName("pets_id") val petsId: Int? = null,
    val tipo: String,
    val titulo: String,
    val descripcion: String? = null,
    val fecha: String,
    @SerialName("fecha_proxima") val fechaProxima: String? = null,
    @SerialName("veterinario_nombre") val veterinarioNombre: String? = null,
    @SerialName("veterinario_telefono") val veterinarioTelefono: String? = null,
    @SerialName("imagen_carnet_url") val imagenCarnetUrl: String? = null,
    @SerialName("fecha_creacion") val fechaCreacion: String? = null
)

/** Body JSON de `PUT /api/pets/expediente/{entradaId}?usuario_id={id}` (sin campo de imagen). */
@Serializable
data class ExpedienteEntryRequest(
    @SerialName("usuario_id") val usuarioId: Int,
    val tipo: String,
    val titulo: String,
    val descripcion: String? = null,
    val fecha: String,
    @SerialName("fecha_proxima") val fechaProxima: String? = null,
    @SerialName("veterinario_nombre") val veterinarioNombre: String? = null,
    @SerialName("veterinario_telefono") val veterinarioTelefono: String? = null
)

/** Los 7 tipos de entrada que acepta el backend, con su etiqueta en español para la UI. */
object ExpedienteTipo {
    const val VACUNA = "VACUNA"
    const val DESPARASITACION = "DESPARASITACION"
    const val ALERGIA = "ALERGIA"
    const val MEDICAMENTO = "MEDICAMENTO"
    const val CIRUGIA = "CIRUGIA"
    const val PESO = "PESO"
    const val NOTA = "NOTA"

    val ALL = listOf(VACUNA, DESPARASITACION, ALERGIA, MEDICAMENTO, CIRUGIA, PESO, NOTA)

    fun label(tipo: String): String = when (tipo) {
        VACUNA -> "Vacuna"
        DESPARASITACION -> "Desparasitación"
        ALERGIA -> "Alergia"
        MEDICAMENTO -> "Medicamento"
        CIRUGIA -> "Cirugía"
        PESO -> "Peso"
        NOTA -> "Nota"
        else -> tipo
    }
}
