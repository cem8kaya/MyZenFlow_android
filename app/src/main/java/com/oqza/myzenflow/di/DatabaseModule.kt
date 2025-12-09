package com.oqza.myzenflow.di

import android.content.Context
import androidx.room.Room
import com.oqza.myzenflow.data.dao.AchievementDao
import com.oqza.myzenflow.data.dao.BreathingSessionDao
import com.oqza.myzenflow.data.dao.FocusSessionDao
import com.oqza.myzenflow.data.dao.MeditationSessionDao
import com.oqza.myzenflow.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides Room database instance
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3
            )
            .fallbackToDestructiveMigration() // For development
            .build()
    }

    /**
     * Provides MeditationSessionDao
     */
    @Provides
    @Singleton
    fun provideMeditationSessionDao(database: AppDatabase): MeditationSessionDao {
        return database.meditationSessionDao()
    }

    /**
     * Provides FocusSessionDao
     */
    @Provides
    @Singleton
    fun provideFocusSessionDao(database: AppDatabase): FocusSessionDao {
        return database.focusSessionDao()
    }

    /**
     * Provides BreathingSessionDao
     */
    @Provides
    @Singleton
    fun provideBreathingSessionDao(database: AppDatabase): BreathingSessionDao {
        return database.breathingSessionDao()
    }

    /**
     * Provides AchievementDao
     */
    @Provides
    @Singleton
    fun provideAchievementDao(database: AppDatabase): AchievementDao {
        return database.achievementDao()
    }
}
