package com.stitchilyas.vakitvedua.data.model

data class District(
    val name: String,
    val lat: Double,
    val lng: Double
)

data class City(
    val name: String,
    val lat: Double,
    val lng: Double,
    val districts: List<District>
)

data class Ayah(
    val number: Int,
    val text: String
)

data class QuranSurah(
    val id: Int,
    val name: String,
    val ayahs: List<Ayah>
) {
    val verseCount: Int get() = ayahs.size
}

data class DuaItem(
    val title: String,
    val arabic: String,
    val okunus: String,
    val meal: String,
    val source: String
)

data class EsmaItem(
    val id: Int,
    val name: String,
    val arabic: String,
    val meaning: String,
    val zikir: Int
)

data class GuideItem(
    val title: String,
    val text: String
)

data class HadithItem(
    val source: String,
    val text: String,
    val arabic: String
)

data class MessageCategory(
    val title: String,
    val messages: List<String>
)

data class KazaCount(
    val key: String,
    val name: String,
    var count: Int
)

data class DhikrPreset(
    val name: String,
    val target: Int,
    val count: Int = 0,
    val isCustom: Boolean = false
)
