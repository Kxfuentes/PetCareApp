package com.proyectopoo.petcareapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectopoo.petcareapp.data.local.database.PetCareDatabase
import com.proyectopoo.petcareapp.data.local.entity.ApplicationStatus
import com.proyectopoo.petcareapp.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CaregiverProfileViewModel(
    private val database: PetCareDatabase,
    private val caregiverId: Int
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _completedServicesCount = MutableStateFlow(0)
    val completedServicesCount: StateFlow<Int> = _completedServicesCount.asStateFlow()

    private val _rating = MutableStateFlow(0.0)
    val rating: StateFlow<Double> = _rating.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadCaregiverData()
    }

    private fun loadCaregiverData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val userEntity = database.userDao().getUserById(caregiverId)
                _user.value = userEntity?.let {
                    User(
                        username = it.fullName,
                        email = it.email,
                        role = it.role.name,
                        id = it.userId
                    )
                }


                val completed = database.serviceApplicationDao()
                    .countByCaregiverAndStatus(caregiverId, ApplicationStatus.COMPLETED)
                _completedServicesCount.value = completed


                val avgRating = database.ratingDao().getAverageRatingForCaregiver(caregiverId)
                _rating.value = avgRating ?: 0.0

            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error al cargar el perfil del cuidador"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        loadCaregiverData()
    }
}