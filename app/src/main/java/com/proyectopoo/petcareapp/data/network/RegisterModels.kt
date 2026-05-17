package com.proyectopoo.petcareapp.data.network

import kotlinx.serialization.SerialName
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
    @SerialName("user") val user: UserDto? = null,
    @SerialName("session") val session: SessionDto? = null,
    @SerialName("useer") val useer: UserDto? = null,
    @SerialName("sessions") val sessions: SessionDto? = null
)

@Serializable
data class UserDto(
    val id: Int,
    val username: String,
    val email: String,
    val rol: String? = null
)

@Serializable
data class SessionDto(
    val id: Int? = null,
    @SerialName("token_sesion") val tokenSesion: String? = null,
    @SerialName("fecha_inicio") val fechaInicio: String? = null
)

@Serializable
data class ErrorResponse(
    val error: String? = null
)
