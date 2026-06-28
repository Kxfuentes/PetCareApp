package com.proyectopoo.petcareapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.proyectopoo.petcareapp.data.local.dao.*
import com.proyectopoo.petcareapp.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        OwnerEntity::class,
        CaregiverEntity::class,
        PetEntity::class,
        ServiceTypeEntity::class,
        ServiceRequestEntity::class,
        ServiceRequestPetEntity::class,
        ServiceApplicationEntity::class,
        OfferedServiceEntity::class,
        AvailabilityEntity::class,
        RatingEntity::class,
        ServiceBookingEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class PetCareDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun ownerDao(): OwnerDao
    abstract fun caregiverDao(): CaregiverDao
    abstract fun petDao(): PetDao
    abstract fun serviceTypeDao(): ServiceTypeDao
    abstract fun serviceRequestDao(): ServiceRequestDao
    abstract fun serviceRequestPetDao(): ServiceRequestPetDao
    abstract fun serviceApplicationDao(): ServiceApplicationDao
    abstract fun offeredServiceDao(): OfferedServiceDao
    abstract fun availabilityDao(): AvailabilityDao
    abstract fun ratingDao(): RatingDao
    abstract fun serviceBookingDao(): ServiceBookingDao

    companion object {
        @Volatile
        private var INSTANCE: PetCareDatabase? = null

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE `service_applications` ADD COLUMN `initiatedBy` TEXT NOT NULL DEFAULT 'CAREGIVER'"
                )
                database.execSQL(
                    "ALTER TABLE `ratings` ADD COLUMN `ratedByRole` TEXT NOT NULL DEFAULT 'OWNER'"
                )
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE `service_applications` ADD COLUMN `offeredServiceId` INTEGER"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_service_applications_offeredServiceId` ON `service_applications` (`offeredServiceId`)"
                )
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE `service_requests` ADD COLUMN `offeredServiceId` INTEGER"
                )
                database.execSQL(
                    "ALTER TABLE `service_requests` ADD COLUMN `sourceType` TEXT NOT NULL DEFAULT 'OPEN'"
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `service_request_pets` (
                        `serviceRequestId` INTEGER NOT NULL,
                        `petId` INTEGER NOT NULL,
                        PRIMARY KEY(`serviceRequestId`, `petId`),
                        FOREIGN KEY(`serviceRequestId`) REFERENCES `service_requests`(`serviceRequestId`) ON DELETE CASCADE,
                        FOREIGN KEY(`petId`) REFERENCES `pets`(`petId`) ON DELETE CASCADE
                    )
                    """
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_service_request_pets_serviceRequestId` ON `service_request_pets` (`serviceRequestId`)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_service_request_pets_petId` ON `service_request_pets` (`petId`)"
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `service_bookings` (
                        `bookingId` INTEGER NOT NULL PRIMARY KEY,
                        `serviceRequestId` INTEGER NOT NULL,
                        `caregiverId` INTEGER NOT NULL,
                        `startDate` TEXT,
                        `endDate` TEXT,
                        `status` TEXT NOT NULL DEFAULT 'ACTIVE',
                        FOREIGN KEY(`serviceRequestId`) REFERENCES `service_requests`(`serviceRequestId`) ON DELETE CASCADE,
                        FOREIGN KEY(`caregiverId`) REFERENCES `caregivers`(`caregiverId`) ON DELETE CASCADE
                    )
                    """
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_service_bookings_serviceRequestId` ON `service_bookings` (`serviceRequestId`)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_service_bookings_caregiverId` ON `service_bookings` (`caregiverId`)"
                )
                // Migrate existing single-pet requests into junction table
                database.execSQL(
                    """
                    INSERT OR IGNORE INTO service_request_pets (serviceRequestId, petId)
                    SELECT serviceRequestId, petId FROM service_requests WHERE petId > 0
                    """
                )
            }
        }

        fun getDatabase(context: Context): PetCareDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PetCareDatabase::class.java,
                    "petcare_database"
                )
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
