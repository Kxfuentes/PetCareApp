package com.proyectopoo.petcareapp.viewmodel

/*
 * Comentario de modulo PetCare:
 * Estado de pantalla. Expone acciones y datos listos para que Compose los pueda mostrar.
 */

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectopoo.petcareapp.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserRoleViewModel(private val prefs: SharedPreferences) : ViewModel() {

    private val _userRole = MutableStateFlow<UserRole?>(null)
    val userRole: StateFlow<UserRole?> = _userRole.asStateFlow()

    private val _registeredRole = MutableStateFlow<UserRole?>(null)
    val registeredRole: StateFlow<UserRole?> = _registeredRole.asStateFlow()

    private val _isRoleLoaded = MutableStateFlow(false)
    val isRoleLoaded: StateFlow<Boolean> = _isRoleLoaded.asStateFlow()

    init {
        loadRole()
    }

    private fun loadRole() {
        val savedRole = prefs.getString("user_role", null)
        _userRole.value = savedRole?.let {
            UserRole.fromBackendValue(it)  // ← Usa fromBackendValue
        }
        _isRoleLoaded.value = true
    }

    fun setRole(role: UserRole?) {
        viewModelScope.launch {
            _userRole.value = role
            prefs.edit().putString("user_role", role?.backendValue).apply()  // ← Guarda backendValue
        }
    }

    fun setRegisteredRole(role: UserRole?) {
        _registeredRole.value = role
    }

    fun clearRole() {
        viewModelScope.launch {
            _userRole.value = null
            _registeredRole.value = null
            prefs.edit().remove("user_role").apply()
        }
    }
}