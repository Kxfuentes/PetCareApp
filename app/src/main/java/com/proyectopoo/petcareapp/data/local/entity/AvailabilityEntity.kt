package com.proyectopoo.petcareapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "availability",
    foreignKeys = [
        ForeignKey(
            entity = CaregiverEntity::class,
            parentColumns = ["caregiverId"],
            childColumns = ["caregiverId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("caregiverId")]
)
data class AvailabilityEntity(
    @PrimaryKey
    val availabilityId: Int,

    val caregiverId: Int,

    val isGloballyAvailable: Boolean = true,

    val updatedAt: String? = null
)