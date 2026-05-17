package com.proyectopoo.petcareapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestStatus

@Dao
interface ServiceRequestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: ServiceRequestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequests(requests: List<ServiceRequestEntity>)

    @Query("SELECT * FROM service_requests")
    suspend fun getAllRequests(): List<ServiceRequestEntity>

    @Query("""
        SELECT * FROM service_requests
        WHERE serviceRequestId = :requestId
        LIMIT 1
    """)
    suspend fun getRequestById(
        requestId: Int
    ): ServiceRequestEntity?

    @Query("""
        SELECT * FROM service_requests
        WHERE ownerId = :ownerId
    """)
    suspend fun getRequestsByOwner(
        ownerId: Int
    ): List<ServiceRequestEntity>

    @Query("""
        SELECT * FROM service_requests
        WHERE status = :status
    """)
    suspend fun getRequestsByStatus(
        status: ServiceRequestStatus
    ): List<ServiceRequestEntity>

    @Update
    suspend fun updateRequest(
        request: ServiceRequestEntity
    )

    @Delete
    suspend fun deleteRequest(
        request: ServiceRequestEntity
    )

    @Query("DELETE FROM service_requests")
    suspend fun deleteAllRequests()
}