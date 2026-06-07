package com.proyectopoo.petcareapp.data.repository

import com.proyectopoo.petcareapp.data.local.dao.ServiceRequestDao
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestStatus

class ServiceRequestRepository(
    private val dao: ServiceRequestDao
) {
    suspend fun insert(request: ServiceRequestEntity) = dao.insertRequest(request)
    suspend fun getByOwner(ownerId: Int) = dao.getRequestsByOwner(ownerId)
    suspend fun getWithApplications(ownerId: Int) = dao.getRequestsWithApplications(ownerId)
    suspend fun getAvailableDetails() = dao.getRequestDetailsByStatus(ServiceRequestStatus.PENDING)
    suspend fun getRecentDetailsByOwner(ownerId: Int) = dao.getRecentRequestDetailsByOwner(ownerId)
    suspend fun updateStatus(id: Int, status: ServiceRequestStatus) = dao.updateStatus(id, status)
}
