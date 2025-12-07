package com.oqza.myzenflow.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oqza.myzenflow.data.entities.AchievementEntity
import com.oqza.myzenflow.data.models.UserStats
import com.oqza.myzenflow.data.repository.AchievementRepository
import com.oqza.myzenflow.data.repository.BreathingRepository
import com.oqza.myzenflow.data.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * UI State for Zen Garden screen
 */
data class ZenGardenUiState(
    val userStats: UserStats = UserStats(),
    val achievements: List<AchievementEntity> = emptyList(),
    val unlockedAchievements: List<AchievementEntity> = emptyList(),
    val lockedAchievements: List<AchievementEntity> = emptyList(),
    val weeklyData: List<DailyStats> = emptyList(),
    val monthlyData: List<DailyStats> = emptyList(),
    val isLoading: Boolean = true,
    val selectedTab: ZenGardenTab = ZenGardenTab.TREE
)

/**
 * Daily stats for charts
 */
data class DailyStats(
    val date: LocalDate,
    val minutes: Int,
    val sessions: Int
)

/**
 * Tabs for Zen Garden screen
 */
enum class ZenGardenTab {
    TREE,           // Tree visualization
    ACHIEVEMENTS,   // Badge gallery
    STATS           // Statistics dashboard
}

/**
 * ViewModel for Zen Garden screen
 * Manages tree growth, achievements, and statistics
 */
@HiltViewModel
class ZenGardenViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val achievementRepository: AchievementRepository,
    private val breathingRepository: BreathingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ZenGardenUiState())
    val uiState: StateFlow<ZenGardenUiState> = _uiState.asStateFlow()

    init {
        initializeData()
    }

    /**
     * Initialize data from repositories
     */
    private fun initializeData() {
        viewModelScope.launch {
            // Initialize achievements if they don't exist
            achievementRepository.initializeAchievements()

            // Combine all data streams
            combine(
                statsRepository.getUserStats(),
                achievementRepository.getAllAchievements()
            ) { stats, achievements ->
                // Update achievement progress based on stats
                updateAchievementProgress(stats)

                val unlocked = achievements.filter { it.isUnlocked }
                val locked = achievements.filter { !it.isUnlocked }

                ZenGardenUiState(
                    userStats = stats,
                    achievements = achievements,
                    unlockedAchievements = unlocked,
                    lockedAchievements = locked,
                    weeklyData = emptyList(), // Will be loaded separately
                    monthlyData = emptyList(), // Will be loaded separately
                    isLoading = false,
                    selectedTab = _uiState.value.selectedTab
                )
            }.collect { newState ->
                _uiState.value = newState
                // Load weekly and monthly data after main state is loaded
                if (!newState.isLoading) {
                    loadChartData()
                }
            }
        }
    }

    /**
     * Update achievement progress based on user stats
     */
    private suspend fun updateAchievementProgress(stats: UserStats) {
        // Calculate special achievement progress
        val earlyBirdSessions = calculateEarlyBirdSessions()
        val nightOwlSessions = calculateNightOwlSessions()
        val weekendStreaks = calculateWeekendStreaks()

        // Update all achievement progress
        achievementRepository.updateAllAchievementProgress(
            totalSessions = stats.totalSessions,
            totalMinutes = stats.totalMinutes,
            currentStreak = stats.currentStreak,
            focusSessions = stats.totalFocusSessions,
            breathingSessions = breathingRepository.getTotalCompletedSessions(),
            earlyBirdSessions = earlyBirdSessions,
            nightOwlSessions = nightOwlSessions,
            weekendStreaks = weekendStreaks,
            treeLevel = stats.treeLevel
        )
    }

    /**
     * Calculate early bird sessions (before 8 AM)
     */
    private suspend fun calculateEarlyBirdSessions(): Int {
        return statsRepository.getSessionsForDateRange(
            LocalDate.now().minusYears(1).atStartOfDay(),
            LocalDateTime.now()
        ).first().count { it.date.hour < 8 }
    }

    /**
     * Calculate night owl sessions (after 10 PM)
     */
    private suspend fun calculateNightOwlSessions(): Int {
        return statsRepository.getSessionsForDateRange(
            LocalDate.now().minusYears(1).atStartOfDay(),
            LocalDateTime.now()
        ).first().count { it.date.hour >= 22 }
    }

    /**
     * Calculate weekend streaks
     */
    private suspend fun calculateWeekendStreaks(): Int {
        val sessions = statsRepository.getSessionsForDateRange(
            LocalDate.now().minusMonths(2).atStartOfDay(),
            LocalDateTime.now()
        ).first()

        val weekendDates = sessions
            .filter {
                val dayOfWeek = it.date.dayOfWeek
                dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
            }
            .map { it.date.toLocalDate() }
            .distinct()
            .sorted()

        // Count consecutive weekends
        var streaks = 0
        var currentStreak = 0
        var lastWeekendDate: LocalDate? = null

        for (date in weekendDates) {
            if (lastWeekendDate == null || date.toEpochDay() - lastWeekendDate.toEpochDay() <= 7) {
                currentStreak++
                if (currentStreak >= 2) { // At least 2 weekends
                    streaks = currentStreak / 2 // Number of complete weekend pairs
                }
            } else {
                currentStreak = 1
            }
            lastWeekendDate = date
        }

        return streaks
    }

    /**
     * Load weekly and monthly chart data
     */
    private fun loadChartData() {
        viewModelScope.launch {
            val weeklyData = calculateWeeklyData()
            val monthlyData = calculateMonthlyData()

            _uiState.value = _uiState.value.copy(
                weeklyData = weeklyData,
                monthlyData = monthlyData
            )
        }
    }

    /**
     * Calculate daily stats for the current week
     */
    private suspend fun calculateWeeklyData(): List<DailyStats> {
        val startOfWeek = LocalDate.now()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = LocalDate.now()
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        val sessions = statsRepository.getSessionsForDateRange(
            startOfWeek.atStartOfDay(),
            endOfWeek.atTime(23, 59, 59)
        ).first()

        val dailyStats = mutableListOf<DailyStats>()
        var currentDate = startOfWeek

        while (!currentDate.isAfter(endOfWeek)) {
            val daySessions = sessions.filter { it.date.toLocalDate() == currentDate }
            dailyStats.add(
                DailyStats(
                    date = currentDate,
                    minutes = daySessions.sumOf { it.duration } / 60,
                    sessions = daySessions.size
                )
            )
            currentDate = currentDate.plusDays(1)
        }

        return dailyStats
    }

    /**
     * Calculate daily stats for the current month
     */
    private suspend fun calculateMonthlyData(): List<DailyStats> {
        val startOfMonth = LocalDate.now()
            .with(TemporalAdjusters.firstDayOfMonth())
        val endOfMonth = LocalDate.now()
            .with(TemporalAdjusters.lastDayOfMonth())

        val sessions = statsRepository.getSessionsForDateRange(
            startOfMonth.atStartOfDay(),
            endOfMonth.atTime(23, 59, 59)
        ).first()

        val dailyStats = mutableListOf<DailyStats>()
        var currentDate = startOfMonth

        while (!currentDate.isAfter(endOfMonth)) {
            val daySessions = sessions.filter { it.date.toLocalDate() == currentDate }
            dailyStats.add(
                DailyStats(
                    date = currentDate,
                    minutes = daySessions.sumOf { it.duration } / 60,
                    sessions = daySessions.size
                )
            )
            currentDate = currentDate.plusDays(1)
        }

        return dailyStats
    }

    /**
     * Select a tab
     */
    fun selectTab(tab: ZenGardenTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    /**
     * Refresh data
     */
    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        initializeData()
    }
}
