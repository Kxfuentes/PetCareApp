package com.proyectopoo.petcareapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.proyectopoo.petcareapp.data.local.entity.OfferedServiceEntity

@Dao
interface OfferedServiceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfferedService(service: OfferedServiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfferedServices(services: List<OfferedServiceEntity>)

    @Query("SELECT * FROM offered_services")
    suspend fun getAllServices(): List<OfferedServiceEntity>

    @Query("SELECT * FROM offered_services WHERE offeredServiceId = :serviceId LIMIT 1")
    suspend fun getServiceById(serviceId: Int): OfferedServiceEntity?

    @Query("SELECT * FROM offered_services WHERE caregiverId = :caregiverId")
    suspend fun getServicesByCaregiver(caregiverId: Int): List<OfferedServiceEntity>

    @Query("SELECT * FROM offered_services WHERE isAvailable = 1")
    suspend fun getAvailableServices(): List<OfferedServiceEntity>

    @Query("""
        SELECT * FROM offered_services
        WHERE caregiverId = :caregiverId
        AND isAvailable = 1
    """)
    suspend fun getAvailableServicesByCaregiver(
        caregiverId: Int
    ): List<OfferedServiceEntity>

    @Update
    suspend fun updateService(service: OfferedServiceEntity)

    @Delete
    suspend fun deleteService(service: OfferedServiceEntity)

    @Query("DELETE FROM offered_services")
    suspend fun deleteAllServices()
}