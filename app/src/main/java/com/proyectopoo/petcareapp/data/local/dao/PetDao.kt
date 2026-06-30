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
import com.proyectopoo.petcareapp.data.local.entity.PetEntity

@Dao
interface PetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: PetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPets(pets: List<PetEntity>)

    @Query("SELECT * FROM pets")
    suspend fun getAllPets(): List<PetEntity>

    @Query("SELECT * FROM pets WHERE petId = :petId LIMIT 1")
    suspend fun getPetById(petId: Int): PetEntity?

    @Query("SELECT * FROM pets WHERE ownerId = :ownerId")
    suspend fun getPetsByOwner(ownerId: Int): List<PetEntity>

    @Update
    suspend fun updatePet(pet: PetEntity)

    @Delete
    suspend fun deletePet(pet: PetEntity)

    @Query("DELETE FROM pets")
    suspend fun deleteAllPets()
}