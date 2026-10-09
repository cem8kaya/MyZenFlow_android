package com.oqza.myzenflow.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oqza.myzenflow.data.models.BreathingExerciseType
import com.oqza.myzenflow.data.models.UserPreferences
import com.oqza.myzenflow.data.models.UserStats
import com.oqza.myzenflow.data.repository.PreferencesRepository
import com.oqza.myzenflow.data.repository.SessionRepository
import com.oqza.myzenflow.data.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * ViewModel for ProfileScreen
 * Manages user profile data, stats, and personalization
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val statsRepository: StatsRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    /**
     * Load profile data from repositories
     */
    private fun loadProfileData() {
        viewModelScope.launch {
            combine(
                preferencesRepository.userPreferences,
                statsRepository.getUserStats()
            ) { preferences, stats ->
                ProfileUiState(
                    userName = preferences.userName.ifEmpty { "Zenmaster" },
                    isPremium = preferences.isPremiumUnlocked,
                    memberSince = formatMemberSinceDate(preferences.installDate, stats),
                    stats = stats,
                    isLoading = false
                )
            }.catch { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to load profile"
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    /**
     * Update user name
     */
    fun updateUserName(name: String) {
        viewModelScope.launch {
            try {
                preferencesRepository.updateUserName(name)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update name"
                )
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Format member since date
     * Uses first session date if available, otherwise install date
     */
    private fun formatMemberSinceDate(installDate: Long, stats: UserStats): String {
        val date = stats.lastSessionDate?.let { lastSession ->
            // Try to get first session date by checking earliest session
            viewModelScope.launch {
                sessionRepository.getAllSessions().first().minByOrNull { it.date }?.date?.toLocalDate()
            }
            // For now, use install date as fallback
            Instant.ofEpochMilli(installDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        } ?: Instant.ofEpochMilli(installDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        return date.format(formatter)
    }

    /**
     * Get formatted favorite exercise name
     */
    fun getFavoriteExerciseName(exercise: BreathingExerciseType?): String {
        return exercise?.displayName ?: "None yet"
    }
}

/**
 * UI state for ProfileScreen
 */
data class ProfileUiState(
    val userName: String = "",
    val isPremium: Boolean = false,
    val memberSince: String = "",
    val stats: UserStats = UserStats(),
    val isLoading: Boolean = true,
    val error: String? = null
)
