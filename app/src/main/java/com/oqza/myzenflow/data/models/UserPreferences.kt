package com.oqza.myzenflow.data.models

/**
 * User preferences data model
 */
data class UserPreferences(
    val language: AppLanguage = AppLanguage.ENGLISH,
    val hapticFeedbackEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val dailyReminderEnabled: Boolean = false,
    val dailyReminderTime: String = "09:00", // HH:mm format
    val soundEnabled: Boolean = true,
    val soundVolume: Float = 0.7f, // 0.0 to 1.0
    val backgroundMusicEnabled: Boolean = true,
    val backgroundMusicType: String = "nature", // nature, rain, ocean, etc.
    val isPremiumUnlocked: Boolean = false,
    val weeklyGoalMinutes: Int = 210, // 30 minutes per day
    val breathingGuidanceVoice: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val autoStartBreathingExercise: Boolean = false,
    val showSessionReminders: Boolean = true,
    val onboardingCompleted: Boolean = false
)

enum class AppLanguage(val displayName: String, val code: String) {
    ENGLISH("English", "en"),
    TURKISH("Türkçe", "tr");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return values().find { it.code == code } ?: ENGLISH
        }
    }
}
