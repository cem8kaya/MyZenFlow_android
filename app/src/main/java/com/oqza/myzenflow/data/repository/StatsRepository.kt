package com.oqza.myzenflow.data.repository

import com.oqza.myzenflow.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for statistics and analytics
 * Calculates user statistics from session data
 */
@Singleton
class StatsRepository @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val focusRepository: FocusRepository,
    private val preferencesRepository: PreferencesRepository
) {

    /**
     * Get comprehensive user statistics
     */
    fun getUserStats(): Flow<UserStats> {
        return combine(
            sessionRepository.getAllSessions(),
            focusRepository.getAllSessions(),
            preferencesRepository.userPreferences
        ) { meditationSessions, focusSessions, preferences ->
            calculateUserStats(meditationSessions, focusSessions, preferences)
        }
    }

    /**
     * Calculate user statistics from sessions
     */
    private fun calculateUserStats(
        meditationSessions: List<SessionData>,
        focusSessions: List<FocusSessionData>,
        preferences: UserPreferences
    ): UserStats {
        val completedMeditationSessions = meditationSessions.filter { it.completed }
        val completedFocusSessions = focusSessions.filter { it.completed }

        val totalSessions = completedMeditationSessions.size
        val totalMinutes = completedMeditationSessions.sumOf { it.duration } / 60

        val totalFocusSessions = completedFocusSessions.size
        val totalFocusMinutes = completedFocusSessions.sumOf { it.duration } / 60

        val currentStreak = calculateCurrentStreak(completedMeditationSessions)
        val longestStreak = calculateLongestStreak(completedMeditationSessions)

        val weeklyMinutes = calculateWeeklyMinutes(completedMeditationSessions)
        val sessionsThisWeek = calculateSessionsThisWeek(completedMeditationSessions)
        val sessionsThisMonth = calculateSessionsThisMonth(completedMeditationSessions)

        val averageSessionDuration = if (totalSessions > 0) {
            totalMinutes / totalSessions
        } else 0

        val favoriteBreathingExercise = findFavoriteBreathingExercise(completedMeditationSessions)
        val mostProductiveTime = findMostProductiveTime(completedMeditationSessions)

        val lastSessionDate = completedMeditationSessions
            .maxByOrNull { it.date }
            ?.date?.toLocalDate()

        return UserStats(
            totalSessions = totalSessions,
            totalMinutes = totalMinutes,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            weeklyGoalMinutes = preferences.weeklyGoalMinutes,
            weeklyCompletedMinutes = weeklyMinutes,
            totalFocusSessions = totalFocusSessions,
            totalFocusMinutes = totalFocusMinutes,
            averageSessionDuration = averageSessionDuration,
            sessionsThisWeek = sessionsThisWeek,
            sessionsThisMonth = sessionsThisMonth,
            favoriteBreathingExercise = favoriteBreathingExercise,
            mostProductiveTime = mostProductiveTime,
            lastSessionDate = lastSessionDate
        )
    }

    /**
     * Calculate current meditation streak
     */
    private fun calculateCurrentStreak(sessions: List<SessionData>): Int {
        if (sessions.isEmpty()) return 0

        val sortedDates = sessions
            .map { it.date.toLocalDate() }
            .distinct()
            .sortedDescending()

        var streak = 0
        var currentDate = LocalDate.now()

        for (date in sortedDates) {
            if (date == currentDate || date == currentDate.minusDays(1)) {
                streak++
                currentDate = date.minusDays(1)
            } else {
                break
            }
        }

        return streak
    }

    /**
     * Calculate longest meditation streak
     */
    private fun calculateLongestStreak(sessions: List<SessionData>): Int {
        if (sessions.isEmpty()) return 0

        val sortedDates = sessions
            .map { it.date.toLocalDate() }
            .distinct()
            .sorted()

        var longestStreak = 1
        var currentStreak = 1

        for (i in 1 until sortedDates.size) {
            val daysDifference = sortedDates[i].toEpochDay() - sortedDates[i - 1].toEpochDay()

            if (daysDifference == 1L) {
                currentStreak++
                longestStreak = maxOf(longestStreak, currentStreak)
            } else {
                currentStreak = 1
            }
        }

        return longestStreak
    }

    /**
     * Calculate minutes meditated this week
     */
    private fun calculateWeeklyMinutes(sessions: List<SessionData>): Int {
        val startOfWeek = LocalDate.now()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .atStartOfDay()

        val endOfWeek = LocalDate.now()
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            .atTime(23, 59, 59)

        return sessions
            .filter { it.date.isAfter(startOfWeek) && it.date.isBefore(endOfWeek) }
            .sumOf { it.duration } / 60
    }

    /**
     * Calculate sessions completed this week
     */
    private fun calculateSessionsThisWeek(sessions: List<SessionData>): Int {
        val startOfWeek = LocalDate.now()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .atStartOfDay()

        return sessions.count { it.date.isAfter(startOfWeek) }
    }

    /**
     * Calculate sessions completed this month
     */
    private fun calculateSessionsThisMonth(sessions: List<SessionData>): Int {
        val startOfMonth = LocalDate.now()
            .with(TemporalAdjusters.firstDayOfMonth())
            .atStartOfDay()

        return sessions.count { it.date.isAfter(startOfMonth) }
    }

    /**
     * Find user's favorite breathing exercise
     */
    private fun findFavoriteBreathingExercise(sessions: List<SessionData>): BreathingExercise? {
        return sessions
            .mapNotNull { it.breathingExercise }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
    }

    /**
     * Find user's most productive time of day
     */
    private fun findMostProductiveTime(sessions: List<SessionData>): TimeOfDay? {
        return sessions
            .map { TimeOfDay.fromHour(it.date.hour) }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
    }

    /**
     * Get sessions for a specific date range
     */
    fun getSessionsForDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<SessionData>> {
        return sessionRepository.getSessionsInDateRange(startDate, endDate)
    }

    /**
     * Get focus sessions for a specific date range
     */
    fun getFocusSessionsForDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<FocusSessionData>> {
        return focusRepository.getSessionsInDateRange(startDate, endDate)
    }
}
