package com.proyectopoo.petcareapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.proyectopoo.petcareapp.data.local.entity.ServiceTypeEntity

@Dao
interface ServiceTypeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceType(serviceType: ServiceTypeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceTypes(serviceTypes: List<ServiceTypeEntity>)

    @Query("SELECT * FROM service_types")
    suspend fun getAllServiceTypes(): List<ServiceTypeEntity>

    @Query("SELECT * FROM service_types WHERE serviceTypeId = :serviceTypeId LIMIT 1")
    suspend fun getServiceTypeById(serviceTypeId: Int): ServiceTypeEntity?

    @Update
    suspend fun updateServiceType(serviceType: ServiceTypeEntity)

    @Delete
    suspend fun deleteServiceType(serviceType: ServiceTypeEntity)

    @Query("DELETE FROM service_types")
    suspend fun deleteAllServiceTypes()
}