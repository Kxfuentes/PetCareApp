package com.proyectopoo.petcareapp.data.local.entity

/*
 * Comentario de modulo PetCare:
 * Modelo local de Room. Representa como se guarda esta informacion dentro de la base local de la app.
 */

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
    @PrimaryKey(autoGenerate = true)
    val notificationId: Int = 0,

    val userId: Int,

    val title: String,

    val message: String,

    val type: NotificationType,

    val isRead: Boolean = false,

    val createdAt: Long = System.currentTimeMillis()
)

enum class NotificationType {
    SERVICE_REQUEST,
    REQUEST_ACCEPTED,
    REQUEST_REJECTED,
    REQUEST_CANCELLED,
    GENERAL
}