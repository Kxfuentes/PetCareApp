package com.proyectopoo.petcareapp.data.repository

/*
 * Comentario de modulo PetCare:
 * Repositorio de datos. Centraliza llamadas a Room y API para que la pantalla no maneje detalles tecnicos.
 */

import com.proyectopoo.petcareapp.data.local.dao.AvailabilityDao
import com.proyectopoo.petcareapp.data.local.entity.AvailabilityEntity

class AvailabilityRepository(
    private val availabilityDao: AvailabilityDao
) {

    suspend fun insertAvailability(
        availability: AvailabilityEntity
    ) {
        availabilityDao.insertAvailability(availability)
    }

    suspend fun getAllAvailability(): List<AvailabilityEntity> {
        return availabilityDao.getAllAvailability()
    }

    suspend fun getAvailabilityByCaregiver(
        caregiverId: Int
    ): AvailabilityEntity? {

        return availabilityDao
            .getAvailabilityByCaregiver(caregiverId)
    }

    suspend fun updateAvailability(
        availability: AvailabilityEntity
    ) {
        availabilityDao.updateAvailability(availability)
    }

    suspend fun deleteAvailability(
        availability: AvailabilityEntity
    ) {
        availabilityDao.deleteAvailability(availability)
    }
}