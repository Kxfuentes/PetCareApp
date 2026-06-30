package com.proyectopoo.petcareapp.data.repository

/*
 * Comentario de modulo PetCare:
 * Repositorio de datos. Centraliza llamadas a Room y API para que la pantalla no maneje detalles tecnicos.
 */

import android.util.Log
import com.proyectopoo.petcareapp.data.local.dao.ServiceApplicationDao
import com.proyectopoo.petcareapp.data.local.dao.ServiceBookingDao
import com.proyectopoo.petcareapp.data.local.dao.ServiceRequestDao
import com.proyectopoo.petcareapp.data.local.entity.ApplicationInitiator
import com.proyectopoo.petcareapp.data.local.entity.ApplicationStatus
import com.proyectopoo.petcareapp.data.local.entity.BookingStatus
import com.proyectopoo.petcareapp.data.local.entity.ServiceApplicationEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceBookingEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestStatus
import com.proyectopoo.petcareapp.data.local.relation.ServiceApplicationDetails
import com.proyectopoo.petcareapp.data.network.ApiService
import com.proyectopoo.petcareapp.data.network.ServiceApplicationRequest
import com.proyectopoo.petcareapp.data.network.ServiceApplicationStatusRequest

class ServiceApplicationRepository(
    private val dao: ServiceApplicationDao,
    private val requestDao: ServiceRequestDao,
    private val bookingDao: ServiceBookingDao,
    private val apiService: ApiService? = null
) {
    suspend fun insert(application: ServiceApplicationEntity) {
        // Primero se intenta crear la postulacion en API; Room queda como respaldo local.
        val localRequestExists = requestDao.getRequestById(application.serviceRequestId) != null

        val existing = if (localRequestExists) {
            dao.getByRequestAndCaregiver(
                serviceRequestId = application.serviceRequestId,
                caregiverId = application.caregiverId
            )
        } else {
            null
        }

        val applicationToSave = try {
            val response = apiService?.applyToServiceRequest(
                request = ServiceApplicationRequest(
                    serviceRequestId = application.serviceRequestId,
                    caregiverId = application.caregiverId,
                    offeredServiceId = application.offeredServiceId,
                    initiatedBy = application.initiatedBy.name,
                    status = application.status.name
                )
            )

            Log.d("ServiceApplicationRepo", "Postulacion creada en API: $response")

            if (response != null) {
                application.copy(
                    applicationId = response.id,
                    serviceRequestId = response.serviceRequestId,
                    caregiverId = response.caregiverId,
                    offeredServiceId = response.offeredServiceId,
                    initiatedBy = response.initiatedBy.toInitiator(),
                    status = response.status.toApplicationStatus()
                )
            } else {
                application
            }
        } catch (e: Exception) {
            Log.e(
                "ServiceApplicationRepo",
                "Error creando postulacion en API. requestId=${application.serviceRequestId}, caregiverId=${application.caregiverId}",
                e
            )
            application
        }

        if (localRequestExists && existing == null) {
            dao.insert(applicationToSave)
        }
    }

    suspend fun getByCaregiver(caregiverId: Int) = dao.getByCaregiver(caregiverId)

    suspend fun getIncomingCaregiverOffersForOwner(ownerId: Int) =
        dao.getDetailsByOwner(ownerId, ApplicationInitiator.CAREGIVER)

    suspend fun getIncomingCaregiverOffersForOwnerFromApi(
        ownerId: Int,
        requestRepo: ServiceRequestRepository
    ): List<ServiceApplicationDetails> {
        // Para el dueno se combinan postulaciones con el detalle de cada solicitud.
        val response = runCatching {
            apiService?.getServiceApplicationsByOwner(ownerId)
        }.getOrNull()

        if (response?.isSuccessful != true) {
            return getIncomingCaregiverOffersForOwner(ownerId)
        }

        val requestDetailsById = requestRepo.getRecentDetailsByOwnerFromApi(ownerId)
            .associateBy { it.serviceRequestId }

        return response.body().orEmpty()
            .filter { it.initiatedBy.equals("CAREGIVER", ignoreCase = true) }
            .filter {
                it.status.equals("PENDING", ignoreCase = true) ||
                    it.status.equals("ACCEPTED", ignoreCase = true) ||
                    it.status.equals("DONE_BY_CAREGIVER", ignoreCase = true) ||
                    it.status.equals("COMPLETED", ignoreCase = true)
            }
            .map { app ->
                val request = requestDetailsById[app.serviceRequestId]
                ServiceApplicationDetails(
                    applicationId = app.id,
                    serviceRequestId = app.serviceRequestId,
                    caregiverId = app.caregiverId,
                    ownerId = ownerId,
                    offeredServiceId = app.offeredServiceId,
                    initiatedBy = app.initiatedBy.toInitiator(),
                    applicationStatus = app.status.toApplicationStatus(),
                    requestTitle = request?.title ?: "Solicitud #${app.serviceRequestId}",
                    requestDescription = request?.description,
                    requestedDate = request?.requestedDate,
                    startTime = request?.startTime,
                    endTime = request?.endTime,
                    requestStatus = request?.status ?: ServiceRequestStatus.PENDING,
                    petName = request?.petName,
                    petNames = request?.petNames,
                    petBreed = request?.petBreed,
                    petSize = request?.petSize,
                    serviceTypeName = request?.serviceTypeName,
                    ownerName = request?.ownerName ?: "Dueño #$ownerId",
                    ownerPhone = request?.ownerPhone,
                    ownerEmail = request?.ownerEmail,
                    caregiverName = app.caregiverName ?: "Cuidador #${app.caregiverId}",
                    caregiverPhone = null,
                    caregiverEmail = null
                )
            }
    }

    suspend fun getAppliedRequestIdsForCaregiverFromApi(caregiverId: Int): Set<Int> {
        // Sirve para no mostrar de nuevo solicitudes donde el cuidador ya participo.
        val response = runCatching {
            apiService?.getServiceApplicationsByCaregiver(caregiverId)
        }.getOrNull()

        if (response?.isSuccessful != true) return emptySet()

        return response.body().orEmpty()
            .filterNot { it.status.equals("REJECTED", ignoreCase = true) || it.status.equals("CANCELLED", ignoreCase = true) }
            .map { it.serviceRequestId }
            .toSet()
    }

    suspend fun getIncomingOwnerRequestsForCaregiver(caregiverId: Int) =
        dao.getDetailsByCaregiver(caregiverId, ApplicationInitiator.OWNER)

    suspend fun getCaregiverApplicationDetailsFromApi(
        caregiverId: Int,
        requestRepo: ServiceRequestRepository
    ): List<ServiceApplicationDetails> {
        val response = runCatching {
            apiService?.getServiceApplicationsByCaregiver(caregiverId)
        }.getOrNull()

        if (response?.isSuccessful != true) {
            return getIncomingOwnerRequestsForCaregiver(caregiverId)
        }

        return response.body().orEmpty()
            .filterNot {
                it.status.equals("REJECTED", ignoreCase = true) ||
                    it.status.equals("CANCELLED", ignoreCase = true)
            }
            .mapNotNull { app ->
                val request = requestRepo.getRequestDetailsByIdFromApi(app.serviceRequestId)

                ServiceApplicationDetails(
                    applicationId = app.id,
                    serviceRequestId = app.serviceRequestId,
                    caregiverId = app.caregiverId,
                    ownerId = request?.ownerId ?: 0,
                    offeredServiceId = app.offeredServiceId,
                    initiatedBy = app.initiatedBy.toInitiator(),
                    applicationStatus = app.status.toApplicationStatus(),
                    requestTitle = request?.title ?: "Solicitud #${app.serviceRequestId}",
                    requestDescription = request?.description,
                    requestedDate = request?.requestedDate,
                    startTime = request?.startTime,
                    endTime = request?.endTime,
                    requestStatus = request?.status ?: ServiceRequestStatus.PENDING,
                    petName = request?.petName,
                    petNames = request?.petNames,
                    petBreed = request?.petBreed,
                    petSize = request?.petSize,
                    serviceTypeName = request?.title,
                    ownerName = app.ownerName ?: request?.ownerName ?: request?.ownerId?.let { "Dueño #$it" },
                    ownerPhone = null,
                    ownerEmail = null,
                    caregiverName = app.caregiverName ?: "Cuidador #${app.caregiverId}",
                    caregiverPhone = null,
                    caregiverEmail = null
                )
            }
    }

    suspend fun getApplicationsForOffer(caregiverId: Int, offeredServiceId: Int) =
        dao.getDetailsByOffer(caregiverId, offeredServiceId)

    suspend fun getCaregiverApplications(caregiverId: Int) =
        dao.getDetailsByCaregiver(caregiverId, ApplicationInitiator.CAREGIVER)

    suspend fun getOwnerRequestsToCaregivers(ownerId: Int) =
        dao.getDetailsByOwner(ownerId, ApplicationInitiator.OWNER)

    suspend fun getApplicationById(id: Int) = dao.getById(id)

    suspend fun updateStatus(id: Int, status: ApplicationStatus) {
        // Se actualiza remoto y local para mantener compatibilidad si la API no responde.
        updateStatusFromApi(id, status)
        dao.updateStatus(id, status)
    }

    suspend fun updateStatusFromApi(id: Int, status: ApplicationStatus): ServiceApplicationEntity? {
        val response = runCatching {
            apiService?.updateServiceApplicationStatus(
                applicationId = id,
                request = ServiceApplicationStatusRequest(status = status.name)
            )
        }.onFailure { error ->
            Log.e("ServiceApplicationRepo", "Error actualizando postulacion en API. applicationId=$id", error)
        }.getOrNull()

        return response?.let { dto ->
            ServiceApplicationEntity(
                applicationId = dto.id,
                serviceRequestId = dto.serviceRequestId,
                caregiverId = dto.caregiverId,
                offeredServiceId = dto.offeredServiceId,
                initiatedBy = dto.initiatedBy.toInitiator(),
                status = dto.status.toApplicationStatus()
            )
        }
    }

    suspend fun acceptAndCreateBooking(applicationId: Int) {
        val application = dao.getById(applicationId) ?: return
        val request = requestDao.getRequestById(application.serviceRequestId) ?: return

        // Al aceptar una postulacion, las demas de la misma solicitud quedan rechazadas.
        updateStatus(applicationId, ApplicationStatus.ACCEPTED)
        dao.rejectOtherApplications(
            requestId = application.serviceRequestId,
            exceptId = applicationId,
            status = ApplicationStatus.REJECTED
        )
        dao.updateRequestStatusForApplication(applicationId, ServiceRequestStatus.ACCEPTED)

        val bookingId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        bookingDao.insertBooking(
            ServiceBookingEntity(
                bookingId = bookingId,
                serviceRequestId = application.serviceRequestId,
                caregiverId = application.caregiverId,
                startDate = request.requestedDate,
                endDate = request.endTime ?: request.startTime,
                status = BookingStatus.ACTIVE
            )
        )
    }

    suspend fun completeAndCloseRequest(applicationId: Int): Boolean {
        // El cierre definitivo se intenta primero en API y luego se refleja en Room.
        val remote = updateStatusFromApi(applicationId, ApplicationStatus.COMPLETED)
        val local = dao.getById(applicationId)

        if (remote != null && local == null) return true
        if (local == null) return false

        dao.updateStatus(applicationId, ApplicationStatus.COMPLETED)
        dao.updateRequestStatusForApplication(applicationId, ServiceRequestStatus.COMPLETED)
        return true
    }

    /** Cancela el servicio aceptado: marca la postulacion, la solicitud y la reserva como canceladas. */
    suspend fun cancelService(applicationId: Int) {
        val application = dao.getById(applicationId)
        if (application == null) {
            // Si la postulacion solo existe en API, igual intentamos cancelar alla.
            updateStatusFromApi(applicationId, ApplicationStatus.CANCELLED)
            return
        }

        updateStatus(applicationId, ApplicationStatus.CANCELLED)
        dao.updateRequestStatusForApplication(applicationId, ServiceRequestStatus.CANCELLED)
        bookingDao.updateStatusByRequest(application.serviceRequestId, BookingStatus.CANCELLED)
    }

    suspend fun updateRequestSchedule(
        requestId: Int,
        date: String?,
        startTime: String?,
        endTime: String?
    ) {
        requestDao.updateSchedule(requestId, date, startTime, endTime)
    }

    private fun String.toInitiator(): ApplicationInitiator {
        return runCatching { ApplicationInitiator.valueOf(uppercase()) }
            .getOrDefault(ApplicationInitiator.CAREGIVER)
    }

    private fun String.toApplicationStatus(): ApplicationStatus {
        return runCatching { ApplicationStatus.valueOf(uppercase()) }
            .getOrDefault(ApplicationStatus.PENDING)
    }
}

