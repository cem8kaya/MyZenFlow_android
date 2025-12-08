package com.oqza.myzenflow.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.oqza.myzenflow.presentation.components.GreetingHeader
import com.oqza.myzenflow.presentation.components.QuickAction
import com.oqza.myzenflow.presentation.components.QuickActionsGrid
import com.oqza.myzenflow.presentation.components.RecentSessionsSection
import com.oqza.myzenflow.presentation.components.TodayStatsRow
import com.oqza.myzenflow.presentation.navigation.Screen
import com.oqza.myzenflow.presentation.viewmodels.HomeViewModel

/**
 * Home screen with iOS-like design
 * Displays greeting, stats, quick actions, and recent sessions
 */
@Composable
fun HomeScreen(
    navController: NavController? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // Show error snackbar if present
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Content
                AnimatedVisibility(
                    visible = !uiState.isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Greeting Header
                        GreetingHeader(
                            userName = uiState.userName,
                            motivationalQuote = uiState.motivationalQuote
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Today's Stats
                        TodayStatsRow(
                            sessionCount = uiState.todaySessionCount,
                            minutes = uiState.todayMinutes,
                            streak = uiState.currentStreak
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Quick Actions Grid
                        QuickActionsGrid(
                            actions = getQuickActions(),
                            onActionClick = { route ->
                                navController?.navigate(route)
                            }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Recent Sessions
                        RecentSessionsSection(
                            sessions = uiState.recentSessions,
                            onStartClick = {
                                navController?.navigate(Screen.Breathing.route)
                            }
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

/**
 * Get quick action items with gradient colors
 */
@Composable
private fun getQuickActions(): List<QuickAction> {
    return listOf(
        QuickAction(
            icon = Icons.Outlined.Air,
            title = "Nefes",
            subtitle = "Egzersizler",
            gradientColors = listOf(
                Color(0xFF6366F1),
                Color(0xFF8B5CF6)
            ),
            route = Screen.Breathing.route
        ),
        QuickAction(
            icon = Icons.Outlined.Timer,
            title = "Odaklan",
            subtitle = "Pomodoro",
            gradientColors = listOf(
                Color(0xFFEC4899),
                Color(0xFFF43F5E)
            ),
            route = Screen.Focus.route
        ),
        QuickAction(
            icon = Icons.Outlined.Park,
            title = "Zen Bahçe",
            subtitle = "Huzur Bul",
            gradientColors = listOf(
                Color(0xFF10B981),
                Color(0xFF059669)
            ),
            route = Screen.ZenGarden.route
        ),
        QuickAction(
            icon = Icons.Outlined.CalendarMonth,
            title = "İlerleme",
            subtitle = "Geçmiş",
            gradientColors = listOf(
                Color(0xFFF59E0B),
                Color(0xFFEF4444)
            ),
            route = Screen.Calendar.route
        )
    )
}
