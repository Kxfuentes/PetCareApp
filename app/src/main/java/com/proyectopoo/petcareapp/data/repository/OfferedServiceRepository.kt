package com.proyectopoo.petcareapp.data.repository

/*
 * Comentario de modulo PetCare:
 * Repositorio de datos. Centraliza llamadas a Room y API para que la pantalla no maneje detalles tecnicos.
 */

import com.proyectopoo.petcareapp.data.local.dao.OfferedServiceDao
import com.proyectopoo.petcareapp.data.local.entity.OfferedServiceEntity
import com.proyectopoo.petcareapp.data.local.relation.OfferedServiceDetails
import com.proyectopoo.petcareapp.data.network.ApiService
import com.proyectopoo.petcareapp.data.network.OfferedServiceDto
import com.proyectopoo.petcareapp.data.network.OfferedServiceRequest
import com.proyectopoo.petcareapp.data.network.RetrofitClient

class OfferedServiceRepository(
    private val offeredServiceDao: OfferedServiceDao,
    private val apiService: ApiService = RetrofitClient.apiService
) {

    suspend fun insertOfferedService(
        service: OfferedServiceEntity
    ) {
        val remoteResponse = runCatching {
            apiService.createOfferedService(service.toRequest())
        }.getOrNull()

        val serviceToSave = if (remoteResponse?.isSuccessful == true) {
            remoteResponse.body()?.toEntity() ?: service
        } else {
            service
        }

        offeredServiceDao.insertOfferedService(serviceToSave)
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
        val remoteResponse = runCatching {
            apiService.getOfferedServiceById(serviceId)
        }.getOrNull()

        return if (remoteResponse?.isSuccessful == true) {
            remoteResponse.body()?.toEntity()
        } else {
            offeredServiceDao.getServiceById(serviceId)
        }
    }

    suspend fun getServicesByCaregiver(
        caregiverId: Int
    ): List<OfferedServiceEntity> {
        val remoteResponse = runCatching {
            apiService.getOfferedServicesByCaregiver(caregiverId)
        }.getOrNull()

        if (remoteResponse?.isSuccessful == true) {
            val remoteServices = remoteResponse.body().orEmpty().map { it.toEntity() }
            offeredServiceDao.insertOfferedServices(remoteServices)
            return remoteServices
        }

        return offeredServiceDao.getServicesByCaregiver(caregiverId)
    }

    suspend fun getAvailableServices(): List<OfferedServiceEntity> {
        val remoteResponse = runCatching {
            apiService.getAvailableOfferedServices()
        }.getOrNull()

        if (remoteResponse?.isSuccessful == true) {
            return remoteResponse.body().orEmpty().map { it.toEntity() }
        }

        return offeredServiceDao.getAvailableServices()
    }

    suspend fun getAvailableServiceDetailsFromApi(): List<OfferedServiceDetails> {
        val remoteResponse = runCatching {
            apiService.getAvailableOfferedServices()
        }.getOrNull()

        if (remoteResponse?.isSuccessful != true) {
            return offeredServiceDao.getAvailableServiceDetails().map { detail ->
                detail.copy(caregiverRating = detail.caregiverRating ?: 5.0)
            }
        }

        return remoteResponse.body().orEmpty()
            .filter { it.isAvailable }
            .map { dto ->
                OfferedServiceDetails(
                    offeredServiceId = dto.id,
                    caregiverId = dto.caregiverId,
                    serviceTypeId = dto.serviceTypeId,
                    title = dto.title,
                    description = dto.description,
                    price = dto.price,
                    isAvailable = dto.isAvailable,
                    createdAt = System.currentTimeMillis(),
                    serviceTypeName = dto.title,
                    caregiverName = "Cuidador #${dto.caregiverId}",
                    caregiverPhone = null,
                    caregiverEmail = null,
                    caregiverRating = getCaregiverRating(dto.caregiverId),
                    caregiverRatingCount = getCaregiverRatingCount(dto.caregiverId)
                )
            }
    }

    suspend fun getAvailableServicesByCaregiver(
        caregiverId: Int
    ): List<OfferedServiceEntity> {
        val remoteResponse = runCatching {
            apiService.getOfferedServicesByCaregiver(caregiverId)
        }.getOrNull()

        if (remoteResponse?.isSuccessful == true) {
            return remoteResponse.body().orEmpty()
                .filter { it.isAvailable }
                .map { it.toEntity() }
        }

        return offeredServiceDao
            .getAvailableServicesByCaregiver(caregiverId)
    }

    suspend fun updateService(
        service: OfferedServiceEntity
    ) {
        runCatching {
            apiService.updateOfferedService(service.offeredServiceId, service.toRequest())
        }
        offeredServiceDao.updateService(service)
    }

    suspend fun deleteService(
        service: OfferedServiceEntity
    ) {
        runCatching {
            apiService.deleteOfferedService(service.offeredServiceId)
        }
        offeredServiceDao.deleteService(service)
    }

    private suspend fun getCaregiverRating(caregiverId: Int): Double {
        val response = runCatching {
            apiService.getCaregiverRatingSummary(caregiverId)
        }.getOrNull()

        return response?.body()?.average?.takeIf { it > 0.0 } ?: 5.0
    }

    private suspend fun getCaregiverRatingCount(caregiverId: Int): Int {
        val response = runCatching {
            apiService.getCaregiverRatingSummary(caregiverId)
        }.getOrNull()

        return response?.body()?.count ?: 0
    }

    private fun OfferedServiceEntity.toRequest(): OfferedServiceRequest {
        return OfferedServiceRequest(
            caregiverId = caregiverId,
            serviceTypeId = serviceTypeId,
            title = title,
            description = description,
            price = price,
            isAvailable = isAvailable,
            latitude = latitude,
            longitude = longitude
        )
    }

    private fun OfferedServiceDto.toEntity(): OfferedServiceEntity {
        return OfferedServiceEntity(
            offeredServiceId = id,
            caregiverId = caregiverId,
            serviceTypeId = serviceTypeId,
            title = title,
            description = description,
            price = price,
            isAvailable = isAvailable,
            latitude = latitude,
            longitude = longitude
        )
    }
}
