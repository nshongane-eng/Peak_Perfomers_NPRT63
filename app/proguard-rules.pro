# Proguard rules for Smart Load Shedding

# Keep Room database & entities
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# Keep Gson serialization and models
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.peakperformers.smartloadshedding.models.** { *; }
-keepclassmembers class com.peakperformers.smartloadshedding.models.** { *; }
-keep class com.peakperformers.smartloadshedding.database.** { *; }
-keepclassmembers class com.peakperformers.smartloadshedding.database.** { *; }

# Retrofit & OkHttp
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn javax.annotation.**
