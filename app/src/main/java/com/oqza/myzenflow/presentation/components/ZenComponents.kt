package com.oqza.myzenflow.presentation.components

import com.oqza.myzenflow.presentation.theme.LocalReducedMotion
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.oqza.myzenflow.data.models.TimeOfDay
import com.oqza.myzenflow.presentation.theme.ZenMotion
import com.oqza.myzenflow.presentation.theme.ZenSpacing
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.composed

/** Scales content slightly while pressed. */
@Composable
private fun rememberPressScale(source: MutableInteractionSource, pressed: Float = 0.97f): Float {
    val isPressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressed else 1f,
        animationSpec = tween(ZenMotion.SHORT, easing = ZenMotion.Standard),
        label = "pressScale"
    )
    return scale
}

/**
 * Base surface for grouped content. Flat with a hairline border instead of heavy shadows.
 * Pass [onClick] to make it interactive (adds ripple and a subtle press scale).
 */
@Composable
fun ZenCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    contentPadding: PaddingValues = PaddingValues(ZenSpacing.lg),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = MaterialTheme.shapes.medium
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    if (onClick != null) {
        val source = remember { MutableInteractionSource() }
        Surface(
            onClick = onClick,
            modifier = modifier.scale(rememberPressScale(source)),
            shape = shape,
            color = containerColor,
            border = border,
            interactionSource = source
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    } else {
        Surface(modifier = modifier, shape = shape, color = containerColor, border = border) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    }
}

/** Gradient-filled card for hero content (recommendation, quick actions). */
@Composable
fun ZenGradientCard(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(ZenSpacing.lg),
    content: @Composable BoxScope.() -> Unit
) {
    val shape = MaterialTheme.shapes.large
    val source = remember { MutableInteractionSource() }
    val base = modifier
        .scale(if (onClick != null) rememberPressScale(source) else 1f)
        .clip(shape)
        .background(Brush.linearGradient(colors))
    val clickable = if (onClick != null) {
        base.clickable(
            interactionSource = source,
            indication = rememberRipple(),
            role = Role.Button,
            onClick = onClick
        )
    } else base
    Box(modifier = clickable.padding(contentPadding), content = content)
}

enum class ZenButtonStyle { Primary, Secondary, Text }

/** App-wide button with consistent height, shape and optional leading icon. */
@Composable
fun ZenButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ZenButtonStyle = ZenButtonStyle.Primary,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val shape = MaterialTheme.shapes.large
    val source = remember { MutableInteractionSource() }
    val buttonModifier = modifier
        .heightIn(min = 52.dp)
        .scale(rememberPressScale(source))
    val label: @Composable () -> Unit = {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(ZenSpacing.sm))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
    when (style) {
        ZenButtonStyle.Primary -> Button(
            onClick = onClick, modifier = buttonModifier, enabled = enabled, shape = shape,
            interactionSource = source, colors = ButtonDefaults.buttonColors()
        ) { label() }
        ZenButtonStyle.Secondary -> FilledTonalButton(
            onClick = onClick, modifier = buttonModifier, enabled = enabled, shape = shape,
            interactionSource = source
        ) { label() }
        ZenButtonStyle.Text -> TextButton(
            onClick = onClick, modifier = buttonModifier, enabled = enabled, shape = shape,
            interactionSource = source
        ) { label() }
    }
}

/** Selectable chip (filters, theme choices). */
@Composable
fun ZenChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        label = { Text(label, style = MaterialTheme.typography.labelLarge) }
    )
}

/** Section title with an optional trailing action. Marked as a heading for TalkBack. */
@Composable
fun ZenSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ZenSpacing.screen),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() }
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

/** Animated placeholder shimmer for loading states. */
fun Modifier.zenShimmer(): Modifier = composed {
    if (LocalReducedMotion.current) {
        return@composed this
            .alpha(0.55f)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(ZenMotion.LONG * 2, easing = ZenMotion.Calm),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    this
        .alpha(alpha)
        .background(MaterialTheme.colorScheme.surfaceVariant)
}

/** Rounded skeleton block used to build loading layouts. */
@Composable
fun ZenSkeleton(
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 16.dp
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(MaterialTheme.shapes.small)
            .zenShimmer()
    )
}

/** Friendly empty state with optional call to action. */
@Composable
fun ZenEmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(ZenSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.height(ZenSpacing.lg))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(ZenSpacing.xs))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(ZenSpacing.lg))
            ZenButton(text = actionLabel, onClick = onAction)
        }
    }
}

/** Gradient colors for the time-of-day atmosphere behind screens. */
@Composable
fun zenBackdropColors(timeOfDay: TimeOfDay): List<Color> {
    val base = MaterialTheme.colorScheme.background
    val dark = base.luminance() < 0.5f
    val tint = when (timeOfDay) {
        TimeOfDay.MORNING -> if (dark) Color(0xFF3A2E4A) else Color(0xFFFFE3CF)
        TimeOfDay.AFTERNOON -> if (dark) Color(0xFF22304A) else Color(0xFFE3EBFF)
        TimeOfDay.EVENING -> if (dark) Color(0xFF3B2A45) else Color(0xFFF7D7DF)
        TimeOfDay.NIGHT -> if (dark) Color(0xFF1B1D45) else Color(0xFFDCDDF5)
    }
    return listOf(tint, base)
}

/** Full-size vertical gradient atmosphere that reflects the time of day. */
@Composable
fun ZenBackdrop(
    modifier: Modifier = Modifier,
    timeOfDay: TimeOfDay = TimeOfDay.fromHour(java.time.LocalTime.now().hour),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.background(Brush.verticalGradient(zenBackdropColors(timeOfDay))),
        content = content
    )
}

/**
 * Text whose value change animates (old value rises out, new value rises in). Used for
 * counters and stats so numbers feel alive. Falls back to a plain cross-fade with
 * reduced motion.
 */
@Composable
fun ZenAnimatedText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineSmall,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null
) {
    val reduced = LocalReducedMotion.current
    AnimatedContent(
        targetState = text,
        modifier = modifier,
        transitionSpec = {
            if (reduced) {
                fadeIn(tween(ZenMotion.SHORT)) togetherWith fadeOut(tween(ZenMotion.SHORT))
            } else {
                (slideInVertically(tween(ZenMotion.MEDIUM, easing = ZenMotion.Standard)) { it / 2 } +
                    fadeIn(tween(ZenMotion.MEDIUM))) togetherWith
                    (slideOutVertically(tween(ZenMotion.SHORT)) { -it / 2 } + fadeOut(tween(ZenMotion.SHORT)))
            }
        },
        label = "zenAnimatedText"
    ) { value ->
        Text(text = value, style = style, color = color, textAlign = textAlign)
    }
}
