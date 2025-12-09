package com.oqza.myzenflow.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.oqza.myzenflow.data.models.TimerSessionType
import com.oqza.myzenflow.data.models.TimerStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * DataStore for persisting Pomodoro timer state
 * Allows timer recovery after app kill or restart
 */
class PomodoroTimerDataStore(private val context: Context) {

    private val Context.timerDataStore: DataStore<Preferences> by preferencesDataStore(
        name = TIMER_PREFERENCES_NAME
    )

    /**
     * Data class representing persisted timer state
     */
    data class TimerState(
        val timerStatus: TimerStatus = TimerStatus.IDLE,
        val sessionType: TimerSessionType = TimerSessionType.WORK,
        val sessionId: String? = null,
        val startTimeMillis: Long = 0L,
        val durationSeconds: Int = 0,
        val completedWorkSessions: Int = 0,
        val totalCycles: Int = 4,
        val taskName: String = "",
        val workDurationMinutes: Int = 25,
        val shortBreakDurationMinutes: Int = 5,
        val longBreakDurationMinutes: Int = 15,
        val autoStartNextSession: Boolean = true
    )

    /**
     * Flow of timer state
     */
    val timerStateFlow: Flow<TimerState> = context.timerDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            mapPreferences(preferences)
        }

    /**
     * Save timer state
     */
    suspend fun saveTimerState(state: TimerState) {
        context.timerDataStore.edit { preferences ->
            preferences[TIMER_STATUS] = state.timerStatus.name
            preferences[SESSION_TYPE] = state.sessionType.name
            preferences[SESSION_ID] = state.sessionId ?: ""
            preferences[START_TIME_MILLIS] = state.startTimeMillis
            preferences[DURATION_SECONDS] = state.durationSeconds
            preferences[COMPLETED_WORK_SESSIONS] = state.completedWorkSessions
            preferences[TOTAL_CYCLES] = state.totalCycles
            preferences[TASK_NAME] = state.taskName
        }
    }

    /**
     * Update timer durations
     */
    suspend fun updateTimerDurations(
        workDuration: Int,
        shortBreakDuration: Int,
        longBreakDuration: Int
    ) {
        context.timerDataStore.edit { preferences ->
            preferences[WORK_DURATION_MINUTES] = workDuration
            preferences[SHORT_BREAK_DURATION_MINUTES] = shortBreakDuration
            preferences[LONG_BREAK_DURATION_MINUTES] = longBreakDuration
        }
    }

    /**
     * Update total cycles setting
     */
    suspend fun updateTotalCycles(cycles: Int) {
        context.timerDataStore.edit { preferences ->
            preferences[TOTAL_CYCLES] = cycles
        }
    }

    /**
     * Update auto-start next session setting
     */
    suspend fun updateAutoStartNextSession(enabled: Boolean) {
        context.timerDataStore.edit { preferences ->
            preferences[AUTO_START_NEXT_SESSION] = enabled
        }
    }

    /**
     * Clear timer state (reset to idle)
     */
    suspend fun clearTimerState() {
        context.timerDataStore.edit { preferences ->
            preferences[TIMER_STATUS] = TimerStatus.IDLE.name
            preferences[SESSION_ID] = ""
            preferences[START_TIME_MILLIS] = 0L
            preferences[DURATION_SECONDS] = 0
            preferences[COMPLETED_WORK_SESSIONS] = 0
            preferences[TASK_NAME] = ""
        }
    }

    /**
     * Map DataStore preferences to TimerState
     */
    private fun mapPreferences(preferences: Preferences): TimerState {
        val statusString = preferences[TIMER_STATUS] ?: TimerStatus.IDLE.name
        val sessionTypeString = preferences[SESSION_TYPE] ?: TimerSessionType.WORK.name

        return TimerState(
            timerStatus = try {
                TimerStatus.valueOf(statusString)
            } catch (e: IllegalArgumentException) {
                TimerStatus.IDLE
            },
            sessionType = try {
                TimerSessionType.valueOf(sessionTypeString)
            } catch (e: IllegalArgumentException) {
                TimerSessionType.WORK
            },
            sessionId = preferences[SESSION_ID]?.takeIf { it.isNotEmpty() },
            startTimeMillis = preferences[START_TIME_MILLIS] ?: 0L,
            durationSeconds = preferences[DURATION_SECONDS] ?: 0,
            completedWorkSessions = preferences[COMPLETED_WORK_SESSIONS] ?: 0,
            totalCycles = preferences[TOTAL_CYCLES] ?: 4,
            taskName = preferences[TASK_NAME] ?: "",
            workDurationMinutes = preferences[WORK_DURATION_MINUTES] ?: 25,
            shortBreakDurationMinutes = preferences[SHORT_BREAK_DURATION_MINUTES] ?: 5,
            longBreakDurationMinutes = preferences[LONG_BREAK_DURATION_MINUTES] ?: 15,
            autoStartNextSession = preferences[AUTO_START_NEXT_SESSION] ?: true
        )
    }

    companion object {
        private const val TIMER_PREFERENCES_NAME = "pomodoro_timer_state"

        // Timer state keys
        private val TIMER_STATUS = stringPreferencesKey("timer_status")
        private val SESSION_TYPE = stringPreferencesKey("session_type")
        private val SESSION_ID = stringPreferencesKey("session_id")
        private val START_TIME_MILLIS = longPreferencesKey("start_time_millis")
        private val DURATION_SECONDS = intPreferencesKey("duration_seconds")
        private val COMPLETED_WORK_SESSIONS = intPreferencesKey("completed_work_sessions")
        private val TOTAL_CYCLES = intPreferencesKey("total_cycles")
        private val TASK_NAME = stringPreferencesKey("task_name")

        // Timer settings keys
        private val WORK_DURATION_MINUTES = intPreferencesKey("work_duration_minutes")
        private val SHORT_BREAK_DURATION_MINUTES = intPreferencesKey("short_break_duration_minutes")
        private val LONG_BREAK_DURATION_MINUTES = intPreferencesKey("long_break_duration_minutes")
        private val AUTO_START_NEXT_SESSION = booleanPreferencesKey("auto_start_next_session")
    }
}
