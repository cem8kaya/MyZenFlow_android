# ZenFlow Release Guide

Bu guide, ZenFlow uygulamasının production release sürecini adım adım açıklar.

## 📦 Release Hazırlık Süreci

### 1. Keystore Oluşturma

#### Yeni Keystore Oluştur
```bash
keytool -genkey -v -keystore myzenflow-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias myzenflow
```

**Bilgileri Kaydet:**
- Keystore password: [GÜVENLE SAKLAYIN]
- Key alias: myzenflow
- Key password: [GÜVENLE SAKLAYIN]

**⚠️ ÖNEMLİ:**
- Keystore dosyasını asla git'e commit etmeyin
- Şifreleri güvenli bir şifre yöneticisinde saklayın
- Yedek alın (Google Drive, 1Password, vb.)

#### Keystore Dosyası Konumu
```
/path/to/secure/location/myzenflow-release.jks
```

### 2. Gradle Properties Yapılandırması

#### `~/.gradle/gradle.properties` (Global - Önerilen)
```properties
MYZENFLOW_RELEASE_STORE_FILE=/path/to/myzenflow-release.jks
MYZENFLOW_RELEASE_STORE_PASSWORD=your_keystore_password
MYZENFLOW_RELEASE_KEY_ALIAS=myzenflow
MYZENFLOW_RELEASE_KEY_PASSWORD=your_key_password
```

**Veya**

#### `local.properties` (Project - Git ignored)
```properties
storeFile=/path/to/myzenflow-release.jks
storePassword=your_keystore_password
keyAlias=myzenflow
keyPassword=your_key_password
```

### 3. Build Gradle Signing Config

`app/build.gradle.kts`'ye ekleyin:

```kotlin
android {
    signingConfigs {
        create("release") {
            // Option 1: From gradle.properties
            storeFile = file(properties["MYZENFLOW_RELEASE_STORE_FILE"] as String? ?: "")
            storePassword = properties["MYZENFLOW_RELEASE_STORE_PASSWORD"] as String?
            keyAlias = properties["MYZENFLOW_RELEASE_KEY_ALIAS"] as String?
            keyPassword = properties["MYZENFLOW_RELEASE_KEY_PASSWORD"] as String?

            // Option 2: From environment variables
            // storeFile = file(System.getenv("MYZENFLOW_RELEASE_STORE_FILE") ?: "")
            // storePassword = System.getenv("MYZENFLOW_RELEASE_STORE_PASSWORD")
            // keyAlias = System.getenv("MYZENFLOW_RELEASE_KEY_ALIAS")
            // keyPassword = System.getenv("MYZENFLOW_RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### 4. Version Yönetimi

#### Semantic Versioning
```
MAJOR.MINOR.PATCH
1.0.0
│ │ │
│ │ └─ Patch: Bug fixes
│ └─── Minor: New features (backwards compatible)
└───── Major: Breaking changes
```

#### `app/build.gradle.kts` Güncelleme
```kotlin
android {
    defaultConfig {
        versionCode = 1        // Her release'de artır (1, 2, 3...)
        versionName = "1.0.0"  // Semantic version
    }
}
```

**Version Code Artırma Kuralı:**
- Her production release'de +1
- Her beta/alpha release'de +1
- Internal test release'lerinde değişmeyebilir

### 5. Pre-Release Checklist

#### Code Quality
- [ ] Tüm TODO'lar giderildi veya issue açıldı
- [ ] No hardcoded strings (hepsi `strings.xml`'de)
- [ ] No debug `Log` statements
- [ ] No `println()` statements
- [ ] Code formatted (Ctrl+Alt+L)
- [ ] Lint warnings gözden geçirildi
- [ ] ProGuard rules test edildi

#### Testing
- [ ] Unit tests çalıştırıldı ve passed
- [ ] UI tests çalıştırıldı ve passed
- [ ] Manuel testing tamamlandı
- [ ] Farklı cihazlarda test edildi (en az 3 farklı)
- [ ] Farklı Android versiyonlarında test edildi (API 24, 28, 33, 34)
- [ ] Edge case'ler test edildi
- [ ] Offline mode test edildi
- [ ] Battery drain test edildi

#### Performance
- [ ] Memory leak yok (LeakCanary)
- [ ] ANR yok (Application Not Responding)
- [ ] Startup time < 2 saniye
- [ ] Smooth animations (60 FPS)
- [ ] No jank or lag
- [ ] Battery usage kabul edilebilir seviyede

#### Build
- [ ] Release build başarıyla oluştu
- [ ] APK boyutu < 50MB
- [ ] ProGuard/R8 çalışıyor
- [ ] Resources shrinking çalışıyor
- [ ] No build warnings
- [ ] Signing configuration doğru

#### Documentation
- [ ] README.md güncel
- [ ] CHANGELOG.md güncel
- [ ] Version bumped
- [ ] Screenshots güncel
- [ ] Store listing hazır

### 6. Release Build Oluşturma

#### Clean Build
```bash
./gradlew clean
```

#### Build Release APK
```bash
./gradlew assembleRelease
```

**Output:**
```
app/build/outputs/apk/release/app-release.apk
```

#### Build Release AAB (App Bundle - Önerilen)
```bash
./gradlew bundleRelease
```

**Output:**
```
app/build/outputs/bundle/release/app-release.aab
```

**AAB Avantajları:**
- Daha küçük indirme boyutu (cihaza özel APK)
- Play Store tarafından optimize edilir
- Dynamic feature modules desteği

### 7. Release Build Test

#### Local Test
```bash
# APK install
adb install app/build/outputs/apk/release/app-release.apk

# AAB'den APK oluştur ve test et (bundletool gerekli)
bundletool build-apks --bundle=app-release.aab --output=app.apks
bundletool install-apks --apks=app.apks
```

#### Test Checklist
- [ ] Uygulama açılıyor
- [ ] Tüm özellikler çalışıyor
- [ ] No crashes
- [ ] ProGuard ile obfuscation doğru çalışıyor
- [ ] Network (varsa) çalışıyor
- [ ] Permissions doğru çalışıyor
- [ ] Billing (varsa) çalışıyor

### 8. Play Console Hazırlık

#### Store Listing

**Kısa Açıklama (80 karakter):**
```
Meditasyon, nefes egzersizleri ve Pomodoro timer ile huzurlu ve üretken bir yaşam
```

**Uzun Açıklama (4000 karakter):**
```markdown
ZenFlow ile içsel dengenizi bulun ve odaklanmanızı artırın! 🧘‍♀️

✨ ÖZELLİKLER:

🧘 Meditasyon
• Serbest meditasyon seansları
• Zamanlayıcı ile rehberli meditasyon
• Ruh hali takibi
• Seans notları

🌬️ Nefes Egzersizleri
• Box Breathing
• 4-7-8 Tekniği
• Wim Hof Yöntemi
• Görsel ve işitsel geri bildirim
• Haptic feedback

⏱️ Pomodoro Timer
• Özelleştirilebilir çalışma/mola süreleri
• Döngü takibi
• Bildirimler
• İstatistik kaydı

🌳 Zen Bahçesi
• İnteraktif ağaç büyütme
• İlerleme görselleştirmesi
• Motivasyon

📊 İstatistikler
• Detaylı analiz
• Grafik görselleştirmeleri
• Streak takibi

🏆 Başarımlar
• 20+ farklı başarım
• İlerleme takibi
• Rozet sistemi

🔒 GİZLİLİK & GÜVENLİK:
✓ Tamamen offline
✓ Veri toplanmaz
✓ Reklam yok
✓ Kullanıcı takibi yok

ZenFlow ile her gün biraz daha huzurlu! 🌸
```

#### Screenshots

**Gereksinimler:**
- **Phone:** 16:9 or 9:16, min 320px
- **Tablet:** 16:9 or 9:16, min 1024px
- **Format:** PNG or JPEG (24-bit, no alpha)
- **Count:** Min 2, max 8 per device type

**Screenshot Locations:**
```
screenshots/
├── phone/
│   ├── 01-home.png
│   ├── 02-breathing.png
│   ├── 03-focus-timer.png
│   ├── 04-zen-garden.png
│   └── 05-stats.png
└── tablet/
    └── [same structure]
```

#### Feature Graphic
- **Size:** 1024 x 500
- **Format:** PNG or JPEG (24-bit, no alpha)
- **Content:** App branding, no text overlays

#### App Icon
- **Size:** 512 x 512
- **Format:** PNG (32-bit, alpha)
- **Already in:** `app/src/main/ic_launcher-playstore.png`

#### Content Rating
- **IARC Questionnaire**
- Expected rating: **Everyone**
- No violence, no adult content, no gambling

#### Privacy Policy
- **Required:** Yes (we store data locally)
- **URL:** Host on GitHub Pages or website

**Example Privacy Policy:**
```markdown
# Privacy Policy for ZenFlow

Last updated: December 13, 2024

## Data Collection
ZenFlow does NOT collect any personal data. All your meditation sessions,
breathing exercises, and statistics are stored locally on your device only.

## Data Storage
- All data is stored in local Room database
- No cloud sync (in current version)
- No analytics or tracking
- No third-party data sharing

## Permissions
- **Notifications:** For timer alerts and reminders
- **Foreground Service:** For background timer operation
- **Vibrate:** For haptic feedback during exercises

## Contact
For questions: your-email@example.com
```

### 9. Google Play Release

#### Internal Testing Track
1. Upload AAB to Internal testing
2. Invite test users (email list)
3. Test for 1-2 days
4. Gather feedback

#### Closed Beta Track
1. Promote from Internal to Closed Beta
2. Expand test user group
3. Test for 1-2 weeks
4. Fix critical bugs

#### Open Beta Track (Optional)
1. Promote to Open Beta
2. Public testing
3. Gather user feedback
4. Final polish

#### Production Track
1. Promote to Production
2. **Phased rollout:**
   - 10% → 25% → 50% → 100%
3. Monitor crash reports
4. Monitor reviews
5. Quick hotfix if needed

### 10. Post-Release

#### Monitoring
- [ ] Crash-free rate > 99.5%
- [ ] ANR rate < 0.5%
- [ ] User reviews (respond within 24h)
- [ ] Star rating > 4.0
- [ ] Vitals (Google Play Console)

#### Hotfix Process
```bash
# Bump version
versionCode = 2
versionName = "1.0.1"

# Fix bug
# Test
# Build release
./gradlew bundleRelease

# Upload to Play Console
# Production → Create new release
```

## 🔄 CI/CD (Future)

### GitHub Actions Example
```yaml
name: Release Build

on:
  push:
    tags:
      - 'v*'

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Build Release AAB
        run: ./gradlew bundleRelease
      - name: Sign AAB
        uses: r0adkll/sign-android-release@v1
        with:
          releaseDirectory: app/build/outputs/bundle/release
          signingKeyBase64: ${{ secrets.SIGNING_KEY }}
          alias: ${{ secrets.ALIAS }}
          keyStorePassword: ${{ secrets.KEY_STORE_PASSWORD }}
          keyPassword: ${{ secrets.KEY_PASSWORD }}
```

## 📝 Release Notes Template

```markdown
## Version 1.0.0 - İlk Sürüm

### 🎉 Yenilikler
- Meditasyon seansları
- Nefes egzersizleri
- Pomodoro timer
- Zen bahçesi
- İstatistikler ve başarımlar

### 🐛 Düzeltmeler
- Yok (ilk sürüm)

### ⚡ İyileştirmeler
- Uygulama performansı optimize edildi
- Animasyonlar düzleştirildi
- Kullanıcı arayüzü iyileştirildi

### 🔒 Güvenlik
- Veri tamamen cihazda saklanıyor
- Kullanıcı gizliliği korunuyor
```

## 🆘 Troubleshooting

### Keystore Hatası
```
Execution failed for task ':app:validateSigningRelease'
```
**Çözüm:** Keystore yolu ve şifreleri kontrol edin.

### ProGuard Hatası
```
ClassNotFoundException at runtime
```
**Çözüm:** `proguard-rules.pro`'da keep rules ekleyin.

### APK Boyutu Çok Büyük
```
APK size > 100MB
```
**Çözüm:**
- Resource shrinking açık mı?
- Unused resources temizlendi mi?
- AAB kullanın (APK yerine)

---

**Last Updated**: December 2024
**Version**: 1.0.0
