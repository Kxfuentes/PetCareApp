package com.proyectopoo.petcareapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectopoo.petcareapp.data.local.dao.ServiceApplicationDao
import com.proyectopoo.petcareapp.data.local.dao.ServiceRequestDao
import com.proyectopoo.petcareapp.data.local.entity.ApplicationStatus
import com.proyectopoo.petcareapp.data.local.relation.RequestWithApplications
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ApplicationViewModel(
    private val requestDao: ServiceRequestDao,
    private val applicationDao: ServiceApplicationDao
) : ViewModel() {

    private val _ownerRequestsWithApps = MutableStateFlow<List<RequestWithApplications>>(emptyList())
    val ownerRequestsWithApps = _ownerRequestsWithApps.asStateFlow()

    fun loadOwnerRequests(ownerId: Int) {
        viewModelScope.launch {
            _ownerRequestsWithApps.value = requestDao.getRequestsWithApplications(ownerId)
        }
    }

    fun acceptApplication(applicationId: Int) {
        viewModelScope.launch {
            applicationDao.updateStatus(applicationId, ApplicationStatus.ACCEPTED)
            // Recargar para reflejar cambios
            _ownerRequestsWithApps.value = requestDao.getRequestsWithApplications(
                _ownerRequestsWithApps.value.firstOrNull()?.request?.ownerId ?: return@launch
            )
        }
    }

    fun rejectApplication(applicationId: Int) {
        viewModelScope.launch {
            applicationDao.updateStatus(applicationId, ApplicationStatus.REJECTED)
            // recargar igual
            _ownerRequestsWithApps.value = requestDao.getRequestsWithApplications(
                _ownerRequestsWithApps.value.firstOrNull()?.request?.ownerId ?: return@launch
            )
        }
    }
}