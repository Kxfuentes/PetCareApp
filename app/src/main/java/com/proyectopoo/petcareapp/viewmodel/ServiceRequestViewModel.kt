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
import com.proyectopoo.petcareapp.data.local.entity.NotificationType
import com.proyectopoo.petcareapp.data.local.entity.UserRoleType
import com.proyectopoo.petcareapp.data.local.relation.OfferedServiceDetails
import com.proyectopoo.petcareapp.data.local.relation.ServiceApplicationDetails
import com.proyectopoo.petcareapp.data.local.relation.ServiceRequestDetails
import com.proyectopoo.petcareapp.data.local.relation.RequestWithApplications
import com.proyectopoo.petcareapp.data.local.entity.ServiceBookingEntity
import com.proyectopoo.petcareapp.data.network.ApiService
import com.proyectopoo.petcareapp.data.network.ServiceApplicationDto
import com.proyectopoo.petcareapp.data.network.RatingDto
import com.proyectopoo.petcareapp.data.network.ServiceRequestDto
import com.proyectopoo.petcareapp.data.network.StatusUpdateRequest
import com.proyectopoo.petcareapp.data.repository.ServiceApplicationRepository
import com.proyectopoo.petcareapp.data.repository.ServiceRequestRepository
import com.proyectopoo.petcareapp.notifications.AppNotifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

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
    private val bookingDao: ServiceBookingDao,
    private val notifier: AppNotifier,
    private val apiService: ApiService? = null
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

    private val _ownerScheduledServices = MutableStateFlow<List<ServiceApplicationDetails>>(emptyList())
    val ownerScheduledServices = _ownerScheduledServices.asStateFlow()

    private val _caregiverApplicationDetails = MutableStateFlow<List<ServiceApplicationDetails>>(emptyList())
    val caregiverApplicationDetails = _caregiverApplicationDetails.asStateFlow()

    private val _caregiverScheduledServices = MutableStateFlow<List<ServiceApplicationDetails>>(emptyList())
    val caregiverScheduledServices = _caregiverScheduledServices.asStateFlow()

    private val _caregiverOffers = MutableStateFlow<List<com.proyectopoo.petcareapp.data.local.entity.OfferedServiceEntity>>(emptyList())
    val caregiverOffers = _caregiverOffers.asStateFlow()

    private val _ownerBookings = MutableStateFlow<List<ServiceBookingEntity>>(emptyList())
    val ownerBookings = _ownerBookings.asStateFlow()

    private val _caregiverBookings = MutableStateFlow<List<ServiceBookingEntity>>(emptyList())
    val caregiverBookings = _caregiverBookings.asStateFlow()

    // Mensajes puntuales para mostrar al usuario (errores de validación, confirmaciones).
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun loadOwnerData(ownerId: Int) {
        if (ownerId <= 0) return
        viewModelScope.launch {
            syncOwnerRequests(ownerId)
            syncOwnerApplications(ownerId)
            _ownerRequests.value = requestRepo.getWithApplications(ownerId)
            _recentOwnerRequests.value = requestRepo.getRecentDetailsByOwner(ownerId)
            _ownerApplicationDetails.value = applicationRepo.getIncomingCaregiverOffersForOwner(ownerId)
            _ownerScheduledServices.value = applicationRepo.getAcceptedApplicationsForOwner(ownerId)
            _ownerBookings.value = bookingDao.getBookingsByOwner(ownerId)
            syncCaregiverRatingsFromRemote()
        }
    }

    fun loadCaregiverData(caregiverId: Int) {
        if (caregiverId <= 0) return
        viewModelScope.launch {
            syncAvailableRequests()
            syncCaregiverApplications(caregiverId)
            _caregiverApplications.value = applicationRepo.getByCaregiver(caregiverId)
            _caregiverApplicationDetails.value = applicationRepo.getIncomingOwnerRequestsForCaregiver(caregiverId)
            _caregiverScheduledServices.value = applicationRepo.getAcceptedApplicationsForCaregiver(caregiverId)
            _caregiverOffers.value = offeredServiceDao.getServicesByCaregiver(caregiverId)
            _caregiverBookings.value = bookingDao.getBookingsByCaregiver(caregiverId)
        }
    }

    fun loadAvailableRequests() {
        viewModelScope.launch {
            syncAvailableRequests()
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
        endTime: String,
        latitude: Double? = null,
        longitude: Double? = null,
        onSuccess: (() -> Unit)? = null
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

            val request = ServiceRequestEntity(
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
                sourceType = RequestSource.OPEN,
                latitude = latitude,
                longitude = longitude
            )

            val persistedRequest = createRemoteRequest(request, petIds) ?: return@launch
            requestRepo.insert(persistedRequest, petIds = petIds)

            refreshOwnerData(ownerId)
            _availableRequests.value = requestRepo.getAvailableDetails()
            _userMessage.value = "Solicitud publicada para cuidadores."
            onSuccess?.invoke()
        }
    }

    fun requestServiceFromOffer(
        ownerId: Int,
        caregiverId: Int,
        offeredServiceId: Int,
        petIds: List<Int>,
        requestedDate: String,
        startTime: String,
        endTime: String? = null,
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

            val request = ServiceRequestEntity(
                serviceRequestId = requestId,
                ownerId = ownerId,
                petId = petIds.first(),
                serviceTypeId = offer.serviceTypeId,
                title = serviceTypeName,
                description = description,
                requestedDate = requestedDate,
                startTime = startTime.ifBlank { null },
                endTime = endTime?.ifBlank { null },
                sourceType = RequestSource.OFFER,
                offeredServiceId = offeredServiceId
            )

            val persistedRequest = createRemoteRequest(request, petIds) ?: return@launch
            requestRepo.insert(persistedRequest, petIds = petIds)

            val application = ServiceApplicationEntity(
                serviceRequestId = persistedRequest.serviceRequestId,
                caregiverId = caregiverId,
                offeredServiceId = offeredServiceId,
                initiatedBy = ApplicationInitiator.OWNER,
                status = ApplicationStatus.PENDING
            )
            val remoteApplication = createRemoteApplication(application)
            if (remoteApplication == null && apiService != null) return@launch
            applicationRepo.insert(remoteApplication ?: application)

            refreshOwnerData(ownerId)
            loadCaregiverData(caregiverId)

            notifier.push(
                recipientUserId = caregiverId,
                title = "Nueva solicitud de un dueño",
                message = "Un dueño solicitó tu servicio de $serviceTypeName.",
                type = NotificationType.SERVICE_REQUEST
            )
            _userMessage.value = "Solicitud enviada al cuidador."
        }
    }

    fun applyToRequest(serviceRequestId: Int, caregiverId: Int) {
        viewModelScope.launch {
            ensureCaregiver(caregiverId)
            val application = ServiceApplicationEntity(
                serviceRequestId = serviceRequestId,
                caregiverId = caregiverId,
                initiatedBy = ApplicationInitiator.CAREGIVER
            )
            val remoteApplication = createRemoteApplication(application)
            if (remoteApplication == null && apiService != null) return@launch
            applicationRepo.insert(remoteApplication ?: application)
            _caregiverApplicationDetails.value = applicationRepo.getIncomingOwnerRequestsForCaregiver(caregiverId)

            val request = requestRepo.getRequestById(serviceRequestId)
            if (request != null) {
                notifier.push(
                    recipientUserId = request.ownerId,
                    title = "Nueva postulación a tu solicitud",
                    message = "Un cuidador se postuló a tu solicitud de \"${request.title}\".",
                    type = NotificationType.SERVICE_REQUEST
                )
            }
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
            val request = requestRepo.getRequestById(application.serviceRequestId)

            if (application.initiatedBy == ApplicationInitiator.CAREGIVER && ownerId == null) {
                _userMessage.value = "Solo el dueño puede aceptar esta postulación."
                return@launch
            }
            if (application.initiatedBy == ApplicationInitiator.OWNER && caregiverId == null) {
                _userMessage.value = "Solo el cuidador puede aceptar esta solicitud directa."
                return@launch
            }

            if (modifiedDate != null || modifiedStartTime != null || modifiedEndTime != null) {
                applicationRepo.updateRequestSchedule(
                    requestId = application.serviceRequestId,
                    date = modifiedDate,
                    startTime = modifiedStartTime,
                    endTime = modifiedEndTime
                )
            }

            val updatedRequest = requestRepo.getRequestById(application.serviceRequestId)
            if (updatedRequest != null && caregiverHasConflict(application.caregiverId, updatedRequest)) {
                _userMessage.value =
                    "Este cuidador ya tiene un servicio aceptado en ese horario. No puede aceptar dos servicios a la vez."
                return@launch
            }

            val remoteApplication = updateRemoteApplicationStatus(applicationId, ApplicationStatus.ACCEPTED)
            if (remoteApplication == null && apiService != null) return@launch

            applicationRepo.acceptAndCreateBooking(remoteApplication?.applicationId ?: applicationId)
            _availableRequests.value = requestRepo.getAvailableDetails()
            ownerId?.let { loadOwnerData(it) }
            caregiverId?.let { loadCaregiverData(it) }

            val recipientUserId = if (application.initiatedBy == ApplicationInitiator.CAREGIVER) {
                application.caregiverId
            } else {
                request?.ownerId ?: application.caregiverId
            }
            val message = if (application.initiatedBy == ApplicationInitiator.CAREGIVER) {
                "Tu postulación fue aceptada. ¡Prepárate para el servicio!"
            } else {
                "Tu solicitud directa fue aceptada por el cuidador."
            }

            notifier.push(
                recipientUserId = recipientUserId,
                title = "Solicitud aceptada",
                message = message,
                type = NotificationType.REQUEST_ACCEPTED
            )
            _userMessage.value = "Servicio confirmado."
        }
    }

    fun rejectApplication(applicationId: Int, ownerId: Int? = null, caregiverId: Int? = null) {
        viewModelScope.launch {
            val application = applicationRepo.getApplicationById(applicationId) ?: return@launch
            val request = requestRepo.getRequestById(application.serviceRequestId)

            if (application.initiatedBy == ApplicationInitiator.CAREGIVER && ownerId == null) return@launch
            if (application.initiatedBy == ApplicationInitiator.OWNER && caregiverId == null) return@launch

            if (updateRemoteApplicationStatus(applicationId, ApplicationStatus.REJECTED) == null && apiService != null) {
                return@launch
            }
            applicationRepo.updateStatus(applicationId, ApplicationStatus.REJECTED)
            ownerId?.let { loadOwnerData(it) }
            caregiverId?.let { loadCaregiverData(it) }
            loadAvailableRequests()

            val recipientUserId = if (application.initiatedBy == ApplicationInitiator.CAREGIVER) {
                application.caregiverId
            } else {
                request?.ownerId ?: application.caregiverId
            }

            notifier.push(
                recipientUserId = recipientUserId,
                title = "Solicitud rechazada",
                message = "Una solicitud fue rechazada.",
                type = NotificationType.REQUEST_REJECTED
            )
            _userMessage.value = "Solicitud rechazada."
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
            if (updateRemoteApplicationStatus(applicationId, ApplicationStatus.COMPLETED) == null && apiService != null) {
                return@launch
            }
            applicationRepo.completeAndCloseRequest(applicationId)
            val existingRating = ratingDao.getRatingForServiceByRole(serviceRequestId, ratedByRole)
            val rating = RatingEntity(
                serviceRequestId = serviceRequestId,
                caregiverId = caregiverId,
                ownerId = ownerId,
                ratedByRole = ratedByRole,
                score = score.coerceIn(1.0, 5.0),
                comment = comment.ifBlank { null }
            )
            if (existingRating == null) {
                ratingDao.insertRating(rating)
                val ratingSaved = createRemoteRating(rating)
                if (ratedByRole == UserRoleType.OWNER) {
                    updateCaregiverRatingFromRemote(caregiverId)
                }
                if (!ratingSaved) {
                    _userMessage.value = "Calificación guardada localmente; revisa la conexión con el servidor."
                }
            }

            reloadOwnerId?.let { loadOwnerData(it) }
            reloadCaregiverId?.let { loadCaregiverData(it) }
            _availableRequests.value = requestRepo.getAvailableDetails()
            _userMessage.value = "Calificación registrada."
        }
    }

    /**
     * Cancela un servicio aceptado. Solo se permite hasta 2 horas antes de su inicio.
     */
    fun cancelService(
        applicationId: Int,
        reloadOwnerId: Int? = null,
        reloadCaregiverId: Int? = null
    ) {
        viewModelScope.launch {
            val application = applicationRepo.getApplicationById(applicationId) ?: return@launch
            val request = requestRepo.getRequestById(application.serviceRequestId)
            val startMillis = request?.let { parseDateTime(it.requestedDate, it.startTime) }

            if (startMillis != null && System.currentTimeMillis() > startMillis - CANCELLATION_WINDOW_MS) {
                _userMessage.value =
                    "Solo puedes cancelar un servicio hasta 3 horas antes de que empiece."
                return@launch
            }

            if (updateRemoteApplicationStatus(applicationId, ApplicationStatus.CANCELLED) == null && apiService != null) {
                return@launch
            }
            applicationRepo.cancelService(applicationId)
            reloadOwnerId?.let { loadOwnerData(it) }
            reloadCaregiverId?.let { loadCaregiverData(it) }
            _availableRequests.value = requestRepo.getAvailableDetails()
            _userMessage.value = "Servicio cancelado."

            val otherPartyId = if (reloadOwnerId != null) application.caregiverId else request?.ownerId
            otherPartyId?.let { recipientId ->
                notifier.push(
                    recipientUserId = recipientId,
                    title = "Servicio cancelado",
                    message = "Un servicio confirmado fue cancelado.",
                    type = NotificationType.REQUEST_CANCELLED
                )
            }
        }
    }

    /** Verifica si el cuidador ya tiene una reserva ACTIVA que se solapa con [candidate]. */
    private suspend fun caregiverHasConflict(
        caregiverId: Int,
        candidate: ServiceRequestEntity
    ): Boolean {
        return bookingDao.getBookingsByCaregiver(caregiverId)
            .filter { it.status == BookingStatus.ACTIVE }
            .filter { it.serviceRequestId != candidate.serviceRequestId }
            .any { booking ->
                requestRepo.getRequestById(booking.serviceRequestId)
                    ?.let { overlaps(it, candidate) } == true
            }
    }

    private fun overlaps(a: ServiceRequestEntity, b: ServiceRequestEntity): Boolean {
        val wa = windowOf(a)
        val wb = windowOf(b)
        if (wa != null && wb != null) {
            return wa.first < wb.second && wb.first < wa.second
        }
        // Sin horas parseables: consideramos conflicto si es el mismo día.
        return !a.requestedDate.isNullOrBlank() && a.requestedDate == b.requestedDate
    }

    /** Devuelve la ventana [inicio, fin] del servicio en milisegundos, o null si no se puede parsear. */
    private fun windowOf(request: ServiceRequestEntity): Pair<Long, Long>? {
        val start = parseDateTime(request.requestedDate, request.startTime) ?: return null
        val end = parseDateTime(request.requestedDate, request.endTime)
            ?: (start + ONE_HOUR_MS)
        return start to (if (end > start) end else start + ONE_HOUR_MS)
    }

    private fun parseDateTime(date: String?, time: String?): Long? {
        if (date.isNullOrBlank()) return null
        return try {
            val hasTime = !time.isNullOrBlank()
            val pattern = if (hasTime) "dd/MM/yyyy HH:mm" else "dd/MM/yyyy"
            val text = if (hasTime) "$date $time" else date
            java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault())
                .apply { isLenient = false }
                .parse(text)
                ?.time
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun refreshOwnerData(ownerId: Int) {
        _recentOwnerRequests.value = requestRepo.getRecentDetailsByOwner(ownerId)
        _ownerApplicationDetails.value = applicationRepo.getIncomingCaregiverOffersForOwner(ownerId)
        _ownerScheduledServices.value = applicationRepo.getAcceptedApplicationsForOwner(ownerId)
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


    private suspend fun syncOwnerRequests(ownerId: Int) {
        val remote = runCatching {
            apiService?.getServiceRequestsByOwner(ownerId)
                ?.takeIf { it.isSuccessful }
                ?.body()
        }.getOrNull() ?: return

        cacheRequests(remote)
    }

    private suspend fun syncAvailableRequests() {
        val remote = runCatching {
            apiService?.getAvailableServiceRequests()
                ?.takeIf { it.isSuccessful }
                ?.body()
        }.getOrNull() ?: return

        cacheRequests(remote)
    }

    private suspend fun syncOwnerApplications(ownerId: Int) {
        val remote = runCatching {
            apiService?.getServiceApplicationsByOwner(ownerId)
                ?.takeIf { it.isSuccessful }
                ?.body()
        }.getOrNull() ?: return

        cacheApplications(remote, notifyUserId = ownerId)
    }

    private suspend fun syncCaregiverApplications(caregiverId: Int) {
        val remote = runCatching {
            apiService?.getServiceApplicationsByCaregiver(caregiverId)
                ?.takeIf { it.isSuccessful }
                ?.body()
        }.getOrNull() ?: return

        cacheApplications(remote, notifyUserId = caregiverId)
    }

    private suspend fun cacheRequests(remote: List<ServiceRequestDto>) {
        remote.forEach { dto -> cacheRequest(dto) }
    }

    private suspend fun cacheRequest(dto: ServiceRequestDto) {
        ensureOwner(dto.ownerId)
        ensureServiceType(dto.serviceTypeId, dto.title)
        ensurePetPlaceholder(dto.ownerId, dto.petId)
        dto.petIds.forEach { petId -> ensurePetPlaceholder(dto.ownerId, petId) }
        requestRepo.insert(dto.toEntity(), dto.petIds.ifEmpty { listOf(dto.petId) })
    }

    private suspend fun cacheApplications(
        remote: List<ServiceApplicationDto>,
        notifyUserId: Int? = null
    ) {
        remote.forEach { dto ->
            syncRequestById(dto.serviceRequestId)
            val request = requestRepo.getRequestById(dto.serviceRequestId) ?: return@forEach
            val isNewApplication = dto.id?.let { applicationRepo.getApplicationById(it) == null } ?: false
            ensureCaregiver(dto.caregiverId)
            applicationRepo.insert(dto.toEntity())
            notifyNewApplicationIfNeeded(dto, request, notifyUserId, isNewApplication)
        }
    }

    private suspend fun syncRequestById(requestId: Int) {
        val remote = runCatching {
            apiService?.getServiceRequestById(requestId)
                ?.takeIf { it.isSuccessful }
                ?.body()
        }.getOrNull() ?: return

        cacheRequest(remote)
    }

    private suspend fun notifyNewApplicationIfNeeded(
        dto: ServiceApplicationDto,
        request: ServiceRequestEntity,
        notifyUserId: Int?,
        isNewApplication: Boolean
    ) {
        if (!isNewApplication || notifyUserId == null || dto.status != ApplicationStatus.PENDING.name) return

        if (dto.initiatedBy == ApplicationInitiator.OWNER.name && dto.caregiverId == notifyUserId) {
            notifier.push(
                recipientUserId = notifyUserId,
                title = "Nueva solicitud de un dueño",
                message = "Un dueño solicitó tu servicio de ${request.title}.",
                type = NotificationType.SERVICE_REQUEST
            )
        }

        if (dto.initiatedBy == ApplicationInitiator.CAREGIVER.name && request.ownerId == notifyUserId) {
            notifier.push(
                recipientUserId = notifyUserId,
                title = "Nueva postulación a tu solicitud",
                message = "Un cuidador se postuló a tu solicitud de \"${request.title}\".",
                type = NotificationType.SERVICE_REQUEST
            )
        }
    }


    private suspend fun updateRemoteApplicationStatus(
        applicationId: Int,
        status: ApplicationStatus
    ): ServiceApplicationEntity? {
        val response = runCatching {
            apiService?.updateServiceApplicationStatus(applicationId, StatusUpdateRequest(status.name))
        }.getOrNull() ?: return null

        if (response.isSuccessful) {
            return response.body()?.toEntity()
        }

        _userMessage.value = parseApiError(response.errorBody()?.string())
            ?: "No se pudo actualizar el estado del servicio."
        return null
    }

    private suspend fun createRemoteRequest(
        request: ServiceRequestEntity,
        petIds: List<Int>
    ): ServiceRequestEntity? {
        val response = runCatching {
            apiService?.createServiceRequest(request.toDto(petIds))
        }.getOrNull() ?: return request

        if (response.isSuccessful) return response.body()?.toEntity() ?: request
        _userMessage.value = parseApiError(response.errorBody()?.string())
            ?: "Error al guardar la solicitud en el servidor."
        return null
    }

    private suspend fun createRemoteApplication(application: ServiceApplicationEntity): ServiceApplicationEntity? {
        val response = runCatching {
            apiService?.createServiceApplication(application.toDto())
        }.getOrNull() ?: return null

        if (response.isSuccessful) {
            return response.body()?.toEntity()
        }

        _userMessage.value = parseApiError(response.errorBody()?.string())
            ?: "Error al registrar la postulación en el servidor."
        return null
    }

    private suspend fun createRemoteRating(rating: RatingEntity): Boolean {
        val response = runCatching {
            apiService?.createRating(rating.toDto())
        }.getOrNull() ?: return false

        return response.isSuccessful
    }

    private suspend fun syncCaregiverRatingsFromRemote() {
        val caregiverIds = offeredServiceDao.getAvailableServices()
            .map { it.caregiverId }
            .distinct()
        caregiverIds.forEach { caregiverId ->
            updateCaregiverRatingFromRemote(caregiverId)
        }
    }

    private suspend fun updateCaregiverRatingFromRemote(caregiverId: Int) {
        val summary = runCatching {
            apiService?.getCaregiverRatingSummary(caregiverId)
                ?.takeIf { it.isSuccessful }
                ?.body()
        }.getOrNull() ?: return

        val caregiver = caregiverDao.getCaregiverById(caregiverId) ?: return
        caregiverDao.updateCaregiver(caregiver.copy(rating = summary.average))
    }

    private fun parseApiError(raw: String?): String? {
        val body = raw?.trim().takeUnless { it.isNullOrBlank() } ?: return null
        return runCatching {
            val json = JSONObject(body)
            json.optString("error")
                .ifBlank { json.optString("message") }
                .ifBlank { json.optString("detail") }
                .ifBlank { body }
        }.getOrDefault(body)
    }

    private suspend fun ensurePetPlaceholder(ownerId: Int, petId: Int) {
        if (petId <= 0 || petDao.getPetById(petId) != null) return
        ensureOwner(ownerId)
        petDao.insertPet(
            PetEntity(
                petId = petId,
                ownerId = ownerId,
                name = "Mascota",
                species = "Dog",
                breed = "Sin raza",
                size = "Mediano"
            )
        )
    }

    fun clear() {
        _ownerRequests.value = emptyList()
        _caregiverApplications.value = emptyList()
        _availableRequests.value = emptyList()
        _recentOwnerRequests.value = emptyList()
        _ownerApplicationDetails.value = emptyList()
        _ownerScheduledServices.value = emptyList()
        _caregiverApplicationDetails.value = emptyList()
        _caregiverScheduledServices.value = emptyList()
        _caregiverOffers.value = emptyList()
        _ownerBookings.value = emptyList()
        _caregiverBookings.value = emptyList()
    }

    companion object {
        private const val ONE_HOUR_MS = 60 * 60 * 1000L
        private const val CANCELLATION_WINDOW_MS = 3L * ONE_HOUR_MS
    }
}


private fun ServiceRequestEntity.toDto(petIds: List<Int> = emptyList()): ServiceRequestDto {
    return ServiceRequestDto(
        id = serviceRequestId,
        ownerId = ownerId,
        petId = petId,
        petIds = petIds.ifEmpty { listOf(petId) },
        serviceTypeId = serviceTypeId,
        title = title,
        description = description,
        requestedDate = requestedDate,
        startTime = startTime,
        endTime = endTime,
        status = status.name,
        offeredServiceId = offeredServiceId,
        sourceType = sourceType.name,
        latitude = latitude,
        longitude = longitude
    )
}

private fun ServiceRequestDto.toEntity(): ServiceRequestEntity {
    return ServiceRequestEntity(
        serviceRequestId = id,
        ownerId = ownerId,
        petId = petId,
        serviceTypeId = serviceTypeId,
        title = title,
        description = description,
        requestedDate = requestedDate,
        startTime = startTime,
        endTime = endTime,
        status = runCatching { ServiceRequestStatus.valueOf(status) }.getOrDefault(ServiceRequestStatus.PENDING),
        offeredServiceId = offeredServiceId,
        sourceType = runCatching { RequestSource.valueOf(sourceType) }.getOrDefault(RequestSource.OPEN),
        latitude = latitude,
        longitude = longitude
    )
}

private fun ServiceApplicationEntity.toDto(): ServiceApplicationDto {
    return ServiceApplicationDto(
        id = applicationId.takeIf { it > 0 },
        serviceRequestId = serviceRequestId,
        caregiverId = caregiverId,
        offeredServiceId = offeredServiceId,
        initiatedBy = initiatedBy.name,
        status = status.name
    )
}

private fun ServiceApplicationDto.toEntity(): ServiceApplicationEntity {
    return ServiceApplicationEntity(
        applicationId = id ?: 0,
        serviceRequestId = serviceRequestId,
        caregiverId = caregiverId,
        offeredServiceId = offeredServiceId,
        initiatedBy = runCatching { ApplicationInitiator.valueOf(initiatedBy) }.getOrDefault(ApplicationInitiator.CAREGIVER),
        status = runCatching { ApplicationStatus.valueOf(status) }.getOrDefault(ApplicationStatus.PENDING)
    )
}


private fun RatingEntity.toDto(): RatingDto {
    return RatingDto(
        id = ratingId.takeIf { it > 0 },
        serviceRequestId = serviceRequestId,
        caregiverId = caregiverId,
        ownerId = ownerId,
        ratedByRole = ratedByRole.name,
        score = score,
        comment = comment
    )
}
