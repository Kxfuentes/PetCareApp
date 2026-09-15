package com.proyectopoo.petcareapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cache local (offline-first) de los mensajes de chat de una solicitud de servicio.
 *
 * Esquema de IDs: [id] es `Long` y no autogenerado. Para los mensajes ya confirmados por el
 * servidor guardamos el mismo id que el backend (`ChatMessageDto.id`, casteado a Long), de forma
 * que un upsert (REPLACE) por id mantiene la fila sincronizada sin duplicados. Para los mensajes
 * compuestos localmente que todavia no tienen id de servidor (por ejemplo, un mensaje que no se
 * pudo enviar por falta de red y queda en la "cola de reintento"), se genera un id NEGATIVO
 * (ver `nuevoIdLocal()`), ya que el backend nunca asigna ids negativos. Esto evita el conflicto
 * entre ids generados por el cliente y por el servidor sin necesitar una columna separada tipo
 * localUuid ni una PK nullable/autoGenerate: cuando el reintento tiene exito, la fila temporal
 * (id negativo) se borra y se inserta la fila definitiva que llega del servidor (id positivo).
 */
@Entity(tableName = "mensajes_locales", indices = [Index("solicitudId")])
data class MensajeLocalEntity(
    @PrimaryKey val id: Long,
    val solicitudId: Int,
    val emisorId: Int,
    val receptorId: Int,
    val mensaje: String,
    val imagenUrl: String?,
    val fecha: Long,
    val enviado: Boolean,
    val leido: Boolean
) {
    companion object {
        /**
         * Genera un id local unico y negativo para un mensaje pendiente de envio (aun no
         * confirmado por el servidor). Siempre negativo para no colisionar con los ids
         * (positivos, autoincrementales) que asigna el backend.
         */
        fun nuevoIdLocal(): Long = -kotlin.math.abs(System.nanoTime())
    }
}
