package com.oqza.myzenflow.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.oqza.myzenflow.data.models.AppLanguage
import com.oqza.myzenflow.data.models.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * DataStore implementation for user preferences
 * Handles all app settings and user preferences
 */
class PreferencesDataStore(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = PREFERENCES_NAME
    )

    /**
     * Flow of user preferences
     */
    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            mapPreferences(preferences)
        }

    /**
     * Update language preference
     */
    suspend fun updateLanguage(language: AppLanguage) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE] = language.code
        }
    }

    /**
     * Update haptic feedback setting
     */
    suspend fun updateHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAPTIC_FEEDBACK] = enabled
        }
    }

    /**
     * Update notifications setting
     */
    suspend fun updateNotifications(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    /**
     * Update daily reminder settings
     */
    suspend fun updateDailyReminder(enabled: Boolean, time: String) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_REMINDER_ENABLED] = enabled
            preferences[DAILY_REMINDER_TIME] = time
        }
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
        context.dataStore.edit { preferences ->
            preferences[SOUND_ENABLED] = soundEnabled
            preferences[SOUND_VOLUME] = volume
            preferences[BACKGROUND_MUSIC_ENABLED] = backgroundMusicEnabled
            preferences[BACKGROUND_MUSIC_TYPE] = backgroundMusicType
        }
    }

    /**
     * Update premium status
     */
    suspend fun updatePremiumStatus(isPremium: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_PREMIUM] = isPremium
        }
    }

    /**
     * Update weekly goal
     */
    suspend fun updateWeeklyGoal(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[WEEKLY_GOAL_MINUTES] = minutes
        }
    }

    /**
     * Update breathing guidance voice setting
     */
    suspend fun updateBreathingGuidanceVoice(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BREATHING_GUIDANCE_VOICE] = enabled
        }
    }

    /**
     * Update dark mode setting
     */
    suspend fun updateDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_ENABLED] = enabled
        }
    }

    /**
     * Update auto-start breathing exercise setting
     */
    suspend fun updateAutoStartBreathingExercise(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_START_BREATHING] = enabled
        }
    }

    /**
     * Update session reminders setting
     */
    suspend fun updateSessionReminders(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_SESSION_REMINDERS] = enabled
        }
    }

    /**
     * Mark onboarding as completed
     */
    suspend fun completeOnboarding() {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = true
        }
    }

    /**
     * Clear all preferences (for logout/reset)
     */
    suspend fun clearPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Map DataStore preferences to UserPreferences model
     */
    private fun mapPreferences(preferences: Preferences): UserPreferences {
        return UserPreferences(
            language = AppLanguage.fromCode(
                preferences[LANGUAGE] ?: AppLanguage.ENGLISH.code
            ),
            hapticFeedbackEnabled = preferences[HAPTIC_FEEDBACK] ?: true,
            notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true,
            dailyReminderEnabled = preferences[DAILY_REMINDER_ENABLED] ?: false,
            dailyReminderTime = preferences[DAILY_REMINDER_TIME] ?: "09:00",
            soundEnabled = preferences[SOUND_ENABLED] ?: true,
            soundVolume = preferences[SOUND_VOLUME] ?: 0.7f,
            backgroundMusicEnabled = preferences[BACKGROUND_MUSIC_ENABLED] ?: true,
            backgroundMusicType = preferences[BACKGROUND_MUSIC_TYPE] ?: "nature",
            isPremiumUnlocked = preferences[IS_PREMIUM] ?: false,
            weeklyGoalMinutes = preferences[WEEKLY_GOAL_MINUTES] ?: 210,
            breathingGuidanceVoice = preferences[BREATHING_GUIDANCE_VOICE] ?: true,
            darkModeEnabled = preferences[DARK_MODE_ENABLED] ?: false,
            autoStartBreathingExercise = preferences[AUTO_START_BREATHING] ?: false,
            showSessionReminders = preferences[SHOW_SESSION_REMINDERS] ?: true,
            onboardingCompleted = preferences[ONBOARDING_COMPLETED] ?: false
        )
    }

    companion object {
        private const val PREFERENCES_NAME = "myzenflow_preferences"

        // Preference keys
        private val LANGUAGE = stringPreferencesKey("language")
        private val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        private val DAILY_REMINDER_TIME = stringPreferencesKey("daily_reminder_time")
        private val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        private val SOUND_VOLUME = floatPreferencesKey("sound_volume")
        private val BACKGROUND_MUSIC_ENABLED = booleanPreferencesKey("background_music_enabled")
        private val BACKGROUND_MUSIC_TYPE = stringPreferencesKey("background_music_type")
        private val IS_PREMIUM = booleanPreferencesKey("is_premium")
        private val WEEKLY_GOAL_MINUTES = intPreferencesKey("weekly_goal_minutes")
        private val BREATHING_GUIDANCE_VOICE = booleanPreferencesKey("breathing_guidance_voice")
        private val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        private val AUTO_START_BREATHING = booleanPreferencesKey("auto_start_breathing")
        private val SHOW_SESSION_REMINDERS = booleanPreferencesKey("show_session_reminders")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }
}
