package com.oqza.myzenflow.domain.zenmaster

import com.oqza.myzenflow.R
import com.oqza.myzenflow.data.models.UserStats
import kotlin.random.Random

/** One answer from the Zen Master. The button, if any, comes from [intent]. */
data class ZenReply(
    val text: String,
    val intent: ZenIntent,
    val sentiment: ZenSentiment,
    val isCrisis: Boolean = false
) {
    val action: ZenAction? get() = if (isCrisis) null else intent.action
}

/**
 * Builds the reply for an analyzed message: a random template for the intent and sentiment, with
 * an occasional personal note about the user's streak or minutes.
 */
class ZenResponseGenerator(
    private val strings: ZenStrings,
    private val random: Random = Random.Default
) {

    fun generate(analysis: ZenAnalysis, stats: UserStats? = null): ZenReply {
        if (analysis.isCrisis) {
            // Fixed text on purpose: no quote, no button, no personal note.
            return ZenReply(
                text = strings.get(R.string.zm_crisis_reply),
                intent = ZenIntent.GENERAL,
                sentiment = ZenSentiment.NEGATIVE,
                isCrisis = true
            )
        }

        val templates = ZenResponseCatalog.templatesFor(analysis.intent, analysis.sentiment)
        val base = if (templates.isEmpty()) {
            strings.get(R.string.zm_response_fallback) + "\n\n" + strings.get(ZenResponseCatalog.quotes.random(random))
        } else {
            strings.get(templates.random(random))
        }

        return ZenReply(
            text = base + personalNote(base, stats),
            intent = analysis.intent,
            sentiment = analysis.sentiment
        )
    }

    /**
     * A streak or milestone line, at most one, shown on roughly three replies in ten and never
     * on long replies. (iOS adds the milestone to every reply while the minutes sit in a window
     * of 15 per 100, which gets repetitive.)
     */
    private fun personalNote(reply: String, stats: UserStats?): String {
        if (stats == null || reply.length >= MAX_LENGTH_FOR_NOTE || !roll()) return ""
        return when {
            stats.currentStreak > MIN_STREAK ->
                "\n\n" + strings.get(R.string.zm_personalization_streak, stats.currentStreak)
            stats.totalMinutes >= MILESTONE_MINUTES && stats.totalMinutes % 100 < MILESTONE_WINDOW ->
                "\n\n" + strings.get(R.string.zm_personalization_milestone, stats.totalMinutes)
            else -> ""
        }
    }

    /** True about three times in ten. */
    private fun roll(): Boolean = random.nextInt(1, 11) > 7

    private companion object {
        const val MAX_LENGTH_FOR_NOTE = 250
        const val MIN_STREAK = 2
        const val MILESTONE_MINUTES = 60
        const val MILESTONE_WINDOW = 15
    }
}
