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
object OwnerProfile

@Serializable
object CaregiverProfile

@Serializable
object PasswordRecovery

@Serializable
data class DogInfo(
    val petId: Int = -1
)
