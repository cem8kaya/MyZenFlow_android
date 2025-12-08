package com.oqza.myzenflow.data.repository

import com.oqza.myzenflow.data.dao.AchievementDao
import com.oqza.myzenflow.data.entities.AchievementEntity
import com.oqza.myzenflow.data.models.AchievementType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for achievement data
 * Manages achievement progress and unlock logic
 */
@Singleton
class AchievementRepository @Inject constructor(
    private val achievementDao: AchievementDao
) {

    /**
     * Get all achievements
     */
    fun getAllAchievements(): Flow<List<AchievementEntity>> {
        return achievementDao.getAllAchievements()
    }

    /**
     * Get unlocked achievements
     */
    fun getUnlockedAchievements(): Flow<List<AchievementEntity>> {
        return achievementDao.getUnlockedAchievements()
    }

    /**
     * Get locked achievements
     */
    fun getLockedAchievements(): Flow<List<AchievementEntity>> {
        return achievementDao.getLockedAchievements()
    }

    /**
     * Get specific achievement by type
     */
    suspend fun getAchievement(type: AchievementType): AchievementEntity? {
        return achievementDao.getAchievementByType(type)
    }

    /**
     * Get unlocked achievement count
     */
    suspend fun getUnlockedCount(): Int {
        return achievementDao.getUnlockedCount()
    }

    /**
     * Get total achievement count
     */
    suspend fun getTotalCount(): Int {
        return achievementDao.getTotalCount()
    }

    /**
     * Initialize achievements (create all if they don't exist)
     */
    suspend fun initializeAchievements() {
        val totalCount = achievementDao.getTotalCount()
        if (totalCount == 0) {
            val initialAchievements = AchievementEntity.createInitialAchievements()
            achievementDao.insertAll(initialAchievements)
        }
    }

    /**
     * Update achievement progress
     */
    suspend fun updateProgress(type: AchievementType, progress: Int) {
        achievementDao.updateProgress(type, progress)
    }

    /**
     * Unlock achievement
     */
    suspend fun unlockAchievement(type: AchievementType) {
        val achievement = achievementDao.getAchievementByType(type)
        if (achievement != null && !achievement.isUnlocked) {
            val timestamp = LocalDateTime.now()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            achievementDao.unlockAchievement(type, timestamp)
        }
    }

    /**
     * Check and unlock achievement if progress reaches target
     */
    suspend fun checkAndUnlock(type: AchievementType, currentProgress: Int): Boolean {
        val achievement = achievementDao.getAchievementByType(type)
        if (achievement != null && !achievement.isUnlocked) {
            updateProgress(type, currentProgress)
            if (currentProgress >= achievement.progressTarget) {
                unlockAchievement(type)
                return true
            }
        }
        return false
    }

    /**
     * Update all achievement progress based on user stats
     */
    suspend fun updateAllAchievementProgress(
        totalSessions: Int,
        totalMinutes: Int,
        currentStreak: Int,
        focusSessions: Int,
        breathingSessions: Int,
        earlyBirdSessions: Int,
        nightOwlSessions: Int,
        weekendStreaks: Int,
        treeLevel: Int
    ) {
        // Streak achievements
        checkAndUnlock(AchievementType.FIRST_SESSION, if (totalSessions > 0) 1 else 0)
        checkAndUnlock(AchievementType.STREAK_3_DAYS, currentStreak)
        checkAndUnlock(AchievementType.STREAK_7_DAYS, currentStreak)
        checkAndUnlock(AchievementType.STREAK_30_DAYS, currentStreak)
        checkAndUnlock(AchievementType.STREAK_100_DAYS, currentStreak)

        // Session count achievements
        checkAndUnlock(AchievementType.SESSIONS_10, totalSessions)
        checkAndUnlock(AchievementType.SESSIONS_50, totalSessions)
        checkAndUnlock(AchievementType.SESSIONS_100, totalSessions)
        checkAndUnlock(AchievementType.SESSIONS_500, totalSessions)

        // Duration achievements
        checkAndUnlock(AchievementType.MINUTES_60, totalMinutes)
        checkAndUnlock(AchievementType.MINUTES_300, totalMinutes)
        checkAndUnlock(AchievementType.MINUTES_1200, totalMinutes)
        checkAndUnlock(AchievementType.MINUTES_6000, totalMinutes)

        // Focus achievements
        checkAndUnlock(AchievementType.FOCUS_MASTER_10, focusSessions)
        checkAndUnlock(AchievementType.FOCUS_MASTER_50, focusSessions)

        // Breathing achievements
        checkAndUnlock(AchievementType.BREATHING_MASTER_10, breathingSessions)
        checkAndUnlock(AchievementType.BREATHING_MASTER_50, breathingSessions)

        // Special achievements
        checkAndUnlock(AchievementType.EARLY_BIRD, earlyBirdSessions)
        checkAndUnlock(AchievementType.NIGHT_OWL, nightOwlSessions)
        checkAndUnlock(AchievementType.WEEKEND_WARRIOR, weekendStreaks)
        checkAndUnlock(AchievementType.TREE_MASTER, treeLevel)
    }
}
