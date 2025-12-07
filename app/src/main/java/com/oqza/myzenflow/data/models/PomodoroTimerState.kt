package com.oqza.myzenflow.data.models

/**
 * Timer session type
 */
enum class TimerSessionType {
    WORK,           // Focus/work session
    SHORT_BREAK,    // Short break (after each work session)
    LONG_BREAK      // Long break (after completing full cycle)
}

/**
 * Timer status
 */
enum class TimerStatus {
    IDLE,       // Timer not started
    RUNNING,    // Timer actively running
    PAUSED,     // Timer paused
    COMPLETED   // Session completed
}

/**
 * State for Pomodoro timer UI
 */
data class PomodoroTimerState(
    // Timer settings
    val selectedMode: FocusMode = FocusMode.POMODORO,
    val customFocusDuration: Int = 25, // in minutes
    val customBreakDuration: Int = 5,  // in minutes
    val customLongBreakDuration: Int = 15, // in minutes

    // Current session
    val currentSessionType: TimerSessionType = TimerSessionType.WORK,
    val timerStatus: TimerStatus = TimerStatus.IDLE,
    val timeRemainingSeconds: Int = 0,
    val totalDurationSeconds: Int = 0,

    // Cycle tracking
    val currentCycle: Int = 0,      // Current cycle (0-based)
    val totalCycles: Int = 4,       // Total cycles before long break
    val completedWorkSessions: Int = 0,

    // Session metadata
    val taskName: String = "",
    val sessionId: String? = null,
    val sessionStartTime: Long = 0L, // System time when session started

    // UI state
    val progress: Float = 0f,       // 0-1 for circular progress
    val hapticEnabled: Boolean = true,
    val soundEnabled: Boolean = true,

    // Background state
    val isBackgroundTimerActive: Boolean = false
) {
    /**
     * Calculate progress as percentage (0-1)
     */
    fun calculateProgress(): Float {
        return if (totalDurationSeconds > 0) {
            1f - (timeRemainingSeconds.toFloat() / totalDurationSeconds.toFloat())
        } else {
            0f
        }
    }

    /**
     * Format time as MM:SS
     */
    fun formatTime(): String {
        val minutes = timeRemainingSeconds / 60
        val seconds = timeRemainingSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    /**
     * Get display name for current session type
     */
    fun getSessionTypeDisplayName(): String {
        return when (currentSessionType) {
            TimerSessionType.WORK -> "Çalışma"
            TimerSessionType.SHORT_BREAK -> "Kısa Mola"
            TimerSessionType.LONG_BREAK -> "Uzun Mola"
        }
    }

    /**
     * Check if it's time for a long break
     */
    fun shouldTakeLongBreak(): Boolean {
        return completedWorkSessions > 0 && completedWorkSessions % totalCycles == 0
    }

    /**
     * Get current focus duration in seconds
     */
    fun getCurrentFocusDurationSeconds(): Int {
        return if (selectedMode == FocusMode.CUSTOM) {
            customFocusDuration * 60
        } else {
            selectedMode.focusDuration * 60
        }
    }

    /**
     * Get current break duration in seconds
     */
    fun getCurrentBreakDurationSeconds(): Int {
        return if (selectedMode == FocusMode.CUSTOM) {
            customBreakDuration * 60
        } else {
            selectedMode.breakDuration * 60
        }
    }

    /**
     * Get current long break duration in seconds
     */
    fun getCurrentLongBreakDurationSeconds(): Int {
        return if (selectedMode == FocusMode.CUSTOM) {
            customLongBreakDuration * 60
        } else {
            15 * 60 // Default 15 minutes for long break
        }
    }
}
