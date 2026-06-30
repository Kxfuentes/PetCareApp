package com.proyectopoo.petcareapp.data.websocket

import android.util.Log
import com.proyectopoo.petcareapp.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class PetCareWebSocketClient(
    private val onEvent: (WsEvent) -> Unit,
    private val onMessage: (String) -> Unit = {},
    private val onStatusChanged: (Boolean) -> Unit = {}
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val client = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    fun connect(userId: Int) {
        if (userId <= 0) return
        if (webSocket != null) return

        val wsUrl = BuildConfig.BASE_URL
            .replace("http://", "ws://")
            .replace("https://", "wss://")
            .trimEnd('/') + "/ws/petcare?userId=$userId"

        val request = Request.Builder()
            .url(wsUrl)
            .build()

        webSocket = client.newWebSocket(
            request,
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d(TAG, "WebSocket conectado")
                    onStatusChanged(true)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    Log.d(TAG, "WebSocket mensaje: $text")
                    onMessage(text)

                    runCatching {
                        json.decodeFromString<WsEvent>(text)
                    }.onSuccess { event ->
                        onEvent(event)
                    }.onFailure { error ->
                        Log.e(TAG, "No se pudo parsear evento WebSocket", error)
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d(TAG, "WebSocket cerrando: $code $reason")
                    webSocket.close(code, reason)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d(TAG, "WebSocket cerrado: $code $reason")
                    this@PetCareWebSocketClient.webSocket = null
                    onStatusChanged(false)
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e(TAG, "WebSocket error", t)
                    this@PetCareWebSocketClient.webSocket = null
                    onStatusChanged(false)
                }
            }
        )
    }

    fun send(message: String) {
        webSocket?.send(message)
    }

    fun disconnect() {
        webSocket?.close(1000, "Cliente desconectado")
        webSocket = null
        onStatusChanged(false)
    }

    companion object {
        private const val TAG = "PetCareWebSocket"
    }
}