package com.proyectopoo.petcareapp.viewmodel

import com.proyectopoo.petcareapp.model.User
import com.proyectopoo.petcareapp.model.UserRole

//ViewModel
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

//Models
data class UserDataState(
    val userModel: User? = null,
    val userViewModel: UserViewModel? = null
)

data class AppStateData(
    val userState: UserDataState = UserDataState(),
    val session: SessionViewModel? = null,
    val currentRole: UserRole? = null
)
