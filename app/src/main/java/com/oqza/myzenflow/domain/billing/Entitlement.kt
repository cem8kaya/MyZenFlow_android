package com.oqza.myzenflow.domain.billing

/** The few facts about a Play purchase that decide access. Kept separate from the Play types for testing. */
data class PurchaseSnapshot(
    val productIds: List<String>,
    val purchased: Boolean,
    val pending: Boolean,
    val acknowledged: Boolean
)

object Entitlement {
    /** Premium if any completed purchase contains one of our products. Pending payments do not unlock. */
    fun isPremium(purchases: List<PurchaseSnapshot>, ourProducts: Set<String>): Boolean =
        purchases.any { it.purchased && it.productIds.any { id -> id in ourProducts } }

    fun hasPending(purchases: List<PurchaseSnapshot>, ourProducts: Set<String>): Boolean =
        purchases.any { it.pending && it.productIds.any { id -> id in ourProducts } }

    /** Completed but not yet acknowledged purchases must be acknowledged within 3 days or Google refunds them. */
    fun needsAcknowledge(purchase: PurchaseSnapshot, ourProducts: Set<String>): Boolean =
        purchase.purchased && !purchase.acknowledged && purchase.productIds.any { it in ourProducts }
}
