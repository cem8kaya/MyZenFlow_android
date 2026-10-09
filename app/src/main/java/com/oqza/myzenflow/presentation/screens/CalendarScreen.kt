package com.oqza.myzenflow.presentation.screens

import com.oqza.myzenflow.presentation.viewmodels.CombinedSession
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.platform.LocalContext
import com.oqza.myzenflow.presentation.theme.zenTabScreenInsets
import com.oqza.myzenflow.presentation.components.ZenEmptyState
import com.oqza.myzenflow.presentation.theme.ZenSpacing
import com.oqza.myzenflow.presentation.components.ZenSkeleton
import com.oqza.myzenflow.R
import androidx.compose.ui.res.stringResource
import com.oqza.myzenflow.presentation.components.ZenCard
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oqza.myzenflow.presentation.viewmodels.*
import java.time.format.DateTimeFormatter

/**
 * Calendar screen - Shows session history with calendar visualization
 * Combines meditation, focus, and breathing sessions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
    onNavigateToZenGarden: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Reload whenever the screen comes back (a session may have finished meanwhile), then warm
    // the neighbouring months so navigating between months is instant
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }
    LaunchedEffect(uiState.selectedMonth, uiState.isLoading) {
        if (!uiState.isLoading) viewModel.preloadAdjacentMonths()
    }

    val context = LocalContext.current
    val formatDuration: (Int) -> String = { seconds ->
        val minutes = seconds / 60
        if (minutes < 60) context.getString(R.string.minutes_count, minutes)
        else context.getString(R.string.hours_minutes_short, minutes / 60, minutes % 60)
    }

    Scaffold(
        contentWindowInsets = zenTabScreenInsets(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.calendar_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    // Today button
                    IconButton(onClick = { viewModel.goToToday() }) {
                        Icon(
                            Icons.Default.Today,
                            contentDescription = stringResource(R.string.cal_go_today)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(ZenSpacing.screen),
                verticalArrangement = Arrangement.spacedBy(ZenSpacing.md)
            ) {
                ZenSkeleton(modifier = Modifier.fillMaxWidth(), height = 40.dp)
                ZenSkeleton(modifier = Modifier.fillMaxWidth(), height = 80.dp)
                ZenSkeleton(modifier = Modifier.fillMaxWidth(), height = 280.dp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Month navigation header
                MonthNavigationHeader(
                    monthDisplayName = viewModel.getMonthDisplayName(),
                    onPreviousMonth = { viewModel.getPreviousMonth() },
                    onNextMonth = { viewModel.getNextMonth() }
                )

                // Month summary card
                MonthSummaryCard(
                    totalSessions = uiState.totalSessionsThisMonth,
                    totalMinutes = uiState.totalMinutesThisMonth
                )

                // Weekday headers
                WeekdayHeaders(weekdayNames = viewModel.getWeekdayNames())

                // Calendar grid
                CalendarGrid(
                    monthDays = uiState.monthDays,
                    onDayClick = { day ->
                        if (day.sessionCount > 0) {
                            viewModel.selectDate(day.date)
                        }
                    }
                )

                // Session details panel (if date selected)
                AnimatedVisibility(
                    visible = uiState.selectedDate != null,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    SessionDetailsPanel(
                        selectedDate = uiState.selectedDate,
                        sessions = uiState.sessionsForSelectedDate,
                        onClose = { viewModel.clearSelection() },
                        onViewInZenGarden = onNavigateToZenGarden,
                        formatTime = { viewModel.formatTime(it) },
                        formatDuration = formatDuration
                    )
                }

                // Empty state (only show when not loading, no sessions, and no date selected)
                if (!uiState.isLoading && uiState.totalSessionsThisMonth == 0 && uiState.selectedDate == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateMessage()
                    }
                }
            }
        }
    }
}

/**
 * Month navigation header with arrows
 */
@Composable
private fun MonthNavigationHeader(
    monthDisplayName: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                Icons.Default.ChevronLeft,
                contentDescription = stringResource(R.string.cal_prev_month),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = monthDisplayName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        IconButton(onClick = onNextMonth) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.cal_next_month),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Month summary card showing total stats
 */
@Composable
private fun MonthSummaryCard(
    totalSessions: Int,
    totalMinutes: Int
) {
    ZenCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = totalSessions.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = stringResource(R.string.cal_sessions),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }

            Divider(
                modifier = Modifier
                    .height(48.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.3f)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = totalMinutes.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = stringResource(R.string.cal_minutes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Weekday headers (Mon, Tue, Wed, etc.)
 */
@Composable
private fun WeekdayHeaders(weekdayNames: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        weekdayNames.forEach { dayName ->
            Text(
                text = dayName,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Calendar grid with day cells
 */
@Composable
private fun CalendarGrid(
    monthDays: List<DayData>,
    onDayClick: (DayData) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        items(monthDays) { day ->
            DayCell(
                day = day,
                onClick = { onDayClick(day) }
            )
        }
    }
}

/**
 * Individual day cell with session indicators
 */
@Composable
private fun DayCell(
    day: DayData,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (day.isSelected) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "day_cell_scale"
    )

    // Color based on session intensity
    val backgroundColor = when {
        day.isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        !day.isCurrentMonth -> Color.Transparent
        else -> when (day.getIntensityLevel()) {
            0 -> Color.Transparent
            1 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            2 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            3 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
            else -> Color.Transparent
        }
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = if (day.isToday) 2.dp else 0.dp,
                color = if (day.isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = day.isCurrentMonth) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Day number
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    !day.isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    day.isSelected -> MaterialTheme.colorScheme.primary
                    day.isToday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            // Session indicator dots
            if (day.sessionCount > 0 && day.isCurrentMonth) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.height(6.dp)
                ) {
                    repeat(minOf(day.sessionCount, 3)) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .padding(horizontal = 1.dp)
                                .clip(CircleShape)
                                .background(
                                    if (day.isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                        )
                    }
                    if (day.sessionCount > 3) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.8f,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Session details panel showing sessions for selected date
 */
@Composable
private fun SessionDetailsPanel(
    selectedDate: java.time.LocalDate?,
    sessions: List<CombinedSession>,
    onClose: () -> Unit,
    onViewInZenGarden: () -> Unit,
    formatTime: (java.time.LocalDateTime) -> String,
    formatDuration: (Int) -> String
) {
    if (selectedDate == null) return

    ZenCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = selectedDate.format(
                            DateTimeFormatter.ofPattern("d MMMM yyyy")
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.cal_day_summary, sessions.size, sessions.sumOf { it.durationSeconds } / 60),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cal_close))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Session list
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions) { session ->
                    SessionItem(
                        session = session,
                        formatTime = formatTime,
                        formatDuration = formatDuration
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // View in Zen Garden button
            Button(
                onClick = onViewInZenGarden,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Park, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.cal_view_in_garden))
            }
        }
    }
}

/**
 * Individual session item in the details panel
 */
@Composable
private fun SessionItem(
    session: CombinedSession,
    formatTime: (java.time.LocalDateTime) -> String,
    formatDuration: (Int) -> String
) {
    val icon = when (session) {
        is CombinedSession.MeditationSession -> Icons.Default.SelfImprovement
        is CombinedSession.FocusSession -> Icons.Default.Timer
        is CombinedSession.BreathingSession -> Icons.Default.Air
    }

    val iconColor = when (session) {
        is CombinedSession.MeditationSession -> MaterialTheme.colorScheme.tertiary
        is CombinedSession.FocusSession -> MaterialTheme.colorScheme.primary
        is CombinedSession.BreathingSession -> MaterialTheme.colorScheme.secondary
    }

    ZenCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Session info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (session) {
                        is CombinedSession.FocusSession ->
                            session.focusData.taskName?.takeIf { it.isNotBlank() }
                                ?: stringResource(R.string.cal_default_focus_title)
                        is CombinedSession.MeditationSession ->
                            session.sessionData.breathingExercise?.displayName
                                ?: stringResource(R.string.cal_type_meditation)
                        is CombinedSession.BreathingSession -> session.title
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(
                        when (session) {
                            is CombinedSession.FocusSession -> R.string.cal_type_focus
                            is CombinedSession.MeditationSession -> R.string.cal_type_meditation
                            is CombinedSession.BreathingSession -> R.string.cal_type_breathing
                        }
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Time and duration
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatTime(session.date),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatDuration(session.durationSeconds),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * Empty state message when no sessions this month
 */
@Composable
private fun EmptyStateMessage() {
    ZenEmptyState(
        icon = Icons.Default.EventBusy,
        title = stringResource(R.string.cal_empty_title),
        message = stringResource(R.string.cal_empty_message)
    )
}
