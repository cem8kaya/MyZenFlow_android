package com.oqza.myzenflow.data

import com.oqza.myzenflow.data.dao.ZenMasterMessageDao
import com.oqza.myzenflow.data.entities.ZenMasterMessageEntity
import com.oqza.myzenflow.data.models.AppLanguage
import com.oqza.myzenflow.data.repository.ZenMasterRepository
import com.oqza.myzenflow.domain.zenmaster.ZenIntent
import com.oqza.myzenflow.domain.zenmaster.ZenLanguageSource
import com.oqza.myzenflow.domain.zenmaster.ZenReply
import com.oqza.myzenflow.domain.zenmaster.ZenSentiment
import com.oqza.myzenflow.domain.zenmaster.ZenStrings
import com.oqza.myzenflow.domain.zenmaster.ZenStringsProvider
import com.oqza.myzenflow.domain.zenmaster.ZenUserStatsSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Runs the real repository against an in-memory DAO. The fake mirrors what the SQL in
 * [ZenMasterMessageDao] is meant to do (oldest first; trimTo keeps the newest rows); the SQL
 * itself is checked by Room at build time and by the migration on a real device.
 */
class ZenMasterRepositoryTest {

    private class FakeDao : ZenMasterMessageDao {
        private val rows = MutableStateFlow<List<ZenMasterMessageEntity>>(emptyList())
        private var nextId = 1L
        override fun observeAll(): Flow<List<ZenMasterMessageEntity>> =
            rows.map { it.sortedWith(compareBy({ r -> r.timestamp }, { r -> r.id })) }
        override suspend fun insert(message: ZenMasterMessageEntity): Long {
            val saved = message.copy(id = nextId++)
            rows.value = rows.value + saved
            return saved.id
        }
        override suspend fun trimTo(keep: Int) {
            rows.value = rows.value.sortedWith(compareByDescending<ZenMasterMessageEntity> { it.timestamp }.thenByDescending { it.id }).take(keep)
        }
        override suspend fun deleteAll() { rows.value = emptyList() }
    }

    private var statsCalls = 0
    private var language = AppLanguage.ENGLISH
    private val repository = ZenMasterRepository(
        dao = FakeDao(),
        statsSource = ZenUserStatsSource { statsCalls++; null },
        languageSource = ZenLanguageSource { language },
        stringsProvider = ZenStringsProvider { ZenStrings { id, _ -> "[$id]" } }
    )

    private suspend fun saved() = repository.messages.first()

    @Test
    fun `starts empty`() = runBlocking {
        assertTrue(saved().isEmpty())
    }

    @Test
    fun `user line is trimmed and saved`() = runBlocking {
        assertTrue(repository.addUserMessage("  hello  "))
        val message = saved().single()
        assertEquals("hello", message.text)
        assertTrue(message.isUser)
        assertNull(message.action)
    }

    @Test
    fun `blank text is not saved`() = runBlocking {
        assertFalse(repository.addUserMessage("   "))
        assertTrue(saved().isEmpty())
    }

    @Test
    fun `very long text is cut to the input limit`() = runBlocking {
        repository.addUserMessage("a".repeat(ZenMasterRepository.MAX_INPUT_LENGTH + 100))
        assertEquals(ZenMasterRepository.MAX_INPUT_LENGTH, saved().single().text.length)
    }

    @Test
    fun `only the newest fifty lines are kept, oldest first`() = runBlocking {
        repeat(60) { repository.addUserMessage("msg $it") }
        val messages = saved()
        assertEquals(ZenMasterRepository.MAX_HISTORY, messages.size)
        assertEquals("msg 10", messages.first().text)
        assertEquals("msg 59", messages.last().text)
    }

    @Test
    fun `reply is stored with its intent and gets that intent's button`() = runBlocking {
        repository.saveReply(ZenReply("breathe", ZenIntent.SLEEP, ZenSentiment.NEGATIVE))
        val message = saved().single()
        assertFalse(message.isUser)
        assertEquals(ZenIntent.SLEEP, message.intent)
        assertEquals(ZenSentiment.NEGATIVE, message.sentiment)
        assertNotNull(message.action)
        assertEquals(ZenIntent.SLEEP.action, message.action)
    }

    @Test
    fun `crisis reply is flagged and has no button`() = runBlocking {
        repository.saveReply(ZenReply("please call", ZenIntent.GENERAL, ZenSentiment.NEGATIVE, isCrisis = true))
        val message = saved().single()
        assertTrue(message.isCrisis)
        assertNull(message.action)
    }

    @Test
    fun `prepared reply is not saved until asked`() = runBlocking {
        val reply = repository.prepareReply("Çok stresli hissediyorum")
        assertEquals(ZenIntent.STRESS, reply.intent)
        assertTrue(saved().isEmpty())
    }

    @Test
    fun `stats are read once per ordinary reply and never for a crisis`() = runBlocking {
        repository.prepareReply("I need motivation today")
        assertEquals(1, statsCalls)
        repository.prepareReply("I want to die")
        assertEquals(1, statsCalls)
    }

    @Test
    fun `clear empties the conversation`() = runBlocking {
        repository.addUserMessage("hi")
        repository.saveReply(ZenReply("hello", ZenIntent.GENERAL, ZenSentiment.NEUTRAL))
        repository.clear()
        assertTrue(saved().isEmpty())
    }
}
