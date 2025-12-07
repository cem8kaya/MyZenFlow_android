package com.oqza.myzenflow.data.models

import java.time.LocalDate

/**
 * Data class for user statistics
 * Used for displaying analytics and progress
 */
data class UserStats(
    val totalSessions: Int = 0,
    val totalMinutes: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val weeklyGoalMinutes: Int = 210, // 30 minutes per day
    val weeklyCompletedMinutes: Int = 0,
    val totalFocusSessions: Int = 0,
    val totalFocusMinutes: Int = 0,
    val averageSessionDuration: Int = 0, // in minutes
    val sessionsThisWeek: Int = 0,
    val sessionsThisMonth: Int = 0,
    val favoriteBreathingExercise: BreathingExercise? = null,
    val mostProductiveTime: TimeOfDay? = null,
    val lastSessionDate: LocalDate? = null,
    val treeLevel: Int = 0, // 0-5 (0 = seed, 5 = full tree)
    val treeGrowthProgress: Float = 0f // 0-1 progress to next level
) {
    val weeklyProgress: Float
        get() = if (weeklyGoalMinutes > 0) {
            (weeklyCompletedMinutes.toFloat() / weeklyGoalMinutes.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val isOnStreak: Boolean
        get() = currentStreak > 0
}

enum class TimeOfDay {
    MORNING,    // 5am - 12pm
    AFTERNOON,  // 12pm - 5pm
    EVENING,    // 5pm - 9pm
    NIGHT;      // 9pm - 5am

    companion object {
        fun fromHour(hour: Int): TimeOfDay {
            return when (hour) {
                in 5..11 -> MORNING
                in 12..16 -> AFTERNOON
                in 17..20 -> EVENING
                else -> NIGHT
            }
        }
    }
}
