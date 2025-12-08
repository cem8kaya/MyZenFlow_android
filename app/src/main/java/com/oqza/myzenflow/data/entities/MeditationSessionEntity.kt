package com.oqza.myzenflow.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.oqza.myzenflow.data.models.BreathingExerciseType
import com.oqza.myzenflow.data.models.MoodLevel
import com.oqza.myzenflow.data.models.SessionData
import com.oqza.myzenflow.data.models.SessionType
import java.time.LocalDateTime
import java.util.UUID

/**
 * Room entity for meditation sessions
 * Equivalent to iOS SessionData
 */
@Entity(tableName = "meditation_sessions")
data class MeditationSessionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDateTime,
    val duration: Int, // in seconds
    val type: SessionType,
    val breathingExercise: BreathingExerciseType? = null,
    val mood: MoodLevel? = null,
    val notes: String? = null,
    val completed: Boolean = true
) {
    /**
     * Convert entity to domain model
     */
    fun toSessionData(): SessionData {
        return SessionData(
            id = id,
            date = date,
            duration = duration,
            type = type,
            breathingExercise = breathingExercise,
            mood = mood,
            notes = notes,
            completed = completed
        )
    }

    companion object {
        /**
         * Create entity from domain model
         */
        fun fromSessionData(sessionData: SessionData): MeditationSessionEntity {
            return MeditationSessionEntity(
                id = sessionData.id,
                date = sessionData.date,
                duration = sessionData.duration,
                type = sessionData.type,
                breathingExercise = sessionData.breathingExercise,
                mood = sessionData.mood,
                notes = sessionData.notes,
                completed = sessionData.completed
            )
        }
    }
}
