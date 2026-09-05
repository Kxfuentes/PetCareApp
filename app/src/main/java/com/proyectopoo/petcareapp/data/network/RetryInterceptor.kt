package com.proyectopoo.petcareapp.data.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Reintenta automáticamente peticiones que fallan por errores de conexión
 * (timeout, sin red, host inalcanzable, etc.) usando backoff exponencial
 * (500ms, 1000ms, 2000ms para los reintentos 1, 2 y 3).
 *
 * Solo se reintentan fallos de red (IOException lanzada por OkHttp antes de
 * recibir una respuesta). Las respuestas HTTP con código de error (4xx/5xx)
 * NO se reintentan aquí: se devuelven tal cual para que la capa de arriba
 * (repositorios/ViewModels) las maneje, ya que reintentar un error de
 * servidor no suele arreglar nada y podría enmascarar el problema real.
 *
 * Para evitar duplicar efectos secundarios (crear un registro dos veces,
 * enviar una calificación repetida, etc.) solo se reintentan solicitudes
 * GET, que son idempotentes por definición. POST/PUT/PATCH/DELETE se dejan
 * pasar sin reintento.
 */
class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val initialBackoffMs: Long = 500L
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Solo reintentamos métodos idempotentes (GET) para no duplicar
        // efectos secundarios en operaciones de escritura.
        if (!request.method.equals("GET", ignoreCase = true)) {
            return chain.proceed(request)
        }

        var attempt = 0
        var lastError: IOException? = null

        while (attempt <= maxRetries) {
            try {
                return chain.proceed(request)
            } catch (error: IOException) {
                lastError = error
                if (attempt == maxRetries) {
                    throw error
                }

                // Backoff exponencial: 500ms, 1000ms, 2000ms
                val backoffMs = initialBackoffMs * (1L shl attempt)
                try {
                    Thread.sleep(backoffMs)
                } catch (interrupted: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw error
                }
                attempt++
            }
        }

        // Inalcanzable en la práctica: el bucle siempre retorna o lanza.
        throw lastError ?: IOException("RetryInterceptor: fallo desconocido")
    }
}
