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
    tableName = "ratings",
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
        ),
        ForeignKey(
            entity = OwnerEntity::class,
            parentColumns = ["ownerId"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("serviceRequestId"),
        Index("caregiverId"),
        Index("ownerId")
    ]
)
data class RatingEntity(
    @PrimaryKey(autoGenerate = true)
    val ratingId: Int = 0,
    val serviceRequestId: Int,
    val caregiverId: Int,
    val ownerId: Int,
    val ratedByRole: UserRoleType = UserRoleType.OWNER,
    val score: Double,
    val comment: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
