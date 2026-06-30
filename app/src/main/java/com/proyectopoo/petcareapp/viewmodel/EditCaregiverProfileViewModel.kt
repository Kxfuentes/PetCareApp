package com.proyectopoo.petcareapp.viewmodel

/*
 * Comentario de modulo PetCare:
 * Estado de pantalla. Expone acciones y datos listos para que Compose los pueda mostrar.
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectopoo.petcareapp.data.local.database.PetCareDatabase
import com.proyectopoo.petcareapp.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditCaregiverProfileViewModel(
    private val database: PetCareDatabase,
    private val caregiverId: Int
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val userEntity = database.userDao().getUserById(caregiverId)
                _user.value = userEntity?.let {
                    User(
                        id = it.userId,
                        username = it.fullName,
                        email = it.email,
                        role = it.role.name
                    )
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error al cargar el cuidador"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUser(fullName: String, email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            try {
                val current = database.userDao().getUserById(caregiverId)
                current?.let {
                    val updated = it.copy(fullName = fullName, email = email)
                    database.userDao().updateUser(updated)
                    _user.value = User(
                        id = updated.userId,
                        username = updated.fullName,
                        email = updated.email,
                        role = updated.role.name
                    )
                    onSuccess()
                } ?: run {
                    onError("Usuario no encontrado")
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error al actualizar"
                onError(_error.value ?: "Error desconocido")
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun refresh() {
        loadUser()
    }
}