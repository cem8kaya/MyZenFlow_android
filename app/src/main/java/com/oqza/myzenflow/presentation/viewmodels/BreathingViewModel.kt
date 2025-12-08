package com.oqza.myzenflow.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oqza.myzenflow.data.entities.BreathingSessionEntity
import com.oqza.myzenflow.data.models.BreathingExerciseType
import com.oqza.myzenflow.data.models.BreathingPhase
import com.oqza.myzenflow.data.repository.BreathingRepository
import com.oqza.myzenflow.domain.services.BreathingAudioManager
import com.oqza.myzenflow.domain.services.HapticManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.currentCoroutineContext
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for breathing exercise screen
 * Manages breathing exercise state, timing, and session tracking
 */
@HiltViewModel
class BreathingViewModel @Inject constructor(
    private val breathingRepository: BreathingRepository,
    private val hapticManager: HapticManager,
    private val audioManager: BreathingAudioManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BreathingUiState())
    val uiState: StateFlow<BreathingUiState> = _uiState.asStateFlow()

    private var exerciseJob: Job? = null
    private var sessionStartTime: LocalDateTime? = null

    /**
     * Select a breathing exercise
     */
    fun selectExercise(exercise: BreathingExerciseType) {
        if (_uiState.value.isActive) {
            stopExercise()
        }
        _uiState.value = _uiState.value.copy(
            selectedExercise = exercise,
            currentCycle = 0,
            currentPhase = BreathingPhase.REST,
            phaseProgress = 0f,
            totalProgress = 0f
        )
    }

    /**
     * Start the breathing exercise
     */
    fun startExercise() {
        val exercise = _uiState.value.selectedExercise ?: return

        if (_uiState.value.isActive) return

        sessionStartTime = LocalDateTime.now()
        hapticManager.vibrateSessionStart()

        if (_uiState.value.soundEnabled) {
            audioManager.playAmbientSound(
                BreathingAudioManager.AmbientSound.OCEAN_WAVES,
                volume = 0.3f
            )
        }

        _uiState.value = _uiState.value.copy(
            isActive = true,
            isPaused = false,
            currentCycle = 0,
            currentPhase = BreathingPhase.INHALE,
            phaseProgress = 0f,
            totalProgress = 0f
        )

        startExerciseLoop(exercise)
    }

    /**
     * Pause the breathing exercise
     */
    fun pauseExercise() {
        exerciseJob?.cancel()
        audioManager.pauseAmbientSound()
        _uiState.value = _uiState.value.copy(
            isPaused = true
        )
    }

    /**
     * Resume the breathing exercise
     */
    fun resumeExercise() {
        val exercise = _uiState.value.selectedExercise ?: return
        if (!_uiState.value.isActive) return

        if (_uiState.value.soundEnabled) {
            audioManager.resumeAmbientSound(volume = 0.3f)
        }

        _uiState.value = _uiState.value.copy(
            isPaused = false
        )

        continueExerciseFromCurrentPhase(exercise)
    }

    /**
     * Stop the breathing exercise
     */
    fun stopExercise() {
        exerciseJob?.cancel()
        audioManager.stopAmbientSound()
        hapticManager.cancel()

        val wasActive = _uiState.value.isActive
        val cyclesCompleted = _uiState.value.currentCycle

        _uiState.value = _uiState.value.copy(
            isActive = false,
            isPaused = false,
            currentPhase = BreathingPhase.REST,
            phaseProgress = 0f
        )

        if (wasActive && cyclesCompleted > 0) {
            saveSession(completed = false)
        }
    }

    /**
     * Toggle haptic feedback
     */
    fun toggleHapticFeedback() {
        _uiState.value = _uiState.value.copy(
            hapticEnabled = !_uiState.value.hapticEnabled
        )
    }

    /**
     * Toggle sound
     */
    fun toggleSound() {
        val newSoundEnabled = !_uiState.value.soundEnabled
        _uiState.value = _uiState.value.copy(
            soundEnabled = newSoundEnabled
        )

        if (_uiState.value.isActive) {
            if (newSoundEnabled) {
                audioManager.playAmbientSound(
                    BreathingAudioManager.AmbientSound.OCEAN_WAVES,
                    volume = 0.3f
                )
            } else {
                audioManager.stopAmbientSound()
            }
        }
    }

    /**
     * Start the breathing exercise loop
     */
    private fun startExerciseLoop(exercise: BreathingExerciseType) {
        exerciseJob?.cancel()
        exerciseJob = viewModelScope.launch {
            repeat(exercise.cycles) { cycleIndex ->
                if (!isActive) return@launch

                _uiState.value = _uiState.value.copy(currentCycle = cycleIndex + 1)

                // Inhale phase
                executePhase(BreathingPhase.INHALE, exercise.inhaleSeconds, exercise)

                if (!isActive) return@launch

                // Hold inhale phase
                if (exercise.holdInhaleSeconds > 0) {
                    executePhase(BreathingPhase.HOLD_INHALE, exercise.holdInhaleSeconds, exercise)
                }

                if (!isActive) return@launch

                // Exhale phase
                executePhase(BreathingPhase.EXHALE, exercise.exhaleSeconds, exercise)

                if (!isActive) return@launch

                // Hold exhale phase
                if (exercise.holdExhaleSeconds > 0) {
                    executePhase(BreathingPhase.HOLD_EXHALE, exercise.holdExhaleSeconds, exercise)
                }
            }

            // Exercise completed
            if (isActive) {
                completeExercise()
            }
        }
    }

    /**
     * Continue exercise from current phase (for resume)
     */
    private fun continueExerciseFromCurrentPhase(exercise: BreathingExerciseType) {
        exerciseJob?.cancel()
        exerciseJob = viewModelScope.launch {
            val currentCycle = _uiState.value.currentCycle
            val currentPhase = _uiState.value.currentPhase
            val currentProgress = _uiState.value.phaseProgress

            // Calculate remaining duration for current phase
            val phaseDuration = when (currentPhase) {
                BreathingPhase.INHALE -> exercise.inhaleSeconds
                BreathingPhase.HOLD_INHALE -> exercise.holdInhaleSeconds
                BreathingPhase.EXHALE -> exercise.exhaleSeconds
                BreathingPhase.HOLD_EXHALE -> exercise.holdExhaleSeconds
                BreathingPhase.REST -> 0
            }

            val remainingSeconds = (phaseDuration * (1f - currentProgress)).toInt()

            // Continue current phase
            if (remainingSeconds > 0) {
                executePhase(currentPhase, remainingSeconds, exercise, startProgress = currentProgress)
            }

            // Continue with remaining cycles
            for (cycleIndex in currentCycle until exercise.cycles) {
                if (!isActive) return@launch
                _uiState.value = _uiState.value.copy(currentCycle = cycleIndex + 1)

                // Execute remaining phases of current cycle
                val phases = getPhaseSequence(exercise, currentPhase)
                for (phase in phases) {
                    if (!isActive) return@launch
                    val duration = getPhaseDuration(phase, exercise)
                    if (duration > 0) {
                        executePhase(phase, duration, exercise)
                    }
                }
            }

            if (isActive) {
                completeExercise()
            }
        }
    }

    /**
     * Execute a single breathing phase
     */
    private suspend fun executePhase(
        phase: BreathingPhase,
        durationSeconds: Int,
        exercise: BreathingExerciseType,
        startProgress: Float = 0f
    ) {
        _uiState.value = _uiState.value.copy(
            currentPhase = phase,
            phaseProgress = startProgress
        )

        if (_uiState.value.hapticEnabled && startProgress == 0f) {
            hapticManager.vibrateForPhase(phase)
        }

        val updateIntervalMs = 16L // ~60 FPS
        val totalDurationMs = durationSeconds * 1000L
        val startProgressMs = (startProgress * totalDurationMs).toLong()

        var elapsedMs = startProgressMs

        while (elapsedMs < totalDurationMs && currentCoroutineContext().isActive && !_uiState.value.isPaused) {
            delay(updateIntervalMs)
            elapsedMs += updateIntervalMs

            val progress = (elapsedMs.toFloat() / totalDurationMs).coerceIn(0f, 1f)
            val totalProgress = calculateTotalProgress(exercise, _uiState.value.currentCycle, phase, progress)

            _uiState.value = _uiState.value.copy(
                phaseProgress = progress,
                totalProgress = totalProgress
            )
        }
    }

    /**
     * Calculate total progress across all cycles
     */
    private fun calculateTotalProgress(
        exercise: BreathingExerciseType,
        currentCycle: Int,
        currentPhase: BreathingPhase,
        phaseProgress: Float
    ): Float {
        val totalCycles = exercise.cycles
        val cycleDuration = exercise.cycleDurationSeconds

        // Progress of completed cycles
        val completedCyclesProgress = (currentCycle - 1).toFloat() / totalCycles

        // Progress within current cycle
        var currentCycleProgress = 0f

        when (currentPhase) {
            BreathingPhase.INHALE -> {
                currentCycleProgress = (exercise.inhaleSeconds * phaseProgress) / cycleDuration
            }
            BreathingPhase.HOLD_INHALE -> {
                val inhaleProgress = exercise.inhaleSeconds.toFloat() / cycleDuration
                val holdProgress = (exercise.holdInhaleSeconds * phaseProgress) / cycleDuration
                currentCycleProgress = inhaleProgress + holdProgress
            }
            BreathingPhase.EXHALE -> {
                val inhaleProgress = exercise.inhaleSeconds.toFloat() / cycleDuration
                val holdInhaleProgress = exercise.holdInhaleSeconds.toFloat() / cycleDuration
                val exhaleProgress = (exercise.exhaleSeconds * phaseProgress) / cycleDuration
                currentCycleProgress = inhaleProgress + holdInhaleProgress + exhaleProgress
            }
            BreathingPhase.HOLD_EXHALE -> {
                val inhaleProgress = exercise.inhaleSeconds.toFloat() / cycleDuration
                val holdInhaleProgress = exercise.holdInhaleSeconds.toFloat() / cycleDuration
                val exhaleProgress = exercise.exhaleSeconds.toFloat() / cycleDuration
                val holdExhaleProgress = (exercise.holdExhaleSeconds * phaseProgress) / cycleDuration
                currentCycleProgress = inhaleProgress + holdInhaleProgress + exhaleProgress + holdExhaleProgress
            }
            BreathingPhase.REST -> currentCycleProgress = 0f
        }

        currentCycleProgress /= totalCycles

        return (completedCyclesProgress + currentCycleProgress).coerceIn(0f, 1f)
    }

    /**
     * Get remaining phases after current phase
     */
    private fun getPhaseSequence(exercise: BreathingExerciseType, currentPhase: BreathingPhase): List<BreathingPhase> {
        val allPhases = mutableListOf<BreathingPhase>()

        when (currentPhase) {
            BreathingPhase.INHALE -> {
                if (exercise.holdInhaleSeconds > 0) allPhases.add(BreathingPhase.HOLD_INHALE)
                allPhases.add(BreathingPhase.EXHALE)
                if (exercise.holdExhaleSeconds > 0) allPhases.add(BreathingPhase.HOLD_EXHALE)
            }
            BreathingPhase.HOLD_INHALE -> {
                allPhases.add(BreathingPhase.EXHALE)
                if (exercise.holdExhaleSeconds > 0) allPhases.add(BreathingPhase.HOLD_EXHALE)
            }
            BreathingPhase.EXHALE -> {
                if (exercise.holdExhaleSeconds > 0) allPhases.add(BreathingPhase.HOLD_EXHALE)
            }
            else -> {}
        }

        return allPhases
    }

    /**
     * Get duration for a specific phase
     */
    private fun getPhaseDuration(phase: BreathingPhase, exercise: BreathingExerciseType): Int {
        return when (phase) {
            BreathingPhase.INHALE -> exercise.inhaleSeconds
            BreathingPhase.HOLD_INHALE -> exercise.holdInhaleSeconds
            BreathingPhase.EXHALE -> exercise.exhaleSeconds
            BreathingPhase.HOLD_EXHALE -> exercise.holdExhaleSeconds
            BreathingPhase.REST -> 0
        }
    }

    /**
     * Complete the exercise
     */
    private fun completeExercise() {
        hapticManager.vibrateSessionComplete()
        audioManager.stopAmbientSound()

        _uiState.value = _uiState.value.copy(
            isActive = false,
            isPaused = false,
            currentPhase = BreathingPhase.REST,
            phaseProgress = 0f,
            totalProgress = 1f
        )

        saveSession(completed = true)
    }

    /**
     * Save session to database
     */
    private fun saveSession(completed: Boolean) {
        val exercise = _uiState.value.selectedExercise ?: return
        val startTime = sessionStartTime ?: return
        val cyclesCompleted = _uiState.value.currentCycle

        viewModelScope.launch {
            val session = BreathingSessionEntity(
                id = UUID.randomUUID().toString(),
                date = startTime,
                exerciseId = exercise.id,
                exerciseName = exercise.displayName,
                durationSeconds = (LocalDateTime.now().toEpochSecond(java.time.ZoneOffset.UTC) -
                                  startTime.toEpochSecond(java.time.ZoneOffset.UTC)).toInt(),
                cyclesCompleted = cyclesCompleted,
                totalCycles = exercise.cycles,
                inhaleSeconds = exercise.inhaleSeconds,
                holdInhaleSeconds = exercise.holdInhaleSeconds,
                exhaleSeconds = exercise.exhaleSeconds,
                holdExhaleSeconds = exercise.holdExhaleSeconds,
                completed = completed
            )

            breathingRepository.insertSession(session)
        }
    }

    override fun onCleared() {
        super.onCleared()
        exerciseJob?.cancel()
        audioManager.release()
        hapticManager.cancel()
    }
}

/**
 * UI state for breathing exercise
 */
data class BreathingUiState(
    val selectedExercise: BreathingExerciseType? = null,
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val currentCycle: Int = 0,
    val currentPhase: BreathingPhase = BreathingPhase.REST,
    val phaseProgress: Float = 0f,
    val totalProgress: Float = 0f,
    val hapticEnabled: Boolean = true,
    val soundEnabled: Boolean = true
)
