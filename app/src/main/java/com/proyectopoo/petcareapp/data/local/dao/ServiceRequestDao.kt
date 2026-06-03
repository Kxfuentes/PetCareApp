package com.proyectopoo.petcareapp.data.local.dao

import androidx.room.*
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestStatus
import com.proyectopoo.petcareapp.data.local.relation.RequestWithApplications

@Dao
interface ServiceRequestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: ServiceRequestEntity)

    @Query("SELECT * FROM service_requests WHERE status = :status")
    suspend fun getRequestsByStatus(status: ServiceRequestStatus): List<ServiceRequestEntity>

    @Query("SELECT * FROM service_requests WHERE ownerId = :ownerId")
    suspend fun getRequestsByOwner(ownerId: Int): List<ServiceRequestEntity>

    @Transaction
    @Query("SELECT * FROM service_requests WHERE ownerId = :ownerId")
    suspend fun getRequestsWithApplications(ownerId: Int): List<RequestWithApplications>

    @Query("UPDATE service_requests SET status = :status WHERE serviceRequestId = :id")
    suspend fun updateStatus(id: Int, status: ServiceRequestStatus)

    @Query("SELECT * FROM service_requests WHERE serviceRequestId = :id")
    suspend fun getRequestById(id: Int): ServiceRequestEntity?
}