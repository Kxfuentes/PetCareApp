package com.proyectopoo.petcareapp.data.repository

import com.proyectopoo.petcareapp.data.local.dao.UserDao
import com.proyectopoo.petcareapp.data.local.entity.UserEntity
import com.proyectopoo.petcareapp.data.local.entity.UserRoleType
import com.proyectopoo.petcareapp.data.network.ApiService
import com.proyectopoo.petcareapp.data.network.LoginRequest
import com.proyectopoo.petcareapp.data.session.SessionManager

class UserRepository(
    private val userDao: UserDao,
    private val sessionManager: SessionManager,
    private val apiService: ApiService
) {

    suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    suspend fun getAllUsers(): List<UserEntity> {
        return userDao.getAllUsers()
    }

    suspend fun getUserById(userId: Int): UserEntity? {
        return userDao.getUserById(userId)
    }

    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email)
    }

    suspend fun login(
        email: String,
        password: String,
        rememberSession: Boolean
    ): UserEntity? {
        val response = apiService.login(
            LoginRequest(
                email = email,
                password = password
            )
        )

        if (!response.isSuccessful) {
            return null
        }

        val body = response.body() ?: return null
        
        val userDto = body.user ?: body.useer ?: return null

        val mappedRole = when (userDto.role?.uppercase()) {
            "OWNER", "DUENO", "DUEÑO", "PROPIETARIO" -> UserRoleType.OWNER
            "CAREGIVER", "CUIDADOR", "GESTOR" -> UserRoleType.CAREGIVER
            else -> UserRoleType.OWNER
        }

        val user = UserEntity(
            userId = userDto.id.toLocalUserId(),
            fullName = userDto.username,
            email = userDto.email,
            phone = null,
            password = null,
            role = mappedRole
        )


        userDao.insertUser(user)

        if (rememberSession) {
            sessionManager.saveSession(
                userId = user.userId,
                email = user.email,
                role = user.role
            )
        }

        return user
    }

    fun isLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }

    fun getCurrentUserId(): Int {
        return sessionManager.getUserId()
    }

    fun getCurrentUserEmail(): String? {
        return sessionManager.getEmail()
    }

    fun getCurrentUserRole() = sessionManager.getRole()

    fun logout() {
        sessionManager.clearSession()
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: UserEntity) {
        userDao.deleteUser(user)
    }
}
private fun String.toLocalUserId(): Int {
    val numericId = this.toIntOrNull()
    if (numericId != null && numericId > 0) return numericId

    val generatedId = this.hashCode() and Int.MAX_VALUE
    return if (generatedId > 0) generatedId else 1
}
