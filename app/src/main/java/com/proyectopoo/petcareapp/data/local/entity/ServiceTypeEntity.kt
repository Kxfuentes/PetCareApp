package com.proyectopoo.petcareapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_types")

data class ServiceTypeEntity(

    @PrimaryKey
    val serviceTypeId: Int,

    val name: String,

    val description: String? = null
)