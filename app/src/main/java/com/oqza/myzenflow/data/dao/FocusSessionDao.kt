package com.oqza.myzenflow.data.dao

import androidx.room.*
import com.oqza.myzenflow.data.entities.FocusSessionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * DAO for focus timer sessions
 * Provides CRUD operations and queries for focus session data
 */
@Dao
interface FocusSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: FocusSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<FocusSessionEntity>)

    @Update
    suspend fun update(session: FocusSessionEntity)

    @Delete
    suspend fun delete(session: FocusSessionEntity)

    @Query("DELETE FROM focus_sessions WHERE id = :sessionId")
    suspend fun deleteById(sessionId: String)

    @Query("SELECT * FROM focus_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): FocusSessionEntity?

    @Query("SELECT * FROM focus_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE completed = 1 ORDER BY date DESC")
    fun getCompletedSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getSessionsInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE date >= :startDate ORDER BY date DESC")
    fun getSessionsSince(startDate: LocalDateTime): Flow<List<FocusSessionEntity>>

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE completed = 1")
    suspend fun getTotalCompletedSessions(): Int

    @Query("SELECT SUM(duration) FROM focus_sessions WHERE completed = 1")
    suspend fun getTotalMinutes(): Int?

    @Query("SELECT SUM(completedCycles) FROM focus_sessions")
    suspend fun getTotalCompletedCycles(): Int?

    @Query("SELECT * FROM focus_sessions ORDER BY date DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 10): Flow<List<FocusSessionEntity>>

    @Query("DELETE FROM focus_sessions")
    suspend fun deleteAll()

    @Query("SELECT * FROM focus_sessions WHERE date >= :startOfDay AND date < :endOfDay")
    suspend fun getSessionsForDay(startOfDay: LocalDateTime, endOfDay: LocalDateTime): List<FocusSessionEntity>

    @Query("SELECT * FROM focus_sessions WHERE interrupted = 1 ORDER BY date DESC")
    fun getInterruptedSessions(): Flow<List<FocusSessionEntity>>
}
