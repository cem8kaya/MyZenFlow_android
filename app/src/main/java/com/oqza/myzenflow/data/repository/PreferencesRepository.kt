package com.oqza.myzenflow.data.repository

import com.oqza.myzenflow.data.datastore.PreferencesDataStore
import com.oqza.myzenflow.data.models.AppLanguage
import com.oqza.myzenflow.data.models.UserPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for user preferences
 * Manages all app settings using DataStore
 */
@Singleton
class PreferencesRepository @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore
) {

    /**
     * Flow of user preferences
     */
    val userPreferences: Flow<UserPreferences> = preferencesDataStore.userPreferencesFlow

    /**
     * Update app language
     */
    suspend fun updateLanguage(language: AppLanguage) {
        preferencesDataStore.updateLanguage(language)
    }

    /**
     * Update haptic feedback setting
     */
    suspend fun updateHapticFeedback(enabled: Boolean) {
        preferencesDataStore.updateHapticFeedback(enabled)
    }

    /**
     * Update notifications setting
     */
    suspend fun updateNotifications(enabled: Boolean) {
        preferencesDataStore.updateNotifications(enabled)
    }

    /**
     * Update daily reminder
     */
    suspend fun updateDailyReminder(enabled: Boolean, time: String) {
        preferencesDataStore.updateDailyReminder(enabled, time)
    }

    /**
     * Update sound settings
     */
    suspend fun updateSoundSettings(
        soundEnabled: Boolean,
        volume: Float,
        backgroundMusicEnabled: Boolean,
        backgroundMusicType: String
    ) {
        preferencesDataStore.updateSoundSettings(
            soundEnabled,
            volume,
            backgroundMusicEnabled,
            backgroundMusicType
        )
    }

    /**
     * Update premium unlock status
     */
    suspend fun updatePremiumStatus(isPremium: Boolean) {
        preferencesDataStore.updatePremiumStatus(isPremium)
    }

    /**
     * Update weekly goal
     */
    suspend fun updateWeeklyGoal(minutes: Int) {
        preferencesDataStore.updateWeeklyGoal(minutes)
    }

    /**
     * Update breathing guidance voice
     */
    suspend fun updateBreathingGuidanceVoice(enabled: Boolean) {
        preferencesDataStore.updateBreathingGuidanceVoice(enabled)
    }

    /**
     * Update dark mode
     */
    suspend fun updateDarkMode(enabled: Boolean) {
        preferencesDataStore.updateDarkMode(enabled)
    }

    /**
     * Update auto-start breathing exercise
     */
    suspend fun updateAutoStartBreathingExercise(enabled: Boolean) {
        preferencesDataStore.updateAutoStartBreathingExercise(enabled)
    }

    /**
     * Update session reminders
     */
    suspend fun updateSessionReminders(enabled: Boolean) {
        preferencesDataStore.updateSessionReminders(enabled)
    }

    /**
     * Mark onboarding as completed
     */
    suspend fun completeOnboarding() {
        preferencesDataStore.completeOnboarding()
    }

    /**
     * Clear all preferences
     */
    suspend fun clearAllPreferences() {
        preferencesDataStore.clearPreferences()
    }
}
