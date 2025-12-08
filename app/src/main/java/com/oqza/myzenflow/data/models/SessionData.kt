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
    val breathingExercise: BreathingExerciseType? = null,
    val mood: MoodLevel? = null,
    val notes: String? = null,
    val completed: Boolean = true
)
