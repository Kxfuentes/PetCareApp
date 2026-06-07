package com.proyectopoo.petcareapp.data.local.dao

import androidx.room.*
import com.proyectopoo.petcareapp.data.local.entity.ApplicationStatus
import com.proyectopoo.petcareapp.data.local.entity.ServiceApplicationEntity
import com.proyectopoo.petcareapp.data.local.relation.ServiceApplicationDetails

@Dao
interface ServiceApplicationDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(application: ServiceApplicationEntity)

    @Query("SELECT * FROM service_applications WHERE caregiverId = :caregiverId")
    suspend fun getByCaregiver(caregiverId: Int): List<ServiceApplicationEntity>

    @Query("SELECT * FROM service_applications WHERE serviceRequestId = :requestId")
    suspend fun getByRequest(requestId: Int): List<ServiceApplicationEntity>

    @Query("UPDATE service_applications SET status = :status WHERE applicationId = :id")
    suspend fun updateStatus(id: Int, status: ApplicationStatus)

    @Query("SELECT * FROM service_applications WHERE status = :status")
    suspend fun getByStatus(status: ApplicationStatus): List<ServiceApplicationEntity>

    @Query(
        """
        SELECT * FROM service_applications
        WHERE serviceRequestId = :serviceRequestId AND caregiverId = :caregiverId
        LIMIT 1
        """
    )
    suspend fun getByRequestAndCaregiver(
        serviceRequestId: Int,
        caregiverId: Int
    ): ServiceApplicationEntity?

    @Query(
        """
        SELECT
            sa.applicationId AS applicationId,
            sa.serviceRequestId AS serviceRequestId,
            sa.caregiverId AS caregiverId,
            sa.status AS applicationStatus,
            sr.title AS requestTitle,
            sr.description AS requestDescription,
            sr.requestedDate AS requestedDate,
            sr.status AS requestStatus,
            p.name AS petName,
            p.breed AS petBreed,
            p.size AS petSize,
            st.name AS serviceTypeName,
            ownerUser.fullName AS ownerName,
            ownerUser.phone AS ownerPhone,
            ownerUser.email AS ownerEmail,
            caregiverUser.fullName AS caregiverName,
            caregiverUser.phone AS caregiverPhone,
            caregiverUser.email AS caregiverEmail
        FROM service_applications sa
        INNER JOIN service_requests sr ON sr.serviceRequestId = sa.serviceRequestId
        LEFT JOIN pets p ON p.petId = sr.petId
        LEFT JOIN service_types st ON st.serviceTypeId = sr.serviceTypeId
        LEFT JOIN owners o ON o.ownerId = sr.ownerId
        LEFT JOIN users ownerUser ON ownerUser.userId = o.userId
        LEFT JOIN caregivers c ON c.caregiverId = sa.caregiverId
        LEFT JOIN users caregiverUser ON caregiverUser.userId = c.userId
        WHERE sr.ownerId = :ownerId
        ORDER BY sa.applicationId DESC
        """
    )
    suspend fun getDetailsByOwner(ownerId: Int): List<ServiceApplicationDetails>

    @Query(
        """
        SELECT
            sa.applicationId AS applicationId,
            sa.serviceRequestId AS serviceRequestId,
            sa.caregiverId AS caregiverId,
            sa.status AS applicationStatus,
            sr.title AS requestTitle,
            sr.description AS requestDescription,
            sr.requestedDate AS requestedDate,
            sr.status AS requestStatus,
            p.name AS petName,
            p.breed AS petBreed,
            p.size AS petSize,
            st.name AS serviceTypeName,
            ownerUser.fullName AS ownerName,
            ownerUser.phone AS ownerPhone,
            ownerUser.email AS ownerEmail,
            caregiverUser.fullName AS caregiverName,
            caregiverUser.phone AS caregiverPhone,
            caregiverUser.email AS caregiverEmail
        FROM service_applications sa
        INNER JOIN service_requests sr ON sr.serviceRequestId = sa.serviceRequestId
        LEFT JOIN pets p ON p.petId = sr.petId
        LEFT JOIN service_types st ON st.serviceTypeId = sr.serviceTypeId
        LEFT JOIN owners o ON o.ownerId = sr.ownerId
        LEFT JOIN users ownerUser ON ownerUser.userId = o.userId
        LEFT JOIN caregivers c ON c.caregiverId = sa.caregiverId
        LEFT JOIN users caregiverUser ON caregiverUser.userId = c.userId
        WHERE sa.caregiverId = :caregiverId
        ORDER BY sa.applicationId DESC
        """
    )
    suspend fun getDetailsByCaregiver(caregiverId: Int): List<ServiceApplicationDetails>
}
