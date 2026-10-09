package com.oqza.myzenflow.domain.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.oqza.myzenflow.BuildConfig
import com.oqza.myzenflow.data.repository.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

enum class PlanType { MONTHLY, YEARLY, LIFETIME }

/** One purchasable plan, ready to show on the paywall. */
data class PremiumPlan(
    val type: PlanType,
    val productId: String,
    val formattedPrice: String,
    val priceMicros: Long,
    /** ISO-8601 renewal period for subscriptions ("P1M", "P1Y"); null for the lifetime purchase. */
    val billingPeriod: String?,
    /** Free-trial length in days if the user is eligible for one. */
    val trialDays: Int?,
    internal val details: ProductDetails,
    internal val offerToken: String?
)

sealed interface ProductsState {
    data object Loading : ProductsState
    data class Ready(val plans: List<PremiumPlan>) : ProductsState
    data object Unavailable : ProductsState
}

sealed interface PurchaseEvent {
    data object Idle : PurchaseEvent
    data object Success : PurchaseEvent
    data object Pending : PurchaseEvent
    data object Cancelled : PurchaseEvent
    data object Failed : PurchaseEvent
}

/**
 * Google Play Billing. Play is the source of truth for what the user owns; the premium flag in
 * DataStore is only a cache so the app also works offline. The cache is only ever cleared after
 * a successful query that finds no purchase.
 *
 * Purchases are verified on the device only. For stronger protection (rooted devices, refunds,
 * cancellations between app starts) add server-side verification with the Play Developer API and
 * real-time developer notifications.
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val subscriptionIds = listOf(BuildConfig.SKU_MONTHLY, BuildConfig.SKU_YEARLY)
    private val allProductIds = (subscriptionIds + BuildConfig.SKU_LIFETIME).toSet()

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    private val _products = MutableStateFlow<ProductsState>(ProductsState.Loading)
    val products: StateFlow<ProductsState> = _products.asStateFlow()

    private val _events = MutableStateFlow<PurchaseEvent>(PurchaseEvent.Idle)
    val events: StateFlow<PurchaseEvent> = _events.asStateFlow()

    fun consumeEvent() {
        _events.value = PurchaseEvent.Idle
    }

    /** Loads plans and prices from Google Play. Safe to call repeatedly. */
    suspend fun loadProducts() {
        _products.value = ProductsState.Loading
        if (!ensureConnected()) {
            _products.value = ProductsState.Unavailable
            return
        }
        val subsResult = client.queryProductDetails(
            QueryProductDetailsParams.newBuilder()
                .setProductList(subscriptionIds.map { productQuery(it, BillingClient.ProductType.SUBS) })
                .build()
        )
        val inAppResult = client.queryProductDetails(
            QueryProductDetailsParams.newBuilder()
                .setProductList(listOf(productQuery(BuildConfig.SKU_LIFETIME, BillingClient.ProductType.INAPP)))
                .build()
        )

        val details = buildList {
            if (subsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                addAll(subsResult.productDetailsList.orEmpty())
            }
            if (inAppResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                addAll(inAppResult.productDetailsList.orEmpty())
            }
        }
        val plans = details.mapNotNull { toPlan(it) }.sortedBy { it.type.ordinal }
        _products.value = if (plans.isEmpty()) ProductsState.Unavailable else ProductsState.Ready(plans)
    }

    /** Starts the Google Play purchase sheet. The result arrives in [onPurchasesUpdated]. */
    fun launchPurchase(activity: Activity, plan: PremiumPlan) {
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(plan.details)
            .apply { plan.offerToken?.let { setOfferToken(it) } }
            .build()
        val flow = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()
        val result = client.launchBillingFlow(activity, flow)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _events.value = PurchaseEvent.Failed
        }
    }

    /**
     * Asks Google Play what the user owns and updates the premium flag. Used at app start, after
     * a purchase and for "Restore purchases".
     * @return whether the user owns Premium, or null if Google Play could not be reached
     * (the cached flag is kept in that case).
     */
    suspend fun refreshEntitlements(): Boolean? {
        if (!ensureConnected()) return null
        val subs = client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        )
        val inApp = client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        )
        val ok = BillingClient.BillingResponseCode.OK
        if (subs.billingResult.responseCode != ok || inApp.billingResult.responseCode != ok) return null

        return process(subs.purchasesList + inApp.purchasesList, authoritative = true)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        scope.launch {
            when (result.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    val premium = process(purchases.orEmpty(), authoritative = false)
                    val pending = Entitlement.hasPending(purchases.orEmpty().map { it.toSnapshot() }, allProductIds)
                    _events.value = when {
                        premium -> PurchaseEvent.Success
                        pending -> PurchaseEvent.Pending
                        else -> PurchaseEvent.Failed
                    }
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> _events.value = PurchaseEvent.Cancelled
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    // Already bought on this Google account: just sync the flag
                    _events.value = if (refreshEntitlements() == true) PurchaseEvent.Success else PurchaseEvent.Failed
                }
                else -> _events.value = PurchaseEvent.Failed
            }
        }
    }

    // ---- internals ----

    /**
     * Acknowledges new purchases and updates the premium flag.
     * @param authoritative true for a full query: the flag is set to exactly what Play reports.
     * false for partial updates: the flag is only ever switched on.
     * @return whether the given purchases grant premium
     */
    private suspend fun process(purchases: List<Purchase>, authoritative: Boolean): Boolean {
        val snapshots = purchases.map { it.toSnapshot() }
        val premium = Entitlement.isPremium(snapshots, allProductIds)

        purchases.forEachIndexed { i, purchase ->
            if (Entitlement.needsAcknowledge(snapshots[i], allProductIds)) {
                client.acknowledgePurchase(
                    AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                )
            }
        }

        if (premium) {
            preferencesRepository.updatePremiumStatus(true)
        } else if (authoritative) {
            preferencesRepository.updatePremiumStatus(false)
        }
        return premium
    }

    private fun Purchase.toSnapshot() = PurchaseSnapshot(
        productIds = products,
        purchased = purchaseState == Purchase.PurchaseState.PURCHASED,
        pending = purchaseState == Purchase.PurchaseState.PENDING,
        acknowledged = isAcknowledged
    )

    private fun productQuery(id: String, type: String) =
        QueryProductDetailsParams.Product.newBuilder().setProductId(id).setProductType(type).build()

    private fun toPlan(details: ProductDetails): PremiumPlan? {
        val type = when (details.productId) {
            BuildConfig.SKU_MONTHLY -> PlanType.MONTHLY
            BuildConfig.SKU_YEARLY -> PlanType.YEARLY
            BuildConfig.SKU_LIFETIME -> PlanType.LIFETIME
            else -> return null
        }
        if (type == PlanType.LIFETIME) {
            val offer = details.oneTimePurchaseOfferDetails ?: return null
            return PremiumPlan(type, details.productId, offer.formattedPrice, offer.priceAmountMicros, null, null, details, null)
        }

        val offers = details.subscriptionOfferDetails.orEmpty()
        // Prefer an offer with a free trial (Play only returns offers the user is eligible for)
        val offer = offers.firstOrNull { o -> o.pricingPhases.pricingPhaseList.any { it.priceAmountMicros == 0L } }
            ?: offers.firstOrNull { it.offerId == null }
            ?: offers.firstOrNull()
            ?: return null
        val phases = offer.pricingPhases.pricingPhaseList
        val recurring = phases.lastOrNull() ?: return null
        val trial = phases.firstOrNull { it.priceAmountMicros == 0L }
        return PremiumPlan(
            type = type,
            productId = details.productId,
            formattedPrice = recurring.formattedPrice,
            priceMicros = recurring.priceAmountMicros,
            billingPeriod = recurring.billingPeriod,
            trialDays = trial?.let { SubscriptionMath.trialDays(it.billingPeriod) },
            details = details,
            offerToken = offer.offerToken
        )
    }

    private suspend fun ensureConnected(): Boolean {
        if (client.isReady) return true
        return suspendCancellableCoroutine { continuation ->
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (continuation.isActive) {
                        continuation.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
                    }
                }

                override fun onBillingServiceDisconnected() {
                    // Auto service reconnection is enabled; the next call connects again
                }
            })
        }
    }
}
