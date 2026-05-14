package com.proyectopoo.petcareapp.Viewmodel

import android.content.SharedPreferences
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

    // Rol activo en la sesión
    private val _userRole = MutableStateFlow<UserRole?>(null)
    val userRole: StateFlow<UserRole?> = _userRole.asStateFlow()

    // Rol que el usuario tiene asignado en su registro (simulado)
    private val _registeredRole = MutableStateFlow<UserRole?>(null)
    val registeredRole: StateFlow<UserRole?> = _registeredRole.asStateFlow()

    private val _isRoleLoaded = MutableStateFlow(false)
    val isRoleLoaded: StateFlow<Boolean> = _isRoleLoaded.asStateFlow()

    init {
        // Al iniciar, forzamos que no haya rol activo para ir a Login
        _userRole.value = null
        _isRoleLoaded.value = true
    }

    fun setRegisteredRole(role: UserRole?) {
        _registeredRole.value = role
    }

    fun setRole(role: UserRole?) {
        viewModelScope.launch {
            _userRole.value = role
        }
    }

    fun clearRole() {
        viewModelScope.launch {
            _userRole.value = null
            _registeredRole.value = null
        }
    }
}
