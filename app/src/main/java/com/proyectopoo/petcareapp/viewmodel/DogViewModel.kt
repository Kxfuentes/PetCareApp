package com.proyectopoo.petcareapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectopoo.petcareapp.data.local.entity.PetEntity
import com.proyectopoo.petcareapp.data.repository.PetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DogViewModel(
    private val petRepository: PetRepository
) : ViewModel() {

    private val _dogs = MutableStateFlow<List<PetEntity>>(emptyList())
    val dogs: StateFlow<List<PetEntity>> = _dogs

    fun loadDogs(ownerId: Int) {
        viewModelScope.launch {
            val pets = petRepository.getPetsByOwner(ownerId)
            _dogs.value = pets
        }
    }

    fun addDog(dog: PetEntity) {
        viewModelScope.launch {
            saveDog(dog, isNewDog = true)
        }
    }

    fun updateDog(updated: PetEntity) {
        viewModelScope.launch {
            saveDog(updated, isNewDog = false)
        }
    }

    suspend fun saveDog(dog: PetEntity, isNewDog: Boolean) {
        if (isNewDog) {
            petRepository.insertPet(dog)
        } else {
            petRepository.updatePet(dog)
        }
        _dogs.value = petRepository.getPetsByOwner(dog.ownerId)
    }

    fun deleteDog(dog: PetEntity) {
        viewModelScope.launch {
            petRepository.deletePet(dog)
            loadDogs(dog.ownerId)
        }
    }
}
