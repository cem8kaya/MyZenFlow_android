package com.oqza.myzenflow.utils

import com.oqza.myzenflow.data.models.SessionData
import com.oqza.myzenflow.presentation.viewmodels.DailyStats
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** Pure helpers behind the Zen Garden statistics charts and special achievements. */
object DailyStatsCalculator {

    /** One entry per day from [from] to [to] inclusive. Days without sessions have zeros. */
    fun daily(sessions: List<SessionData>, from: LocalDate, to: LocalDate): List<DailyStats> {
        val byDay = sessions.filter { it.completed }.groupBy { it.date.toLocalDate() }
        return generateSequence(from) { it.plusDays(1) }
            .takeWhile { !it.isAfter(to) }
            .map { day ->
                val list = byDay[day].orEmpty()
                DailyStats(date = day, minutes = list.sumOf { it.duration } / 60, sessions = list.size)
            }
            .toList()
    }

    /** Monday to Sunday of the week containing [today]. */
    fun currentWeek(sessions: List<SessionData>, today: LocalDate = LocalDate.now()): List<DailyStats> =
        daily(
            sessions,
            today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
            today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        )

    /** First to last day of the month containing [today]. */
    fun currentMonth(sessions: List<SessionData>, today: LocalDate = LocalDate.now()): List<DailyStats> =
        daily(
            sessions,
            today.with(TemporalAdjusters.firstDayOfMonth()),
            today.with(TemporalAdjusters.lastDayOfMonth())
        )

    /** Completed sessions that started before 08:00 in the last year. */
    fun earlyBirdSessions(sessions: List<SessionData>, today: LocalDate = LocalDate.now()): Int =
        sessions.count { it.completed && it.date.toLocalDate() >= today.minusYears(1) && it.date.hour < 8 }

    /** Completed sessions that started at or after 22:00 in the last year. */
    fun nightOwlSessions(sessions: List<SessionData>, today: LocalDate = LocalDate.now()): Int =
        sessions.count { it.completed && it.date.toLocalDate() >= today.minusYears(1) && it.date.hour >= 22 }

    /**
     * Number of consecutive weekends (Saturday or Sunday) with a session, looking at the last two
     * months. A weekend counts as one unit even if both days were used.
     */
    fun consecutiveWeekends(sessions: List<SessionData>, today: LocalDate = LocalDate.now()): Int {
        val weekends = sessions
            .filter { it.completed && it.date.toLocalDate() >= today.minusMonths(2) }
            .map { it.date.toLocalDate() }
            .filter { it.dayOfWeek == DayOfWeek.SATURDAY || it.dayOfWeek == DayOfWeek.SUNDAY }
            // Saturday and Sunday of the same weekend share one Monday-based week start
            .map { it.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
            .distinct()
            .sorted()
        if (weekends.isEmpty()) return 0

        var best = 1
        var run = 1
        for (i in 1 until weekends.size) {
            run = if (weekends[i].toEpochDay() - weekends[i - 1].toEpochDay() == 7L) run + 1 else 1
            best = maxOf(best, run)
        }
        return best
    }
}
