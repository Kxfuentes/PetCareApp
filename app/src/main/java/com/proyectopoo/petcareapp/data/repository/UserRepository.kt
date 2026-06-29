package com.proyectopoo.petcareapp.data.repository

import com.proyectopoo.petcareapp.data.local.dao.UserDao
import com.proyectopoo.petcareapp.data.local.entity.UserEntity
import com.proyectopoo.petcareapp.data.local.entity.UserRoleType
import com.proyectopoo.petcareapp.data.network.ApiService
import com.proyectopoo.petcareapp.data.network.LoginRequest
import com.proyectopoo.petcareapp.data.session.SessionManager
import com.proyectopoo.petcareapp.data.session.resolveStableUserId
import com.proyectopoo.petcareapp.data.session.upsertLocalUser

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
        return try {
            val cleanEmail = email.trim()
            val cleanPassword = password.trim()

            val response = apiService.login(
                LoginRequest(
                    email = cleanEmail,
                    password = cleanPassword
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

            // 🔥 CORREGIDO: convertir userDto.id a String
            val stableUserId = resolveStableUserId(
                userDao = userDao,
                email = userDto.email,
                apiUserId = userDto.id.toString()
            )

            val user = upsertLocalUser(
                userDao = userDao,
                userId = stableUserId,
                fullName = userDto.username,
                email = userDto.email,
                role = mappedRole
            )

            val token = body.token ?: body.session?.tokenSesion ?: body.session?.token
            sessionManager.saveSession(
                userId = user.userId,
                email = user.email,
                role = user.role,
                token = token,
                apiUserId = userDto.id.toString()
            )

            if (token != null) {
                sessionManager.saveToken(token, rememberSession)
            }

            user
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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
        sessionManager.clearToken()
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: UserEntity) {
        userDao.deleteUser(user)
    }
}