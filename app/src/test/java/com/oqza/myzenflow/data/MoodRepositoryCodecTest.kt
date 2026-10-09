package com.oqza.myzenflow.data

import com.oqza.myzenflow.data.models.MoodContext
import com.oqza.myzenflow.data.models.MoodEntry
import com.oqza.myzenflow.data.models.MoodLevel
import com.oqza.myzenflow.data.repository.MoodRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class MoodRepositoryCodecTest {
    @Test
    fun `round trip keeps entries`() {
        val list = listOf(
            MoodEntry(1_000L, MoodLevel.GOOD, MoodContext.DAILY),
            MoodEntry(2_000L, MoodLevel.BAD, MoodContext.AFTER_SESSION)
        )
        assertEquals(list, MoodRepository.decode(MoodRepository.encode(list)))
    }

    @Test
    fun `empty string decodes to empty list`() {
        assertEquals(emptyList<MoodEntry>(), MoodRepository.decode(""))
    }

    @Test
    fun `corrupt parts are skipped`() {
        val decoded = MoodRepository.decode("1000,4,DAILY;garbage;2000,x,DAILY;3000,5,NOPE;4000,2,AFTER_SESSION")
        assertEquals(
            listOf(
                MoodEntry(1000L, MoodLevel.GOOD, MoodContext.DAILY),
                MoodEntry(4000L, MoodLevel.BAD, MoodContext.AFTER_SESSION)
            ),
            decoded
        )
    }
}
