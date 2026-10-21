package com.oqza.myzenflow.domain.zenmaster

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ZenMasterConversationTest {

    private val normalReply = ZenReply("hello", ZenIntent.GENERAL, ZenSentiment.NEUTRAL)
    private val crisisReply = ZenReply("please call", ZenIntent.GENERAL, ZenSentiment.NEGATIVE, isCrisis = true)

    /** Records every call in order; [wait] writes into the same list. */
    private class FakeChat(private val reply: ZenReply, private val accepts: Boolean = true) : ZenMasterChat {
        val events = mutableListOf<String>()
        override val messages: Flow<List<ZenMasterMessage>> = emptyFlow()
        override suspend fun addUserMessage(text: String): Boolean { events += "add:$text"; return accepts }
        override suspend fun prepareReply(userText: String): ZenReply { events += "prepare:$userText"; return reply }
        override suspend fun saveReply(reply: ZenReply) { events += "save:${reply.text}" }
        override suspend fun clear() { events += "clear" }
    }

    private fun conversation(chat: FakeChat, wait: suspend (Long) -> Unit = { chat.events += "wait:$it" }) =
        ZenMasterConversation(chat, typingDelayMillis = 600, wait = wait)

    @Test
    fun `saves the user line, waits, then saves the reply`() = runBlocking {
        val chat = FakeChat(normalReply)
        assertTrue(conversation(chat).send("hi"))
        assertEquals(listOf("add:hi", "prepare:hi", "wait:600", "save:hello"), chat.events)
    }

    @Test
    fun `crisis reply is saved without waiting`() = runBlocking {
        val chat = FakeChat(crisisReply)
        conversation(chat).send("help")
        assertEquals(listOf("add:help", "prepare:help", "save:please call"), chat.events)
    }

    @Test
    fun `blank text does nothing after the first step`() = runBlocking {
        val chat = FakeChat(normalReply, accepts = false)
        assertFalse(conversation(chat).send("  "))
        assertEquals(listOf("add:  "), chat.events)
    }

    @Test
    fun `reply is still saved when the turn is cancelled during the wait`() = runBlocking {
        val chat = FakeChat(normalReply)
        val conversation = conversation(chat, wait = { awaitCancellation() })
        val job = launch(start = CoroutineStart.UNDISPATCHED) { conversation.send("hi") }
        // The turn is now parked in the wait; closing the screen cancels it
        job.cancelAndJoin()
        assertEquals(listOf("add:hi", "prepare:hi", "save:hello"), chat.events)
    }
}
