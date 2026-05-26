package com.proyectopoo.petcareapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.proyectopoo.petcareapp.data.local.entity.AvailabilityEntity
import com.proyectopoo.petcareapp.data.local.entity.CaregiverEntity
import com.proyectopoo.petcareapp.data.local.entity.NotificationEntity
import com.proyectopoo.petcareapp.data.local.entity.OfferedServiceEntity
import com.proyectopoo.petcareapp.data.local.entity.OwnerEntity
import com.proyectopoo.petcareapp.data.local.entity.PetEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceBookingEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceTypeEntity
import com.proyectopoo.petcareapp.data.local.entity.UserEntity
import com.proyectopoo.petcareapp.data.local.dao.AvailabilityDao
import com.proyectopoo.petcareapp.data.local.dao.CaregiverDao
import com.proyectopoo.petcareapp.data.local.dao.NotificationDao
import com.proyectopoo.petcareapp.data.local.dao.OfferedServiceDao
import com.proyectopoo.petcareapp.data.local.dao.OwnerDao
import com.proyectopoo.petcareapp.data.local.dao.PetDao
import com.proyectopoo.petcareapp.data.local.dao.ServiceBookingDao
import com.proyectopoo.petcareapp.data.local.dao.ServiceRequestDao
import com.proyectopoo.petcareapp.data.local.dao.ServiceTypeDao
import com.proyectopoo.petcareapp.data.local.dao.UserDao

@Database(
    entities = [
        UserEntity::class,
        OwnerEntity::class,
        CaregiverEntity::class,
        PetEntity::class,
        ServiceTypeEntity::class,
        OfferedServiceEntity::class,
        ServiceRequestEntity::class,
        ServiceBookingEntity::class,
        NotificationEntity::class,
        AvailabilityEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class PetCareDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun ownerDao(): OwnerDao
    abstract fun caregiverDao(): CaregiverDao
    abstract fun petDao(): PetDao
    abstract fun serviceTypeDao(): ServiceTypeDao
    abstract fun offeredServiceDao(): OfferedServiceDao
    abstract fun serviceRequestDao(): ServiceRequestDao
    abstract fun serviceBookingDao(): ServiceBookingDao
    abstract fun notificationDao(): NotificationDao
    abstract fun availabilityDao(): AvailabilityDao

    companion object {

        @Volatile
        private var INSTANCE: PetCareDatabase? = null

        fun getDatabase(context: Context): PetCareDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PetCareDatabase::class.java,
                    "petcare_database"
                ).build()

                INSTANCE = instance

                instance
            }
        }
    }
}
