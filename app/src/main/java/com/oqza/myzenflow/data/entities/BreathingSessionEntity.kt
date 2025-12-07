package com.oqza.myzenflow.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

/**
 * Room entity for breathing exercise sessions
 * Stores completed breathing session data
 */
@Entity(tableName = "breathing_sessions")
data class BreathingSessionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDateTime,
    val exerciseId: String,
    val exerciseName: String,
    val durationSeconds: Int,
    val cyclesCompleted: Int,
    val totalCycles: Int,
    val inhaleSeconds: Int,
    val holdInhaleSeconds: Int,
    val exhaleSeconds: Int,
    val holdExhaleSeconds: Int,
    val completed: Boolean = true,
    val notes: String? = null
)
