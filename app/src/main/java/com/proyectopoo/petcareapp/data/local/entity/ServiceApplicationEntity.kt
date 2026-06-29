package com.proyectopoo.petcareapp.data.local.entity

import androidx.room.*

@Entity(
    tableName = "service_applications",

    foreignKeys = [
        ForeignKey(
            entity = ServiceRequestEntity::class,
            parentColumns = ["serviceRequestId"],
            childColumns = ["serviceRequestId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CaregiverEntity::class,
            parentColumns = ["caregiverId"],
            childColumns = ["caregiverId"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    indices = [
        Index("serviceRequestId"),
        Index("caregiverId"),
        Index("offeredServiceId")
    ]
)
data class ServiceApplicationEntity(

    @PrimaryKey(autoGenerate = true)
    val applicationId: Int = 0,

    val serviceRequestId: Int,

    val caregiverId: Int,

    val offeredServiceId: Int? = null,

    val initiatedBy: ApplicationInitiator = ApplicationInitiator.CAREGIVER,

    val status: ApplicationStatus = ApplicationStatus.PENDING
)

enum class ApplicationInitiator {
    OWNER,
    CAREGIVER
}

enum class ApplicationStatus {
    PENDING,
    ACCEPTED,
    REJECTED,

    COMPLETED,

    CANCELLED
}
