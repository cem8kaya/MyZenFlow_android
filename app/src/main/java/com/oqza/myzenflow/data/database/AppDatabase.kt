package com.oqza.myzenflow.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.oqza.myzenflow.data.dao.FocusSessionDao
import com.oqza.myzenflow.data.dao.MeditationSessionDao
import com.oqza.myzenflow.data.entities.FocusSessionEntity
import com.oqza.myzenflow.data.entities.MeditationSessionEntity

/**
 * Room database for MyZenFlow app
 * Contains all entities and provides DAOs
 */
@Database(
    entities = [
        MeditationSessionEntity::class,
        FocusSessionEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun meditationSessionDao(): MeditationSessionDao
    abstract fun focusSessionDao(): FocusSessionDao

    companion object {
        const val DATABASE_NAME = "myzenflow_database"

        /**
         * Migration strategies for future database updates
         *
         * Migration plan:
         * - Version 1: Initial database with MeditationSession and FocusSession tables
         * - Version 2 (planned): Add user preferences table, achievement tracking
         * - Version 3 (planned): Add social features, friend connections
         * - Version 4 (planned): Add custom meditation guides, audio files
         */

        // Example migration from version 1 to 2 (for future use)
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Future migration code will go here
                // Example:
                // database.execSQL("CREATE TABLE IF NOT EXISTS achievements ...")
            }
        }

        // Example migration from version 2 to 3 (for future use)
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Future migration code will go here
            }
        }
    }
}
