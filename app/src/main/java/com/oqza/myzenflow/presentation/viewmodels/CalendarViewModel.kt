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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
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

    // Only the most recent month request may update the screen (rapid next/prev taps, refreshes)
    private var loadJob: Job? = null

    init {
        loadMonth(YearMonth.now())
    }

    /**
     * Show a month. Cached months appear instantly; others are loaded (the latest request wins).
     * @param silent keep the current content visible instead of showing the loading skeleton
     */
    fun loadMonth(yearMonth: YearMonth, silent: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val cached = _uiState.value.monthCache[yearMonth]
            if (cached != null) {
                showMonth(yearMonth, cached)
                return@launch
            }

            if (!silent) {
                _uiState.update { it.copy(isLoading = true) }
            }
            try {
                val days = buildMonth(yearMonth)
                _uiState.update { it.copy(monthCache = it.monthCache + (yearMonth to days)) }
                showMonth(yearMonth, days)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, monthDays = emptyList()) }
            }
        }
    }

    /**
     * Publish a month to the UI. Selection flags and the selected day's sessions are always
     * re-derived, so a cached month never shows an outdated selection.
     */
    private fun showMonth(yearMonth: YearMonth, days: List<DayData>) {
        _uiState.update { state ->
            val selected = state.selectedDate
            val shown = days.map { it.copy(isSelected = it.date == selected) }
            val inMonth = shown.filter { it.isCurrentMonth }
            state.copy(
                selectedMonth = yearMonth,
                monthDays = shown,
                sessionsForSelectedDate = selected?.let { d -> shown.find { it.date == d }?.sessions }
                    ?: emptyList(),
                isLoading = false,
                totalSessionsThisMonth = inMonth.sumOf { it.sessionCount },
                totalMinutesThisMonth = inMonth.sumOf { it.totalMinutes }
            )
        }
    }

    /** Builds the day cells (including leading/trailing days of neighbouring months). */
    private suspend fun buildMonth(yearMonth: YearMonth): List<DayData> {
        val firstDayOfMonth = yearMonth.atDay(1)
        val lastDayOfMonth = yearMonth.atEndOfMonth()
        val firstDayOfCalendar = firstDayOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val lastDayOfCalendar = lastDayOfMonth.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        val allSessions = combineSessionsForDateRange(
            firstDayOfCalendar.atStartOfDay(),
            lastDayOfCalendar.atTime(23, 59, 59)
        )
        return generateCalendarDays(
            firstDayOfCalendar,
            lastDayOfCalendar,
            yearMonth,
            allSessions.groupBy { it.date.toLocalDate() }
        )
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
     * Reload from the database, e.g. after returning to the screen: a session finished elsewhere
     * must show up, so every cached month is dropped.
     */
    fun refresh() {
        _uiState.update { it.copy(monthCache = emptyMap()) }
        loadMonth(_uiState.value.selectedMonth, silent = true)
    }

    /**
     * Warm the cache for the neighbouring months so month navigation feels instant.
     * Only fills the cache: it never changes the month that is on screen.
     */
    fun preloadAdjacentMonths() {
        viewModelScope.launch {
            val current = _uiState.value.selectedMonth
            listOf(current.minusMonths(1), current.plusMonths(1)).forEach { month ->
                if (!_uiState.value.monthCache.containsKey(month)) {
                    try {
                        val days = buildMonth(month)
                        _uiState.update { it.copy(monthCache = it.monthCache + (month to days)) }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        // Preloading is optional
                    }
                }
            }
        }
    }
}
