package com.oqza.myzenflow.presentation.viewmodels

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oqza.myzenflow.data.datastore.PomodoroTimerDataStore
import com.oqza.myzenflow.data.entities.FocusSessionEntity
import com.oqza.myzenflow.data.models.FocusMode
import com.oqza.myzenflow.data.models.PomodoroTimerState
import com.oqza.myzenflow.data.models.TimerSessionType
import com.oqza.myzenflow.data.models.TimerStatus
import com.oqza.myzenflow.data.repository.FocusRepository
import com.oqza.myzenflow.domain.services.HapticManager
import com.oqza.myzenflow.domain.services.NotificationHelper
import com.oqza.myzenflow.domain.services.PomodoroTimerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for Pomodoro timer screen
 * Manages timer state, cycle transitions, and session tracking
 */
@HiltViewModel
class PomodoroViewModel @Inject constructor(
    application: Application,
    private val focusRepository: FocusRepository,
    private val hapticManager: HapticManager,
    private val notificationHelper: NotificationHelper,
    private val timerDataStore: PomodoroTimerDataStore
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PomodoroTimerState())
    val uiState: StateFlow<PomodoroTimerState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var sessionStartTime: LocalDateTime? = null
    private var totalWorkDurationSeconds: Int = 0
    private var targetCompletionTime: Long = 0L

    // Flow for today's completed sessions
    private val _todaysSessions = MutableStateFlow<List<com.oqza.myzenflow.data.models.FocusSessionData>>(emptyList())
    val todaysSessions: StateFlow<List<com.oqza.myzenflow.data.models.FocusSessionData>> = _todaysSessions.asStateFlow()

    // Stats for today
    private val _todaysStats = MutableStateFlow(TodaysStats())
    val todaysStats: StateFlow<TodaysStats> = _todaysStats.asStateFlow()

    // BroadcastReceiver for timer updates from service
    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                PomodoroTimerService.BROADCAST_TIMER_TICK -> {
                    val timeRemaining = intent.getIntExtra(PomodoroTimerService.EXTRA_TIME_REMAINING, 0)
                    val progress = intent.getFloatExtra(PomodoroTimerService.EXTRA_PROGRESS, 0f)

                    _uiState.value = _uiState.value.copy(
                        timeRemainingSeconds = timeRemaining,
                        progress = progress
                    )

                    // Track work duration for session saving
                    if (_uiState.value.currentSessionType == TimerSessionType.WORK) {
                        totalWorkDurationSeconds++
                    }
                }
                PomodoroTimerService.BROADCAST_TIMER_COMPLETE -> {
                    onSessionCompleted()
                }
            }
        }
    }

    data class TodaysStats(
        val completedWorkSessions: Int = 0,
        val totalFocusMinutes: Int = 0,
        val currentStreak: Int = 0
    )

    init {
        // Initialize with default Pomodoro mode
        selectMode(FocusMode.POMODORO)

        // Register broadcast receiver for timer updates
        val filter = IntentFilter().apply {
            addAction(PomodoroTimerService.BROADCAST_TIMER_TICK)
            addAction(PomodoroTimerService.BROADCAST_TIMER_COMPLETE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getApplication<Application>().registerReceiver(timerReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            getApplication<Application>().registerReceiver(timerReceiver, filter)
        }

        // Load persisted timer state
        recoverTimerState()

        // Load today's sessions
        loadTodaysSessions()

        // Load timer settings from DataStore
        loadTimerSettings()
    }

    /**
     * Load timer settings from DataStore
     */
    private fun loadTimerSettings() {
        viewModelScope.launch {
            timerDataStore.timerStateFlow.collect { persistedState ->
                if (_uiState.value.timerStatus == TimerStatus.IDLE) {
                    _uiState.value = _uiState.value.copy(
                        customFocusDuration = persistedState.workDurationMinutes,
                        customBreakDuration = persistedState.shortBreakDurationMinutes,
                        customLongBreakDuration = persistedState.longBreakDurationMinutes,
                        totalCycles = persistedState.totalCycles
                    )
                }
            }
        }
    }

    /**
     * Recover timer state from DataStore (app restart/kill recovery)
     */
    private fun recoverTimerState() {
        viewModelScope.launch {
            val persistedState = timerDataStore.timerStateFlow.first()

            if (persistedState.timerStatus == TimerStatus.RUNNING &&
                persistedState.targetCompletionTimeMillis > 0) {

                // Calculate remaining time dynamically from targetCompletionTime
                val currentTime = System.currentTimeMillis()
                val remainingMillis = persistedState.targetCompletionTimeMillis - currentTime
                val remainingSeconds = (remainingMillis / 1000).toInt()

                if (remainingSeconds > 0) {
                    // Timer is still valid, restore state
                    val elapsedSeconds = ((currentTime - persistedState.startTimeMillis) / 1000).toInt()

                    _uiState.value = _uiState.value.copy(
                        timerStatus = TimerStatus.RUNNING,
                        currentSessionType = persistedState.sessionType,
                        sessionId = persistedState.sessionId,
                        sessionStartTime = persistedState.startTimeMillis,
                        timeRemainingSeconds = remainingSeconds,
                        totalDurationSeconds = persistedState.durationSeconds,
                        completedWorkSessions = persistedState.completedWorkSessions,
                        totalCycles = persistedState.totalCycles,
                        taskName = persistedState.taskName,
                        progress = 1f - (remainingSeconds.toFloat() / persistedState.durationSeconds.toFloat())
                    )

                    targetCompletionTime = persistedState.targetCompletionTimeMillis
                    totalWorkDurationSeconds = elapsedSeconds

                    // Restart the foreground service
                    startForegroundService()
                } else {
                    // Timer expired while app was closed, complete the session
                    timerDataStore.clearTimerState()
                }
            }
        }
    }

    /**
     * Load today's sessions from database
     */
    private fun loadTodaysSessions() {
        viewModelScope.launch {
            val startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0)
            val endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59)

            val sessions = focusRepository.getSessionsForDay(startOfDay, endOfDay)
            _todaysSessions.value = sessions

            // Calculate stats
            val completedSessions = sessions.count { it.completed }
            val totalMinutes = sessions.sumOf { it.duration } / 60

            _todaysStats.value = TodaysStats(
                completedWorkSessions = completedSessions,
                totalFocusMinutes = totalMinutes,
                currentStreak = calculateStreak(sessions)
            )
        }
    }

    /**
     * Calculate current streak
     */
    private fun calculateStreak(sessions: List<com.oqza.myzenflow.data.models.FocusSessionData>): Int {
        val completedSessions = sessions.filter { it.completed }.sortedBy { it.date }
        if (completedSessions.isEmpty()) return 0

        var streak = 0
        for (session in completedSessions.reversed()) {
            if (session.completed) {
                streak++
            } else {
                break
            }
        }
        return streak
    }

    /**
     * Select a focus mode (Pomodoro, Short Focus, Long Focus, Custom)
     */
    fun selectMode(mode: FocusMode) {
        if (_uiState.value.timerStatus == TimerStatus.RUNNING) {
            stopTimer()
        }

        val focusDuration = if (mode == FocusMode.CUSTOM) {
            _uiState.value.customFocusDuration
        } else {
            mode.focusDuration
        }

        _uiState.value = _uiState.value.copy(
            selectedMode = mode,
            currentSessionType = TimerSessionType.WORK,
            timerStatus = TimerStatus.IDLE,
            timeRemainingSeconds = focusDuration * 60,
            totalDurationSeconds = focusDuration * 60,
            currentCycle = 0,
            completedWorkSessions = 0,
            progress = 0f
        )
    }

    /**
     * Set custom duration for focus session
     */
    fun setCustomFocusDuration(minutes: Int) {
        if (minutes in 1..120) {
            _uiState.value = _uiState.value.copy(customFocusDuration = minutes)
            if (_uiState.value.selectedMode == FocusMode.CUSTOM && _uiState.value.timerStatus == TimerStatus.IDLE) {
                _uiState.value = _uiState.value.copy(
                    timeRemainingSeconds = minutes * 60,
                    totalDurationSeconds = minutes * 60
                )
            }
        }
    }

    /**
     * Set custom duration for break session
     */
    fun setCustomBreakDuration(minutes: Int) {
        if (minutes in 1..30) {
            _uiState.value = _uiState.value.copy(customBreakDuration = minutes)
        }
    }

    /**
     * Set custom duration for long break
     */
    fun setCustomLongBreakDuration(minutes: Int) {
        if (minutes in 1..60) {
            _uiState.value = _uiState.value.copy(customLongBreakDuration = minutes)
        }
    }

    /**
     * Set task name
     */
    fun setTaskName(name: String) {
        _uiState.value = _uiState.value.copy(taskName = name)
    }

    /**
     * Set total cycles before long break
     */
    fun setTotalCycles(cycles: Int) {
        if (cycles in 1..10) {
            _uiState.value = _uiState.value.copy(totalCycles = cycles)
            viewModelScope.launch {
                timerDataStore.updateTotalCycles(cycles)
            }
        }
    }

    /**
     * Save timer durations to DataStore
     */
    fun saveTimerDurations(workMinutes: Int, shortBreakMinutes: Int, longBreakMinutes: Int) {
        viewModelScope.launch {
            timerDataStore.updateTimerDurations(
                workDuration = workMinutes,
                shortBreakDuration = shortBreakMinutes,
                longBreakDuration = longBreakMinutes
            )
            // Update current state
            _uiState.value = _uiState.value.copy(
                customFocusDuration = workMinutes,
                customBreakDuration = shortBreakMinutes,
                customLongBreakDuration = longBreakMinutes
            )
        }
    }

    /**
     * Skip to next session
     */
    fun skipToNextSession() {
        if (_uiState.value.timerStatus == TimerStatus.IDLE) return

        // Stop foreground service
        stopForegroundService()

        val currentState = _uiState.value

        // Determine next session
        when (currentState.currentSessionType) {
            TimerSessionType.WORK -> {
                val newCompletedWorkSessions = currentState.completedWorkSessions + 1
                val shouldTakeLongBreak = newCompletedWorkSessions % currentState.totalCycles == 0
                val nextSessionType = if (shouldTakeLongBreak) {
                    TimerSessionType.LONG_BREAK
                } else {
                    TimerSessionType.SHORT_BREAK
                }

                transitionToSession(
                    sessionType = nextSessionType,
                    completedWorkSessions = newCompletedWorkSessions
                )
            }
            TimerSessionType.SHORT_BREAK, TimerSessionType.LONG_BREAK -> {
                transitionToSession(
                    sessionType = TimerSessionType.WORK,
                    completedWorkSessions = currentState.completedWorkSessions
                )
            }
        }
    }

    /**
     * Start the timer
     */
    fun startTimer() {
        if (_uiState.value.timerStatus == TimerStatus.RUNNING) return

        val currentState = _uiState.value

        // If starting fresh, mark session start time
        if (currentState.timerStatus == TimerStatus.IDLE) {
            sessionStartTime = LocalDateTime.now()
            totalWorkDurationSeconds = 0

            val sessionId = UUID.randomUUID().toString()
            val startTimeMillis = System.currentTimeMillis()

            _uiState.value = currentState.copy(
                sessionId = sessionId,
                sessionStartTime = startTimeMillis
            )

            targetCompletionTime = startTimeMillis + (currentState.timeRemainingSeconds * 1000L)
        } else if (currentState.timerStatus == TimerStatus.PAUSED) {
            // Resume from pause - recalculate target completion time
            targetCompletionTime = System.currentTimeMillis() + (currentState.timeRemainingSeconds * 1000L)
        }

        // Provide haptic feedback
        hapticManager.vibrateSessionStart()

        // Update state to running
        _uiState.value = _uiState.value.copy(
            timerStatus = TimerStatus.RUNNING,
            isBackgroundTimerActive = true
        )

        // Persist state to DataStore (only when starting)
        persistTimerState()

        // Start foreground service
        startForegroundService()
    }

    /**
     * Persist timer state to DataStore
     */
    private fun persistTimerState() {
        viewModelScope.launch {
            val currentState = _uiState.value
            timerDataStore.saveTimerState(
                PomodoroTimerDataStore.TimerState(
                    timerStatus = currentState.timerStatus,
                    sessionType = currentState.currentSessionType,
                    sessionId = currentState.sessionId,
                    startTimeMillis = currentState.sessionStartTime,
                    targetCompletionTimeMillis = targetCompletionTime,
                    durationSeconds = currentState.totalDurationSeconds,
                    completedWorkSessions = currentState.completedWorkSessions,
                    totalCycles = currentState.totalCycles,
                    taskName = currentState.taskName,
                    workDurationMinutes = currentState.customFocusDuration,
                    shortBreakDurationMinutes = currentState.customBreakDuration,
                    longBreakDurationMinutes = currentState.customLongBreakDuration
                )
            )
        }
    }

    /**
     * Start foreground service
     */
    private fun startForegroundService() {
        val currentState = _uiState.value
        val context = getApplication<Application>()

        val intent = Intent(context, PomodoroTimerService::class.java).apply {
            action = PomodoroTimerService.ACTION_START
            putExtra(PomodoroTimerService.EXTRA_TARGET_TIME, targetCompletionTime)
            putExtra(PomodoroTimerService.EXTRA_SESSION_TYPE, currentState.currentSessionType.name)
            putExtra(PomodoroTimerService.EXTRA_TOTAL_DURATION, currentState.totalDurationSeconds)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    /**
     * Stop foreground service
     */
    private fun stopForegroundService() {
        val context = getApplication<Application>()
        val intent = Intent(context, PomodoroTimerService::class.java).apply {
            action = PomodoroTimerService.ACTION_STOP
        }
        context.startService(intent)
    }

    /**
     * Pause the timer
     */
    fun pauseTimer() {
        // Stop foreground service
        stopForegroundService()

        hapticManager.vibrateForPhase(com.oqza.myzenflow.data.models.BreathingPhase.REST)

        _uiState.value = _uiState.value.copy(
            timerStatus = TimerStatus.PAUSED,
            isBackgroundTimerActive = false
        )

        // Persist paused state (only when pausing)
        persistTimerState()

        // Update progress notification
        val currentState = _uiState.value
        notificationHelper.showProgressNotification(
            sessionType = currentState.currentSessionType,
            timeRemaining = currentState.formatTime(),
            progress = currentState.calculateProgress(),
            isPaused = true
        )
    }

    /**
     * Resume the timer
     */
    fun resumeTimer() {
        if (_uiState.value.timerStatus != TimerStatus.PAUSED) return

        // Recalculate target completion time based on remaining time
        targetCompletionTime = System.currentTimeMillis() + (_uiState.value.timeRemainingSeconds * 1000L)

        hapticManager.vibrateForPhase(com.oqza.myzenflow.data.models.BreathingPhase.INHALE)

        _uiState.value = _uiState.value.copy(
            timerStatus = TimerStatus.RUNNING,
            isBackgroundTimerActive = true
        )

        // Persist state (only when resuming)
        persistTimerState()

        // Restart foreground service
        startForegroundService()
    }

    /**
     * Stop the timer and save session
     */
    fun stopTimer() {
        // Stop foreground service
        stopForegroundService()

        val currentState = _uiState.value
        val wasRunning = currentState.timerStatus == TimerStatus.RUNNING

        // Save session if any work was done
        if (wasRunning && totalWorkDurationSeconds > 0) {
            saveSession(interrupted = true)
        }

        // Cancel notifications
        notificationHelper.cancelProgressNotification()

        // Reset to idle state
        val focusDuration = currentState.getCurrentFocusDurationSeconds()
        _uiState.value = currentState.copy(
            timerStatus = TimerStatus.IDLE,
            timeRemainingSeconds = focusDuration,
            totalDurationSeconds = focusDuration,
            currentSessionType = TimerSessionType.WORK,
            currentCycle = 0,
            completedWorkSessions = 0,
            progress = 0f,
            isBackgroundTimerActive = false,
            sessionId = null
        )

        // Clear persisted state (only when stopping)
        viewModelScope.launch {
            timerDataStore.clearTimerState()
        }

        totalWorkDurationSeconds = 0
        sessionStartTime = null
        targetCompletionTime = 0L

        // Reload today's sessions
        loadTodaysSessions()
    }

    /**
     * Toggle haptic feedback
     */
    fun toggleHaptic() {
        _uiState.value = _uiState.value.copy(
            hapticEnabled = !_uiState.value.hapticEnabled
        )
    }

    /**
     * Toggle sound
     */
    fun toggleSound() {
        _uiState.value = _uiState.value.copy(
            soundEnabled = !_uiState.value.soundEnabled
        )
    }


    /**
     * Handle session completion and transition to next session
     */
    private fun onSessionCompleted() {
        val currentState = _uiState.value

        // Provide haptic feedback
        if (currentState.hapticEnabled) {
            hapticManager.vibrateSessionComplete()
        }

        // Handle transitions based on current session type
        when (currentState.currentSessionType) {
            TimerSessionType.WORK -> {
                // Work session completed
                val newCompletedWorkSessions = currentState.completedWorkSessions + 1

                // Determine next session type
                val shouldTakeLongBreak = newCompletedWorkSessions % currentState.totalCycles == 0
                val nextSessionType = if (shouldTakeLongBreak) {
                    TimerSessionType.LONG_BREAK
                } else {
                    TimerSessionType.SHORT_BREAK
                }

                // Show completion notification
                notificationHelper.showTimerCompletionNotification(
                    sessionType = TimerSessionType.WORK,
                    nextSessionType = nextSessionType
                )

                // Transition to break
                transitionToSession(
                    sessionType = nextSessionType,
                    completedWorkSessions = newCompletedWorkSessions
                )
            }

            TimerSessionType.SHORT_BREAK, TimerSessionType.LONG_BREAK -> {
                // Break session completed
                notificationHelper.showTimerCompletionNotification(
                    sessionType = currentState.currentSessionType,
                    nextSessionType = TimerSessionType.WORK
                )

                // Check if we completed all cycles
                if (currentState.completedWorkSessions >= currentState.totalCycles * 4) {
                    // All cycles completed, save session and reset
                    saveSession(interrupted = false)
                    stopTimer()
                } else {
                    // Transition back to work
                    transitionToSession(
                        sessionType = TimerSessionType.WORK,
                        completedWorkSessions = currentState.completedWorkSessions
                    )
                }
            }
        }
    }

    /**
     * Transition to a new session type
     */
    private fun transitionToSession(sessionType: TimerSessionType, completedWorkSessions: Int) {
        val duration = when (sessionType) {
            TimerSessionType.WORK -> _uiState.value.getCurrentFocusDurationSeconds()
            TimerSessionType.SHORT_BREAK -> _uiState.value.getCurrentBreakDurationSeconds()
            TimerSessionType.LONG_BREAK -> _uiState.value.getCurrentLongBreakDurationSeconds()
        }

        _uiState.value = _uiState.value.copy(
            currentSessionType = sessionType,
            timeRemainingSeconds = duration,
            totalDurationSeconds = duration,
            completedWorkSessions = completedWorkSessions,
            progress = 0f,
            timerStatus = TimerStatus.IDLE
        )

        // Auto-start next session after a brief pause
        viewModelScope.launch {
            delay(2000) // 2 second pause before auto-starting
            if (_uiState.value.timerStatus == TimerStatus.IDLE) {
                startTimer()
            }
        }
    }

    /**
     * Save the focus session to database
     */
    private fun saveSession(interrupted: Boolean) {
        val currentState = _uiState.value
        val startTime = sessionStartTime ?: return

        viewModelScope.launch {
            try {
                val session = FocusSessionEntity(
                    id = currentState.sessionId ?: UUID.randomUUID().toString(),
                    date = startTime,
                    duration = totalWorkDurationSeconds,
                    focusDuration = currentState.getCurrentFocusDurationSeconds(),
                    breakDuration = currentState.getCurrentBreakDurationSeconds(),
                    completedCycles = currentState.completedWorkSessions,
                    targetCycles = currentState.totalCycles,
                    taskName = currentState.taskName.ifBlank { null },
                    completed = !interrupted,
                    interrupted = interrupted
                )

                focusRepository.insertSession(session.toFocusSessionData())

                // Reload today's sessions after saving
                loadTodaysSessions()
            } catch (e: Exception) {
                // Handle error silently or log
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Unregister broadcast receiver
        try {
            getApplication<Application>().unregisterReceiver(timerReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver already unregistered
        }
        // Don't stop the foreground service here - let it continue in background
    }
}
