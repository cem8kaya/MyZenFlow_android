package com.oqza.myzenflow.domain

import com.oqza.myzenflow.data.models.ReviewState
import com.oqza.myzenflow.domain.review.ReviewPromptPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ReviewPromptPolicyTest {

    private fun launches(state: ReviewState, upTo: Int = 40) =
        (1..upTo).filter { ReviewPromptPolicy.shouldPrompt(it, state) }

    @Test
    fun `not reviewed users are asked every third launch`() {
        assertEquals(listOf(3, 6, 9, 12, 15), launches(ReviewState.NOT_REVIEWED, upTo = 15))
    }

    @Test
    fun `reviewed users are never asked`() {
        assertEquals(emptyList<Int>(), launches(ReviewState.REVIEWED))
    }

    @Test
    fun `users who declined are asked on launch 6 and 10, then every tenth`() {
        assertEquals(listOf(6, 10, 20, 30, 40), launches(ReviewState.DECLINED))
    }

    @Test
    fun `launch zero is never a prompt`() {
        ReviewState.entries.forEach { assertFalse(ReviewPromptPolicy.shouldPrompt(0, it)) }
    }
}
