package com.oqza.myzenflow.domain

import com.oqza.myzenflow.domain.workers.ReminderScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime

class ReminderSchedulerTest {
    private val now = LocalDateTime.of(2026, 10, 9, 10, 0)

    @Test
    fun `later today when the time has not passed`() {
        assertEquals(Duration.ofHours(1), ReminderScheduler.delayUntilNext("11:00", now))
    }

    @Test
    fun `tomorrow when the time has passed`() {
        assertEquals(Duration.ofHours(23), ReminderScheduler.delayUntilNext("09:00", now))
    }

    @Test
    fun `exactly now schedules for tomorrow`() {
        assertEquals(Duration.ofHours(24), ReminderScheduler.delayUntilNext("10:00", now))
    }

    @Test
    fun `invalid time falls back to nine in the morning`() {
        assertEquals(Duration.ofHours(23), ReminderScheduler.delayUntilNext("nonsense", now))
    }

    @Test
    fun `delay is always positive`() {
        listOf("00:00", "09:59", "10:01", "23:59").forEach {
            assertTrue(ReminderScheduler.delayUntilNext(it, now) > Duration.ZERO)
        }
    }
}
