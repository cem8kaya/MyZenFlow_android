package com.oqza.myzenflow.presentation.screens

import com.oqza.myzenflow.presentation.theme.zenTabScreenInsets
import com.oqza.myzenflow.presentation.theme.LocalReducedMotion
import com.oqza.myzenflow.presentation.theme.ZenDawnGold
import com.oqza.myzenflow.R
import androidx.compose.ui.res.stringResource
import com.oqza.myzenflow.presentation.components.ZenCard
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oqza.myzenflow.presentation.components.*
import com.oqza.myzenflow.presentation.viewmodels.ZenGardenTab
import com.oqza.myzenflow.presentation.viewmodels.ZenGardenViewModel

/**
 * Zen Garden screen - Main gamification screen
 * Shows tree visualization, achievements, and statistics
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZenGardenScreen(
    viewModel: ZenGardenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val particleSystem = rememberParticleSystem()

    Scaffold(
        contentWindowInsets = zenTabScreenInsets(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.garden_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab row
            TabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                Tab(
                    selected = uiState.selectedTab == ZenGardenTab.TREE,
                    onClick = { viewModel.selectTab(ZenGardenTab.TREE) },
                    text = { Text(stringResource(R.string.garden_tab_tree)) },
                    icon = { Icon(Icons.Default.Park, contentDescription = null) }
                )
                Tab(
                    selected = uiState.selectedTab == ZenGardenTab.ACHIEVEMENTS,
                    onClick = { viewModel.selectTab(ZenGardenTab.ACHIEVEMENTS) },
                    text = { Text(stringResource(R.string.garden_tab_achievements)) },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) }
                )
                Tab(
                    selected = uiState.selectedTab == ZenGardenTab.STATS,
                    onClick = { viewModel.selectTab(ZenGardenTab.STATS) },
                    text = { Text(stringResource(R.string.garden_tab_stats)) },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = null) }
                )
            }

            // Content based on selected tab
            when (uiState.selectedTab) {
                ZenGardenTab.TREE -> TreeTab(
                    uiState = uiState,
                    particleSystem = particleSystem
                )
                ZenGardenTab.ACHIEVEMENTS -> AchievementsTab(uiState = uiState)
                ZenGardenTab.STATS -> StatsTab(uiState = uiState)
            }
        }
    }
}

/**
 * Tree tab - Shows tree visualization and stats cards
 */
@Composable
private fun TreeTab(
    uiState: com.oqza.myzenflow.presentation.viewmodels.ZenGardenUiState,
    particleSystem: ParticleSystem
) {
    // Track previous level for level-up detection
    var previousLevel by remember { mutableIntStateOf(uiState.userStats.treeLevel) }
    var showLevelUpAnimation by remember { mutableStateOf(false) }

    // Detect level change
    LaunchedEffect(uiState.userStats.treeLevel) {
        if (uiState.userStats.treeLevel > previousLevel && previousLevel > 0) {
            showLevelUpAnimation = true
        }
        previousLevel = uiState.userStats.treeLevel
    }

    // Level-up animation
    LevelUpAnimation(
        show = showLevelUpAnimation,
        newLevel = uiState.userStats.treeLevel,
        onDismiss = { showLevelUpAnimation = false }
    )

    // Particle burst for level-up
    LevelUpParticleBurst(
        particleSystem = particleSystem,
        width = 800f,
        height = 1200f,
        trigger = showLevelUpAnimation && !LocalReducedMotion.current
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats cards
        item {
            StatsCardsRow(uiState = uiState)
        }

        // Tree visualization
        item {
            ZenCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.background,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    TreeVisualization(
                        level = uiState.userStats.treeLevel,
                        progress = uiState.userStats.treeGrowthProgress,
                        particles = particleSystem.getParticles()
                    )

                    // Particle effect
                    LaunchedParticleEffect(
                        particleSystem = particleSystem,
                        width = 800f, // Approximate width
                        height = 1200f, // Approximate height
                        enabled = uiState.userStats.currentStreak > 0 && !LocalReducedMotion.current,
                        particleType = ParticleType.SPARKLE
                    )
                }
            }
        }

        // Growth info
        item {
            GrowthInfoCard(uiState = uiState)
        }
    }
}

/**
 * Stats cards row - Shows key metrics
 */
@Composable
private fun StatsCardsRow(
    uiState: com.oqza.myzenflow.presentation.viewmodels.ZenGardenUiState
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        item {
            StatCard(
                title = stringResource(R.string.garden_stat_sessions),
                value = uiState.userStats.totalSessions.toString(),
                icon = Icons.Default.SelfImprovement,
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            StatCard(
                title = stringResource(R.string.garden_stat_minutes),
                value = uiState.userStats.totalMinutes.toString(),
                icon = Icons.Default.Timer,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        item {
            StatCard(
                title = stringResource(R.string.garden_stat_streak),
                value = stringResource(R.string.days_short, uiState.userStats.currentStreak),
                icon = Icons.Default.Whatshot,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        item {
            StatCard(
                title = stringResource(R.string.garden_stat_achievements),
                value = "${uiState.unlockedAchievements.size}/${uiState.achievements.size}",
                icon = Icons.Default.EmojiEvents,
                color = ZenDawnGold
            )
        }
    }
}

/**
 * Individual stat card
 */
@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    ZenCard(
        modifier = Modifier
            .width(140.dp)
            .height(100.dp),
        containerColor = color.copy(alpha = 0.1f),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Growth info card - Shows next level requirements
 */
@Composable
private fun GrowthInfoCard(
    uiState: com.oqza.myzenflow.presentation.viewmodels.ZenGardenUiState
) {
    ZenCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.garden_growth_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.garden_level_format, uiState.userStats.treeLevel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LinearProgressIndicator(
                progress = { uiState.userStats.treeGrowthProgress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            if (uiState.userStats.treeLevel < 5) {
                Text(
                    text = getNextLevelInfo(uiState.userStats.treeLevel, uiState.userStats.totalMinutes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            } else {
                Text(
                    text = stringResource(R.string.garden_max_level),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Get next level info text
 */
@Composable
private fun getNextLevelInfo(currentLevel: Int, totalMinutes: Int): String {
    val thresholds = listOf(0, 30, 120, 360, 900, 1800)
    if (currentLevel >= 5) return stringResource(R.string.garden_max_level)

    val nextThreshold = thresholds[currentLevel + 1]
    val minutesNeeded = nextThreshold - totalMinutes

    return stringResource(R.string.garden_next_level, minutesNeeded)
}

/**
 * Achievements tab - Placeholder (will be implemented with BadgeGallery)
 */
@Composable
private fun AchievementsTab(
    uiState: com.oqza.myzenflow.presentation.viewmodels.ZenGardenUiState
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.garden_unlocked_title, uiState.unlockedAchievements.size),
                style = MaterialTheme.typography.titleLarge
            )
        }

        items(uiState.unlockedAchievements) { achievement ->
            AchievementCard(achievement = achievement, isUnlocked = true)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.garden_locked_title, uiState.lockedAchievements.size),
                style = MaterialTheme.typography.titleLarge
            )
        }

        items(uiState.lockedAchievements) { achievement ->
            AchievementCard(achievement = achievement, isUnlocked = false)
        }
    }
}

/**
 * Stats tab - Shows charts and statistics
 */
@Composable
private fun StatsTab(
    uiState: com.oqza.myzenflow.presentation.viewmodels.ZenGardenUiState
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.garden_stats_title),
                style = MaterialTheme.typography.titleLarge
            )
        }

        // Weekly chart
        item {
            WeeklyBarChart(
                weeklyData = uiState.weeklyData
            )
        }

        // Monthly chart
        item {
            MonthlyBarChart(
                monthlyData = uiState.monthlyData
            )
        }

        // All-time stats
        item {
            AllTimeStatsCard(
                totalSessions = uiState.userStats.totalSessions,
                totalMinutes = uiState.userStats.totalMinutes,
                longestStreak = uiState.userStats.longestStreak,
                favoriteExercise = uiState.userStats.favoriteBreathingExercise?.displayName ?: "N/A"
            )
        }

        // Weekly goal progress card
        item {
            ZenCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.garden_weekly_goal),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.garden_goal_progress, uiState.userStats.weeklyCompletedMinutes, uiState.userStats.weeklyGoalMinutes),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "${(uiState.userStats.weeklyProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { uiState.userStats.weeklyProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}
