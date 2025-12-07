package com.oqza.myzenflow.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oqza.myzenflow.data.models.BreathingExerciseType
import com.oqza.myzenflow.data.models.BreathingPhase
import com.oqza.myzenflow.presentation.viewmodels.BreathingViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingScreen(
    viewModel: BreathingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nefes Egzersizi") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Exercise selection dropdown
            if (!uiState.isActive) {
                ExerciseSelectionSection(
                    selectedExercise = uiState.selectedExercise,
                    onExerciseSelected = { viewModel.selectExercise(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Breathing circle animation
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.selectedExercise != null) {
                    BreathingCircle(
                        phase = uiState.currentPhase,
                        progress = uiState.phaseProgress,
                        isActive = uiState.isActive
                    )
                } else {
                    Text(
                        text = "Bir nefes egzersizi seçin",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Phase and progress info
            if (uiState.selectedExercise != null) {
                ProgressInfoSection(
                    phase = uiState.currentPhase,
                    currentCycle = uiState.currentCycle,
                    totalCycles = uiState.selectedExercise?.cycles ?: 0,
                    totalProgress = uiState.totalProgress,
                    isActive = uiState.isActive
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Control buttons
            ControlButtonsSection(
                isActive = uiState.isActive,
                isPaused = uiState.isPaused,
                selectedExercise = uiState.selectedExercise,
                onStart = { viewModel.startExercise() },
                onPause = { viewModel.pauseExercise() },
                onResume = { viewModel.resumeExercise() },
                onStop = { viewModel.stopExercise() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Settings row
            SettingsRow(
                hapticEnabled = uiState.hapticEnabled,
                soundEnabled = uiState.soundEnabled,
                onToggleHaptic = { viewModel.toggleHapticFeedback() },
                onToggleSound = { viewModel.toggleSound() }
            )
        }
    }
}

@Composable
fun ExerciseSelectionSection(
    selectedExercise: BreathingExerciseType?,
    onExerciseSelected: (BreathingExerciseType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Egzersiz Seç",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedExercise?.displayName ?: "Seçim yapın",
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
                BreathingExerciseType.ALL_EXERCISES.forEach { exercise ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = exercise.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = exercise.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            onExerciseSelected(exercise)
                            expanded = false
                        }
                    )
                }
            }
        }

        if (selectedExercise != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${selectedExercise.cycles} döngü • ${selectedExercise.totalDurationSeconds / 60} dakika",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BreathingCircle(
    phase: BreathingPhase,
    progress: Float,
    isActive: Boolean
) {
    // Calculate target scale based on phase
    val targetScale = when (phase) {
        BreathingPhase.INHALE -> 0.5f + (progress * 0.5f) // 0.5 to 1.0
        BreathingPhase.HOLD_INHALE -> 1.0f // Hold at max
        BreathingPhase.EXHALE -> 1.0f - (progress * 0.5f) // 1.0 to 0.5
        BreathingPhase.HOLD_EXHALE -> 0.5f // Hold at min
        BreathingPhase.REST -> 0.5f
    }

    // Animated scale with smooth transitions
    val animatedScale by animateFloatAsState(
        targetValue = if (isActive) targetScale else 0.5f,
        animationSpec = tween(
            durationMillis = 100,
            easing = LinearEasing
        ),
        label = "circle_scale"
    )

    // Color based on phase
    val circleColor = when (phase) {
        BreathingPhase.INHALE -> Color(0xFF4CAF50) // Green
        BreathingPhase.HOLD_INHALE -> Color(0xFF2196F3) // Blue
        BreathingPhase.EXHALE -> Color(0xFFFF9800) // Orange
        BreathingPhase.HOLD_EXHALE -> Color(0xFF9C27B0) // Purple
        BreathingPhase.REST -> Color(0xFF757575) // Gray
    }

    // Animated color transition
    val animatedColor by animateColorAsState(
        targetValue = circleColor,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "circle_color"
    )

    Box(
        modifier = Modifier
            .size(300.dp)
            .scale(animatedScale),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow circle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            animatedColor.copy(alpha = 0.3f),
                            animatedColor.copy(alpha = 0.0f)
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Main circle
        Box(
            modifier = Modifier
                .size(250.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            animatedColor.copy(alpha = 0.8f),
                            animatedColor.copy(alpha = 0.4f)
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Inner circle
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    color = animatedColor.copy(alpha = 0.9f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Phase text
            Text(
                text = getPhaseText(phase),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun ProgressInfoSection(
    phase: BreathingPhase,
    currentCycle: Int,
    totalCycles: Int,
    totalProgress: Float,
    isActive: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isActive && currentCycle > 0) {
            Text(
                text = "Döngü $currentCycle / $totalCycles",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = totalProgress,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${(totalProgress * 100).roundToInt()}% tamamlandı",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ControlButtonsSection(
    isActive: Boolean,
    isPaused: Boolean,
    selectedExercise: BreathingExerciseType?,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isActive) {
            // Start button
            FilledTonalButton(
                onClick = onStart,
                enabled = selectedExercise != null,
                modifier = Modifier.size(width = 120.dp, height = 56.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Başlat")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Başlat")
            }
        } else {
            // Pause/Resume button
            FilledTonalButton(
                onClick = if (isPaused) onResume else onPause,
                modifier = Modifier.size(width = 120.dp, height = 56.dp)
            ) {
                Icon(
                    if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPaused) "Devam" else "Duraklat"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isPaused) "Devam" else "Duraklat")
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Stop button
            OutlinedButton(
                onClick = onStop,
                modifier = Modifier.size(width = 120.dp, height = 56.dp)
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Durdur")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Durdur")
            }
        }
    }
}

@Composable
fun SettingsRow(
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
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.PhoneAndroid,
                contentDescription = "Titreşim",
                tint = if (hapticEnabled) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = hapticEnabled,
                onCheckedChange = { onToggleHaptic() }
            )
        }

        // Sound toggle
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                contentDescription = "Ses",
                tint = if (soundEnabled) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = soundEnabled,
                onCheckedChange = { onToggleSound() }
            )
        }
    }
}

fun getPhaseText(phase: BreathingPhase): String {
    return when (phase) {
        BreathingPhase.INHALE -> "İç Çek"
        BreathingPhase.HOLD_INHALE -> "Tut"
        BreathingPhase.EXHALE -> "Ver"
        BreathingPhase.HOLD_EXHALE -> "Tut"
        BreathingPhase.REST -> "Hazır"
    }
}
