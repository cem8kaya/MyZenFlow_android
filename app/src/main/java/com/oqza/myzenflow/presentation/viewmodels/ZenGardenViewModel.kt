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
import java.time.LocalDate
import com.oqza.myzenflow.utils.DailyStatsCalculator
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
 *
 * Data flow (one direction, no cycles):
 *   sessions -> stats ------------------------> UI state (tree, charts, achievements list)
 *   sessions + stats -> achievement progress -> achievements table -> UI state
 * Writing achievement progress must never be part of the flow that produces the UI state. It used
 * to be, and the endless write -> re-emit -> write loop made the charts flash empty and refill.
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
        trackAchievementProgress()
        observeUiState()
    }

    /**
     * Keeps achievement progress in sync with practice. Runs on its own so that its database
     * writes cannot trigger it again.
     */
    private fun trackAchievementProgress() {
        viewModelScope.launch {
            achievementRepository.initializeAchievements()
            combine(
                statsRepository.getUserStats(),
                statsRepository.getPracticeSessions()
            ) { stats, sessions -> stats to sessions }
                .distinctUntilChanged()
                .collect { (stats, sessions) ->
                    achievementRepository.updateAllAchievementProgress(
                        totalSessions = stats.totalSessions,
                        totalMinutes = stats.totalMinutes,
                        currentStreak = stats.currentStreak,
                        focusSessions = stats.totalFocusSessions,
                        breathingSessions = breathingRepository.getTotalCompletedSessions(),
                        earlyBirdSessions = DailyStatsCalculator.earlyBirdSessions(sessions),
                        nightOwlSessions = DailyStatsCalculator.nightOwlSessions(sessions),
                        weekendStreaks = DailyStatsCalculator.consecutiveWeekends(sessions),
                        treeLevel = stats.treeLevel
                    )
                }
        }
    }

    /**
     * Builds the screen state, charts included, in a single step so nothing is ever briefly empty.
     */
    private fun observeUiState() {
        viewModelScope.launch {
            combine(
                statsRepository.getUserStats(),
                achievementRepository.getAllAchievements(),
                statsRepository.getPracticeSessions()
            ) { stats, achievements, sessions ->
                Triple(stats, achievements, sessions)
            }.collect { (stats, achievements, sessions) ->
                _uiState.update { current ->
                    current.copy(
                        userStats = stats,
                        achievements = achievements,
                        unlockedAchievements = achievements.filter { it.isUnlocked },
                        lockedAchievements = achievements.filter { !it.isUnlocked },
                        weeklyData = DailyStatsCalculator.currentWeek(sessions),
                        monthlyData = DailyStatsCalculator.currentMonth(sessions),
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Select a tab
     */
    fun selectTab(tab: ZenGardenTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}
