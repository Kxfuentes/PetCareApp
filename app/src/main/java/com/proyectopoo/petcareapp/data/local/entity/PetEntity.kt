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

    val size: String?
)