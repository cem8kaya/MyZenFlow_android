package com.oqza.myzenflow.domain.billing

import java.time.Period

/** Price helpers for the paywall. Pure functions, so they are easy to test. */
object SubscriptionMath {

    /**
     * Price of one month for a plan billed every [billingPeriod] (ISO-8601 like "P1M", "P1Y", "P1W"),
     * in micro-units of the currency. Null if the period is unknown or empty.
     */
    fun monthlyEquivalentMicros(priceMicros: Long, billingPeriod: String): Long? {
        val period = runCatching { Period.parse(billingPeriod) }.getOrNull() ?: return null
        val months = period.toTotalMonths() + period.days / 30.0
        if (months <= 0.0) return null
        return (priceMicros / months).toLong()
    }

    /** Whole-percent saving of [yearlyMicros] against paying [monthlyMicros] twelve times (0 if none). */
    fun yearlySavingsPercent(monthlyMicros: Long, yearlyMicros: Long): Int {
        if (monthlyMicros <= 0L || yearlyMicros <= 0L) return 0
        val full = monthlyMicros * 12.0
        return (((full - yearlyMicros) / full) * 100).toInt().coerceAtLeast(0)
    }

    /** Length of a free trial phase in days, from an ISO-8601 period ("P7D", "P1W", "P1M"); null if invalid. */
    fun trialDays(billingPeriod: String): Int? {
        val period = runCatching { Period.parse(billingPeriod) }.getOrNull() ?: return null
        val days = period.toTotalMonths() * 30 + period.days
        return if (days > 0) days.toInt() else null
    }
}
