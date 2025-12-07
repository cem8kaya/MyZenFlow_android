package com.oqza.myzenflow.domain.services

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
     * Vibrate for phase transition
     */
    fun vibrateForPhase(phase: BreathingPhase) {
        if (!vibrator.hasVibrator()) return

        val pattern = when (phase) {
            BreathingPhase.INHALE -> {
                // Gentle increase for inhale
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    null
                }
            }
            BreathingPhase.HOLD_INHALE -> {
                // Short pulse for hold
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    VibrationEffect.createOneShot(30, 128)
                } else {
                    null
                }
            }
            BreathingPhase.EXHALE -> {
                // Gentle decrease for exhale
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    null
                }
            }
            BreathingPhase.HOLD_EXHALE -> {
                // Short pulse for hold
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    VibrationEffect.createOneShot(30, 128)
                } else {
                    null
                }
            }
            BreathingPhase.REST -> {
                // Longer pulse for rest/completion
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    null
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && pattern != null) {
            vibrator.vibrate(pattern)
        } else {
            // Fallback for older devices
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
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
