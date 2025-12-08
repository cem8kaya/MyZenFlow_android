package com.oqza.myzenflow.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oqza.myzenflow.data.models.SessionData
import com.oqza.myzenflow.data.models.UserPreferences
import com.oqza.myzenflow.data.repository.BreathingRepository
import com.oqza.myzenflow.data.repository.PreferencesRepository
import com.oqza.myzenflow.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * ViewModel for Home screen
 * Manages home screen state and data from multiple repositories
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val breathingRepository: BreathingRepository,
    private val sessionRepository: SessionRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    /**
     * Load all home screen data
     */
    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Load user preferences
                val preferences = preferencesRepository.userPreferences.first()

                // Load today's stats
                val todayStart = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)
                val todayEnd = todayStart.plusDays(1)
                val todaySessions = sessionRepository.getSessionsForDay(todayStart, todayEnd)

                val todaySessionCount = todaySessions.count { it.completed }
                val todayMinutes = todaySessions
                    .filter { it.completed }
                    .sumOf { it.duration } / 60

                // Calculate weekly streak
                val streak = calculateWeeklyStreak()

                // Get recent sessions
                val recentSessions = sessionRepository.getRecentSessions(3).first()

                // Get random motivational quote
                val quote = getMotivationalQuote()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    todaySessionCount = todaySessionCount,
                    todayMinutes = todayMinutes,
                    currentStreak = streak,
                    recentSessions = recentSessions,
                    userName = null, // Can be extended later
                    motivationalQuote = quote,
                    userPreferences = preferences
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Veriler yüklenirken bir hata oluştu"
                )
            }
        }
    }

    /**
     * Refresh all data
     */
    fun refreshData() {
        loadData()
    }

    /**
     * Calculate weekly streak (consecutive days with at least one session)
     */
    private suspend fun calculateWeeklyStreak(): Int {
        var streak = 0
        var currentDate = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)

        // Check last 7 days
        for (i in 0 until 7) {
            val dayStart = currentDate.minusDays(i.toLong())
            val dayEnd = dayStart.plusDays(1)
            val sessions = sessionRepository.getSessionsForDay(dayStart, dayEnd)

            if (sessions.any { it.completed }) {
                streak++
            } else if (i > 0) {
                // If we find a day without sessions (and it's not today), stop counting
                break
            }
        }

        return streak
    }

    /**
     * Get a random motivational quote in Turkish
     */
    private fun getMotivationalQuote(): String {
        val quotes = listOf(
            "Her nefes, yeni bir başlangıçtır",
            "Bugün kendine zaman ayırdığın için teşekkürler",
            "Huzur, içinde başlar",
            "Şu an, tek gerçek zamandır",
            "Nefesini takip et, anı yaşa",
            "Her gün biraz daha güçleniyorsun",
            "İçsel dengen, dışsal gücündür",
            "Bugün harika bir gün olacak",
            "Kendinle barışık olmak, en büyük kazanımdır",
            "Ufak adımlar, büyük değişimler yaratır"
        )
        return quotes.random()
    }

    /**
     * Clear any error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for Home screen
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val todaySessionCount: Int = 0,
    val todayMinutes: Int = 0,
    val currentStreak: Int = 0,
    val recentSessions: List<SessionData> = emptyList(),
    val userName: String? = null,
    val motivationalQuote: String = "",
    val userPreferences: UserPreferences = UserPreferences()
)
