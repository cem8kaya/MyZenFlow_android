package com.oqza.myzenflow.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oqza.myzenflow.data.entities.FocusSessionEntity
import com.oqza.myzenflow.data.models.FocusMode
import com.oqza.myzenflow.data.models.PomodoroTimerState
import com.oqza.myzenflow.data.models.TimerSessionType
import com.oqza.myzenflow.data.models.TimerStatus
import com.oqza.myzenflow.data.repository.FocusRepository
import com.oqza.myzenflow.domain.services.HapticManager
import com.oqza.myzenflow.domain.services.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val focusRepository: FocusRepository,
    private val hapticManager: HapticManager,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(PomodoroTimerState())
    val uiState: StateFlow<PomodoroTimerState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var sessionStartTime: LocalDateTime? = null
    private var totalWorkDurationSeconds: Int = 0

    init {
        // Initialize with default Pomodoro mode
        selectMode(FocusMode.POMODORO)
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
            _uiState.value = currentState.copy(
                sessionId = sessionId,
                sessionStartTime = System.currentTimeMillis()
            )
        }

        // Provide haptic feedback
        hapticManager.vibrateSessionStart()

        // Update state to running
        _uiState.value = _uiState.value.copy(
            timerStatus = TimerStatus.RUNNING,
            isBackgroundTimerActive = true
        )

        // Start the countdown
        startCountdown()
    }

    /**
     * Pause the timer
     */
    fun pauseTimer() {
        timerJob?.cancel()
        hapticManager.vibrateForPhase(com.oqza.myzenflow.data.models.BreathingPhase.REST)

        _uiState.value = _uiState.value.copy(
            timerStatus = TimerStatus.PAUSED,
            isBackgroundTimerActive = false
        )

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

        hapticManager.vibrateForPhase(com.oqza.myzenflow.data.models.BreathingPhase.INHALE)

        _uiState.value = _uiState.value.copy(
            timerStatus = TimerStatus.RUNNING,
            isBackgroundTimerActive = true
        )

        startCountdown()
    }

    /**
     * Stop the timer and save session
     */
    fun stopTimer() {
        timerJob?.cancel()

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

        totalWorkDurationSeconds = 0
        sessionStartTime = null
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
     * Start the countdown loop
     */
    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _uiState.value.timeRemainingSeconds > 0) {
                delay(1000) // Update every second

                val currentState = _uiState.value
                val newTimeRemaining = currentState.timeRemainingSeconds - 1

                // Track work duration for session saving
                if (currentState.currentSessionType == TimerSessionType.WORK) {
                    totalWorkDurationSeconds++
                }

                // Update progress
                val newProgress = currentState.calculateProgress()

                _uiState.value = currentState.copy(
                    timeRemainingSeconds = newTimeRemaining,
                    progress = newProgress
                )

                // Update progress notification every 10 seconds
                if (newTimeRemaining % 10 == 0) {
                    notificationHelper.showProgressNotification(
                        sessionType = currentState.currentSessionType,
                        timeRemaining = _uiState.value.formatTime(),
                        progress = newProgress,
                        isPaused = false
                    )
                }

                // Check if session completed
                if (newTimeRemaining <= 0) {
                    onSessionCompleted()
                }
            }
        }
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
            } catch (e: Exception) {
                // Handle error silently or log
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        notificationHelper.cancelProgressNotification()
    }
}
