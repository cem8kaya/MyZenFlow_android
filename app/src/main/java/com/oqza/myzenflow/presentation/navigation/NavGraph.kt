package com.oqza.myzenflow.presentation.navigation

import com.oqza.myzenflow.presentation.screens.WeeklySummaryScreen
import com.oqza.myzenflow.presentation.theme.ZenMotion
import com.oqza.myzenflow.presentation.theme.LocalReducedMotion
import androidx.navigation.NavBackStackEntry
import androidx.compose.runtime.remember
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.oqza.myzenflow.presentation.screens.BreathingScreen
import com.oqza.myzenflow.presentation.screens.CalendarScreen
import com.oqza.myzenflow.presentation.screens.FocusScreen
import com.oqza.myzenflow.presentation.screens.HomeScreen
import com.oqza.myzenflow.presentation.screens.OnboardingScreen
import com.oqza.myzenflow.presentation.screens.ProfileScreen
import com.oqza.myzenflow.presentation.screens.SettingsScreen
import com.oqza.myzenflow.presentation.screens.ZenGardenScreen

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    val reducedMotion = LocalReducedMotion.current
    val tabRoutes = remember { bottomNavigationScreens.map { it.route }.toSet() }

    // Tab <-> tab: fade-through. Anything else (opening or leaving an immersive screen):
    // a soft rise with fade. Reduced motion: short fades only.
    fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabSwitch() =
        initialState.destination.route in tabRoutes && targetState.destination.route in tabRoutes

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            when {
                reducedMotion -> fadeIn(tween(ZenMotion.SHORT))
                isTabSwitch() -> fadeIn(tween(ZenMotion.MEDIUM, delayMillis = 90, easing = ZenMotion.Standard)) +
                    scaleIn(tween(ZenMotion.MEDIUM, delayMillis = 90, easing = ZenMotion.Standard), initialScale = 0.96f)
                else -> fadeIn(tween(ZenMotion.MEDIUM, easing = ZenMotion.Standard)) +
                    slideInVertically(tween(ZenMotion.LONG, easing = ZenMotion.Standard)) { it / 16 }
            }
        },
        exitTransition = {
            if (reducedMotion || isTabSwitch()) fadeOut(tween(90)) else fadeOut(tween(ZenMotion.SHORT))
        },
        popEnterTransition = {
            if (reducedMotion) fadeIn(tween(ZenMotion.SHORT))
            else fadeIn(tween(ZenMotion.MEDIUM, easing = ZenMotion.Standard))
        },
        popExitTransition = {
            if (reducedMotion) fadeOut(tween(ZenMotion.SHORT))
            else fadeOut(tween(ZenMotion.MEDIUM)) +
                slideOutVertically(tween(ZenMotion.MEDIUM, easing = ZenMotion.Standard)) { it / 16 }
        }
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(route = Screen.Focus.route) {
            FocusScreen()
        }

        composable(route = Screen.ZenGarden.route) {
            ZenGardenScreen()
        }

        composable(route = Screen.Calendar.route) {
            CalendarScreen(
                onNavigateToZenGarden = { navController.navigateToTab(Screen.ZenGarden.route) }
            )
        }

        composable(route = Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(onNavigateBack = { navController.goBackOrHome() })
        }

        composable(route = Screen.Breathing.route) {
            BreathingScreen(onNavigateBack = { navController.goBackOrHome() })
        }

        composable(route = Screen.WeeklySummary.route) {
            WeeklySummaryScreen(navController = navController)
        }

        composable(route = Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }
    }
}
