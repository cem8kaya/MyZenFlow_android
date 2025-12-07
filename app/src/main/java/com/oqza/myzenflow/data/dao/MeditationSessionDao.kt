package com.oqza.myzenflow.data.dao

import androidx.room.*
import com.oqza.myzenflow.data.entities.MeditationSessionEntity
import com.oqza.myzenflow.data.models.SessionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * DAO for meditation sessions
 * Provides CRUD operations and queries for meditation data
 */
@Dao
interface MeditationSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: MeditationSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<MeditationSessionEntity>)

    @Update
    suspend fun update(session: MeditationSessionEntity)

    @Delete
    suspend fun delete(session: MeditationSessionEntity)

    @Query("DELETE FROM meditation_sessions WHERE id = :sessionId")
    suspend fun deleteById(sessionId: String)

    @Query("SELECT * FROM meditation_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): MeditationSessionEntity?

    @Query("SELECT * FROM meditation_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<MeditationSessionEntity>>

    @Query("SELECT * FROM meditation_sessions WHERE completed = 1 ORDER BY date DESC")
    fun getCompletedSessions(): Flow<List<MeditationSessionEntity>>

    @Query("SELECT * FROM meditation_sessions WHERE type = :type ORDER BY date DESC")
    fun getSessionsByType(type: SessionType): Flow<List<MeditationSessionEntity>>

    @Query("SELECT * FROM meditation_sessions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getSessionsInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<MeditationSessionEntity>>

    @Query("SELECT * FROM meditation_sessions WHERE date >= :startDate ORDER BY date DESC")
    fun getSessionsSince(startDate: LocalDateTime): Flow<List<MeditationSessionEntity>>

    @Query("SELECT COUNT(*) FROM meditation_sessions WHERE completed = 1")
    suspend fun getTotalCompletedSessions(): Int

    @Query("SELECT SUM(duration) FROM meditation_sessions WHERE completed = 1")
    suspend fun getTotalMinutes(): Int?

    @Query("SELECT * FROM meditation_sessions ORDER BY date DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 10): Flow<List<MeditationSessionEntity>>

    @Query("DELETE FROM meditation_sessions")
    suspend fun deleteAll()

    @Query("SELECT AVG(duration) FROM meditation_sessions WHERE completed = 1")
    suspend fun getAverageSessionDuration(): Int?

    @Query("SELECT * FROM meditation_sessions WHERE date >= :startOfDay AND date < :endOfDay")
    suspend fun getSessionsForDay(startOfDay: LocalDateTime, endOfDay: LocalDateTime): List<MeditationSessionEntity>
}
