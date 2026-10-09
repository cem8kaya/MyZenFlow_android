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
import androidx.compose.ui.res.stringResource
import com.oqza.myzenflow.R
import com.oqza.myzenflow.presentation.theme.ZenSpacing
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
    Column(modifier = modifier.fillMaxWidth()) {
        ZenSectionHeader(title = stringResource(R.string.recent_sessions))

        Spacer(modifier = Modifier.height(ZenSpacing.md))

        if (sessions.isEmpty()) {
            ZenCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ZenSpacing.screen)
            ) {
                ZenEmptyState(
                    icon = Icons.Outlined.SelfImprovement,
                    title = stringResource(R.string.no_sessions_yet),
                    message = stringResource(R.string.start_first_session),
                    actionLabel = stringResource(R.string.button_start),
                    onAction = onStartClick
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(ZenSpacing.md),
                contentPadding = PaddingValues(horizontal = ZenSpacing.screen)
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
 * Localized session title
 */
@Composable
private fun getSessionTitle(session: SessionData): String {
    if (session.type == SessionType.BREATHING) {
        session.breathingExercise?.let { return it.displayName }
    }
    return stringResource(
        when (session.type) {
            SessionType.BREATHING -> R.string.session_type_breathing
            SessionType.MEDITATION -> R.string.session_type_meditation
            SessionType.FOCUS -> R.string.session_type_focus
            SessionType.MINDFULNESS -> R.string.session_type_mindfulness
            SessionType.SLEEP -> R.string.session_type_sleep
        }
    )
}

/**
 * Format duration in minutes and seconds
 */
@Composable
private fun formatDuration(durationSeconds: Int): String {
    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    return if (minutes > 0) {
        stringResource(R.string.duration_min_sec, minutes, seconds)
    } else {
        stringResource(R.string.duration_sec, seconds)
    }
}

/**
 * Format timestamp relative to now, in the app language
 */
@Composable
private fun formatTimestamp(dateTime: java.time.LocalDateTime): String {
    val now = java.time.LocalDateTime.now()
    val minutesAgo = ChronoUnit.MINUTES.between(dateTime, now)
    val hoursAgo = ChronoUnit.HOURS.between(dateTime, now)
    val daysAgo = ChronoUnit.DAYS.between(dateTime.toLocalDate().atStartOfDay(), now.toLocalDate().atStartOfDay())

    return when {
        minutesAgo < 1 -> stringResource(R.string.time_just_now)
        minutesAgo < 60 -> stringResource(R.string.time_minutes_ago, minutesAgo.toInt())
        hoursAgo < 24 && daysAgo == 0L -> stringResource(R.string.time_hours_ago, hoursAgo.toInt())
        daysAgo == 1L -> stringResource(R.string.time_yesterday)
        daysAgo < 7 -> stringResource(R.string.time_days_ago, daysAgo.toInt())
        else -> dateTime.format(DateTimeFormatter.ofPattern("d MMM", java.util.Locale.getDefault()))
    }
}
