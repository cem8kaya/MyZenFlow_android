package com.oqza.myzenflow.data.models

/**
 * User preferences data model
 */
data class UserPreferences(
    val language: AppLanguage = AppLanguage.SYSTEM,
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
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = false, // Material You colors instead of brand palette
    val autoStartBreathingExercise: Boolean = false,
    val showSessionReminders: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val userName: String = "",
    val primaryGoal: PracticeGoal = PracticeGoal.NONE,
    val installDate: Long = System.currentTimeMillis()
)

/**
 * App theme preference. SYSTEM follows the device setting.
 */
/** What the user mainly wants from the app; picked during onboarding and used to tailor suggestions. */
enum class PracticeGoal {
    NONE, STRESS, SLEEP, FOCUS, CALM;

    companion object {
        fun fromName(name: String?): PracticeGoal = entries.firstOrNull { it.name == name } ?: NONE
    }
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromName(name: String?): ThemeMode =
            entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}

/**
 * App languages. [SYSTEM] follows the device (or the per-app language chosen in Android settings)
 * and is the default, so a Turkish phone opens in Turkish on first launch. Native names are used
 * for the others so people can find their language whatever the current one is.
 */
enum class AppLanguage(val displayName: String, val code: String) {
    SYSTEM("", ""),
    ENGLISH("English", "en"),
    TURKISH("Türkçe", "tr"),
    SPANISH("Español", "es"),
    GERMAN("Deutsch", "de"),
    FRENCH("Français", "fr"),
    PORTUGUESE_BR("Português (Brasil)", "pt-BR");

    companion object {
        /** Exact code first ("pt-BR"), then by language ("pt"); unknown or empty means follow the system. */
        fun fromCode(code: String): AppLanguage =
            entries.firstOrNull { it.code.isNotEmpty() && it.code.equals(code, ignoreCase = true) }
                ?: entries.firstOrNull { it.code.isNotEmpty() && it.code.substringBefore('-').equals(code.substringBefore('-'), ignoreCase = true) }
                ?: SYSTEM
    }
}
