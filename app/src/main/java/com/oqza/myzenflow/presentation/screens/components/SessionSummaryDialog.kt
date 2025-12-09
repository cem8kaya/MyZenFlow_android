package com.oqza.myzenflow.presentation.screens.components

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
    modifier: Modifier = Modifier
) {
    val completionRate = (cyclesCompleted.toFloat() / exercise.cycles * 100).toInt()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Tamamlandı",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Tebrikler!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
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
                    text = "Nefes egzersizini tamamladınız!",
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
                        label = "Egzersiz",
                        value = exercise.displayName
                    )

                    // Cycles completed
                    StatisticCard(
                        icon = Icons.Default.Loop,
                        label = "Döngü",
                        value = "$cyclesCompleted / ${exercise.cycles}"
                    )

                    // Total duration
                    StatisticCard(
                        icon = Icons.Default.Timer,
                        label = "Süre",
                        value = formatDuration(durationSeconds)
                    )

                    // Completion rate
                    StatisticCard(
                        icon = Icons.Default.CheckCircle,
                        label = "Tamamlanma",
                        value = "%$completionRate"
                    )
                }

                // Future features (placeholders)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Save to Health (future)
                    OutlinedButton(
                        onClick = { /* TODO: Health integration */ },
                        modifier = Modifier.weight(1f),
                        enabled = false
                    ) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = "Sağlık",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kaydet", style = MaterialTheme.typography.labelSmall)
                    }

                    // Share (future)
                    OutlinedButton(
                        onClick = { /* TODO: Share */ },
                        modifier = Modifier.weight(1f),
                        enabled = false
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Paylaş",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paylaş", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tamam")
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
                    contentDescription = label,
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
private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return if (minutes > 0) {
        "$minutes dk $secs sn"
    } else {
        "$secs sn"
    }
}
