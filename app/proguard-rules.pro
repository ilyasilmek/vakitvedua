# ProGuard rules

# --- Room: veritabani siniflari yansima (reflection) ile olusturulur ---
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class * implements androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.**

# --- WorkManager: acilista InitializationProvider ile baslatiliyor ---
-keep class androidx.work.impl.** { *; }
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# --- androidx.startup saglayicisi ---
-keep class androidx.startup.** { *; }
-dontwarn androidx.startup.**

# --- Reklam/etkinlik kayitlari manifest yoluyla bulundugundan ---
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

