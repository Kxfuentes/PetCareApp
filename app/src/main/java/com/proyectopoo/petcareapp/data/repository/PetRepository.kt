package com.proyectopoo.petcareapp.data.repository

import com.proyectopoo.petcareapp.data.local.dao.PetDao
import com.proyectopoo.petcareapp.data.local.entity.PetEntity

class PetRepository(
    private val petDao: PetDao
) {

    suspend fun insertPet(pet: PetEntity) {
        petDao.insertPet(pet)
    }

    suspend fun insertPets(
        pets: List<PetEntity>
    ) {
        petDao.insertPets(pets)
    }

    suspend fun getAllPets(): List<PetEntity> {
        return petDao.getAllPets()
    }

    suspend fun getPetById(
        petId: Int
    ): PetEntity? {

        return petDao.getPetById(petId)
    }

    suspend fun getPetsByOwner(
        ownerId: Int
    ): List<PetEntity> {

        return petDao.getPetsByOwner(ownerId)
    }

    suspend fun updatePet(
        pet: PetEntity
    ) {
        petDao.updatePet(pet)
    }

    suspend fun deletePet(
        pet: PetEntity
    ) {
        petDao.deletePet(pet)
    }
}