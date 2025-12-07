package com.oqza.myzenflow.data.repository

import com.oqza.myzenflow.data.dao.MeditationSessionDao
import com.oqza.myzenflow.data.entities.MeditationSessionEntity
import com.oqza.myzenflow.data.models.SessionData
import com.oqza.myzenflow.data.models.SessionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for meditation session data
 * Implements offline-first data management
 */
@Singleton
class SessionRepository @Inject constructor(
    private val meditationSessionDao: MeditationSessionDao
) {

    /**
     * Get all sessions as Flow
     */
    fun getAllSessions(): Flow<List<SessionData>> {
        return meditationSessionDao.getAllSessions()
            .map { entities -> entities.map { it.toSessionData() } }
    }

    /**
     * Get completed sessions
     */
    fun getCompletedSessions(): Flow<List<SessionData>> {
        return meditationSessionDao.getCompletedSessions()
            .map { entities -> entities.map { it.toSessionData() } }
    }

    /**
     * Get sessions by type
     */
    fun getSessionsByType(type: SessionType): Flow<List<SessionData>> {
        return meditationSessionDao.getSessionsByType(type)
            .map { entities -> entities.map { it.toSessionData() } }
    }

    /**
     * Get sessions in date range
     */
    fun getSessionsInDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<SessionData>> {
        return meditationSessionDao.getSessionsInDateRange(startDate, endDate)
            .map { entities -> entities.map { it.toSessionData() } }
    }

    /**
     * Get sessions since a specific date
     */
    fun getSessionsSince(startDate: LocalDateTime): Flow<List<SessionData>> {
        return meditationSessionDao.getSessionsSince(startDate)
            .map { entities -> entities.map { it.toSessionData() } }
    }

    /**
     * Get recent sessions
     */
    fun getRecentSessions(limit: Int = 10): Flow<List<SessionData>> {
        return meditationSessionDao.getRecentSessions(limit)
            .map { entities -> entities.map { it.toSessionData() } }
    }

    /**
     * Get session by ID
     */
    suspend fun getSessionById(sessionId: String): SessionData? {
        return meditationSessionDao.getSessionById(sessionId)?.toSessionData()
    }

    /**
     * Insert new session
     */
    suspend fun insertSession(sessionData: SessionData) {
        val entity = MeditationSessionEntity.fromSessionData(sessionData)
        meditationSessionDao.insert(entity)
    }

    /**
     * Insert multiple sessions
     */
    suspend fun insertSessions(sessions: List<SessionData>) {
        val entities = sessions.map { MeditationSessionEntity.fromSessionData(it) }
        meditationSessionDao.insertAll(entities)
    }

    /**
     * Update existing session
     */
    suspend fun updateSession(sessionData: SessionData) {
        val entity = MeditationSessionEntity.fromSessionData(sessionData)
        meditationSessionDao.update(entity)
    }

    /**
     * Delete session
     */
    suspend fun deleteSession(sessionData: SessionData) {
        val entity = MeditationSessionEntity.fromSessionData(sessionData)
        meditationSessionDao.delete(entity)
    }

    /**
     * Delete session by ID
     */
    suspend fun deleteSessionById(sessionId: String) {
        meditationSessionDao.deleteById(sessionId)
    }

    /**
     * Get total completed sessions count
     */
    suspend fun getTotalCompletedSessions(): Int {
        return meditationSessionDao.getTotalCompletedSessions()
    }

    /**
     * Get total minutes meditated
     */
    suspend fun getTotalMinutes(): Int {
        return meditationSessionDao.getTotalMinutes() ?: 0
    }

    /**
     * Get average session duration
     */
    suspend fun getAverageSessionDuration(): Int {
        return meditationSessionDao.getAverageSessionDuration() ?: 0
    }

    /**
     * Get sessions for a specific day
     */
    suspend fun getSessionsForDay(startOfDay: LocalDateTime, endOfDay: LocalDateTime): List<SessionData> {
        return meditationSessionDao.getSessionsForDay(startOfDay, endOfDay)
            .map { it.toSessionData() }
    }

    /**
     * Delete all sessions
     */
    suspend fun deleteAllSessions() {
        meditationSessionDao.deleteAll()
    }
}
