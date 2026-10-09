package com.oqza.myzenflow.utils

import com.oqza.myzenflow.data.models.MoodContext
import com.oqza.myzenflow.data.models.MoodEntry
import com.oqza.myzenflow.data.models.MoodLevel
import com.oqza.myzenflow.data.models.SessionData
import com.oqza.myzenflow.data.models.SessionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class WeeklySummaryCalculatorTest {
    private val today = LocalDate.of(2026, 10, 9)
    private val zone = ZoneId.of("UTC")

    private fun session(daysAgo: Int, minutes: Int, type: SessionType = SessionType.BREATHING, completed: Boolean = true) =
        SessionData(
            date = today.minusDays(daysAgo.toLong()).atTime(8, 0),
            duration = minutes * 60,
            type = type,
            completed = completed
        )

    private fun mood(daysAgo: Int, level: MoodLevel) = MoodEntry(
        today.minusDays(daysAgo.toLong()).atTime(12, 0).atZone(zone).toInstant().toEpochMilli(),
        level,
        MoodContext.DAILY
    )

    @Test
    fun `empty week`() {
        val s = WeeklySummaryCalculator.compute(emptyList(), emptyList(), 210, today, zone)
        assertEquals(7, s.days.size)
        assertEquals(0, s.totalMinutes)
        assertFalse(s.hasActivity)
        assertNull(s.averageMood)
        assertNull(s.topType)
    }

    @Test
    fun `totals, active days and top type`() {
        val sessions = listOf(
            session(0, 10), session(0, 5, SessionType.FOCUS), session(2, 20), session(6, 15),
            session(7, 99), // outside the 7-day window
            session(1, 30, completed = false) // not completed
        )
        val s = WeeklySummaryCalculator.compute(sessions, emptyList(), 100, today, zone)
        assertEquals(50, s.totalMinutes)
        assertEquals(4, s.sessionCount)
        assertEquals(3, s.activeDays)
        assertEquals(SessionType.BREATHING, s.topType)
        assertEquals(0.5f, s.goalProgress, 0.001f)
        assertEquals(15, s.days.first().second) // oldest day is 6 days ago
        assertEquals(15, s.days.last().second)  // today: 10 + 5
    }

    @Test
    fun `goal progress is capped at one`() {
        val s = WeeklySummaryCalculator.compute(listOf(session(0, 500)), emptyList(), 100, today, zone)
        assertEquals(1f, s.goalProgress, 0.001f)
    }

    @Test
    fun `mood average and change versus previous week`() {
        val moods = listOf(mood(1, MoodLevel.GOOD), mood(3, MoodLevel.VERY_GOOD), mood(8, MoodLevel.NEUTRAL))
        val s = WeeklySummaryCalculator.compute(emptyList(), moods, 100, today, zone)
        assertEquals(4.5f, s.averageMood!!, 0.001f)
        assertEquals(1.5f, s.moodChange!!, 0.001f)
    }

    @Test
    fun `no mood change without previous week data`() {
        val s = WeeklySummaryCalculator.compute(emptyList(), listOf(mood(0, MoodLevel.BAD)), 100, today, zone)
        assertEquals(2f, s.averageMood!!, 0.001f)
        assertNull(s.moodChange)
    }
}
