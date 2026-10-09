package com.oqza.myzenflow.domain.billing

import com.oqza.myzenflow.data.models.BreathingExerciseType
import com.oqza.myzenflow.domain.services.BreathingAudioManager.AmbientSound

/**
 * What is free and what needs Premium. One place, so the free tier stays a deliberate decision.
 *
 * Free: 2 breathing exercises, 2 ambient sounds, the weekly chart, mood check-ins, the focus timer,
 * the Zen garden, streaks and the weekly summary.
 * Premium: all breathing exercises, all ambient sounds, advanced statistics.
 *
 * None of this is enforced while BuildConfig.PREMIUM_ENABLED is false (see [LocalPremiumUnlocked]).
 */
object PremiumPolicy {
    private val freeExerciseIds = setOf("box_breathing", "calming_breath")
    private val freeSounds = setOf(AmbientSound.NONE, AmbientSound.OCEAN_WAVES, AmbientSound.RAIN)

    fun requiresPremium(exercise: BreathingExerciseType): Boolean = exercise.id !in freeExerciseIds

    fun requiresPremium(sound: AmbientSound): Boolean = sound !in freeSounds
}
