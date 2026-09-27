package com.stitchilyas.vakitvedua.data.local

import android.content.Context
import com.stitchilyas.vakitvedua.data.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

object DataLoader {
    private var cachedCities: List<City>? = null
    private var cachedSurahs: List<QuranSurah>? = null

    var dualar: List<DuaItem> = emptyList()
        private set
    var esmaList: List<EsmaItem> = emptyList()
        private set
    var abdestGuide: List<GuideItem> = emptyList()
        private set
    var namazGuide: List<GuideItem> = emptyList()
        private set
    var gununDualari: List<DuaItem> = emptyList()
        private set
    var hadisList: List<HadithItem> = emptyList()
        private set
    var messageCategories: List<MessageCategory> = emptyList()
        private set

    private fun readAsset(context: Context, filename: String): String {
        return context.assets.open(filename).use { stream ->
            BufferedReader(InputStreamReader(stream)).readText()
        }
    }

    fun loadAll(context: Context) {
        loadAppData(context)
        loadCities(context)
    }

    fun loadCities(context: Context): List<City> {
        cachedCities?.let { return it }
        try {
            val jsonStr = readAsset(context, "cities.json")
            val root = JSONObject(jsonStr)
            val list = mutableListOf<City>()

            val keys = root.keys()
            while (keys.hasNext()) {
                val cityName = keys.next()
                val cityObj = root.getJSONObject(cityName)
                val cArr = cityObj.getJSONArray("c")
                val cityLat = cArr.getDouble(0)
                val cityLng = cArr.getDouble(1)

                val districts = mutableListOf<District>()
                if (cityObj.has("d")) {
                    val dObj = cityObj.getJSONObject("d")
                    val dKeys = dObj.keys()
                    while (dKeys.hasNext()) {
                        val dName = dKeys.next()
                        val dArr = dObj.getJSONArray(dName)
                        val dLat = dArr.getDouble(0)
                        val dLng = dArr.getDouble(1)
                        districts.add(District(name = dName, lat = dLat, lng = dLng))
                    }
                }
                districts.sortBy { it.name }
                list.add(City(name = cityName, lat = cityLat, lng = cityLng, districts = districts))
            }
            list.sortBy { it.name }
            cachedCities = list
            return list
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    private fun loadAppData(context: Context) {
        try {
            val jsonStr = readAsset(context, "app_data.json")
            val root = JSONObject(jsonStr)

            // Dualar
            if (root.has("dua")) {
                val arr = root.getJSONArray("dua")
                val list = mutableListOf<DuaItem>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        DuaItem(
                            title = obj.optString("t"),
                            arabic = obj.optString("a"),
                            okunus = obj.optString("o"),
                            meal = obj.optString("m"),
                            source = obj.optString("s")
                        )
                    )
                }
                dualar = list
            }

            // Esma
            if (root.has("esma")) {
                val arr = root.getJSONArray("esma")
                val list = mutableListOf<EsmaItem>()
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONArray(i)
                    list.add(
                        EsmaItem(
                            id = i + 1,
                            name = item.optString(0),
                            arabic = item.optString(1),
                            meaning = item.optString(2),
                            zikir = if (item.length() > 3) item.optInt(3, 100) else 100
                        )
                    )
                }
                esmaList = list
            }

            // Abdest
            if (root.has("abdest")) {
                val arr = root.getJSONArray("abdest")
                val list = mutableListOf<GuideItem>()
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONArray(i)
                    list.add(
                        GuideItem(
                            title = item.optString(0),
                            text = item.optString(1)
                        )
                    )
                }
                abdestGuide = list
            }

            // Namaz
            if (root.has("namaz")) {
                val arr = root.getJSONArray("namaz")
                val list = mutableListOf<GuideItem>()
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONArray(i)
                    list.add(
                        GuideItem(
                            title = item.optString(1),
                            text = item.optString(2)
                        )
                    )
                }
                namazGuide = list
            }

            // Günün Duası
            if (root.has("gdua")) {
                val arr = root.getJSONArray("gdua")
                val list = mutableListOf<DuaItem>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        DuaItem(
                            title = "Günün Duası",
                            arabic = obj.optString("a"),
                            okunus = "",
                            meal = obj.optString("t"),
                            source = obj.optString("s")
                        )
                    )
                }
                gununDualari = list
            }

            // Hadisler
            if (root.has("hadis")) {
                val arr = root.getJSONArray("hadis")
                val list = mutableListOf<HadithItem>()
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONArray(i)
                    list.add(
                        HadithItem(
                            source = "${item.optString(0)}, ${item.opt(1)}",
                            text = item.optString(2),
                            arabic = item.optString(3)
                        )
                    )
                }
                hadisList = list
            }

            // Hazır Mesajlar
            if (root.has("msg")) {
                val msgObj = root.getJSONObject("msg")
                val catList = mutableListOf<MessageCategory>()
                val keys = msgObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val item = msgObj.get(k)
                    val msgs = mutableListOf<String>()
                    var catTitle = k

                    if (item is JSONObject) {
                        catTitle = item.optString("n", k)
                        if (item.has("m")) {
                            val arr = item.getJSONArray("m")
                            for (j in 0 until arr.length()) {
                                msgs.add(arr.getString(j))
                            }
                        }
                    } else if (item is JSONArray) {
                        for (j in 0 until item.length()) {
                            msgs.add(item.getString(j))
                        }
                    }

                    if (msgs.isNotEmpty()) {
                        catList.add(MessageCategory(title = catTitle, messages = msgs))
                    }
                }
                messageCategories = catList
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadQuran(context: Context): List<QuranSurah> {
        cachedSurahs?.let { return it }
        try {
            val jsonStr = readAsset(context, "quran.json").trim()
            val arr = if (jsonStr.startsWith("{")) {
                val root = JSONObject(jsonStr)
                root.getJSONArray("sureler")
            } else {
                JSONArray(jsonStr)
            }
            val list = mutableListOf<QuranSurah>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val surahId = obj.getInt("s")
                val surahName = obj.getString("n")
                val ayahsArr = obj.getJSONArray("a")
                val ayahs = mutableListOf<Ayah>()
                for (j in 0 until ayahsArr.length()) {
                    val aItem = ayahsArr.getJSONArray(j)
                    ayahs.add(Ayah(number = aItem.getInt(0), text = aItem.getString(1)))
                }
                list.add(QuranSurah(id = surahId, name = surahName, ayahs = ayahs))
            }
            cachedSurahs = list
            return list
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
}
