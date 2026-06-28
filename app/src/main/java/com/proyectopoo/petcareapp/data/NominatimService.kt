package com.proyectopoo.petcareapp.data

import com.proyectopoo.petcareapp.model.NominatimResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimService {

    @GET("search")
    suspend fun searchLocation(
        @Query("q") query: String,          // La dirección que escribe el usuario
        @Query("format") format: String = "json", // Le decimos que queremos JSON
        @Query("limit") limit: Int = 5      // Máximo 5 resultados
    ): List<NominatimResponse>
}