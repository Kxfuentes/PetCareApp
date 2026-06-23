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
        ServiceApplicationEntity::class,
        OfferedServiceEntity::class,
        AvailabilityEntity::class,
        RatingEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class PetCareDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun ownerDao(): OwnerDao
    abstract fun caregiverDao(): CaregiverDao
    abstract fun petDao(): PetDao
    abstract fun serviceTypeDao(): ServiceTypeDao
    abstract fun serviceRequestDao(): ServiceRequestDao
    abstract fun serviceApplicationDao(): ServiceApplicationDao
    abstract fun offeredServiceDao(): OfferedServiceDao
    abstract fun availabilityDao(): AvailabilityDao
    abstract fun ratingDao(): RatingDao

    companion object {
        @Volatile
        private var INSTANCE: PetCareDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {

            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `ratings` (
                        `ratingId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `serviceRequestId` INTEGER NOT NULL,
                        `caregiverId` INTEGER NOT NULL,
                        `ownerId` INTEGER NOT NULL,
                        `score` REAL NOT NULL,
                        `comment` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        FOREIGN KEY(`serviceRequestId`) REFERENCES `service_requests`(`serviceRequestId`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`caregiverId`) REFERENCES `caregivers`(`caregiverId`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`ownerId`) REFERENCES `owners`(`ownerId`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_ratings_serviceRequestId` ON `ratings` (`serviceRequestId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_ratings_caregiverId` ON `ratings` (`caregiverId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_ratings_ownerId` ON `ratings` (`ownerId`)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `offered_services` (
                        `offeredServiceId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `caregiverId` INTEGER NOT NULL,
                        `serviceTypeId` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT,
                        `price` REAL NOT NULL,
                        `isAvailable` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        FOREIGN KEY(`caregiverId`) REFERENCES `caregivers`(`caregiverId`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`serviceTypeId`) REFERENCES `service_types`(`serviceTypeId`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_offered_services_caregiverId` ON `offered_services` (`caregiverId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_offered_services_serviceTypeId` ON `offered_services` (`serviceTypeId`)")
            }
        }

        fun getDatabase(context: Context): PetCareDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PetCareDatabase::class.java,
                    "petcare_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
