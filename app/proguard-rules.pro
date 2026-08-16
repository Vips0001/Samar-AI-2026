# ===================================================================
# Samar AI - Comprehensive ProGuard & R8 Optimization Rules
# ===================================================================

# -------------------------------------------------------------------
# General & Debugging Attributes
# -------------------------------------------------------------------
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions
-renamesourcefileattribute SourceFile

# -------------------------------------------------------------------
# Android Architecture & Jetpack
# -------------------------------------------------------------------
# Keep ViewModel constructors for reflection instantiation
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel {
    <init>(...);
}

# -------------------------------------------------------------------
# Room Database
# -------------------------------------------------------------------
# Keep Room database, DAOs, and Entity classes
-keep @androidx.room.Database class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Entity class * { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomDatabase$Callback
-keep class * extends androidx.room.migration.Migration
-keep class *_Impl { *; }

# Keep Room entity fields and constructors
-keepclassmembers @androidx.room.Entity class * {
    private <fields>;
    public <fields>;
    <init>(...);
}

# -------------------------------------------------------------------
# Moshi JSON Serialization & Reflection
# -------------------------------------------------------------------
# Keep Moshi annotations
-keepattributes *Annotation*
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}

# Keep all generated Moshi adapters
-keep class *JsonAdapter {
    <init>(...);
    public <fields>;
    public <methods>;
}

# Keep Moshi core internals
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# -------------------------------------------------------------------
# Application Data Models & Entities
# -------------------------------------------------------------------
-keep class com.example.data.remote.** { *; }
-keep class com.example.data.local.entity.** { *; }
-keepclassmembers class com.example.data.remote.** { *; }
-keepclassmembers class com.example.data.local.entity.** { *; }

# -------------------------------------------------------------------
# Retrofit & OkHttp
# -------------------------------------------------------------------
# Retrofit does reflection on interface methods and annotations
-keepattributes Exceptions, InnerClasses, Signature, *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# -------------------------------------------------------------------
# Kotlin Coroutines
# -------------------------------------------------------------------
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# -------------------------------------------------------------------
# Coil Image Loading
# -------------------------------------------------------------------
-keep class coil.** { *; }
-keep interface coil.** { *; }
-dontwarn coil.**

