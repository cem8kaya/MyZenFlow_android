package com.oqza.myzenflow.presentation.screens.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oqza.myzenflow.data.models.BreathingExerciseType
import com.oqza.myzenflow.data.models.BreathingPhase

/**
 * Timer display with smooth countdown animation
 * Shows remaining time in current phase
 */
@Composable
fun TimerDisplay(
    phase: BreathingPhase,
    phaseProgress: Float,
    exercise: BreathingExerciseType?,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (exercise == null || !isActive) return

    val phaseDuration = when (phase) {
        BreathingPhase.INHALE -> exercise.inhaleSeconds
        BreathingPhase.HOLD_INHALE -> exercise.holdInhaleSeconds
        BreathingPhase.EXHALE -> exercise.exhaleSeconds
        BreathingPhase.HOLD_EXHALE -> exercise.holdExhaleSeconds
        BreathingPhase.REST -> 0
    }

    val remainingSeconds = ((phaseDuration * (1f - phaseProgress))).toInt()

    AnimatedContent(
        targetState = remainingSeconds,
        transitionSpec = {
            if (targetState < initialState) {
                slideInVertically(
                    animationSpec = tween(200, easing = FastOutSlowInEasing),
                    initialOffsetY = { it }
                ) togetherWith slideOutVertically(
                    animationSpec = tween(200, easing = FastOutSlowInEasing),
                    targetOffsetY = { -it }
                )
            } else {
                slideInVertically(
                    animationSpec = tween(200, easing = FastOutSlowInEasing),
                    initialOffsetY = { -it }
                ) togetherWith slideOutVertically(
                    animationSpec = tween(200, easing = FastOutSlowInEasing),
                    targetOffsetY = { it }
                )
            }
        },
        label = "timer_countdown",
        modifier = modifier
    ) { seconds ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = formatTime(seconds),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "kalan süre",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Format seconds to MM:SS
 */
private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(minutes, secs)
}
