package com.proyectopoo.petcareapp.data.local.dao

import androidx.room.*
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestStatus
import com.proyectopoo.petcareapp.data.local.relation.ServiceRequestDetails
import com.proyectopoo.petcareapp.data.local.relation.RequestWithApplications

@Dao
interface ServiceRequestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: ServiceRequestEntity)

    @Query("SELECT * FROM service_requests WHERE status = :status")
    suspend fun getRequestsByStatus(status: ServiceRequestStatus): List<ServiceRequestEntity>

    @Query("SELECT * FROM service_requests WHERE ownerId = :ownerId ORDER BY serviceRequestId DESC")
    suspend fun getRequestsByOwner(ownerId: Int): List<ServiceRequestEntity>

    @Transaction
    @Query("SELECT * FROM service_requests WHERE ownerId = :ownerId")
    suspend fun getRequestsWithApplications(ownerId: Int): List<RequestWithApplications>

    @Query("UPDATE service_requests SET status = :status WHERE serviceRequestId = :id")
    suspend fun updateStatus(id: Int, status: ServiceRequestStatus)

    @Query(
        """
        UPDATE service_requests
        SET requestedDate = :date, startTime = :startTime, endTime = :endTime
        WHERE serviceRequestId = :id
        """
    )
    suspend fun updateSchedule(id: Int, date: String?, startTime: String?, endTime: String?)

    @Query("SELECT * FROM service_requests WHERE serviceRequestId = :id")
    suspend fun getRequestById(id: Int): ServiceRequestEntity?

    @Query(
        """
        SELECT
            sr.serviceRequestId AS serviceRequestId,
            sr.ownerId AS ownerId,
            sr.petId AS petId,
            sr.serviceTypeId AS serviceTypeId,
            sr.title AS title,
            sr.description AS description,
            sr.requestedDate AS requestedDate,
            sr.startTime AS startTime,
            sr.endTime AS endTime,
            sr.status AS status,
            p.name AS petName,
            (
                SELECT GROUP_CONCAT(p2.name, ', ')
                FROM service_request_pets srp
                INNER JOIN pets p2 ON p2.petId = srp.petId
                WHERE srp.serviceRequestId = sr.serviceRequestId
            ) AS petNames,
            st.name AS serviceTypeName,
            u.fullName AS ownerName,
            u.phone AS ownerPhone,
            u.email AS ownerEmail
        FROM service_requests sr
        LEFT JOIN pets p ON p.petId = sr.petId
        LEFT JOIN service_types st ON st.serviceTypeId = sr.serviceTypeId
        LEFT JOIN owners o ON o.ownerId = sr.ownerId
        LEFT JOIN users u ON u.userId = o.userId
        WHERE sr.status = :status AND sr.sourceType = 'OPEN'
        ORDER BY sr.serviceRequestId DESC
        """
    )
    suspend fun getRequestDetailsByStatus(status: ServiceRequestStatus): List<ServiceRequestDetails>

    @Query(
        """
        SELECT
            sr.serviceRequestId AS serviceRequestId,
            sr.ownerId AS ownerId,
            sr.petId AS petId,
            sr.serviceTypeId AS serviceTypeId,
            sr.title AS title,
            sr.description AS description,
            sr.requestedDate AS requestedDate,
            sr.startTime AS startTime,
            sr.endTime AS endTime,
            sr.status AS status,
            p.name AS petName,
            (
                SELECT GROUP_CONCAT(p2.name, ', ')
                FROM service_request_pets srp
                INNER JOIN pets p2 ON p2.petId = srp.petId
                WHERE srp.serviceRequestId = sr.serviceRequestId
            ) AS petNames,
            st.name AS serviceTypeName,
            u.fullName AS ownerName,
            u.phone AS ownerPhone,
            u.email AS ownerEmail
        FROM service_requests sr
        LEFT JOIN pets p ON p.petId = sr.petId
        LEFT JOIN service_types st ON st.serviceTypeId = sr.serviceTypeId
        LEFT JOIN owners o ON o.ownerId = sr.ownerId
        LEFT JOIN users u ON u.userId = o.userId
        WHERE sr.ownerId = :ownerId
        ORDER BY sr.serviceRequestId DESC
        LIMIT :limit
        """
    )
    suspend fun getRecentRequestDetailsByOwner(ownerId: Int, limit: Int = 50): List<ServiceRequestDetails>

    @Query(
        """
        SELECT
            sr.serviceRequestId AS serviceRequestId,
            sr.ownerId AS ownerId,
            sr.petId AS petId,
            sr.serviceTypeId AS serviceTypeId,
            sr.title AS title,
            sr.description AS description,
            sr.requestedDate AS requestedDate,
            sr.startTime AS startTime,
            sr.endTime AS endTime,
            sr.status AS status,
            p.name AS petName,
            (
                SELECT GROUP_CONCAT(p2.name, ', ')
                FROM service_request_pets srp
                INNER JOIN pets p2 ON p2.petId = srp.petId
                WHERE srp.serviceRequestId = sr.serviceRequestId
            ) AS petNames,
            st.name AS serviceTypeName,
            u.fullName AS ownerName,
            u.phone AS ownerPhone,
            u.email AS ownerEmail
        FROM service_requests sr
        LEFT JOIN pets p ON p.petId = sr.petId
        LEFT JOIN service_types st ON st.serviceTypeId = sr.serviceTypeId
        LEFT JOIN owners o ON o.ownerId = sr.ownerId
        LEFT JOIN users u ON u.userId = o.userId
        WHERE sr.ownerId = :ownerId AND sr.status = :status
        ORDER BY sr.serviceRequestId DESC
        """
    )
    suspend fun getRequestDetailsByOwnerAndStatus(
        ownerId: Int,
        status: ServiceRequestStatus
    ): List<ServiceRequestDetails>
}
