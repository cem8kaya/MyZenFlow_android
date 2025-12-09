package com.oqza.myzenflow.presentation.screens.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Control buttons with animated states
 * Features:
 * - Start/Pause/Resume/Stop buttons
 * - Animated transitions between states
 * - FloatingActionButton style
 */
@Composable
fun BreathingControlButtons(
    isActive: Boolean,
    isPaused: Boolean,
    hasExerciseSelected: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedContent(
            targetState = Triple(isActive, isPaused, hasExerciseSelected),
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "control_buttons"
        ) { (active, paused, hasExercise) ->
            when {
                !active -> {
                    // Start button (large FAB)
                    LargeFloatingActionButton(
                        onClick = onStart,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Başlat",
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Başlat",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                else -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pause/Resume button
                        FloatingActionButton(
                            onClick = if (paused) onResume else onPause,
                            containerColor = if (paused)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (paused)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (paused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = if (paused) "Devam" else "Duraklat",
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (paused) "Devam" else "Duraklat",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        // Stop button
                        FloatingActionButton(
                            onClick = onStop,
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Durdur",
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Durdur",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Settings buttons row (sound and haptic toggles)
 */
@Composable
fun SettingsButtonsRow(
    soundEnabled: Boolean,
    hapticEnabled: Boolean,
    onSoundClick: () -> Unit,
    onHapticClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
    ) {
        // Sound button
        SettingButton(
            icon = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
            label = "Ses",
            isEnabled = soundEnabled,
            onClick = onSoundClick
        )

        // Haptic button
        SettingButton(
            icon = Icons.Default.PhoneAndroid,
            label = "Titreşim",
            isEnabled = hapticEnabled,
            onClick = onHapticClick
        )
    }
}

/**
 * Individual setting button
 */
@Composable
private fun SettingButton(
    icon: ImageVector,
    label: String,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isEnabled)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isEnabled)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label)
    }
}
