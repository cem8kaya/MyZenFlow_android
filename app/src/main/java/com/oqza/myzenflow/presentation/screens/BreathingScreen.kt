package com.oqza.myzenflow.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oqza.myzenflow.presentation.screens.components.*
import com.oqza.myzenflow.presentation.viewmodels.BreathingViewModel

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

    // Gradient background colors
    val gradientColors = listOf(
        Color(0xFF1A1A2E), // Deep dark blue
        Color(0xFF16213E), // Navy
        Color(0xFF0F3460)  // Deep purple-blue
    )

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
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Transparent,
            contentColor = Color.White
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
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                LinearProgressIndicator(
                                    progress = uiState.totalProgress,
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .height(6.dp),
                                    color = Color(0xFF4A90E2),
                                    trackColor = Color.White.copy(alpha = 0.2f)
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
                                tint = Color.White.copy(alpha = 0.5f)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Bir nefes egzersizi seçin",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            FilledTonalButton(
                                onClick = { showExerciseSheet = true },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.2f),
                                    contentColor = Color.White
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
