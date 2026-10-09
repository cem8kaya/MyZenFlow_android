package com.oqza.myzenflow.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.oqza.myzenflow.R
import com.oqza.myzenflow.presentation.theme.ZenSpacing

/**
 * Single statistic: icon, big value and label. Read by TalkBack as "value label".
 */
@Composable
fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    ZenCard(
        modifier = modifier.clearAndSetSemantics { contentDescription = "$value $label" },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = ZenSpacing.md,
            vertical = ZenSpacing.lg
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(ZenSpacing.sm))

            ZenAnimatedText(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Today's stats: sessions, minutes and streak.
 */
@Composable
fun TodayStatsRow(
    sessionCount: Int,
    minutes: Int,
    streak: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ZenSpacing.screen),
        horizontalArrangement = Arrangement.spacedBy(ZenSpacing.md)
    ) {
        StatCard(
            icon = Icons.Outlined.CheckCircle,
            label = stringResource(R.string.stat_sessions),
            value = sessionCount.toString(),
            modifier = Modifier.weight(1f)
        )

        StatCard(
            icon = Icons.Outlined.Timer,
            label = stringResource(R.string.stat_minutes),
            value = minutes.toString(),
            modifier = Modifier.weight(1f)
        )

        StatCard(
            icon = Icons.Outlined.LocalFireDepartment,
            label = stringResource(R.string.stat_streak),
            value = streak.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}
