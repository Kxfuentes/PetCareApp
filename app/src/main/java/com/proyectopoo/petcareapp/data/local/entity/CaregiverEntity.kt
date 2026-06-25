package com.proyectopoo.petcareapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "caregivers",

    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    indices = [Index("userId")]
)

data class CaregiverEntity(

    @PrimaryKey
    val caregiverId: Int,

    val userId: String,

    val description: String? = null,

    val experienceYears: Int = 0,

    val rating: Double = 0.0
)