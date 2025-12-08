package com.oqza.myzenflow.data.models

enum class AchievementType {
    // Streak achievements
    FIRST_SESSION,          // Complete first session
    STREAK_3_DAYS,         // 3-day streak
    STREAK_7_DAYS,         // 7-day streak
    STREAK_30_DAYS,        // 30-day streak
    STREAK_100_DAYS,       // 100-day streak

    // Session count achievements
    SESSIONS_10,           // 10 total sessions
    SESSIONS_50,           // 50 total sessions
    SESSIONS_100,          // 100 total sessions
    SESSIONS_500,          // 500 total sessions

    // Duration achievements
    MINUTES_60,            // 60 total minutes
    MINUTES_300,           // 5 hours (300 minutes)
    MINUTES_1200,          // 20 hours (1200 minutes)
    MINUTES_6000,          // 100 hours (6000 minutes)

    // Focus achievements
    FOCUS_MASTER_10,       // 10 focus sessions
    FOCUS_MASTER_50,       // 50 focus sessions

    // Breathing achievements
    BREATHING_MASTER_10,   // 10 breathing sessions
    BREATHING_MASTER_50,   // 50 breathing sessions

    // Special achievements
    EARLY_BIRD,            // Complete 5 sessions before 8 AM
    NIGHT_OWL,             // Complete 5 sessions after 10 PM
    WEEKEND_WARRIOR,       // Complete sessions 4 weekends in a row
    TREE_MASTER            // Grow tree to max level
}
