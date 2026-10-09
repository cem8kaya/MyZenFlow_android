package com.oqza.myzenflow.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oqza.myzenflow.data.models.MoodContext
import com.oqza.myzenflow.data.models.MoodEntry
import com.oqza.myzenflow.data.models.MoodLevel
import com.oqza.myzenflow.data.repository.MoodRepository
import com.oqza.myzenflow.data.repository.MoodRepository.Companion.localDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** Drives the once-a-day mood check-in card on Home. */
@HiltViewModel
class CheckInViewModel @Inject constructor(
    private val moodRepository: MoodRepository
) : ViewModel() {

    /** Today's daily check-in, or null if the user has not answered yet. */
    val todayCheckIn: StateFlow<MoodEntry?> = moodRepository.entries
        .map { list ->
            val today = LocalDate.now()
            list.lastOrNull { it.context == MoodContext.DAILY && it.localDate() == today }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun submit(level: MoodLevel) {
        viewModelScope.launch { moodRepository.add(level, MoodContext.DAILY) }
    }

    /** Records how the user felt right after a session. */
    fun submitAfterSession(level: MoodLevel) {
        viewModelScope.launch { moodRepository.add(level, MoodContext.AFTER_SESSION) }
    }
}
