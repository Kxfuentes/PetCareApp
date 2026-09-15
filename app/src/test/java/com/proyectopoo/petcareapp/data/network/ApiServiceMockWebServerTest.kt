package com.proyectopoo.petcareapp.data.network

import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Cubre la capa de red de [ApiService] contra un [MockWebServer] real, en vez de contra el
 * backend de verdad. Verifica que Retrofit/OkHttp exponen éxito y errores HTTP tal como los
 * espera el resto de la app (p.ej. `ChatScreen.kt`, que hace
 * `runCatching { RetrofitClient.apiService.xxx() }.getOrNull()` y luego revisa
 * `response?.isSuccessful` / `response.code()`), y que un timeout de red se manifiesta como la
 * [java.io.IOException] que [RetryInterceptor] sabe reintentar en GETs.
 *
 * No hay infraestructura de test de Compose/Espresso en este módulo, así que estos tests no
 * verifican el Snackbar en sí — verifican la señal de red (código HTTP / excepción) que hace
 * que la UI ya existente decida mostrar ese Snackbar o reintentar.
 */
class ApiServiceMockWebServerTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    /** Cliente sin RetryInterceptor y con timeouts generosos, para los casos de éxito/error HTTP. */
    private fun plainApiService(): ApiService =
        buildApiService(server.url("/").toString(), OkHttpClient.Builder().build())

    /** Cliente con el mismo RetryInterceptor de producción, para el caso de timeout. */
    private fun apiServiceWithRetry(timeoutMs: Long): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(RetryInterceptor(maxRetries = 1, initialBackoffMs = 10L))
            .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .build()
        return buildApiService(server.url("/").toString(), client)
    }

    // ---- getChatMessages: éxito 200 ----

    @Test
    fun `getChatMessages returns parsed body on 200`() = runBlocking {
        val json = """
            [
                {
                    "id": 1,
                    "service_request_id": 42,
                    "sender_id": 7,
                    "receiver_id": 9,
                    "message": "Hola, ¿cómo va todo?",
                    "is_read": false,
                    "created_at": "2026-09-14 10:00:00",
                    "image_url": null
                }
            ]
        """.trimIndent()
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(json)
                .setHeader("Content-Type", "application/json")
        )

        val response = plainApiService().getChatMessages(42)

        assertTrue(response.isSuccessful)
        assertEquals(200, response.code())
        val body = response.body()
        assertEquals(1, body?.size)
        assertEquals("Hola, ¿cómo va todo?", body?.first()?.message)
        assertEquals(42, body?.first()?.serviceRequestId)

        val recorded = server.takeRequest()
        assertEquals("GET", recorded.method)
        assertTrue(recorded.path?.contains("api/chat/mensajes/42") == true)
    }

    // ---- sendChatMessage: éxito 200 ----

    @Test
    fun `sendChatMessage returns parsed body on 200`() = runBlocking {
        val json = """
            {
                "id": 5,
                "service_request_id": 42,
                "sender_id": 7,
                "receiver_id": 9,
                "message": "Ya casi llego",
                "is_read": false,
                "created_at": "2026-09-14 10:05:00",
                "image_url": null
            }
        """.trimIndent()
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(json)
                .setHeader("Content-Type", "application/json")
        )

        val response = plainApiService().sendChatMessage(
            ChatMessageRequest(serviceRequestId = 42, senderId = 7, receiverId = 9, message = "Ya casi llego")
        )

        assertTrue(response.isSuccessful)
        assertEquals(5, response.body()?.id)

        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertTrue(recorded.body.readUtf8().contains("Ya casi llego"))
    }

    // ---- Error 400 ----

    @Test
    fun `getChatMessages surfaces 400 as unsuccessful response, not an exception`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody("""{"error":"bad request"}""")
        )

        val response = plainApiService().getChatMessages(42)

        assertFalse(response.isSuccessful)
        assertEquals(400, response.code())
    }

    // ---- Error 500 ----

    @Test
    fun `getChatMessages surfaces 500 as unsuccessful response, not an exception`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("""{"error":"internal error"}""")
        )

        val response = plainApiService().getChatMessages(42)

        assertFalse(response.isSuccessful)
        assertEquals(500, response.code())
    }

    // ---- Timeout: se propaga como IOException (lo que runCatching{}.getOrNull() maneja) ----

    @Test
    fun `getChatMessages times out and RetryInterceptor retries the GET before giving up`() {
        // NO_RESPONSE no responde nunca; con maxRetries = 1 se agotan intento inicial + 1
        // reintento, así que debe haber 2 requests grabadas en el servidor antes de fallar.
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

        val apiService = apiServiceWithRetry(timeoutMs = 500)

        var thrown: Throwable? = null
        try {
            runBlocking { apiService.getChatMessages(42) }
            fail("Se esperaba que la petición lanzara una excepción de timeout")
        } catch (t: Throwable) {
            thrown = t
        }

        assertTrue(
            "Se esperaba SocketTimeoutException (subclase de IOException), fue: $thrown",
            thrown is SocketTimeoutException
        )

        // El RetryInterceptor reintenta GET una vez antes de rendirse: 2 requests.
        assertEquals(2, server.requestCount)
    }
}
