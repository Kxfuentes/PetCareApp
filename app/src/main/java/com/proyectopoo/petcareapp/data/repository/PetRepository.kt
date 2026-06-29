package com.proyectopoo.petcareapp.data.repository

import com.proyectopoo.petcareapp.data.local.dao.PetDao
import com.proyectopoo.petcareapp.data.local.entity.PetEntity
import com.proyectopoo.petcareapp.data.network.ApiService
import com.proyectopoo.petcareapp.data.network.PetDto
import com.proyectopoo.petcareapp.data.network.PetRequest

class PetRepository(
    private val petDao: PetDao,
    private val apiService: ApiService? = null
) {

    suspend fun insertPet(pet: PetEntity) {
        val saved = runCatching {
            apiService?.createPet(pet.toRequest())
                ?.takeIf { it.isSuccessful }
                ?.body()
                ?.toEntity(fallbackId = pet.petId)
        }.getOrNull() ?: pet

        petDao.insertPet(saved)
    }

    suspend fun insertPets(pets: List<PetEntity>) {
        petDao.insertPets(pets)
    }

    suspend fun getAllPets(): List<PetEntity> {
        return petDao.getAllPets()
    }

    suspend fun getPetById(petId: Int): PetEntity? {
        return petDao.getPetById(petId)
    }

    suspend fun getPetsByOwner(ownerId: Int): List<PetEntity> {
        val remotePets = runCatching {
            apiService?.getPetsByOwner(ownerId)
                ?.takeIf { it.isSuccessful }
                ?.body()
                ?.map { it.toEntity() }
        }.getOrNull()

        if (remotePets != null) {
            remotePets.forEach { petDao.insertPet(it) }
            return remotePets
        }

        return petDao.getPetsByOwner(ownerId)
    }

    suspend fun updatePet(pet: PetEntity) {
        val saved = runCatching {
            apiService?.updatePet(pet.petId, pet.toRequest())
                ?.takeIf { it.isSuccessful }
                ?.body()
                ?.toEntity(fallbackId = pet.petId)
        }.getOrNull() ?: pet

        petDao.updatePet(saved)
    }

    suspend fun deletePet(pet: PetEntity) {
        runCatching { apiService?.deletePet(pet.petId) }
        petDao.deletePet(pet)
    }
}

private fun PetEntity.toRequest(): PetRequest {
    return PetRequest(
        ownerId = ownerId,
        name = name,
        species = species,
        breed = breed.orEmpty().ifBlank { "Sin raza" },
        size = size.orEmpty().ifBlank { "Mediano" }
    )
}

private fun PetDto.toEntity(fallbackId: Int? = null): PetEntity {
    return PetEntity(
        petId = id ?: fallbackId ?: 0,
        ownerId = ownerId,
        name = name,
        species = species ?: "Dog",
        breed = breed,
        size = size
    )
}
