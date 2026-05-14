package com.proyectopoo.petcareapp.data.network

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val rol: String? = null
)

@Serializable
data class RegisterResponse(
    val user: UserDto,
    val session: SessionDto
)

@Serializable
data class UserDto(
    val id: Int,
    val username: String,
    val email: String,
    val rol: String
)

@Serializable
data class SessionDto(
    val id: Int,
    val token_sesion: String,
    val fecha_inicio: String
)

@Serializable
data class ErrorResponse(
    val error: String
)
