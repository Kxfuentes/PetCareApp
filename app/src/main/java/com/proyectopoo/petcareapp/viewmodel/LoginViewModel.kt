package com.proyectopoo.petcareapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectopoo.petcareapp.data.local.entity.UserEntity
import com.proyectopoo.petcareapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _loggedUser = MutableStateFlow<UserEntity?>(null)
    val loggedUser: StateFlow<UserEntity?> = _loggedUser.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun login(
        email: String,
        password: String,
        rememberSession: Boolean
    ) {

        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Complete todos los campos"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Correo inválido"
            return
        }

        if (password.length < 6) {
            _errorMessage.value =
                "La contraseña debe tener al menos 6 caracteres"
            return
        }

        viewModelScope.launch {

            _isLoading.value = true

            try {

                val user = userRepository.login(
                    email = email,
                    password = password,
                    rememberSession = rememberSession
                )

                if (user != null) {
                    _loggedUser.value = user
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Correo o contraseña incorrectos"
                }

            } catch (e: Exception) {

                _errorMessage.value = e.message

            } finally {

                _isLoading.value = false
            }
        }
    }

    fun logout() {
        userRepository.logout()
        _loggedUser.value = null
    }

    fun isLoggedIn(): Boolean {
        return userRepository.isLoggedIn()
    }
}