package com.oqza.myzenflow.presentation.screens.components

import com.oqza.myzenflow.presentation.components.MoodPicker
import com.oqza.myzenflow.data.models.MoodLevel
import com.oqza.myzenflow.R
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.oqza.myzenflow.data.models.BreathingExerciseType

/**
 * Session summary dialog shown after exercise completion
 * Features:
 * - Statistics: cycles completed, total duration, completion rate
 * - Future placeholders for Health integration and sharing
 */
@Composable
fun SessionSummaryDialog(
    exercise: BreathingExerciseType,
    cyclesCompleted: Int,
    durationSeconds: Int,
    onDismiss: () -> Unit,
    onMoodSelected: (MoodLevel) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedMood by remember { mutableStateOf<MoodLevel?>(null) }
    val completionRate = (cyclesCompleted.toFloat() / exercise.cycles * 100).toInt()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.summary_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.summary_message),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Statistics cards
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Exercise name
                    StatisticCard(
                        icon = Icons.Default.FavoriteBorder,
                        label = stringResource(R.string.summary_exercise),
                        value = exercise.displayName
                    )

                    // Cycles completed
                    StatisticCard(
                        icon = Icons.Default.Loop,
                        label = stringResource(R.string.summary_cycles),
                        value = "$cyclesCompleted / ${exercise.cycles}"
                    )

                    // Total duration
                    StatisticCard(
                        icon = Icons.Default.Timer,
                        label = stringResource(R.string.summary_duration),
                        value = formatDuration(durationSeconds)
                    )

                    // Completion rate
                    StatisticCard(
                        icon = Icons.Default.CheckCircle,
                        label = stringResource(R.string.summary_completion),
                        value = "$completionRate%"
                    )
                }

                // How do you feel now? (stored on this device only)
                Text(
                    text = stringResource(R.string.aftersession_mood),
                    style = MaterialTheme.typography.titleSmall
                )
                MoodPicker(
                    selected = selectedMood,
                    onSelect = {
                        selectedMood = it
                        onMoodSelected(it)
                    }
                )
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.summary_ok))
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

/**
 * Statistic card item
 */
@Composable
private fun StatisticCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Format duration in seconds to readable format
 */
@Composable
private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return if (minutes > 0) {
        stringResource(R.string.duration_min_sec, minutes, secs)
    } else {
        stringResource(R.string.duration_sec, secs)
    }
}
