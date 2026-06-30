package com.proyectopoo.petcareapp.data.repository

/*
 * Comentario de modulo PetCare:
 * Repositorio de datos. Centraliza llamadas a Room y API para que la pantalla no maneje detalles tecnicos.
 */

import com.proyectopoo.petcareapp.data.local.dao.ServiceRequestDao
import com.proyectopoo.petcareapp.data.local.dao.ServiceRequestPetDao
import com.proyectopoo.petcareapp.data.local.entity.RequestSource
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestPetEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestStatus
import com.proyectopoo.petcareapp.data.local.relation.ServiceRequestDetails
import com.proyectopoo.petcareapp.data.network.ApiService
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import com.proyectopoo.petcareapp.data.network.ServiceRequestDto
import com.proyectopoo.petcareapp.data.network.ServiceRequestRequest
import com.proyectopoo.petcareapp.data.network.StatusUpdateRequest

class ServiceRequestRepository(
    private val dao: ServiceRequestDao,
    private val petDao: ServiceRequestPetDao,
    private val apiService: ApiService = RetrofitClient.apiService
) {
    suspend fun insert(request: ServiceRequestEntity, petIds: List<Int> = emptyList()) {
        // La API es la fuente principal. Si falla, se conserva la solicitud en Room.
        val localPetIds = petIds.ifEmpty { listOf(request.petId) }

        val remoteResponse = runCatching {
            apiService.createServiceRequest(request.toRequest(localPetIds))
        }.getOrNull()

        val requestToSave = if (remoteResponse?.isSuccessful == true) {
            remoteResponse.body()?.toEntity() ?: request
        } else {
            request
        }

        val petsToSave = if (remoteResponse?.isSuccessful == true) {
            remoteResponse.body()?.petIds?.ifEmpty { localPetIds } ?: localPetIds
        } else {
            localPetIds
        }

        saveLocal(requestToSave, petsToSave)
    }

    suspend fun getByOwner(ownerId: Int): List<ServiceRequestEntity> {
        return dao.getRequestsByOwner(ownerId)
    }

    suspend fun getWithApplications(ownerId: Int) = dao.getRequestsWithApplications(ownerId)

    suspend fun getAvailableDetails() =
        dao.getRequestDetailsByStatus(ServiceRequestStatus.PENDING)

    suspend fun getAvailableDetailsFromApi(): List<ServiceRequestDetails> {
        // Las solicitudes disponibles se leen remoto para que todos los usuarios vean lo mismo.
        val remoteResponse = runCatching {
            apiService.getAvailableServiceRequests()
        }.getOrNull()

        if (remoteResponse?.isSuccessful != true) {
            return getAvailableDetails()
        }

        val remoteRequests = remoteResponse.body().orEmpty()

        if (remoteRequests.isEmpty()) {
            return emptyList()
        }

        return remoteRequests.toDetailsList()
    }

    suspend fun getRecentDetailsByOwner(ownerId: Int): List<ServiceRequestDetails> {
        return dao.getRecentRequestDetailsByOwner(ownerId)
    }

    suspend fun getRecentDetailsByOwnerFromApi(ownerId: Int): List<ServiceRequestDetails> {
        // El dueno debe ver su historial reciente aunque haya cambiado de dispositivo.
        val remoteResponse = runCatching {
            apiService.getServiceRequestsByOwner(ownerId)
        }.getOrNull()

        if (remoteResponse?.isSuccessful != true) {
            return getRecentDetailsByOwner(ownerId)
        }

        return remoteResponse.body().orEmpty().toDetailsList()
    }

    suspend fun getRequestDetailsByIdFromApi(id: Int): ServiceRequestDetails? {
        val remoteResponse = runCatching {
            apiService.getServiceRequestById(id)
        }.getOrNull()

        if (remoteResponse?.isSuccessful != true) return null

        val request = remoteResponse.body() ?: return null
        val petNamesById = petNameMapForOwner(request.ownerId)
        return request.toDetails(petNamesById)
    }

    suspend fun getDetailsByOwnerAndStatus(
        ownerId: Int,
        status: ServiceRequestStatus
    ): List<ServiceRequestDetails> {
        return dao.getRequestDetailsByOwnerAndStatus(ownerId, status)
    }

    suspend fun updateStatus(id: Int, status: ServiceRequestStatus) {
        // El estado se manda a la API y tambien se actualiza localmente si existe en Room.
        runCatching {
            apiService.updateServiceRequestStatus(
                id = id,
                request = StatusUpdateRequest(status = status.name)
            )
        }

        dao.updateStatus(id, status)
    }

    suspend fun getRequestById(id: Int): ServiceRequestEntity? {
        // Primero buscamos local; si no esta, se consulta API para reconstruir el dato minimo.
        val local = dao.getRequestById(id)
        if (local != null) return local

        val remoteResponse = runCatching {
            apiService.getServiceRequestById(id)
        }.getOrNull()

        if (remoteResponse?.isSuccessful == true) {
            val dto = remoteResponse.body() ?: return null
            return dto.toEntity()
        }

        return null
    }

    suspend fun loadAvailableFromApi() {
        // Ya no guardamos solicitudes remotas en Room aquÃ­,
        // porque pueden venir con mascotas/tipos/dueÃ±os que no existen localmente.
        getAvailableDetailsFromApi()
    }

    private suspend fun saveLocal(request: ServiceRequestEntity, petIds: List<Int>) {
        // Room guarda la solicitud y una tabla puente para soportar varias mascotas.
        dao.insertRequest(request)

        petDao.insertAll(
            petIds.ifEmpty { listOf(request.petId) }.map { petId ->
                ServiceRequestPetEntity(
                    serviceRequestId = request.serviceRequestId,
                    petId = petId
                )
            }
        )
    }

    private fun ServiceRequestEntity.toRequest(petIds: List<Int>): ServiceRequestRequest {
        return ServiceRequestRequest(
            id = serviceRequestId,
            ownerId = ownerId,
            petId = petId,
            petIds = petIds,
            serviceTypeId = serviceTypeId,
            title = title,
            description = description,
            requestedDate = requestedDate,
            startTime = startTime,
            endTime = endTime,
            status = status.name,
            offeredServiceId = offeredServiceId,
            sourceType = sourceType.name,
            latitude = latitude,
            longitude = longitude
        )
    }

    private fun ServiceRequestDto.toEntity(): ServiceRequestEntity {
        return ServiceRequestEntity(
            serviceRequestId = id,
            ownerId = ownerId,
            petId = petId,
            serviceTypeId = serviceTypeId,
            title = title,
            description = description,
            requestedDate = requestedDate,
            startTime = startTime,
            endTime = endTime,
            status = status.toRequestStatus(),
            offeredServiceId = offeredServiceId,
            sourceType = sourceType.toRequestSource(),
            latitude = latitude,
            longitude = longitude
        )
    }

    private suspend fun List<ServiceRequestDto>.toDetailsList(): List<ServiceRequestDetails> {
        // Cache simple para no pedir las mascotas del mismo dueno muchas veces.
        val petNamesCache = mutableMapOf<Int, Map<Int, String>>()

        return map { request ->
            val petNamesById = petNamesCache[request.ownerId]
                ?: petNameMapForOwner(request.ownerId).also { petNamesCache[request.ownerId] = it }

            request.toDetails(petNamesById)
        }
    }

    private suspend fun petNameMapForOwner(ownerId: Int): Map<Int, String> {
        val response = runCatching {
            apiService.getPetsByOwner(ownerId)
        }.getOrNull()

        if (response?.isSuccessful != true) return emptyMap()

        return response.body().orEmpty()
            .associate { pet -> pet.id to pet.name }
    }

    private fun ServiceRequestDto.toDetails(petNamesById: Map<Int, String> = emptyMap()): ServiceRequestDetails {
        // La UI trabaja con nombres listos; aqui se traducen ids de mascotas a texto visible.
        val requestPetIds = petIds.ifEmpty { listOf(petId) }
        val primaryPetName = petNamesById[petId] ?: "Mascota #$petId"
        val allPetNames = requestPetIds.joinToString(", ") { id ->
            petNamesById[id] ?: "Mascota #$id"
        }

        return ServiceRequestDetails(
            serviceRequestId = id,
            ownerId = ownerId,
            petId = petId,
            serviceTypeId = serviceTypeId,
            title = title,
            description = description,
            requestedDate = requestedDate,
            startTime = startTime,
            endTime = endTime,
            status = status.toRequestStatus(),
            petName = primaryPetName,
            petNames = allPetNames,
            petBreed = null,
            petSize = null,
            serviceTypeName = title,
            ownerName = "DueÃ±o #$ownerId",
            ownerPhone = null,
            ownerEmail = null
        )
    }

    private fun String.toRequestStatus(): ServiceRequestStatus {
        return runCatching {
            ServiceRequestStatus.valueOf(uppercase())
        }.getOrDefault(ServiceRequestStatus.PENDING)
    }

    private fun String.toRequestSource(): RequestSource {
        return runCatching {
            RequestSource.valueOf(uppercase())
        }.getOrDefault(RequestSource.OPEN)
    }
}
