package com.proyectopoo.petcareapp.data.repository

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
import com.proyectopoo.petcareapp.data.network.ApiService
import com.proyectopoo.petcareapp.data.network.ServiceApplicationRequest

class ServiceApplicationRepository(
    private val dao: ServiceApplicationDao,
    private val requestDao: ServiceRequestDao,
    private val bookingDao: ServiceBookingDao,
    private val apiService: ApiService? = null
) {
    suspend fun insert(application: ServiceApplicationEntity) {
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
                requestId = application.serviceRequestId,
                request = ServiceApplicationRequest(
                    caregiverId = application.caregiverId,
                    initiatedBy = application.initiatedBy.name
                )
            )

            Log.d("ServiceApplicationRepo", "Postulacion creada en API: $response")

            if (response != null) {
                application.copy(
                    applicationId = response.id,
                    serviceRequestId = response.serviceRequestId,
                    caregiverId = response.caregiverId,
                    initiatedBy = ApplicationInitiator.valueOf(response.initiatedBy),
                    status = ApplicationStatus.valueOf(response.status)
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

    suspend fun getIncomingOwnerRequestsForCaregiver(caregiverId: Int) =
        dao.getDetailsByCaregiver(caregiverId, ApplicationInitiator.OWNER)

    suspend fun getApplicationsForOffer(caregiverId: Int, offeredServiceId: Int) =
        dao.getDetailsByOffer(caregiverId, offeredServiceId)

    suspend fun getCaregiverApplications(caregiverId: Int) =
        dao.getDetailsByCaregiver(caregiverId, ApplicationInitiator.CAREGIVER)

    suspend fun getOwnerRequestsToCaregivers(ownerId: Int) =
        dao.getDetailsByOwner(ownerId, ApplicationInitiator.OWNER)

    suspend fun getApplicationById(id: Int) = dao.getById(id)

    suspend fun updateStatus(id: Int, status: ApplicationStatus) = dao.updateStatus(id, status)

    suspend fun acceptAndCreateBooking(applicationId: Int) {
        val application = dao.getById(applicationId) ?: return
        val request = requestDao.getRequestById(application.serviceRequestId) ?: return

        dao.updateStatus(applicationId, ApplicationStatus.ACCEPTED)
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

    suspend fun completeAndCloseRequest(applicationId: Int) {
        dao.updateStatus(applicationId, ApplicationStatus.COMPLETED)
        dao.updateRequestStatusForApplication(applicationId, ServiceRequestStatus.COMPLETED)
    }

    /** Cancela el servicio aceptado: marca la postulacion, la solicitud y la reserva como canceladas. */
    suspend fun cancelService(applicationId: Int) {
        val application = dao.getById(applicationId) ?: return
        dao.updateStatus(applicationId, ApplicationStatus.CANCELLED)
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
}