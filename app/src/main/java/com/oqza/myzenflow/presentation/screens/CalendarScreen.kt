package com.oqza.myzenflow.presentation.screens

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

    // Preload adjacent months on first load
    LaunchedEffect(Unit) {
        viewModel.preloadAdjacentMonths()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Takvim") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    // Today button
                    IconButton(onClick = { viewModel.goToToday() }) {
                        Icon(
                            Icons.Default.Today,
                            contentDescription = "Bugüne git"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
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
                        formatDuration = { viewModel.formatDuration(it) }
                    )
                }

                // Empty state (if no sessions this month and no date selected)
                if (uiState.totalSessionsThisMonth == 0 && uiState.selectedDate == null) {
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
                contentDescription = "Önceki ay",
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
                contentDescription = "Sonraki ay",
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
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
                    text = "Oturum",
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
                    text = "Dakika",
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                        text = "${sessions.size} oturum • ${sessions.sumOf { it.durationSeconds } / 60} dakika",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat")
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
                Text("Zen Bahçesi'nde Gör")
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
        is CombinedSession.MeditationSession -> Color(0xFF9C27B0) // Purple
        is CombinedSession.FocusSession -> Color(0xFF2196F3) // Blue
        is CombinedSession.BreathingSession -> Color(0xFF4CAF50) // Green
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
                    text = session.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = session.sessionType,
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(
            Icons.Default.EventBusy,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Bu ay henüz oturum yok",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "İlk meditasyon, odaklanma veya nefes egzersizi oturumunu başlatın!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}
