package com.proyectopoo.petcareapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "service_requests",
    foreignKeys = [
        ForeignKey(
            entity = OwnerEntity::class,
            parentColumns = ["ownerId"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PetEntity::class,
            parentColumns = ["petId"],
            childColumns = ["petId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ServiceTypeEntity::class,
            parentColumns = ["serviceTypeId"],
            childColumns = ["serviceTypeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("ownerId"),
        Index("petId"),
        Index("serviceTypeId")
    ]
)
data class ServiceRequestEntity(
    @PrimaryKey
    val serviceRequestId: Int,

    val ownerId: Int,
    val petId: Int,
    val serviceTypeId: Int,

    val title: String,
    val description: String? = null,

    val requestedDate: String? = null,

    // === NUEVOS CAMPOS ===
    val startTime: String? = null,
    val endTime: String? = null,

    val status: ServiceRequestStatus = ServiceRequestStatus.PENDING
)

enum class ServiceRequestStatus {
    PENDING,
    ACCEPTED,
    CANCELLED,
    COMPLETED
}