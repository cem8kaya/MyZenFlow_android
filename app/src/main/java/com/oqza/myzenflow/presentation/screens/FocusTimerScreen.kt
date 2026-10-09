package com.oqza.myzenflow.presentation.screens

import com.oqza.myzenflow.presentation.components.ZenReadableWidth
import com.oqza.myzenflow.presentation.components.rememberNotificationPermissionRequester
import com.oqza.myzenflow.presentation.theme.zenTabScreenInsets
import com.oqza.myzenflow.presentation.theme.ZenTypography
import com.oqza.myzenflow.presentation.theme.ZenTertiaryDark
import com.oqza.myzenflow.presentation.theme.ZenSurfaceVariantDark
import com.oqza.myzenflow.presentation.theme.ZenShapes
import com.oqza.myzenflow.presentation.theme.ZenSecondaryDark
import com.oqza.myzenflow.presentation.theme.ZenPrimaryDark
import com.oqza.myzenflow.presentation.theme.ZenOutlineVariantDark
import com.oqza.myzenflow.presentation.theme.ZenOnSurfaceVariantDark
import com.oqza.myzenflow.presentation.theme.ZenOnSurfaceDark
import com.oqza.myzenflow.presentation.theme.ZenOnBackgroundDark
import com.oqza.myzenflow.presentation.theme.ZenMidnight
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalView
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import com.oqza.myzenflow.presentation.components.ZenButtonStyle
import com.oqza.myzenflow.presentation.components.ZenButton
import com.oqza.myzenflow.R
import androidx.compose.ui.res.stringResource
import com.oqza.myzenflow.presentation.components.ZenCard
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oqza.myzenflow.data.models.FocusMode
import com.oqza.myzenflow.data.models.FocusSessionData
import com.oqza.myzenflow.data.models.TimerSessionType
import com.oqza.myzenflow.data.models.TimerStatus
import com.oqza.myzenflow.presentation.viewmodels.PomodoroViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerScreen(
    viewModel: PomodoroViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val todaysStats by viewModel.todaysStats.collectAsState()
    val todaysSessions by viewModel.todaysSessions.collectAsState()
    var showSettingsDialog by remember { mutableStateOf(false) }
    var deepFocus by remember { mutableStateOf(false) }
    // Timer alerts and the ongoing timer notification need this permission on Android 13+
    val requestNotificationPermission = rememberNotificationPermissionRequester()
    val timerActive = uiState.timerStatus == TimerStatus.RUNNING || uiState.timerStatus == TimerStatus.PAUSED

    // Leave deep focus when the session ends or is stopped
    LaunchedEffect(timerActive) { if (!timerActive) deepFocus = false }

    // Keep the screen on while the timer runs in deep focus
    val view = LocalView.current
    DisposableEffect(deepFocus) {
        view.keepScreenOn = deepFocus
        onDispose { view.keepScreenOn = false }
    }

    ZenReadableWidth {
        Scaffold(
            contentWindowInsets = zenTabScreenInsets(),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.focus_title_bar)) },
                    actions = {
                        if (timerActive) {
                            IconButton(onClick = { deepFocus = true }) {
                                Icon(
                                    Icons.Default.NightsStay,
                                    contentDescription = stringResource(R.string.focus_deep_mode)
                                )
                            }
                        }
                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.focus_settings))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mode selection (only when idle)
                if (uiState.timerStatus == TimerStatus.IDLE) {
                    item {
                        ModeSelectionSection(
                            selectedMode = uiState.selectedMode,
                            onModeSelected = { viewModel.selectMode(it) }
                        )
                    }
                }

                // Session type indicator
                item {
                    SessionTypeCard(
                        sessionType = uiState.currentSessionType,
                        cycleInfo = "${uiState.completedWorkSessions}/${uiState.totalCycles * 4}"
                    )
                }

                // Circular timer display
                item {
                    CircularTimerDisplay(
                        timeRemaining = uiState.formatTime(),
                        progress = uiState.calculateProgress(),
                        sessionType = uiState.currentSessionType,
                        isRunning = uiState.timerStatus == TimerStatus.RUNNING
                    )
                }

                // Task name input (only when idle)
                if (uiState.timerStatus == TimerStatus.IDLE) {
                    item {
                        TaskNameInput(
                            taskName = uiState.taskName,
                            onTaskNameChanged = { viewModel.setTaskName(it) }
                        )
                    }
                }

                // Control buttons
                item {
                    TimerControlButtons(
                        timerStatus = uiState.timerStatus,
                        onStart = {
                            requestNotificationPermission()
                            viewModel.startTimer()
                        },
                        onPause = { viewModel.pauseTimer() },
                        onResume = { viewModel.resumeTimer() },
                        onStop = { viewModel.stopTimer() },
                        onSkip = { viewModel.skipToNextSession() }
                    )
                }

                // Settings row
                item {
                    SettingsRow(
                        hapticEnabled = uiState.hapticEnabled,
                        soundEnabled = uiState.soundEnabled,
                        onToggleHaptic = { viewModel.toggleHaptic() },
                        onToggleSound = { viewModel.toggleSound() }
                    )
                }

                // Stats card
                item {
                    TodaysStatsCard(stats = todaysStats)
                }

                // Session history
                if (todaysSessions.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.focus_todays_sessions),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(todaysSessions) { session ->
                        SessionHistoryItem(session = session)
                    }
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (deepFocus) {
        DeepFocusOverlay(
            timeRemaining = uiState.formatTime(),
            progress = uiState.calculateProgress(),
            sessionType = uiState.currentSessionType,
            isRunning = uiState.timerStatus == TimerStatus.RUNNING,
            onExit = { deepFocus = false }
        )
    }

    // Settings dialog
    if (showSettingsDialog) {
        TimerSettingsDialog(
            workDuration = uiState.customFocusDuration,
            shortBreakDuration = uiState.customBreakDuration,
            longBreakDuration = uiState.customLongBreakDuration,
            totalCycles = uiState.totalCycles,
            onDismiss = { showSettingsDialog = false },
            onSave = { work, shortBreak, longBreak, cycles ->
                viewModel.saveTimerDurations(work, shortBreak, longBreak)
                viewModel.setTotalCycles(cycles)
                showSettingsDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelectionSection(
    selectedMode: FocusMode,
    onModeSelected: (FocusMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.focus_mode_label),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedMode.displayName,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(0.8f),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                FocusMode.values().forEach { mode ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(mode.displayName)
                                Text(
                                    text = "${mode.focusDuration} dk çalışma / ${mode.breakDuration} dk mola",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            onModeSelected(mode)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SessionTypeCard(
    sessionType: TimerSessionType,
    cycleInfo: String
) {
    val backgroundColor: Color
    val textColor: Color
    val icon: androidx.compose.ui.graphics.vector.ImageVector
    val text: String

    when (sessionType) {
        TimerSessionType.WORK -> {
            backgroundColor = MaterialTheme.colorScheme.primaryContainer
            textColor = MaterialTheme.colorScheme.onPrimaryContainer
            icon = Icons.Default.WorkOutline
            text = stringResource(R.string.work_session)
        }
        TimerSessionType.SHORT_BREAK -> {
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
            textColor = MaterialTheme.colorScheme.onTertiaryContainer
            icon = Icons.Default.Coffee
            text = stringResource(R.string.short_break)
        }
        TimerSessionType.LONG_BREAK -> {
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer
            textColor = MaterialTheme.colorScheme.onSecondaryContainer
            icon = Icons.Default.SelfImprovement
            text = stringResource(R.string.long_break)
        }
    }

    ZenCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        containerColor = backgroundColor,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            Text(
                text = stringResource(R.string.focus_session_info, cycleInfo),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
    }
}

@Composable
fun CircularTimerDisplay(
    timeRemaining: String,
    progress: Float,
    sessionType: TimerSessionType,
    isRunning: Boolean
) {
    val progressColor = when (sessionType) {
        TimerSessionType.WORK -> MaterialTheme.colorScheme.primary
        TimerSessionType.SHORT_BREAK -> MaterialTheme.colorScheme.tertiary
        TimerSessionType.LONG_BREAK -> MaterialTheme.colorScheme.secondary
    }

    // Animate progress
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    // Pulsing animation when running
    val scale by animateFloatAsState(
        targetValue = if (isRunning) 1.02f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(280.dp)
    ) {
        // Background circle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        )

        // Progress circle
        CircularProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            color = progressColor,
            strokeWidth = 12.dp,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        // Time display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = timeRemaining,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 56.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TaskNameInput(
    taskName: String,
    onTaskNameChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = taskName,
        onValueChange = onTaskNameChanged,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        label = { Text(stringResource(R.string.focus_task_name)) },
        placeholder = { Text(stringResource(R.string.focus_task_hint)) },
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Task,
                contentDescription = null
            )
        }
    )
}

@Composable
fun TimerControlButtons(
    timerStatus: TimerStatus,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (timerStatus) {
                TimerStatus.IDLE -> ZenButton(
                    text = stringResource(R.string.button_start),
                    onClick = onStart,
                    icon = Icons.Default.PlayArrow,
                    modifier = Modifier.weight(1f)
                )

                TimerStatus.RUNNING -> {
                    ZenButton(
                        text = stringResource(R.string.timer_pause),
                        onClick = onPause,
                        icon = Icons.Default.Pause,
                        modifier = Modifier.weight(1f)
                    )
                    ZenButton(
                        text = stringResource(R.string.timer_stop),
                        onClick = onStop,
                        style = ZenButtonStyle.Secondary,
                        icon = Icons.Default.Stop,
                        modifier = Modifier.weight(1f)
                    )
                }

                TimerStatus.PAUSED -> {
                    ZenButton(
                        text = stringResource(R.string.timer_resume),
                        onClick = onResume,
                        icon = Icons.Default.PlayArrow,
                        modifier = Modifier.weight(1f)
                    )
                    ZenButton(
                        text = stringResource(R.string.timer_stop),
                        onClick = onStop,
                        style = ZenButtonStyle.Secondary,
                        icon = Icons.Default.Stop,
                        modifier = Modifier.weight(1f)
                    )
                }

                TimerStatus.COMPLETED -> ZenButton(
                    text = stringResource(R.string.focus_new_session),
                    onClick = onStart,
                    icon = Icons.Default.Refresh,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Skip button (only when running or paused)
        if (timerStatus == TimerStatus.RUNNING || timerStatus == TimerStatus.PAUSED) {
            ZenButton(
                text = stringResource(R.string.focus_next_session),
                onClick = onSkip,
                style = ZenButtonStyle.Text,
                icon = Icons.Default.SkipNext,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsRow(
    hapticEnabled: Boolean,
    soundEnabled: Boolean,
    onToggleHaptic: () -> Unit,
    onToggleSound: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Haptic feedback toggle
        FilterChip(
            selected = hapticEnabled,
            onClick = onToggleHaptic,
            label = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(stringResource(R.string.focus_haptic))
                }
            },
            leadingIcon = if (hapticEnabled) {
                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
            } else null
        )

        // Sound toggle
        FilterChip(
            selected = soundEnabled,
            onClick = onToggleSound,
            label = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(stringResource(R.string.focus_sound))
                }
            },
            leadingIcon = if (soundEnabled) {
                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
            } else null
        )
    }
}

@Composable
fun TodaysStatsCard(stats: PomodoroViewModel.TodaysStats) {
    ZenCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.focus_todays_stats),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.CheckCircle,
                    label = stringResource(R.string.focus_stat_done),
                    value = "${stats.completedWorkSessions}"
                )
                StatItem(
                    icon = Icons.Default.Timer,
                    label = stringResource(R.string.focus_stat_focus),
                    value = stringResource(R.string.minutes_count, stats.totalFocusMinutes)
                )
                StatItem(
                    icon = Icons.Default.LocalFireDepartment,
                    label = stringResource(R.string.focus_stat_streak),
                    value = "${stats.currentStreak}"
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun SessionHistoryItem(session: FocusSessionData) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val sessionColor = if (session.completed) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }

    ZenCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = sessionColor,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (session.completed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (session.completed) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    },
                    modifier = Modifier.size(24.dp)
                )

                Column {
                    Text(
                        text = session.taskName ?: "Çalışma Seansı",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${session.duration / 60} dakika • ${session.date.format(timeFormatter)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            if (session.completedCycles > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${session.completedCycles} döngü",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerSettingsDialog(
    workDuration: Int,
    shortBreakDuration: Int,
    longBreakDuration: Int,
    totalCycles: Int,
    onDismiss: () -> Unit,
    onSave: (Int, Int, Int, Int) -> Unit
) {
    var workDurationState by remember { mutableStateOf(workDuration.toFloat()) }
    var shortBreakDurationState by remember { mutableStateOf(shortBreakDuration.toFloat()) }
    var longBreakDurationState by remember { mutableStateOf(longBreakDuration.toFloat()) }
    var totalCyclesState by remember { mutableStateOf(totalCycles.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.focus_timer_settings), style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Work duration slider
                Column {
                    Text(
                        text = stringResource(R.string.focus_work_duration, workDurationState.toInt()),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = workDurationState,
                        onValueChange = { workDurationState = it },
                        valueRange = 15f..60f,
                        steps = 8
                    )
                }

                // Short break duration slider
                Column {
                    Text(
                        text = stringResource(R.string.focus_short_break_duration, shortBreakDurationState.toInt()),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = shortBreakDurationState,
                        onValueChange = { shortBreakDurationState = it },
                        valueRange = 3f..15f,
                        steps = 11
                    )
                }

                // Long break duration slider
                Column {
                    Text(
                        text = stringResource(R.string.focus_long_break_duration, longBreakDurationState.toInt()),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = longBreakDurationState,
                        onValueChange = { longBreakDurationState = it },
                        valueRange = 10f..30f,
                        steps = 19
                    )
                }

                // Total cycles slider
                Column {
                    Text(
                        text = stringResource(R.string.focus_cycles_until_long, totalCyclesState.toInt()),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = totalCyclesState,
                        onValueChange = { totalCyclesState = it },
                        valueRange = 2f..6f,
                        steps = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        workDurationState.toInt(),
                        shortBreakDurationState.toInt(),
                        longBreakDurationState.toInt(),
                        totalCyclesState.toInt()
                    )
                }
            ) {
                Text(stringResource(R.string.button_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        }
    )
}

/**
 * Distraction-free full-screen timer: near-black background, only the time and progress.
 * Any tap (or the back button) returns to the normal screen.
 */
@Composable
private fun DeepFocusOverlay(
    timeRemaining: String,
    progress: Float,
    sessionType: TimerSessionType,
    isRunning: Boolean,
    onExit: () -> Unit
) {
    Dialog(
        onDismissRequest = onExit,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = ZenPrimaryDark,
                secondary = ZenSecondaryDark,
                tertiary = ZenTertiaryDark,
                background = ZenMidnight,
                surface = ZenMidnight,
                onBackground = ZenOnBackgroundDark,
                onSurface = ZenOnSurfaceDark,
                surfaceVariant = ZenSurfaceVariantDark,
                onSurfaceVariant = ZenOnSurfaceVariantDark,
                outlineVariant = ZenOutlineVariantDark
            ),
            typography = ZenTypography,
            shapes = ZenShapes
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ZenMidnight)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onExit
                    ),
                contentAlignment = Alignment.Center
            ) {
                CircularTimerDisplay(
                    timeRemaining = timeRemaining,
                    progress = progress,
                    sessionType = sessionType,
                    isRunning = isRunning
                )
                Text(
                    text = stringResource(R.string.focus_deep_exit),
                    style = MaterialTheme.typography.bodySmall,
                    color = ZenOnSurfaceVariantDark.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp)
                )
            }
        }
    }
}
