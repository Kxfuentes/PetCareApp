package com.proyectopoo.petcareapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.proyectopoo.petcareapp.data.local.entity.NotificationEntity

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("SELECT * FROM notifications")
    suspend fun getAllNotifications(): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE userId = :userId")
    suspend fun getNotificationsByUser(userId: String): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE userId = :userId AND isRead = 0")
    suspend fun getUnreadNotificationsByUser(userId: String): List<NotificationEntity>

    @Query("UPDATE notifications SET isRead = 1 WHERE notificationId = :notificationId")
    suspend fun markAsRead(notificationId: Int)

    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()
}