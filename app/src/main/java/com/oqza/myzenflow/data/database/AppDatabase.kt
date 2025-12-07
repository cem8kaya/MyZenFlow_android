package com.oqza.myzenflow.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.oqza.myzenflow.data.dao.AchievementDao
import com.oqza.myzenflow.data.dao.BreathingSessionDao
import com.oqza.myzenflow.data.dao.FocusSessionDao
import com.oqza.myzenflow.data.dao.MeditationSessionDao
import com.oqza.myzenflow.data.entities.AchievementEntity
import com.oqza.myzenflow.data.entities.BreathingSessionEntity
import com.oqza.myzenflow.data.entities.FocusSessionEntity
import com.oqza.myzenflow.data.entities.MeditationSessionEntity

/**
 * Room database for MyZenFlow app
 * Contains all entities and provides DAOs
 */
@Database(
    entities = [
        MeditationSessionEntity::class,
        FocusSessionEntity::class,
        BreathingSessionEntity::class,
        AchievementEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun meditationSessionDao(): MeditationSessionDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun breathingSessionDao(): BreathingSessionDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        const val DATABASE_NAME = "myzenflow_database"

        /**
         * Migration strategies for future database updates
         *
         * Migration plan:
         * - Version 1: Initial database with MeditationSession and FocusSession tables
         * - Version 2: Add BreathingSession table
         * - Version 3: Add Achievement table for gamification
         * - Version 4 (planned): Add social features, friend connections
         * - Version 5 (planned): Add custom meditation guides, audio files
         */

        // Migration from version 1 to 2: Add breathing sessions table
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS breathing_sessions (
                        id TEXT PRIMARY KEY NOT NULL,
                        date TEXT NOT NULL,
                        exerciseId TEXT NOT NULL,
                        exerciseName TEXT NOT NULL,
                        durationSeconds INTEGER NOT NULL,
                        cyclesCompleted INTEGER NOT NULL,
                        totalCycles INTEGER NOT NULL,
                        inhaleSeconds INTEGER NOT NULL,
                        holdInhaleSeconds INTEGER NOT NULL,
                        exhaleSeconds INTEGER NOT NULL,
                        holdExhaleSeconds INTEGER NOT NULL,
                        completed INTEGER NOT NULL,
                        notes TEXT
                    )
                """.trimIndent())
            }
        }

        // Migration from version 2 to 3: Add achievements table
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS achievements (
                        type TEXT PRIMARY KEY NOT NULL,
                        unlockedAt INTEGER,
                        isUnlocked INTEGER NOT NULL,
                        progress INTEGER NOT NULL,
                        progressTarget INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }
    }
}
