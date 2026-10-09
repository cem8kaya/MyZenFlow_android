package com.oqza.myzenflow.presentation.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.oqza.myzenflow.R
import com.oqza.myzenflow.presentation.theme.ZenSpacing
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment

/**
 * Quick action tile with a brand gradient. Text stays white; gradients are dark enough
 * to keep the title readable (see colors in HomeScreen).
 */
@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ZenGradientCard(
        colors = gradientColors,
        onClick = onClick,
        modifier = modifier.heightIn(min = 144.dp),
        contentPadding = PaddingValues(ZenSpacing.lg)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(ZenSpacing.lg)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = Color.White
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.92f)
                )
            }
        }
    }
}

/**
 * Data class for quick action items
 */
data class QuickAction(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val gradientColors: List<Color>,
    val route: String
)

/**
 * Quick actions laid out as a two-column grid. Plain rows (not a lazy grid) so it can sit
 * inside a scrolling column without a fixed height.
 */
@Composable
fun QuickActionsGrid(
    actions: List<QuickAction>,
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ZenSectionHeader(title = stringResource(R.string.quick_actions_title))

        Spacer(modifier = Modifier.height(ZenSpacing.md))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ZenSpacing.screen),
            verticalArrangement = Arrangement.spacedBy(ZenSpacing.md)
        ) {
            actions.chunked(2).forEach { rowActions ->
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min), // equal card heights, even with large fonts
                    horizontalArrangement = Arrangement.spacedBy(ZenSpacing.md)
                ) {
                    rowActions.forEach { action ->
                        QuickActionCard(
                            icon = action.icon,
                            title = action.title,
                            subtitle = action.subtitle,
                            gradientColors = action.gradientColors,
                            onClick = { onActionClick(action.route) },
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    if (rowActions.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
