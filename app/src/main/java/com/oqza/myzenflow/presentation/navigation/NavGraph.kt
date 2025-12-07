package com.oqza.myzenflow.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.oqza.myzenflow.presentation.screens.BreathingScreen
import com.oqza.myzenflow.presentation.screens.CalendarScreen
import com.oqza.myzenflow.presentation.screens.FocusScreen
import com.oqza.myzenflow.presentation.screens.HomeScreen
import com.oqza.myzenflow.presentation.screens.ProfileScreen
import com.oqza.myzenflow.presentation.screens.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(route = Screen.Focus.route) {
            FocusScreen()
        }

        composable(route = Screen.Calendar.route) {
            CalendarScreen()
        }

        composable(route = Screen.Profile.route) {
            ProfileScreen()
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen()
        }

        composable(route = Screen.Breathing.route) {
            BreathingScreen()
        }
    }
}
