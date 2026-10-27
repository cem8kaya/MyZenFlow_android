package com.oqza.myzenflow.domain.zenmaster

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * One turn of the chat: save the user's line, wait a moment so the reply does not feel
 * instant, save the reply.
 *
 * The wait is the typing indicator's time. It is skipped for the crisis reply, because someone
 * in distress should not be kept waiting. If the turn is cancelled during the wait (the screen
 * closed), the reply is still saved so the conversation never ends on an unanswered line.
 */
class ZenMasterConversation(
    private val chat: ZenMasterChat,
    private val typingDelayMillis: Long = TYPING_DELAY_MS,
    private val wait: suspend (Long) -> Unit = { delay(it) }
) {

    /** @return false when [text] was blank and nothing happened. */
    suspend fun send(text: String): Boolean {
        if (!chat.addUserMessage(text)) return false
        val reply = withContext(NonCancellable) { chat.prepareReply(text) }
        try {
            if (!reply.isCrisis) wait(typingDelayMillis)
        } finally {
            withContext(NonCancellable) { chat.saveReply(reply) }
        }
        return true
    }

    companion object {
        const val TYPING_DELAY_MS = 600L
    }
}
