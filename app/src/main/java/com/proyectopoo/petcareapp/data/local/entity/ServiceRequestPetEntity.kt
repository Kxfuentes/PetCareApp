package com.proyectopoo.petcareapp.data.local.entity

/*
 * Comentario de modulo PetCare:
 * Modelo local de Room. Representa como se guarda esta informacion dentro de la base local de la app.
 */

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "service_request_pets",
    primaryKeys = ["serviceRequestId", "petId"],
    foreignKeys = [
        ForeignKey(
            entity = ServiceRequestEntity::class,
            parentColumns = ["serviceRequestId"],
            childColumns = ["serviceRequestId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PetEntity::class,
            parentColumns = ["petId"],
            childColumns = ["petId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("serviceRequestId"), Index("petId")]
)
data class ServiceRequestPetEntity(
    val serviceRequestId: Int,
    val petId: Int
)
