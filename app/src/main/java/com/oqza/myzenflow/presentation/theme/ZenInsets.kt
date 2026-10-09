package com.oqza.myzenflow.presentation.theme

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

/**
 * True when a bottom navigation bar sits under the screen content and therefore already covers the
 * bottom system inset. False in the wide layout (navigation rail), where each screen must add it.
 */
val LocalBottomInsetHandled = compositionLocalOf { true }

/**
 * Window insets for a screen in the main navigation.
 *
 * With the bottom bar, the bar (and the outer Scaffold padding in MainActivity) already accounts
 * for the bottom system inset, so a screen's own Scaffold must not add it a second time. That
 * second inset showed up as an empty band between the content and the bar. With the navigation
 * rail there is no bar, so the bottom inset is kept.
 *
 * Usage: `Scaffold(contentWindowInsets = zenTabScreenInsets(), ...)`
 */
@Composable
fun zenTabScreenInsets(): WindowInsets {
    val sides = if (LocalBottomInsetHandled.current) {
        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
    } else {
        WindowInsetsSides.Top + WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
    }
    return WindowInsets.systemBars.only(sides)
}
