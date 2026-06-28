package com.proyectopoo.petcareapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectopoo.petcareapp.model.User
import com.proyectopoo.petcareapp.model.UserRole
import com.proyectopoo.petcareapp.data.NominatimClient
import com.proyectopoo.petcareapp.data.local.relation.ServiceRequestDetails
import com.proyectopoo.petcareapp.model.NominatimResponse
import com.proyectopoo.petcareapp.model.Cuidador
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModels
data class UserViewModel(
    val id: Int,
    val username: String,
    val email: String,
    val role: String? = null
)

data class SessionViewModel(
    val id: Int,
    val token: String
)

// Models
data class UserDataState(
    val userModel: User? = null,
    val userViewModel: UserViewModel? = null
)

data class AppStateData(
    val userState: UserDataState = UserDataState(),
    val session: SessionViewModel? = null,
    val currentRole: UserRole? = null
)

// Estado para la búsqueda de ubicaciones
data class LocationSearchState(
    val query: String = "",
    val results: List<NominatimResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedLocation: NominatimResponse? = null
)

data class CaregiverState(
    val caregivers: List<Cuidador> = emptyList(),
    val nearbyCaregivers: List<Cuidador> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalFound: Int = 0
)

class MainViewModel : ViewModel() {

    private val _appState = MutableStateFlow(AppStateData())
    val appState: StateFlow<AppStateData> = _appState.asStateFlow()

    private val _locationSearchState = MutableStateFlow(LocationSearchState())
    val locationSearchState: StateFlow<LocationSearchState> = _locationSearchState.asStateFlow()

    private val _caregiverState = MutableStateFlow(CaregiverState())
    val caregiverState: StateFlow<CaregiverState> = _caregiverState.asStateFlow()

    private val _caregivers = MutableStateFlow<List<Cuidador>>(emptyList())
    val caregivers: StateFlow<List<Cuidador>> = _caregivers.asStateFlow()

    // ========== FUNCIONES DE USUARIO ==========

    fun updateUser(user: User?) {
        _appState.update {
            it.copy(
                userState = it.userState.copy(
                    userModel = user,
                    userViewModel = user?.let {
                        UserViewModel(
                            id = it.id,
                            username = it.username,
                            email = it.email,
                            role = it.role
                        )
                    }
                )
            )
        }
    }

    fun updateSession(session: SessionViewModel?) {
        _appState.update { it.copy(session = session) }
    }

    fun updateRole(role: UserRole?) {
        _appState.update { it.copy(currentRole = role) }
    }

    fun clearSession() {
        _appState.update {
            it.copy(
                session = null,
                userState = UserDataState(),
                currentRole = null
            )
        }
    }

    // ========== FUNCIONES DE UBICACIÓN (Nominatim) ==========

    fun updateSearchQuery(query: String) {
        _locationSearchState.update { it.copy(query = query) }

        if (query.isBlank()) {
            clearSearchResults()
            return
        }

        searchLocation(query)
    }

    fun searchLocation(query: String) {
        if (query.isBlank()) {
            clearSearchResults()
            return
        }

        viewModelScope.launch {
            _locationSearchState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            try {
                val results = NominatimClient.instance.searchLocation(
                    query = query,
                    format = "json",
                    limit = 5
                )

                _locationSearchState.update {
                    it.copy(
                        results = results,
                        isLoading = false,
                        error = if (results.isEmpty()) "No se encontraron resultados" else null
                    )
                }
            } catch (e: Exception) {
                _locationSearchState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al buscar: ${e.message}"
                    )
                }
                e.printStackTrace()
            }
        }
    }

    fun selectLocation(location: NominatimResponse) {
        _locationSearchState.update {
            it.copy(
                selectedLocation = location,
                query = location.display_name
            )
        }
    }

    // 🔥 CORREGIDO: Limpia resultados y el query, pero mantiene selectedLocation
    fun clearSearchResults() {
        _locationSearchState.update {
            it.copy(
                results = emptyList(),
                error = null,
                isLoading = false,
                query = ""  // 🔥 Limpia también el query
                // 🔥 selectedLocation se mantiene
            )
        }
    }

    // 🔥 CORREGIDO: Limpia TODO incluyendo selectedLocation
    fun clearLocationData() {
        _locationSearchState.update {
            LocationSearchState() // Reset a estado inicial
        }
    }

    fun getSelectedCoordinates(): Pair<Double, Double>? {
        val location = _locationSearchState.value.selectedLocation ?: return null
        return try {
            Pair(location.lat.toDouble(), location.lon.toDouble())
        } catch (e: Exception) {
            null
        }
    }


    fun searchServicesNearby(
        lat: Double,
        lon: Double,
        radius: Double,
        onResult: (List<ServiceRequestDetails>) -> Unit
    ) {
        viewModelScope.launch {
            // Aquí iría la llamada real a tu API
            kotlinx.coroutines.delay(1000)
            onResult(emptyList())
        }
    }

    fun loadAllCaregivers() {
        viewModelScope.launch {
            _caregiverState.update { it.copy(isLoading = true, error = null) }
            try {
                // MOCK
                kotlinx.coroutines.delay(1000)
                val mockCaregivers = listOf(
                    Cuidador("1", "Juan Pérez", "Granada", "C$150/h", 4.8, 12, listOf("Paseo", "Alojamiento"), "Excelente servicio"),
                    Cuidador("2", "María García", "Managua", "C$200/h", 5.0, 8, listOf("Guardería", "Peluquería"), "Muy atenta con mis perros"),
                    Cuidador("3", "Carlos López", "León", "C$120/h", 4.5, 20, listOf("Taxi", "Paseo"), "Puntual y confiable")
                )
                _caregiverState.update { it.copy(caregivers = mockCaregivers, isLoading = false) }
                _caregivers.value = mockCaregivers
            } catch (e: Exception) {
                _caregiverState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun searchCaregiversNearby(
        lat: Double,
        lon: Double,
        radius: Double = 10.0
    ) {
        viewModelScope.launch {
            _caregiverState.update { it.copy(isLoading = true, error = null) }
            try {
                kotlinx.coroutines.delay(1000)
                val nearby = _caregiverState.value.caregivers.take(2)
                _caregiverState.update {
                    it.copy(
                        nearbyCaregivers = nearby,
                        totalFound = nearby.size,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _caregiverState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun searchCaregiversByService(serviceType: String) {
        viewModelScope.launch {
            _caregiverState.update { it.copy(isLoading = true) }
            kotlinx.coroutines.delay(500)
            val filtered = _caregiverState.value.caregivers.filter { it.servicios.contains(serviceType) }
            _caregiverState.update { it.copy(caregivers = filtered, isLoading = false) }
        }
    }

    fun clearCaregiverSearch() {
        _caregiverState.update {
            it.copy(nearbyCaregivers = emptyList(), totalFound = 0, error = null)
        }
    }

    fun loadCaregivers() {
        loadAllCaregivers()
    }
}