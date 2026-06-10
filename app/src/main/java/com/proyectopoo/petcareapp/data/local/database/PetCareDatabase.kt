package com.proyectopoo.petcareapp.data.local.database

import android.content.Context
import com.proyectopoo.petcareapp.data.local.dao.ServiceApplicationDao
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
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
import com.proyectopoo.petcareapp.data.local.entity.ServiceApplicationEntity
import androidx.sqlite.db.SupportSQLiteDatabase

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
        AvailabilityEntity::class,
        ServiceApplicationEntity::class

    ],
    version = 2,
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

    abstract fun serviceApplicationDao(): ServiceApplicationDao
    

    companion object {

        @Volatile
        private var INSTANCE: PetCareDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migratePetsTable(db)
                migrateServiceApplicationsTable(db)
            }
        }

        fun getDatabase(context: Context): PetCareDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PetCareDatabase::class.java,
                    "petcare_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}

private fun migratePetsTable(db: SupportSQLiteDatabase) {
    if (!tableExists(db, "pets")) {
        createPetsTable(db)
        return
    }

    val columns = tableColumns(db, "pets")
    val petId = columnExpression(columns, "petId", "rowid")
    val ownerId = columnExpression(columns, "ownerId", "0")
    val name = columnExpression(columns, "name", "'Mascota'")
    val species = columnExpression(columns, "species", "'Dog'")
    val breed = columnExpression(columns, "breed", "NULL")
    val size = columnExpression(columns, "size", "NULL")

    db.execSQL("DROP TABLE IF EXISTS `pets_new`")
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `pets_new` (
            `petId` INTEGER NOT NULL,
            `ownerId` INTEGER NOT NULL,
            `name` TEXT NOT NULL,
            `species` TEXT NOT NULL,
            `breed` TEXT,
            `size` TEXT,
            PRIMARY KEY(`petId`),
            FOREIGN KEY(`ownerId`) REFERENCES `owners`(`ownerId`) ON UPDATE NO ACTION ON DELETE CASCADE
        )
        """.trimIndent()
    )
    db.execSQL(
        """
        INSERT OR REPLACE INTO `pets_new` (`petId`, `ownerId`, `name`, `species`, `breed`, `size`)
        SELECT
            COALESCE($petId, rowid),
            $ownerId,
            COALESCE($name, 'Mascota'),
            COALESCE($species, 'Dog'),
            $breed,
            $size
        FROM `pets`
        WHERE $ownerId > 0
          AND EXISTS (SELECT 1 FROM `owners` WHERE `owners`.`ownerId` = $ownerId)
        """.trimIndent()
    )
    db.execSQL("DROP TABLE `pets`")
    db.execSQL("ALTER TABLE `pets_new` RENAME TO `pets`")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_pets_ownerId` ON `pets` (`ownerId`)")
}

private fun createPetsTable(db: SupportSQLiteDatabase) {
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `pets` (
            `petId` INTEGER NOT NULL,
            `ownerId` INTEGER NOT NULL,
            `name` TEXT NOT NULL,
            `species` TEXT NOT NULL,
            `breed` TEXT,
            `size` TEXT,
            PRIMARY KEY(`petId`),
            FOREIGN KEY(`ownerId`) REFERENCES `owners`(`ownerId`) ON UPDATE NO ACTION ON DELETE CASCADE
        )
        """.trimIndent()
    )
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_pets_ownerId` ON `pets` (`ownerId`)")
}

private fun migrateServiceApplicationsTable(db: SupportSQLiteDatabase) {
    if (!tableExists(db, "service_applications")) {
        createServiceApplicationsTable(db)
        return
    }

    val columns = tableColumns(db, "service_applications")
    val applicationId = columnExpression(columns, "applicationId", "rowid")
    val serviceRequestId = columnExpression(columns, "serviceRequestId", "0")
    val caregiverId = columnExpression(columns, "caregiverId", "0")
    val status = columnExpression(columns, "status", "'PENDING'")

    db.execSQL("DROP TABLE IF EXISTS `service_applications_new`")
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `service_applications_new` (
            `applicationId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `serviceRequestId` INTEGER NOT NULL,
            `caregiverId` INTEGER NOT NULL,
            `status` TEXT NOT NULL,
            FOREIGN KEY(`serviceRequestId`) REFERENCES `service_requests`(`serviceRequestId`) ON UPDATE NO ACTION ON DELETE CASCADE,
            FOREIGN KEY(`caregiverId`) REFERENCES `caregivers`(`caregiverId`) ON UPDATE NO ACTION ON DELETE CASCADE
        )
        """.trimIndent()
    )
    db.execSQL(
        """
        INSERT OR REPLACE INTO `service_applications_new` (
            `applicationId`,
            `serviceRequestId`,
            `caregiverId`,
            `status`
        )
        SELECT
            COALESCE($applicationId, rowid),
            $serviceRequestId,
            $caregiverId,
            COALESCE($status, 'PENDING')
        FROM `service_applications`
        WHERE $serviceRequestId > 0
          AND $caregiverId > 0
          AND EXISTS (
              SELECT 1 FROM `service_requests`
              WHERE `service_requests`.`serviceRequestId` = $serviceRequestId
          )
          AND EXISTS (
              SELECT 1 FROM `caregivers`
              WHERE `caregivers`.`caregiverId` = $caregiverId
          )
        """.trimIndent()
    )
    db.execSQL("DROP TABLE `service_applications`")
    db.execSQL("ALTER TABLE `service_applications_new` RENAME TO `service_applications`")
    db.execSQL(
        "CREATE INDEX IF NOT EXISTS `index_service_applications_serviceRequestId` " +
            "ON `service_applications` (`serviceRequestId`)"
    )
    db.execSQL(
        "CREATE INDEX IF NOT EXISTS `index_service_applications_caregiverId` " +
            "ON `service_applications` (`caregiverId`)"
    )
}

private fun createServiceApplicationsTable(db: SupportSQLiteDatabase) {
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `service_applications` (
            `applicationId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `serviceRequestId` INTEGER NOT NULL,
            `caregiverId` INTEGER NOT NULL,
            `status` TEXT NOT NULL,
            FOREIGN KEY(`serviceRequestId`) REFERENCES `service_requests`(`serviceRequestId`) ON UPDATE NO ACTION ON DELETE CASCADE,
            FOREIGN KEY(`caregiverId`) REFERENCES `caregivers`(`caregiverId`) ON UPDATE NO ACTION ON DELETE CASCADE
        )
        """.trimIndent()
    )
    db.execSQL(
        "CREATE INDEX IF NOT EXISTS `index_service_applications_serviceRequestId` " +
            "ON `service_applications` (`serviceRequestId`)"
    )
    db.execSQL(
        "CREATE INDEX IF NOT EXISTS `index_service_applications_caregiverId` " +
            "ON `service_applications` (`caregiverId`)"
    )
}

private fun tableExists(db: SupportSQLiteDatabase, tableName: String): Boolean {
    db.query(
        "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
        arrayOf(tableName)
    ).use { cursor ->
        return cursor.moveToFirst()
    }
}

private fun tableColumns(db: SupportSQLiteDatabase, tableName: String): Set<String> {
    db.query("PRAGMA table_info(`$tableName`)").use { cursor ->
        val nameIndex = cursor.getColumnIndex("name")
        val columns = mutableSetOf<String>()
        while (cursor.moveToNext()) {
            columns += cursor.getString(nameIndex)
        }
        return columns
    }
}

private fun columnExpression(columns: Set<String>, columnName: String, fallback: String): String {
    return if (columnName in columns) "`$columnName`" else fallback
}
