package com.oqza.myzenflow.presentation.navigation

import com.oqza.myzenflow.data.models.BreathingExerciseType
import com.oqza.myzenflow.domain.billing.PremiumPolicy
import com.oqza.myzenflow.domain.zenmaster.ZenAction

/**
 * Where a Zen Master button leads. An exercise that needs Premium, which the user does not have,
 * leads to the paywall like every other locked item in the app; it is never opened for free.
 */
fun ZenAction.toRoute(premiumUnlocked: Boolean): String = when (this) {
    is ZenAction.Breathing -> {
        val exercise = BreathingExerciseType.fromId(exerciseId)
        if (exercise != null && !premiumUnlocked && PremiumPolicy.requiresPremium(exercise)) {
            Screen.Paywall.route
        } else {
            Screen.Breathing.routeFor(exerciseId)
        }
    }
    is ZenAction.Focus -> Screen.Focus.route
    is ZenAction.Garden -> Screen.ZenGarden.route
}
