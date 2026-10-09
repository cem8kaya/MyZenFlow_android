package com.oqza.myzenflow.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oqza.myzenflow.data.repository.MoodRepository
import com.oqza.myzenflow.data.repository.PreferencesRepository
import com.oqza.myzenflow.data.repository.SessionRepository
import com.oqza.myzenflow.utils.StreakCalculator
import com.oqza.myzenflow.utils.WeeklySummary
import com.oqza.myzenflow.utils.WeeklySummaryCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class WeeklySummaryUiState(
    val isLoading: Boolean = true,
    val summary: WeeklySummary? = null,
    val streak: Int = 0
)

@HiltViewModel
class WeeklySummaryViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    moodRepository: MoodRepository,
    preferencesRepository: PreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<WeeklySummaryUiState> = combine(
        sessionRepository.getAllPracticeSessions(),
        moodRepository.entries,
        preferencesRepository.userPreferences
    ) { sessions, moods, prefs ->
        WeeklySummaryUiState(
            isLoading = false,
            summary = WeeklySummaryCalculator.compute(sessions, moods, prefs.weeklyGoalMinutes),
            streak = StreakCalculator.current(sessions.map { it.date.toLocalDate() })
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeeklySummaryUiState())
}
