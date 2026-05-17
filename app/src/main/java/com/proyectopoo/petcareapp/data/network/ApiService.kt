package com.proyectopoo.petcareapp.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("api/users")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegisterResponse>

    @PUT("api/users/{id}")
    suspend fun updateUserRole(
        @Path("id") userId: Int,
        @Body request: RegisterRequest
    ): Response<RegisterResponse>
}
