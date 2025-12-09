package com.oqza.myzenflow.presentation.screens.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oqza.myzenflow.data.models.BreathingPhase

/**
 * Animated phase indicator with icon and text
 * Features:
 * - Smooth phase transitions with AnimatedContent
 * - Icon changes based on phase
 * - Fade + slide animations
 */
@Composable
fun PhaseIndicator(
    phase: BreathingPhase,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = phase,
        transitionSpec = {
            fadeIn(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + slideInVertically(
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                initialOffsetY = { it / 4 }
            ) togetherWith fadeOut(
                animationSpec = tween(200, easing = FastOutSlowInEasing)
            ) + slideOutVertically(
                animationSpec = tween(200, easing = FastOutSlowInEasing),
                targetOffsetY = { -it / 4 }
            )
        },
        label = "phase_indicator",
        modifier = modifier
    ) { targetPhase ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            // Phase icon
            Icon(
                imageVector = getPhaseIcon(targetPhase),
                contentDescription = getPhaseText(targetPhase),
                modifier = Modifier.size(48.dp),
                tint = Color(0xFF4A90E2)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Phase text
            Text(
                text = getPhaseText(targetPhase),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (isActive) Color.White else Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Get icon for breathing phase
 */
private fun getPhaseIcon(phase: BreathingPhase): ImageVector {
    return when (phase) {
        BreathingPhase.INHALE -> Icons.Default.ArrowUpward
        BreathingPhase.HOLD_INHALE -> Icons.Default.Pause
        BreathingPhase.EXHALE -> Icons.Default.ArrowDownward
        BreathingPhase.HOLD_EXHALE -> Icons.Default.Pause
        BreathingPhase.REST -> Icons.Default.FavoriteBorder
    }
}

/**
 * Get text for breathing phase
 */
private fun getPhaseText(phase: BreathingPhase): String {
    return when (phase) {
        BreathingPhase.INHALE -> "Nefes Al"
        BreathingPhase.HOLD_INHALE -> "Tut"
        BreathingPhase.EXHALE -> "Nefes Ver"
        BreathingPhase.HOLD_EXHALE -> "Tut"
        BreathingPhase.REST -> "Hazır"
    }
}
