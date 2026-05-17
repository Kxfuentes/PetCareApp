package com.proyectopoo.petcareapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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

abstract class PetCareDatabase : RoomDatabase() {

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