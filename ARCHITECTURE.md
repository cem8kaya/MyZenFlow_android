# ZenFlow Architecture

Bu doküman ZenFlow Android uygulamasının mimari yapısını detaylı olarak açıklar.

## 📐 Genel Mimari

ZenFlow **MVVM (Model-View-ViewModel)** pattern ve **Clean Architecture** prensipleriyle geliştirilmiştir.

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Screens    │  │  Components  │  │  ViewModels  │  │
│  │  (Compose)   │←→│  (Compose)   │←→│  (StateFlow) │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└──────────────────────────┬──────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│                      Domain Layer                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  Use Cases   │  │   Services   │  │   Workers    │  │
│  │ (Business)   │  │ (Foreground) │  │ (Background) │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└──────────────────────────┬──────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│                       Data Layer                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Repositories │  │     DAOs     │  │  DataStore   │  │
│  │              │←→│    (Room)    │  │ (Preferences)│  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
```

## 🏛️ Layer Detayları

### 1. Presentation Layer

UI katmanı, kullanıcı arayüzünden ve state management'tan sorumludur.

#### Screens (Jetpack Compose)
```kotlin
presentation/
├── screens/
│   ├── HomeScreen.kt          // Ana ekran
│   ├── BreathingScreen.kt     // Nefes egzersizleri
│   ├── FocusTimerScreen.kt    // Pomodoro timer
│   ├── CalendarScreen.kt      // Geçmiş ve takvim
│   ├── ZenGardenScreen.kt     // Zen bahçesi
│   ├── ProfileScreen.kt       // Profil
│   ├── SettingsScreen.kt      // Ayarlar
│   └── OnboardingScreen.kt    // İlk açılış
```

**Sorumluluklar:**
- UI rendering (Jetpack Compose)
- User interaction handling
- Navigation
- State observation (StateFlow/Flow)

**Örnek:**
```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()

    // UI composition based on state
    when {
        uiState.isLoading -> LoadingIndicator()
        uiState.error != null -> ErrorMessage(uiState.error)
        else -> HomeContent(uiState)
    }
}
```

#### Components (Reusable UI)
```kotlin
presentation/
├── components/
│   ├── QuickActionCard.kt     // Hızlı erişim kartları
│   ├── StatCard.kt            // İstatistik kartları
│   ├── AchievementComponents.kt // Başarım bileşenleri
│   ├── BottomNavigationBar.kt // Alt navigasyon
│   └── ...
```

**Prensip:** Tek sorumluluk, yeniden kullanılabilirlik, composable functions.

#### ViewModels
```kotlin
presentation/
├── viewmodels/
│   ├── HomeViewModel.kt
│   ├── BreathingViewModel.kt
│   ├── PomodoroViewModel.kt
│   ├── CalendarViewModel.kt
│   ├── ZenGardenViewModel.kt
│   ├── ProfileViewModel.kt
│   └── SettingsViewModel.kt
```

**Sorumluluklar:**
- UI state management (StateFlow)
- Business logic orchestration
- Repository interaction
- Configuration change survival
- Lifecycle awareness

**Örnek:**
```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val sessions = sessionRepository.getRecentSessions(10).first()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    sessions = sessions
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error loading data"
                )
            }
        }
    }
}
```

**State Pattern:**
```kotlin
data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val sessions: List<SessionData> = emptyList(),
    val stats: UserStats? = null
)
```

### 2. Domain Layer

Business logic ve uygulamanın domain spesifik işlemlerini içerir.

#### Services
```kotlin
domain/
├── services/
│   ├── PomodoroTimerService.kt    // Foreground service
│   ├── NotificationHelper.kt      // Bildirim yönetimi
│   ├── HapticManager.kt           // Titreşim feedback
│   └── BreathingAudioManager.kt   // Ses yönetimi
```

**PomodoroTimerService:**
- Foreground service for long-running timer
- State persistence during configuration changes
- Notification with controls
- Background execution

```kotlin
class PomodoroTimerService : Service() {
    private val binder = TimerBinder()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTimer()
            ACTION_PAUSE -> pauseTimer()
            ACTION_STOP -> stopTimer()
        }
        return START_NOT_STICKY
    }

    private fun startTimer() {
        createNotification()
        startForeground(NOTIFICATION_ID, notification)
        // Timer logic with Coroutines
    }
}
```

#### Workers
```kotlin
domain/
├── workers/
│   └── ReminderWorker.kt    // Periodic reminders
```

**WorkManager kullanımı:**
```kotlin
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Show reminder notification
        notificationHelper.showReminder()
        return Result.success()
    }
}
```

#### Receivers
```kotlin
domain/
├── receivers/
│   └── TimerActionReceiver.kt    // Notification actions
```

### 3. Data Layer

Veri kaynakları ve veri yönetiminden sorumludur.

#### Repository Pattern
```kotlin
data/
├── repository/
│   ├── SessionRepository.kt
│   ├── FocusRepository.kt
│   ├── BreathingRepository.kt
│   ├── AchievementRepository.kt
│   ├── StatsRepository.kt
│   └── PreferencesRepository.kt
```

**Sorumluluklar:**
- Data source abstraction
- Multiple data source coordination
- Data transformation (Entity ↔ Model)
- Error handling
- Caching strategy

**Örnek:**
```kotlin
class SessionRepository @Inject constructor(
    private val meditationDao: MeditationSessionDao,
    private val focusDao: FocusSessionDao,
    private val breathingDao: BreathingSessionDao
) {
    fun getRecentSessions(limit: Int): Flow<List<SessionData>> = flow {
        // Combine all session types
        val meditation = meditationDao.getRecentSessions(limit).first()
        val focus = focusDao.getRecentSessions(limit).first()
        val breathing = breathingDao.getRecentSessions(limit).first()

        // Merge and sort by date
        val allSessions = (meditation.map { it.toSessionData() } +
                          focus.map { it.toFocusSessionData() } +
                          breathing.map { /* transform */ })
            .sortedByDescending { it.date }
            .take(limit)

        emit(allSessions)
    }

    suspend fun insertSession(session: SessionData) {
        when (session.type) {
            SessionType.MEDITATION -> meditationDao.insert(
                MeditationSessionEntity.fromSessionData(session)
            )
            SessionType.FOCUS -> focusDao.insert(/* ... */)
            SessionType.BREATHING -> breathingDao.insert(/* ... */)
        }
    }
}
```

#### Room Database
```kotlin
data/
├── database/
│   ├── AppDatabase.kt      // Database configuration
│   └── Converters.kt       // Type converters
├── entities/
│   ├── MeditationSessionEntity.kt
│   ├── FocusSessionEntity.kt
│   ├── BreathingSessionEntity.kt
│   └── AchievementEntity.kt
└── dao/
    ├── MeditationSessionDao.kt
    ├── FocusSessionDao.kt
    ├── BreathingSessionDao.kt
    └── AchievementDao.kt
```

**Database Schema:**

```sql
-- Meditation Sessions
CREATE TABLE meditation_sessions (
    id TEXT PRIMARY KEY,
    date INTEGER,
    duration INTEGER,
    type TEXT,
    breathingExercise TEXT,
    mood TEXT,
    notes TEXT,
    completed INTEGER,
    INDEX(date),
    INDEX(completed),
    INDEX(date, completed)
);

-- Focus Sessions (Pomodoro)
CREATE TABLE focus_sessions (
    id TEXT PRIMARY KEY,
    date INTEGER,
    duration INTEGER,
    focusDuration INTEGER,
    breakDuration INTEGER,
    completedCycles INTEGER,
    targetCycles INTEGER,
    taskName TEXT,
    completed INTEGER,
    interrupted INTEGER,
    INDEX(date),
    INDEX(completed),
    INDEX(date, completed)
);

-- Breathing Sessions
CREATE TABLE breathing_sessions (
    id TEXT PRIMARY KEY,
    date INTEGER,
    exerciseId TEXT,
    exerciseName TEXT,
    durationSeconds INTEGER,
    cyclesCompleted INTEGER,
    totalCycles INTEGER,
    inhaleSeconds INTEGER,
    holdInhaleSeconds INTEGER,
    exhaleSeconds INTEGER,
    holdExhaleSeconds INTEGER,
    completed INTEGER,
    notes TEXT,
    INDEX(date),
    INDEX(completed),
    INDEX(exerciseId),
    INDEX(date, completed)
);

-- Achievements
CREATE TABLE achievements (
    type TEXT PRIMARY KEY,
    unlockedAt INTEGER,
    isUnlocked INTEGER,
    progress INTEGER,
    progressTarget INTEGER,
    INDEX(isUnlocked),
    INDEX(unlockedAt)
);
```

**Performance Optimization:**
- Indices on frequently queried columns (date, completed)
- Composite indices for complex queries
- Flow-based reactive queries
- Pagination for large datasets

**Migrations:**
```kotlin
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add indices for performance
        database.execSQL("CREATE INDEX index_meditation_sessions_date ON meditation_sessions(date)")
        // ... more indices
    }
}
```

#### DataStore (Preferences)
```kotlin
data/
├── datastore/
│   ├── PreferencesDataStore.kt
│   └── PomodoroTimerDataStore.kt
```

**Proto DataStore kullanımı:**
```kotlin
class PreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val userPreferences: Flow<UserPreferences> = dataStore.data
        .map { preferences ->
            UserPreferences(
                language = preferences[LANGUAGE_KEY] ?: "tr",
                theme = preferences[THEME_KEY] ?: "system",
                // ...
            )
        }

    suspend fun updateLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }
}
```

## 🔄 Veri Akışı

### Read Flow (Data → UI)
```
Database/DataStore
    ↓ (Flow/StateFlow)
DAO/DataStore
    ↓ (Flow)
Repository (Data transformation)
    ↓ (Flow)
ViewModel (State mapping)
    ↓ (StateFlow)
Composable (UI)
```

### Write Flow (UI → Data)
```
User Action
    ↓
Composable (Event)
    ↓
ViewModel (Business logic)
    ↓
Repository (Data validation)
    ↓
DAO/DataStore (Persistence)
    ↓
Database/DataStore
```

## 🔌 Dependency Injection (Hilt)

### Module Organization
```kotlin
di/
├── AppModule.kt         // Application-wide dependencies
└── DatabaseModule.kt    // Database dependencies
```

**AppModule:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.createDataStore("user_preferences")
    }
}
```

**DatabaseModule:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "myzenflow_database"
        )
        .addMigrations(MIGRATION_3_4)
        .build()
    }

    @Provides
    fun provideMeditationDao(db: AppDatabase) = db.meditationSessionDao()
}
```

### Scope Hierarchy
```
SingletonComponent (Application-wide)
    ↓
ActivityRetainedComponent (Survives config changes)
    ↓
ViewModelComponent (ViewModel lifecycle)
    ↓
ActivityComponent (Activity lifecycle)
```

## 🎭 State Management

### StateFlow Pattern
```kotlin
// ViewModel
private val _uiState = MutableStateFlow(UiState())
val uiState: StateFlow<UiState> = _uiState.asStateFlow()

// Update state
_uiState.update { currentState ->
    currentState.copy(isLoading = false, data = newData)
}

// Composable
val state by viewModel.uiState.collectAsState()
```

### Flow Operators
```kotlin
// Transform
sessionRepository.getAllSessions()
    .map { sessions -> sessions.filter { it.completed } }
    .flowOn(Dispatchers.IO)

// Combine
combine(
    sessionsFlow,
    preferencesFlow,
    achievementsFlow
) { sessions, prefs, achievements ->
    UiState(sessions, prefs, achievements)
}

// Debounce (for search)
searchQuery
    .debounce(300)
    .distinctUntilChanged()
    .flatMapLatest { query ->
        repository.search(query)
    }
```

## 🧪 Testing Strategy

### Unit Tests (JUnit + MockK)
```kotlin
@Test
fun `loadData with completed sessions updates state correctly`() = runTest {
    // Arrange
    val mockData = listOf(SessionData(completed = true))
    coEvery { repository.getSessions() } returns flowOf(mockData)

    // Act
    val viewModel = HomeViewModel(repository)
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    assertEquals(1, viewModel.uiState.value.sessionCount)
}
```

### UI Tests (Compose Testing)
```kotlin
@Test
fun breathingScreen_startButton_startsExercise() {
    composeTestRule.setContent {
        BreathingScreen(viewModel = testViewModel)
    }

    composeTestRule
        .onNodeWithContentDescription("Başlat")
        .performClick()

    composeTestRule
        .onNodeWithText("Nefes Al")
        .assertIsDisplayed()
}
```

## 🚀 Performance Best Practices

### Compose Optimization
```kotlin
// ✅ Use remember for expensive calculations
val expensiveValue = remember(key1) {
    calculateExpensiveValue()
}

// ✅ Use derivedStateOf for derived state
val filteredList by remember {
    derivedStateOf {
        list.filter { it.isActive }
    }
}

// ✅ Use LaunchedEffect for side effects
LaunchedEffect(key1) {
    viewModel.loadData()
}
```

### Database Optimization
- Indices on frequently queried columns
- Pagination for large lists
- Background thread execution (Dispatchers.IO)
- Transaction batching

### Memory Management
- LeakCanary integration (debug)
- Proper lifecycle management
- Flow cancellation
- WeakReference where appropriate

## 📱 Navigation Architecture

```kotlin
NavHost(navController, startDestination = "home") {
    composable("home") { HomeScreen() }
    composable("breathing") { BreathingScreen() }
    composable("focus") { FocusTimerScreen() }
    composable("calendar") { CalendarScreen() }
    composable("zen_garden") { ZenGardenScreen() }
    composable("profile") { ProfileScreen() }
    composable("settings") { SettingsScreen() }
}
```

**State Preservation:**
- `saveState = true` for bottom navigation
- `restoreState = true` for back navigation
- ViewModel survives configuration changes

## 🔐 Security Considerations

- No hardcoded secrets
- ProGuard/R8 obfuscation in release
- Secure data storage (encrypted preferences if needed)
- No sensitive data in logs (release)
- Runtime permission checks
- Input validation

## 📊 Performance Metrics

**Target Metrics:**
- Cold start: < 2 seconds
- Screen transition: < 100ms
- Database query: < 50ms
- Memory usage: < 150MB
- APK size: < 50MB
- Frame rate: 60 FPS

---

**Last Updated**: December 2024
**Version**: 1.0.0
