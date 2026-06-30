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
import com.proyectopoo.petcareapp.data.local.entity.CaregiverEntity

@Dao
interface CaregiverDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaregiver(caregiver: CaregiverEntity)

    @Query("SELECT * FROM caregivers")
    suspend fun getAllCaregivers(): List<CaregiverEntity>

    @Query("SELECT * FROM caregivers WHERE caregiverId = :caregiverId LIMIT 1")
    suspend fun getCaregiverById(caregiverId: Int): CaregiverEntity?

    @Query("SELECT * FROM caregivers WHERE userId = :userId LIMIT 1")
    suspend fun getCaregiverByUserId(userId: String): CaregiverEntity?

    @Update
    suspend fun updateCaregiver(caregiver: CaregiverEntity)

    @Delete
    suspend fun deleteCaregiver(caregiver: CaregiverEntity)

    @Query("DELETE FROM caregivers")
    suspend fun deleteAllCaregivers()
}