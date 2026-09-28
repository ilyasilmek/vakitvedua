package com.stitchilyas.vakitvedua.data.local

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("vakitvedua_prefs", Context.MODE_PRIVATE)

    init {
        // Force language preference to Turkish
        if (prefs.getString("app_language", "tr") != "tr") {
            prefs.edit().putString("app_language", "tr").apply()
        }
    }

    // Location
    var cityName: String
        get() = prefs.getString("city_name", "İstanbul") ?: "İstanbul"
        set(value) = prefs.edit().putString("city_name", value).apply()

    var districtName: String
        get() = prefs.getString("district_name", "") ?: ""
        set(value) = prefs.edit().putString("district_name", value).apply()

    var latitude: Double
        get() = java.lang.Double.longBitsToDouble(
            prefs.getLong("latitude", java.lang.Double.doubleToRawLongBits(41.0082))
        )
        set(value) = prefs.edit().putLong("latitude", java.lang.Double.doubleToRawLongBits(value)).apply()

    var longitude: Double
        get() = java.lang.Double.longBitsToDouble(
            prefs.getLong("longitude", java.lang.Double.doubleToRawLongBits(28.9784))
        )
        set(value) = prefs.edit().putLong("longitude", java.lang.Double.doubleToRawLongBits(value)).apply()

    // Theme (0: System, 1: Light, 2: Dark)
    var themeMode: Int
        get() = prefs.getInt("theme_mode", 0)
        set(value) = prefs.edit().putInt("theme_mode", value).apply()

    // Notifications
    var ezanSoundEnabled: Boolean
        get() = prefs.getBoolean("ezan_sound_enabled", true)
        set(value) = prefs.edit().putBoolean("ezan_sound_enabled", value).apply()

    var reminderMinutesBefore: Int
        get() = prefs.getInt("reminder_minutes_before", 15)
        set(value) = prefs.edit().putInt("reminder_minutes_before", value).apply()

    var kerahatWarningEnabled: Boolean
        get() = prefs.getBoolean("kerahat_warning_enabled", true)
        set(value) = prefs.edit().putBoolean("kerahat_warning_enabled", value).apply()

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean("vibration_enabled", true)
        set(value) = prefs.edit().putBoolean("vibration_enabled", value).apply()

    // Kaza Counters
    fun getKazaCount(key: String): Int = prefs.getInt("kaza_$key", 0)

    fun setKazaCount(key: String, count: Int) {
        prefs.edit().putInt("kaza_$key", count.coerceAtLeast(0)).apply()
    }

    // Weekly prayer tracker (Date string "yyyy-MM-dd" -> Set of completed prayers e.g. "Sabah", "Öğle")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun isPrayerChecked(date: Date, prayerName: String): Boolean {
        val key = "pray_" + dateFormat.format(date) + "_" + prayerName
        return prefs.getBoolean(key, false)
    }

    fun togglePrayerChecked(date: Date, prayerName: String): Boolean {
        val key = "pray_" + dateFormat.format(date) + "_" + prayerName
        val current = prefs.getBoolean(key, false)
        prefs.edit().putBoolean(key, !current).apply()
        return !current
    }

    // Dhikr
    var activeDhikrName: String
        get() = prefs.getString("active_dhikr", "Sübhânallâh") ?: "Sübhânallâh"
        set(value) = prefs.edit().putString("active_dhikr", value).apply()

    var activeDhikrTarget: Int
        get() = prefs.getInt("active_dhikr_target", 33)
        set(value) = prefs.edit().putInt("active_dhikr_target", value).apply()

    fun getDhikrCount(name: String): Int = prefs.getInt("dhikr_count_$name", 0)

    fun setDhikrCount(name: String, count: Int) {
        prefs.edit().putInt("dhikr_count_$name", count.coerceAtLeast(0)).apply()
    }

    fun getCustomDhikrs(): List<String> {
        val str = prefs.getString("custom_dhikrs", "") ?: ""
        return if (str.isEmpty()) emptyList() else str.split(";;;").filter { it.isNotBlank() }
    }

    fun addCustomDhikr(name: String) {
        val current = getCustomDhikrs().toMutableList()
        if (!current.contains(name)) {
            current.add(name)
            prefs.edit().putString("custom_dhikrs", current.joinToString(";;;")).apply()
        }
    }

    fun removeCustomDhikr(name: String) {
        val current = getCustomDhikrs().toMutableList()
        current.remove(name)
        prefs.edit().putString("custom_dhikrs", current.joinToString(";;;")).apply()
    }

    // Language (Always "tr")
    var language: String
        get() = "tr"
        set(_) = prefs.edit().putString("app_language", "tr").apply()

    // Ezan Makam Choice ("Genel", "Saba", "Uşşak", "Rast", "Segâh", "Hicaz")
    var ezanMakam: String
        get() = prefs.getString("ezan_makam", "Genel") ?: "Genel"
        set(value) = prefs.edit().putString("ezan_makam", value).apply()

    // Auto Silent Mode during Prayer
    var autoSilentMode: Boolean
        get() = prefs.getBoolean("auto_silent_mode", false)
        set(value) = prefs.edit().putBoolean("auto_silent_mode", value).apply()

    // Quran Reciter ID
    var quranReciter: String
        get() = prefs.getString("quran_reciter", "mishari") ?: "mishari"
        set(value) = prefs.edit().putString("quran_reciter", value).apply()

    // Quran bookmark
    var bookmarkSurah: Int
        get() = prefs.getInt("bookmark_surah", 1)
        set(value) = prefs.edit().putInt("bookmark_surah", value).apply()

    var bookmarkAyah: Int
        get() = prefs.getInt("bookmark_ayah", 1)
        set(value) = prefs.edit().putInt("bookmark_ayah", value).apply()
}
