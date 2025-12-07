package com.oqza.myzenflow.presentation.navigation

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

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen(
        route = "home",
        title = "Ana Sayfa",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    object Focus : Screen(
        route = "focus",
        title = "Odaklan",
        selectedIcon = Icons.Filled.Timer,
        unselectedIcon = Icons.Outlined.Timer
    )

    object ZenGarden : Screen(
        route = "zen_garden",
        title = "Zen Bahçem",
        selectedIcon = Icons.Filled.Park,
        unselectedIcon = Icons.Outlined.Park
    )

    object Calendar : Screen(
        route = "calendar",
        title = "Takvim",
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    )

    object Profile : Screen(
        route = "profile",
        title = "Profil",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    object Settings : Screen(
        route = "settings",
        title = "Ayarlar",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    // Non-bottom navigation screens
    object Breathing : Screen(
        route = "breathing",
        title = "Nefes Egzersizi",
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
