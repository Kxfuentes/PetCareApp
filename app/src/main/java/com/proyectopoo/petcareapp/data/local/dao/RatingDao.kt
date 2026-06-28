package com.proyectopoo.petcareapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.proyectopoo.petcareapp.data.local.entity.RatingEntity

@Dao
interface RatingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: RatingEntity)

    @Query("SELECT AVG(score) FROM ratings WHERE caregiverId = :caregiverId")
    suspend fun getAverageRatingForCaregiver(caregiverId: String): Double?

    @Query("SELECT COUNT(*) FROM ratings WHERE caregiverId = :caregiverId")
    suspend fun getRatingCountForCaregiver(caregiverId: Int): Int
}