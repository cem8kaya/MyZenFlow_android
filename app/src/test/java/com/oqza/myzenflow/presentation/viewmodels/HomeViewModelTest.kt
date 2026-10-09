package com.oqza.myzenflow.presentation.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.oqza.myzenflow.data.models.SessionData
import com.oqza.myzenflow.data.models.SessionType
import com.oqza.myzenflow.data.models.UserPreferences
import com.oqza.myzenflow.data.repository.BreathingRepository
import com.oqza.myzenflow.data.repository.PreferencesRepository
import com.oqza.myzenflow.data.repository.SessionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

/**
 * Unit tests for HomeViewModel
 * Tests home screen state management and data loading
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var breathingRepository: BreathingRepository
    private lateinit var sessionRepository: SessionRepository
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        breathingRepository = mockk(relaxed = true)
        sessionRepository = mockk(relaxed = true)
        preferencesRepository = mockk(relaxed = true)

        // Setup default mock responses
        coEvery { preferencesRepository.userPreferences } returns flowOf(UserPreferences())
        coEvery { sessionRepository.getSessionsForDay(any(), any()) } returns emptyList()
        coEvery { sessionRepository.getRecentSessions(any()) } returns flowOf(emptyList())
        coEvery { sessionRepository.getAllPracticeSessions() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads data and updates state`() = runTest {
        viewModel = HomeViewModel(breathingRepository, sessionRepository, preferencesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertNull(state.error)
        coVerify { preferencesRepository.userPreferences }
        coVerify { sessionRepository.getSessionsForDay(any(), any()) }
    }

    @Test
    fun `loadData with completed sessions updates state correctly`() = runTest {
        val completedSession = SessionData(
            id = "1",
            date = LocalDateTime.now(),
            duration = 300,
            type = SessionType.MEDITATION,
            completed = true
        )

        coEvery { sessionRepository.getSessionsForDay(any(), any()) } returns listOf(completedSession)

        viewModel = HomeViewModel(breathingRepository, sessionRepository, preferencesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(1, state.todaySessionCount)
        assertEquals(5, state.todayMinutes) // 300 seconds = 5 minutes
    }

    @Test
    fun `loadData with incomplete sessions excludes them from count`() = runTest {
        val incompleteSession = SessionData(
            id = "1",
            date = LocalDateTime.now(),
            duration = 300,
            type = SessionType.MEDITATION,
            completed = false
        )

        coEvery { sessionRepository.getSessionsForDay(any(), any()) } returns listOf(incompleteSession)

        viewModel = HomeViewModel(breathingRepository, sessionRepository, preferencesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(0, state.todaySessionCount)
        assertEquals(0, state.todayMinutes)
    }

    @Test
    fun `loadData with multiple sessions calculates total correctly`() = runTest {
        val sessions = listOf(
            SessionData(
                id = "1",
                date = LocalDateTime.now(),
                duration = 600, // 10 minutes
                type = SessionType.MEDITATION,
                completed = true
            ),
            SessionData(
                id = "2",
                date = LocalDateTime.now(),
                duration = 900, // 15 minutes
                type = SessionType.FOCUS,
                completed = true
            ),
            SessionData(
                id = "3",
                date = LocalDateTime.now(),
                duration = 300, // 5 minutes (not completed)
                type = SessionType.BREATHING,
                completed = false
            )
        )

        coEvery { sessionRepository.getSessionsForDay(any(), any()) } returns sessions

        viewModel = HomeViewModel(breathingRepository, sessionRepository, preferencesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(2, state.todaySessionCount)
        assertEquals(25, state.todayMinutes) // Only completed sessions: 10 + 15
    }

    @Test
    fun `loadData on error updates error state`() = runTest {
        coEvery { sessionRepository.getSessionsForDay(any(), any()) } throws Exception("Database error")

        viewModel = HomeViewModel(breathingRepository, sessionRepository, preferencesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("hata"))
    }

    @Test
    fun `refreshData reloads data`() = runTest {
        viewModel = HomeViewModel(breathingRepository, sessionRepository, preferencesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Reset verification
        coEvery { sessionRepository.getSessionsForDay(any(), any()) } returns emptyList()

        viewModel.refreshData()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify data was loaded again
        coVerify(atLeast = 2) { sessionRepository.getSessionsForDay(any(), any()) }
    }

    @Test
    fun `clearError removes error from state`() = runTest {
        coEvery { sessionRepository.getSessionsForDay(any(), any()) } throws Exception("Test error")

        viewModel = HomeViewModel(breathingRepository, sessionRepository, preferencesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify error exists
        assertNotNull(viewModel.uiState.value.error)

        viewModel.clearError()

        // Verify error is cleared
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `motivationalQuote is not empty`() = runTest {
        viewModel = HomeViewModel(breathingRepository, sessionRepository, preferencesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state.motivationalQuote.isNotEmpty())
    }

    @Test
    fun `recentSessions are loaded correctly`() = runTest {
        val recentSessions = listOf(
            SessionData(
                id = "1",
                date = LocalDateTime.now(),
                duration = 600,
                type = SessionType.MEDITATION,
                completed = true
            )
        )

        coEvery { sessionRepository.getRecentSessions(3) } returns flowOf(recentSessions)

        viewModel = HomeViewModel(breathingRepository, sessionRepository, preferencesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(1, state.recentSessions.size)
        assertEquals("1", state.recentSessions[0].id)
    }

    @Test
    fun `userPreferences are loaded correctly`() = runTest {
        val preferences = UserPreferences(
            language = "tr",
            theme = "dark"
        )

        coEvery { preferencesRepository.userPreferences } returns flowOf(preferences)

        viewModel = HomeViewModel(breathingRepository, sessionRepository, preferencesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals("tr", state.userPreferences.language)
        assertEquals("dark", state.userPreferences.theme)
    }
}
