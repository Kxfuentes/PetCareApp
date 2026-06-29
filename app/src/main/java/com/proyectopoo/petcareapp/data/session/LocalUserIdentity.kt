package com.proyectopoo.petcareapp.data.session

import com.proyectopoo.petcareapp.data.local.dao.UserDao
import com.proyectopoo.petcareapp.data.local.entity.UserEntity
import com.proyectopoo.petcareapp.data.local.entity.UserRoleType

fun String.toStableLocalUserId(): Int {
    val numericId = this.toIntOrNull()
    if (numericId != null && numericId > 0) return numericId

    val generatedId = this.hashCode() and Int.MAX_VALUE
    return if (generatedId > 0) generatedId else 1
}

suspend fun resolveStableUserId(
    userDao: UserDao,
    email: String,
    apiUserId: String
): Int {
    val normalizedEmail = email.trim().lowercase()
    val existing = userDao.getUserByEmail(normalizedEmail)
        ?: userDao.getAllUsers().firstOrNull { it.email.trim().equals(normalizedEmail, ignoreCase = true) }

    return existing?.userId ?: apiUserId.toStableLocalUserId()
}

suspend fun upsertLocalUser(
    userDao: UserDao,
    userId: Int,
    fullName: String,
    email: String,
    role: UserRoleType
): UserEntity {
    val normalizedEmail = email.trim().lowercase()
    val user = UserEntity(
        userId = userId,
        fullName = fullName.ifBlank { normalizedEmail.substringBefore("@") },
        email = normalizedEmail,
        phone = null,
        password = null,
        role = role
    )
    userDao.insertUser(user)
    return user
}