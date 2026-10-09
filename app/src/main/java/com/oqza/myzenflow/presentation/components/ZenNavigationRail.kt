package com.oqza.myzenflow.presentation.components

import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.oqza.myzenflow.presentation.navigation.bottomNavigationScreens
import com.oqza.myzenflow.presentation.navigation.navigateToTab

/**
 * Side navigation for wide windows (tablets, foldables, phones in landscape).
 * Same destinations and behaviour as [BottomNavigationBar].
 */
@Composable
fun ZenNavigationRail(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationRail(containerColor = MaterialTheme.colorScheme.background) {
        bottomNavigationScreens.forEach { screen ->
            val isSelected = currentRoute == screen.route
            NavigationRailItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) navController.navigateToTab(screen.route)
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = null // the label names the destination
                    )
                },
                label = { Text(stringResource(screen.titleResId)) },
                alwaysShowLabel = true,
                colors = NavigationRailItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

/**
 * Keeps reading width comfortable on wide screens: content is centred and capped at [maxWidth].
 * A no-op on phones in portrait.
 */
@Composable
fun ZenReadableWidth(
    modifier: Modifier = Modifier,
    maxWidth: androidx.compose.ui.unit.Dp = 720.dp,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Box(modifier = Modifier.widthIn(max = maxWidth).fillMaxSize()) {
            content()
        }
    }
}
