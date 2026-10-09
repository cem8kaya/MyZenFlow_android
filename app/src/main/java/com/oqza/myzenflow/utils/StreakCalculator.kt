package com.oqza.myzenflow.utils

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Practice streaks with a built-in rest day.
 *
 * A streak is the number of consecutive days with at least one completed session. One missed
 * day is forgiven (it does not add to the count but does not break the streak either), and a
 * forgiven day can be used at most once in any 7-day window. Streaks should motivate, not
 * punish, so a single off day never wipes out weeks of practice.
 */
object StreakCalculator {

    /** Where today's streak stands, used for gentle messaging on Home. */
    enum class State {
        /** Never practiced yet. */
        NONE,
        /** There were sessions before, but the streak is over: welcome back. */
        BROKEN,
        /** A session today already secured the streak. */
        SAFE_TODAY,
        /** Last session was yesterday; practicing today keeps the streak going. */
        AT_RISK,
        /** Yesterday was skipped and forgiven; practicing today keeps the streak going. */
        REST_DAY_USED
    }

    fun current(days: Collection<LocalDate>, today: LocalDate = LocalDate.now()): Int {
        val sorted = days.filter { !it.isAfter(today) }.distinct().sortedDescending()
        if (sorted.isEmpty()) return 0

        val last = sorted.first()
        val sinceLast = ChronoUnit.DAYS.between(last, today)
        var lastBridge: LocalDate? = null
        when {
            sinceLast <= 1 -> Unit
            sinceLast == 2L -> lastBridge = today.minusDays(1) // yesterday forgiven
            else -> return 0
        }

        var count = 1
        var previous = last
        for (i in 1 until sorted.size) {
            val date = sorted[i]
            val gap = ChronoUnit.DAYS.between(date, previous)
            when {
                gap == 1L -> count++
                gap == 2L -> {
                    val bridge = previous.minusDays(1)
                    if (lastBridge != null && ChronoUnit.DAYS.between(bridge, lastBridge) < 7) return count
                    lastBridge = bridge
                    count++
                }
                else -> return count
            }
            previous = date
        }
        return count
    }

    fun longest(days: Collection<LocalDate>): Int {
        val sorted = days.distinct().sorted()
        if (sorted.isEmpty()) return 0

        var best = 1
        var count = 1
        var lastBridge: LocalDate? = null
        for (i in 1 until sorted.size) {
            val gap = ChronoUnit.DAYS.between(sorted[i - 1], sorted[i])
            when {
                gap == 1L -> count++
                gap == 2L -> {
                    val bridge = sorted[i].minusDays(1)
                    if (lastBridge == null || ChronoUnit.DAYS.between(lastBridge, bridge) >= 7) {
                        lastBridge = bridge
                        count++
                    } else {
                        count = 1
                        lastBridge = null
                    }
                }
                else -> {
                    count = 1
                    lastBridge = null
                }
            }
            best = maxOf(best, count)
        }
        return best
    }

    fun state(days: Collection<LocalDate>, today: LocalDate = LocalDate.now()): State {
        val past = days.filter { !it.isAfter(today) }
        if (past.isEmpty()) return State.NONE
        if (past.contains(today)) return State.SAFE_TODAY
        if (current(past, today) == 0) return State.BROKEN
        return if (past.contains(today.minusDays(1))) State.AT_RISK else State.REST_DAY_USED
    }
}
