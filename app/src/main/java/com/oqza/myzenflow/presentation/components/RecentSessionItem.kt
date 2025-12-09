package com.oqza.myzenflow.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oqza.myzenflow.data.models.SessionData
import com.oqza.myzenflow.data.models.SessionType
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Recent session item component
 * Displays session information in a horizontal card
 */
@Composable
fun RecentSessionItem(
    session: SessionData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(260.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Icon(
                imageVector = getSessionIcon(session.type),
                contentDescription = session.type.displayName,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Session info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = getSessionTitle(session),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatDuration(session.duration),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = formatTimestamp(session.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * Recent sessions section with horizontal scrolling
 */
@Composable
fun RecentSessionsSection(
    sessions: List<SessionData>,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Son Seanslar",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (sessions.isEmpty()) {
            // Empty state
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SelfImprovement,
                        contentDescription = "Henüz seans yok",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Henüz seans kaydınız yok",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "İlk seansınıza başlayın!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = onStartClick) {
                        Text("Başla")
                    }
                }
            }
        } else {
            // Sessions list
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) {
                items(sessions) { session ->
                    RecentSessionItem(session = session)
                }
            }
        }
    }
}

/**
 * Get icon for session type
 */
private fun getSessionIcon(type: SessionType): ImageVector {
    return when (type) {
        SessionType.BREATHING -> Icons.Outlined.Air
        SessionType.MEDITATION -> Icons.Outlined.SelfImprovement
        SessionType.FOCUS -> Icons.Outlined.Timer
        SessionType.MINDFULNESS -> Icons.Outlined.SelfImprovement
        SessionType.SLEEP -> Icons.Outlined.SelfImprovement // Using SelfImprovement as fallback, or use Bedtime if available
    }
}

/**
 * Get session title
 */
private fun getSessionTitle(session: SessionData): String {
    return when (session.type) {
        SessionType.BREATHING -> session.breathingExercise?.displayName ?: session.type.displayName
        SessionType.MEDITATION -> session.type.displayName
        SessionType.FOCUS -> session.type.displayName
        SessionType.MINDFULNESS -> session.type.displayName
        SessionType.SLEEP -> session.type.displayName
    }
}

/**
 * Format duration in minutes and seconds
 */
private fun formatDuration(durationSeconds: Int): String {
    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    return if (minutes > 0) {
        "$minutes dk $seconds sn"
    } else {
        "$seconds sn"
    }
}

/**
 * Format timestamp relative to now
 */
private fun formatTimestamp(dateTime: java.time.LocalDateTime): String {
    val now = java.time.LocalDateTime.now()
    val minutesAgo = ChronoUnit.MINUTES.between(dateTime, now)
    val hoursAgo = ChronoUnit.HOURS.between(dateTime, now)
    val daysAgo = ChronoUnit.DAYS.between(dateTime, now)

    return when {
        minutesAgo < 1 -> "Az önce"
        minutesAgo < 60 -> "$minutesAgo dakika önce"
        hoursAgo < 24 -> "$hoursAgo saat önce"
        daysAgo == 1L -> "Dün"
        daysAgo < 7 -> "$daysAgo gün önce"
        else -> {
            val formatter = DateTimeFormatter.ofPattern("d MMM", java.util.Locale("tr"))
            dateTime.format(formatter)
        }
    }
}
