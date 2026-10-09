package com.oqza.myzenflow.domain.services

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi
import com.oqza.myzenflow.data.models.BreathingPhase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages haptic feedback for breathing exercises
 * Provides phase-based vibration patterns
 */
@Singleton
class HapticManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    /**
     * Vibrate for phase transition.
     *
     * The patterns follow the breath: inhale ramps up, exhale ramps down, holds are a soft
     * tick and rest is one longer pulse. Devices without amplitude control get a single pulse.
     */
    fun vibrateForPhase(phase: BreathingPhase) {
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
            return
        }
        vibrator.vibrate(phaseEffect(phase))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun phaseEffect(phase: BreathingPhase): VibrationEffect {
        val canShape = vibrator.hasAmplitudeControl()
        return when (phase) {
            BreathingPhase.INHALE -> if (canShape) {
                // Swelling: short pulses that grow stronger
                VibrationEffect.createWaveform(
                    longArrayOf(0, 30, 30, 40, 30, 60),
                    intArrayOf(0, 40, 0, 90, 0, 170),
                    -1
                )
            } else {
                VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            }
            BreathingPhase.EXHALE -> if (canShape) {
                // Releasing: strong to soft
                VibrationEffect.createWaveform(
                    longArrayOf(0, 60, 30, 40, 30, 30),
                    intArrayOf(0, 170, 0, 90, 0, 40),
                    -1
                )
            } else {
                VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            }
            BreathingPhase.HOLD_INHALE,
            BreathingPhase.HOLD_EXHALE ->
                VibrationEffect.createOneShot(25, if (canShape) 60 else VibrationEffect.DEFAULT_AMPLITUDE)
            BreathingPhase.REST ->
                VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
        }
    }

    /**
     * Vibrate for session start
     */
    fun vibrateSessionStart() {
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = VibrationEffect.createWaveform(
                longArrayOf(0, 50, 50, 50),
                intArrayOf(0, 128, 0, 128),
                -1
            )
            vibrator.vibrate(pattern)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 50, 50, 50), -1)
        }
    }

    /**
     * Vibrate for session completion
     */
    fun vibrateSessionComplete() {
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = VibrationEffect.createWaveform(
                longArrayOf(0, 100, 50, 100, 50, 100),
                intArrayOf(0, 200, 0, 200, 0, 200),
                -1
            )
            vibrator.vibrate(pattern)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 100, 50, 100, 50, 100), -1)
        }
    }

    /**
     * Cancel any ongoing vibration
     */
    fun cancel() {
        vibrator.cancel()
    }
}
