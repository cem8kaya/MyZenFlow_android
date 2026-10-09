package com.oqza.myzenflow.presentation.screens

import com.oqza.myzenflow.presentation.viewmodels.CheckInViewModel
import androidx.compose.runtime.DisposableEffect
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.oqza.myzenflow.R
import com.oqza.myzenflow.presentation.screens.components.*
import com.oqza.myzenflow.presentation.viewmodels.BreathingViewModel
import com.oqza.myzenflow.presentation.theme.breathingGradientColors

/**
 * iOS-quality BreathingScreen with smooth animations and polish
 * Features:
 * - Gradient background
 * - Canvas-based breathing circle
 * - Animated phase indicator
 * - Timer display
 * - Exercise selection sheet
 * - Sound controls sheet
 * - Session summary dialog
 * - Smooth 60 FPS animations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: BreathingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sleepTimerRemainingMs by viewModel.sleepTimerRemainingMs.collectAsState()
    val checkInViewModel: CheckInViewModel = hiltViewModel()

    // Screen stays awake while breathing, so the guide never dims mid-session
    val view = LocalView.current
    val keepScreenOn = uiState.isActive && !uiState.isPaused
    DisposableEffect(keepScreenOn) {
        view.keepScreenOn = keepScreenOn
        onDispose { view.keepScreenOn = false }
    }

    // Sheet states
    var showExerciseSheet by remember { mutableStateOf(false) }
    var showSoundSheet by remember { mutableStateOf(false) }

    // Exit dialog state
    var showExitDialog by remember { mutableStateOf(false) }

    // BackHandler for hardware back button
    BackHandler(enabled = true) {
        if (uiState.isActive) {
            showExitDialog = true
        } else {
            onNavigateBack()
        }
    }

    // Gradient background colors from theme
    val gradientColors = breathingGradientColors()

    // Status bar handling
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = MaterialTheme.colorScheme

    DisposableEffect(Unit) {
        val window = (view.context as Activity).window
        val insetsController = WindowCompat.getInsetsController(window, view)

        // Save original status bar color
        val originalStatusBarColor = window.statusBarColor
        val originalLightStatusBars = insetsController.isAppearanceLightStatusBars

        // Set transparent status bar for breathing screen
        window.statusBarColor = Color.Transparent.toArgb()
        // For dark gradient background, use light icons; for light gradient, use dark icons
        insetsController.isAppearanceLightStatusBars = !darkTheme

        onDispose {
            // Restore original status bar color when leaving the screen
            window.statusBarColor = originalStatusBarColor
            insetsController.isAppearanceLightStatusBars = originalLightStatusBars
        }
    }

    // Swipe gesture detection
    val density = LocalDensity.current
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(gradientColors)
            )
            .pointerInput(uiState.isActive) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        val dragThreshold = with(density) { 200.dp.toPx() }
                        if (dragOffset > dragThreshold) {
                            if (uiState.isActive) {
                                showExitDialog = true
                            } else {
                                onNavigateBack()
                            }
                        }
                        dragOffset = 0f
                    },
                    onDragCancel = {
                        dragOffset = 0f
                    },
                    onVerticalDrag = { _, dragAmount ->
                        if (dragAmount > 0) { // Only track downward swipes
                            dragOffset += dragAmount
                        }
                    }
                )
            }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = uiState.selectedExercise?.displayName ?: stringResource(R.string.breathing_title),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    },
                    actions = {
                        // Exercise selection button
                        if (!uiState.isActive) {
                            IconButton(onClick = { showExerciseSheet = true }) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = stringResource(R.string.exercise_select)
                                )
                            }
                        }

                        // Sound controls button
                        IconButton(onClick = { showSoundSheet = true }) {
                            Icon(
                                imageVector = if (uiState.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = stringResource(R.string.sound_settings)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top spacer
                Spacer(modifier = Modifier.weight(0.2f))

                // Main content area
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (uiState.selectedExercise != null) {
                        // Breathing circle with Canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            BreathingCircleCanvas(
                                phase = uiState.currentPhase,
                                progress = uiState.phaseProgress,
                                isActive = uiState.isActive
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Phase indicator
                        PhaseIndicator(
                            phase = uiState.currentPhase,
                            isActive = uiState.isActive
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Timer display
                        TimerDisplay(
                            phase = uiState.currentPhase,
                            phaseProgress = uiState.phaseProgress,
                            exercise = uiState.selectedExercise,
                            isActive = uiState.isActive
                        )

                        // Progress indicator
                        if (uiState.isActive && uiState.currentCycle > 0) {
                            Spacer(modifier = Modifier.height(24.dp))

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.cycle_progress, uiState.currentCycle, uiState.selectedExercise?.cycles ?: 0),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                LinearProgressIndicator(
                                    progress = uiState.totalProgress,
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .height(6.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                )
                            }
                        }
                    } else {
                        // No exercise selected state
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SelfImprovement,
                                contentDescription = stringResource(R.string.breathing_exercise),
                                modifier = Modifier.size(120.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = stringResource(R.string.select_exercise),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            FilledTonalButton(
                                onClick = { showExerciseSheet = true },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = stringResource(R.string.exercise_select)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.exercise_select))
                            }
                        }
                    }
                }

                // Bottom spacer
                Spacer(modifier = Modifier.weight(0.2f))

                // Control buttons
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    BreathingControlButtons(
                        isActive = uiState.isActive,
                        isPaused = uiState.isPaused,
                        hasExerciseSelected = uiState.selectedExercise != null,
                        onStart = { viewModel.startExercise() },
                        onPause = { viewModel.pauseExercise() },
                        onResume = { viewModel.resumeExercise() },
                        onStop = { viewModel.stopExercise() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Settings buttons
                    SettingsButtonsRow(
                        soundEnabled = uiState.soundEnabled,
                        hapticEnabled = uiState.hapticEnabled,
                        onSoundClick = { showSoundSheet = true },
                        onHapticClick = { viewModel.toggleHapticFeedback() }
                    )
                }
            }
        }

        // Exercise Selection Sheet
        if (showExerciseSheet) {
            ExerciseSelectionSheet(
                selectedExercise = uiState.selectedExercise,
                onExerciseSelected = { exercise ->
                    viewModel.selectExercise(exercise)
                },
                onDismiss = { showExerciseSheet = false }
            )
        }

        // Sound Controls Sheet
        if (showSoundSheet) {
            SoundControlsSheet(
                currentSound = uiState.selectedAmbientSound,
                volume = uiState.volume,
                soundEnabled = uiState.soundEnabled,
                onSoundSelected = { sound ->
                    viewModel.setAmbientSound(sound)
                },
                onVolumeChanged = { volume ->
                    viewModel.setVolume(volume)
                },
                onToggleSound = { enabled ->
                    viewModel.toggleSound()
                },
                sleepTimerMinutes = uiState.sleepTimerMinutes,
                sleepTimerRemainingMs = sleepTimerRemainingMs,
                onSleepTimerSelected = { viewModel.setSleepTimer(it) },
                onDismiss = { showSoundSheet = false }
            )
        }

        // Session Summary Dialog
        if (uiState.showSessionSummary && uiState.selectedExercise != null) {
            SessionSummaryDialog(
                exercise = uiState.selectedExercise!!,
                cyclesCompleted = uiState.currentCycle,
                durationSeconds = uiState.sessionDurationSeconds,
                onDismiss = { viewModel.dismissSessionSummary() },
                onMoodSelected = { checkInViewModel.submitAfterSession(it) }
            )
        }

        // Exit Confirmation Dialog
        if (showExitDialog) {
            ExitSessionDialog(
                onConfirm = {
                    viewModel.stopExercise()
                    showExitDialog = false
                    onNavigateBack()
                },
                onDismiss = {
                    showExitDialog = false
                }
            )
        }

        // Semi-transparent floating back button (visible only when session is NOT active)
        if (!uiState.isActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .systemBarsPadding(),
                contentAlignment = Alignment.TopStart
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Exit session confirmation dialog
 */
@Composable
private fun ExitSessionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = stringResource(R.string.breathing_exit_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.breathing_exit_message),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.breathing_exit))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.breathing_continue))
            }
        }
    )
}
