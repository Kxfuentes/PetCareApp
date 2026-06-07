package com.proyectopoo.petcareapp.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.proyectopoo.petcareapp.data.local.entity.PetEntity

class DogViewModel : ViewModel() {

    private val _dogs = MutableStateFlow<List<PetEntity>>(emptyList())
    val dogs: StateFlow<List<PetEntity>> = _dogs

    fun addDog(dog: PetEntity) {
        _dogs.value = _dogs.value + dog
    }

    fun updateDog(updated: PetEntity) {
        if (_dogs.value.none { it.petId == updated.petId }) return

        _dogs.value = _dogs.value.map {
            if (it.petId == updated.petId) updated else it
        }
    }

    fun deleteDog(dog: PetEntity) {
        _dogs.value = _dogs.value.filterNot { it.petId == dog.petId }
    }
}
