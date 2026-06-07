package com.proyectopoo.petcareapp.data.repository

import com.proyectopoo.petcareapp.data.local.dao.ServiceApplicationDao
import com.proyectopoo.petcareapp.data.local.entity.ApplicationStatus
import com.proyectopoo.petcareapp.data.local.entity.ServiceApplicationEntity

class ServiceApplicationRepository(
    private val dao: ServiceApplicationDao
) {
    suspend fun insert(application: ServiceApplicationEntity) {
        val existing = dao.getByRequestAndCaregiver(
            serviceRequestId = application.serviceRequestId,
            caregiverId = application.caregiverId
        )

        if (existing == null) {
            dao.insert(application)
        }
    }

    suspend fun getByCaregiver(caregiverId: Int) = dao.getByCaregiver(caregiverId)
    suspend fun getDetailsByOwner(ownerId: Int) = dao.getDetailsByOwner(ownerId)
    suspend fun getDetailsByCaregiver(caregiverId: Int) = dao.getDetailsByCaregiver(caregiverId)
    suspend fun updateStatus(id: Int, status: ApplicationStatus) = dao.updateStatus(id, status)
}
