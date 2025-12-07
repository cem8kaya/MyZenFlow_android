package com.oqza.myzenflow.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Achievement badge types for gamification
 */
enum class AchievementType {
    // Streak achievements
    FIRST_SESSION,          // Complete first session
    STREAK_3_DAYS,         // 3-day streak
    STREAK_7_DAYS,         // 7-day streak
    STREAK_30_DAYS,        // 30-day streak
    STREAK_100_DAYS,       // 100-day streak

    // Session count achievements
    SESSIONS_10,           // 10 total sessions
    SESSIONS_50,           // 50 total sessions
    SESSIONS_100,          // 100 total sessions
    SESSIONS_500,          // 500 total sessions

    // Duration achievements
    MINUTES_60,            // 60 total minutes
    MINUTES_300,           // 5 hours (300 minutes)
    MINUTES_1200,          // 20 hours (1200 minutes)
    MINUTES_6000,          // 100 hours (6000 minutes)

    // Focus achievements
    FOCUS_MASTER_10,       // 10 focus sessions
    FOCUS_MASTER_50,       // 50 focus sessions

    // Breathing achievements
    BREATHING_MASTER_10,   // 10 breathing sessions
    BREATHING_MASTER_50,   // 50 breathing sessions

    // Special achievements
    EARLY_BIRD,            // Complete 5 sessions before 8 AM
    NIGHT_OWL,             // Complete 5 sessions after 10 PM
    WEEKEND_WARRIOR,       // Complete sessions 4 weekends in a row
    TREE_MASTER            // Grow tree to max level
}

/**
 * Room entity for user achievements
 */
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val type: AchievementType,
    val unlockedAt: LocalDateTime? = null,
    val isUnlocked: Boolean = false,
    val progress: Int = 0,           // Current progress towards achievement
    val progressTarget: Int = 0       // Target progress to unlock
) {
    companion object {
        /**
         * Get achievement metadata (title, description, icon)
         */
        fun getAchievementInfo(type: AchievementType): AchievementInfo {
            return when (type) {
                AchievementType.FIRST_SESSION -> AchievementInfo(
                    title = "İlk Adım",
                    description = "İlk meditasyon seansını tamamla",
                    icon = "🌱",
                    progressTarget = 1
                )
                AchievementType.STREAK_3_DAYS -> AchievementInfo(
                    title = "Tutarlılık",
                    description = "3 gün üst üste meditasyon yap",
                    icon = "🔥",
                    progressTarget = 3
                )
                AchievementType.STREAK_7_DAYS -> AchievementInfo(
                    title = "Bir Hafta",
                    description = "7 gün streak elde et",
                    icon = "⭐",
                    progressTarget = 7
                )
                AchievementType.STREAK_30_DAYS -> AchievementInfo(
                    title = "Alışkanlık Ustası",
                    description = "30 gün streak elde et",
                    icon = "💎",
                    progressTarget = 30
                )
                AchievementType.STREAK_100_DAYS -> AchievementInfo(
                    title = "Zen Ustası",
                    description = "100 gün streak elde et",
                    icon = "👑",
                    progressTarget = 100
                )
                AchievementType.SESSIONS_10 -> AchievementInfo(
                    title = "Başlangıç",
                    description = "10 seans tamamla",
                    icon = "📝",
                    progressTarget = 10
                )
                AchievementType.SESSIONS_50 -> AchievementInfo(
                    title = "Deneyimli",
                    description = "50 seans tamamla",
                    icon = "📚",
                    progressTarget = 50
                )
                AchievementType.SESSIONS_100 -> AchievementInfo(
                    title = "Veteran",
                    description = "100 seans tamamla",
                    icon = "🎖️",
                    progressTarget = 100
                )
                AchievementType.SESSIONS_500 -> AchievementInfo(
                    title = "Efsane",
                    description = "500 seans tamamla",
                    icon = "🏆",
                    progressTarget = 500
                )
                AchievementType.MINUTES_60 -> AchievementInfo(
                    title = "İlk Saat",
                    description = "60 dakika meditasyon yap",
                    icon = "⏰",
                    progressTarget = 60
                )
                AchievementType.MINUTES_300 -> AchievementInfo(
                    title = "Sabırlı",
                    description = "5 saat meditasyon yap",
                    icon = "⏳",
                    progressTarget = 300
                )
                AchievementType.MINUTES_1200 -> AchievementInfo(
                    title = "Adanmış",
                    description = "20 saat meditasyon yap",
                    icon = "💪",
                    progressTarget = 1200
                )
                AchievementType.MINUTES_6000 -> AchievementInfo(
                    title = "Meditasyon Gurusu",
                    description = "100 saat meditasyon yap",
                    icon = "🧘",
                    progressTarget = 6000
                )
                AchievementType.FOCUS_MASTER_10 -> AchievementInfo(
                    title = "Odaklanma Başlangıcı",
                    description = "10 odaklanma seansı tamamla",
                    icon = "🎯",
                    progressTarget = 10
                )
                AchievementType.FOCUS_MASTER_50 -> AchievementInfo(
                    title = "Odaklanma Ustası",
                    description = "50 odaklanma seansı tamamla",
                    icon = "🎪",
                    progressTarget = 50
                )
                AchievementType.BREATHING_MASTER_10 -> AchievementInfo(
                    title = "Nefes Başlangıcı",
                    description = "10 nefes egzersizi tamamla",
                    icon = "🌬️",
                    progressTarget = 10
                )
                AchievementType.BREATHING_MASTER_50 -> AchievementInfo(
                    title = "Nefes Ustası",
                    description = "50 nefes egzersizi tamamla",
                    icon = "💨",
                    progressTarget = 50
                )
                AchievementType.EARLY_BIRD -> AchievementInfo(
                    title = "Erken Kuş",
                    description = "5 seansı sabah 8'den önce tamamla",
                    icon = "🌅",
                    progressTarget = 5
                )
                AchievementType.NIGHT_OWL -> AchievementInfo(
                    title = "Gece Kuşu",
                    description = "5 seansı gece 22'den sonra tamamla",
                    icon = "🦉",
                    progressTarget = 5
                )
                AchievementType.WEEKEND_WARRIOR -> AchievementInfo(
                    title = "Hafta Sonu Savaşçısı",
                    description = "4 hafta sonu üst üste seans yap",
                    icon = "🏃",
                    progressTarget = 4
                )
                AchievementType.TREE_MASTER -> AchievementInfo(
                    title = "Ağaç Ustası",
                    description = "Ağacını maksimum seviyeye çıkar",
                    icon = "🌳",
                    progressTarget = 5
                )
            }
        }

        /**
         * Create initial achievement entities for all types
         */
        fun createInitialAchievements(): List<AchievementEntity> {
            return AchievementType.values().map { type ->
                val info = getAchievementInfo(type)
                AchievementEntity(
                    type = type,
                    unlockedAt = null,
                    isUnlocked = false,
                    progress = 0,
                    progressTarget = info.progressTarget
                )
            }
        }
    }
}

/**
 * Achievement metadata
 */
data class AchievementInfo(
    val title: String,
    val description: String,
    val icon: String,
    val progressTarget: Int
)
