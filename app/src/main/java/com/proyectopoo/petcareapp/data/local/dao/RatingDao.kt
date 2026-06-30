package com.proyectopoo.petcareapp.data.local.dao

/*
 * Comentario de modulo PetCare:
 * Acceso local a datos. Aqui se definen las consultas que Room usa para leer y guardar informacion.
 */

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.proyectopoo.petcareapp.data.local.entity.RatingEntity
import com.proyectopoo.petcareapp.data.local.entity.UserRoleType

@Dao
interface RatingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: RatingEntity)

    @Query("SELECT AVG(score) FROM ratings WHERE caregiverId = :caregiverId AND ratedByRole = 'OWNER'")
    suspend fun getAverageRatingForCaregiver(caregiverId: Int): Double?

    @Query("SELECT COUNT(*) FROM ratings WHERE caregiverId = :caregiverId AND ratedByRole = 'OWNER'")
    suspend fun getRatingCountForCaregiver(caregiverId: Int): Int

    @Query("SELECT AVG(score) FROM ratings WHERE ownerId = :ownerId AND ratedByRole = 'CAREGIVER'")
    suspend fun getAverageRatingForOwner(ownerId: Int): Double?

    @Query(
        """
        SELECT * FROM ratings
        WHERE serviceRequestId = :serviceRequestId AND ratedByRole = :ratedByRole
        LIMIT 1
        """
    )
    suspend fun getRatingForServiceByRole(
        serviceRequestId: Int,
        ratedByRole: UserRoleType
    ): RatingEntity?
}
