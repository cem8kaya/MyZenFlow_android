package com.oqza.myzenflow.presentation.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.oqza.myzenflow.BuildConfig
import com.oqza.myzenflow.R
import com.oqza.myzenflow.domain.billing.PlanType
import com.oqza.myzenflow.domain.billing.PremiumPlan
import com.oqza.myzenflow.domain.billing.ProductsState
import com.oqza.myzenflow.domain.billing.PurchaseEvent
import com.oqza.myzenflow.domain.billing.SubscriptionMath
import com.oqza.myzenflow.presentation.components.ZenBackdrop
import com.oqza.myzenflow.presentation.components.ZenButton
import com.oqza.myzenflow.presentation.components.ZenButtonStyle
import com.oqza.myzenflow.presentation.components.ZenReadableWidth
import com.oqza.myzenflow.presentation.components.ZenSkeleton
import com.oqza.myzenflow.presentation.navigation.goBackOrHome
import com.oqza.myzenflow.presentation.theme.ZenDawnGold
import com.oqza.myzenflow.presentation.theme.ZenSpacing
import com.oqza.myzenflow.presentation.viewmodels.PaywallViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Premium paywall. Honest by design: real prices from Google Play, renewal and cancellation terms in
 * plain sight, a clear close button, no countdowns, no hidden options, and a way to restore.
 */
@Composable
fun PaywallScreen(
    navController: NavController,
    viewModel: PaywallViewModel = hiltViewModel()
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val inFlight by viewModel.purchaseInFlight.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val successMessage = stringResource(R.string.paywall_success)
    val pendingMessage = stringResource(R.string.paywall_pending)
    val failedMessage = stringResource(R.string.paywall_failed)
    val restoredMessage = stringResource(R.string.paywall_restored)
    val noneMessage = stringResource(R.string.paywall_restore_none)
    val unreachableMessage = stringResource(R.string.paywall_restore_failed)

    LaunchedEffect(events) {
        when (events) {
            PurchaseEvent.Idle -> Unit
            PurchaseEvent.Success -> {
                viewModel.onEventHandled()
                snackbar.showSnackbar(successMessage)
                navController.goBackOrHome()
            }
            PurchaseEvent.Pending -> {
                viewModel.onEventHandled()
                snackbar.showSnackbar(pendingMessage)
            }
            PurchaseEvent.Cancelled -> viewModel.onEventHandled() // the user chose to stop: say nothing
            PurchaseEvent.Failed -> {
                viewModel.onEventHandled()
                snackbar.showSnackbar(failedMessage)
            }
        }
    }

    ZenBackdrop(modifier = Modifier.fillMaxSize()) {
        ZenReadableWidth {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = ZenSpacing.screen, vertical = ZenSpacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(ZenSpacing.xl))
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = ZenDawnGold,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(ZenSpacing.md))
                    Text(
                        text = stringResource(R.string.paywall_title),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(ZenSpacing.xs))
                    Text(
                        text = stringResource(R.string.paywall_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(ZenSpacing.xl))
                    FeatureLine(stringResource(R.string.feature_all_exercises))
                    FeatureLine(stringResource(R.string.feature_all_sounds))
                    FeatureLine(stringResource(R.string.feature_advanced_stats))
                    FeatureLine(stringResource(R.string.feature_premium_themes))

                    Spacer(Modifier.height(ZenSpacing.xl))

                    if (isPremium) {
                        Text(
                            text = stringResource(R.string.premium_description),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(ZenSpacing.lg))
                        ZenButton(
                            text = stringResource(R.string.paywall_manage),
                            style = ZenButtonStyle.Secondary,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { openSubscriptionManagement(context) }
                        )
                    } else {
                        when (val state = products) {
                            ProductsState.Loading -> {
                                repeat(3) {
                                    ZenSkeleton(modifier = Modifier.fillMaxWidth(), height = 76.dp)
                                    Spacer(Modifier.height(ZenSpacing.sm))
                                }
                            }
                            ProductsState.Unavailable -> {
                                Text(
                                    text = stringResource(R.string.paywall_load_error),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(ZenSpacing.md))
                                ZenButton(
                                    text = stringResource(R.string.paywall_retry),
                                    style = ZenButtonStyle.Secondary,
                                    onClick = { viewModel.load() }
                                )
                            }
                            is ProductsState.Ready -> PlanPicker(
                                plans = state.plans,
                                selected = selected,
                                inFlight = inFlight,
                                onSelect = viewModel::select,
                                onPurchase = { plan ->
                                    context.findActivity()?.let { viewModel.purchase(it, plan) }
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(ZenSpacing.lg))
                    TextButton(onClick = {
                        viewModel.restore { restored ->
                            scope.launch {
                                snackbar.showSnackbar(
                                    when (restored) {
                                        true -> restoredMessage
                                        false -> noneMessage
                                        null -> unreachableMessage
                                    }
                                )
                            }
                        }
                    }) { Text(stringResource(R.string.restore_purchases)) }

                    Row(horizontalArrangement = Arrangement.Center) {
                        if (BuildConfig.PRIVACY_POLICY_URL.isNotBlank()) {
                            TextButton(onClick = { openUrl(context, BuildConfig.PRIVACY_POLICY_URL) }) {
                                Text(stringResource(R.string.privacy_policy), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        if (BuildConfig.TERMS_URL.isNotBlank()) {
                            TextButton(onClick = { openUrl(context, BuildConfig.TERMS_URL) }) {
                                Text(stringResource(R.string.terms_service), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                // Clear, always-visible way out
                IconButton(
                    onClick = { navController.goBackOrHome() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(ZenSpacing.sm)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.paywall_close))
                }

                SnackbarHost(
                    hostState = snackbar,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                )
            }
        }
    }
}

@Composable
private fun FeatureLine(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ZenSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.size(ZenSpacing.md))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun PlanPicker(
    plans: List<PremiumPlan>,
    selected: PlanType,
    inFlight: Boolean,
    onSelect: (PlanType) -> Unit,
    onPurchase: (PremiumPlan) -> Unit
) {
    // If the preselected plan is not available, fall back to the first one that is
    val current = plans.firstOrNull { it.type == selected } ?: plans.first()
    val monthly = plans.firstOrNull { it.type == PlanType.MONTHLY }

    Column(modifier = Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(ZenSpacing.sm)) {
        plans.forEach { plan ->
            PlanCard(
                plan = plan,
                isSelected = plan.type == current.type,
                savingsPercent = if (plan.type == PlanType.YEARLY && monthly != null) {
                    SubscriptionMath.yearlySavingsPercent(monthly.priceMicros, plan.priceMicros)
                } else 0,
                onClick = { onSelect(plan.type) }
            )
        }
    }

    Spacer(Modifier.height(ZenSpacing.lg))

    ZenButton(
        text = stringResource(
            if (current.trialDays != null) R.string.paywall_cta_trial else R.string.paywall_cta
        ),
        enabled = !inFlight,
        modifier = Modifier.fillMaxWidth(),
        onClick = { onPurchase(current) }
    )

    Spacer(Modifier.height(ZenSpacing.sm))
    Text(
        text = stringResource(
            if (current.type == PlanType.LIFETIME) R.string.paywall_terms_lifetime else R.string.paywall_terms_subscription
        ),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun PlanCard(
    plan: PremiumPlan,
    isSelected: Boolean,
    savingsPercent: Int,
    onClick: () -> Unit
) {
    val name = stringResource(
        when (plan.type) {
            PlanType.MONTHLY -> R.string.plan_monthly
            PlanType.YEARLY -> R.string.plan_yearly
            PlanType.LIFETIME -> R.string.plan_lifetime
        }
    )
    val price = stringResource(
        when (plan.type) {
            PlanType.MONTHLY -> R.string.plan_per_month
            PlanType.YEARLY -> R.string.plan_per_year
            PlanType.LIFETIME -> R.string.plan_one_time
        },
        plan.formattedPrice
    )
    val equivalent = if (plan.type == PlanType.YEARLY) {
        plan.billingPeriod
            ?.let { SubscriptionMath.monthlyEquivalentMicros(plan.priceMicros, it) }
            ?.let { stringResource(R.string.plan_equivalent_month, formatMicros(it, plan.details.currencyCode())) }
    } else null

    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(if (isSelected) 2.dp else 1.dp, borderColor, MaterialTheme.shapes.medium)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .padding(ZenSpacing.lg)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            if (plan.type == PlanType.YEARLY && savingsPercent > 0) {
                Text(
                    text = stringResource(R.string.plan_save_percent, savingsPercent),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        plan.trialDays?.let {
            Text(
                text = stringResource(R.string.plan_trial_days, it),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(text = price, style = MaterialTheme.typography.bodyLarge)
        equivalent?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun com.android.billingclient.api.ProductDetails.currencyCode(): String =
    subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.lastOrNull()?.priceCurrencyCode
        ?: oneTimePurchaseOfferDetails?.priceCurrencyCode
        ?: "USD"

private fun formatMicros(micros: Long, currencyCode: String): String =
    NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        runCatching { currency = Currency.getInstance(currencyCode) }
    }.format(micros / 1_000_000.0)

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun openUrl(context: Context, url: String) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}

/** Opens the Google Play page where subscriptions are managed or cancelled. */
fun openSubscriptionManagement(context: Context) {
    openUrl(context, "https://play.google.com/store/account/subscriptions?package=${context.packageName}")
}
