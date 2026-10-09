package com.oqza.myzenflow.presentation.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Spacing scale (4dp grid). */
object ZenSpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val huge = 48.dp

    /** Horizontal screen gutter. */
    val screen = 20.dp
}

/** Corner radii. Soft, generous curves for a calm feel. */
object ZenRadius {
    val small = 12.dp
    val medium = 20.dp
    val large = 28.dp
    val xl = 36.dp
}

val ZenShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(ZenRadius.small),
    medium = RoundedCornerShape(ZenRadius.medium),
    large = RoundedCornerShape(ZenRadius.large),
    extraLarge = RoundedCornerShape(ZenRadius.xl)
)

/** Motion durations (ms) and easings shared by all animations. */
object ZenMotion {
    const val SHORT = 150
    const val MEDIUM = 300
    const val LONG = 600
    const val AMBIENT = 4000

    /** Standard easing for elements moving within the screen. */
    val Standard = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    /** Gentle ease-in-out for ambient and breathing-style loops. */
    val Calm = CubicBezierEasing(0.45f, 0f, 0.55f, 1f)
}
