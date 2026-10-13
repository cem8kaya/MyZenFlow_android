package com.oqza.myzenflow.domain.review

import com.oqza.myzenflow.data.models.ReviewState

/**
 * Decides on which app launches the "would you like to rate ZenFlow?" prompt appears.
 * [launchCount] is the number of app launches including the current one.
 *
 * - Already rated: never.
 * - Not rated yet: every 3rd launch (3, 6, 9, ...).
 * - Said "no thanks": launches 6 and 10, then every 10th (20, 30, ...).
 */
object ReviewPromptPolicy {
    private const val NOT_REVIEWED_INTERVAL = 3
    private const val DECLINED_INTERVAL = 10
    private val DECLINED_EARLY_LAUNCHES = setOf(6, 10)

    fun shouldPrompt(launchCount: Int, state: ReviewState): Boolean {
        if (launchCount <= 0) return false
        return when (state) {
            ReviewState.REVIEWED -> false
            ReviewState.NOT_REVIEWED -> launchCount % NOT_REVIEWED_INTERVAL == 0
            ReviewState.DECLINED ->
                launchCount in DECLINED_EARLY_LAUNCHES ||
                    (launchCount > DECLINED_INTERVAL && launchCount % DECLINED_INTERVAL == 0)
        }
    }
}
