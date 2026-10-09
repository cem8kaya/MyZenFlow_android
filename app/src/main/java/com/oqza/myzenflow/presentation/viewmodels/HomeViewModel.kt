package com.oqza.myzenflow.presentation.viewmodels

import com.oqza.myzenflow.utils.StreakCalculator
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
import java.time.LocalDate
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
    private fun loadData(silent: Boolean = false) {
        viewModelScope.launch {
            // A silent refresh (returning to the screen) keeps the content visible
            _uiState.value = _uiState.value.copy(isLoading = !silent, error = null)

            try {
                // Load user preferences
                val preferences = preferencesRepository.userPreferences.first()

                // All practice (meditation, breathing and focus) feeds today's stats, streak and recents
                val practice = sessionRepository.getAllPracticeSessions().first()
                val today = LocalDate.now()
                val todaySessions = practice.filter { it.date.toLocalDate() == today }

                val todaySessionCount = todaySessions.size
                val todayMinutes = todaySessions.sumOf { it.duration } / 60

                // Streak over the full history (one rest day per week is forgiven)
                val practiceDays = practice.map { it.date.toLocalDate() }
                val streak = StreakCalculator.current(practiceDays)
                val streakState = StreakCalculator.state(practiceDays)

                val recentSessions = practice.take(3)

                // Get random motivational quote
                val quote = _uiState.value.motivationalQuote.ifEmpty { getMotivationalQuote() }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    todaySessionCount = todaySessionCount,
                    todayMinutes = todayMinutes,
                    currentStreak = streak,
                    streakState = streakState,
                    recentSessions = recentSessions,
                    userName = preferences.userName.takeIf { it.isNotBlank() && it != "Zenmaster" }, // "Zenmaster" was the old default
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
        loadData(silent = true)
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
    val streakState: StreakCalculator.State = StreakCalculator.State.NONE,
    val recentSessions: List<SessionData> = emptyList(),
    val userName: String? = null,
    val motivationalQuote: String = "",
    val userPreferences: UserPreferences = UserPreferences()
)
