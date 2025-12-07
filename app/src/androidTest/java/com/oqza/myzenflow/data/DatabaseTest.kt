package com.oqza.myzenflow.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oqza.myzenflow.data.dao.MeditationSessionDao
import com.oqza.myzenflow.data.dao.FocusSessionDao
import com.oqza.myzenflow.data.database.AppDatabase
import com.oqza.myzenflow.data.entities.MeditationSessionEntity
import com.oqza.myzenflow.data.entities.FocusSessionEntity
import com.oqza.myzenflow.data.models.SessionType
import com.oqza.myzenflow.data.models.BreathingExercise
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.io.IOException

/**
 * Instrumented test for Room database operations
 */
@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var meditationSessionDao: MeditationSessionDao
    private lateinit var focusSessionDao: FocusSessionDao
    private lateinit var database: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()
        meditationSessionDao = database.meditationSessionDao()
        focusSessionDao = database.focusSessionDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeMeditationSessionAndRead() = runBlocking {
        val session = MeditationSessionEntity(
            id = "test-1",
            date = LocalDateTime.now(),
            duration = 600, // 10 minutes in seconds
            type = SessionType.MEDITATION,
            breathingExercise = BreathingExercise.BOX_BREATHING,
            completed = true
        )

        meditationSessionDao.insert(session)
        val sessions = meditationSessionDao.getAllSessions().first()

        assert(sessions.isNotEmpty())
        assert(sessions[0].id == "test-1")
        assert(sessions[0].duration == 600)
        assert(sessions[0].type == SessionType.MEDITATION)
    }

    @Test
    @Throws(Exception::class)
    fun writeFocusSessionAndRead() = runBlocking {
        val session = FocusSessionEntity(
            id = "focus-test-1",
            date = LocalDateTime.now(),
            duration = 1500, // 25 minutes in seconds
            focusDuration = 1500,
            breakDuration = 300,
            completedCycles = 1,
            targetCycles = 4,
            taskName = "Test Task",
            completed = false
        )

        focusSessionDao.insert(session)
        val sessions = focusSessionDao.getAllSessions().first()

        assert(sessions.isNotEmpty())
        assert(sessions[0].id == "focus-test-1")
        assert(sessions[0].taskName == "Test Task")
        assert(sessions[0].completedCycles == 1)
    }

    @Test
    @Throws(Exception::class)
    fun deleteSessionById() = runBlocking {
        val session = MeditationSessionEntity(
            id = "delete-test",
            date = LocalDateTime.now(),
            duration = 300,
            type = SessionType.BREATHING,
            completed = true
        )

        meditationSessionDao.insert(session)
        meditationSessionDao.deleteById("delete-test")
        val sessions = meditationSessionDao.getAllSessions().first()

        assert(sessions.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun getTotalMinutes() = runBlocking {
        // Insert multiple sessions
        meditationSessionDao.insert(
            MeditationSessionEntity(
                id = "1",
                date = LocalDateTime.now(),
                duration = 600,
                type = SessionType.MEDITATION,
                completed = true
            )
        )
        meditationSessionDao.insert(
            MeditationSessionEntity(
                id = "2",
                date = LocalDateTime.now(),
                duration = 900,
                type = SessionType.MEDITATION,
                completed = true
            )
        )

        val totalMinutes = meditationSessionDao.getTotalMinutes()
        assert(totalMinutes == 1500) // 600 + 900
    }
}
