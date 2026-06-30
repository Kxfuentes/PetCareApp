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
        apiUserId: String? = null,
        rememberSession: Boolean? = null
    ) {
        val editor = prefs.edit()
            .putInt("user_id", userId)
            .putString("email", email.trim())
            .putString("role", role.name)
            .putBoolean("is_logged_in", true)

        token?.let { editor.putString("token", it) }
        apiUserId?.let { editor.putString("api_user_id", it) }
        rememberSession?.let { editor.putBoolean("remember_session", it) }

        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    fun shouldRememberSession(): Boolean {
        return prefs.getBoolean("remember_session", false)
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

    fun getBackendUserId(): Int {
        return getApiUserId()?.toIntOrNull()?.takeIf { it > 0 } ?: getUserId()
    }

    fun saveToken(token: String, rememberSession: Boolean) {
        prefs.edit()
            .putString("auth_token", token)
            .putBoolean("remember_session", rememberSession)
            .apply()
    }

    fun clearToken() {
        prefs.edit()
            .remove("auth_token")
            .apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}