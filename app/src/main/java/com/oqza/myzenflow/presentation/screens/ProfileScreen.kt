package com.oqza.myzenflow.presentation.screens

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

    Scaffold(
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
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
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
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
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
                    text = "Quick Links",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                QuickLinksSection(
                    onAchievementsClick = { navController?.navigate(Screen.ZenGarden.route) },
                    onHistoryClick = { navController?.navigate(Screen.Calendar.route) },
                    onSettingsClick = { navController?.navigate(Screen.Settings.route) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Premium Card (if not premium)
                if (!uiState.isPremium) {
                    PremiumCard()
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
                        colors = listOf(
                            Color(0xFF6366F1),
                            Color(0xFF8B5CF6)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Profile Avatar",
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
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onEditClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit name",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Member since
        Text(
            text = "Member since $memberSince",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
                value = "$currentStreak ${stringResource(R.string.days_count, currentStreak).substringAfter(" ")}",
                modifier = Modifier.weight(1f)
            )

            ProfileStatCard(
                icon = Icons.Outlined.EmojiEvents,
                label = "Longest Streak",
                value = "$longestStreak ${stringResource(R.string.days_count, longestStreak).substringAfter(" ")}",
                modifier = Modifier.weight(1f)
            )
        }

        // Favorite Exercise Card (full width)
        ProfileStatCard(
            icon = Icons.Outlined.Air,
            label = "Favorite Exercise",
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
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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
    onSettingsClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickLinkItem(
            icon = Icons.Outlined.EmojiEvents,
            title = "My Achievements",
            subtitle = "View Zen Garden achievements",
            onClick = onAchievementsClick
        )

        QuickLinkItem(
            icon = Icons.Outlined.CalendarMonth,
            title = "Session History",
            subtitle = "View calendar and progress",
            onClick = onHistoryClick
        )

        QuickLinkItem(
            icon = Icons.Outlined.Settings,
            title = stringResource(R.string.screen_settings),
            subtitle = "App settings and preferences",
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

/**
 * Premium upgrade card
 */
@Composable
private fun PremiumCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Premium",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.premium_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Get unlimited access to all premium features",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* TODO: Navigate to premium screen */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.button_unlock))
            }
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
                label = { Text("Name") },
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
