import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.oqza.myzenflow"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.oqza.myzenflow"
        minSdk = 24
        targetSdk = 36
        versionCode = (project.findProperty("versionCode") as String?)?.toIntOrNull() ?: 1
        versionName = (project.findProperty("versionName") as String?) ?: "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // Release signing is read from keystore.properties (git-ignored) or CI env vars.
    // Without them the release build stays unsigned; Play App Signing re-signs uploads.
    val keystoreProps = Properties().apply {
        val f = rootProject.file("keystore.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }
    fun signingValue(key: String, env: String): String? =
        keystoreProps.getProperty(key) ?: System.getenv(env)

    signingConfigs {
        val storePath = signingValue("storeFile", "ZENFLOW_KEYSTORE_FILE")
        if (storePath != null) {
            create("release") {
                storeFile = rootProject.file(storePath)
                storePassword = signingValue("storePassword", "ZENFLOW_KEYSTORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "ZENFLOW_KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "ZENFLOW_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfigs.findByName("release")?.let { signingConfig = it }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        // ./gradlew assembleRelease -PcomposeMetrics  ->  app/build/compose_metrics (stability / skippability reports)
        if (project.hasProperty("composeMetrics")) {
            val out = layout.buildDirectory.dir("compose_metrics").get().asFile.absolutePath
            freeCompilerArgs += listOf(
                "-P", "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=$out",
                "-P", "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=$out"
            )
        }
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    // Installs the baseline profile (src/main/baseline-prof.txt) for faster startup and scrolling
    implementation("androidx.profileinstaller:profileinstaller:1.3.1")

    // Jetpack Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.0.0")
    ksp("androidx.hilt:hilt-compiler:1.0.0")

    // Accompanist (for permissions and other utilities)
    val accompanistVersion = "0.32.0"
    implementation("com.google.accompanist:accompanist-permissions:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanistVersion")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ViewModel Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // LeakCanary for memory leak detection (debug only)
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")

    // Testing dependencies
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("io.mockk:mockk:1.13.8")
    androidTestImplementation("io.mockk:mockk-android:1.13.8")
}
