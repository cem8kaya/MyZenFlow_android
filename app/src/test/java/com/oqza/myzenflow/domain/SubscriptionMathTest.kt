package com.oqza.myzenflow.domain

import com.oqza.myzenflow.domain.billing.SubscriptionMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SubscriptionMathTest {
    @Test
    fun `yearly price divided into months`() {
        assertEquals(10_000_000L, SubscriptionMath.monthlyEquivalentMicros(120_000_000L, "P1Y"))
    }

    @Test
    fun `monthly stays monthly`() {
        assertEquals(49_990_000L, SubscriptionMath.monthlyEquivalentMicros(49_990_000L, "P1M"))
    }

    @Test
    fun `invalid or empty period gives null`() {
        assertNull(SubscriptionMath.monthlyEquivalentMicros(1_000_000L, "nonsense"))
        assertNull(SubscriptionMath.monthlyEquivalentMicros(1_000_000L, "P0D"))
    }

    @Test
    fun `savings percent`() {
        // 10/month = 120/year; yearly at 60 saves 50%
        assertEquals(50, SubscriptionMath.yearlySavingsPercent(10_000_000L, 60_000_000L))
        assertEquals(0, SubscriptionMath.yearlySavingsPercent(10_000_000L, 130_000_000L))
        assertEquals(0, SubscriptionMath.yearlySavingsPercent(0L, 60_000_000L))
    }

    @Test
    fun `trial days`() {
        assertEquals(7, SubscriptionMath.trialDays("P7D"))
        assertEquals(7, SubscriptionMath.trialDays("P1W"))
        assertEquals(30, SubscriptionMath.trialDays("P1M"))
        assertNull(SubscriptionMath.trialDays("x"))
    }
}
