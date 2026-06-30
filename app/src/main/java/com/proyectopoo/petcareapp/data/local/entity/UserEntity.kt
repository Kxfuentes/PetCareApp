package com.proyectopoo.petcareapp.data.local.entity

/*
 * Comentario de modulo PetCare:
 * Modelo local de Room. Representa como se guarda esta informacion dentro de la base local de la app.
 */

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(

    @PrimaryKey
    val userId: Int,

    val fullName: String,

    val email: String,

    val phone: String? = null,

    val password: String? = null,

    val role: UserRoleType
)

enum class UserRoleType {
    OWNER,
    CAREGIVER
}
