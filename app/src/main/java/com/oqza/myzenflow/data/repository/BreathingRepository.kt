package com.oqza.myzenflow.data.repository

import com.oqza.myzenflow.data.dao.BreathingSessionDao
import com.oqza.myzenflow.data.entities.BreathingSessionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for breathing session data
 * Implements offline-first data management for breathing exercises
 */
@Singleton
class BreathingRepository @Inject constructor(
    private val breathingSessionDao: BreathingSessionDao
) {

    /**
     * Get all breathing sessions as Flow
     */
    fun getAllSessions(): Flow<List<BreathingSessionEntity>> {
        return breathingSessionDao.getAllSessions()
    }

    /**
     * Get completed breathing sessions
     */
    fun getCompletedSessions(): Flow<List<BreathingSessionEntity>> {
        return breathingSessionDao.getCompletedSessions()
    }

    /**
     * Get sessions by exercise type
     */
    fun getSessionsByExerciseType(exerciseId: String): Flow<List<BreathingSessionEntity>> {
        return breathingSessionDao.getSessionsByExerciseType(exerciseId)
    }

    /**
     * Get sessions in date range
     */
    fun getSessionsInDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<BreathingSessionEntity>> {
        return breathingSessionDao.getSessionsInDateRange(startDate, endDate)
    }

    /**
     * Get sessions since a specific date
     */
    fun getSessionsSince(startDate: LocalDateTime): Flow<List<BreathingSessionEntity>> {
        return breathingSessionDao.getSessionsSince(startDate)
    }

    /**
     * Get recent sessions
     */
    fun getRecentSessions(limit: Int = 10): Flow<List<BreathingSessionEntity>> {
        return breathingSessionDao.getRecentSessions(limit)
    }

    /**
     * Get session by ID
     */
    suspend fun getSessionById(sessionId: String): BreathingSessionEntity? {
        return breathingSessionDao.getSessionById(sessionId)
    }

    /**
     * Insert new breathing session
     */
    suspend fun insertSession(session: BreathingSessionEntity) {
        breathingSessionDao.insert(session)
    }

    /**
     * Insert multiple sessions
     */
    suspend fun insertSessions(sessions: List<BreathingSessionEntity>) {
        breathingSessionDao.insertAll(sessions)
    }

    /**
     * Update existing session
     */
    suspend fun updateSession(session: BreathingSessionEntity) {
        breathingSessionDao.update(session)
    }

    /**
     * Delete session
     */
    suspend fun deleteSession(session: BreathingSessionEntity) {
        breathingSessionDao.delete(session)
    }

    /**
     * Delete session by ID
     */
    suspend fun deleteSessionById(sessionId: String) {
        breathingSessionDao.deleteById(sessionId)
    }

    /**
     * Get total completed sessions count
     */
    suspend fun getTotalCompletedSessions(): Int {
        return breathingSessionDao.getTotalCompletedSessions()
    }

    /**
     * Get total duration in seconds
     */
    suspend fun getTotalDurationSeconds(): Int {
        return breathingSessionDao.getTotalDurationSeconds() ?: 0
    }

    /**
     * Get total breathing cycles completed
     */
    suspend fun getTotalCycles(): Int {
        return breathingSessionDao.getTotalCycles() ?: 0
    }

    /**
     * Get average session duration
     */
    suspend fun getAverageSessionDuration(): Int {
        return breathingSessionDao.getAverageSessionDuration() ?: 0
    }

    /**
     * Get sessions for a specific day
     */
    suspend fun getSessionsForDay(startOfDay: LocalDateTime, endOfDay: LocalDateTime): List<BreathingSessionEntity> {
        return breathingSessionDao.getSessionsForDay(startOfDay, endOfDay)
    }

    /**
     * Get most used exercise type
     */
    suspend fun getMostUsedExercise(): String? {
        return breathingSessionDao.getMostUsedExercise()
    }

    /**
     * Delete all sessions
     */
    suspend fun deleteAllSessions() {
        breathingSessionDao.deleteAll()
    }
}
