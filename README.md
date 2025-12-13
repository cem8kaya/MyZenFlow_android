# ZenFlow - Meditasyon ve Odaklanma

Modern Android uygulaması - Jetpack Compose ile geliştirilmiş meditasyon, nefes egzersizleri ve Pomodoro odaklanma timer uygulaması.

## 📱 Özellikler

### ✨ Ana Özellikler
- **Meditasyon Seansları**: Rehberli ve serbest meditasyon seansları
- **Nefes Egzersizleri**: Box Breathing, 4-7-8 Tekniği, Wim Hof yöntemi ve daha fazlası
- **Pomodoro Timer**: Üretkenlik için odaklanma/mola döngüleri
- **Zen Bahçesi**: İlerlemenizi görselleştiren interaktif ağaç animasyonu
- **İstatistikler**: Detaylı kullanım istatistikleri ve grafikler
- **Başarımlar**: İlerlemenizi takip edin ve rozetler kazanın
- **Karanlık Mod**: Göz dostu karanlık tema desteği
- **Çoklu Dil**: Türkçe ve İngilizce desteği

### 🎯 Teknik Özellikler
- **100% Kotlin**: Modern Kotlin kodu
- **Jetpack Compose**: Tamamen Compose UI
- **MVVM Mimari**: Clean architecture pattern
- **Room Database**: Yerel veri saklama
- **Hilt**: Dependency injection
- **Coroutines & Flow**: Reactive programlama
- **Material Design 3**: Modern UI/UX
- **Offline First**: İnternet bağlantısı gerektirmez

## 🏗️ Mimari

### MVVM Pattern
```
presentation/
├── screens/          # UI Screens (Compose)
├── components/       # Reusable UI Components
├── viewmodels/       # ViewModels (State Management)
└── theme/           # Theme & Styling

data/
├── entities/        # Room Database Entities
├── dao/            # Database Access Objects
├── repository/     # Data Repositories
├── models/         # Domain Models
└── datastore/      # DataStore (Preferences)

domain/
├── services/       # Foreground Services
├── workers/        # Background Workers
└── receivers/      # Broadcast Receivers

di/                 # Dependency Injection (Hilt)
```

### Veri Akışı
```
UI (Compose) → ViewModel → Repository → Data Source (Room/DataStore)
     ↑                           ↓
     └── StateFlow/Flow ←────────┘
```

## 🚀 Kurulum

### Gereksinimler
- Android Studio Hedgehog | 2023.1.1 veya üzeri
- JDK 17
- Android SDK 24+ (minimum)
- Android SDK 34 (target)

### Proje Kurulumu

1. **Repository'yi klonlayın**
```bash
git clone https://github.com/cem8kaya/MyZenFlow_android.git
cd MyZenFlow_android
```

2. **Android Studio'da açın**
   - File → Open → Proje klasörünü seçin

3. **Gradle Sync**
   - Android Studio otomatik olarak dependencies indirecektir
   - Veya: File → Sync Project with Gradle Files

4. **Uygulamayı çalıştırın**
   - Emülatör veya fiziksel cihaz seçin
   - Run → Run 'app' veya Shift+F10

## 🔧 Build Varyantları

### Debug Build
```bash
./gradlew assembleDebug
```
- LeakCanary aktif
- Debug logging açık
- ProGuard kapalı
- Application ID: `com.oqza.myzenflow.debug`

### Release Build
```bash
./gradlew assembleRelease
```
- ProGuard/R8 aktif
- Resource shrinking açık
- Logging kapalı
- Optimizasyon maksimum

### Release AAB (App Bundle)
```bash
./gradlew bundleRelease
```
- Google Play Store için optimize edilmiş

## 🧪 Test

### Unit Tests
```bash
./gradlew test
```
- ViewModel testleri
- Repository testleri
- Utility testleri

### Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```
- UI testleri (Compose)
- Database testleri
- Integration testleri

### Test Coverage
```bash
./gradlew testDebugUnitTestCoverage
```

## 📦 Dependencies

### Core
- Kotlin 1.9+
- Jetpack Compose BOM 2024.06.00
- Material 3
- Coroutines 1.7.3

### Jetpack
- Room 2.6.1 (Database)
- Hilt 2.51.1 (DI)
- Navigation Compose 2.8.0
- DataStore 1.1.1
- WorkManager 2.9.0
- Lifecycle 2.8.0

### Testing
- JUnit 4
- Mockk 1.13.8
- Turbine 1.0.0 (Flow testing)
- Compose UI Test

### Debug
- LeakCanary 2.14 (Memory leak detection)

## 🎨 Tema & Tasarım

### Material Design 3
- Dynamic Color desteği
- Karanlık/Açık tema
- Custom color schemes
- Typography system
- Shape system

### Animasyonlar
- Smooth transitions
- Particle effects
- Tree growth animation
- Breathing circle animation

## 🔐 Güvenlik & Privacy

### Veri Saklama
- **Tamamen Offline**: Hiçbir veri sunucuya gönderilmez
- **Local Storage**: Tüm veriler cihazda Room Database'de saklanır
- **No Tracking**: Kullanıcı takibi yok
- **No Ads**: Reklam yok
- **No Analytics**: Analitik yok

### Permissions
- `POST_NOTIFICATIONS`: Timer ve hatırlatıcı bildirimleri için
- `FOREGROUND_SERVICE`: Arka planda timer çalışması için
- `SCHEDULE_EXACT_ALARM`: Hassas zamanlayıcılar için
- `VIBRATE`: Haptic feedback için

## 🌍 Localization

### Desteklenen Diller
- 🇹🇷 Türkçe (Varsayılan)
- 🇬🇧 English

### Yeni Dil Ekleme
1. `res/values-{locale}/strings.xml` oluşturun
2. Tüm string resource'ları çevirin
3. `LocaleManager.kt`'de locale ekleyin

## 📱 Minimum Gereksinimler

- **Android Version**: 7.0 (API 24) ve üzeri
- **RAM**: 2 GB önerilir
- **Storage**: ~50 MB

## 🐛 Sorun Giderme

### Build Hataları
```bash
# Clean build
./gradlew clean

# Invalidate caches
# Android Studio → File → Invalidate Caches / Restart
```

### Database Migration Hataları
```bash
# Debug build'de database sıfırlanır (fallbackToDestructiveMigration)
# Release build'de migration gereklidir
```

### ProGuard Hataları
- `proguard-rules.pro` dosyasını kontrol edin
- Keep rules ekleyin gerekirse

## 🤝 Katkıda Bulunma

1. Fork edin
2. Feature branch oluşturun (`git checkout -b feature/AmazingFeature`)
3. Commit edin (`git commit -m 'Add some AmazingFeature'`)
4. Push edin (`git push origin feature/AmazingFeature`)
5. Pull Request açın

### Code Style
- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)

## 📄 License

Bu proje MIT lisansı altında lisanslanmıştır. Detaylar için `LICENSE` dosyasına bakın.

## 👤 Author

**Cem Kaya**
- GitHub: [@cem8kaya](https://github.com/cem8kaya)

## 🙏 Teşekkürler

- Jetpack Compose Team
- Material Design Team
- Android Developer Community

## 📚 Kaynaklar

- [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)

---

**Versiyon**: 1.0.0
**Son Güncelleme**: Aralık 2024
