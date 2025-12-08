package com.oqza.myzenflow.data.dao

import androidx.room.*
import com.oqza.myzenflow.data.entities.BreathingSessionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * DAO for breathing exercise sessions
 * Provides CRUD operations and queries for breathing session data
 */
@Dao
interface BreathingSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: BreathingSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<BreathingSessionEntity>)

    @Update
    suspend fun update(session: BreathingSessionEntity)

    @Delete
    suspend fun delete(session: BreathingSessionEntity)

    @Query("DELETE FROM breathing_sessions WHERE id = :sessionId")
    suspend fun deleteById(sessionId: String)

    @Query("SELECT * FROM breathing_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): BreathingSessionEntity?

    @Query("SELECT * FROM breathing_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<BreathingSessionEntity>>

    @Query("SELECT * FROM breathing_sessions WHERE completed = 1 ORDER BY date DESC")
    fun getCompletedSessions(): Flow<List<BreathingSessionEntity>>

    @Query("SELECT * FROM breathing_sessions WHERE exerciseId = :exerciseId ORDER BY date DESC")
    fun getSessionsByExerciseType(exerciseId: String): Flow<List<BreathingSessionEntity>>

    @Query("SELECT * FROM breathing_sessions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getSessionsInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<BreathingSessionEntity>>

    @Query("SELECT * FROM breathing_sessions WHERE date >= :startDate ORDER BY date DESC")
    fun getSessionsSince(startDate: LocalDateTime): Flow<List<BreathingSessionEntity>>

    @Query("SELECT COUNT(*) FROM breathing_sessions WHERE completed = 1")
    suspend fun getTotalCompletedSessions(): Int

    @Query("SELECT SUM(durationSeconds) FROM breathing_sessions WHERE completed = 1")
    suspend fun getTotalDurationSeconds(): Int?

    @Query("SELECT SUM(cyclesCompleted) FROM breathing_sessions WHERE completed = 1")
    suspend fun getTotalCycles(): Int?

    @Query("SELECT * FROM breathing_sessions ORDER BY date DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 10): Flow<List<BreathingSessionEntity>>

    @Query("DELETE FROM breathing_sessions")
    suspend fun deleteAll()

    @Query("SELECT AVG(durationSeconds) FROM breathing_sessions WHERE completed = 1")
    suspend fun getAverageSessionDuration(): Int?

    @Query("SELECT * FROM breathing_sessions WHERE date >= :startOfDay AND date < :endOfDay")
    suspend fun getSessionsForDay(startOfDay: LocalDateTime, endOfDay: LocalDateTime): List<BreathingSessionEntity>

    @Query("SELECT exerciseId FROM breathing_sessions WHERE completed = 1 GROUP BY exerciseId ORDER BY COUNT(*) DESC LIMIT 1")
    suspend fun getMostUsedExercise(): String?
}
