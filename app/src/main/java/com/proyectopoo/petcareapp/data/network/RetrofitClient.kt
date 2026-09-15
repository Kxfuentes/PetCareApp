package com.proyectopoo.petcareapp.data.network

import com.proyectopoo.petcareapp.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Construye una implementación de [ApiService] apuntando a [baseUrl], usando el mismo
 * [Json] y (opcionalmente) el mismo [OkHttpClient] que usa la app en producción.
 *
 * Extraído de [RetrofitClient] para que los tests puedan apuntar la misma configuración
 * de Retrofit/serialización a un servidor de prueba (p.ej. MockWebServer) sin depender de
 * [BuildConfig.BASE_URL]. [RetrofitClient.apiService] es simplemente el resultado de llamar
 * esta función con la BASE_URL real de la app; su comportamiento público no cambia.
 */
fun buildApiService(baseUrl: String, client: OkHttpClient = RetrofitClient.defaultHttpClient): ApiService {
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(RetrofitClient.json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(ApiService::class.java)
}

object RetrofitClient {
    private val BASE_URL = BuildConfig.BASE_URL

    internal val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    internal val defaultHttpClient = OkHttpClient.Builder()
        .addInterceptor(RetryInterceptor())
        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        buildApiService(BASE_URL, defaultHttpClient)
    }

    /**
     * El backend devuelve URLs de imagen como rutas root-relative (p.ej. "/api/chat/imagen/xyz.jpg").
     * Las prefija con BASE_URL (sin la barra final) para que Coil pueda cargarlas. Si [path] ya
     * es una URL absoluta o es nulo/vacío, se devuelve tal cual (o null).
     */
    fun resolveImageUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("http://") || path.startsWith("https://")) return path
        return BASE_URL.trimEnd('/') + path
    }
}
