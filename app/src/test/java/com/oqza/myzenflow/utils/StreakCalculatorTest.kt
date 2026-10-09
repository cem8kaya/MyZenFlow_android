package com.oqza.myzenflow.utils

import com.oqza.myzenflow.utils.StreakCalculator.State
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreakCalculatorTest {

    private val today = LocalDate.of(2026, 10, 9)
    private fun daysAgo(vararg n: Int) = n.map { today.minusDays(it.toLong()) }

    @Test
    fun `empty history has no streak`() {
        assertEquals(0, StreakCalculator.current(emptyList(), today))
        assertEquals(0, StreakCalculator.longest(emptyList()))
        assertEquals(State.NONE, StreakCalculator.state(emptyList(), today))
    }

    @Test
    fun `consecutive days ending today`() {
        val days = daysAgo(0, 1, 2, 3)
        assertEquals(4, StreakCalculator.current(days, today))
        assertEquals(State.SAFE_TODAY, StreakCalculator.state(days, today))
    }

    @Test
    fun `streak stays alive when last session was yesterday`() {
        val days = daysAgo(1, 2, 3)
        assertEquals(3, StreakCalculator.current(days, today))
        assertEquals(State.AT_RISK, StreakCalculator.state(days, today))
    }

    @Test
    fun `one skipped day is forgiven`() {
        // today, (yesterday skipped is not the case) -> sessions today, 2, 3 days ago: gap of one day
        val days = daysAgo(0, 2, 3)
        assertEquals(3, StreakCalculator.current(days, today))
    }

    @Test
    fun `skipping yesterday keeps the streak alive today using the rest day`() {
        val days = daysAgo(2, 3, 4)
        assertEquals(3, StreakCalculator.current(days, today))
        assertEquals(State.REST_DAY_USED, StreakCalculator.state(days, today))
    }

    @Test
    fun `two missed days break the streak`() {
        val days = daysAgo(3, 4, 5)
        assertEquals(0, StreakCalculator.current(days, today))
        assertEquals(State.BROKEN, StreakCalculator.state(days, today))
    }

    @Test
    fun `second forgiven day inside seven days ends the streak`() {
        // 0, [1 skipped], 2, [3 skipped], 4, 5
        val days = daysAgo(0, 2, 4, 5)
        // walking back: 0 -> 2 (bridge 1), 2 -> 4 would need a bridge at 3 within 7 days: stop
        assertEquals(2, StreakCalculator.current(days, today))
    }

    @Test
    fun `forgiven days a week apart both count`() {
        // 0,1 then skip 2, 3..9 (7 days), skip 10, 11,12
        val days = daysAgo(0, 1, 3, 4, 5, 6, 7, 8, 9, 11, 12)
        // bridge at 2 and bridge at 10: 8 days apart -> allowed
        assertEquals(11, StreakCalculator.current(days, today))
    }

    @Test
    fun `longest streak across history`() {
        val a = LocalDate.of(2026, 1, 1)
        val days = listOf(a, a.plusDays(1), a.plusDays(2), a.plusDays(10), a.plusDays(11))
        assertEquals(3, StreakCalculator.longest(days))
    }

    @Test
    fun `longest streak bridges one skipped day`() {
        val a = LocalDate.of(2026, 1, 1)
        val days = listOf(a, a.plusDays(1), a.plusDays(3), a.plusDays(4))
        assertEquals(4, StreakCalculator.longest(days))
    }

    @Test
    fun `future dates and duplicates are ignored`() {
        val days = daysAgo(0, 0, 1) + listOf(today.plusDays(3))
        assertEquals(2, StreakCalculator.current(days, today))
    }
}
