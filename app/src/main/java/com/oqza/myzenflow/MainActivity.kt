package com.oqza.myzenflow

import com.oqza.myzenflow.presentation.theme.ZenMotion
import com.oqza.myzenflow.presentation.navigation.shouldShowBottomBar
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import com.oqza.myzenflow.utils.DeepLinks
import androidx.compose.runtime.mutableStateOf
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.oqza.myzenflow.data.models.ThemeMode
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.oqza.myzenflow.data.repository.PreferencesRepository
import com.oqza.myzenflow.presentation.components.BottomNavigationBar
import com.oqza.myzenflow.presentation.navigation.NavGraph
import com.oqza.myzenflow.presentation.navigation.Screen
import com.oqza.myzenflow.presentation.theme.MyZenFlowTheme
import com.oqza.myzenflow.utils.LocaleManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import com.oqza.myzenflow.data.models.UserPreferences
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    // Keeps the splash visible until the first DataStore read, so the start destination
    // (onboarding vs. home) and theme are correct on the first frame.
    @Volatile
    private var preferencesLoaded = false

    // Destination requested by a shortcut, widget or other launch intent (whitelisted)
    private val pendingRoute = mutableStateOf<String?>(null)

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingRoute.value = DeepLinks.routeFrom(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { !preferencesLoaded }
        super.onCreate(savedInstanceState)
        pendingRoute.value = DeepLinks.routeFrom(intent)
        enableEdgeToEdge()

        setContent {
            // Observe user preferences for locale changes
            val loadedPreferences by preferencesRepository.userPreferences
                .map<UserPreferences, UserPreferences?> { it }
                .collectAsStateWithLifecycle(initialValue = null)
            val userPreferences = loadedPreferences ?: UserPreferences()

            LaunchedEffect(loadedPreferences != null) {
                if (loadedPreferences != null) preferencesLoaded = true
            }

            // Apply locale when language changes
            LaunchedEffect(userPreferences.language) {
                LocaleManager.applyLocale(this@MainActivity, userPreferences.language)
            }

            if (loadedPreferences == null) return@setContent

            val darkTheme = when (userPreferences.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            MyZenFlowTheme(
                darkTheme = darkTheme,
                dynamicColor = userPreferences.dynamicColorEnabled
            ) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Check onboarding status and navigate accordingly
                LaunchedEffect(userPreferences.onboardingCompleted) {
                    if (!userPreferences.onboardingCompleted && currentRoute != Screen.Onboarding.route) {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                }

                // Open a shortcut/widget destination once onboarding is done
                val requestedRoute = pendingRoute.value
                LaunchedEffect(requestedRoute, userPreferences.onboardingCompleted) {
                    if (requestedRoute != null && userPreferences.onboardingCompleted) {
                        navController.navigate(requestedRoute) { launchSingleTop = true }
                        pendingRoute.value = null
                    }
                }

                // The bar stays on every non-immersive destination. Until the first back stack entry
                // exists, fall back to the start route so the bar does not pop in late.
                val startRoute = if (userPreferences.onboardingCompleted) {
                    Screen.Home.route
                } else {
                    Screen.Onboarding.route
                }
                val shouldShowBottomBar = shouldShowBottomBar(currentRoute ?: startRoute)

                // Screens own their system-bar insets; the outer scaffold only reserves
                // space for the bottom navigation bar.
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        // Slides in/out instead of popping, and the content padding follows the animation
                        AnimatedVisibility(
                            visible = shouldShowBottomBar,
                            enter = slideInVertically(tween(ZenMotion.MEDIUM, easing = ZenMotion.Standard)) { it } +
                                fadeIn(tween(ZenMotion.MEDIUM)),
                            exit = slideOutVertically(tween(ZenMotion.MEDIUM, easing = ZenMotion.Standard)) { it } +
                                fadeOut(tween(ZenMotion.SHORT))
                        ) {
                            BottomNavigationBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    NavGraph(
                        modifier = Modifier.padding(
                            PaddingValues(bottom = innerPadding.calculateBottomPadding())
                        ),
                        navController = navController,
                        startDestination = startRoute
                    )
                }
            }
        }
    }
}
