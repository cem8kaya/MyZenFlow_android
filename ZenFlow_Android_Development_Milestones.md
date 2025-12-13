Mevcut Android repository'sini inceledim. Kod yapısı oldukça iyi kurulmuş - MVVM mimari, Hilt DI, Room database, DataStore, ViewModels ve bazı Composable'lar mevcut. Ancak bazı ekranlar placeholder durumunda ve bazı özellikler eksik. İşte iOS ZenFlow'a paralel, mevcut kod tabanına göre revize edilmiş detaylı geliştirme planı:

---

# 🎯 ZenFlow Android - Revize Edilmiş Geliştirme Planı

## 📊 Mevcut Durum Analizi

### ✅ Tamamlanmış Komponentler
- MVVM + Hilt + Room + DataStore mimarisi kurulu
- Bottom navigation (5 tab)
- Data entities ve DAOs hazır
- ViewModels: BreathingViewModel, PomodoroViewModel, ZenGardenViewModel (partial)
- Services: HapticManager, NotificationHelper, BreathingAudioManager
- Theme: Material 3 color scheme
- Navigation graph yapısı

### ⚠️ Kısmi Tamamlanmış
- BreathingScreen (animasyon ve UI polish gerekli)
- FocusScreen (UI var, tam entegrasyon eksik)
- ZenGardenScreen (skeleton var)

### ❌ Eksik veya Placeholder
- HomeScreen (tamamen placeholder)
- CalendarScreen (placeholder)
- ProfileScreen (placeholder)
- SettingsScreen (eksik)
- Premium billing sistemi
- Localization (strings.xml sadece app_name var)
- Ambient sound assets
- Onboarding flow

---

## 🎯 Milestone 1: HomeScreen ve Quick Actions (2 gün)

### Hedef
iOS HomeView'a eşdeğer kapsamlı ana sayfa oluştur - quick actions, daily stats, motivational content

### Prompt İçeriği

```
ZenFlow Android projesinde HomeScreen'i iOS benzeri şekilde tamamen yeniden oluştur.

MEVCUT DURUM:
- HomeScreen.kt placeholder durumda
- NavGraph'ta HomeScreen route var
- Theme sistem hazır (Material 3)
- BreathingViewModel, PomodoroViewModel, SessionRepository hazır

YARATILACAK YAPITAŞLARI:

1. HomeViewModel oluştur (data layer ile entegrasyon):
   - BreathingRepository, SessionRepository, PreferencesRepository inject et
   - UI state: todayStats (sessions, minutes), weeklyStreak, quickActionCards
   - getUserStats(), getTodaysSessions(), getWeeklyStreak() fonksiyonları
   - refreshData() suspend fonksiyonu

2. HomeScreen composable yeniden yaz:
   - Scaffold ile scroll edebilir Column
   - LazyColumn yerine normal Column + ScrollState (daha smooth)
   
   BÖLÜMLER (yukarıdan aşağıya):
   a) Greeting Header:
      - "Merhaba" + kullanıcı adı (varsa preferences'tan)
      - Motivational quote (random, Türkçe)
      - Bugünün tarihi
      
   b) Today's Stats Card:
      - Row ile 3 istatistik göster
      - Icon + başlık + değer formatında
      - Total sessions today, total minutes, current streak
      - Card stilinde, gradient background
      
   c) Quick Actions Grid:
      - 2x2 grid layout (LazyVerticalGrid)
      - 4 action card:
        1. Breathing Exercises → navigate(Screen.Breathing.route)
        2. Focus Timer → navigate(Screen.Focus.route)
        3. Zen Garden → navigate(Screen.ZenGarden.route)
        4. My Progress → navigate(Screen.Calendar.route)
      - Her card: icon, title, subtitle, gradient background
      - Tıklanabilir (clickable modifier)
      - Haptic feedback ekle (HapticManager)
      
   d) Recent Sessions Section:
      - "Recent Sessions" başlığı
      - Son 3 session göster (SessionRepository.getRecentSessions(3))
      - Her session: icon, exercise name, duration, timestamp
      - LazyRow ile horizontal scroll
      - Eğer boşsa: "No sessions yet" message + başla butonu

3. UI Components oluştur (ayrı dosyalar):
   - StatCard.kt: tek istatistik gösterir (icon, label, value)
   - QuickActionCard.kt: action button card (icon, title, subtitle, gradient, onClick)
   - RecentSessionItem.kt: session bilgisi gösterir (horizontal card)
   - GreetingHeader.kt: greeting + quote + date

4. Animasyonlar ekle:
   - LaunchedEffect ile stats fade-in animation
   - Quick action cards'a scale animation (enter)
   - Pull-to-refresh için SwipeRefresh (Accompanist)

5. Error handling ve loading states:
   - isLoading: CircularProgressIndicator göster
   - Error state: retry button ile
   - Empty state: onboarding hint

STIL REHBERİ:
- Background: MaterialTheme.colorScheme.background
- Cards: MaterialTheme.colorScheme.surfaceVariant
- Gradient: ZenTheme colors (existing theme system)
- Typography: MaterialTheme.typography.headlineMedium, bodyLarge
- Spacing: 16.dp standard, 24.dp section arası
- Corner radius: 16.dp
- Elevation: 4.dp
- Icons: Material Icons Outlined (existing in dependencies)

PERFORMANS:
- remember { } kullan computed values için
- derivedStateOf kullan expensive calculations için
- LaunchedEffect + collectAsStateWithLifecycle for viewModel flows

Tüm kodları oluştur ve mevcut projeye entegre et. Kod hazır olduğunda integration notları ver.
```

### Başarı Kriterleri
- [ ] HomeScreen greeting, stats, quick actions gösterir
- [ ] Navigation tüm ekranlara çalışır
- [ ] Recent sessions listelenir
- [ ] Pull-to-refresh aktif
- [ ] Animations smooth

---

## 🎯 Milestone 2: BreathingScreen UI Polish ve Animation (2 gün)

### Hedef
Mevcut BreathingScreen'i iOS seviyesine taşı - smooth animations, perfect circle, phase transitions

### Prompt İçeriği

```
ZenFlow Android projesinde mevcut BreathingScreen'i iOS kalitesinde smooth animations ve polish ile tamamla.

MEVCUT DURUM:
- BreathingScreen.kt kısmen hazır
- BreathingViewModel var (exercise logic, session tracking)
- BreathingAudioManager, HapticManager hazır
- Theme system hazır

YENİDEN YARATILACAK/İYİLEŞTİRİLECEK:

1. Breathing Circle Animation sistemi:
   - Canvas API kullan (Android Canvas, iOS benzeri)
   - AnimatableCircle composable oluştur:
     * Center circle (inner) - scale animation ile büyür/küçülür
     * Outer ring (progress indicator) - sweep angle ile dolum
     * Color transitions: Inhale (calmBlue), Hold (mysticalViolet), Exhale (softPurple)
   - animateFloatAsState kullan scale için (animationSpec = tween(durationMillis = phase duration))
   - Smooth 60 FPS hedefle

2. Phase Indicator System:
   - AnimatedContent ile phase geçişleri
   - Text "Nefes Al", "Tut", "Nefes Ver", "Dinlen"
   - Icon değişimi (arrow up, pause, arrow down, rest)
   - Fade + slide animations
   - Typography: headlineLarge, bold

3. Timer Display:
   - Countdown timer (remaining time in phase)
   - Format: "00:05" (MM:SS)
   - Phase içinde kalan süre
   - animateIntAsState ile smooth counting

4. Exercise Selection Sheet:
   - ModalBottomSheet (Material 3)
   - List of exercises:
     * Box Breathing (4-4-4-4)
     * 4-7-8 Technique
     * Deep Breathing (4-2-8-2)
     * Alternate Nostril (premium)
   - Her exercise: title, description, duration, difficulty badge
   - Free vs Premium badges
   - Seçim yapınca sheet close, animation başlar

5. Control Buttons:
   - Start/Pause/Stop buttons
   - Animated button states (color, icon transitions)
   - Haptic feedback her tıklamada
   - FloatingActionButton style veya custom buttons
   - Position: bottom center, safe area aware

6. Session Summary Dialog:
   - Exercise bitince göster
   - Stats: cycles completed, total duration, completion rate
   - "Save to Health" (gelecek için placeholder)
   - "Share" button (future)
   - "Done" button → navigate back

7. Sound Integration:
   - Ambient sound picker (ModalBottomSheet)
   - Sound on/off toggle
   - Volume slider (0.0 - 1.0)
   - BreathingAudioManager ile integration
   - Sound fade in/out phase geçişlerinde

8. Haptic Feedback Patterns:
   - Phase başlangıçlarında vibration (HapticManager)
   - Inhale: light impact
   - Exhale: medium impact
   - Completion: success notification

ANİMASYON DETAYLARİ:
- animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f) for scale
- tween(durationMillis = 400, easing = FastOutSlowInEasing) for colors
- animateContentSize() for text transitions
- Targeting 60 FPS: avoid expensive operations in composition

LAYOUT:
- Scaffold with TopAppBar (title: exercise name, back button)
- Column layout:
  * Spacer(Modifier.weight(0.2f))
  * Breathing Circle (Modifier.weight(1f).aspectRatio(1f))
  * Phase indicator
  * Timer display
  * Spacer(Modifier.weight(0.2f))
  * Control buttons (fixed bottom)
- Background: ZenTheme gradient

ERROR HANDLING:
- Audio failure: show snackbar, continue without sound
- Permission denied: graceful degradation
- ViewModel errors: retry button

Tüm iyileştirmeleri yap, mevcut BreathingScreen.kt ve BreathingViewModel.kt'yi güncelle. Yeni composable'ları ayrı dosyalarda oluştur.
```

### Başarı Kriterleri
- [ ] Circle animations smooth (60 FPS)
- [ ] Phase transitions seamless
- [ ] Haptic feedback aktif
- [ ] Sound system entegre
- [ ] Session summary dialog
- [ ] Exercise selection sheet

---

## 🎯 Milestone 3: FocusTimerScreen Polish ve Notification (2 gün)

### Hedef
Mevcut Focus Timer'ı complete et - background timer, notifications, cycle management

### Prompt İçeriği

```
ZenFlow Android projesinde FocusTimerScreen'i tamamla ve production-ready hale getir.

MEVCUT DURUM:
- FocusScreen.kt var ama eksik
- PomodoroViewModel kısmen hazır (timer logic partial)
- FocusRepository, NotificationHelper hazır
- WorkManager configured

TÜM SİSTEMİ TAMAMLA:

1. PomodoroViewModel Complete:
   - Timer state machine: IDLE, RUNNING, PAUSED, COMPLETED
   - Session types: WORK (25 min), SHORT_BREAK (5 min), LONG_BREAK (15 min)
   - Cycle tracking: workSessionsUntilLongBreak (default 4)
   - Auto-transition logic: work → short break → work → ... → long break
   - Background timer: viewModelScope.launch + delay(1000) loop
   - Persistence: save timer state to DataStore (recovery after app kill)
   - Notifications: trigger at phase completions

2. FocusTimerScreen UI Complete:
   - Scaffold with TopAppBar ("Focus Timer")
   - Circular Progress Indicator (Canvas):
     * Outer ring: total session progress (sweepAngle)
     * Inner text: remaining time (MM:SS format)
     * Color by phase: work (blue), short break (green), long break (purple)
     * animateFloatAsState for smooth progress
   - Session Mode Indicator:
     * Text: "Focus", "Short Break", "Long Break"
     * Icon + text
     * Animated transition (AnimatedContent)
   - Cycle Counter:
     * "Cycle X/4" indicator
     * Progress dots (4 dots, filled based on completion)
   - Control Buttons:
     * Start/Pause/Stop
     * Skip button (next phase)
     * Settings (duration customization)
   - Stats Card:
     * Today's completed work sessions
     * Total focus time today
     * Current streak

3. Background Timer Implementation:
   - WorkManager OneTimeWorkRequest:
     * Worker class: PomodoroTimerWorker
     * Input: targetCompletionTime, sessionType
     * Work: check time, update notification, trigger completion
   - Notification updates every 10 seconds (ongoing notification)
   - Foreground service (Android 12+)
   - Timer recovery: check DataStore on app resume

4. Notification System:
   - Ongoing notification (timer running):
     * Title: "Focus Timer - 15:30 remaining"
     * Content: "Work Session"
     * Actions: Pause, Stop
     * Progress bar (determinate)
   - Completion notification:
     * Title: "Focus Session Complete!"
     * Content: "Time for a break" or "Break complete, back to work"
     * Sound + vibration
     * Action: Start next session
   - NotificationHelper extensions:
     * showTimerNotification(timeRemaining, sessionType)
     * updateTimerNotification(timeRemaining)
     * showCompletionNotification(sessionType)

5. Settings Dialog:
   - ModalBottomSheet veya AlertDialog
   - Duration sliders:
     * Work duration: 15-60 min (default 25)
     * Short break: 3-15 min (default 5)
     * Long break: 10-30 min (default 15)
   - Work sessions until long break: 2-6 (default 4)
   - Auto-start next session: toggle
   - Notification sound: toggle
   - Save to PreferencesRepository

6. Session History:
   - Save completed sessions to FocusRepository
   - Display today's sessions below timer
   - LazyColumn: session type, duration, timestamp
   - Stats: completion rate (completed vs interrupted)

BACKGROUND PERSISTENCE:
- DataStore keys:
  * timerState (idle/running/paused)
  * sessionType (work/short_break/long_break)
  * startTime (timestamp)
  * durationSeconds
  * currentCycle
- Recovery logic:
  * onResume: check DataStore
  * Calculate elapsed time
  * Resume timer if still valid

ANIMATIONS:
- Progress ring: smooth sweep angle (animateFloatAsState)
- Time display: animated integer counting
- Button states: color/icon transitions
- Phase completion: celebration animation (scale pulse)

ERROR HANDLING:
- Permission denied (notifications): show rationale dialog
- Background restrictions: prompt user to disable battery optimization
- WorkManager failure: fallback to in-app timer

Tüm kodları complete et, PomodoroViewModel ve FocusTimerScreen'i güncelle. PomodoroTimerWorker yeni oluştur.
```

### Başarı Kriterleri
- [ ] Timer background'da çalışır
- [ ] Notifications görünür ve actionable
- [ ] Auto-transition çalışır
- [ ] Session history kaydedilir
- [ ] Settings persisted
- [ ] App kill'den sonra recovery

---

## 🎯 Milestone 4: ZenGardenScreen Gamification Complete (2 gün)

### Hedef
Tree visualization, achievement system, statistics dashboard

### Prompt İçeriği

```
ZenFlow Android projesinde ZenGardenScreen'i iOS ZenGarden'a eşdeğer seviyede tamamla.

MEVCUT DURUM:
- ZenGardenScreen.kt skeleton var
- ZenGardenViewModel partial
- AchievementRepository, StatsRepository hazır
- AchievementEntity database'de

COMPLETE IMPLEMENTATION:

1. ZenGardenViewModel Complete:
   - UI state:
     * TreeLevel (1-5 based on total sessions)
     * CurrentStreak (days)
     * TotalSessions, TotalMinutes
     * Achievements (all, unlocked, locked)
     * WeeklyChart data, MonthlyChart data
     * SelectedTab (Tree, Achievements, Stats)
   - Functions:
     * calculateTreeLevel(totalSessions): Int
     * getCurrentStreak(sessions): Int
     * checkAchievements(stats): List<Achievement>
     * getWeeklyData(): List<DailyStats>
     * getMonthlyData(): List<MonthlyStats>
   - Flow collection: observe all repositories

2. Tree Visualization (Canvas):
   - TreeCanvas composable:
     * Draw tree trunk (Path API)
     * Draw branches (based on level 1-5)
     * Draw leaves (animated particles)
     * Growth animation (scale from previous level)
   - Levels:
     * Level 1: Small sapling (1-5 sessions)
     * Level 2: Young tree (6-15 sessions)
     * Level 3: Growing tree (16-30 sessions)
     * Level 4: Mature tree (31-60 sessions)
     * Level 5: Ancient tree (61+ sessions)
   - Color gradient: brown trunk, green leaves (alpha based on level)
   - Particle effect: floating leaves (animateFloatAsState, random positions)

3. Achievement System:
   - Achievement definitions (preloaded in database):
     * "First Step" - Complete 1 session
     * "Beginner" - Complete 5 sessions
     * "Committed" - Complete 10 sessions
     * "Dedicated" - 7 day streak
     * "Master" - 30 day streak
     * "Focus Champion" - 20 focus sessions
     * "Breathing Expert" - 50 breathing sessions
     * "Zen Master" - 100 total sessions
   - Achievement card UI:
     * Badge icon (locked/unlocked state)
     * Title, description
     * Progress bar (if in progress)
     * Unlock date (if unlocked)
   - LazyVerticalGrid (2 columns)
   - Unlock animation: scale + fade in

4. Statistics Dashboard:
   - Weekly Chart:
     * BarChart (androidx.compose.foundation.Canvas or external library)
     * 7 days, sessions per day
     * Interactive bars (tap to see detail)
   - Monthly Summary:
     * Total sessions this month
     * Total minutes this month
     * Average per day
     * Most active day
   - All-time Stats:
     * Total sessions, minutes
     * Current streak, longest streak
     * Most used exercise
     * Favorite time of day

5. Tab Navigation (internal):
   - TabRow with 3 tabs:
     * Tree (default)
     * Achievements
     * Statistics
   - AnimatedContent for tab content transitions

6. Tree Level-Up Animation:
   - Detect level change (LaunchedEffect + previousLevel comparison)
   - Show celebration:
     * Particle explosion (Canvas)
     * Success sound (if sound enabled)
     * Haptic feedback
     * "Level Up!" text overlay (AnimatedVisibility)
     * Fade out after 2 seconds

LAYOUT:
- TabRow at top
- Content area:
  * Tree tab: Tree canvas + stats summary card
  * Achievements tab: LazyVerticalGrid of achievement cards
  * Stats tab: Charts + summary cards
- Background: ZenTheme gradient

DATA FLOW:
- Real-time updates: collectAsStateWithLifecycle from repositories
- Cache expensive calculations (remember, derivedStateOf)
- Debounce updates (if too frequent)

ANIMATIONS:
- Tree growth: animateFloatAsState for scale
- Particles: continuous animation (infiniteRepeatable)
- Achievement unlock: spring animation
- Chart bars: animated height (animateFloatAsState)

Tüm kodları oluştur. TreeCanvas, AchievementCard, StatsChart composable'ları ayrı dosyalarda yarat. ZenGardenViewModel'i complete et.
```

### Başarı Kriterleri
- [ ] Tree visualization 5 level
- [ ] Achievements unlock otomatik
- [ ] Statistics accurate
- [ ] Charts interactive
- [ ] Level-up animation
- [ ] Tab switching smooth

---

## 🎯 Milestone 5: CalendarScreen ve Session History (1-2 gün)

### Hedef
Calendar view ile monthly session visualization

### Prompt İçeriği

```
ZenFlow Android projesinde CalendarScreen'i oluştur - session history ve calendar visualization.

MEVCUT DURUM:
- CalendarScreen.kt placeholder
- SessionRepository, FocusRepository, BreathingRepository hazır
- Date utilities gerekebilir

YARATILACAK SİSTEM:

1. CalendarViewModel:
   - UI state:
     * SelectedMonth (YearMonth)
     * DaySessionsMap (Map<LocalDate, List<Session>>)
     * SelectedDate (nullable)
     * SessionDetails (for selected date)
   - Functions:
     * loadMonth(yearMonth): suspend fun
     * selectDate(date): void
     * getSessionsForDate(date): List<Session>
     * getPreviousMonth(), getNextMonth()
   - Combine all repositories: meditation + focus + breathing

2. Calendar Grid UI:
   - Month Header:
     * "November 2024" format
     * Previous/Next month buttons
   - Weekday Headers:
     * Mon, Tue, Wed, Thu, Fri, Sat, Sun
   - Day Grid:
     * LazyVerticalGrid (7 columns)
     * Each day: DayCell composable
     * Current day: highlighted border
     * Days with sessions: dot indicator (color by session count)
     * Empty days: dimmed text
     * Selectable: clickable

3. DayCell Composable:
   - Box with:
     * Day number (Text)
     * Session indicator dots (Row of small circles)
     * Background color (selected vs not)
     * Border (current day)
   - State colors:
     * No sessions: transparent
     * 1-2 sessions: light green
     * 3-5 sessions: medium green
     * 6+ sessions: dark green
   - Animated selection (scale)

4. Session Details Panel:
   - Bottom sheet or expandable section
   - Shows when date selected
   - List of sessions for that day:
     * Session type icon
     * Title (exercise name or "Focus Session")
     * Duration
     * Time of day
   - Total stats for the day
   - "View in Zen Garden" link

5. Month Navigation:
   - Swipe left/right to change month (HorizontalPager)
   - Smooth transitions
   - Load data on demand (LaunchedEffect)
   - Cache 3 months (current, prev, next)

6. Empty State:
   - Show when no sessions in month
   - Motivational message
   - "Start your first session" button → navigate to Home

LAYOUT:
- Scaffold with TopAppBar ("Calendar")
- Column:
  * Month selector (Row with arrows)
  * Weekday headers
  * Calendar grid
  * Spacer or Divider
  * Session details (if date selected)

DATA LOADING:
- Efficient: only load selected month
- Cache in ViewModel
- Use Flow for real-time updates
- Handle date changes smoothly

ANIMATIONS:
- Month transition: slide animation
- Day selection: scale + color
- Session details expand: slideInVertically

EDGE CASES:
- Handle different month lengths (28-31 days)
- Week alignment (month start day)
- Today highlighting
- Future dates (grayed out, not selectable)

Tüm kodları oluştur. CalendarViewModel, CalendarScreen, DayCell composable'larını yarat.
```

### Başarı Kriterleri
- [ ] Calendar grid doğru render
- [ ] Session indicators görünür
- [ ] Month navigation çalışır
- [ ] Session details gösterilir
- [ ] Swipe gestures aktif
- [ ] Performance optimize

---

## 🎯 Milestone 6: SettingsScreen ve Preferences (1-2 gün)

### Hedef
Complete settings screen - user preferences, premium status, app info

### Prompt İçeriği

```
ZenFlow Android projesinde SettingsScreen'i iOS SettingsView seviyesinde oluştur.

MEVCUT DURUM:
- SettingsScreen eksik
- PreferencesRepository, PreferencesDataStore hazır
- StoreManager equivalent gerekli (billing için)

YARATILACAK SİSTEM:

1. SettingsViewModel:
   - UI state from PreferencesRepository:
     * Language (Turkish/English)
     * HapticFeedback (boolean)
     * Notifications (boolean)
     * DailyReminder (enabled, time)
     * SoundSettings (enabled, volume, backgroundMusic)
     * PremiumStatus (isPremium)
     * DarkMode (boolean)
   - Functions:
     * updateLanguage(lang)
     * updateHapticFeedback(enabled)
     * updateNotifications(enabled)
     * updateDailyReminder(enabled, time)
     * updateSoundSettings(...)
     * purchasePremium() (navigate to paywall)
   - Collect userPreferences flow

2. SettingsScreen Layout:
   - Scaffold with TopAppBar ("Settings")
   - LazyColumn with sections:

   SECTION 1: Premium
   - PremiumCard (if not premium):
     * "Upgrade to Premium" text
     * Features list (short)
     * "Unlock Now" button → navigate to paywall
     * Gradient background
   - PremiumStatusCard (if premium):
     * "Premium Active" text
     * Crown icon
     * "Restore Purchases" button

   SECTION 2: General
   - Language selector:
     * DropdownMenu or ModalBottomSheet
     * Turkish / English
   - Dark Mode toggle:
     * Switch
     * "Enable Dark Mode"

   SECTION 3: Notifications
   - Notifications toggle
   - Daily Reminder settings:
     * Enable toggle
     * Time picker (if enabled)
     * "Set your daily meditation reminder"

   SECTION 4: Sound & Haptics
   - Haptic Feedback toggle
   - Sound toggle
   - Volume slider (if sound enabled)
   - Background music toggle

   SECTION 5: About
   - App version (read from BuildConfig)
   - Privacy Policy (link)
   - Terms of Service (link)
   - Support email (link to email intent)
   - Rate app (link to Play Store)

3. UI Components:
   - SettingItem composable:
     * Icon, title, trailing content (Switch/Text/Icon)
     * Clickable or toggleable
     * Divider
   - SettingSection composable:
     * Section header (Text)
     * Content (Column)
     * Spacing

4. Time Picker Dialog:
   - TimePicker (Material 3)
   - Save/Cancel buttons
   - Update reminder time in preferences
   - Schedule notification (WorkManager)

5. Restore Purchases:
   - BillingClient check (future milestone)
   - For now: check PreferencesRepository
   - Show confirmation dialog

STYLING:
- Section headers: MaterialTheme.typography.titleMedium
- Items: MaterialTheme.typography.bodyLarge
- Icons: 24.dp, tinted primary
- Switches: MaterialTheme.colorScheme.primary
- Padding: 16.dp horizontal, 12.dp vertical

INTERACTIONS:
- Haptic feedback on toggle changes
- Confirmation dialogs for critical actions
- Snackbar for success messages
- Navigation to external links (Intent)

DATA PERSISTENCE:
- All changes auto-saved to DataStore
- Use rememberUpdatedState for current values
- collectAsStateWithLifecycle for preferences flow

Tüm kodları oluştur. SettingsViewModel, SettingsScreen, SettingItem, TimePicker integration.
```

### Başarı Kriterleri
- [ ] All settings visible
- [ ] Toggles work
- [ ] Language switching
- [ ] Notifications configurable
- [ ] Preferences persist
- [ ] Links functional

---

## 🎯 Milestone 7: Premium Billing System (2-3 gün)

### Hedef
Google Play Billing Library integration, paywall UI, purchase flow

### Prompt İçeriği

```
ZenFlow Android projesinde Google Play Billing System'i implement et - iOS StoreManager eşdeğeri.

HEDEF:
- Product ID: com.myzenflow.premium.lifetime
- Fiyat: $2.99 (veya ₺149 Turkey)
- Type: Non-consumable (one-time purchase)
- Features: Tüm breathing exercises, ambient sounds, advanced stats

YARATILACAK SİSTEM:

1. BillingManager (StoreManager equivalent):
   - Singleton class (Hilt @Singleton)
   - BillingClient setup ve lifecycle management
   - Product query (Product.ProductType.INAPP)
   - Purchase flow başlatma
   - Purchase verification
   - Purchase state tracking
   - Functions:
     * initialize(context): setup billing client
     * queryProducts(): get product details
     * purchase(activity): launch billing flow
     * restorePurchases(): check existing purchases
     * isPremium(): Boolean (from preferences)
     * premiumPrice: String (from product details or fallback)
   - State flows:
     * isPremiumFlow: StateFlow<Boolean>
     * purchaseStatusFlow: StateFlow<PurchaseStatus>
     * productDetailsFlow: StateFlow<ProductDetails?>

2. Billing Module (Hilt):
   - @Module @InstallIn(SingletonComponent::class)
   - Provide BillingManager
   - Inject PreferencesRepository

3. PaywallScreen:
   - Full-screen overlay (Dialog or separate screen)
   - Animated entrance (slideInVertically)
   - Close button (X top-right)
   - Content (similar to iOS PremiumPaywallView):
     * Hero section:
       - Crown icon (large, gradient)
       - "ZenFlow Premium" title
       - "Unlock all features" subtitle
     * Features list (LazyColumn):
       - "All Breathing Exercises" + description
       - "All Ambient Sounds" + description
       - "Advanced Statistics" + description
       - "Premium Themes" + description
       - Each with icon, checkmark, gradient card
     * Price section:
       - "One-time payment" label
       - Price (large, bold) from BillingManager
       - "Lifetime access" subtitle
     * Purchase button:
       - "Unlock Premium" text + crown icon
       - Gradient background
       - Loading state (if purchasing)
       - onClick: BillingManager.purchase(activity)
     * Restore button:
       - "Restore Purchases" text
       - Secondary style
       - onClick: BillingManager.restorePurchases()

4. Purchase Flow:
   - BillingManager.purchase():
     * Check billing client ready
     * Get product details
     * Launch billing flow (BillingFlowParams)
     * Handle result in onPurchasesUpdated()
   - onPurchasesUpdated():
     * Verify purchase (security)
     * Acknowledge purchase (BillingClient.acknowledgePurchase)
     * Update PreferencesRepository (isPremium = true)
     * Show success message (Snackbar)
     * Close paywall
   - Error handling:
     * Billing not available: show error dialog
     * Purchase canceled: no action
     * Purchase failed: show retry option

5. Restore Purchases:
   - Query existing purchases (BillingClient.queryPurchasesAsync)
   - Check for com.myzenflow.premium.lifetime
   - If found: update preferences, show success
   - If not found: show "No purchases found"

6. Premium Gates:
   - Extension function: isPremiumRequired(feature)
   - Check current premium status
   - If not premium: navigate to paywall
   - Apply to:
     * Advanced breathing exercises
     * All ambient sounds (keep 2 free)
     * Advanced statistics
     * Premium themes

7. Security:
   - Purchase verification (signature check)
   - Obfuscate premium status in preferences (encrypt)
   - Server-side verification (future, optional)

DEPENDENCIES (add to build.gradle):
```groovy
implementation("com.android.billingclient:billing-ktx:6.1.0")
```

PLAY CONSOLE SETUP (document in README):
1. Create in-app product:
   - Product ID: com.myzenflow.premium.lifetime
   - Type: Non-consumable
   - Price: Tier 3 ($2.99)
2. Add localized descriptions (TR/EN)
3. Activate product

TESTING:
- Use Play Console sandbox testing
- Create test account
- Test purchase flow
- Test restore flow
- Test premium gates

Tüm kodları oluştur. BillingManager, BillingModule, PaywallScreen, premium gate utilities.
```

### Başarı Kriterleri
- [ ] BillingClient initialized
- [ ] Product details fetched
- [ ] Purchase flow works
- [ ] Restore purchases works
- [ ] Premium status persisted
- [ ] Gates work correctly

---

## 🎯 Milestone 8: Localization System (1-2 gün)

### Hedef
Complete Turkish-English localization - strings.xml, language switcher, formatted dates

### Prompt İçeriği

```
ZenFlow Android projesinde complete localization system implement et - iOS Localizable.xcstrings eşdeğeri.

MEVCUT DURUM:
- strings.xml sadece app_name var
- PreferencesRepository language support var
- Tüm UI strings hardcoded

YARATILACAK SİSTEM:

1. String Resources:
   - res/values/strings.xml (English - default)
   - res/values-tr/strings.xml (Turkish)
   
   CATEGORIES:
   
   A) App & Navigation:
   - app_name
   - tab_home, tab_focus, tab_garden, tab_calendar, tab_profile
   - screen_breathing, screen_settings
   
   B) Home Screen:
   - greeting_morning, greeting_afternoon, greeting_evening
   - today_stats, weekly_streak
   - quick_action_breathing, quick_action_focus, quick_action_garden, quick_action_progress
   - recent_sessions, no_sessions_yet
   
   C) Breathing:
   - breathing_title
   - exercise_box, exercise_478, exercise_deep, exercise_alternate
   - phase_inhale, phase_hold, phase_exhale, phase_rest
   - session_complete, cycles_completed
   
   D) Focus Timer:
   - focus_title, work_session, short_break, long_break
   - timer_start, timer_pause, timer_stop, timer_skip
   - session_complete_focus, break_time, back_to_work
   
   E) Zen Garden:
   - garden_title, tree_level, current_streak
   - achievement_locked, achievement_unlocked
   - stats_weekly, stats_monthly, stats_alltime
   
   F) Settings:
   - settings_title
   - premium_upgrade, premium_active, restore_purchases
   - language_setting, dark_mode, notifications
   - haptic_feedback, sound_settings
   - about_version, privacy_policy, terms_service, support_email
   
   G) Premium:
   - premium_title, premium_subtitle
   - feature_all_exercises, feature_all_sounds, feature_advanced_stats, feature_premium_themes
   - price_onetime, price_lifetime
   - button_unlock, purchase_success, purchase_failed
   
   H) Common:
   - button_start, button_pause, button_stop, button_cancel, button_save
   - button_close, button_done, button_retry
   - loading, error_generic, success

2. String Usage Pattern:
   ```kotlin
   Text(text = stringResource(R.string.tab_home))
   ```

3. LocaleManager utility:
   - getCurrentLocale(context): Locale
   - setLocale(context, languageCode): void
   - getLocaleString(stringResId): String
   - Handle locale changes (recreate activities)

4. Language Switcher (in Settings):
   - Current language display
   - Dropdown or BottomSheet
   - Options: English, Türkçe
   - On change:
     * Update PreferencesRepository
     * Call LocaleManager.setLocale()
     * Recreate MainActivity (activity.recreate())

5. Date & Time Formatting:
   - DateTimeFormatter with locale
   - Extension functions:
     * LocalDate.format(pattern, locale)
     * LocalDateTime.format(pattern, locale)
   - Patterns:
     * "dd MMM yyyy" - 15 Nov 2024 / 15 Kas 2024
     * "HH:mm" - 14:30
     * "EEEE" - Monday / Pazartesi

6. Number Formatting:
   - DecimalFormat with locale
   - Extension functions:
     * Int.formatMinutes() - "25 minutes" / "25 dakika"
     * Float.formatDecimal(decimals) - locale-aware

7. Plurals (if needed):
   - res/values/plurals.xml
   - Example: session_count (one/other)

8. RTL Support (future):
   - android:supportsRtl="true"
   - Layout mirroring for Arabic (future locale)

IMPLEMENTATION STEPS:

Step 1: Create string resources
- Copy all hardcoded strings from UI
- Translate to Turkish
- Organize by category

Step 2: Replace hardcoded strings
- Search project for hardcoded Text("")
- Replace with stringResource(R.string.xxx)
- Update ViewModels to use resource IDs

Step 3: Implement LocaleManager
- Create LocaleManager.kt in utils/
- Implement locale change logic
- Handle configuration changes

Step 4: Update Settings
- Add language switcher
- Connect to PreferencesRepository
- Test locale change

Step 5: Format utilities
- Create FormatUtils.kt
- Date/time/number formatters
- Apply throughout app

QUALITY CHECK:
- All UI text localized
- No hardcoded strings remain
- Translations accurate and natural
- Date/time formats correct per locale
- Language switching smooth

iOS Parity:
- Match iOS Localizable.xcstrings keys
- Same string IDs for consistency
- Equivalent formatting functions

Tüm string resources'ları oluştur. LocaleManager, FormatUtils implement et. Tüm UI'da stringResource() kullan.
```

### Başarı Kriterleri
- [ ] Tüm strings localized
- [ ] Language switcher çalışır
- [ ] Date formatting locale-aware
- [ ] No hardcoded text
- [ ] Translations accurate
- [ ] App restart handles locale

---

## 🎯 Milestone 9: Profile Screen ve Onboarding (1-2 gün)

### Hedef
User profile, onboarding flow, first-time experience

### Prompt İçeriği

```
ZenFlow Android projesinde ProfileScreen ve Onboarding flow'u implement et.

YARATILACAK SİSTEM:

1. ProfileScreen:
   - User info section:
     * Avatar (placeholder icon or future photo)
     * Name (editable, from preferences)
     * Member since date
   - Stats summary:
     * Total sessions (all time)
     * Total minutes
     * Current streak
     * Longest streak
     * Favorite exercise
   - Quick links:
     * "My Achievements" → ZenGarden achievements tab
     * "Session History" → Calendar
     * "Settings" → Settings
   - Premium status card (if not premium):
     * "Unlock more features"
     * Link to paywall

2. ProfileViewModel:
   - UI state:
     * Username (from preferences)
     * AvatarUrl (future)
     * MemberSince (first session date or install date)
     * StatsModel (from StatsRepository)
   - Functions:
     * updateUsername(name)
     * getStatsModel()
     * getMemberSince()

3. Onboarding Flow:
   - 3-4 screens (HorizontalPager)
   - Screen 1: Welcome
     * App logo
     * "Welcome to ZenFlow"
     * "Your meditation companion"
   - Screen 2: Features
     * Icons + descriptions
     * Breathing exercises, Focus timer, Zen Garden
   - Screen 3: Personalization
     * "What's your name?" (optional)
     * Name input field
     * "Set your daily goal" slider (minutes per week)
   - Screen 4: Notifications
     * "Stay motivated"
     * "Enable daily reminders"
     * Permission request (POST_NOTIFICATIONS)
     * Time picker
   - Navigation:
     * "Next" button (each screen)
     * "Skip" button (optional)
     * "Get Started" button (final screen)
   - On complete:
     * Update PreferencesRepository (onboardingCompleted = true)
     * Navigate to Home
     * Show welcome dialog

4. Onboarding Check (MainActivity):
   - LaunchedEffect:
     * Check PreferencesRepository.onboardingCompleted
     * If false: navigate to onboarding
     * If true: stay on home

5. UI Design:
   - Onboarding: full-screen, gradient background
   - Page indicator dots (bottom)
   - Smooth transitions (HorizontalPager)
   - Animations: fade-in for content

6. Profile UI:
   - Scaffold with TopAppBar ("Profile")
   - LazyColumn:
     * Avatar + name section
     * Stats cards (Grid 2x2)
     * Quick links section
     * Premium card (conditional)
   - Edit name: Dialog or inline editing

LAYOUT SPECS:

Onboarding:
- Background: ZenTheme gradient
- Content: Centered Column
- Typography: headlineLarge for titles
- Spacing: 32.dp between sections
- Buttons: Full-width, rounded corners

Profile:
- Stats cards: 2x2 Grid
- Card elevation: 4.dp
- Card padding: 16.dp
- Section spacing: 24.dp

DATA FLOW:
- Profile data from repositories (real-time)
- Onboarding preferences persisted
- Name synced across app (greeting, etc.)

Tüm kodları oluştur. OnboardingScreen, ProfileScreen, ProfileViewModel, onboarding check logic.
```

### Başarı Kriterleri
- [ ] Onboarding shows on first launch
- [ ] Profile displays user stats
- [ ] Name editing works
- [ ] Quick links navigate
- [ ] Onboarding completion persisted
- [ ] Smooth animations

---

## 🎯 Milestone 10: Final Polish ve Testing (2-3 gün)

### Hedef
Performance optimization, bug fixes, testing, release preparation

### Prompt İçeriği

```
ZenFlow Android projesinde final polish, optimization, testing yap. Production-ready hale getir.

ÖNCELİKLİ GÖREVLER:

1. PERFORMANCE OPTIMIZATION:
   
   A) Memory:
   - LeakCanary integration (debug build)
   - Check for memory leaks:
     * ViewModels properly cleared
     * Flow collections canceled
     * Bitmap recycling (if any images)
   - Profile memory usage: target < 150MB
   
   B) Animations:
   - Profiler ile animation frame rate check
   - Target: consistent 60 FPS
   - Optimize heavy composables:
     * Use remember for expensive calculations
     * derivedStateOf for derived state
     * LaunchedEffect optimization
   - Reduce recompositions (Compose Layout Inspector)
   
   C) Database:
   - Add indices to frequently queried columns
   - Optimize DAO queries (EXPLAIN QUERY PLAN)
   - Pagination for large lists (PagingSource)
   
   D) Startup:
   - Profile app startup time
   - Target: < 2 seconds cold start
   - Lazy initialization where possible
   - Move heavy work to background

2. UI POLISH:
   
   A) Consistency:
   - Review all screens for design consistency
   - Spacing: use standard 8dp grid
   - Typography: consistent hierarchy
   - Colors: all from theme, no hardcoded
   
   B) Accessibility:
   - Content descriptions for all icons/buttons
   - Semantic properties for screen readers
   - Touch targets: minimum 48.dp
   - Color contrast: WCAG AA compliance
   - Test with TalkBack
   
   C) Animations:
   - Review all transitions
   - Add missing animations (if any)
   - Ensure smooth easing
   - No jank or stuttering
   
   D) Empty States:
   - All lists have empty state UI
   - Helpful messages
   - Action buttons where appropriate

3. ERROR HANDLING:
   
   A) Network (future-proof):
   - Error states for any future network calls
   - Retry mechanisms
   - Offline fallbacks
   
   B) Permissions:
   - Graceful degradation if denied
   - Rationale dialogs
   - Settings navigation
   
   C) Data:
   - Handle missing data
   - Corrupted database recovery
   - Migration failures
   
   D) User Feedback:
   - Snackbars for errors
   - Toast for quick feedback
   - Dialogs for critical errors

4. TESTING:
   
   A) Unit Tests:
   - ViewModels: test all functions
   - Repositories: test CRUD operations
   - Use cases: test business logic
   - Utilities: test formatters, helpers
   - Target: 70%+ coverage
   
   B) UI Tests (Compose):
   - Critical flows:
     * Breathing exercise end-to-end
     * Focus timer start/pause/stop
     * Purchase flow (mocked)
     * Settings changes
   - Test navigation
   - Test state changes
   
   C) Manual Testing:
   - Complete app walkthrough
   - Test all features
   - Test edge cases
   - Test different screen sizes
   - Test different Android versions (min SDK 24)

5. RELEASE PREPARATION:
   
   A) ProGuard/R8:
   - Configure proguard-rules.pro
   - Keep rules for:
     * Room entities
     * Hilt modules
     * Billing library
     * Any reflection usage
   - Test release build thoroughly
   
   B) Signing:
   - Generate release keystore
   - Configure signing in build.gradle
   - Secure keystore (don't commit)
   
   C) App Bundle:
   - Generate AAB (not APK)
   - Enable App Bundle in build.gradle
   - Test on device
   
   D) Version Management:
   - Update versionCode and versionName
   - Version 1.0.0 for first release
   - Prepare changelog

6. PLAY STORE ASSETS:
   
   A) Screenshots:
   - 5-8 screenshots per device type
   - Phone (required): 1080x1920 or similar
   - Tablet (optional): 1536x2048 or similar
   - Feature graphic: 1024x500
   
   B) Store Listing:
   - Turkish (primary):
     * Title: "ZenFlow - Meditasyon ve Odaklanma"
     * Short description (80 chars)
     * Full description (4000 chars)
     * Keywords
   - English (secondary):
     * Same structure
   
   C) Content Rating:
   - IARC questionnaire
   - Should be "Everyone"
   
   D) Privacy Policy:
   - Host on GitHub Pages
   - URL in Play Console
   - Mention offline-only, no tracking

7. FINAL CHECKS:
   
   - [ ] All strings localized
   - [ ] No hardcoded strings
   - [ ] All TODOs addressed or removed
   - [ ] No console errors
   - [ ] No crashes in critical flows
   - [ ] ProGuard rules correct
   - [ ] Release build tested
   - [ ] Battery usage acceptable
   - [ ] App size reasonable (< 50MB)
   - [ ] Permissions justified
   - [ ] Privacy policy uploaded

8. DOCUMENTATION:
   
   - README.md:
     * Project overview
     * Setup instructions
     * Build instructions
     * Testing instructions
   - Architecture.md:
     * Explain MVVM pattern
     * Dependency injection
     * Data flow
   - CHANGELOG.md:
     * Version 1.0.0 initial release

IMPLEMENTATION:

Step 1: Performance profiling
- Run Profiler on key screens
- Identify bottlenecks
- Optimize

Step 2: Testing
- Write unit tests
- Write UI tests
- Manual testing

Step 3: Release build
- Configure ProGuard
- Generate signed AAB
- Test release build

Step 4: Assets & listing
- Capture screenshots
- Write descriptions
- Prepare graphics

Step 5: Final review
- Complete checklist
- Fix remaining issues

Tüm optimizasyonları yap, testleri yaz, release configuration'ı hazırla. Checklist'i tamamla.
```

### Başarı Kriterleri
- [ ] No memory leaks
- [ ] 60 FPS animations
- [ ] < 2s startup
- [ ] Tests pass (70%+ coverage)
- [ ] Release APK builds
- [ ] Store assets ready
- [ ] Documentation complete
- [ ] All checklist items ✓

---

## 📝 Önemli Notlar

### iOS'tan Android Farkları (Tekrar Vurgu)
- **Haptic Feedback**: iOS CoreHaptics → Android Vibrator + VibrationEffect
- **Local Storage**: iOS UserDefaults → Android DataStore Preferences
- **IAP**: iOS StoreKit 2 → Android Google Play Billing Library
- **Notifications**: iOS UNUserNotificationCenter → Android NotificationManager + WorkManager
- **Navigation**: iOS NavigationView/TabView → Android Navigation Compose + BottomNavigation

### Platform-Specific Özellikler
- **Android Widgets**: Gelecek için home screen widget (breathing timer)
- **Android Shortcuts**: App shortcuts for quick actions
- **Android Wear**: Gelecek için WearOS companion app
- **iOS HealthKit**: Android'de karşılığı yok, tracking internal only

### Market Strategy
- Turkish market primary, English secondary
- Premium pricing: ₺149 (iOS ile aynı değer)
- ASO: "meditasyon", "nefes egzersizi", "odaklanma", "mindfulness"
- No ads, no subscriptions, one-time lifetime purchase

### Teknik Debt Prevention
- Code review before each milestone completion
- Refactor as you go
- Document complex logic
- Keep ViewModels testable
- Separate UI from business logic

Bu plan ile Android ZenFlow uygulaması iOS versiyonu ile feature parity'e ulaşacak ve yüksek kalitede bir product olacak. Her prompt Claude Code'a direkt copy-paste edilebilir ve actionable. Başarılar! 🚀