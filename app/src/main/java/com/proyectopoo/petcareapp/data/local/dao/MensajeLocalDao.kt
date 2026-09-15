package com.proyectopoo.petcareapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.proyectopoo.petcareapp.data.local.entity.MensajeLocalEntity

@Dao
interface MensajeLocalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(mensaje: MensajeLocalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(mensajes: List<MensajeLocalEntity>)

    @Query("SELECT * FROM mensajes_locales WHERE solicitudId = :solicitudId ORDER BY fecha ASC, id ASC")
    suspend fun obtenerPorSolicitud(solicitudId: Int): List<MensajeLocalEntity>

    @Query("SELECT * FROM mensajes_locales WHERE solicitudId = :solicitudId AND enviado = 0 ORDER BY fecha ASC, id ASC")
    suspend fun obtenerPendientesPorSolicitud(solicitudId: Int): List<MensajeLocalEntity>

    @Query("UPDATE mensajes_locales SET enviado = :enviado WHERE id = :id")
    suspend fun marcarEnviado(id: Long, enviado: Boolean)

    @Query("UPDATE mensajes_locales SET leido = 1 WHERE id = :id")
    suspend fun marcarLeido(id: Long)

    @Query("UPDATE mensajes_locales SET leido = 1 WHERE solicitudId = :solicitudId")
    suspend fun marcarTodosLeidosDeSolicitud(solicitudId: Int)

    @Query("DELETE FROM mensajes_locales WHERE id = :id")
    suspend fun eliminarPorId(id: Long)

    @Query("DELETE FROM mensajes_locales WHERE solicitudId = :solicitudId")
    suspend fun eliminarPorSolicitud(solicitudId: Int)
}
