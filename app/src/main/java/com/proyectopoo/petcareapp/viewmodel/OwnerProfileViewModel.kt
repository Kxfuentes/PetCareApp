package com.proyectopoo.petcareapp.viewmodel

/*
 * Comentario de modulo PetCare:
 * Estado de pantalla. Expone acciones y datos listos para que Compose los pueda mostrar.
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectopoo.petcareapp.data.local.dao.PetDao
import com.proyectopoo.petcareapp.data.local.dao.UserDao
import com.proyectopoo.petcareapp.data.local.entity.PetEntity
import com.proyectopoo.petcareapp.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OwnerProfileViewModel(
    private val userDao: UserDao,
    private val petDao: PetDao,
    private val ownerId: Int
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _dogs = MutableStateFlow<List<PetEntity>>(emptyList())
    val dogs = _dogs.asStateFlow()

    init {
        loadUser()
        loadDogs()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val userEntity = userDao.getUserById(ownerId)
            _user.value = userEntity?.let {
                User(
                    id = it.userId,
                    username = it.fullName,
                    email = it.email,
                    role = it.role.name,
                )
            }
        }
    }

    private fun loadDogs() {
        viewModelScope.launch {
            _dogs.value = petDao.getPetsByOwner(ownerId)
        }
    }

    fun addDog(pet: PetEntity) {
        viewModelScope.launch {
            petDao.insertPet(pet)
            loadDogs()
        }
    }

    fun updateDog(pet: PetEntity) {
        viewModelScope.launch {
            petDao.updatePet(pet)
            loadDogs()
        }
    }

    fun refreshData() {
        loadUser()
        loadDogs()
    }
}