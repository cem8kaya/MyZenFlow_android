package com.oqza.myzenflow.presentation.viewmodels

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oqza.myzenflow.data.repository.ZenMasterRepository
import com.oqza.myzenflow.domain.zenmaster.ZenMasterConversation
import com.oqza.myzenflow.domain.zenmaster.ZenMasterMessage
import com.oqza.myzenflow.domain.zenmaster.ZenResponseCatalog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ZenMasterUiState(
    val messages: List<ZenMasterMessage> = emptyList(),
    /** The coach is "typing"; the input and the clear button are off meanwhile. */
    val isTyping: Boolean = false,
    /** False until the saved conversation has been read, so the empty state does not flash. */
    val isLoaded: Boolean = false,
    /** A Zen quote for the empty state, picked once per screen visit. */
    @StringRes val quoteRes: Int
)

@HiltViewModel
class ZenMasterViewModel @Inject constructor(
    private val repository: ZenMasterRepository
) : ViewModel() {

    private val conversation = ZenMasterConversation(repository)
    private val typing = MutableStateFlow(false)
    private val quoteRes = ZenResponseCatalog.quotes.random()

    val uiState: StateFlow<ZenMasterUiState> = combine(repository.messages, typing) { messages, isTyping ->
        ZenMasterUiState(messages = messages, isTyping = isTyping, isLoaded = true, quoteRes = quoteRes)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ZenMasterUiState(quoteRes = quoteRes)
    )

    /** Sends [text] as the user's line. Ignored while a reply is pending or the text is blank. */
    fun send(text: String) {
        if (text.isBlank() || !typing.compareAndSet(expect = false, update = true)) return
        viewModelScope.launch {
            try {
                conversation.send(text)
            } finally {
                typing.value = false
            }
        }
    }

    fun clearHistory() {
        if (typing.value) return
        viewModelScope.launch { repository.clear() }
    }
}
