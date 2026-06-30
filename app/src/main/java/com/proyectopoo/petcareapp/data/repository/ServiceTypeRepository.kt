package com.proyectopoo.petcareapp.data.repository

/*
 * Comentario de modulo PetCare:
 * Repositorio de datos. Centraliza llamadas a Room y API para que la pantalla no maneje detalles tecnicos.
 */

import com.proyectopoo.petcareapp.data.local.dao.ServiceTypeDao
import com.proyectopoo.petcareapp.data.local.entity.ServiceTypeEntity

class ServiceTypeRepository(
    private val serviceTypeDao: ServiceTypeDao
) {

    suspend fun insertServiceType(
        serviceType: ServiceTypeEntity
    ) {
        serviceTypeDao.insertServiceType(serviceType)
    }

    suspend fun insertServiceTypes(
        serviceTypes: List<ServiceTypeEntity>
    ) {
        serviceTypeDao.insertServiceTypes(serviceTypes)
    }

    suspend fun getAllServiceTypes(): List<ServiceTypeEntity> {
        return serviceTypeDao.getAllServiceTypes()
    }

    suspend fun getServiceTypeById(
        serviceTypeId: Int
    ): ServiceTypeEntity? {

        return serviceTypeDao.getServiceTypeById(serviceTypeId)
    }

    suspend fun updateServiceType(
        serviceType: ServiceTypeEntity
    ) {
        serviceTypeDao.updateServiceType(serviceType)
    }

    suspend fun deleteServiceType(
        serviceType: ServiceTypeEntity
    ) {
        serviceTypeDao.deleteServiceType(serviceType)
    }
}