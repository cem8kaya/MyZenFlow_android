package com.oqza.myzenflow.domain.zenmaster

import com.oqza.myzenflow.data.models.AppLanguage
import com.oqza.myzenflow.data.models.UserStats
import kotlin.random.Random

/** Reads a user message and writes the reply, in the user's language. No storage, no Android. */
class ZenReplyComposer(
    private val stringsProvider: ZenStringsProvider,
    private val random: Random = Random.Default
) {

    /**
     * [loadStats] is only called when the reply can use it; a crisis reply never does,
     * and should not wait on a database read.
     */
    suspend fun compose(
        userText: String,
        language: AppLanguage,
        loadStats: suspend () -> UserStats?
    ): ZenReply {
        val analysis = ZenMessageAnalyzer.analyze(userText)
        val stats = if (analysis.isCrisis) null else loadStats()
        return ZenResponseGenerator(stringsProvider.strings(language), random).generate(analysis, stats)
    }
}
