package com.stitchilyas.vakitvedua.calculation

import java.util.Calendar
import java.util.Date
import kotlin.math.floor

object HijriCalc {
    val MONTH_NAMES = arrayOf(
        "Muharrem", "Safer", "Rebiülevvel", "Rebiülahir",
        "Cemaziyelevvel", "Cemaziyelahir", "Recep", "Şaban",
        "Ramazan", "Şevval", "Zilkade", "Zilhicce"
    )

    data class HijriDate(val day: Int, val month: Int, val year: Int) {
        val monthName: String get() = MONTH_NAMES.getOrElse(month - 1) { "" }
        override fun toString(): String = "$day $monthName $year"
    }

    data class ReligiousEvent(
        val name: String,
        val subtitle: String,
        val date: Date,
        val daysLeft: Int
    )

    fun fromGregorian(calendar: Calendar): HijriDate {
        val y = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH) + 1
        val d = calendar.get(Calendar.DAY_OF_MONTH)

        var yr = y
        var mo = m
        if (mo <= 2) {
            yr--
            mo += 12
        }
        val a = floor(yr / 100.0)
        val b = 2 - a + floor(a / 4.0)
        val jd = floor(365.25 * (yr + 4716)) + floor(30.6001 * (mo + 1)) + d + b - 1524.5

        // Civil Hijri algorithm
        val z = jd + 0.5
        val l = (z - 1948440 + 10632).toLong()
        val n = ((l - 1) / 10631).toInt()
        val l2 = l - 10631 * n + 354
        val j = (((10985 - l2) / 5316) * ((50 * l2) / 17719) + ((l2 / 5670) * ((43 * l2) / 15238))).toInt()
        val l3 = l2 - (((30 - j) / 15) * ((17719 * j) / 50)) - (((j / 16) * ((15238 * j) / 43))) + 29
        val month = ((24 * l3) / 709).toInt()
        val day = (l3 - ((709 * month) / 24)).toInt()
        val year = 30 * n + j - 30

        return HijriDate(day = day.coerceIn(1, 30), month = month.coerceIn(1, 12), year = year)
    }

    fun getUpcomingEvents(maxDays: Int = 400): List<ReligiousEvent> {
        val list = mutableListOf<ReligiousEvent>()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayMillis = cal.timeInMillis

        for (i in 0 until maxDays) {
            val h = fromGregorian(cal)
            val nextCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
            val hn = fromGregorian(nextCal)
            val daysLeft = Math.round((cal.timeInMillis - todayMillis) / 86400000.0).toInt()

            fun addEv(name: String, sub: String) {
                list.add(ReligiousEvent(name, sub, cal.time, daysLeft))
            }

            if (h.month == 1 && h.day == 1) addEv("Hicri Yılbaşı", "1 Muharrem")
            if (h.month == 1 && h.day == 10) addEv("Aşure Günü", "10 Muharrem")
            if (hn.month == 3 && hn.day == 12) addEv("Mevlid Kandili", "Peygamber Efendimiz'in (s.a.v.) Doğumu")
            if (h.month == 7 && h.day == 1) addEv("Üç Ayların Başlangıcı", "1 Recep")
            if (h.month == 7 && cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY && hn.month == 7 && hn.day <= 7) {
                addEv("Regaip Kandili", "Mübarek Cuma Gecesi")
            }
            if (hn.month == 7 && hn.day == 27) addEv("Miraç Kandili", "İsra ve Miraç Mucizesi")
            if (hn.month == 8 && hn.day == 15) addEv("Berat Kandili", "Bağışlanma ve Kurtuluş Gecesi")
            if (h.month == 9 && h.day == 1) addEv("Ramazan Başlangıcı", "İlk Oruç")
            if (hn.month == 9 && hn.day == 27) addEv("Kadir Gecesi", "Bin Aydan Hayırlı Gece")
            if (hn.month == 10 && hn.day == 1) addEv("Ramazan Bayramı Arifesi", "Bayram Hazırlığı")
            if (h.month == 10 && h.day == 1) addEv("Ramazan Bayramı 1. Gün", "Bayram Sevinci")
            if (h.month == 12 && h.day == 9) addEv("Kurban Bayramı Arifesi", "Arafat Günü")
            if (h.month == 12 && h.day == 10) addEv("Kurban Bayramı 1. Gün", "Kurban İbadeti")

            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        return list.sortedBy { it.daysLeft }
    }
}
