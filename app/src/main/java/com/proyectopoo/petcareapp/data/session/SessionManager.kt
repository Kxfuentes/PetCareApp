package com.proyectopoo.petcareapp.data.session

import android.content.Context
import com.proyectopoo.petcareapp.data.local.entity.UserRoleType

class SessionManager(
    context: Context
) {
    private val prefs = context.getSharedPreferences(
        "petcare_session",
        Context.MODE_PRIVATE
    )

    fun saveSession(
        userId: Int,
        email: String,
        role: UserRoleType,
        token: String? = null,
        apiUserId: String? = null
    ) {
        val editor = prefs.edit()
            .putInt("user_id", userId)
            .putString("email", email.trim())
            .putString("role", role.name)
            .putBoolean("is_logged_in", true)
        token?.let { editor.putString("token", it) }
        apiUserId?.let { editor.putString("api_user_id", it) }
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    fun getUserId(): Int {
        return prefs.getInt("user_id", -1)
    }

    fun getEmail(): String? {
        return prefs.getString("email", null)
    }

    fun getRole(): UserRoleType? {
        val role = prefs.getString("role", null)
        return role?.let { UserRoleType.valueOf(it) }
    }

    fun getToken(): String? {
        return prefs.getString("token", null)
    }

    fun getApiUserId(): String? {
        return prefs.getString("api_user_id", null)
    }

    fun saveToken(token: String, rememberSession: Boolean) {
        if (rememberSession) {
            prefs.edit().putString("auth_token", token).apply()
        }
    }

    fun clearToken() {
        prefs.edit().remove("auth_token").apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}