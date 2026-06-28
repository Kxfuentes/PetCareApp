package com.proyectopoo.petcareapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class NotificationEntity(
    @PrimaryKey
    val notificationId: Int,

    val userId: String,

    val title: String,

    val message: String,

    val type: NotificationType,

    val isRead: Boolean = false,

    val createdAt: String? = null
)

enum class NotificationType {
    SERVICE_REQUEST,
    REQUEST_ACCEPTED,
    REQUEST_REJECTED,
    REQUEST_CANCELLED,
    GENERAL
}