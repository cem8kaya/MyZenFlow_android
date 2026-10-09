package com.oqza.myzenflow.presentation.screens

import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import com.oqza.myzenflow.utils.pluralString
import com.oqza.myzenflow.presentation.components.ZenReadableWidth
import com.oqza.myzenflow.BuildConfig
import com.oqza.myzenflow.presentation.navigation.navigateTo
import com.oqza.myzenflow.presentation.theme.zenTabScreenInsets
import androidx.compose.material.icons.outlined.AutoAwesome
import com.oqza.myzenflow.presentation.components.ZenAnimatedText
import com.oqza.myzenflow.presentation.theme.ZenDawnGold
import com.oqza.myzenflow.presentation.theme.ZenIndigo
import com.oqza.myzenflow.presentation.theme.ZenSpacing
import com.oqza.myzenflow.presentation.components.ZenSkeleton
import com.oqza.myzenflow.presentation.components.ZenGradientCard
import com.oqza.myzenflow.presentation.components.ZenButton
import com.oqza.myzenflow.presentation.components.ZenCard
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.oqza.myzenflow.R
import com.oqza.myzenflow.presentation.navigation.Screen
import com.oqza.myzenflow.presentation.viewmodels.ProfileViewModel

/**
 * ProfileScreen with comprehensive user info and stats
 * Displays avatar, name, stats, quick links, and premium status
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController? = null,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showEditNameDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    ZenReadableWidth {
        Scaffold(
            contentWindowInsets = zenTabScreenInsets(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.profile_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { padding ->
            if (uiState.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(ZenSpacing.screen),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ZenSkeleton(modifier = Modifier.size(100.dp), height = 100.dp)
                    Spacer(modifier = Modifier.height(ZenSpacing.lg))
                    ZenSkeleton(modifier = Modifier.fillMaxWidth(0.5f), height = 28.dp)
                    Spacer(modifier = Modifier.height(ZenSpacing.xl))
                    ZenSkeleton(modifier = Modifier.fillMaxWidth(), height = 110.dp)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(scrollState)
                        .padding(horizontal = ZenSpacing.screen)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Avatar and Name Section
                    ProfileHeaderSection(
                        userName = uiState.userName,
                        memberSince = uiState.memberSince,
                        onEditClick = { showEditNameDialog = true }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Stats Section
                    Text(
                        text = stringResource(R.string.your_stats),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    StatsGrid(
                        totalSessions = uiState.stats.totalSessions,
                        totalMinutes = uiState.stats.totalMinutes,
                        currentStreak = uiState.stats.currentStreak,
                        longestStreak = uiState.stats.longestStreak,
                        favoriteExercise = viewModel.getFavoriteExerciseName(uiState.stats.favoriteBreathingExercise)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Quick Links Section
                    Text(
                        text = stringResource(R.string.profile_quick_links),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    QuickLinksSection(
                        onAchievementsClick = { navController?.navigateTo(Screen.ZenGarden.route) },
                        onHistoryClick = { navController?.navigateTo(Screen.Calendar.route) },
                        onWeeklyClick = { navController?.navigateTo(Screen.WeeklySummary.route) },
                        onSettingsClick = { navController?.navigateTo(Screen.Settings.route) }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Premium Card (if not premium)
                    if (BuildConfig.PREMIUM_ENABLED && !uiState.isPremium) {
                        PremiumCard(onUnlock = { navController?.navigateTo(Screen.Paywall.route) })
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            // Edit Name Dialog
            if (showEditNameDialog) {
                EditNameDialog(
                    currentName = uiState.userName,
                    onDismiss = { showEditNameDialog = false },
                    onSave = { newName ->
                        viewModel.updateUserName(newName)
                        showEditNameDialog = false
                    }
                )
            }

            // Error Snackbar
            uiState.error?.let { error ->
                LaunchedEffect(error) {
                    viewModel.clearError()
                }
            }
        }
    }
}

/**
 * Profile header with avatar and name
 */
@Composable
private fun ProfileHeaderSection(
    userName: String,
    memberSince: String,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(ZenIndigo, Color(0xFF9A5C8F))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name with edit button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onEditClick,
                modifier = Modifier.size(48.dp) // minimum touch target
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.profile_edit_name),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Member since
        Text(
            text = stringResource(R.string.profile_member_since, memberSince),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Stats grid with 2x2 layout
 */
@Composable
private fun StatsGrid(
    totalSessions: Int,
    totalMinutes: Int,
    currentStreak: Int,
    longestStreak: Int,
    favoriteExercise: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileStatCard(
                icon = Icons.Outlined.CheckCircle,
                label = stringResource(R.string.total_sessions),
                value = totalSessions.toString(),
                modifier = Modifier.weight(1f)
            )

            ProfileStatCard(
                icon = Icons.Outlined.Timer,
                label = stringResource(R.string.total_minutes),
                value = totalMinutes.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileStatCard(
                icon = Icons.Outlined.LocalFireDepartment,
                label = stringResource(R.string.current_streak),
                value = pluralString(R.plurals.days_count, currentStreak, currentStreak),
                modifier = Modifier.weight(1f)
            )

            ProfileStatCard(
                icon = Icons.Outlined.EmojiEvents,
                label = stringResource(R.string.profile_longest_streak),
                value = pluralString(R.plurals.days_count, longestStreak, longestStreak),
                modifier = Modifier.weight(1f)
            )
        }

        // Favorite Exercise Card (full width)
        ProfileStatCard(
            icon = Icons.Outlined.Air,
            label = stringResource(R.string.profile_favorite_exercise),
            value = favoriteExercise,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Individual stat card
 */
@Composable
private fun ProfileStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    ZenCard(
        modifier = modifier.clearAndSetSemantics { contentDescription = "$value $label" }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(ZenSpacing.sm))

            ZenAnimatedText(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(ZenSpacing.xs))

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Quick links section
 */
@Composable
private fun QuickLinksSection(
    onAchievementsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onWeeklyClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickLinkItem(
            icon = Icons.Outlined.EmojiEvents,
            title = stringResource(R.string.profile_achievements),
            subtitle = stringResource(R.string.profile_achievements_subtitle),
            onClick = onAchievementsClick
        )

        QuickLinkItem(
            icon = Icons.Outlined.CalendarMonth,
            title = stringResource(R.string.profile_history),
            subtitle = stringResource(R.string.profile_history_subtitle),
            onClick = onHistoryClick
        )

        QuickLinkItem(
            icon = Icons.Outlined.AutoAwesome,
            title = stringResource(R.string.weekly_title),
            subtitle = stringResource(R.string.weekly_card_subtitle),
            onClick = onWeeklyClick
        )

        QuickLinkItem(
            icon = Icons.Outlined.Settings,
            title = stringResource(R.string.screen_settings),
            subtitle = stringResource(R.string.profile_settings_subtitle),
            onClick = onSettingsClick
        )
    }
}

/**
 * Quick link item
 */
@Composable
private fun QuickLinkItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ZenCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(ZenSpacing.lg))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Premium upgrade card
 */
@Composable
private fun PremiumCard(onUnlock: () -> Unit) {
    ZenGradientCard(
        colors = listOf(ZenIndigo, Color(0xFF6B58B5), Color(0xFF9A5C8F)),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(ZenSpacing.xl)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = ZenDawnGold
            )

            Spacer(modifier = Modifier.height(ZenSpacing.md))

            Text(
                text = stringResource(R.string.premium_title),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(ZenSpacing.sm))

            Text(
                text = stringResource(R.string.premium_card_message),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.92f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(ZenSpacing.lg))

            ZenButton(
                text = stringResource(R.string.button_unlock),
                onClick = onUnlock,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Edit name dialog
 */
@Composable
private fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.edit_profile))
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.profile_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name) },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.button_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        }
    )
}
