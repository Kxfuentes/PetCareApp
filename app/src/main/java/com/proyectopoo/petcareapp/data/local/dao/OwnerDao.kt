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
import com.proyectopoo.petcareapp.data.local.entity.OwnerEntity

@Dao
interface OwnerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwner(owner: OwnerEntity)

    @Query("SELECT * FROM owners")
    suspend fun getAllOwners(): List<OwnerEntity>

    @Query("SELECT * FROM owners WHERE ownerId = :ownerId LIMIT 1")
    suspend fun getOwnerById(ownerId: Int): OwnerEntity?

    @Query("SELECT * FROM owners WHERE userId = :userId LIMIT 1")
    suspend fun getOwnerByUserId(userId: Int): OwnerEntity?

    @Update
    suspend fun updateOwner(owner: OwnerEntity)

    @Delete
    suspend fun deleteOwner(owner: OwnerEntity)

    @Query("DELETE FROM owners")
    suspend fun deleteAllOwners()
}