package com.oqza.myzenflow.presentation.screens.components

import androidx.annotation.StringRes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oqza.myzenflow.R
import com.oqza.myzenflow.domain.zenmaster.ZenAction
import com.oqza.myzenflow.domain.zenmaster.ZenMasterMessage
import com.oqza.myzenflow.domain.zenmaster.ZenStarterPrompts
import com.oqza.myzenflow.presentation.theme.LocalReducedMotion
import com.oqza.myzenflow.presentation.theme.ZenMotion
import com.oqza.myzenflow.presentation.theme.ZenRadius
import com.oqza.myzenflow.presentation.theme.ZenSpacing
import java.text.DateFormat
import java.util.Date

private val BubbleTail = 4.dp

/** One chat line, with its time and, under a coach reply, the button that leads to a feature. */
@Composable
fun ZenMasterBubble(
    message: ZenMasterMessage,
    onAction: (ZenAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.isUser
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(
        topStart = ZenRadius.medium,
        topEnd = ZenRadius.medium,
        bottomStart = if (isUser) ZenRadius.medium else BubbleTail,
        bottomEnd = if (isUser) BubbleTail else ZenRadius.medium
    )
    val sender = stringResource(if (isUser) R.string.zm_sender_you else R.string.zm_title)
    val time = remember(message.timestampMillis) {
        DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(message.timestampMillis))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isUser) colors.primaryContainer else colors.surfaceVariant,
            contentColor = if (isUser) colors.onPrimaryContainer else colors.onSurfaceVariant,
            shape = shape,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(horizontal = ZenSpacing.lg, vertical = ZenSpacing.md)
                    .semantics { contentDescription = "$sender. ${message.text}" }
            )
        }
        Text(
            text = time,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = ZenSpacing.xs, start = ZenSpacing.xs, end = ZenSpacing.xs)
        )
        message.action?.let { action ->
            FilledTonalButton(
                onClick = { onAction(action) },
                modifier = Modifier.padding(top = ZenSpacing.sm)
            ) {
                Text(stringResource(action.labelRes))
                Spacer(Modifier.width(ZenSpacing.sm))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/** Three softly pulsing dots. Static when the user turned animations off. */
@Composable
fun ZenMasterTypingIndicator(modifier: Modifier = Modifier) {
    val reducedMotion = LocalReducedMotion.current
    val label = stringResource(R.string.zm_status_typing)
    val dotColor = MaterialTheme.colorScheme.onSurfaceVariant
    val transition = rememberInfiniteTransition(label = "typing")

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(
            topStart = ZenRadius.medium,
            topEnd = ZenRadius.medium,
            bottomStart = BubbleTail,
            bottomEnd = ZenRadius.medium
        ),
        modifier = modifier.semantics {
            contentDescription = label
            liveRegion = LiveRegionMode.Polite
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = ZenSpacing.lg, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(3) { index ->
                val pulse = transition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 600, easing = ZenMotion.Calm),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(index * 150)
                    ),
                    label = "dot$index"
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .alpha(if (reducedMotion) 0.6f else pulse.value)
                        .background(dotColor, CircleShape)
                )
            }
        }
    }
}

/** The five starter questions. Tapping one sends its words (without the emoji). */
@Composable
fun ZenMasterStarterPrompts(
    onPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(ZenSpacing.sm)) {
        Text(
            text = stringResource(R.string.zm_suggested_header),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = ZenSpacing.xs)
        )
        ZenStarterPrompts.all.forEach { prompt ->
            val text = stringResource(prompt.textRes)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(ZenRadius.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .clickable(role = Role.Button, onClick = { onPrompt(text) })
                        .padding(horizontal = ZenSpacing.lg, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = prompt.emoji, fontSize = 20.sp)
                    Spacer(Modifier.width(ZenSpacing.md))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** First thing seen before any conversation: who this is, a quote, the starter questions. */
@Composable
fun ZenMasterEmptyState(
    @StringRes quoteRes: Int,
    onPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ZenSpacing.screen, vertical = ZenSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ZenSpacing.md)
    ) {
        Text(text = "🧘", fontSize = 64.sp)
        Text(
            text = stringResource(R.string.zm_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.zm_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(quoteRes),
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.size(ZenSpacing.sm))
        ZenMasterStarterPrompts(onPrompt = onPrompt)
        ZenMasterDisclaimer(modifier = Modifier.padding(top = ZenSpacing.md))
    }
}

/** Says plainly what the Zen Master is not. */
@Composable
fun ZenMasterDisclaimer(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.zm_disclaimer),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Text field and send button. The field stays usable while a reply is pending so the user can
 * already write the next line; only sending waits.
 */
@Composable
fun ZenMasterInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    canSend: Boolean,
    modifier: Modifier = Modifier
) {
    val sendEnabled = canSend && value.isNotBlank()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ZenSpacing.lg, vertical = ZenSpacing.sm),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(ZenSpacing.sm)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(R.string.zm_input_placeholder)) },
            maxLines = 4,
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(onSend = { if (sendEnabled) onSend() })
        )
        FilledIconButton(onClick = onSend, enabled = sendEnabled) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(R.string.zm_send)
            )
        }
    }
}
