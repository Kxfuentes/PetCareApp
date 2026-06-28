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
        return try {
            val cleanEmail = email.trim()
            val cleanPassword = password.trim()

            println("REPOSITORY LOGIN - Email: $cleanEmail")

            val response = apiService.login(
                LoginRequest(
                    email = cleanEmail,
                    password = cleanPassword
                )
            )

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                println("Login failed: ${response.code()} - $errorBody")
                return null
            }

            val body = response.body()
            if (body == null) {
                println("Response body is null")
                return null
            }

            val userDto = body.user
            if (userDto == null) {
                println("User is null in response")
                return null
            }

            val mappedRole = when (userDto.role?.uppercase()) {
                "OWNER", "DUENO", "DUEÑO", "PROPIETARIO" -> UserRoleType.OWNER
                "CAREGIVER", "CUIDADOR", "GESTOR" -> UserRoleType.CAREGIVER
                else -> UserRoleType.OWNER
            }

            val user = UserEntity(
                userId = userDto.id,
                fullName = userDto.username,
                email = userDto.email,
                phone = null,
                password = null,
                role = mappedRole
            )

            userDao.insertUser(user)

            sessionManager.saveSession(
                userId = user.userId,
                email = user.email,
                role = user.role
            )

            val token = body.token
            if (token != null) {
                sessionManager.saveToken(token, rememberSession)
            }

            return user
        } catch (e: Exception) {
            println("Exception in login: ${e.message}")
            e.printStackTrace()
            return null
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