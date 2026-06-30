package com.proyectopoo.petcareapp.data.local.entity

/*
 * Comentario de modulo PetCare:
 * Modelo local de Room. Representa como se guarda esta informacion dentro de la base local de la app.
 */

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_types")

data class ServiceTypeEntity(

    @PrimaryKey
    val serviceTypeId: Int,

    val name: String,

    val description: String? = null
)