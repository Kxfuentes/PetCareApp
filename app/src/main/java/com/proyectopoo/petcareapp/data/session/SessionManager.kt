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
        role: UserRoleType
    ) {
        prefs.edit()
            .putInt("user_id", userId)
            .putString("email", email)
            .putString("role", role.name)
            .putBoolean("is_logged_in", true)
            .apply()
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
        return role?.let {
            UserRoleType.valueOf(it)
        }
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}