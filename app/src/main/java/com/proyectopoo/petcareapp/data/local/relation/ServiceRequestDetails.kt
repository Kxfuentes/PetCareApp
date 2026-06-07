package com.proyectopoo.petcareapp.data.local.relation

import com.proyectopoo.petcareapp.data.local.entity.ApplicationStatus
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestStatus

data class ServiceRequestDetails(
    val serviceRequestId: Int,
    val ownerId: Int,
    val petId: Int,
    val serviceTypeId: Int,
    val title: String,
    val description: String?,
    val requestedDate: String?,
    val status: ServiceRequestStatus,
    val petName: String?,
    val petBreed: String?,
    val petSize: String?,
    val serviceTypeName: String?,
    val ownerName: String?,
    val ownerPhone: String?,
    val ownerEmail: String?
)

data class ServiceApplicationDetails(
    val applicationId: Int,
    val serviceRequestId: Int,
    val caregiverId: Int,
    val applicationStatus: ApplicationStatus,
    val requestTitle: String,
    val requestDescription: String?,
    val requestedDate: String?,
    val requestStatus: ServiceRequestStatus,
    val petName: String?,
    val petBreed: String?,
    val petSize: String?,
    val serviceTypeName: String?,
    val ownerName: String?,
    val ownerPhone: String?,
    val ownerEmail: String?,
    val caregiverName: String?,
    val caregiverPhone: String?,
    val caregiverEmail: String?
)
