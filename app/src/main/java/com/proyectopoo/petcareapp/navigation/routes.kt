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
    val email: String
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
data class CreateService(
    val serviceType: String = "",
    val petName: String = "",
)

@Serializable
data class OwnerProfile(
    val ownerId: Int = -1,
    val serviceRequestId: Int = -1
)

@Serializable
data class CaregiverProfile(
    val caregiverId: Int = -1
)

@Serializable
object PasswordRecovery

@Serializable
data class DogInfo(
    val petId: Int = -1
)

@Serializable
data class OwnerPublicProfile(val ownerId: Int)

@Serializable
data class CaregiverPublicProfile(val caregiverId: Int)

@Serializable
data class EditOwnerProfile(val ownerId: Int = -1)

@Serializable
data class EditCaregiverProfile(val caregiverId: Int = -1)
