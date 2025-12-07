package com.oqza.myzenflow.data.models

import java.time.LocalDateTime
import java.util.UUID

/**
 * Data class for focus timer sessions
 */
data class FocusSessionData(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDateTime = LocalDateTime.now(),
    val duration: Int, // in seconds
    val focusDuration: Int, // work duration in seconds
    val breakDuration: Int, // break duration in seconds
    val completedCycles: Int = 0,
    val targetCycles: Int = 4,
    val taskName: String? = null,
    val completed: Boolean = false,
    val interrupted: Boolean = false
)

/**
 * Preset focus modes
 */
enum class FocusMode(
    val displayName: String,
    val focusDuration: Int, // in minutes
    val breakDuration: Int  // in minutes
) {
    POMODORO(
        displayName = "Pomodoro",
        focusDuration = 25,
        breakDuration = 5
    ),
    SHORT_FOCUS(
        displayName = "Short Focus",
        focusDuration = 15,
        breakDuration = 3
    ),
    LONG_FOCUS(
        displayName = "Long Focus",
        focusDuration = 50,
        breakDuration = 10
    ),
    CUSTOM(
        displayName = "Custom",
        focusDuration = 25,
        breakDuration = 5
    );

    companion object {
        fun fromString(value: String): FocusMode {
            return values().find { it.name == value } ?: POMODORO
        }
    }
}
