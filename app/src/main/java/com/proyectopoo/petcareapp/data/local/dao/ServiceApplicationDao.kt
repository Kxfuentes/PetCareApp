package com.proyectopoo.petcareapp.data.local.dao

import androidx.room.*
import com.proyectopoo.petcareapp.data.local.entity.ApplicationStatus
import com.proyectopoo.petcareapp.data.local.entity.ServiceApplicationEntity

@Dao
interface ServiceApplicationDao {

    @Insert
    suspend fun insert(application: ServiceApplicationEntity)

    @Query("SELECT * FROM service_applications WHERE caregiverId = :caregiverId")
    suspend fun getByCaregiver(caregiverId: Int): List<ServiceApplicationEntity>

    @Query("SELECT * FROM service_applications WHERE serviceRequestId = :requestId")
    suspend fun getByRequest(requestId: Int): List<ServiceApplicationEntity>

    @Query("UPDATE service_applications SET status = :status WHERE applicationId = :id")
    suspend fun updateStatus(id: Int, status: ApplicationStatus)

    @Query("SELECT * FROM service_applications WHERE status = :status")
    suspend fun getByStatus(status: ApplicationStatus): List<ServiceApplicationEntity>
}