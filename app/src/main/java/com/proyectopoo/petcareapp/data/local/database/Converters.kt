package com.proyectopoo.petcareapp.data.local.database

import androidx.room.TypeConverter
import com.proyectopoo.petcareapp.data.local.entity.*

class Converters {

    @TypeConverter
    fun fromUserRoleType(value: UserRoleType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toUserRoleType(value: String?): UserRoleType? {
        return value?.let { UserRoleType.valueOf(it) }
    }

    @TypeConverter
    fun fromBookingStatus(value: BookingStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toBookingStatus(value: String?): BookingStatus? {
        return value?.let { BookingStatus.valueOf(it) }
    }

    @TypeConverter
    fun fromServiceRequestStatus(value: ServiceRequestStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toServiceRequestStatus(value: String?): ServiceRequestStatus? {
        return value?.let { ServiceRequestStatus.valueOf(it) }
    }

    @TypeConverter
    fun fromApplicationStatus(value: ApplicationStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toApplicationStatus(value: String?): ApplicationStatus? {
        return value?.let { ApplicationStatus.valueOf(it) }
    }

    @TypeConverter
    fun fromApplicationInitiator(value: ApplicationInitiator?): String? {
        return value?.name
    }

    @TypeConverter
    fun toApplicationInitiator(value: String?): ApplicationInitiator? {
        return value?.let { ApplicationInitiator.valueOf(it) }
    }

    @TypeConverter
    fun fromNotificationType(value: NotificationType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toNotificationType(value: String?): NotificationType? {
        return value?.let { NotificationType.valueOf(it) }
    }
}
