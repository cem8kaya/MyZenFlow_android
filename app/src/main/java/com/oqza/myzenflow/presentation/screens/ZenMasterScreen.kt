package com.oqza.myzenflow.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oqza.myzenflow.R
import com.oqza.myzenflow.data.repository.ZenMasterRepository
import com.oqza.myzenflow.domain.zenmaster.ZenAction
import com.oqza.myzenflow.domain.zenmaster.ZenMasterMessage
import com.oqza.myzenflow.presentation.components.ZenReadableWidth
import com.oqza.myzenflow.presentation.screens.components.ZenMasterBubble
import com.oqza.myzenflow.presentation.screens.components.ZenMasterDisclaimer
import com.oqza.myzenflow.presentation.screens.components.ZenMasterEmptyState
import com.oqza.myzenflow.presentation.screens.components.ZenMasterInputBar
import com.oqza.myzenflow.presentation.screens.components.ZenMasterTypingIndicator
import com.oqza.myzenflow.presentation.theme.LocalReducedMotion
import com.oqza.myzenflow.presentation.theme.ZenSpacing
import com.oqza.myzenflow.presentation.viewmodels.ZenMasterViewModel

/**
 * The Zen Master chat. A full screen without the bottom bar, so the keyboard simply lifts the
 * input. [onAction] opens the feature a reply points to (breathing, focus, garden).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZenMasterScreen(
    onNavigateBack: () -> Unit,
    onAction: (ZenAction) -> Unit,
    viewModel: ZenMasterViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var input by rememberSaveable { mutableStateOf("") }
    var showClearDialog by rememberSaveable { mutableStateOf(false) }

    fun sendInput() {
        if (state.isTyping || input.isBlank()) return
        viewModel.send(input)
        input = ""
    }

    ZenReadableWidth {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = stringResource(R.string.zm_title),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = stringResource(if (state.isTyping) R.string.zm_status_typing else R.string.zm_status_ready),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showClearDialog = true },
                            enabled = state.messages.isNotEmpty() && !state.isTyping
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = stringResource(R.string.zm_clear_history)
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    // The Scaffold padding already covers the system bars; only the keyboard is added here
                    .consumeWindowInsets(padding)
                    .imePadding()
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when {
                        !state.isLoaded -> Unit
                        state.messages.isEmpty() -> ZenMasterEmptyState(
                            quoteRes = state.quoteRes,
                            onPrompt = { prompt -> if (!state.isTyping) viewModel.send(prompt) }
                        )
                        else -> ZenMasterMessageList(
                            messages = state.messages,
                            isTyping = state.isTyping,
                            onAction = onAction
                        )
                    }
                }
                ZenMasterInputBar(
                    value = input,
                    onValueChange = { input = it.take(ZenMasterRepository.MAX_INPUT_LENGTH) },
                    onSend = ::sendInput,
                    canSend = !state.isTyping
                )
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.zm_clear_title)) },
            text = { Text(stringResource(R.string.zm_clear_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearDialog = false
                }) { Text(stringResource(R.string.zm_clear_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text(stringResource(R.string.zm_cancel)) }
            }
        )
    }
}

@Composable
private fun ZenMasterMessageList(
    messages: List<ZenMasterMessage>,
    isTyping: Boolean,
    onAction: (ZenAction) -> Unit
) {
    val listState = rememberLazyListState()
    val reducedMotion = LocalReducedMotion.current

    // Items: the disclaimer, every message, and the typing dots while a reply is pending
    val lastIndex = messages.size + if (isTyping) 1 else 0

    // Follow the conversation. The first time, jump; afterwards glide (or jump, with reduced motion).
    var hasScrolled by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(messages.size, isTyping) {
        if (reducedMotion || !hasScrolled) listState.scrollToItem(lastIndex) else listState.animateScrollToItem(lastIndex)
        hasScrolled = true
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = ZenSpacing.lg, vertical = ZenSpacing.md),
        verticalArrangement = Arrangement.spacedBy(ZenSpacing.md)
    ) {
        item(key = "disclaimer") { ZenMasterDisclaimer(modifier = Modifier.padding(bottom = 4.dp)) }
        items(messages, key = { it.id }) { message ->
            ZenMasterBubble(message = message, onAction = onAction)
        }
        if (isTyping) {
            item(key = "typing") { ZenMasterTypingIndicator() }
        }
    }
}
