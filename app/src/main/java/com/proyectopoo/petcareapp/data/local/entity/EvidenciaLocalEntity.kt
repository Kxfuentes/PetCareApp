package com.proyectopoo.petcareapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cola local (offline-first) de evidencia fotográfica antes/después de un servicio (Bloque 8).
 *
 * Mismo esquema de IDs que [MensajeLocalEntity] (ver comentario ahí para el detalle completo):
 * [id] es `Long` y no autogenerado. Para evidencia ya confirmada por el servidor se guarda el id
 * que devuelve el backend (`EvidenciaDto.id`, casteado a Long); para evidencia que no se pudo
 * subir (sin conexión) se genera un id NEGATIVO vía [nuevoIdLocal] y [imagenUrl] queda apuntando
 * a un Uri local (`content://...`) hasta que se reintenta el envío con éxito.
 *
 * El backend nunca bloquea la transición de estado del servicio por falta de evidencia
 * ("excepción flexible"): esta tabla es solo la cola de reintento para subir la foto más tarde,
 * el servicio sigue su curso de inmediato aunque la fila quede con `enviado = false`.
 */
@Entity(tableName = "evidencias_locales", indices = [Index("solicitudId")])
data class EvidenciaLocalEntity(
    @PrimaryKey val id: Long,
    val solicitudId: Int,
    val tipo: String, // "ANTES" | "DESPUES"
    val imagenUrl: String?,
    val nota: String?,
    val latitud: Double?,
    val longitud: Double?,
    val fecha: Long,
    val enviado: Boolean
) {
    companion object {
        /** Id local único y negativo para evidencia pendiente de envío (ver [MensajeLocalEntity]). */
        fun nuevoIdLocal(): Long = -kotlin.math.abs(System.nanoTime())
    }
}
