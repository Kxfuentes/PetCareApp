package com.proyectopoo.petcareapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.proyectopoo.petcareapp.data.local.entity.OfferedServiceEntity
import com.proyectopoo.petcareapp.data.local.relation.OfferedServiceDetails

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
        SELECT
            os.offeredServiceId AS offeredServiceId,
            os.caregiverId AS caregiverId,
            os.serviceTypeId AS serviceTypeId,
            os.title AS title,
            os.description AS description,
            os.price AS price,
            os.isAvailable AS isAvailable,
            os.createdAt AS createdAt,
            st.name AS serviceTypeName,
            u.fullName AS caregiverName,
            u.phone AS caregiverPhone,
            u.email AS caregiverEmail,
            AVG(r.score) AS caregiverRating,
            COUNT(r.ratingId) AS caregiverRatingCount
        FROM offered_services os
        LEFT JOIN service_types st ON st.serviceTypeId = os.serviceTypeId
        LEFT JOIN caregivers c ON c.caregiverId = os.caregiverId
        LEFT JOIN users u ON u.userId = c.userId
        LEFT JOIN ratings r ON r.caregiverId = os.caregiverId AND r.ratedByRole = 'OWNER'
        WHERE os.isAvailable = 1
        GROUP BY os.offeredServiceId
        ORDER BY os.createdAt DESC
    """)
    suspend fun getAvailableServiceDetails(): List<OfferedServiceDetails>

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
