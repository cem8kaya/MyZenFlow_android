package com.oqza.myzenflow.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import com.oqza.myzenflow.R

sealed class Screen(
    val route: String,
    @StringRes val titleResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen(
        route = "home",
        titleResId = R.string.tab_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    object Focus : Screen(
        route = "focus",
        titleResId = R.string.tab_focus,
        selectedIcon = Icons.Filled.Timer,
        unselectedIcon = Icons.Outlined.Timer
    )

    object ZenGarden : Screen(
        route = "zen_garden",
        titleResId = R.string.tab_garden,
        selectedIcon = Icons.Filled.Park,
        unselectedIcon = Icons.Outlined.Park
    )

    object Calendar : Screen(
        route = "calendar",
        titleResId = R.string.tab_calendar,
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    )

    object Profile : Screen(
        route = "profile",
        titleResId = R.string.tab_profile,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    object Settings : Screen(
        route = "settings",
        titleResId = R.string.screen_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    // Non-bottom navigation screens
    object Breathing : Screen(
        route = "breathing",
        titleResId = R.string.screen_breathing,
        selectedIcon = Icons.Filled.Home, // Placeholder
        unselectedIcon = Icons.Outlined.Home // Placeholder
    )

    object Onboarding : Screen(
        route = "onboarding",
        titleResId = R.string.app_name, // Placeholder
        selectedIcon = Icons.Filled.Home, // Placeholder
        unselectedIcon = Icons.Outlined.Home // Placeholder
    )
}

// List of all bottom navigation screens
val bottomNavigationScreens = listOf(
    Screen.Home,
    Screen.Focus,
    Screen.ZenGarden,
    Screen.Profile,
    Screen.Settings
)
