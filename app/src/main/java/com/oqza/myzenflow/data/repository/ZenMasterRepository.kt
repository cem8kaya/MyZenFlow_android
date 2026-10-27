package com.oqza.myzenflow.data.repository

import com.oqza.myzenflow.data.dao.ZenMasterMessageDao
import com.oqza.myzenflow.data.entities.ZenMasterMessageEntity
import com.oqza.myzenflow.domain.zenmaster.ZenMasterChat
import com.oqza.myzenflow.domain.zenmaster.ZenMasterMessage
import com.oqza.myzenflow.domain.zenmaster.ZenLanguageSource
import com.oqza.myzenflow.domain.zenmaster.ZenReply
import com.oqza.myzenflow.domain.zenmaster.ZenReplyComposer
import com.oqza.myzenflow.domain.zenmaster.ZenStringsProvider
import com.oqza.myzenflow.domain.zenmaster.ZenUserStatsSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores the Zen Master conversation and produces replies. The conversation survives app
 * restarts; only the newest [MAX_HISTORY] lines are kept.
 */
@Singleton
class ZenMasterRepository @Inject constructor(
    private val dao: ZenMasterMessageDao,
    private val statsSource: ZenUserStatsSource,
    private val languageSource: ZenLanguageSource,
    stringsProvider: ZenStringsProvider
) : ZenMasterChat {

    private val composer = ZenReplyComposer(stringsProvider)

    override val messages: Flow<List<ZenMasterMessage>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun addUserMessage(text: String): Boolean {
        val trimmed = text.trim().take(MAX_INPUT_LENGTH)
        if (trimmed.isEmpty()) return false
        dao.insert(ZenMasterMessageEntity.user(trimmed, System.currentTimeMillis()))
        dao.trimTo(MAX_HISTORY)
        return true
    }

    override suspend fun prepareReply(userText: String): ZenReply {
        return composer.compose(userText.trim().take(MAX_INPUT_LENGTH), languageSource.current()) {
            statsSource.current()
        }
    }

    override suspend fun saveReply(reply: ZenReply) {
        dao.insert(ZenMasterMessageEntity.coach(reply.text, reply.intent, reply.sentiment, reply.isCrisis, System.currentTimeMillis()))
        dao.trimTo(MAX_HISTORY)
    }

    override suspend fun clear() = dao.deleteAll()

    companion object {
        const val MAX_HISTORY = 50
        const val MAX_INPUT_LENGTH = 500
    }
}
