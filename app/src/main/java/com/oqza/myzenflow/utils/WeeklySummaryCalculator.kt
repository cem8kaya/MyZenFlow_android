package com.oqza.myzenflow.utils

import com.oqza.myzenflow.data.models.MoodEntry
import com.oqza.myzenflow.data.models.SessionData
import com.oqza.myzenflow.data.models.SessionType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Aggregates the last seven days (ending today) into the weekly summary numbers. */
data class WeeklySummary(
    /** Oldest to newest, always 7 entries: date and minutes practiced that day. */
    val days: List<Pair<LocalDate, Int>>,
    val totalMinutes: Int,
    val sessionCount: Int,
    val activeDays: Int,
    val goalMinutes: Int,
    val topType: SessionType?,
    /** Average mood this week (1..5), or null without check-ins. */
    val averageMood: Float?,
    /** Average mood minus the previous week's, or null if either week has no check-ins. */
    val moodChange: Float?
) {
    val goalProgress: Float
        get() = if (goalMinutes > 0) (totalMinutes.toFloat() / goalMinutes).coerceIn(0f, 1f) else 0f
    val hasActivity: Boolean get() = sessionCount > 0
}

object WeeklySummaryCalculator {

    fun compute(
        sessions: List<SessionData>,
        moods: List<MoodEntry>,
        goalMinutes: Int,
        today: LocalDate = LocalDate.now(),
        zone: ZoneId = ZoneId.systemDefault()
    ): WeeklySummary {
        val start = today.minusDays(6)
        val completed = sessions.filter { it.completed }
        val inWeek = completed.filter { val d = it.date.toLocalDate(); !d.isBefore(start) && !d.isAfter(today) }

        val days = (0..6).map { offset ->
            val date = start.plusDays(offset.toLong())
            val seconds = inWeek.filter { it.date.toLocalDate() == date }.sumOf { it.duration }
            date to seconds / 60
        }

        val topType = inWeek.groupingBy { it.type }.eachCount().maxByOrNull { it.value }?.key

        fun avg(from: LocalDate, to: LocalDate): Float? {
            val values = moods.filter {
                val d = Instant.ofEpochMilli(it.epochMillis).atZone(zone).toLocalDate()
                !d.isBefore(from) && !d.isAfter(to)
            }.map { it.level.value }
            return if (values.isEmpty()) null else values.average().toFloat()
        }
        val thisWeek = avg(start, today)
        val lastWeek = avg(start.minusDays(7), start.minusDays(1))

        return WeeklySummary(
            days = days,
            totalMinutes = days.sumOf { it.second },
            sessionCount = inWeek.size,
            activeDays = days.count { (date, _) -> inWeek.any { it.date.toLocalDate() == date } },
            goalMinutes = goalMinutes,
            topType = topType,
            averageMood = thisWeek,
            moodChange = if (thisWeek != null && lastWeek != null) thisWeek - lastWeek else null
        )
    }
}
