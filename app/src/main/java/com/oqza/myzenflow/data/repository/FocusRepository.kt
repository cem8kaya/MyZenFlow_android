package com.oqza.myzenflow.data.repository

import com.oqza.myzenflow.data.dao.FocusSessionDao
import com.oqza.myzenflow.data.entities.FocusSessionEntity
import com.oqza.myzenflow.data.models.FocusSessionData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for focus timer session data
 * Implements offline-first data management
 */
@Singleton
class FocusRepository @Inject constructor(
    private val focusSessionDao: FocusSessionDao
) {

    /**
     * Get all focus sessions as Flow
     */
    fun getAllSessions(): Flow<List<FocusSessionData>> {
        return focusSessionDao.getAllSessions()
            .map { entities -> entities.map { it.toFocusSessionData() } }
    }

    /**
     * Get completed focus sessions
     */
    fun getCompletedSessions(): Flow<List<FocusSessionData>> {
        return focusSessionDao.getCompletedSessions()
            .map { entities -> entities.map { it.toFocusSessionData() } }
    }

    /**
     * Get interrupted sessions
     */
    fun getInterruptedSessions(): Flow<List<FocusSessionData>> {
        return focusSessionDao.getInterruptedSessions()
            .map { entities -> entities.map { it.toFocusSessionData() } }
    }

    /**
     * Get focus sessions in date range
     */
    fun getSessionsInDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<FocusSessionData>> {
        return focusSessionDao.getSessionsInDateRange(startDate, endDate)
            .map { entities -> entities.map { it.toFocusSessionData() } }
    }

    /**
     * Get focus sessions since a specific date
     */
    fun getSessionsSince(startDate: LocalDateTime): Flow<List<FocusSessionData>> {
        return focusSessionDao.getSessionsSince(startDate)
            .map { entities -> entities.map { it.toFocusSessionData() } }
    }

    /**
     * Get recent focus sessions
     */
    fun getRecentSessions(limit: Int = 10): Flow<List<FocusSessionData>> {
        return focusSessionDao.getRecentSessions(limit)
            .map { entities -> entities.map { it.toFocusSessionData() } }
    }

    /**
     * Get focus session by ID
     */
    suspend fun getSessionById(sessionId: String): FocusSessionData? {
        return focusSessionDao.getSessionById(sessionId)?.toFocusSessionData()
    }

    /**
     * Insert new focus session
     */
    suspend fun insertSession(sessionData: FocusSessionData) {
        val entity = FocusSessionEntity.fromFocusSessionData(sessionData)
        focusSessionDao.insert(entity)
    }

    /**
     * Insert multiple focus sessions
     */
    suspend fun insertSessions(sessions: List<FocusSessionData>) {
        val entities = sessions.map { FocusSessionEntity.fromFocusSessionData(it) }
        focusSessionDao.insertAll(entities)
    }

    /**
     * Update existing focus session
     */
    suspend fun updateSession(sessionData: FocusSessionData) {
        val entity = FocusSessionEntity.fromFocusSessionData(sessionData)
        focusSessionDao.update(entity)
    }

    /**
     * Delete focus session
     */
    suspend fun deleteSession(sessionData: FocusSessionData) {
        val entity = FocusSessionEntity.fromFocusSessionData(sessionData)
        focusSessionDao.delete(entity)
    }

    /**
     * Delete focus session by ID
     */
    suspend fun deleteSessionById(sessionId: String) {
        focusSessionDao.deleteById(sessionId)
    }

    /**
     * Get total completed focus sessions count
     */
    suspend fun getTotalCompletedSessions(): Int {
        return focusSessionDao.getTotalCompletedSessions()
    }

    /**
     * Get total focus minutes
     */
    suspend fun getTotalMinutes(): Int {
        return focusSessionDao.getTotalMinutes() ?: 0
    }

    /**
     * Get total completed cycles
     */
    suspend fun getTotalCompletedCycles(): Int {
        return focusSessionDao.getTotalCompletedCycles() ?: 0
    }

    /**
     * Get focus sessions for a specific day
     */
    suspend fun getSessionsForDay(startOfDay: LocalDateTime, endOfDay: LocalDateTime): List<FocusSessionData> {
        return focusSessionDao.getSessionsForDay(startOfDay, endOfDay)
            .map { it.toFocusSessionData() }
    }

    /**
     * Delete all focus sessions
     */
    suspend fun deleteAllSessions() {
        focusSessionDao.deleteAll()
    }
}
