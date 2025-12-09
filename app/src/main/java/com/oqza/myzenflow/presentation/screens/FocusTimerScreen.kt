package com.oqza.myzenflow.presentation.screens

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pomodoro Timer") },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                    onStart = { viewModel.startTimer() },
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
                        text = "Bugünün Seansları",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
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
            text = "Odaklanma Modu",
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
            text = "Çalışma Seansı"
        }
        TimerSessionType.SHORT_BREAK -> {
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
            textColor = MaterialTheme.colorScheme.onTertiaryContainer
            icon = Icons.Default.Coffee
            text = "Kısa Mola"
        }
        TimerSessionType.LONG_BREAK -> {
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer
            textColor = MaterialTheme.colorScheme.onSecondaryContainer
            icon = Icons.Default.SelfImprovement
            text = "Uzun Mola"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
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
                text = "Seans: $cycleInfo",
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
        label = { Text("Görev Adı (Opsiyonel)") },
        placeholder = { Text("Üzerinde çalıştığın görevi gir") },
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
                TimerStatus.IDLE -> {
                    // Start button
                    Button(
                        onClick = onStart,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Başla", style = MaterialTheme.typography.titleMedium)
                    }
                }

                TimerStatus.RUNNING -> {
                    // Pause button
                    Button(
                        onClick = onPause,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Duraklat", style = MaterialTheme.typography.titleMedium)
                    }

                    // Stop button
                    OutlinedButton(
                        onClick = onStop,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Durdur", style = MaterialTheme.typography.titleMedium)
                    }
                }

                TimerStatus.PAUSED -> {
                    // Resume button
                    Button(
                        onClick = onResume,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Resume",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Devam Et", style = MaterialTheme.typography.titleMedium)
                    }

                    // Stop button
                    OutlinedButton(
                        onClick = onStop,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Durdur", style = MaterialTheme.typography.titleMedium)
                    }
                }

                TimerStatus.COMPLETED -> {
                    // Start new session button
                    Button(
                        onClick = onStart,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Start New",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Yeni Seans", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        // Skip button (only when running or paused)
        if (timerStatus == TimerStatus.RUNNING || timerStatus == TimerStatus.PAUSED) {
            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Skip",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sonraki Seans", style = MaterialTheme.typography.bodyLarge)
            }
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
                        contentDescription = "Haptic",
                        modifier = Modifier.size(18.dp)
                    )
                    Text("Titreşim")
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
                        contentDescription = "Sound",
                        modifier = Modifier.size(18.dp)
                    )
                    Text("Ses")
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Bugünün İstatistikleri",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.CheckCircle,
                    label = "Tamamlanan",
                    value = "${stats.completedWorkSessions}"
                )
                StatItem(
                    icon = Icons.Default.Timer,
                    label = "Odaklanma",
                    value = "${stats.totalFocusMinutes} dk"
                )
                StatItem(
                    icon = Icons.Default.LocalFireDepartment,
                    label = "Seri",
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = sessionColor
        ),
        shape = RoundedCornerShape(12.dp)
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
            Text("Zamanlayıcı Ayarları", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Work duration slider
                Column {
                    Text(
                        text = "Çalışma Süresi: ${workDurationState.toInt()} dakika",
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
                        text = "Kısa Mola: ${shortBreakDurationState.toInt()} dakika",
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
                        text = "Uzun Mola: ${longBreakDurationState.toInt()} dakika",
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
                        text = "Uzun Molaya Kadar: ${totalCyclesState.toInt()} döngü",
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
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
