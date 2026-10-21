package com.oqza.myzenflow.domain.zenmaster

import com.oqza.myzenflow.R

/**
 * Reply templates by intent and sentiment, three variants each, plus the Zen quotes.
 *
 * Listed explicitly instead of looked up by name at runtime: a release build shrinks resources,
 * and strings reached only through getIdentifier() would be stripped. A typo here is a compile error.
 */
internal object ZenResponseCatalog {

    private val templates: Map<ZenIntent, Map<ZenSentiment, List<Int>>> = mapOf(
        ZenIntent.STRESS to mapOf(
            ZenSentiment.POSITIVE to listOf(
                R.string.zm_response_stress_positive_0,
                R.string.zm_response_stress_positive_1,
                R.string.zm_response_stress_positive_2
            ),
            ZenSentiment.NEUTRAL to listOf(
                R.string.zm_response_stress_neutral_0,
                R.string.zm_response_stress_neutral_1,
                R.string.zm_response_stress_neutral_2
            ),
            ZenSentiment.NEGATIVE to listOf(
                R.string.zm_response_stress_negative_0,
                R.string.zm_response_stress_negative_1,
                R.string.zm_response_stress_negative_2
            ),
        ),
        ZenIntent.FOCUS to mapOf(
            ZenSentiment.POSITIVE to listOf(
                R.string.zm_response_focus_positive_0,
                R.string.zm_response_focus_positive_1,
                R.string.zm_response_focus_positive_2
            ),
            ZenSentiment.NEUTRAL to listOf(
                R.string.zm_response_focus_neutral_0,
                R.string.zm_response_focus_neutral_1,
                R.string.zm_response_focus_neutral_2
            ),
            ZenSentiment.NEGATIVE to listOf(
                R.string.zm_response_focus_negative_0,
                R.string.zm_response_focus_negative_1,
                R.string.zm_response_focus_negative_2
            ),
        ),
        ZenIntent.SLEEP to mapOf(
            ZenSentiment.POSITIVE to listOf(
                R.string.zm_response_sleep_positive_0,
                R.string.zm_response_sleep_positive_1,
                R.string.zm_response_sleep_positive_2
            ),
            ZenSentiment.NEUTRAL to listOf(
                R.string.zm_response_sleep_neutral_0,
                R.string.zm_response_sleep_neutral_1,
                R.string.zm_response_sleep_neutral_2
            ),
            ZenSentiment.NEGATIVE to listOf(
                R.string.zm_response_sleep_negative_0,
                R.string.zm_response_sleep_negative_1,
                R.string.zm_response_sleep_negative_2
            ),
        ),
        ZenIntent.BREATHING to mapOf(
            ZenSentiment.POSITIVE to listOf(
                R.string.zm_response_breathing_positive_0,
                R.string.zm_response_breathing_positive_1,
                R.string.zm_response_breathing_positive_2
            ),
            ZenSentiment.NEUTRAL to listOf(
                R.string.zm_response_breathing_neutral_0,
                R.string.zm_response_breathing_neutral_1,
                R.string.zm_response_breathing_neutral_2
            ),
            ZenSentiment.NEGATIVE to listOf(
                R.string.zm_response_breathing_negative_0,
                R.string.zm_response_breathing_negative_1,
                R.string.zm_response_breathing_negative_2
            ),
        ),
        ZenIntent.MOTIVATION to mapOf(
            ZenSentiment.POSITIVE to listOf(
                R.string.zm_response_motivation_positive_0,
                R.string.zm_response_motivation_positive_1,
                R.string.zm_response_motivation_positive_2
            ),
            ZenSentiment.NEUTRAL to listOf(
                R.string.zm_response_motivation_neutral_0,
                R.string.zm_response_motivation_neutral_1,
                R.string.zm_response_motivation_neutral_2
            ),
            ZenSentiment.NEGATIVE to listOf(
                R.string.zm_response_motivation_negative_0,
                R.string.zm_response_motivation_negative_1,
                R.string.zm_response_motivation_negative_2
            ),
        ),
        ZenIntent.MEDITATION to mapOf(
            ZenSentiment.POSITIVE to listOf(
                R.string.zm_response_meditation_positive_0,
                R.string.zm_response_meditation_positive_1,
                R.string.zm_response_meditation_positive_2
            ),
            ZenSentiment.NEUTRAL to listOf(
                R.string.zm_response_meditation_neutral_0,
                R.string.zm_response_meditation_neutral_1,
                R.string.zm_response_meditation_neutral_2
            ),
            ZenSentiment.NEGATIVE to listOf(
                R.string.zm_response_meditation_negative_0,
                R.string.zm_response_meditation_negative_1,
                R.string.zm_response_meditation_negative_2
            ),
        ),
        ZenIntent.PROGRESS to mapOf(
            ZenSentiment.POSITIVE to listOf(
                R.string.zm_response_progress_positive_0,
                R.string.zm_response_progress_positive_1,
                R.string.zm_response_progress_positive_2
            ),
            ZenSentiment.NEUTRAL to listOf(
                R.string.zm_response_progress_neutral_0,
                R.string.zm_response_progress_neutral_1,
                R.string.zm_response_progress_neutral_2
            ),
            ZenSentiment.NEGATIVE to listOf(
                R.string.zm_response_progress_negative_0,
                R.string.zm_response_progress_negative_1,
                R.string.zm_response_progress_negative_2
            ),
        ),
        ZenIntent.GENERAL to mapOf(
            ZenSentiment.POSITIVE to listOf(
                R.string.zm_response_general_positive_0,
                R.string.zm_response_general_positive_1,
                R.string.zm_response_general_positive_2
            ),
            ZenSentiment.NEUTRAL to listOf(
                R.string.zm_response_general_neutral_0,
                R.string.zm_response_general_neutral_1,
                R.string.zm_response_general_neutral_2
            ),
            ZenSentiment.NEGATIVE to listOf(
                R.string.zm_response_general_negative_0,
                R.string.zm_response_general_negative_1,
                R.string.zm_response_general_negative_2
            ),
        ),
    )

    fun templatesFor(intent: ZenIntent, sentiment: ZenSentiment): List<Int> =
        templates[intent]?.get(sentiment).orEmpty()

    val quotes: List<Int> = listOf(
        R.string.zm_quote_0,
        R.string.zm_quote_1,
        R.string.zm_quote_2,
        R.string.zm_quote_3,
        R.string.zm_quote_4,
        R.string.zm_quote_5,
        R.string.zm_quote_6,
        R.string.zm_quote_7,
        R.string.zm_quote_8,
        R.string.zm_quote_9,
        R.string.zm_quote_10,
        R.string.zm_quote_11,
        R.string.zm_quote_12,
        R.string.zm_quote_13,
        R.string.zm_quote_14,
        R.string.zm_quote_15,
        R.string.zm_quote_16,
        R.string.zm_quote_17,
        R.string.zm_quote_18,
        R.string.zm_quote_19
    )
}
