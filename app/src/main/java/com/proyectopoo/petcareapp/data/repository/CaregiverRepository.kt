package com.proyectopoo.petcareapp.data.repository

/*
 * Comentario de modulo PetCare:
 * Repositorio de datos. Centraliza llamadas a Room y API para que la pantalla no maneje detalles tecnicos.
 */

import com.proyectopoo.petcareapp.data.local.dao.CaregiverDao
import com.proyectopoo.petcareapp.data.local.entity.CaregiverEntity

class CaregiverRepository(
    private val caregiverDao: CaregiverDao
) {

    suspend fun insertCaregiver(
        caregiver: CaregiverEntity
    ) {
        caregiverDao.insertCaregiver(caregiver)
    }

    suspend fun getAllCaregivers(): List<CaregiverEntity> {
        return caregiverDao.getAllCaregivers()
    }

    suspend fun getCaregiverById(
        caregiverId: Int
    ): CaregiverEntity? {

        return caregiverDao.getCaregiverById(caregiverId)
    }

    suspend fun getCaregiverByUserId(
        userId: String
    ): CaregiverEntity? {

        return caregiverDao.getCaregiverByUserId(userId)
    }

    suspend fun updateCaregiver(
        caregiver: CaregiverEntity
    ) {
        caregiverDao.updateCaregiver(caregiver)
    }

    suspend fun deleteCaregiver(
        caregiver: CaregiverEntity
    ) {
        caregiverDao.deleteCaregiver(caregiver)
    }
}