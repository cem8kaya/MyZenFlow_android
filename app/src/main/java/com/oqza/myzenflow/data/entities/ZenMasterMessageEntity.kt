package com.oqza.myzenflow.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.oqza.myzenflow.domain.zenmaster.ZenIntent
import com.oqza.myzenflow.domain.zenmaster.ZenMasterMessage
import com.oqza.myzenflow.domain.zenmaster.ZenSentiment

/**
 * One line of the Zen Master chat. Intent and sentiment are stored by enum name, so reordering
 * or adding enum values later cannot corrupt old rows; an unknown name just reads back as null.
 */
@Entity(tableName = "zen_master_messages")
data class ZenMasterMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long,
    val intent: String? = null,
    val sentiment: String? = null,
    val isCrisis: Boolean = false
) {
    fun toDomain() = ZenMasterMessage(
        id = id,
        text = text,
        isUser = isUser,
        timestampMillis = timestamp,
        intent = ZenIntent.entries.firstOrNull { it.name == intent },
        sentiment = ZenSentiment.entries.firstOrNull { it.name == sentiment },
        isCrisis = isCrisis
    )

    companion object {
        fun user(text: String, timestamp: Long) =
            ZenMasterMessageEntity(text = text, isUser = true, timestamp = timestamp)

        fun coach(text: String, intent: ZenIntent, sentiment: ZenSentiment, isCrisis: Boolean, timestamp: Long) =
            ZenMasterMessageEntity(
                text = text,
                isUser = false,
                timestamp = timestamp,
                intent = intent.name,
                sentiment = sentiment.name,
                isCrisis = isCrisis
            )
    }
}
