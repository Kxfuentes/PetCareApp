package com.proyectopoo.petcareapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "offered_services",

    foreignKeys = [

        ForeignKey(
            entity = CaregiverEntity::class,
            parentColumns = ["caregiverId"],
            childColumns = ["caregiverId"],
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
        Index("caregiverId"),
        Index("serviceTypeId")
    ]
)

data class OfferedServiceEntity(

    @PrimaryKey
    val offeredServiceId: Int,

    val caregiverId: Int,

    val serviceTypeId: Int,

    val title: String,

    val description: String? = null,

    val price: Double,

    val isAvailable: Boolean = true
)