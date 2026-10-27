package com.oqza.myzenflow.domain.zenmaster

import androidx.annotation.StringRes
import com.oqza.myzenflow.R
import com.oqza.myzenflow.data.models.BreathingExerciseType

/**
 * What the user is asking the Zen Master about. Declaration order is the tie-break when two
 * intents match equally well, so keep the most specific, most urgent ones first.
 */
enum class ZenIntent {
    STRESS,
    FOCUS,
    SLEEP,
    BREATHING,
    MOTIVATION,
    MEDITATION,
    PROGRESS,
    GENERAL;

    /**
     * Where the reply's button leads. Derived from the intent, so it is the same for every
     * message of that intent and nothing has to be stored with the message.
     */
    val action: ZenAction?
        get() = when (this) {
            STRESS, BREATHING -> ZenAction.Breathing(BreathingExerciseType.BOX_BREATHING.id, R.string.zm_action_breathing)
            SLEEP -> ZenAction.Breathing(BreathingExerciseType.TECHNIQUE_478.id, R.string.zm_action_478)
            MEDITATION -> ZenAction.Breathing(BreathingExerciseType.CALMING_BREATH.id, R.string.zm_action_meditation)
            FOCUS -> ZenAction.Focus(R.string.zm_action_focus)
            MOTIVATION -> ZenAction.Garden(R.string.zm_action_garden)
            PROGRESS -> ZenAction.Garden(R.string.zm_action_progress)
            GENERAL -> null
        }
}

/** A destination the Zen Master can send the user to. The UI turns it into navigation. */
sealed interface ZenAction {
    @get:StringRes
    val labelRes: Int

    /** Breathing screen with this exercise preselected (an id from [BreathingExerciseType]). */
    data class Breathing(val exerciseId: String, override val labelRes: Int) : ZenAction

    data class Focus(override val labelRes: Int) : ZenAction

    /** The Zen garden, which is also where achievements live. */
    data class Garden(override val labelRes: Int) : ZenAction
}
