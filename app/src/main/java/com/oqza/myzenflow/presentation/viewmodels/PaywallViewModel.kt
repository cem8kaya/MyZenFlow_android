package com.oqza.myzenflow.presentation.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oqza.myzenflow.data.repository.PreferencesRepository
import com.oqza.myzenflow.domain.billing.BillingManager
import com.oqza.myzenflow.domain.billing.PlanType
import com.oqza.myzenflow.domain.billing.PremiumPlan
import com.oqza.myzenflow.domain.billing.ProductsState
import com.oqza.myzenflow.domain.billing.PurchaseEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billing: BillingManager,
    preferencesRepository: PreferencesRepository
) : ViewModel() {

    val products: StateFlow<ProductsState> = billing.products
    val events: StateFlow<PurchaseEvent> = billing.events

    val isPremium: StateFlow<Boolean> = preferencesRepository.userPreferences
        .map { it.isPremiumUnlocked }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _selected = MutableStateFlow(PlanType.YEARLY)
    val selected: StateFlow<PlanType> = _selected.asStateFlow()

    private val _purchaseInFlight = MutableStateFlow(false)
    val purchaseInFlight: StateFlow<Boolean> = _purchaseInFlight.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            billing.loadProducts()
            billing.refreshEntitlements()
        }
    }

    fun select(type: PlanType) {
        _selected.value = type
    }

    fun purchase(activity: Activity, plan: PremiumPlan) {
        _purchaseInFlight.value = true
        billing.launchPurchase(activity, plan)
    }

    /** Called when the purchase sheet closed with a result (success, cancel or error). */
    fun onEventHandled() {
        _purchaseInFlight.value = false
        billing.consumeEvent()
    }

    /** @param onResult true = premium restored, false = nothing found, null = Google Play unreachable */
    fun restore(onResult: (restored: Boolean?) -> Unit) {
        viewModelScope.launch { onResult(billing.refreshEntitlements()) }
    }
}
