package com.proyectopoo.petcareapp.data.local.entity

/*
 * Comentario de modulo PetCare:
 * Modelo local de Room. Representa como se guarda esta informacion dentro de la base local de la app.
 */

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "service_bookings",
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
        Index("caregiverId")
    ]
)
data class ServiceBookingEntity(
    @PrimaryKey
    val bookingId: Int,

    val serviceRequestId: Int,

    val caregiverId: Int,

    val startDate: String? = null,

    val endDate: String? = null,

    val status: BookingStatus = BookingStatus.ACTIVE
)

enum class BookingStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED
}