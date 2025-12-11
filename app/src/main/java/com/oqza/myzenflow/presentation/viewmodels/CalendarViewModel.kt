package com.oqza.myzenflow.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oqza.myzenflow.data.entities.BreathingSessionEntity
import com.oqza.myzenflow.data.models.FocusSessionData
import com.oqza.myzenflow.data.models.SessionData
import com.oqza.myzenflow.data.repository.BreathingRepository
import com.oqza.myzenflow.data.repository.FocusRepository
import com.oqza.myzenflow.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject

/**
 * Combined session data from all session types
 */
sealed class CombinedSession {
    abstract val id: String
    abstract val date: LocalDateTime
    abstract val durationSeconds: Int
    abstract val title: String
    abstract val sessionType: String

    data class MeditationSession(
        override val id: String,
        override val date: LocalDateTime,
        override val durationSeconds: Int,
        val sessionData: SessionData
    ) : CombinedSession() {
        override val title: String = sessionData.breathingExercise?.displayName ?: "Meditation"
        override val sessionType: String = "Meditation"
    }

    data class FocusSession(
        override val id: String,
        override val date: LocalDateTime,
        override val durationSeconds: Int,
        val focusData: FocusSessionData
    ) : CombinedSession() {
        override val title: String = focusData.taskName ?: "Focus Session"
        override val sessionType: String = "Focus"
    }

    data class BreathingSession(
        override val id: String,
        override val date: LocalDateTime,
        override val durationSeconds: Int,
        val breathingData: BreathingSessionEntity
    ) : CombinedSession() {
        override val title: String = breathingData.exerciseName
        override val sessionType: String = "Breathing"
    }
}

/**
 * Day data for calendar cell
 */
data class DayData(
    val date: LocalDate,
    val sessions: List<CombinedSession>,
    val totalMinutes: Int,
    val isToday: Boolean,
    val isCurrentMonth: Boolean,
    val isSelected: Boolean
) {
    val sessionCount: Int get() = sessions.size

    /**
     * Get color intensity based on session count
     */
    fun getIntensityLevel(): Int = when (sessionCount) {
        0 -> 0
        1, 2 -> 1
        3, 4, 5 -> 2
        else -> 3
    }
}

/**
 * Calendar UI State
 */
data class CalendarUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate? = null,
    val monthDays: List<DayData> = emptyList(),
    val sessionsForSelectedDate: List<CombinedSession> = emptyList(),
    val isLoading: Boolean = true,
    val monthCache: Map<YearMonth, List<DayData>> = emptyMap(),
    val totalSessionsThisMonth: Int = 0,
    val totalMinutesThisMonth: Int = 0
)

/**
 * ViewModel for Calendar screen
 * Manages calendar navigation, session visualization, and date selection
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val focusRepository: FocusRepository,
    private val breathingRepository: BreathingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadMonth(YearMonth.now())
    }

    /**
     * Load data for a specific month
     */
    fun loadMonth(yearMonth: YearMonth) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Check cache first
            val cachedData = _uiState.value.monthCache[yearMonth]
            if (cachedData != null) {
                _uiState.value = _uiState.value.copy(
                    selectedMonth = yearMonth,
                    monthDays = cachedData,
                    isLoading = false,
                    totalSessionsThisMonth = cachedData.sumOf { it.sessionCount },
                    totalMinutesThisMonth = cachedData.sumOf { it.totalMinutes }
                )
                return@launch
            }

            // Calculate month range
            val firstDayOfMonth = yearMonth.atDay(1)
            val lastDayOfMonth = yearMonth.atEndOfMonth()

            // Get first day of calendar (might be from previous month)
            val firstDayOfCalendar = firstDayOfMonth.with(DayOfWeek.MONDAY)

            // Get last day of calendar (might be from next month)
            val lastDayOfCalendar = lastDayOfMonth.with(DayOfWeek.SUNDAY).let {
                if (it.isBefore(lastDayOfMonth)) lastDayOfMonth.plusWeeks(1).with(DayOfWeek.SUNDAY)
                else it
            }

            // Fetch all sessions for the month (including surrounding days)
            val startDateTime = firstDayOfCalendar.atStartOfDay()
            val endDateTime = lastDayOfCalendar.atTime(23, 59, 59)

            try {
                // Combine all session types
                val allSessions = combineSessionsForDateRange(startDateTime, endDateTime)

                // Group sessions by date
                val sessionsByDate = allSessions.groupBy { it.date.toLocalDate() }

                // Generate day data for each day in the calendar
                val monthDays = generateCalendarDays(
                    firstDayOfCalendar,
                    lastDayOfCalendar,
                    yearMonth,
                    sessionsByDate
                )

                // Update cache
                val newCache = _uiState.value.monthCache.toMutableMap()
                newCache[yearMonth] = monthDays

                _uiState.value = _uiState.value.copy(
                    selectedMonth = yearMonth,
                    monthDays = monthDays,
                    isLoading = false,
                    monthCache = newCache,
                    totalSessionsThisMonth = monthDays
                        .filter { it.isCurrentMonth }
                        .sumOf { it.sessionCount },
                    totalMinutesThisMonth = monthDays
                        .filter { it.isCurrentMonth }
                        .sumOf { it.totalMinutes }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    monthDays = emptyList()
                )
            }
        }
    }

    /**
     * Combine sessions from all repositories for a date range
     */
    private suspend fun combineSessionsForDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<CombinedSession> {
        val combinedSessions = mutableListOf<CombinedSession>()

        // Get meditation sessions
        val meditationSessions = sessionRepository.getSessionsInDateRange(startDate, endDate).first()
        combinedSessions.addAll(
            meditationSessions
                .filter { it.completed }
                .map { session ->
                    CombinedSession.MeditationSession(
                        id = session.id,
                        date = session.date,
                        durationSeconds = session.duration,
                        sessionData = session
                    )
                }
        )

        // Get focus sessions
        val focusSessions = focusRepository.getSessionsInDateRange(startDate, endDate).first()
        combinedSessions.addAll(
            focusSessions
                .filter { it.completed || it.interrupted }
                .map { session ->
                    CombinedSession.FocusSession(
                        id = session.id,
                        date = session.date,
                        durationSeconds = session.duration,
                        focusData = session
                    )
                }
        )

        // Get breathing sessions
        val breathingSessions = breathingRepository.getSessionsInDateRange(startDate, endDate).first()
        combinedSessions.addAll(
            breathingSessions
                .filter { it.completed }
                .map { session ->
                    CombinedSession.BreathingSession(
                        id = session.id,
                        date = session.date,
                        durationSeconds = session.durationSeconds,
                        breathingData = session
                    )
                }
        )

        return combinedSessions.sortedBy { it.date }
    }

    /**
     * Generate calendar days with session data
     */
    private fun generateCalendarDays(
        startDate: LocalDate,
        endDate: LocalDate,
        currentMonth: YearMonth,
        sessionsByDate: Map<LocalDate, List<CombinedSession>>
    ): List<DayData> {
        val days = mutableListOf<DayData>()
        var currentDate = startDate
        val today = LocalDate.now()
        val selectedDate = _uiState.value.selectedDate

        while (!currentDate.isAfter(endDate)) {
            val sessions = sessionsByDate[currentDate] ?: emptyList()
            val totalMinutes = sessions.sumOf { it.durationSeconds } / 60

            days.add(
                DayData(
                    date = currentDate,
                    sessions = sessions,
                    totalMinutes = totalMinutes,
                    isToday = currentDate == today,
                    isCurrentMonth = YearMonth.from(currentDate) == currentMonth,
                    isSelected = currentDate == selectedDate
                )
            )

            currentDate = currentDate.plusDays(1)
        }

        return days
    }

    /**
     * Select a specific date
     */
    fun selectDate(date: LocalDate) {
        val sessions = _uiState.value.monthDays
            .find { it.date == date }
            ?.sessions ?: emptyList()

        _uiState.value = _uiState.value.copy(
            selectedDate = date,
            sessionsForSelectedDate = sessions,
            monthDays = _uiState.value.monthDays.map { day ->
                day.copy(isSelected = day.date == date)
            }
        )
    }

    /**
     * Clear date selection
     */
    fun clearSelection() {
        _uiState.value = _uiState.value.copy(
            selectedDate = null,
            sessionsForSelectedDate = emptyList(),
            monthDays = _uiState.value.monthDays.map { day ->
                day.copy(isSelected = false)
            }
        )
    }

    /**
     * Get sessions for a specific date
     */
    fun getSessionsForDate(date: LocalDate): List<CombinedSession> {
        return _uiState.value.monthDays
            .find { it.date == date }
            ?.sessions ?: emptyList()
    }

    /**
     * Navigate to previous month
     */
    fun getPreviousMonth() {
        val newMonth = _uiState.value.selectedMonth.minusMonths(1)
        loadMonth(newMonth)
    }

    /**
     * Navigate to next month
     */
    fun getNextMonth() {
        val newMonth = _uiState.value.selectedMonth.plusMonths(1)
        loadMonth(newMonth)
    }

    /**
     * Navigate to today
     */
    fun goToToday() {
        val today = LocalDate.now()
        val currentMonth = YearMonth.from(today)
        if (_uiState.value.selectedMonth != currentMonth) {
            loadMonth(currentMonth)
        }
        selectDate(today)
    }

    /**
     * Get month display name
     */
    fun getMonthDisplayName(): String {
        val month = _uiState.value.selectedMonth
        return "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}"
    }

    /**
     * Get weekday names
     */
    fun getWeekdayNames(): List<String> {
        return DayOfWeek.values().map {
            it.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        }
    }

    /**
     * Format time for display
     */
    fun formatTime(dateTime: LocalDateTime): String {
        return String.format(
            "%02d:%02d",
            dateTime.hour,
            dateTime.minute
        )
    }

    /**
     * Format duration for display
     */
    fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        return if (minutes < 60) {
            "$minutes min"
        } else {
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            "${hours}h ${remainingMinutes}min"
        }
    }

    /**
     * Refresh current month
     */
    fun refresh() {
        // Clear cache for current month
        val newCache = _uiState.value.monthCache.toMutableMap()
        newCache.remove(_uiState.value.selectedMonth)
        _uiState.value = _uiState.value.copy(monthCache = newCache)

        // Reload
        loadMonth(_uiState.value.selectedMonth)
    }

    /**
     * Preload adjacent months for smooth navigation
     */
    fun preloadAdjacentMonths() {
        viewModelScope.launch {
            val currentMonth = _uiState.value.selectedMonth
            val prevMonth = currentMonth.minusMonths(1)
            val nextMonth = currentMonth.plusMonths(1)

            // Load previous month if not cached
            if (!_uiState.value.monthCache.containsKey(prevMonth)) {
                loadMonth(prevMonth)
            }

            // Load next month if not cached
            if (!_uiState.value.monthCache.containsKey(nextMonth)) {
                loadMonth(nextMonth)
            }

            // Restore current month
            loadMonth(currentMonth)
        }
    }
}
