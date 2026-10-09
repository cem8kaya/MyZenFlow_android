package com.oqza.myzenflow.presentation.screens.components

import com.oqza.myzenflow.presentation.theme.LocalPremiumUnlocked
import com.oqza.myzenflow.domain.billing.PremiumPolicy
import com.oqza.myzenflow.presentation.components.ZenChip
import com.oqza.myzenflow.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oqza.myzenflow.domain.services.BreathingAudioManager

/**
 * Material 3 ModalBottomSheet for sound controls
 * Features:
 * - Ambient sound picker
 * - Volume slider
 * - Sound on/off toggle
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundControlsSheet(
    currentSound: BreathingAudioManager.AmbientSound,
    volume: Float,
    soundEnabled: Boolean,
    onSoundSelected: (BreathingAudioManager.AmbientSound) -> Unit,
    onVolumeChanged: (Float) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    sleepTimerMinutes: Int,
    sleepTimerRemainingMs: Long?,
    onSleepTimerSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onLockedClick: () -> Unit = {}
) {
    val premiumUnlocked = LocalPremiumUnlocked.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.sound_sheet_title),
                    style = MaterialTheme.typography.headlineSmall
                )

                // Sound toggle
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = onToggleSound
                )
            }

            Text(
                text = stringResource(R.string.sound_sheet_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Volume control
            if (soundEnabled) {
                VolumeControl(
                    volume = volume,
                    onVolumeChanged = onVolumeChanged,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Ambient sound list
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(getAllAmbientSounds()) { sound ->
                    SoundItem(
                        sound = sound,
                        isSelected = currentSound == sound,
                        isEnabled = soundEnabled,
                        isLocked = !premiumUnlocked && PremiumPolicy.requiresPremium(sound),
                        onClick = {
                            if (soundEnabled) {
                                if (!premiumUnlocked && PremiumPolicy.requiresPremium(sound)) {
                                    onDismiss()
                                    onLockedClick()
                                } else {
                                    onSoundSelected(sound)
                                    onDismiss()
                                }
                            }
                        }
                    )

                    if (sound != getAllAmbientSounds().last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            if (soundEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                SleepTimerSection(
                    selectedMinutes = sleepTimerMinutes,
                    remainingMs = sleepTimerRemainingMs,
                    onSelected = onSleepTimerSelected,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Sleep timer: fades the ambient sound out after the chosen time.
 */
@Composable
private fun SleepTimerSection(
    selectedMinutes: Int,
    remainingMs: Long?,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.sleep_timer_title),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(0, 10, 20, 30, 60).forEach { minutes ->
                ZenChip(
                    label = if (minutes == 0) stringResource(R.string.sleep_timer_off)
                    else stringResource(R.string.sleep_timer_minutes, minutes),
                    selected = selectedMinutes == minutes,
                    onClick = { onSelected(minutes) }
                )
            }
        }
        if (remainingMs != null) {
            Spacer(modifier = Modifier.height(8.dp))
            val totalSeconds = remainingMs / 1000
            Text(
                text = stringResource(
                    R.string.sleep_timer_remaining,
                    "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Volume control slider
 */
@Composable
private fun VolumeControl(
    volume: Float,
    onVolumeChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.sound_volume_label),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "${(volume * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.VolumeDown,
                contentDescription = stringResource(R.string.sound_volume_low),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )

            Slider(
                value = volume,
                onValueChange = onVolumeChanged,
                modifier = Modifier.weight(1f),
                valueRange = 0f..1f
            )

            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = stringResource(R.string.sound_volume_high),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Individual sound item
 */
@Composable
private fun SoundItem(
    sound: BreathingAudioManager.AmbientSound,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLocked: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        enabled = isEnabled,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected && isEnabled)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = getSoundIcon(sound),
                    contentDescription = null,
                    tint = if (isSelected && isEnabled)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (isEnabled) 1f else 0.5f
                        ),
                    modifier = Modifier.size(32.dp)
                )

                Column {
                    Text(
                        text = getSoundName(sound),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected && isEnabled)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (isEnabled) 1f else 0.5f
                            )
                    )

                    Text(
                        text = getSoundDescription(sound),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected && isEnabled)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (isEnabled) 0.6f else 0.3f
                            )
                    )
                }
            }

            if (isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = stringResource(R.string.locked_feature_desc),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (isSelected && isEnabled) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = stringResource(R.string.sound_selected),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Get all available ambient sounds
 */
private fun getAllAmbientSounds(): List<BreathingAudioManager.AmbientSound> {
    return listOf(
        BreathingAudioManager.AmbientSound.NONE,
        BreathingAudioManager.AmbientSound.OCEAN_WAVES,
        BreathingAudioManager.AmbientSound.RAIN,
        BreathingAudioManager.AmbientSound.FOREST,
        BreathingAudioManager.AmbientSound.WHITE_NOISE
    )
}

/**
 * Get icon for ambient sound
 */
private fun getSoundIcon(sound: BreathingAudioManager.AmbientSound): androidx.compose.ui.graphics.vector.ImageVector {
    return when (sound) {
        BreathingAudioManager.AmbientSound.NONE -> Icons.Default.VolumeOff
        BreathingAudioManager.AmbientSound.OCEAN_WAVES -> Icons.Default.Waves
        BreathingAudioManager.AmbientSound.RAIN -> Icons.Default.WaterDrop
        BreathingAudioManager.AmbientSound.FOREST -> Icons.Default.Park
        BreathingAudioManager.AmbientSound.WHITE_NOISE -> Icons.Default.GraphicEq
    }
}

/**
 * Get name for ambient sound
 */
@Composable
private fun getSoundName(sound: BreathingAudioManager.AmbientSound): String {
    return when (sound) {
        BreathingAudioManager.AmbientSound.NONE -> stringResource(R.string.sound_none)
        BreathingAudioManager.AmbientSound.OCEAN_WAVES -> stringResource(R.string.sound_ocean)
        BreathingAudioManager.AmbientSound.RAIN -> stringResource(R.string.sound_rain)
        BreathingAudioManager.AmbientSound.FOREST -> stringResource(R.string.sound_forest)
        BreathingAudioManager.AmbientSound.WHITE_NOISE -> stringResource(R.string.sound_white)
    }
}

/**
 * Get description for ambient sound
 */
@Composable
private fun getSoundDescription(sound: BreathingAudioManager.AmbientSound): String {
    return when (sound) {
        BreathingAudioManager.AmbientSound.NONE -> stringResource(R.string.sound_none_desc)
        BreathingAudioManager.AmbientSound.OCEAN_WAVES -> stringResource(R.string.sound_ocean_desc)
        BreathingAudioManager.AmbientSound.RAIN -> stringResource(R.string.sound_rain_desc)
        BreathingAudioManager.AmbientSound.FOREST -> stringResource(R.string.sound_forest_desc)
        BreathingAudioManager.AmbientSound.WHITE_NOISE -> stringResource(R.string.sound_white_desc)
    }
}
