package com.oqza.myzenflow.presentation.screens.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.oqza.myzenflow.data.models.BreathingPhase

/**
 * iOS-quality breathing circle with Canvas API
 * Features:
 * - Smooth 60 FPS animations
 * - Center circle with scale animation
 * - Outer ring with progress indicator
 * - Color transitions based on phase
 */
@Composable
fun BreathingCircleCanvas(
    phase: BreathingPhase,
    progress: Float,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    // Calculate target scale based on phase
    val targetScale = when (phase) {
        BreathingPhase.INHALE -> 0.6f + (progress * 0.4f) // 0.6 to 1.0
        BreathingPhase.HOLD_INHALE -> 1.0f // Hold at max
        BreathingPhase.EXHALE -> 1.0f - (progress * 0.4f) // 1.0 to 0.6
        BreathingPhase.HOLD_EXHALE -> 0.6f // Hold at min
        BreathingPhase.REST -> 0.6f
    }

    // Smooth scale animation with spring
    val animatedScale by animateFloatAsState(
        targetValue = if (isActive) targetScale else 0.6f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 300f
        ),
        label = "circle_scale"
    )

    // Phase colors - calm and mystical palette
    val phaseColors = remember(phase) {
        when (phase) {
            BreathingPhase.INHALE -> ColorPair(
                primary = Color(0xFF4A90E2), // Calm Blue
                secondary = Color(0xFF50C9E9)
            )
            BreathingPhase.HOLD_INHALE -> ColorPair(
                primary = Color(0xFF7B68EE), // Mystical Violet
                secondary = Color(0xFF9B88FF)
            )
            BreathingPhase.EXHALE -> ColorPair(
                primary = Color(0xFFB47DED), // Soft Purple
                secondary = Color(0xFFD4A5FF)
            )
            BreathingPhase.HOLD_EXHALE -> ColorPair(
                primary = Color(0xFF9B88FF), // Light Violet
                secondary = Color(0xFFB4A0FF)
            )
            BreathingPhase.REST -> ColorPair(
                primary = Color(0xFF9E9E9E), // Gray
                secondary = Color(0xFFBDBDBD)
            )
        }
    }

    // Animate colors smoothly
    val animatedPrimaryColor by animateColorAsState(
        targetValue = phaseColors.primary,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "primary_color"
    )

    val animatedSecondaryColor by animateColorAsState(
        targetValue = phaseColors.secondary,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "secondary_color"
    )

    // Sweep angle for progress ring
    val sweepAngle = 360f * progress

    Box(
        modifier = modifier
            .size(320.dp)
            .aspectRatio(1f)
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2f
            val centerY = canvasHeight / 2f

            // Outer ring radius
            val outerRingRadius = canvasWidth / 2f - 40f
            val innerCircleRadius = (canvasWidth / 2f - 80f) * animatedScale

            // Draw outer progress ring (background)
            drawCircle(
                color = animatedPrimaryColor.copy(alpha = 0.15f),
                radius = outerRingRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 12f, cap = StrokeCap.Round)
            )

            // Draw outer progress ring (foreground - animated sweep)
            if (isActive && progress > 0f) {
                drawArc(
                    color = animatedPrimaryColor,
                    startAngle = -90f, // Start from top
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerX - outerRingRadius, centerY - outerRingRadius),
                    size = androidx.compose.ui.geometry.Size(outerRingRadius * 2, outerRingRadius * 2),
                    style = Stroke(width = 12f, cap = StrokeCap.Round)
                )
            }

            // Draw outer glow circle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animatedPrimaryColor.copy(alpha = 0.4f),
                        animatedPrimaryColor.copy(alpha = 0.2f),
                        animatedPrimaryColor.copy(alpha = 0.0f)
                    ),
                    center = Offset(centerX, centerY),
                    radius = innerCircleRadius + 60f
                ),
                radius = innerCircleRadius + 60f,
                center = Offset(centerX, centerY)
            )

            // Draw middle circle with gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animatedSecondaryColor.copy(alpha = 0.6f),
                        animatedPrimaryColor.copy(alpha = 0.8f)
                    ),
                    center = Offset(centerX, centerY),
                    radius = innerCircleRadius
                ),
                radius = innerCircleRadius,
                center = Offset(centerX, centerY)
            )

            // Draw inner circle (solid core)
            drawCircle(
                color = animatedPrimaryColor.copy(alpha = 0.95f),
                radius = innerCircleRadius * 0.85f,
                center = Offset(centerX, centerY)
            )

            // Draw subtle inner glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        animatedPrimaryColor.copy(alpha = 0.0f)
                    ),
                    center = Offset(centerX, centerY),
                    radius = innerCircleRadius * 0.5f
                ),
                radius = innerCircleRadius * 0.5f,
                center = Offset(centerX, centerY)
            )
        }
    }
}

/**
 * Data class to hold color pair for gradient
 */
private data class ColorPair(
    val primary: Color,
    val secondary: Color
)
