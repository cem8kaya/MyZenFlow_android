package com.oqza.myzenflow.presentation.components

import com.oqza.myzenflow.presentation.theme.LocalReducedMotion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import com.oqza.myzenflow.R
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oqza.myzenflow.presentation.viewmodels.DailyStats
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

/**
 * Weekly bar chart component
 * Shows 7 days of session activity
 */
@Composable
fun WeeklyBarChart(
    weeklyData: List<DailyStats>,
    modifier: Modifier = Modifier
) {
    ZenCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.chart_this_week),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (weeklyData.isEmpty()) {
                Text(
                    text = stringResource(R.string.chart_no_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                // Bar chart
                BarChart(
                    data = weeklyData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    barColor = MaterialTheme.colorScheme.primary
                )

                // Summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ChartSummaryItem(
                        label = stringResource(R.string.chart_total),
                        value = stringResource(R.string.sessions_count, weeklyData.sumOf { it.sessions }),
                        color = MaterialTheme.colorScheme.primary
                    )
                    ChartSummaryItem(
                        label = stringResource(R.string.chart_total),
                        value = stringResource(R.string.minutes_count, weeklyData.sumOf { it.minutes }),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    ChartSummaryItem(
                        label = stringResource(R.string.weekly_active_days),
                        value = "${weeklyData.count { it.sessions > 0 }}/${weeklyData.size}",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

/**
 * Monthly bar chart component
 * Shows current month's session activity
 */
@Composable
fun MonthlyBarChart(
    monthlyData: List<DailyStats>,
    modifier: Modifier = Modifier
) {
    ZenCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.chart_this_month),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (monthlyData.isEmpty()) {
                Text(
                    text = stringResource(R.string.chart_no_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                // Simplified bar chart for month (showing weekly aggregates)
                val weeklyAggregates = aggregateByWeek(monthlyData)
                BarChart(
                    data = weeklyAggregates,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    barColor = MaterialTheme.colorScheme.secondary
                )

                // Summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ChartSummaryItem(
                        label = stringResource(R.string.chart_total),
                        value = stringResource(R.string.sessions_count, monthlyData.sumOf { it.sessions }),
                        color = MaterialTheme.colorScheme.primary
                    )
                    ChartSummaryItem(
                        label = stringResource(R.string.chart_total),
                        value = stringResource(R.string.minutes_count, monthlyData.sumOf { it.minutes }),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    val avgPerDay = monthlyData.filter { it.sessions > 0 }
                        .let { if (it.isEmpty()) 0 else it.sumOf { d -> d.minutes } / it.size }
                    ChartSummaryItem(
                        label = stringResource(R.string.chart_avg_per_day),
                        value = stringResource(R.string.minutes_count, avgPerDay),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

/**
 * Generic bar chart canvas.
 *
 * Bars grow in once when the chart first appears (skipped with reduced motion); after that a data
 * change simply redraws, so a refresh can never make the chart blink or restart.
 */
@Composable
private fun BarChart(
    data: List<DailyStats>,
    modifier: Modifier = Modifier,
    barColor: Color
) {
    val textMeasurer = rememberTextMeasurer()
    val maxValue = data.maxOfOrNull { it.sessions }?.coerceAtLeast(1) ?: 1
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val locale = Locale.getDefault()
    val compact = data.size > 10 // month view: day numbers every few bars, no value labels

    val reducedMotion = LocalReducedMotion.current
    var entered by remember { mutableStateOf(reducedMotion) }
    LaunchedEffect(Unit) { entered = true }
    val grow by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(durationMillis = if (reducedMotion) 0 else 600),
        label = "bar_grow"
    )

    val description = remember(data, locale) {
        data.joinToString(", ") {
            "${it.date.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, locale)} ${it.date.dayOfMonth}: ${it.sessions}"
        }
    }

    Canvas(modifier = modifier.semantics { contentDescription = description }) {
        if (data.isEmpty()) return@Canvas
        val slot = size.width / data.size
        val barWidth = slot * 0.62f
        val chartHeight = size.height * 0.78f
        val bottomPadding = size.height * 0.2f
        val baseline = size.height - bottomPadding

        data.forEachIndexed { index, stat ->
            val x = index * slot + (slot - barWidth) / 2f
            val fraction = stat.sessions.toFloat() / maxValue.toFloat()
            val barHeight = (chartHeight * fraction * grow).coerceAtLeast(if (stat.sessions > 0) 4f else 0f)

            // Track (empty-day marker) so every day has a visible slot
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(x, baseline - 4f),
                size = Size(barWidth, 4f),
                cornerRadius = CornerRadius(2f, 2f)
            )

            if (barHeight > 0f) {
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, baseline - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }

            // Value on top of the bar (week view only)
            if (!compact && stat.sessions > 0) {
                val value = textMeasurer.measure(
                    text = stat.sessions.toString(),
                    style = TextStyle(color = barColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                )
                drawText(
                    textLayoutResult = value,
                    topLeft = Offset(
                        x + (barWidth - value.size.width) / 2,
                        baseline - barHeight - value.size.height - 4f
                    )
                )
            }

            // Axis label: weekday initial for a week, day number every 5th day for a month
            val showLabel = !compact || index % 5 == 0 || index == data.lastIndex
            if (showLabel) {
                val text = if (compact) {
                    stat.date.dayOfMonth.toString()
                } else {
                    stat.date.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, locale)
                }
                val label = textMeasurer.measure(
                    text = text,
                    style = TextStyle(color = labelColor, fontSize = 11.sp)
                )
                drawText(
                    textLayoutResult = label,
                    topLeft = Offset(
                        x + (barWidth - label.size.width) / 2,
                        baseline + 8f
                    )
                )
            }
        }
    }
}

/**
 * Chart summary item
 */
@Composable
private fun ChartSummaryItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * Aggregate daily data by week for monthly view
 */
private fun aggregateByWeek(dailyStats: List<DailyStats>): List<DailyStats> {
    if (dailyStats.isEmpty()) return emptyList()

    val weeks = dailyStats.groupBy {
        it.date.month to (it.date.dayOfMonth / 7)
    }

    return weeks.map { (_, weekStats) ->
        DailyStats(
            date = weekStats.first().date,
            minutes = weekStats.sumOf { it.minutes },
            sessions = weekStats.sumOf { it.sessions }
        )
    }
}

/**
 * All-time stats summary card
 */
@Composable
fun AllTimeStatsCard(
    totalSessions: Int,
    totalMinutes: Int,
    longestStreak: Int,
    favoriteExercise: String?,
    modifier: Modifier = Modifier
) {
    ZenCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.chart_all_time),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AllTimeStatItem(
                    label = stringResource(R.string.chart_total_sessions),
                    value = totalSessions.toString(),
                    icon = "🧘"
                )
                AllTimeStatItem(
                    label = stringResource(R.string.chart_total_time),
                    value = stringResource(R.string.hours_minutes_short, totalMinutes / 60, totalMinutes % 60),
                    icon = "⏱️"
                )
            }

            Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AllTimeStatItem(
                    label = stringResource(R.string.chart_longest_streak),
                    value = stringResource(R.string.days_short, longestStreak),
                    icon = "🔥"
                )
                AllTimeStatItem(
                    label = stringResource(R.string.chart_favorite),
                    value = favoriteExercise ?: "N/A",
                    icon = "💨"
                )
            }
        }
    }
}

/**
 * All-time stat item
 */
@Composable
private fun AllTimeStatItem(
    label: String,
    value: String,
    icon: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = icon,
            fontSize = 24.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}
