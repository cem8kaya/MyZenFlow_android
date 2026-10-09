package com.oqza.myzenflow.presentation.screens

import com.oqza.myzenflow.presentation.theme.zenTabScreenInsets
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.oqza.myzenflow.R
import com.oqza.myzenflow.data.models.SessionType
import com.oqza.myzenflow.presentation.components.ZenButton
import com.oqza.myzenflow.presentation.components.ZenButtonStyle
import com.oqza.myzenflow.presentation.components.ZenCard
import com.oqza.myzenflow.presentation.components.ZenEmptyState
import com.oqza.myzenflow.presentation.components.ZenSkeleton
import com.oqza.myzenflow.presentation.theme.ZenSpacing
import com.oqza.myzenflow.presentation.viewmodels.WeeklySummaryViewModel
import com.oqza.myzenflow.utils.WeeklySummary
import java.time.format.TextStyle
import java.util.Locale

/**
 * Weekly review: minutes vs goal, per-day bars, activity numbers, mood trend and a share action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklySummaryScreen(
    navController: NavController? = null,
    viewModel: WeeklySummaryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        contentWindowInsets = zenTabScreenInsets(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.weekly_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        val summary = state.summary
        if (state.isLoading || summary == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(ZenSpacing.screen),
                verticalArrangement = Arrangement.spacedBy(ZenSpacing.md)
            ) {
                ZenSkeleton(modifier = Modifier.fillMaxWidth(), height = 160.dp)
                ZenSkeleton(modifier = Modifier.fillMaxWidth(), height = 140.dp)
            }
        } else if (!summary.hasActivity) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                ZenEmptyState(
                    icon = Icons.Outlined.AutoAwesome,
                    title = stringResource(R.string.weekly_empty_title),
                    message = stringResource(R.string.weekly_empty_message)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(ZenSpacing.screen),
                verticalArrangement = Arrangement.spacedBy(ZenSpacing.md)
            ) {
                item { GoalCard(summary) }
                item { DailyBarsCard(summary) }
                item { NumbersRow(summary, state.streak) }
                summary.topType?.let { item { TopTypeCard(it) } }
                item { MoodCard(summary) }
                item {
                    val shareText = stringResource(
                        R.string.weekly_share_text, summary.totalMinutes, summary.sessionCount, summary.activeDays
                    )
                    val chooserTitle = stringResource(R.string.weekly_share)
                    ZenButton(
                        text = chooserTitle,
                        icon = Icons.Default.Share,
                        style = ZenButtonStyle.Secondary,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            // User-initiated share of a short text summary; nothing leaves the device otherwise
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(send, chooserTitle))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalCard(summary: WeeklySummary) {
    ZenCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(96.dp)) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 8.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
                CircularProgressIndicator(
                    progress = { summary.goalProgress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 8.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${(summary.goalProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Column(modifier = Modifier.padding(start = ZenSpacing.lg)) {
                Text(
                    text = stringResource(R.string.weekly_minutes_big, summary.totalMinutes),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = stringResource(R.string.weekly_goal_of, summary.goalMinutes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DailyBarsCard(summary: WeeklySummary) {
    val max = (summary.days.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)
    ZenCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.weekly_daily_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(ZenSpacing.md))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.spacedBy(ZenSpacing.sm),
            verticalAlignment = Alignment.Bottom
        ) {
            summary.days.forEach { (date, minutes) ->
                val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clearAndSetSemantics { contentDescription = "$dayName: ${minutes}" },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = if (minutes > 0) minutes.toString() else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((72.dp * minutes / max).coerceAtLeast(if (minutes > 0) 6.dp else 3.dp))
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (minutes > 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                    Spacer(Modifier.height(ZenSpacing.xs))
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun NumbersRow(summary: WeeklySummary, streak: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(ZenSpacing.md), modifier = Modifier.fillMaxWidth()) {
        NumberTile(summary.activeDays.toString(), stringResource(R.string.weekly_active_days), Modifier.weight(1f))
        NumberTile(summary.sessionCount.toString(), stringResource(R.string.weekly_sessions), Modifier.weight(1f))
        NumberTile(streak.toString(), stringResource(R.string.weekly_streak), Modifier.weight(1f))
    }
}

@Composable
private fun NumberTile(value: String, label: String, modifier: Modifier = Modifier) {
    ZenCard(modifier = modifier.clearAndSetSemantics { contentDescription = "$value $label" }) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TopTypeCard(type: SessionType) {
    ZenCard(modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.weekly_top_title), style = MaterialTheme.typography.titleMedium)
        Text(
            text = stringResource(
                when (type) {
                    SessionType.MEDITATION -> R.string.session_type_meditation
                    SessionType.BREATHING -> R.string.session_type_breathing
                    SessionType.MINDFULNESS -> R.string.session_type_mindfulness
                    SessionType.SLEEP -> R.string.session_type_sleep
                    SessionType.FOCUS -> R.string.session_type_focus
                }
            ),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun MoodCard(summary: WeeklySummary) {
    ZenCard(modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.weekly_mood_title), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(ZenSpacing.xs))
        val avg = summary.averageMood
        if (avg == null) {
            Text(
                text = stringResource(R.string.weekly_mood_none),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val emoji = listOf("😞", "😕", "😐", "🙂", "😄")[(avg.coerceIn(1f, 5f) - 1f).toInt().coerceIn(0, 4)]
            Text(
                text = stringResource(R.string.weekly_mood_avg, "$emoji %.1f".format(avg)),
                style = MaterialTheme.typography.headlineSmall
            )
            val change = summary.moodChange
            if (change != null) {
                Text(
                    text = stringResource(
                        when {
                            change > 0.3f -> R.string.weekly_mood_up
                            change < -0.3f -> R.string.weekly_mood_down
                            else -> R.string.weekly_mood_same
                        }
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
