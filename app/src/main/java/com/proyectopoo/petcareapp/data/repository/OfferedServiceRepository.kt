package com.proyectopoo.petcareapp.data.repository

import com.proyectopoo.petcareapp.data.local.dao.OfferedServiceDao
import com.proyectopoo.petcareapp.data.local.entity.OfferedServiceEntity

class OfferedServiceRepository(
    private val offeredServiceDao: OfferedServiceDao
) {

    suspend fun insertOfferedService(
        service: OfferedServiceEntity
    ) {
        offeredServiceDao.insertOfferedService(service)
    }

    suspend fun insertOfferedServices(
        services: List<OfferedServiceEntity>
    ) {
        offeredServiceDao.insertOfferedServices(services)
    }

    suspend fun getAllServices(): List<OfferedServiceEntity> {
        return offeredServiceDao.getAllServices()
    }

    suspend fun getServiceById(
        serviceId: Int
    ): OfferedServiceEntity? {

        return offeredServiceDao.getServiceById(serviceId)
    }

    suspend fun getServicesByCaregiver(
        caregiverId: Int
    ): List<OfferedServiceEntity> {

        return offeredServiceDao.getServicesByCaregiver(caregiverId)
    }

    suspend fun getAvailableServices(): List<OfferedServiceEntity> {
        return offeredServiceDao.getAvailableServices()
    }

    suspend fun getAvailableServicesByCaregiver(
        caregiverId: Int
    ): List<OfferedServiceEntity> {

        return offeredServiceDao
            .getAvailableServicesByCaregiver(caregiverId)
    }

    suspend fun updateService(
        service: OfferedServiceEntity
    ) {
        offeredServiceDao.updateService(service)
    }

    suspend fun deleteService(
        service: OfferedServiceEntity
    ) {
        offeredServiceDao.deleteService(service)
    }
}