// app/src/main/java/com/proyectopoo/petcareapp/data/local/entity/RatingEntity.kt

package com.proyectopoo.petcareapp.data.local.entity

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
    val serviceRequestId: String,   // ID de la solicitud de servicio calificada
    val caregiverId: String,         // ID del cuidador calificado
    val ownerId: String,             // ID del dueño que califica
    val score: Double,            // Calificación (1.0 a 5.0)
    val comment: String? = null,  // Comentario opcional
    val createdAt: Long = System.currentTimeMillis()  // Timestamp
)