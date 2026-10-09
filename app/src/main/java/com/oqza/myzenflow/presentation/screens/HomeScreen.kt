package com.oqza.myzenflow.presentation.screens

import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import com.oqza.myzenflow.presentation.components.ZenReadableWidth
import com.oqza.myzenflow.presentation.navigation.navigateTo
import com.oqza.myzenflow.presentation.theme.zenTabScreenInsets
import com.oqza.myzenflow.data.models.PracticeGoal
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import com.oqza.myzenflow.presentation.components.ZenCard
import androidx.compose.material3.Icon
import androidx.compose.material.icons.outlined.AutoAwesome
import com.oqza.myzenflow.utils.StreakCalculator
import com.oqza.myzenflow.presentation.components.DailyCheckInCard
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Timer
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.oqza.myzenflow.R
import com.oqza.myzenflow.data.models.TimeOfDay
import com.oqza.myzenflow.presentation.components.GreetingHeader
import com.oqza.myzenflow.presentation.components.QuickAction
import com.oqza.myzenflow.presentation.components.QuickActionsGrid
import com.oqza.myzenflow.presentation.components.RecentSessionsSection
import com.oqza.myzenflow.presentation.components.TodayStatsRow
import com.oqza.myzenflow.presentation.components.ZenBackdrop
import com.oqza.myzenflow.presentation.components.ZenButton
import com.oqza.myzenflow.presentation.components.ZenGradientCard
import com.oqza.myzenflow.presentation.components.ZenSkeleton
import com.oqza.myzenflow.presentation.navigation.Screen
import com.oqza.myzenflow.presentation.theme.ZenDawnPeach
import com.oqza.myzenflow.presentation.theme.ZenIndigo
import com.oqza.myzenflow.presentation.theme.ZenMotion
import com.oqza.myzenflow.presentation.theme.ZenSpacing
import com.oqza.myzenflow.presentation.viewmodels.HomeViewModel
import java.time.LocalTime

/**
 * Home screen: time-of-day atmosphere, greeting, a "right now" recommendation,
 * today's stats, quick actions and recent sessions.
 */
@Composable
fun HomeScreen(
    navController: NavController? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val timeOfDay = remember { TimeOfDay.fromHour(LocalTime.now().hour) }

    // Pick up sessions finished elsewhere when returning to Home
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshData() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    ZenBackdrop(modifier = Modifier.fillMaxSize(), timeOfDay = timeOfDay) {
        ZenReadableWidth {
            Scaffold(
                contentWindowInsets = zenTabScreenInsets(),
                containerColor = Color.Transparent,
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    if (uiState.isLoading) {
                        HomeSkeleton()
                    } else {
                        AnimatedVisibility(
                            visibleState = remember {
                                MutableTransitionState(false).apply { targetState = true }
                            },
                            enter = fadeIn(tween(ZenMotion.MEDIUM)) +
                                slideInVertically(tween(ZenMotion.MEDIUM)) { it / 20 }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                            ) {
                                GreetingHeader(
                                    userName = uiState.userName,
                                    motivationalQuote = uiState.motivationalQuote,
                                    timeOfDay = timeOfDay
                                )

                                RecommendationCard(
                                    timeOfDay = timeOfDay,
                                    goal = uiState.userPreferences.primaryGoal,
                                    isFirstSession = uiState.recentSessions.isEmpty(),
                                    onStart = { route -> navController?.navigateTo(route) }
                                )

                                Spacer(modifier = Modifier.height(ZenSpacing.lg))

                                DailyCheckInCard(
                                    modifier = Modifier.padding(horizontal = ZenSpacing.screen)
                                )

                                Spacer(modifier = Modifier.height(ZenSpacing.xl))

                                TodayStatsRow(
                                    sessionCount = uiState.todaySessionCount,
                                    minutes = uiState.todayMinutes,
                                    streak = uiState.currentStreak
                                )

                                StreakMessage(uiState.streakState)

                                Spacer(modifier = Modifier.height(ZenSpacing.xxl))

                                QuickActionsGrid(
                                    actions = getQuickActions(),
                                    onActionClick = { route -> navController?.navigateTo(route) }
                                )

                                if (uiState.recentSessions.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(ZenSpacing.xxl))
                                    WeeklySummaryEntry(
                                        onClick = { navController?.navigateTo(Screen.WeeklySummary.route) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(ZenSpacing.xxl))

                                RecentSessionsSection(
                                    sessions = uiState.recentSessions,
                                    onStartClick = { navController?.navigateTo(Screen.Breathing.route) }
                                )

                                Spacer(modifier = Modifier.height(ZenSpacing.xxl))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Entry card to the weekly review.
 */
@Composable
private fun WeeklySummaryEntry(onClick: () -> Unit) {
    ZenCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ZenSpacing.screen),
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = ZenSpacing.md)
            ) {
                Text(stringResource(R.string.weekly_card_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.weekly_card_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * One supportive line about the streak. Never guilt-tripping: a broken streak is a fresh start.
 */
@Composable
private fun StreakMessage(state: StreakCalculator.State) {
    val message = when (state) {
        StreakCalculator.State.NONE -> return
        StreakCalculator.State.BROKEN -> R.string.streak_broken
        StreakCalculator.State.AT_RISK -> R.string.streak_at_risk
        StreakCalculator.State.REST_DAY_USED -> R.string.streak_rest_day
        StreakCalculator.State.SAFE_TODAY -> R.string.streak_safe
    }
    Text(
        text = stringResource(message),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = ZenSpacing.screen, vertical = ZenSpacing.sm)
    )
}

/**
 * Hero card suggesting one thing to do right now, based on time of day.
 */
@Composable
private fun RecommendationCard(
    timeOfDay: TimeOfDay,
    goal: PracticeGoal,
    isFirstSession: Boolean,
    onStart: (String) -> Unit
) {
    val (titleRes, messageRes, route) = if (isFirstSession && goal != PracticeGoal.NONE) when (goal) {
        // Very first session: speak to the goal the user chose in onboarding
        PracticeGoal.STRESS -> Triple(R.string.reco_first_stress_title, R.string.reco_first_stress_message, Screen.Breathing.route)
        PracticeGoal.SLEEP -> Triple(R.string.reco_first_sleep_title, R.string.reco_first_sleep_message, Screen.Breathing.route)
        PracticeGoal.FOCUS -> Triple(R.string.reco_first_focus_title, R.string.reco_first_focus_message, Screen.Focus.route)
        else -> Triple(R.string.reco_first_calm_title, R.string.reco_first_calm_message, Screen.Breathing.route)
    } else when (timeOfDay) {
        TimeOfDay.MORNING -> Triple(R.string.reco_morning_title, R.string.reco_morning_message, Screen.Breathing.route)
        TimeOfDay.AFTERNOON -> Triple(R.string.reco_afternoon_title, R.string.reco_afternoon_message, Screen.Focus.route)
        TimeOfDay.EVENING -> Triple(R.string.reco_evening_title, R.string.reco_evening_message, Screen.Breathing.route)
        TimeOfDay.NIGHT -> Triple(R.string.reco_night_title, R.string.reco_night_message, Screen.Breathing.route)
    }

    ZenGradientCard(
        colors = listOf(ZenIndigo, Color(0xFF6B58B5), Color(0xFF9A5C8F)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ZenSpacing.screen),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(ZenSpacing.xl)
    ) {
        Column {
            Text(
                text = stringResource(R.string.reco_label),
                style = MaterialTheme.typography.labelLarge,
                color = ZenDawnPeach
            )
            Spacer(modifier = Modifier.height(ZenSpacing.xs))
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(ZenSpacing.sm))
            Text(
                text = stringResource(messageRes),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.92f)
            )
            Spacer(modifier = Modifier.height(ZenSpacing.lg))
            ZenButton(
                text = stringResource(R.string.button_start),
                onClick = { onStart(route) },
                icon = Icons.Outlined.PlayArrow
            )
        }
    }
}

/**
 * Loading placeholder that mirrors the real layout, replacing a bare spinner.
 */
@Composable
private fun HomeSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = ZenSpacing.screen, vertical = ZenSpacing.lg)
    ) {
        ZenSkeleton(modifier = Modifier.width(140.dp), height = 14.dp)
        Spacer(modifier = Modifier.height(ZenSpacing.sm))
        ZenSkeleton(modifier = Modifier.width(240.dp), height = 32.dp)
        Spacer(modifier = Modifier.height(ZenSpacing.xl))
        ZenSkeleton(modifier = Modifier.fillMaxWidth(), height = 180.dp)
        Spacer(modifier = Modifier.height(ZenSpacing.xl))
        Row(horizontalArrangement = Arrangement.spacedBy(ZenSpacing.md)) {
            repeat(3) {
                ZenSkeleton(modifier = Modifier.weight(1f), height = 96.dp)
            }
        }
    }
}

/**
 * Quick action items. Gradients are dark enough for white text (AA for large/bold text).
 */
@Composable
private fun getQuickActions(): List<QuickAction> {
    return listOf(
        QuickAction(
            icon = Icons.Outlined.Air,
            title = stringResource(R.string.quick_action_breathing),
            subtitle = stringResource(R.string.quick_action_breathing_subtitle),
            gradientColors = listOf(Color(0xFF4B4F9E), Color(0xFF6C70C4)),
            route = Screen.Breathing.route
        ),
        QuickAction(
            icon = Icons.Outlined.Timer,
            title = stringResource(R.string.quick_action_focus),
            subtitle = stringResource(R.string.quick_action_focus_subtitle),
            gradientColors = listOf(Color(0xFF9A4A2B), Color(0xFFC0643F)),
            route = Screen.Focus.route
        ),
        QuickAction(
            icon = Icons.Outlined.Park,
            title = stringResource(R.string.quick_action_garden),
            subtitle = stringResource(R.string.quick_action_garden_subtitle),
            gradientColors = listOf(Color(0xFF3F6B52), Color(0xFF5B8A70)),
            route = Screen.ZenGarden.route
        ),
        QuickAction(
            icon = Icons.Outlined.CalendarMonth,
            title = stringResource(R.string.quick_action_progress),
            subtitle = stringResource(R.string.quick_action_progress_subtitle),
            gradientColors = listOf(Color(0xFF7D5A14), Color(0xFFA67A22)),
            route = Screen.Calendar.route
        )
    )
}
