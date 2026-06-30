package com.proyectopoo.petcareapp.data.local.dao

/*
 * Comentario de modulo PetCare:
 * Acceso local a datos. Aqui se definen las consultas que Room usa para leer y guardar informacion.
 */

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.proyectopoo.petcareapp.data.local.entity.AvailabilityEntity

@Dao
interface AvailabilityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAvailability(
        availability: AvailabilityEntity
    )

    @Query("SELECT * FROM availability")
    suspend fun getAllAvailability(): List<AvailabilityEntity>

    @Query("""
        SELECT * FROM availability
        WHERE caregiverId = :caregiverId
        LIMIT 1
    """)
    suspend fun getAvailabilityByCaregiver(
        caregiverId: Int
    ): AvailabilityEntity?

    @Update
    suspend fun updateAvailability(
        availability: AvailabilityEntity
    )

    @Delete
    suspend fun deleteAvailability(
        availability: AvailabilityEntity
    )

    @Query("DELETE FROM availability")
    suspend fun deleteAllAvailability()
}