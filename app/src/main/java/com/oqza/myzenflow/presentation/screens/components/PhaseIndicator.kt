package com.oqza.myzenflow.presentation.screens.components

import com.oqza.myzenflow.presentation.theme.breathingContentColor
import com.oqza.myzenflow.R
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.res.stringResource
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
        // Polite live region: TalkBack announces each new phase ("Inhale", "Hold", ...)
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite }
    ) { targetPhase ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            // Phase icon
            Icon(
                imageVector = getPhaseIcon(targetPhase),
                contentDescription = null, // the text below carries the meaning
                modifier = Modifier.size(48.dp),
                tint = breathingContentColor()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Phase text
            Text(
                text = getPhaseText(targetPhase),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = breathingContentColor().copy(alpha = if (isActive) 1f else 0.75f)
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
 * Localized text for a breathing phase
 */
@Composable
private fun getPhaseText(phase: BreathingPhase): String = stringResource(
    when (phase) {
        BreathingPhase.INHALE -> R.string.phase_inhale
        BreathingPhase.HOLD_INHALE, BreathingPhase.HOLD_EXHALE -> R.string.phase_hold
        BreathingPhase.EXHALE -> R.string.phase_exhale
        BreathingPhase.REST -> R.string.phase_ready
    }
)
