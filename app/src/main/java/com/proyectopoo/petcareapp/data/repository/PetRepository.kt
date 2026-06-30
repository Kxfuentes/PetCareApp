package com.proyectopoo.petcareapp.data.repository

import com.proyectopoo.petcareapp.data.local.dao.PetDao
import com.proyectopoo.petcareapp.data.local.entity.PetEntity
import com.proyectopoo.petcareapp.data.network.ApiService
import com.proyectopoo.petcareapp.data.network.PetDto
import com.proyectopoo.petcareapp.data.network.PetRequest
import com.proyectopoo.petcareapp.data.network.RetrofitClient

class PetRepository(
    private val petDao: PetDao,
    private val apiService: ApiService = RetrofitClient.apiService
) {

    suspend fun insertPet(pet: PetEntity) {
        val remotePet = runCatching {
            apiService.createPet(pet.toRequest())
        }.getOrNull()

        if (remotePet?.isSuccessful == true) {
            val savedPet = remotePet.body()?.toEntity()
            if (savedPet != null) {
                petDao.insertPet(savedPet)
                return
            }
        }

        petDao.insertPet(pet)
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
            apiService.getPetsByOwner(ownerId)
        }.getOrNull()

        if (remotePets?.isSuccessful == true) {
            val pets = remotePets.body()
                .orEmpty()
                .map { it.toEntity() }

            if (pets.isNotEmpty()) {
                petDao.insertPets(pets)
            }

            return pets
        }

        return petDao.getPetsByOwner(ownerId)
    }

    suspend fun updatePet(pet: PetEntity) {
        val remotePet = runCatching {
            apiService.updatePet(pet.petId, pet.toRequest())
        }.getOrNull()

        if (remotePet?.isSuccessful == true) {
            val savedPet = remotePet.body()?.toEntity()
            if (savedPet != null) {
                petDao.updatePet(savedPet)
                return
            }
        }

        petDao.updatePet(pet)
    }

    suspend fun deletePet(pet: PetEntity) {
        runCatching {
            apiService.deletePet(pet.petId)
        }

        petDao.deletePet(pet)
    }

    private fun PetEntity.toRequest(): PetRequest {
        return PetRequest(
            ownerId = ownerId,
            name = name,
            species = species.ifBlank { "Dog" },
            breed = breed.orEmpty().ifBlank { "Sin raza" },
            size = size.orEmpty().ifBlank { "Mediano" }
        )
    }

    private fun PetDto.toEntity(): PetEntity {
        return PetEntity(
            petId = id,
            ownerId = ownerId,
            name = name,
            species = species ?: "Dog",
            breed = breed,
            size = size
        )
    }
}