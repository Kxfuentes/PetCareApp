package com.proyectopoo.petcareapp.data.repository

/*
 * Comentario de modulo PetCare:
 * Repositorio de datos. Centraliza llamadas a Room y API para que la pantalla no maneje detalles tecnicos.
 */

import com.proyectopoo.petcareapp.data.local.dao.OwnerDao
import com.proyectopoo.petcareapp.data.local.entity.OwnerEntity

class OwnerRepository(
    private val ownerDao: OwnerDao
) {

    suspend fun insertOwner(owner: OwnerEntity) {
        ownerDao.insertOwner(owner)
    }

    suspend fun getAllOwners(): List<OwnerEntity> {
        return ownerDao.getAllOwners()
    }

    suspend fun getOwnerById(ownerId: Int): OwnerEntity? {
        return ownerDao.getOwnerById(ownerId)
    }

    suspend fun getOwnerByUserId(userId: Int): OwnerEntity? {
        return ownerDao.getOwnerByUserId(userId)
    }

    suspend fun updateOwner(owner: OwnerEntity) {
        ownerDao.updateOwner(owner)
    }

    suspend fun deleteOwner(owner: OwnerEntity) {
        ownerDao.deleteOwner(owner)
    }
}