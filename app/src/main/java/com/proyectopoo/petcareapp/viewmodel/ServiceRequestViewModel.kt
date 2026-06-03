package com.proyectopoo.petcareapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectopoo.petcareapp.data.local.entity.ApplicationStatus
import com.proyectopoo.petcareapp.data.local.entity.ServiceApplicationEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestStatus
import com.proyectopoo.petcareapp.data.local.relation.RequestWithApplications
import com.proyectopoo.petcareapp.data.repository.ServiceApplicationRepository
import com.proyectopoo.petcareapp.data.repository.ServiceRequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServiceRequestViewModel(
    private val requestRepo: ServiceRequestRepository,
    private val applicationRepo: ServiceApplicationRepository
) : ViewModel() {

    private val _ownerRequests = MutableStateFlow<List<RequestWithApplications>>(emptyList())
    val ownerRequests = _ownerRequests.asStateFlow()

    private val _caregiverApplications = MutableStateFlow<List<ServiceApplicationEntity>>(emptyList())
    val caregiverApplications = _caregiverApplications.asStateFlow()

    fun loadOwnerData(ownerId: Int) {
        viewModelScope.launch {
            _ownerRequests.value = requestRepo.getWithApplications(ownerId)
        }
    }

    fun loadCaregiverData(caregiverId: Int) {
        viewModelScope.launch {
            _caregiverApplications.value = applicationRepo.getByCaregiver(caregiverId)
        }
    }

    fun createRequest(request: ServiceRequestEntity) {
        viewModelScope.launch {
            requestRepo.insert(request)
        }
    }

    fun applyToRequest(serviceRequestId: Int, caregiverId: Int) {
        viewModelScope.launch {
            applicationRepo.insert(
                ServiceApplicationEntity(
                    serviceRequestId = serviceRequestId,
                    caregiverId = caregiverId
                )
            )
        }
    }

    fun acceptApplication(applicationId: Int) {
        viewModelScope.launch {
            applicationRepo.updateStatus(applicationId, ApplicationStatus.ACCEPTED)
        }
    }

    fun rejectApplication(applicationId: Int) {
        viewModelScope.launch {
            applicationRepo.updateStatus(applicationId, ApplicationStatus.REJECTED)
        }
    }
}