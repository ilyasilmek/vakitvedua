package com.stitchilyas.vakitvedua.util

object L10n {
    private val tr = mapOf(
        // Navigation & TopBar
        "app_name" to "Vakit ve Dua",
        "nav_vakitler" to "Vakitler",
        "nav_kible" to "Kıble",
        "nav_rehber" to "Rehber",
        "nav_kuran" to "Kur'an",
        "nav_mesajlar" to "Mesajlar",
        "nav_diger" to "Menü",
        "nav_zikir" to "Zikir",
        "nav_dualar" to "Dualar",
        "nav_takip" to "Takip",
        "nav_takvim" to "Takvim",
        "nav_ayarlar" to "Ayarlar",

        // Vakitler Screen
        "prayer_imsak" to "İmsak",
        "prayer_gunes" to "Güneş",
        "prayer_ogle" to "Öğle",
        "prayer_ikindi" to "İkindi",
        "prayer_aksam" to "Akşam",
        "prayer_yatsi" to "Yatsı",
        "next_prayer" to "Sıradaki Vakit",
        "remaining" to "kaldı",
        "kerahat_warning" to "Kerahat Vakti!",
        "kerahat_desc" to "Mekruh vakit içerisindesiniz.",
        "mark_prayer_done" to "Namazı Kıldım İşaretle",
        "prayer_marked_done" to "Allah kabul etsin! Vakit kılındı olarak kaydedildi.",
        "locate_gps" to "GPS ile Konum Bul",
        "locating" to "Konum aranıyor...",

        // Kible Screen
        "qibla_title" to "Kıble Pusulası",
        "qibla_angle" to "Kıble Açısı",
        "kaba_distance" to "Kâbe Mesafesi",
        "aligned" to "Kıbleye Tam Hizalandınız!",
        "rotate_device" to "Telefonu düz tutun ve Kâbe yönüne dönün",
        "ar_camera_button" to "Kamera (AR) ile Kıble Yönü Bul",
        "sun_qibla_verification" to "Güneş ile Kıble Doğrulama",

        // Kuran Screen
        "quran_title" to "Kur'an-ı Kerim",
        "search_surah" to "Sure ara...",
        "last_read" to "Son Okunan Sure",
        "reciter" to "Hafız / Okuyucu",

        // Takip Screen
        "tracking_title" to "Namaz Takibi ve Kaza",
        "weekly_tracker" to "Son 7 Günlük İbadet Takibi",
        "kaza_counter" to "Kaza Namazı Sayacı",
        "wizard" to "Sihirbaz",
        "wizard_title" to "Kaza Borcu Hesaplama Sihirbazı",
        "calculate_and_save" to "Hesapla ve Kaydet",

        // Ayarlar Screen
        "settings_title" to "Ayarlar",
        "location" to "KONUM",
        "appearance" to "GÖRÜNÜM",
        "notifications_sounds" to "BİLDİRİM VE SESLER",
        "ezan_sound" to "Ezan Sesiyle Hatırlatma",
        "ezan_makam" to "Ezan Makamı Seçimi",
        "auto_silent" to "Namazda Otomatik Sessiz Mod",
        "about" to "HAKKINDA",

        // Common
        "cancel" to "İptal",
        "ok" to "Tamam",
        "share" to "Paylaş",
        "copy" to "Kopyala"
    )

    fun get(key: String, lang: String = "tr"): String {
        // App is 100% Turkish
        return tr[key] ?: key
    }
}
