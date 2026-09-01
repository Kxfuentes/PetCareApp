package com.proyectopoo.petcareapp.data.network

import kotlinx.serialization.Serializable

@Serializable
data class DogBreedsResponse(
    val razas: List<String> = emptyList()
)
