package com.proyectopoo.petcareapp.data

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

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