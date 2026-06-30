package com.proyectopoo.petcareapp.data.network

/*
 * Comentario de modulo PetCare:
 * Contrato de red. Define datos y endpoints usados para comunicarse con la API de PetCare.
 */

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("api/users")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegisterResponse>

    @PUT("api/users/{id}")
    suspend fun updateUserRole(
        @Path("id") userId: Int,
        @Body request: RoleUpdateRequest
    ): Response<UserDto>

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("api/pets/owner/{ownerId}")
    suspend fun getPetsByOwner(
        @Path("ownerId") ownerId: Int
    ): Response<List<PetDto>>

    @POST("api/pets")
    suspend fun createPet(
        @Body request: PetRequest
    ): Response<PetDto>

    @PUT("api/pets/{id}")
    suspend fun updatePet(
        @Path("id") petId: Int,
        @Body request: PetRequest
    ): Response<PetDto>

    @DELETE("api/pets/{id}")
    suspend fun deletePet(
        @Path("id") petId: Int
    ): Response<Unit>

    @GET("api/service-requests/owner/{ownerId}")
    suspend fun getServiceRequestsByOwner(
        @Path("ownerId") ownerId: Int
    ): Response<List<ServiceRequestDto>>

    @GET("api/service-requests/available")
    suspend fun getAvailableServiceRequests(): Response<List<ServiceRequestDto>>

    @GET("api/service-requests/{id}")
    suspend fun getServiceRequestById(
        @Path("id") id: Int
    ): Response<ServiceRequestDto>

    @POST("api/service-requests")
    suspend fun createServiceRequest(
        @Body request: ServiceRequestRequest
    ): Response<ServiceRequestDto>

    @PUT("api/service-requests/{id}/status")
    suspend fun updateServiceRequestStatus(
        @Path("id") id: Int,
        @Body request: StatusUpdateRequest
    ): Response<ServiceRequestDto>

    @POST("api/service-applications")
    suspend fun applyToServiceRequest(
        @Body request: ServiceApplicationRequest
    ): ServiceApplicationDto

    @GET("api/service-applications/owner/{ownerId}")
    suspend fun getServiceApplicationsByOwner(
        @Path("ownerId") ownerId: Int
    ): Response<List<ServiceApplicationDto>>

    @GET("api/service-applications/caregiver/{caregiverId}")
    suspend fun getServiceApplicationsByCaregiver(
        @Path("caregiverId") caregiverId: Int
    ): Response<List<ServiceApplicationDto>>

    @PUT("api/service-applications/{applicationId}/status")
    suspend fun updateServiceApplicationStatus(
        @Path("applicationId") applicationId: Int,
        @Body request: ServiceApplicationStatusRequest
    ): ServiceApplicationDto

    @GET("api/offered-services/caregiver/{caregiverId}")
    suspend fun getOfferedServicesByCaregiver(
        @Path("caregiverId") caregiverId: Int
    ): Response<List<OfferedServiceDto>>

    @GET("api/offered-services/available")
    suspend fun getAvailableOfferedServices(): Response<List<OfferedServiceDto>>

    @GET("api/offered-services/{id}")
    suspend fun getOfferedServiceById(
        @Path("id") id: Int
    ): Response<OfferedServiceDto>

    @POST("api/offered-services")
    suspend fun createOfferedService(
        @Body request: OfferedServiceRequest
    ): Response<OfferedServiceDto>

    @PUT("api/offered-services/{id}")
    suspend fun updateOfferedService(
        @Path("id") id: Int,
        @Body request: OfferedServiceRequest
    ): Response<OfferedServiceDto>

    @DELETE("api/offered-services/{id}")
    suspend fun deleteOfferedService(
        @Path("id") id: Int
    ): Response<Unit>

    @POST("api/ratings")
    suspend fun createRating(
        @Body request: RatingRequest
    ): Response<RatingDto>

    @GET("api/ratings/caregiver/{caregiverId}/summary")
    suspend fun getCaregiverRatingSummary(
        @Path("caregiverId") caregiverId: Int
    ): Response<RatingSummaryDto>

    @GET("api/ratings/owner/{ownerId}/summary")
    suspend fun getOwnerRatingSummary(
        @Path("ownerId") ownerId: Int
    ): Response<RatingSummaryDto>
}
