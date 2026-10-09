package com.oqza.myzenflow.utils

import com.oqza.myzenflow.data.models.SessionData
import com.oqza.myzenflow.data.models.SessionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DailyStatsCalculatorTest {
    // Friday
    private val today = LocalDate.of(2026, 10, 9)

    private fun session(date: LocalDate, hour: Int = 12, minutes: Int = 10, completed: Boolean = true) =
        SessionData(date = date.atTime(hour, 0), duration = minutes * 60, type = SessionType.BREATHING, completed = completed)

    @Test
    fun `week is monday to sunday with zero filled days`() {
        val week = DailyStatsCalculator.currentWeek(listOf(session(today, minutes = 20)), today)
        assertEquals(7, week.size)
        assertEquals(LocalDate.of(2026, 10, 5), week.first().date)
        assertEquals(LocalDate.of(2026, 10, 11), week.last().date)
        assertEquals(20, week[4].minutes)
        assertEquals(1, week[4].sessions)
        assertEquals(0, week[0].minutes)
    }

    @Test
    fun `month covers every day`() {
        assertEquals(31, DailyStatsCalculator.currentMonth(emptyList(), today).size)
    }

    @Test
    fun `incomplete sessions are ignored`() {
        val week = DailyStatsCalculator.currentWeek(listOf(session(today, completed = false)), today)
        assertEquals(0, week.sumOf { it.sessions })
    }

    @Test
    fun `early bird and night owl counts`() {
        val sessions = listOf(session(today, 7), session(today, 22), session(today, 23), session(today, 12))
        assertEquals(1, DailyStatsCalculator.earlyBirdSessions(sessions, today))
        assertEquals(2, DailyStatsCalculator.nightOwlSessions(sessions, today))
    }

    @Test
    fun `consecutive weekends`() {
        val sat1 = LocalDate.of(2026, 9, 19)
        val sessions = listOf(
            session(sat1), session(sat1.plusDays(1)),  // weekend 1 (both days = one weekend)
            session(sat1.plusDays(7)),                   // weekend 2
            session(sat1.plusDays(21))                   // weekend 4 (weekend 3 missed)
        )
        assertEquals(2, DailyStatsCalculator.consecutiveWeekends(sessions, today))
    }

    @Test
    fun `no weekend sessions`() {
        assertEquals(0, DailyStatsCalculator.consecutiveWeekends(listOf(session(today)), today))
    }
}
