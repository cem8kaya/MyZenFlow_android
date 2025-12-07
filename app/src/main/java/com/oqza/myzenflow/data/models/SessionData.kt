package com.oqza.myzenflow.data.models

import java.time.LocalDateTime
import java.util.UUID

/**
 * Data class for meditation session
 * Equivalent to iOS SessionData
 */
data class SessionData(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDateTime = LocalDateTime.now(),
    val duration: Int, // in seconds
    val type: SessionType,
    val breathingExercise: BreathingExercise? = null,
    val mood: MoodLevel? = null,
    val notes: String? = null,
    val completed: Boolean = true
)

enum class SessionType {
    MEDITATION,
    BREATHING,
    MINDFULNESS,
    SLEEP;

    companion object {
        fun fromString(value: String): SessionType {
            return values().find { it.name == value } ?: MEDITATION
        }
    }
}

enum class MoodLevel(val value: Int) {
    VERY_BAD(1),
    BAD(2),
    NEUTRAL(3),
    GOOD(4),
    VERY_GOOD(5);

    companion object {
        fun fromValue(value: Int): MoodLevel {
            return values().find { it.value == value } ?: NEUTRAL
        }
    }
}
