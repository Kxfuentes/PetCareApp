package com.proyectopoo.petcareapp.data.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/users")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegisterResponse>

    @GET("api/razas")
    suspend fun searchDogBreeds(@Query("q") q: String?): Response<DogBreedsResponse>

    @POST("api/chat/mensajes")
    suspend fun sendChatMessage(@Body request: ChatMessageRequest): Response<ChatMessageDto>

    @GET("api/chat/mensajes/{serviceRequestId}")
    suspend fun getChatMessages(@Path("serviceRequestId") serviceRequestId: Int): Response<List<ChatMessageDto>>

    @PUT("api/chat/mensajes/leidos")
    suspend fun markChatMessagesRead(
        @Query("serviceRequestId") serviceRequestId: Int,
        @Query("userId") userId: Int
    ): Response<Unit>

    @GET("api/chat/no-leidos/{userId}")
    suspend fun getUnreadChatCount(@Path("userId") userId: Int): Response<UnreadCountDto>

    @Multipart
    @POST("api/chat/{serviceRequestId}/imagen")
    suspend fun sendChatImage(
        @Path("serviceRequestId") serviceRequestId: Int,
        @Part("senderId") senderId: RequestBody,
        @Part("receiverId") receiverId: RequestBody,
        @Part("message") message: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<ChatMessageDto>

    @GET("api/chat/{serviceRequestId}/imagenes")
    suspend fun getChatImages(@Path("serviceRequestId") serviceRequestId: Int): Response<List<ChatMessageDto>>

    @POST("api/auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<SendOtpResponse>

    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<VerifyOtpResponse>

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
    suspend fun deletePet(@Path("id") id: Int): Response<Unit>

    @GET("api/offered-services/caregiver/{caregiverId}")
    suspend fun getOfferedServicesByCaregiver(@Path("caregiverId") caregiverId: Int): Response<List<OfferedServiceDto>>

    @GET("api/offered-services/available")
    suspend fun getAvailableOfferedServices(): Response<List<OfferedServiceDto>>

    @POST("api/offered-services")
    suspend fun createOfferedService(@Body request: OfferedServiceRequest): Response<OfferedServiceDto>

    @PUT("api/offered-services/{id}")
    suspend fun updateOfferedService(
        @Path("id") id: Int,
        @Body request: OfferedServiceRequest
    ): Response<OfferedServiceDto>

    @DELETE("api/offered-services/{id}")
    suspend fun deleteOfferedService(@Path("id") id: Int): Response<Unit>

    @GET("api/offered-services/{id}")
    suspend fun getOfferedServiceById(
        @Path("id") id: Int
    ): Response<OfferedServiceDto>

    @GET("api/service-requests/owner/{ownerId}")
    suspend fun getServiceRequestsByOwner(@Path("ownerId") ownerId: Int): Response<List<ServiceRequestDto>>

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
    ): Response<ServiceApplicationDto>

    @POST("api/service-applications")
    suspend fun createServiceApplication(
        @Body request: ServiceApplicationRequest
    ): Response<ServiceApplicationDto>

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
    ): Response<ServiceApplicationDto>

    @POST("api/ofertas/{id}/rechazar")
    suspend fun rejectOferta(
        @Path("id") id: Int,
        @Body request: OfertaMotivoRequest
    ): Response<ServiceApplicationDto>

    @POST("api/ofertas/{id}/cancelar")
    suspend fun cancelOferta(
        @Path("id") id: Int,
        @Body request: OfertaMotivoRequest
    ): Response<ServiceApplicationDto>

    @POST("api/ratings")
    suspend fun createRating(
        @Body request: RatingRequest
    ): Response<RatingDto>

    @GET("api/ratings/caregiver/{caregiverId}/reviews")
    suspend fun getCaregiverReviews(
        @Path("caregiverId") caregiverId: Int
    ): Response<List<RatingDto>>

    @GET("api/ratings/owner/{ownerId}/reviews")
    suspend fun getOwnerReviews(
        @Path("ownerId") ownerId: Int
    ): Response<List<RatingDto>>

    @GET("api/ratings/caregiver/{caregiverId}/summary")
    suspend fun getCaregiverRatingSummary(
        @Path("caregiverId") caregiverId: Int
    ): Response<RatingSummaryDto>

    @GET("api/ratings/owner/{ownerId}/summary")
    suspend fun getOwnerRatingSummary(
        @Path("ownerId") ownerId: Int
    ): Response<RatingSummaryDto>

    @GET("api/usuarios/{id}/badge")
    suspend fun getUsuarioBadge(@Path("id") id: Int): Response<BadgeDto>

    @POST("api/usuarios/fcm-token")
    suspend fun sendFcmToken(
        @Body request: FcmTokenRequest
    ): Response<Unit>

    @PUT("api/usuarios/no-molestar")
    suspend fun updateNoMolestar(
        @Body request: NoMolestarRequest
    ): Response<Unit>

    @GET("api/users/{id}")
    suspend fun getUserById(
        @Path("id") id: Int
    ): Response<UserLocationDto>

    @GET("api/solicitudes/historial")
    suspend fun getHistorialSolicitudes(
        @Query("usuarioId") usuarioId: Int,
        @Query("role") role: String
    ): Response<List<ServiceRequestDto>>

    @GET("api/solicitudes/buscar")
    suspend fun buscarSolicitudes(
        @Query("q") q: String?,
        @Query("serviceTypeId") serviceTypeId: Int?,
        @Query("status") status: String?
    ): Response<List<ServiceRequestDto>>

    @PUT("api/solicitudes/{id}")
    suspend fun editarSolicitud(
        @Path("id") id: Int,
        @Body request: SolicitudEditRequest
    ): Response<ServiceRequestDto>

    @POST("api/solicitudes/{id}/ubicacion")
    suspend fun postUbicacion(
        @Path("id") id: Int,
        @Body request: UbicacionRequest
    ): Response<UbicacionResponse>

    @GET("api/solicitudes/{id}/ubicacion-actual")
    suspend fun getUbicacionActual(
        @Path("id") id: Int
    ): Response<UbicacionActualResponse>

    @POST("api/emergencias")
    suspend fun reportarEmergencia(
        @Body request: EmergenciaRequest
    ): Response<EmergenciaDto>

    @POST("api/solicitudes/{id}/valorar-durante")
    suspend fun valorarDurante(
        @Path("id") id: Int,
        @Body request: ValoracionDuranteRequest
    ): Response<ValoracionDuranteDto>

    @GET("api/favoritos")
    suspend fun getFavoritos(
        @Query("usuarioId") usuarioId: Int
    ): Response<List<FavoritoDto>>

    @POST("api/favoritos")
    suspend fun agregarFavorito(
        @Body request: FavoritoDto
    ): Response<FavoritoDto>

    @DELETE("api/favoritos/{id}")
    suspend fun eliminarFavorito(
        @Path("id") id: Int
    ): Response<Unit>

    // ===== Evidencia foto antes/después de un servicio (Bloque 8) =====

    @Multipart
    @POST("api/solicitudes/{id}/evidencia")
    suspend fun subirEvidencia(
        @Path("id") id: Int,
        @Part("tipo") tipo: RequestBody,
        @Part file: MultipartBody.Part,
        @Part("nota") nota: RequestBody?,
        @Query("latitud") latitud: Double? = null,
        @Query("longitud") longitud: Double? = null
    ): Response<EvidenciaDto>

    @GET("api/solicitudes/{id}/evidencias")
    suspend fun getEvidencias(
        @Path("id") id: Int
    ): Response<List<EvidenciaDto>>

    // ===== Calendario integrado (Bloque 9) =====

    @GET("api/calendario")
    suspend fun getCalendario(
        @Query("usuario_id") usuarioId: Int,
        @Query("mes") mes: Int,
        @Query("anio") anio: Int
    ): Response<CalendarioResponseDto>

    // ===== Expediente médico de la mascota (Bloque 11) =====

    @GET("api/pets/{id}/expediente")
    suspend fun getExpediente(
        @Path("id") petId: Int,
        @Query("usuario_id") usuarioId: Int
    ): Response<List<ExpedienteEntryDto>>

    @Multipart
    @POST("api/pets/{id}/expediente")
    suspend fun createExpedienteEntry(
        @Path("id") petId: Int,
        @Part("usuario_id") usuarioId: RequestBody,
        @Part("tipo") tipo: RequestBody,
        @Part("titulo") titulo: RequestBody,
        @Part("descripcion") descripcion: RequestBody?,
        @Part("fecha") fecha: RequestBody,
        @Part("fecha_proxima") fechaProxima: RequestBody?,
        @Part("veterinario_nombre") veterinarioNombre: RequestBody?,
        @Part("veterinario_telefono") veterinarioTelefono: RequestBody?,
        @Part file: MultipartBody.Part?
    ): Response<ExpedienteEntryDto>

    @PUT("api/pets/expediente/{entradaId}")
    suspend fun updateExpedienteEntry(
        @Path("entradaId") entradaId: Int,
        @Query("usuario_id") usuarioId: Int,
        @Body request: ExpedienteEntryRequest
    ): Response<ExpedienteEntryDto>

    @DELETE("api/pets/expediente/{entradaId}")
    suspend fun deleteExpedienteEntry(
        @Path("entradaId") entradaId: Int,
        @Query("usuario_id") usuarioId: Int
    ): Response<Unit>

    // ===== Alerta de mascota perdida (Bloque 12) =====

    @POST("api/alertas-perdida")
    suspend fun crearAlertaPerdida(@Body request: AlertaPerdidaRequest): Response<AlertaPerdidaDto>

    @POST("api/alertas-perdida/{id}/avistamiento")
    suspend fun reportarAvistamiento(
        @Path("id") alertaId: Int,
        @Body request: AvistamientoRequest
    ): Response<AvistamientoDto>

    @GET("api/alertas-perdida/{id}/avistamientos")
    suspend fun getAvistamientos(@Path("id") alertaId: Int): Response<List<AvistamientoDto>>

    @PUT("api/alertas-perdida/{id}/encontrada")
    suspend fun marcarAlertaEncontrada(
        @Path("id") alertaId: Int,
        @Query("usuario_id") usuarioId: Int
    ): Response<AlertaPerdidaDto>

    @GET("api/alertas-perdida/cercanas")
    suspend fun getAlertasCercanas(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radio") radioKm: Double = 10.0
    ): Response<List<AlertaCercanaDto>>
}
