package com.oqza.myzenflow.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.oqza.myzenflow.data.models.FocusSessionData
import java.time.LocalDateTime
import java.util.UUID

/**
 * Room entity for focus timer sessions
 */
@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDateTime,
    val duration: Int, // actual duration in seconds
    val focusDuration: Int, // planned work duration in seconds
    val breakDuration: Int, // planned break duration in seconds
    val completedCycles: Int = 0,
    val targetCycles: Int = 4,
    val taskName: String? = null,
    val completed: Boolean = false,
    val interrupted: Boolean = false
) {
    /**
     * Convert entity to domain model
     */
    fun toFocusSessionData(): FocusSessionData {
        return FocusSessionData(
            id = id,
            date = date,
            duration = duration,
            focusDuration = focusDuration,
            breakDuration = breakDuration,
            completedCycles = completedCycles,
            targetCycles = targetCycles,
            taskName = taskName,
            completed = completed,
            interrupted = interrupted
        )
    }

    companion object {
        /**
         * Create entity from domain model
         */
        fun fromFocusSessionData(sessionData: FocusSessionData): FocusSessionEntity {
            return FocusSessionEntity(
                id = sessionData.id,
                date = sessionData.date,
                duration = sessionData.duration,
                focusDuration = sessionData.focusDuration,
                breakDuration = sessionData.breakDuration,
                completedCycles = sessionData.completedCycles,
                targetCycles = sessionData.targetCycles,
                taskName = sessionData.taskName,
                completed = sessionData.completed,
                interrupted = sessionData.interrupted
            )
        }
    }
}
