package com.proyectopoo.petcareapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectopoo.petcareapp.data.local.dao.CaregiverDao
import com.proyectopoo.petcareapp.data.local.dao.OwnerDao
import com.proyectopoo.petcareapp.data.local.dao.PetDao
import com.proyectopoo.petcareapp.data.local.dao.ServiceTypeDao
import com.proyectopoo.petcareapp.data.local.dao.UserDao
import com.proyectopoo.petcareapp.data.local.entity.ApplicationStatus
import com.proyectopoo.petcareapp.data.local.entity.CaregiverEntity
import com.proyectopoo.petcareapp.data.local.entity.OwnerEntity
import com.proyectopoo.petcareapp.data.local.entity.PetEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceApplicationEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceTypeEntity
import com.proyectopoo.petcareapp.data.local.entity.UserEntity
import com.proyectopoo.petcareapp.data.local.entity.UserRoleType
import com.proyectopoo.petcareapp.data.local.relation.ServiceApplicationDetails
import com.proyectopoo.petcareapp.data.local.relation.ServiceRequestDetails
import com.proyectopoo.petcareapp.data.local.relation.RequestWithApplications
import com.proyectopoo.petcareapp.data.repository.ServiceApplicationRepository
import com.proyectopoo.petcareapp.data.repository.ServiceRequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServiceRequestViewModel(
    private val requestRepo: ServiceRequestRepository,
    private val applicationRepo: ServiceApplicationRepository,
    private val userDao: UserDao,
    private val ownerDao: OwnerDao,
    private val caregiverDao: CaregiverDao,
    private val petDao: PetDao,
    private val serviceTypeDao: ServiceTypeDao
) : ViewModel() {

    private val _ownerRequests = MutableStateFlow<List<RequestWithApplications>>(emptyList())
    val ownerRequests = _ownerRequests.asStateFlow()

    private val _caregiverApplications = MutableStateFlow<List<ServiceApplicationEntity>>(emptyList())
    val caregiverApplications = _caregiverApplications.asStateFlow()

    private val _availableRequests = MutableStateFlow<List<ServiceRequestDetails>>(emptyList())
    val availableRequests = _availableRequests.asStateFlow()

    private val _recentOwnerRequests = MutableStateFlow<List<ServiceRequestDetails>>(emptyList())
    val recentOwnerRequests = _recentOwnerRequests.asStateFlow()

    private val _ownerApplicationDetails = MutableStateFlow<List<ServiceApplicationDetails>>(emptyList())
    val ownerApplicationDetails = _ownerApplicationDetails.asStateFlow()

    private val _caregiverApplicationDetails = MutableStateFlow<List<ServiceApplicationDetails>>(emptyList())
    val caregiverApplicationDetails = _caregiverApplicationDetails.asStateFlow()

    fun loadOwnerData(ownerId: Int) {
        viewModelScope.launch {
            _ownerRequests.value = requestRepo.getWithApplications(ownerId)
            _recentOwnerRequests.value = requestRepo.getRecentDetailsByOwner(ownerId)
            _ownerApplicationDetails.value = applicationRepo.getDetailsByOwner(ownerId)
        }
    }

    fun loadCaregiverData(caregiverId: Int) {
        viewModelScope.launch {
            _caregiverApplications.value = applicationRepo.getByCaregiver(caregiverId)
            _caregiverApplicationDetails.value = applicationRepo.getDetailsByCaregiver(caregiverId)
        }
    }

    fun loadAvailableRequests() {
        viewModelScope.launch {
            _availableRequests.value = requestRepo.getAvailableDetails()
        }
    }

    fun createRequest(request: ServiceRequestEntity) {
        viewModelScope.launch {
            requestRepo.insert(request)
            _recentOwnerRequests.value = requestRepo.getRecentDetailsByOwner(request.ownerId)
            _availableRequests.value = requestRepo.getAvailableDetails()
        }
    }

    fun createRequestFromForm(
        ownerId: Int,
        petId: Int,
        petName: String,
        serviceTypeName: String,
        description: String,
        location: String,
        price: String,
        requestedDate: String
    ) {
        viewModelScope.launch {
            ensureOwner(ownerId)

            val cleanServiceTypeName = serviceTypeName.trim().ifBlank { "Servicio" }
            val serviceTypeId = serviceTypeIdFor(cleanServiceTypeName)

            if (serviceTypeDao.getServiceTypeById(serviceTypeId) == null) {
                serviceTypeDao.insertServiceType(
                    ServiceTypeEntity(
                        serviceTypeId = serviceTypeId,
                        name = cleanServiceTypeName,
                        description = null
                    )
                )
            }

            if (petDao.getPetById(petId) == null) {
                petDao.insertPet(
                    PetEntity(
                        petId = petId,
                        ownerId = ownerId,
                        name = petName.trim().ifBlank { "Mascota" },
                        species = "Dog",
                        breed = null,
                        size = null
                    )
                )
            }

            val requestId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
            requestRepo.insert(
                ServiceRequestEntity(
                    serviceRequestId = requestId,
                    ownerId = ownerId,
                    petId = petId,
                    serviceTypeId = serviceTypeId,
                    title = cleanServiceTypeName,
                    description = buildString {
                        append(description.trim())
                        if (location.isNotBlank()) append("\nUbicación: ${location.trim()}")
                        if (price.isNotBlank()) append("\nPrecio: C$${price.trim()}")
                    },
                    requestedDate = requestedDate
                )
            )

            _recentOwnerRequests.value = requestRepo.getRecentDetailsByOwner(ownerId)
            _availableRequests.value = requestRepo.getAvailableDetails()
        }
    }

    fun applyToRequest(serviceRequestId: Int, caregiverId: Int) {
        viewModelScope.launch {
            ensureCaregiver(caregiverId)
            applicationRepo.insert(
                ServiceApplicationEntity(
                    serviceRequestId = serviceRequestId,
                    caregiverId = caregiverId
                )
            )
            _availableRequests.value = requestRepo.getAvailableDetails()
            _caregiverApplicationDetails.value = applicationRepo.getDetailsByCaregiver(caregiverId)
        }
    }

    fun acceptApplication(applicationId: Int, ownerId: Int? = null, caregiverId: Int? = null) {
        viewModelScope.launch {
            applicationRepo.updateStatus(applicationId, ApplicationStatus.ACCEPTED)
            ownerId?.let {
                _ownerApplicationDetails.value = applicationRepo.getDetailsByOwner(it)
            }
            caregiverId?.let {
                _caregiverApplicationDetails.value = applicationRepo.getDetailsByCaregiver(it)
            }
        }
    }

    fun rejectApplication(applicationId: Int, ownerId: Int? = null, caregiverId: Int? = null) {
        viewModelScope.launch {
            applicationRepo.updateStatus(applicationId, ApplicationStatus.REJECTED)
            ownerId?.let {
                _ownerApplicationDetails.value = applicationRepo.getDetailsByOwner(it)
            }
            caregiverId?.let {
                _caregiverApplicationDetails.value = applicationRepo.getDetailsByCaregiver(it)
            }
        }
    }

    private suspend fun ensureOwner(ownerId: Int) {
        if (ownerDao.getOwnerById(ownerId) != null) return

        if (userDao.getUserById(ownerId) == null) {
            userDao.insertUser(
                UserEntity(
                    userId = ownerId,
                    fullName = "Dueño",
                    email = "dueno@petcare.local",
                    phone = null,
                    password = null,
                    role = UserRoleType.OWNER
                )
            )
        }

        ownerDao.insertOwner(
            OwnerEntity(
                ownerId = ownerId,
                userId = ownerId,
                address = null
            )
        )
    }

    private suspend fun ensureCaregiver(caregiverId: Int) {
        if (caregiverDao.getCaregiverById(caregiverId) != null) return

        val userId = caregiverId + 10_000
        if (userDao.getUserById(userId) == null) {
            userDao.insertUser(
                UserEntity(
                    userId = userId,
                    fullName = "Cuidador",
                    email = "cuidador@petcare.local",
                    phone = null,
                    password = null,
                    role = UserRoleType.CAREGIVER
                )
            )
        }

        caregiverDao.insertCaregiver(
            CaregiverEntity(
                caregiverId = caregiverId,
                userId = userId
            )
        )
    }

    private fun serviceTypeIdFor(serviceTypeName: String): Int {
        return when (serviceTypeName.lowercase()) {
            "alojamiento" -> 1
            "guardería", "guarderia" -> 2
            "paseo" -> 3
            "taxi" -> 4
            "peluquería", "peluqueria" -> 5
            "visitante" -> 6
            else -> (serviceTypeName.hashCode() and Int.MAX_VALUE)
        }
    }
}
