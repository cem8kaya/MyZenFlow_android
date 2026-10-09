package com.oqza.myzenflow.presentation.screens

import com.oqza.myzenflow.data.models.PracticeGoal
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.Role
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.selectable
import com.oqza.myzenflow.domain.workers.ReminderScheduler
import com.oqza.myzenflow.presentation.theme.ZenIndigo
import android.Manifest
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.oqza.myzenflow.R
import com.oqza.myzenflow.data.repository.PreferencesRepository
import com.oqza.myzenflow.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * OnboardingScreen with multi-page introduction flow
 * Guides users through app features and personalization
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    var userName by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf(PracticeGoal.NONE) }
    var weeklyGoalMinutes by remember { mutableStateOf(210) }
    var notificationsEnabled by remember { mutableStateOf(false) }
    var reminderTime by remember { mutableStateOf("09:00") }

    // Notification permission (Android 13+)
    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ZenIndigo,
                        Color(0xFF6B58B5),
                        Color(0xFF9A5C8F)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> FeaturesPage()
                    2 -> GoalPage(
                        selected = goal,
                        onSelect = { goal = it }
                    )
                    3 -> PersonalizationPage(
                        userName = userName,
                        onNameChange = { userName = it },
                        weeklyGoalMinutes = weeklyGoalMinutes,
                        onGoalChange = { weeklyGoalMinutes = it }
                    )
                    4 -> NotificationsPage(
                        notificationsEnabled = notificationsEnabled,
                        onNotificationsToggle = { enabled ->
                            notificationsEnabled = enabled
                            if (enabled && notificationPermissionState?.status?.isGranted == false) {
                                notificationPermissionState.launchPermissionRequest()
                            }
                        },
                        reminderTime = reminderTime,
                        onTimeChange = { reminderTime = it }
                    )
                }
            }

            // Bottom Navigation
            OnboardingBottomBar(
                currentPage = pagerState.currentPage,
                totalPages = PAGE_COUNT,
                onNextClick = {
                    if (pagerState.currentPage < PAGE_COUNT - 1) {
                        viewModel.navigateToPage(pagerState.currentPage + 1, pagerState)
                    } else {
                        // Complete onboarding
                        viewModel.completeOnboarding(
                            userName = userName.trim(),
                            goal = goal,
                            weeklyGoal = weeklyGoalMinutes,
                            notificationsEnabled = notificationsEnabled &&
                                    (notificationPermissionState?.status?.isGranted ?: true),
                            reminderTime = reminderTime
                        )
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                },
                onSkipClick = {
                    viewModel.completeOnboarding(
                        userName = "",
                        goal = PracticeGoal.NONE,
                        weeklyGoal = 210,
                        notificationsEnabled = false,
                        reminderTime = "09:00"
                    )
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onBackClick = {
                    if (pagerState.currentPage > 0) {
                        viewModel.navigateToPage(pagerState.currentPage - 1, pagerState)
                    }
                }
            )
        }
    }
}

private const val PAGE_COUNT = 5

/**
 * Goal page: what brings the user here. Used to tailor the first suggestions.
 */
@Composable
private fun GoalPage(
    selected: PracticeGoal,
    onSelect: (PracticeGoal) -> Unit
) {
    val options = listOf(
        Triple(PracticeGoal.STRESS, "😮\u200D💨", R.string.onb_goal_stress),
        Triple(PracticeGoal.SLEEP, "🌙", R.string.onb_goal_sleep),
        Triple(PracticeGoal.FOCUS, "🎯", R.string.onb_goal_focus),
        Triple(PracticeGoal.CALM, "🌿", R.string.onb_goal_calm)
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .selectableGroup(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.onb_goal_page_title),
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.onb_goal_page_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        options.forEach { (goal, emoji, label) ->
            val isSelected = goal == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = if (isSelected) 0.32f else 0.14f))
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(goal) })
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = emoji, fontSize = 26.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(label),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                if (isSelected) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

/**
 * Welcome page (Page 1)
 */
@Composable
private fun WelcomePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo/Icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Spa,
                contentDescription = "ZenFlow",
                modifier = Modifier.size(80.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.onb_welcome_title),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.onb_welcome_subtitle),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Features page (Page 2)
 */
@Composable
private fun FeaturesPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.onb_features_title),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        FeatureItem(
            icon = Icons.Outlined.Air,
            title = stringResource(R.string.onb_feature_breathing),
            description = stringResource(R.string.onb_feature_breathing_desc)
        )

        Spacer(modifier = Modifier.height(32.dp))

        FeatureItem(
            icon = Icons.Outlined.Timer,
            title = stringResource(R.string.onb_feature_focus),
            description = stringResource(R.string.onb_feature_focus_desc)
        )

        Spacer(modifier = Modifier.height(32.dp))

        FeatureItem(
            icon = Icons.Outlined.Park,
            title = stringResource(R.string.onb_feature_garden),
            description = stringResource(R.string.onb_feature_garden_desc)
        )
    }
}

/**
 * Personalization page (Page 3)
 */
@Composable
private fun PersonalizationPage(
    userName: String,
    onNameChange: (String) -> Unit,
    weeklyGoalMinutes: Int,
    onGoalChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.onb_personalize_title),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Name Input
        Text(
            text = stringResource(R.string.onb_name_question),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = onNameChange,
            placeholder = { Text(stringResource(R.string.onb_name_hint), color = Color.White.copy(alpha = 0.6f)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                cursorColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Weekly Goal Slider
        Text(
            text = stringResource(R.string.onb_goal_title),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.onb_goal_per_week, weeklyGoalMinutes),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Slider(
            value = weeklyGoalMinutes.toFloat(),
            onValueChange = { onGoalChange(it.toInt()) },
            valueRange = 60f..420f,
            steps = 11,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )

        Text(
            text = stringResource(R.string.onb_goal_per_day, weeklyGoalMinutes / 7),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

/**
 * Notifications page (Page 4)
 */
@Composable
private fun NotificationsPage(
    notificationsEnabled: Boolean,
    onNotificationsToggle: (Boolean) -> Unit,
    reminderTime: String,
    onTimeChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "Notifications",
            modifier = Modifier.size(80.dp),
            tint = Color.White
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.onb_motivated_title),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.onb_motivated_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Notification Toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.onb_daily_reminders),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = if (notificationsEnabled) "At $reminderTime" else "Disabled",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = onNotificationsToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.White.copy(alpha = 0.5f),
                        uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}

/**
 * Feature item component
 */
@Composable
private fun FeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

/**
 * Bottom navigation bar for onboarding
 */
@Composable
private fun OnboardingBottomBar(
    currentPage: Int,
    totalPages: Int,
    onNextClick: () -> Unit,
    onSkipClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        // Page Indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(totalPages) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (index == currentPage) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == currentPage)
                                Color.White
                            else
                                Color.White.copy(alpha = 0.4f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentPage > 0) {
                TextButton(onClick = onBackClick) {
                    Text(
                        text = stringResource(R.string.back),
                        color = Color.White
                    )
                }
            } else {
                TextButton(onClick = onSkipClick) {
                    Text(
                        text = stringResource(R.string.onb_skip),
                        color = Color.White
                    )
                }
            }

            Button(
                onClick = onNextClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = ZenIndigo
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = if (currentPage == totalPages - 1)
                        stringResource(R.string.onb_get_started)
                    else
                        stringResource(R.string.onb_next),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * ViewModel for OnboardingScreen
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    fun navigateToPage(page: Int, pagerState: androidx.compose.foundation.pager.PagerState) {
        viewModelScope.launch {
            pagerState.animateScrollToPage(page)
        }
    }

    fun completeOnboarding(
        userName: String,
        goal: PracticeGoal,
        weeklyGoal: Int,
        notificationsEnabled: Boolean,
        reminderTime: String
    ) {
        viewModelScope.launch {
            preferencesRepository.updateUserName(userName)
            preferencesRepository.updatePrimaryGoal(goal)
            preferencesRepository.updateWeeklyGoal(weeklyGoal)
            preferencesRepository.updateDailyReminder(notificationsEnabled, reminderTime)
            if (notificationsEnabled) reminderScheduler.schedule(reminderTime, replace = true)
            preferencesRepository.setInstallDateIfNeeded()
            preferencesRepository.completeOnboarding()
        }
    }
}
