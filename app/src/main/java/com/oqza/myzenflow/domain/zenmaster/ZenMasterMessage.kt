package com.oqza.myzenflow.domain.zenmaster

/** One line of the conversation, as the screen sees it. */
data class ZenMasterMessage(
    val id: Long,
    val text: String,
    val isUser: Boolean,
    val timestampMillis: Long,
    val intent: ZenIntent? = null,
    val sentiment: ZenSentiment? = null,
    val isCrisis: Boolean = false
) {
    /** The button under a coach reply. Never on the user's own lines or on the crisis reply. */
    val action: ZenAction? get() = if (isUser || isCrisis) null else intent?.action
}
