package com.oqza.myzenflow.data

import com.oqza.myzenflow.data.entities.ZenMasterMessageEntity
import com.oqza.myzenflow.domain.zenmaster.ZenIntent
import com.oqza.myzenflow.domain.zenmaster.ZenSentiment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ZenMasterMessageEntityTest {

    @Test
    fun `coach message round trips`() {
        val message = ZenMasterMessageEntity
            .coach("hi", ZenIntent.FOCUS, ZenSentiment.POSITIVE, isCrisis = false, timestamp = 42L)
            .copy(id = 7)
            .toDomain()
        assertEquals(7L, message.id)
        assertEquals("hi", message.text)
        assertFalse(message.isUser)
        assertEquals(42L, message.timestampMillis)
        assertEquals(ZenIntent.FOCUS, message.intent)
        assertEquals(ZenSentiment.POSITIVE, message.sentiment)
    }

    @Test
    fun `user message carries no intent`() {
        val message = ZenMasterMessageEntity.user("hello", 1L).toDomain()
        assertTrue(message.isUser)
        assertNull(message.intent)
        assertNull(message.sentiment)
        assertNull(message.action)
    }

    @Test
    fun `an unknown enum name reads back as null instead of crashing`() {
        val row = ZenMasterMessageEntity(text = "x", isUser = false, timestamp = 1L, intent = "REMOVED_LATER", sentiment = "???")
        val message = row.toDomain()
        assertNull(message.intent)
        assertNull(message.sentiment)
        assertNull(message.action)
    }
}
