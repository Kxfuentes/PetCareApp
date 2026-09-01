package com.proyectopoo.petcareapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectopoo.petcareapp.data.local.database.PetCareDatabase
import com.proyectopoo.petcareapp.data.local.entity.ApplicationStatus
import com.proyectopoo.petcareapp.data.network.ApiService
import com.proyectopoo.petcareapp.data.network.RatingDto
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import com.proyectopoo.petcareapp.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CaregiverProfileViewModel(
    private val database: PetCareDatabase,
    private val caregiverId: Int,
    private val apiService: ApiService = RetrofitClient.apiService
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _completedServicesCount = MutableStateFlow(0)
    val completedServicesCount: StateFlow<Int> = _completedServicesCount.asStateFlow()

    private val _rating = MutableStateFlow(5.0)
    val rating: StateFlow<Double> = _rating.asStateFlow()

    private val _reviews = MutableStateFlow<List<RatingDto>>(emptyList())
    val reviews: StateFlow<List<RatingDto>> = _reviews.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadCaregiverData()
    }

    fun loadCaregiverData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val userEntity = database.userDao().getUserById(caregiverId)
                if (userEntity != null) {
                    _user.value = User(
                        id = userEntity.userId,
                        username = userEntity.fullName,
                        email = userEntity.email,
                        role = userEntity.role.name
                    )

                    val caregiverEntity = database.caregiverDao().getCaregiverById(caregiverId)
                    val remoteSummary = runCatching {
                        apiService.getCaregiverRatingSummary(caregiverId)
                    }.getOrNull()

                    val summaryBody = remoteSummary?.takeIf { it.isSuccessful }?.body()
                    if (summaryBody != null) {
                        _rating.value = summaryBody.average.takeIf { it > 0.0 } ?: 5.0
                        caregiverEntity?.let {
                            database.caregiverDao().updateCaregiver(
                                it.copy(rating = summaryBody.average.takeIf { it > 0.0 } ?: 5.0)
                            )
                        }
                    } else {
                        _rating.value = caregiverEntity?.rating?.takeIf { it > 0.0 } ?: 5.0
                    }

                    _reviews.value = runCatching {
                        apiService.getCaregiverReviews(caregiverId)
                    }.getOrNull()
                        ?.takeIf { it.isSuccessful }
                        ?.body()
                        .orEmpty()

                    val applications = database.serviceApplicationDao().getByCaregiver(caregiverId)
                    _completedServicesCount.value = applications.count { it.status == ApplicationStatus.COMPLETED }
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar datos del cuidador: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
