package com.oqza.myzenflow.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.oqza.myzenflow.data.models.MoodContext
import com.oqza.myzenflow.data.models.MoodEntry
import com.oqza.myzenflow.data.models.MoodLevel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

private val Context.moodDataStore: DataStore<Preferences> by preferencesDataStore(name = "mood_log")

/**
 * Local mood journal. Mood check-ins are personal wellbeing data: they stay on the device
 * (no analytics, no network) and are removed by [clearAll] or by uninstalling the app.
 *
 * Stored as one compact string "millis,level,context;..." and capped to the latest entries.
 */
@Singleton
class MoodRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val entries: Flow<List<MoodEntry>> = context.moodDataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { decode(it[LOG_KEY].orEmpty()) }

    suspend fun add(level: MoodLevel, moodContext: MoodContext, nowMillis: Long = System.currentTimeMillis()) {
        context.moodDataStore.edit { prefs ->
            val list = decode(prefs[LOG_KEY].orEmpty()) + MoodEntry(nowMillis, level, moodContext)
            prefs[LOG_KEY] = encode(list.takeLast(MAX_ENTRIES))
        }
    }

    suspend fun clearAll() {
        context.moodDataStore.edit { it.remove(LOG_KEY) }
    }

    companion object {
        private val LOG_KEY = stringPreferencesKey("log")
        private const val MAX_ENTRIES = 400

        internal fun encode(list: List<MoodEntry>): String =
            list.joinToString(";") { "${it.epochMillis},${it.level.value},${it.context.name}" }

        internal fun decode(raw: String): List<MoodEntry> =
            raw.split(";").mapNotNull { part ->
                val f = part.split(",")
                if (f.size != 3) return@mapNotNull null
                val millis = f[0].toLongOrNull() ?: return@mapNotNull null
                val level = f[1].toIntOrNull()?.let { MoodLevel.fromValue(it) } ?: return@mapNotNull null
                val ctx = runCatching { MoodContext.valueOf(f[2]) }.getOrNull() ?: return@mapNotNull null
                MoodEntry(millis, level, ctx)
            }

        fun MoodEntry.localDate(zone: ZoneId = ZoneId.systemDefault()): LocalDate =
            Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
    }
}
