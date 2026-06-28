package com.proyectopoo.petcareapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectopoo.petcareapp.data.local.dao.CaregiverDao
import com.proyectopoo.petcareapp.data.local.dao.OfferedServiceDao
import com.proyectopoo.petcareapp.data.local.dao.OwnerDao
import com.proyectopoo.petcareapp.data.local.dao.PetDao
import com.proyectopoo.petcareapp.data.local.dao.RatingDao
import com.proyectopoo.petcareapp.data.local.dao.ServiceBookingDao
import com.proyectopoo.petcareapp.data.local.dao.ServiceTypeDao
import com.proyectopoo.petcareapp.data.local.dao.UserDao
import com.proyectopoo.petcareapp.data.local.entity.ApplicationInitiator
import com.proyectopoo.petcareapp.data.local.entity.ApplicationStatus
import com.proyectopoo.petcareapp.data.local.entity.BookingStatus
import com.proyectopoo.petcareapp.data.local.entity.CaregiverEntity
import com.proyectopoo.petcareapp.data.local.entity.OwnerEntity
import com.proyectopoo.petcareapp.data.local.entity.PetEntity
import com.proyectopoo.petcareapp.data.local.entity.RatingEntity
import com.proyectopoo.petcareapp.data.local.entity.RequestSource
import com.proyectopoo.petcareapp.data.local.entity.ServiceApplicationEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestStatus
import com.proyectopoo.petcareapp.data.local.entity.ServiceTypeEntity
import com.proyectopoo.petcareapp.data.local.entity.UserEntity
import com.proyectopoo.petcareapp.data.local.entity.UserRoleType
import com.proyectopoo.petcareapp.data.local.relation.OfferedServiceDetails
import com.proyectopoo.petcareapp.data.local.relation.ServiceApplicationDetails
import com.proyectopoo.petcareapp.data.local.relation.ServiceRequestDetails
import com.proyectopoo.petcareapp.data.local.relation.RequestWithApplications
import com.proyectopoo.petcareapp.data.local.entity.ServiceBookingEntity
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
    private val serviceTypeDao: ServiceTypeDao,
    private val ratingDao: RatingDao,
    private val offeredServiceDao: OfferedServiceDao,
    private val bookingDao: ServiceBookingDao
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

    private val _caregiverOffers = MutableStateFlow<List<com.proyectopoo.petcareapp.data.local.entity.OfferedServiceEntity>>(emptyList())
    val caregiverOffers = _caregiverOffers.asStateFlow()

    private val _ownerBookings = MutableStateFlow<List<ServiceBookingEntity>>(emptyList())
    val ownerBookings = _ownerBookings.asStateFlow()

    private val _caregiverBookings = MutableStateFlow<List<ServiceBookingEntity>>(emptyList())
    val caregiverBookings = _caregiverBookings.asStateFlow()

    fun loadOwnerData(ownerId: Int) {
        viewModelScope.launch {
            _ownerRequests.value = requestRepo.getWithApplications(ownerId)
            _recentOwnerRequests.value = requestRepo.getRecentDetailsByOwner(ownerId)
            _ownerApplicationDetails.value = applicationRepo.getIncomingCaregiverOffersForOwner(ownerId)
            _ownerBookings.value = bookingDao.getBookingsByOwner(ownerId)
        }
    }

    fun loadCaregiverData(caregiverId: Int) {
        viewModelScope.launch {
            _caregiverApplications.value = applicationRepo.getByCaregiver(caregiverId)
            _caregiverApplicationDetails.value = applicationRepo.getIncomingOwnerRequestsForCaregiver(caregiverId)
            _caregiverOffers.value = offeredServiceDao.getServicesByCaregiver(caregiverId)
            _caregiverBookings.value = bookingDao.getBookingsByCaregiver(caregiverId)
        }
    }

    fun loadAvailableRequests() {
        viewModelScope.launch {
            _availableRequests.value = requestRepo.getAvailableDetails()
        }
    }

    fun getApplicationsForOffer(caregiverId: Int, offeredServiceId: Int): List<ServiceApplicationDetails> {
        var result = emptyList<ServiceApplicationDetails>()
        viewModelScope.launch {
            result = applicationRepo.getApplicationsForOffer(caregiverId, offeredServiceId)
        }
        return result
    }

    suspend fun fetchApplicationsForOffer(caregiverId: Int, offeredServiceId: Int) =
        applicationRepo.getApplicationsForOffer(caregiverId, offeredServiceId)

    fun createRequestFromForm(
        ownerId: Int,
        petIds: List<Int>,
        serviceTypeName: String,
        description: String,
        location: String,
        price: String,
        requestedDate: String,
        startTime: String,
        endTime: String
    ) {
        viewModelScope.launch {
            val numericPrice = price.toDoubleOrNull()
            if (numericPrice == null || numericPrice !in 20.0..6000.0) return@launch
            if (petIds.isEmpty()) return@launch

            ensureOwner(ownerId)

            val cleanServiceTypeName = serviceTypeName.trim().ifBlank { "Servicio" }
            val serviceTypeId = serviceTypeIdFor(cleanServiceTypeName)
            ensureServiceType(serviceTypeId, cleanServiceTypeName)

            val primaryPetId = petIds.first()
            val requestId = generateRequestId()

            requestRepo.insert(
                ServiceRequestEntity(
                    serviceRequestId = requestId,
                    ownerId = ownerId,
                    petId = primaryPetId,
                    serviceTypeId = serviceTypeId,
                    title = cleanServiceTypeName,
                    description = buildString {
                        append(description.trim())
                        if (location.isNotBlank()) append("\nUbicación: ${location.trim()}")
                        if (price.isNotBlank()) append("\nPrecio: C$${price.trim()}")
                    },
                    requestedDate = requestedDate,
                    startTime = startTime.ifBlank { null },
                    endTime = endTime.ifBlank { null },
                    sourceType = RequestSource.OPEN
                ),
                petIds = petIds
            )

            refreshOwnerData(ownerId)
            _availableRequests.value = requestRepo.getAvailableDetails()
        }
    }

    fun requestServiceFromOffer(
        ownerId: Int,
        caregiverId: Int,
        offeredServiceId: Int,
        petIds: List<Int>,
        requestedDate: String,
        startTime: String,
        notes: String,
        suggestedPrice: String?
    ) {
        viewModelScope.launch {
            if (petIds.isEmpty()) return@launch

            ensureOwner(ownerId)
            ensureCaregiver(caregiverId)

            val offer = offeredServiceDao.getServiceById(offeredServiceId) ?: return@launch
            val serviceType = serviceTypeDao.getServiceTypeById(offer.serviceTypeId)
            val serviceTypeName = serviceType?.name ?: offer.title

            val requestId = generateRequestId()
            val description = buildString {
                if (notes.isNotBlank()) append(notes.trim())
                suggestedPrice?.toDoubleOrNull()?.let { append("\nPrecio sugerido: C$${"%.2f".format(it)}") }
            }.ifBlank { null }

            requestRepo.insert(
                ServiceRequestEntity(
                    serviceRequestId = requestId,
                    ownerId = ownerId,
                    petId = petIds.first(),
                    serviceTypeId = offer.serviceTypeId,
                    title = serviceTypeName,
                    description = description,
                    requestedDate = requestedDate,
                    startTime = startTime.ifBlank { null },
                    sourceType = RequestSource.OFFER,
                    offeredServiceId = offeredServiceId
                ),
                petIds = petIds
            )

            applicationRepo.insert(
                ServiceApplicationEntity(
                    serviceRequestId = requestId,
                    caregiverId = caregiverId,
                    offeredServiceId = offeredServiceId,
                    initiatedBy = ApplicationInitiator.OWNER,
                    status = ApplicationStatus.PENDING
                )
            )

            refreshOwnerData(ownerId)
            loadCaregiverData(caregiverId)
        }
    }

    fun applyToRequest(serviceRequestId: Int, caregiverId: Int) {
        viewModelScope.launch {
            ensureCaregiver(caregiverId)
            applicationRepo.insert(
                ServiceApplicationEntity(
                    serviceRequestId = serviceRequestId,
                    caregiverId = caregiverId,
                    initiatedBy = ApplicationInitiator.CAREGIVER
                )
            )
            _availableRequests.value = requestRepo.getAvailableDetails()
            _caregiverApplicationDetails.value = applicationRepo.getIncomingOwnerRequestsForCaregiver(caregiverId)
        }
    }

    fun acceptApplication(
        applicationId: Int,
        ownerId: Int? = null,
        caregiverId: Int? = null,
        modifiedDate: String? = null,
        modifiedStartTime: String? = null,
        modifiedEndTime: String? = null
    ) {
        viewModelScope.launch {
            val application = applicationRepo.getApplicationById(applicationId) ?: return@launch

            if (modifiedDate != null || modifiedStartTime != null || modifiedEndTime != null) {
                applicationRepo.updateRequestSchedule(
                    requestId = application.serviceRequestId,
                    date = modifiedDate,
                    startTime = modifiedStartTime,
                    endTime = modifiedEndTime
                )
            }

            applicationRepo.acceptAndCreateBooking(applicationId)
            _availableRequests.value = requestRepo.getAvailableDetails()
            ownerId?.let { refreshOwnerData(it) }
            caregiverId?.let { loadCaregiverData(it) }
        }
    }

    fun rejectApplication(applicationId: Int, ownerId: Int? = null, caregiverId: Int? = null) {
        viewModelScope.launch {
            applicationRepo.updateStatus(applicationId, ApplicationStatus.REJECTED)
            ownerId?.let {
                _ownerApplicationDetails.value = applicationRepo.getIncomingCaregiverOffersForOwner(it)
            }
            caregiverId?.let {
                _caregiverApplicationDetails.value = applicationRepo.getIncomingOwnerRequestsForCaregiver(it)
            }
        }
    }

    fun completeAndRateService(
        applicationId: Int,
        serviceRequestId: Int,
        caregiverId: Int,
        ownerId: Int,
        ratedByRole: UserRoleType,
        score: Double,
        comment: String,
        reloadOwnerId: Int? = null,
        reloadCaregiverId: Int? = null
    ) {
        viewModelScope.launch {
            applicationRepo.completeAndCloseRequest(applicationId)
            val existingRating = ratingDao.getRatingForServiceByRole(serviceRequestId, ratedByRole)
            if (existingRating == null) {
                ratingDao.insertRating(
                    RatingEntity(
                        serviceRequestId = serviceRequestId,
                        caregiverId = caregiverId,
                        ownerId = ownerId,
                        ratedByRole = ratedByRole,
                        score = score.coerceIn(1.0, 5.0),
                        comment = comment.ifBlank { null }
                    )
                )
            }

            reloadOwnerId?.let { refreshOwnerData(it) }
            reloadCaregiverId?.let { loadCaregiverData(it) }
            _availableRequests.value = requestRepo.getAvailableDetails()
        }
    }

    private suspend fun refreshOwnerData(ownerId: Int) {
        _recentOwnerRequests.value = requestRepo.getRecentDetailsByOwner(ownerId)
        _ownerApplicationDetails.value = applicationRepo.getIncomingCaregiverOffersForOwner(ownerId)
        _ownerBookings.value = bookingDao.getBookingsByOwner(ownerId)
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

        if (userDao.getUserById(caregiverId) == null) {
            userDao.insertUser(
                UserEntity(
                    userId = caregiverId,
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
                userId = caregiverId
            )
        )
    }

    private suspend fun ensureServiceType(serviceTypeId: Int, name: String) {
        if (serviceTypeDao.getServiceTypeById(serviceTypeId) == null) {
            serviceTypeDao.insertServiceType(
                ServiceTypeEntity(
                    serviceTypeId = serviceTypeId,
                    name = name,
                    description = null
                )
            )
        }
    }

    private fun generateRequestId(): Int {
        return (System.currentTimeMillis() % Int.MAX_VALUE).toInt().let {
            if (it <= 0) 1 else it
        }
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

    fun clear() {
        _ownerRequests.value = emptyList()
        _caregiverApplications.value = emptyList()
        _availableRequests.value = emptyList()
        _recentOwnerRequests.value = emptyList()
        _ownerApplicationDetails.value = emptyList()
        _caregiverApplicationDetails.value = emptyList()
        _caregiverOffers.value = emptyList()
        _ownerBookings.value = emptyList()
        _caregiverBookings.value = emptyList()
    }
}
