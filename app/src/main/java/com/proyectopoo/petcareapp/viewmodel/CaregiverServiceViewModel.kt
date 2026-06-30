package com.proyectopoo.petcareapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectopoo.petcareapp.data.local.dao.ServiceTypeDao
import com.proyectopoo.petcareapp.data.local.entity.OfferedServiceEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceTypeEntity
import com.proyectopoo.petcareapp.data.network.ApiService
import com.proyectopoo.petcareapp.data.network.OfferedServiceDto
import com.proyectopoo.petcareapp.data.repository.OfferedServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CaregiverServiceViewModel(
    private val offeredServiceRepository: OfferedServiceRepository,
    private val serviceTypeDao: ServiceTypeDao,
    private val caregiverId: Int,
    private val apiService: ApiService? = null
) : ViewModel() {

    private val _servicios = MutableStateFlow<List<OfferedServiceEntity>>(emptyList())
    val servicios = _servicios.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadServices()
    }

    fun loadServices() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val remote = runCatching {
                    apiService?.getOfferedServicesByCaregiver(caregiverId)
                        ?.takeIf { it.isSuccessful }
                        ?.body()
                        ?.map { it.toEntity() }
                }.getOrNull()

                if (remote != null) {
                    remote.forEach { service ->
                        ensureServiceType(service.serviceTypeId, service.title)
                        offeredServiceDao.insertOfferedService(service)
                    }
                    _servicios.value = remote
                } else {
                    _servicios.value = offeredServiceDao.getServicesByCaregiver(caregiverId)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addService(
        serviceTypeId: Int,
        title: String,
        price: Double,
        description: String? = null,
        isAvailable: Boolean = true,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        viewModelScope.launch {
            if (price !in 20.0..6000.0) return@launch
            ensureServiceType(serviceTypeId, title)
            val newService = OfferedServiceEntity(
                offeredServiceId = 0, // autoGenerate
                caregiverId = caregiverId,
                serviceTypeId = serviceTypeId,
                title = title,
                description = description,
                price = price,
                isAvailable = isAvailable,
                latitude = latitude,
                longitude = longitude
            )
            val savedService = runCatching {
                apiService?.createOfferedService(newService.toDto())
                    ?.takeIf { it.isSuccessful }
                    ?.body()
                    ?.toEntity()
            }.getOrNull() ?: newService

            offeredServiceDao.insertOfferedService(savedService)
            loadServices() // refrescar lista
        }
    }

    fun updateService(
        id: Int,
        price: Double,
        description: String? = null,
        isAvailable: Boolean
    ) {
        viewModelScope.launch {
            if (price !in 20.0..6000.0) return@launch
            val service = offeredServiceDao.getServiceById(id) ?: return@launch
            val candidate = service.copy(
                price = price,
                description = description,
                isAvailable = isAvailable
            )
            val savedService = runCatching {
                apiService?.updateOfferedService(candidate.offeredServiceId, candidate.toDto())
                    ?.takeIf { it.isSuccessful }
                    ?.body()
                    ?.toEntity()
            }.getOrNull() ?: candidate

            offeredServiceDao.updateService(savedService)
            loadServices()
        }
    }

    fun toggleAvailability(service: OfferedServiceEntity) {
        viewModelScope.launch {
            val updated = service.copy(isAvailable = !service.isAvailable)
            val savedService = runCatching {
                apiService?.updateOfferedService(updated.offeredServiceId, updated.toDto())
                    ?.takeIf { it.isSuccessful }
                    ?.body()
                    ?.toEntity()
            }.getOrNull() ?: updated
            offeredServiceDao.updateService(savedService)
            loadServices()
        }
    }

    fun deleteService(service: OfferedServiceEntity) {
        viewModelScope.launch {
            runCatching { apiService?.deleteOfferedService(service.offeredServiceId) }
            offeredServiceDao.deleteService(service)
            loadServices()
        }
    }

    private suspend fun ensureServiceType(serviceTypeId: Int, title: String) {
        if (serviceTypeDao.getServiceTypeById(serviceTypeId) != null) return

        serviceTypeDao.insertServiceType(
            ServiceTypeEntity(
                serviceTypeId = serviceTypeId,
                name = title,
                description = null
            )
        )
    }
}


private fun OfferedServiceEntity.toDto(): OfferedServiceDto {
    return OfferedServiceDto(
        id = offeredServiceId.takeIf { it > 0 },
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
        offeredServiceId = id ?: 0,
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
