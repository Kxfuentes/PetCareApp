package com.proyectopoo.petcareapp.data

/*
 * Comentario de modulo PetCare:
 * Archivo del proyecto PetCare. Mantiene una parte especifica de la app y debe conservarse simple de seguir.
 */

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType

object NominatimClient {
    // La URL base de Nominatim
    private const val BASE_URL = "https://nominatim.openstreetmap.org/"

    private val json = Json {
        ignoreUnknownKeys = true
    }

    // Cliente OkHttp con el User-Agent
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "PetCareApp/1.0 (ariavila@uamv.edu.ni)")
                .build()
            chain.proceed(request)
        }
        .build()

    val instance: NominatimService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)  // Añadimos el cliente con el User-Agent
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(NominatimService::class.java)
    }
}