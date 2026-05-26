package com.proyectopoo.petcareapp.data.local.database

import androidx.room.TypeConverter
import com.proyectopoo.petcareapp.data.local.entity.UserRoleType
import com.proyectopoo.petcareapp.data.local.entity.BookingStatus
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestStatus
import com.proyectopoo.petcareapp.data.local.entity.NotificationType

class Converters {
    @TypeConverter
    fun fromUserRoleType(value: UserRoleType): String {
        return value.name
    }

    @TypeConverter
    fun toUserRoleType(value: String): UserRoleType {
        return UserRoleType.valueOf(value)
    }

    @TypeConverter
    fun fromBookingStatus(value: BookingStatus): String {
        return value.name
    }

    @TypeConverter
    fun toBookingStatus(value: String): BookingStatus {
        return BookingStatus.valueOf(value)
    }

    @TypeConverter
    fun fromServiceRequestStatus(value: ServiceRequestStatus): String {
        return value.name
    }

    @TypeConverter
    fun toServiceRequestStatus(value: String): ServiceRequestStatus {
        return ServiceRequestStatus.valueOf(value)
    }

    @TypeConverter
    fun fromNotificationType(value: NotificationType): String {
        return value.name
    }

    @TypeConverter
    fun toNotificationType(value: String): NotificationType {
        return NotificationType.valueOf(value)
    }
}
