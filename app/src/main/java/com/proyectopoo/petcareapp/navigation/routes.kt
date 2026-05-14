package com.proyectopoo.petcareapp.navigation

import kotlinx.serialization.Serializable

@Serializable
object Login

@Serializable
object Register

@Serializable
data class RoleSection(
    val userId: Int,
    val username: String,
    val email: String,
    val password: String
)

@Serializable
object DogInfo

@Serializable
object OwnerHome

@Serializable
object OwnerFeed

@Serializable
object CaregiverHome

@Serializable
object CaregiverFeed

@Serializable
object CaregiverService

@Serializable
object CreateService

@Serializable
object Profile