package com.oqza.myzenflow.domain.zenmaster

import kotlinx.coroutines.flow.Flow

/** What the conversation flow needs from storage. The repository implements it. */
interface ZenMasterChat {
    /** The whole saved conversation, oldest first. */
    val messages: Flow<List<ZenMasterMessage>>

    /** Saves the user's line. False when the text is blank and nothing was saved. */
    suspend fun addUserMessage(text: String): Boolean

    /** Works out the reply without saving it. */
    suspend fun prepareReply(userText: String): ZenReply

    suspend fun saveReply(reply: ZenReply)

    suspend fun clear()
}
