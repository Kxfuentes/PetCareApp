package com.proyectopoo.petcareapp.data.local.dao

/*
 * Comentario de modulo PetCare:
 * Acceso local a datos. Aqui se definen las consultas que Room usa para leer y guardar informacion.
 */

import androidx.room.*
import com.proyectopoo.petcareapp.data.local.entity.ApplicationInitiator
import com.proyectopoo.petcareapp.data.local.entity.ApplicationStatus
import com.proyectopoo.petcareapp.data.local.entity.ServiceApplicationEntity
import com.proyectopoo.petcareapp.data.local.relation.ServiceApplicationDetails

@Dao
interface ServiceApplicationDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(application: ServiceApplicationEntity): Long

    @Query("SELECT * FROM service_applications WHERE caregiverId = :caregiverId")
    suspend fun getByCaregiver(caregiverId: Int): List<ServiceApplicationEntity>

    @Query("SELECT * FROM service_applications WHERE serviceRequestId = :requestId")
    suspend fun getByRequest(requestId: Int): List<ServiceApplicationEntity>

    @Query("UPDATE service_applications SET status = :status WHERE applicationId = :id")
    suspend fun updateStatus(id: Int, status: ApplicationStatus)

    @Query("SELECT * FROM service_applications WHERE applicationId = :id LIMIT 1")
    suspend fun getById(id: Int): ServiceApplicationEntity?

    @Query(
        """
        UPDATE service_requests
        SET status = :requestStatus
        WHERE serviceRequestId = (
            SELECT serviceRequestId FROM service_applications WHERE applicationId = :applicationId
        )
        """
    )
    suspend fun updateRequestStatusForApplication(
        applicationId: Int,
        requestStatus: com.proyectopoo.petcareapp.data.local.entity.ServiceRequestStatus
    )

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

    @Query("SELECT COUNT(*) FROM service_applications WHERE caregiverId = :caregiverId AND status = :status")
    suspend fun countByCaregiverAndStatus(caregiverId: String, status: ApplicationStatus): Int

    @Query(
        """
        UPDATE service_applications SET status = :status
        WHERE serviceRequestId = :requestId AND applicationId != :exceptId AND status = 'PENDING'
        """
    )
    suspend fun rejectOtherApplications(requestId: Int, exceptId: Int, status: ApplicationStatus)

    @Query(
        """
        SELECT
            sa.applicationId AS applicationId,
            sa.serviceRequestId AS serviceRequestId,
            sa.caregiverId AS caregiverId,
            sr.ownerId AS ownerId,
            sa.offeredServiceId AS offeredServiceId,
            sa.initiatedBy AS initiatedBy,
            sa.status AS applicationStatus,
            sr.title AS requestTitle,
            sr.description AS requestDescription,
            sr.requestedDate AS requestedDate,
            sr.startTime AS startTime,
            sr.endTime AS endTime,
            sr.status AS requestStatus,
            p.name AS petName,
            (
                SELECT GROUP_CONCAT(p2.name, ', ')
                FROM service_request_pets srp
                INNER JOIN pets p2 ON p2.petId = srp.petId
                WHERE srp.serviceRequestId = sr.serviceRequestId
            ) AS petNames,
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
        AND sa.initiatedBy = :initiatedBy
        ORDER BY sa.applicationId DESC
        """
    )
    suspend fun getDetailsByOwner(
        ownerId: Int,
        initiatedBy: ApplicationInitiator = ApplicationInitiator.CAREGIVER
    ): List<ServiceApplicationDetails>

    @Query(
        """
        SELECT
            sa.applicationId AS applicationId,
            sa.serviceRequestId AS serviceRequestId,
            sa.caregiverId AS caregiverId,
            sr.ownerId AS ownerId,
            sa.offeredServiceId AS offeredServiceId,
            sa.initiatedBy AS initiatedBy,
            sa.status AS applicationStatus,
            sr.title AS requestTitle,
            sr.description AS requestDescription,
            sr.requestedDate AS requestedDate,
            sr.startTime AS startTime,
            sr.endTime AS endTime,
            sr.status AS requestStatus,
            p.name AS petName,
            (
                SELECT GROUP_CONCAT(p2.name, ', ')
                FROM service_request_pets srp
                INNER JOIN pets p2 ON p2.petId = srp.petId
                WHERE srp.serviceRequestId = sr.serviceRequestId
            ) AS petNames,
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
        AND sa.initiatedBy = :initiatedBy
        ORDER BY sa.applicationId DESC
        """
    )
    suspend fun getDetailsByCaregiver(
        caregiverId: Int,
        initiatedBy: ApplicationInitiator = ApplicationInitiator.OWNER
    ): List<ServiceApplicationDetails>

    @Query(
        """
        SELECT
            sa.applicationId AS applicationId,
            sa.serviceRequestId AS serviceRequestId,
            sa.caregiverId AS caregiverId,
            sr.ownerId AS ownerId,
            sa.offeredServiceId AS offeredServiceId,
            sa.initiatedBy AS initiatedBy,
            sa.status AS applicationStatus,
            sr.title AS requestTitle,
            sr.description AS requestDescription,
            sr.requestedDate AS requestedDate,
            sr.startTime AS startTime,
            sr.endTime AS endTime,
            sr.status AS requestStatus,
            p.name AS petName,
            (
                SELECT GROUP_CONCAT(p2.name, ', ')
                FROM service_request_pets srp
                INNER JOIN pets p2 ON p2.petId = srp.petId
                WHERE srp.serviceRequestId = sr.serviceRequestId
            ) AS petNames,
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
        AND sa.offeredServiceId = :offeredServiceId
        ORDER BY sa.applicationId DESC
        """
    )
    suspend fun getDetailsByOffer(
        caregiverId: Int,
        offeredServiceId: Int
    ): List<ServiceApplicationDetails>
}
