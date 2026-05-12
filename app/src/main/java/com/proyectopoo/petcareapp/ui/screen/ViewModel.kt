package com.proyectopoo.petcareapp.ui.screen

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class UserRole {
    OWNER,
    CAREGIVER
}

class UserRoleViewModel(private val prefs: SharedPreferences) : ViewModel() {

    private val _userRole = MutableStateFlow<UserRole?>(null)
    val userRole: StateFlow<UserRole?> = _userRole.asStateFlow()

    private val _isRoleLoaded = MutableStateFlow(false)
    val isRoleLoaded: StateFlow<Boolean> = _isRoleLoaded.asStateFlow()

    init {
        loadRoleFromPreferences()
    }

    private fun loadRoleFromPreferences() {
        viewModelScope.launch {
            val roleName = prefs.getString("user_role", null)
            val role = when (roleName) {
                "OWNER" -> UserRole.OWNER
                "CAREGIVER" -> UserRole.CAREGIVER
                else -> null
            }
            _userRole.value = role
            _isRoleLoaded.value = true
        }
    }

    fun setRole(role: UserRole) {
        viewModelScope.launch {
            prefs.edit { putString("user_role", role.name) }
            _userRole.value = role
        }
    }

    fun clearRole() {
        viewModelScope.launch {
            prefs.edit { remove("user_role") }
            _userRole.value = null
        }
    }
}
