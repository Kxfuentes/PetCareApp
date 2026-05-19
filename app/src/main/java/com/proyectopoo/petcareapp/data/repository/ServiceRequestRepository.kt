package com.proyectopoo.petcareapp.data.repository

import com.proyectopoo.petcareapp.data.local.dao.ServiceRequestDao
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestStatus

class ServiceRequestRepository(
    private val serviceRequestDao: ServiceRequestDao
) {

    suspend fun insertRequest(
        request: ServiceRequestEntity
    ) {
        serviceRequestDao.insertRequest(request)
    }

    suspend fun insertRequests(
        requests: List<ServiceRequestEntity>
    ) {
        serviceRequestDao.insertRequests(requests)
    }

    suspend fun getAllRequests(): List<ServiceRequestEntity> {
        return serviceRequestDao.getAllRequests()
    }

    suspend fun getRequestById(
        requestId: Int
    ): ServiceRequestEntity? {

        return serviceRequestDao.getRequestById(requestId)
    }

    suspend fun getRequestsByOwner(
        ownerId: Int
    ): List<ServiceRequestEntity> {

        return serviceRequestDao.getRequestsByOwner(ownerId)
    }

    suspend fun getRequestsByStatus(
        status: ServiceRequestStatus
    ): List<ServiceRequestEntity> {

        return serviceRequestDao.getRequestsByStatus(status)
    }

    suspend fun updateRequest(
        request: ServiceRequestEntity
    ) {
        serviceRequestDao.updateRequest(request)
    }

    suspend fun deleteRequest(
        request: ServiceRequestEntity
    ) {
        serviceRequestDao.deleteRequest(request)
    }
}