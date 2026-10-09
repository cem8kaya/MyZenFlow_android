package com.oqza.myzenflow.presentation.theme

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Window insets for a screen that is shown above the bottom navigation bar.
 *
 * The navigation bar (and the outer Scaffold padding in MainActivity) already accounts for the
 * bottom system inset, so a screen's own Scaffold must not add it a second time. That second
 * inset showed up as an empty band between the content and the bar.
 *
 * Usage: `Scaffold(contentWindowInsets = zenTabScreenInsets(), ...)`
 */
@Composable
@ReadOnlyComposable
fun zenTabScreenInsets(): WindowInsets =
    WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
