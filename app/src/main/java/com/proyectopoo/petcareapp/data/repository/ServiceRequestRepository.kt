package com.proyectopoo.petcareapp.data.repository

import com.proyectopoo.petcareapp.data.local.dao.ServiceRequestDao
import com.proyectopoo.petcareapp.data.local.dao.ServiceRequestPetDao
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestPetEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestStatus

class ServiceRequestRepository(
    private val dao: ServiceRequestDao,
    private val petDao: ServiceRequestPetDao
) {
    suspend fun insert(request: ServiceRequestEntity, petIds: List<Int> = emptyList()) {
        dao.insertRequest(request)
        val pets = petIds.ifEmpty { listOf(request.petId) }
        petDao.insertAll(
            pets.map { petId ->
                ServiceRequestPetEntity(
                    serviceRequestId = request.serviceRequestId,
                    petId = petId
                )
            }
        )
    }

    suspend fun getByOwner(ownerId: Int) = dao.getRequestsByOwner(ownerId)
    suspend fun getWithApplications(ownerId: Int) = dao.getRequestsWithApplications(ownerId)
    suspend fun getAvailableDetails() = dao.getRequestDetailsByStatus(ServiceRequestStatus.PENDING)
    suspend fun getRecentDetailsByOwner(ownerId: Int) = dao.getRecentRequestDetailsByOwner(ownerId)
    suspend fun getDetailsByOwnerAndStatus(ownerId: Int, status: ServiceRequestStatus) =
        dao.getRequestDetailsByOwnerAndStatus(ownerId, status)
    suspend fun updateStatus(id: Int, status: ServiceRequestStatus) = dao.updateStatus(id, status)
    suspend fun getRequestById(id: Int) = dao.getRequestById(id)
}
