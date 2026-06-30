package com.proyectopoo.petcareapp.viewmodel

/*
 * Comentario de modulo PetCare:
 * Estado de pantalla. Expone acciones y datos listos para que Compose los pueda mostrar.
 */

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

    private var currentOwnerId: Int = 0

    fun loadOwnerRequests(ownerId: Int) {
        currentOwnerId = ownerId
        viewModelScope.launch {
            _ownerRequestsWithApps.value = requestDao.getRequestsWithApplications(ownerId)
        }
    }

    fun acceptApplication(applicationId: Int) {
        viewModelScope.launch {
            applicationDao.updateStatus(applicationId, ApplicationStatus.ACCEPTED)
            if (currentOwnerId > 0) {
                _ownerRequestsWithApps.value = requestDao.getRequestsWithApplications(currentOwnerId)
            }
        }
    }

    fun rejectApplication(applicationId: Int) {
        viewModelScope.launch {
            applicationDao.updateStatus(applicationId, ApplicationStatus.REJECTED)
            if (currentOwnerId > 0) {
                _ownerRequestsWithApps.value = requestDao.getRequestsWithApplications(currentOwnerId)
            }
        }
    }
}