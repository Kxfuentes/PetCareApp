package com.proyectopoo.petcareapp.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.proyectopoo.petcareapp.R
import com.proyectopoo.petcareapp.data.local.dao.NotificationDao
import com.proyectopoo.petcareapp.data.local.entity.NotificationEntity
import com.proyectopoo.petcareapp.data.local.entity.NotificationType

/**
 * Centraliza el envío de notificaciones locales (push en el dispositivo):
 *  1. Persiste la notificación en Room para que el usuario pueda consultarla luego.
 *  2. La muestra en la bandeja del sistema (heads-up) si el usuario otorgó el permiso.
 *
 * No usa servicios remotos (FCM); funciona sin backend ni Firebase.
 */
class AppNotifier(
    context: Context,
    private val notificationDao: NotificationDao
) {
    private val appContext = context.applicationContext

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notificaciones PetCare",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Solicitudes, aceptaciones y novedades de tus servicios"
            }
            appContext.getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    /** Persiste la notificación y la muestra en la bandeja del sistema. */
    suspend fun push(
        recipientUserId: Int,
        title: String,
        message: String,
        type: NotificationType
    ) {
        notificationDao.insertNotification(
            NotificationEntity(
                userId = recipientUserId,
                title = title,
                message = message,
                type = type
            )
        )
        showSystemNotification(title, message)
    }

    @SuppressLint("MissingPermission") // El permiso se verifica explícitamente abajo.
    private fun showSystemNotification(title: String, message: String) {
        if (!hasPostPermission()) {
            // Sin permiso del usuario: la notificación queda persistida pero no se muestra.
            return
        }

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val systemId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        NotificationManagerCompat.from(appContext).notify(systemId, notification)
    }

    private fun hasPostPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val CHANNEL_ID = "petcare_general"
    }
}
