package com.proyectopoo.petcareapp.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.proyectopoo.petcareapp.data.local.database.PetCareDatabase
import com.proyectopoo.petcareapp.data.local.entity.NotificationType
import com.proyectopoo.petcareapp.data.network.FcmTokenRequest
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import com.proyectopoo.petcareapp.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Servicio de Firebase Cloud Messaging (FCM).
 *
 * IMPORTANTE: queda estructuralmente completo pero INACTIVO hasta que exista un proyecto
 * Firebase real. Sin `app/google-services.json` (ver comentario en app/build.gradle.kts),
 * FirebaseApp nunca se inicializa con credenciales reales, el sistema nunca registra este
 * dispositivo ante FCM y, por lo tanto, el sistema operativo jamas instancia esta clase.
 * El resto de la app (WebSocket + AppNotifier locales) sigue funcionando igual que antes.
 *
 * Reusa AppNotifier.push(...) para no duplicar la logica de persistencia/visualizacion de
 * notificaciones que ya usa el flujo por WebSocket.
 */
class PetCareFirebaseMessagingService : FirebaseMessagingService() {

    // FirebaseMessagingService no expone un CoroutineScope propio atado a su ciclo de vida
    // (no es un LifecycleService), asi que se crea uno simple con Dispatchers.IO para las
    // llamadas de red, con try/catch para no dejar excepciones sin capturar.
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    /** Se llama cuando FCM genera/renueva el token del dispositivo. Se envia al backend. */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token FCM generado")

        serviceScope.launch {
            try {
                val sessionManager = SessionManager(applicationContext)
                val userId = sessionManager.getBackendUserId()
                if (userId <= 0) {
                    // Sin sesion iniciada todavia: no hay a quien asociar el token.
                    // FCM volvera a entregar el token (o se puede reenviar tras el login).
                    Log.d(TAG, "Token FCM generado sin sesion activa; no se envia al backend")
                    return@launch
                }

                val response = RetrofitClient.apiService.sendFcmToken(
                    FcmTokenRequest(usuarioId = userId, token = token)
                )
                if (!response.isSuccessful) {
                    Log.e(TAG, "El backend rechazo el token FCM: HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error enviando token FCM al backend", e)
            }
        }
    }

    /** Se llama cuando llega un mensaje push mientras la app esta en foreground. */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Soporta tanto notificaciones "notification" (mostradas automaticamente por FCM
        // en background) como payloads puramente "data" (necesarios para foreground o para
        // incluir un tipo de notificacion propio de PetCare).
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "PetCare"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: message.data["message"]
            ?: "Tienes una nueva notificación."
        val type = notificationTypeFrom(message.data["type"])

        serviceScope.launch {
            try {
                val database = PetCareDatabase.getDatabase(applicationContext)
                val notifier = AppNotifier(applicationContext, database.notificationDao())
                val recipientUserId = SessionManager(applicationContext).getBackendUserId()
                    .takeIf { it > 0 } ?: 0

                // Reusa la logica existente de AppNotifier (persiste en Room + muestra la
                // notificacion del sistema) en lugar de duplicarla aqui.
                notifier.push(
                    recipientUserId = recipientUserId,
                    title = title,
                    message = body,
                    type = type
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error mostrando notificacion FCM", e)
            }
        }
    }

    private fun notificationTypeFrom(type: String?): NotificationType = when (type) {
        "SERVICE_REQUEST_CREATED", "APPLICATION_CREATED", "SERVICE_REQUEST" -> NotificationType.SERVICE_REQUEST
        "APPLICATION_STATUS_UPDATED", "REQUEST_ACCEPTED" -> NotificationType.REQUEST_ACCEPTED
        "REQUEST_REJECTED" -> NotificationType.REQUEST_REJECTED
        "REQUEST_CANCELLED" -> NotificationType.REQUEST_CANCELLED
        else -> NotificationType.GENERAL
    }

    companion object {
        private const val TAG = "PetCareFcmService"
    }
}
