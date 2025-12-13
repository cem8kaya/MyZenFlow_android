package com.oqza.myzenflow.presentation.screens

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
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

    // Sheet states
    var showExerciseSheet by remember { mutableStateOf(false) }
    var showSoundSheet by remember { mutableStateOf(false) }

    // Gradient background colors from theme
    val gradientColors = breathingGradientColors()

    // Status bar handling
    val view = LocalView.current
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(gradientColors)
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = uiState.selectedExercise?.displayName ?: "Nefes Egzersizi",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Geri"
                            )
                        }
                    },
                    actions = {
                        // Exercise selection button
                        if (!uiState.isActive) {
                            IconButton(onClick = { showExerciseSheet = true }) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = "Egzersiz Seç"
                                )
                            }
                        }

                        // Sound controls button
                        IconButton(onClick = { showSoundSheet = true }) {
                            Icon(
                                imageVector = if (uiState.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "Ses Ayarları"
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
                                    text = "Döngü ${uiState.currentCycle} / ${uiState.selectedExercise?.cycles}",
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
                                contentDescription = "Nefes",
                                modifier = Modifier.size(120.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Bir nefes egzersizi seçin",
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
                                    contentDescription = "Egzersiz Seç"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Egzersiz Seç")
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
                onDismiss = { showSoundSheet = false }
            )
        }

        // Session Summary Dialog
        if (uiState.showSessionSummary && uiState.selectedExercise != null) {
            SessionSummaryDialog(
                exercise = uiState.selectedExercise!!,
                cyclesCompleted = uiState.currentCycle,
                durationSeconds = uiState.sessionDurationSeconds,
                onDismiss = { viewModel.dismissSessionSummary() }
            )
        }
    }
}
