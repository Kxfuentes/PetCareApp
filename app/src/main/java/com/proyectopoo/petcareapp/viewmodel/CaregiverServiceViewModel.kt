package com.proyectopoo.petcareapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectopoo.petcareapp.data.local.dao.OfferedServiceDao
import com.proyectopoo.petcareapp.data.local.dao.ServiceTypeDao
import com.proyectopoo.petcareapp.data.local.entity.OfferedServiceEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceTypeEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CaregiverServiceViewModel(
    private val offeredServiceDao: OfferedServiceDao,
    private val serviceTypeDao: ServiceTypeDao,
    private val caregiverId: Int
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
                _servicios.value = offeredServiceDao.getServicesByCaregiver(caregiverId)
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
        isAvailable: Boolean = true
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
                isAvailable = isAvailable
            )
            offeredServiceDao.insertOfferedService(newService)
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
            offeredServiceDao.updateService(
                service.copy(
                    price = price,
                    description = description,
                    isAvailable = isAvailable
                )
            )
            loadServices()
        }
    }

    fun toggleAvailability(service: OfferedServiceEntity) {
        viewModelScope.launch {
            val updated = service.copy(isAvailable = !service.isAvailable)
            offeredServiceDao.updateService(updated)
            loadServices()
        }
    }

    fun deleteService(service: OfferedServiceEntity) {
        viewModelScope.launch {
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
