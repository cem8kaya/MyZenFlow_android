package com.oqza.myzenflow.data.dao

import androidx.room.*
import com.oqza.myzenflow.data.entities.AchievementEntity
import com.oqza.myzenflow.data.entities.AchievementType
import kotlinx.coroutines.flow.Flow

/**
 * DAO for achievements
 * Provides CRUD operations and queries for achievement data
 */
@Dao
interface AchievementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(achievement: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<AchievementEntity>)

    @Update
    suspend fun update(achievement: AchievementEntity)

    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE isUnlocked = 1 ORDER BY unlockedAt DESC")
    fun getUnlockedAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE isUnlocked = 0")
    fun getLockedAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE type = :type")
    suspend fun getAchievementByType(type: AchievementType): AchievementEntity?

    @Query("SELECT COUNT(*) FROM achievements WHERE isUnlocked = 1")
    suspend fun getUnlockedCount(): Int

    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getTotalCount(): Int

    @Query("UPDATE achievements SET isUnlocked = 1, unlockedAt = :unlockedAt WHERE type = :type")
    suspend fun unlockAchievement(type: AchievementType, unlockedAt: Long)

    @Query("UPDATE achievements SET progress = :progress WHERE type = :type")
    suspend fun updateProgress(type: AchievementType, progress: Int)

    @Query("DELETE FROM achievements")
    suspend fun deleteAll()
}
