package com.oqza.myzenflow.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.oqza.myzenflow.data.entities.ZenMasterMessageEntity
import kotlinx.coroutines.flow.Flow

/** DAO for the Zen Master chat history. */
@Dao
interface ZenMasterMessageDao {

    @Query("SELECT * FROM zen_master_messages ORDER BY timestamp ASC, id ASC")
    fun observeAll(): Flow<List<ZenMasterMessageEntity>>

    @Insert
    suspend fun insert(message: ZenMasterMessageEntity): Long

    /** Keeps the newest [keep] messages and deletes the rest. */
    @Query(
        "DELETE FROM zen_master_messages WHERE id NOT IN " +
            "(SELECT id FROM zen_master_messages ORDER BY timestamp DESC, id DESC LIMIT :keep)"
    )
    suspend fun trimTo(keep: Int)

    @Query("DELETE FROM zen_master_messages")
    suspend fun deleteAll()
}
