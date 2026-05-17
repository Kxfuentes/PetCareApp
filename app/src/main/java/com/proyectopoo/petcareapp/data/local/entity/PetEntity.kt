package com.proyectopoo.petcareapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pets",

    foreignKeys = [
        ForeignKey(
            entity = OwnerEntity::class,
            parentColumns = ["ownerId"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    indices = [Index("ownerId")]
)

data class PetEntity(

    @PrimaryKey
    val petId: Int,

    val ownerId: Int,

    val name: String,

    val species: String,

    val breed: String? = null,

    val age: Int? = null,

    val weight: Double? = null,

    val notes: String? = null
)