# MyZenFlow ProGuard Rules
# Production-ready configuration for R8/ProGuard

# ==================== General ====================
# Keep line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotations
-keepattributes *Annotation*

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# ==================== Kotlin ====================
# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# Kotlin metadata
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }
-dontwarn kotlin.reflect.**

# ==================== Room Database ====================
# Keep all Room entities
-keep @androidx.room.Entity class * { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Database class * { *; }

# Keep DAO methods
-keep @androidx.room.Dao class * { *; }

# Keep TypeConverters
-keep @androidx.room.TypeConverter class * { *; }
-keep class * {
    @androidx.room.TypeConverter <methods>;
}

# Keep entity field names (for queries)
-keepclassmembers class com.oqza.myzenflow.data.entities.** {
    <fields>;
}

# Room migration
-keep class androidx.room.migration.Migration { *; }

# ==================== Hilt / Dagger ====================
# Keep Hilt annotations and generated code
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep class dagger.hilt.** { *; }
-dontwarn dagger.hilt.**

# Keep Hilt modules
-keep @dagger.Module class *
-keep @dagger.hilt.InstallIn class *
-keep @dagger.hilt.components.SingletonComponent class *

# Keep Hilt entry points
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# Keep Hilt generated classes
-keep class **_HiltComponents { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }
-keep class **_HiltModules { *; }

# ==================== Jetpack Compose ====================
# Keep composable functions
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-dontwarn androidx.compose.**

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class androidx.lifecycle.** { *; }

# ==================== Data Classes & Models ====================
# Keep all data models and their fields
-keep class com.oqza.myzenflow.data.models.** { *; }
-keep class com.oqza.myzenflow.data.entities.** { *; }

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ==================== Serialization ====================
# Keep Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Keep Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ==================== WorkManager ====================
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class androidx.work.** { *; }

# ==================== Services & Receivers ====================
# Keep services
-keep class com.oqza.myzenflow.domain.services.** { *; }

# Keep broadcast receivers
-keep class com.oqza.myzenflow.domain.receivers.** { *; }

# ==================== Optimization Settings ====================
# Enable aggressive optimization
-optimizationpasses 5
-dontusemixedcaseclassnames
-verbose

# Allow optimization
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# ==================== Warnings to Ignore ====================
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# ==================== Testing & Debug ====================
# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ==================== Crashlytics (Future) ====================
# Uncomment when adding Crashlytics
#-keepattributes SourceFile,LineNumberTable
#-keep public class * extends java.lang.Exception
