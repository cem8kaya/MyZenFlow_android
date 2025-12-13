package com.oqza.myzenflow.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.oqza.myzenflow.data.models.BreathingPhase

/**
 * Data class to hold color pair for gradients
 */
data class ColorPair(
    val primary: Color,
    val secondary: Color
)

/**
 * Get breathing screen background gradient colors based on theme
 */
@Composable
@ReadOnlyComposable
fun breathingGradientColors(): List<Color> {
    val darkTheme = isSystemInDarkTheme()
    return if (darkTheme) {
        listOf(
            BreathingGradient1Dark,
            BreathingGradient2Dark,
            BreathingGradient3Dark
        )
    } else {
        listOf(
            BreathingGradient1Light,
            BreathingGradient2Light,
            BreathingGradient3Light
        )
    }
}

/**
 * Get breathing circle phase colors based on theme
 */
@Composable
@ReadOnlyComposable
fun breathingPhaseColors(phase: BreathingPhase): ColorPair {
    val darkTheme = isSystemInDarkTheme()

    return if (darkTheme) {
        when (phase) {
            BreathingPhase.INHALE -> ColorPair(
                primary = BreathingInhalePrimaryDark,
                secondary = BreathingInhaleSecondaryDark
            )
            BreathingPhase.HOLD_INHALE -> ColorPair(
                primary = BreathingHoldInhalePrimaryDark,
                secondary = BreathingHoldInhaleSecondaryDark
            )
            BreathingPhase.EXHALE -> ColorPair(
                primary = BreathingExhalePrimaryDark,
                secondary = BreathingExhaleSecondaryDark
            )
            BreathingPhase.HOLD_EXHALE -> ColorPair(
                primary = BreathingHoldExhalePrimaryDark,
                secondary = BreathingHoldExhaleSecondaryDark
            )
            BreathingPhase.REST -> ColorPair(
                primary = BreathingRestPrimaryDark,
                secondary = BreathingRestSecondaryDark
            )
        }
    } else {
        when (phase) {
            BreathingPhase.INHALE -> ColorPair(
                primary = BreathingInhalePrimaryLight,
                secondary = BreathingInhaleSecondaryLight
            )
            BreathingPhase.HOLD_INHALE -> ColorPair(
                primary = BreathingHoldInhalePrimaryLight,
                secondary = BreathingHoldInhaleSecondaryLight
            )
            BreathingPhase.EXHALE -> ColorPair(
                primary = BreathingExhalePrimaryLight,
                secondary = BreathingExhaleSecondaryLight
            )
            BreathingPhase.HOLD_EXHALE -> ColorPair(
                primary = BreathingHoldExhalePrimaryLight,
                secondary = BreathingHoldExhaleSecondaryLight
            )
            BreathingPhase.REST -> ColorPair(
                primary = BreathingRestPrimaryLight,
                secondary = BreathingRestSecondaryLight
            )
        }
    }
}

/**
 * Get tree leaves color based on theme
 */
val treeLeavesColor: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) TreeLeavesColorDark else TreeLeavesColorLight

/**
 * Get tree ground color based on theme
 */
val treeGroundColor: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) TreeGroundColorDark else TreeGroundColorLight
