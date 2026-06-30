package com.proyectopoo.petcareapp.data.repository

/*
 * Comentario de modulo PetCare:
 * Repositorio de datos. Centraliza llamadas a Room y API para que la pantalla no maneje detalles tecnicos.
 */

import com.proyectopoo.petcareapp.data.local.dao.NotificationDao
import com.proyectopoo.petcareapp.data.local.entity.NotificationEntity

class NotificationRepository(
    private val notificationDao: NotificationDao
) {

    suspend fun insertNotification(
        notification: NotificationEntity
    ) {
        notificationDao.insertNotification(notification)
    }

    suspend fun insertNotifications(
        notifications: List<NotificationEntity>
    ) {
        notificationDao.insertNotifications(notifications)
    }

    suspend fun getAllNotifications(): List<NotificationEntity> {
        return notificationDao.getAllNotifications()
    }

    suspend fun getNotificationsByUser(
        userId: Int
    ): List<NotificationEntity> {

        return notificationDao.getNotificationsByUser(userId)
    }

    suspend fun getUnreadNotificationsByUser(
        userId: Int
    ): List<NotificationEntity> {

        return notificationDao
            .getUnreadNotificationsByUser(userId)
    }

    suspend fun getUnreadCount(userId: Int): Int {
        return notificationDao.getUnreadCount(userId)
    }

    suspend fun markAsRead(
        notificationId: Int
    ) {
        notificationDao.markAsRead(notificationId)
    }

    suspend fun updateNotification(
        notification: NotificationEntity
    ) {
        notificationDao.updateNotification(notification)
    }

    suspend fun deleteNotification(
        notification: NotificationEntity
    ) {
        notificationDao.deleteNotification(notification)
    }
}