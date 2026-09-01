package com.proyectopoo.petcareapp.data.network

import kotlinx.serialization.Serializable

@Serializable
data class SendOtpRequest(val email: String)

@Serializable
data class SendOtpResponse(
    val message: String? = null,
    // Solo viene relleno cuando el backend no tiene SMTP configurado (modo de prueba).
    val otp: String? = null
)

@Serializable
data class VerifyOtpRequest(val email: String, val otp: String)

@Serializable
data class VerifyOtpResponse(val verified: Boolean = false, val error: String? = null)
