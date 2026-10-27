package com.oqza.myzenflow.domain.zenmaster

import com.oqza.myzenflow.data.models.AppLanguage
import com.oqza.myzenflow.data.models.UserStats
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ZenReplyComposerTest {

    private val languages = mutableListOf<AppLanguage>()
    private val provider = ZenStringsProvider { language ->
        languages += language
        ZenStrings { id, _ -> "[$id]" }
    }

    /** Never rolls the personal note, so replies are predictable. */
    private val quiet = object : Random() {
        override fun nextBits(bitCount: Int) = 0
        override fun nextInt(from: Int, until: Int) = from
    }

    private val composer = ZenReplyComposer(provider, quiet)

    @Test
    fun `reply follows the message and uses the chosen language`() = runBlocking {
        val reply = composer.compose("Çok stresli hissediyorum", AppLanguage.TURKISH) { null }
        assertEquals(ZenIntent.STRESS, reply.intent)
        assertEquals(ZenSentiment.NEGATIVE, reply.sentiment)
        assertEquals(listOf(AppLanguage.TURKISH), languages)
    }

    @Test
    fun `stats are loaded for a normal message`() = runBlocking {
        var loads = 0
        composer.compose("I need motivation today", AppLanguage.ENGLISH) { loads++; UserStats() }
        assertEquals(1, loads)
    }

    @Test
    fun `crisis messages never wait on stats`() = runBlocking {
        var loads = 0
        val reply = composer.compose("I want to die", AppLanguage.ENGLISH) { loads++; UserStats() }
        assertTrue(reply.isCrisis)
        assertEquals(0, loads)
        assertFalse(reply.action != null)
    }
}
