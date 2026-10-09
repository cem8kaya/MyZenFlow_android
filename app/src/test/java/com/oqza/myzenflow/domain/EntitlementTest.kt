package com.oqza.myzenflow.domain

import com.oqza.myzenflow.domain.billing.Entitlement
import com.oqza.myzenflow.domain.billing.PurchaseSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EntitlementTest {
    private val ours = setOf("monthly", "yearly", "lifetime")

    private fun purchase(
        id: String,
        purchased: Boolean = true,
        pending: Boolean = false,
        acknowledged: Boolean = true
    ) = PurchaseSnapshot(listOf(id), purchased, pending, acknowledged)

    @Test
    fun `no purchases is not premium`() {
        assertFalse(Entitlement.isPremium(emptyList(), ours))
    }

    @Test
    fun `completed purchase of our product is premium`() {
        assertTrue(Entitlement.isPremium(listOf(purchase("yearly")), ours))
        assertTrue(Entitlement.isPremium(listOf(purchase("lifetime")), ours))
    }

    @Test
    fun `pending payment does not unlock`() {
        val p = listOf(purchase("monthly", purchased = false, pending = true))
        assertFalse(Entitlement.isPremium(p, ours))
        assertTrue(Entitlement.hasPending(p, ours))
    }

    @Test
    fun `other products are ignored`() {
        assertFalse(Entitlement.isPremium(listOf(purchase("something_else")), ours))
    }

    @Test
    fun `unacknowledged completed purchase must be acknowledged`() {
        assertTrue(Entitlement.needsAcknowledge(purchase("yearly", acknowledged = false), ours))
        assertFalse(Entitlement.needsAcknowledge(purchase("yearly", acknowledged = true), ours))
        assertFalse(Entitlement.needsAcknowledge(purchase("yearly", purchased = false, pending = true, acknowledged = false), ours))
        assertEquals(false, Entitlement.needsAcknowledge(purchase("other", acknowledged = false), ours))
    }
}
