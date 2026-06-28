package com.proyectopoo.petcareapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestPetEntity

@Dao
interface ServiceRequestPetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<ServiceRequestPetEntity>)

    @Query("SELECT petId FROM service_request_pets WHERE serviceRequestId = :requestId")
    suspend fun getPetIdsForRequest(requestId: Int): List<Int>

    @Query(
        """
        SELECT GROUP_CONCAT(p.name, ', ') FROM service_request_pets srp
        INNER JOIN pets p ON p.petId = srp.petId
        WHERE srp.serviceRequestId = :requestId
        """
    )
    suspend fun getPetNamesForRequest(requestId: Int): String?
}
