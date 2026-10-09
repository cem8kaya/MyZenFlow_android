package com.oqza.myzenflow.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oqza.myzenflow.R
import com.oqza.myzenflow.data.models.MoodLevel
import com.oqza.myzenflow.presentation.theme.ZenSpacing
import com.oqza.myzenflow.presentation.viewmodels.CheckInViewModel

private fun MoodLevel.emoji(): String = when (this) {
    MoodLevel.VERY_BAD -> "😞"
    MoodLevel.BAD -> "😕"
    MoodLevel.NEUTRAL -> "😐"
    MoodLevel.GOOD -> "🙂"
    MoodLevel.VERY_GOOD -> "😄"
}

@Composable
private fun MoodLevel.label(): String = stringResource(
    when (this) {
        MoodLevel.VERY_BAD -> R.string.mood_very_bad
        MoodLevel.BAD -> R.string.mood_bad
        MoodLevel.NEUTRAL -> R.string.mood_neutral
        MoodLevel.GOOD -> R.string.mood_good
        MoodLevel.VERY_GOOD -> R.string.mood_very_good
    }
)

/**
 * Five-step mood selector. Each option is a labelled radio button for TalkBack
 * and meets the 48dp touch target.
 */
@Composable
fun MoodPicker(
    selected: MoodLevel?,
    onSelect: (MoodLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MoodLevel.entries.forEach { level ->
            val isSelected = level == selected
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.18f else 1f,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
                label = "moodScale"
            )
            val label = level.label()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = { onSelect(level) }
                    )
                    .semantics { contentDescription = label }
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = level.emoji(),
                        fontSize = 26.sp,
                        modifier = Modifier.scale(scale)
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Once-a-day "how are you feeling?" card for Home. After answering it turns into a short,
 * supportive note. Entries are stored locally only.
 */
@Composable
fun DailyCheckInCard(
    modifier: Modifier = Modifier,
    viewModel: CheckInViewModel = hiltViewModel()
) {
    val today by viewModel.todayCheckIn.collectAsStateWithLifecycle()

    ZenCard(modifier = modifier.fillMaxWidth()) {
        AnimatedContent(targetState = today, label = "checkIn") { entry ->
            if (entry == null) {
                Column {
                    Text(
                        text = stringResource(R.string.checkin_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.checkin_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(ZenSpacing.md))
                    MoodPicker(selected = null, onSelect = { viewModel.submit(it) })
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = entry.level.emoji(), fontSize = 32.sp)
                    Column(modifier = Modifier.padding(start = ZenSpacing.md)) {
                        Text(
                            text = stringResource(R.string.checkin_done_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(
                                when (entry.level) {
                                    MoodLevel.VERY_BAD, MoodLevel.BAD -> R.string.checkin_done_low
                                    MoodLevel.NEUTRAL -> R.string.checkin_done_mid
                                    MoodLevel.GOOD, MoodLevel.VERY_GOOD -> R.string.checkin_done_high
                                }
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
