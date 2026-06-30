package com.proyectopoo.petcareapp.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PetRequest(
    @SerialName("owner_id")
    val ownerId: Int,
    val name: String,
    val species: String = "Dog",
    val breed: String,
    val size: String
)

@Serializable
data class PetDto(
    val id: Int,
    @SerialName("owner_id")
    val ownerId: Int,
    val name: String,
    val species: String? = null,
    val breed: String? = null,
    val size: String? = null
)