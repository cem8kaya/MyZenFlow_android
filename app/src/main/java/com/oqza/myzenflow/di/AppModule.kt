package com.oqza.myzenflow.di

import android.content.Context
import androidx.work.WorkManager
import com.oqza.myzenflow.data.datastore.PomodoroTimerDataStore
import com.oqza.myzenflow.data.datastore.PreferencesDataStore
import com.oqza.myzenflow.data.repository.PreferencesRepository
import com.oqza.myzenflow.data.repository.StatsRepository
import com.oqza.myzenflow.domain.zenmaster.AndroidZenStringsProvider
import com.oqza.myzenflow.domain.zenmaster.ZenLanguageSource
import com.oqza.myzenflow.domain.zenmaster.ZenStringsProvider
import com.oqza.myzenflow.domain.zenmaster.ZenUserStatsSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): PreferencesDataStore {
        return PreferencesDataStore(context)
    }

    @Provides
    @Singleton
    fun providePomodoroTimerDataStore(@ApplicationContext context: Context): PomodoroTimerDataStore {
        return PomodoroTimerDataStore(context)
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideZenStringsProvider(@ApplicationContext context: Context): ZenStringsProvider {
        return AndroidZenStringsProvider(context)
    }

    @Provides
    @Singleton
    fun provideZenUserStatsSource(statsRepository: StatsRepository): ZenUserStatsSource {
        return ZenUserStatsSource { statsRepository.getUserStats().first() }
    }

    @Provides
    @Singleton
    fun provideZenLanguageSource(preferencesRepository: PreferencesRepository): ZenLanguageSource {
        return ZenLanguageSource { preferencesRepository.userPreferences.first().language }
    }
}
