package com.proyectopoo.petcareapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.proyectopoo.petcareapp.data.local.entity.EvidenciaLocalEntity

@Dao
interface EvidenciaLocalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(evidencia: EvidenciaLocalEntity)

    @Query("SELECT * FROM evidencias_locales WHERE solicitudId = :solicitudId ORDER BY fecha ASC, id ASC")
    suspend fun obtenerPorSolicitud(solicitudId: Int): List<EvidenciaLocalEntity>

    @Query("SELECT * FROM evidencias_locales WHERE enviado = 0 ORDER BY fecha ASC, id ASC")
    suspend fun obtenerTodosPendientes(): List<EvidenciaLocalEntity>

    @Query("DELETE FROM evidencias_locales WHERE id = :id")
    suspend fun eliminarPorId(id: Long)
}
