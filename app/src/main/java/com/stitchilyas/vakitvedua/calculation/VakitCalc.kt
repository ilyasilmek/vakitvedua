package com.stitchilyas.vakitvedua.calculation

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

object VakitCalc {
    val NAMES = arrayOf("İmsak", "Güneş", "Öğle", "İkindi", "Akşam", "Yatsı")
    private val TEMKIN = intArrayOf(0, -7, 5, 4, 7, 0)

    private fun sinD(d: Double): Double = sin(Math.toRadians(d))
    private fun cosD(d: Double): Double = cos(Math.toRadians(d))
    private fun tanD(d: Double): Double = tan(Math.toRadians(d))
    private fun asinD(x: Double): Double = Math.toDegrees(asin(x.coerceIn(-1.0, 1.0)))
    private fun acosD(x: Double): Double = Math.toDegrees(acos(x.coerceIn(-1.0, 1.0)))
    private fun atan2D(y: Double, x: Double): Double = Math.toDegrees(atan2(y, x))
    private fun acotD(x: Double): Double = Math.toDegrees(atan(1.0 / x))

    private fun fix(a: Double, b: Double): Double {
        var v = a - b * floor(a / b)
        if (v < 0) v += b
        return v
    }

    private fun julian(y: Int, m: Int, d: Int): Double {
        var yr = y
        var mo = m
        if (mo <= 2) {
            yr--
            mo += 12
        }
        val a = floor(yr / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (yr + 4716)) + floor(30.6001 * (mo + 1)) + d + b - 1524.5
    }

    /** Returns [declination, equationOfTime] */
    fun sunPos(jd: Double): DoubleArray {
        val d = jd - 2451545.0
        val g = fix(357.529 + 0.98560028 * d, 360.0)
        val q = fix(280.459 + 0.98564736 * d, 360.0)
        val l = fix(q + 1.915 * sinD(g) + 0.02 * sinD(2 * g), 360.0)
        val e = 23.439 - 0.00000036 * d
        var ra = fix(atan2D(cosD(e) * sinD(l), cosD(l)) / 15.0, 24.0)
        return doubleArrayOf(asinD(sinD(e) * sinD(l)), q / 15.0 - ra)
    }

    private fun midDay(jd0: Double, t: Double): Double {
        return fix(12.0 - sunPos(jd0 + t)[1], 24.0)
    }

    private fun angT(jd0: Double, lat: Double, ang: Double, t: Double, ccw: Boolean): Double {
        val dc = sunPos(jd0 + t)[0]
        val n = midDay(jd0, t)
        val cosT = (-sinD(ang) - sinD(dc) * sinD(lat)) / (cosD(dc) * cosD(lat))
        val bigT = acosD(cosT.coerceIn(-1.0, 1.0)) / 15.0
        return n + (if (ccw) -bigT else bigT)
    }

    /**
     * Calculates the 6 prayer times in epoch milliseconds for the specified date and coordinates.
     * [0: İmsak, 1: Güneş, 2: Öğle, 3: İkindi, 4: Akşam, 5: Yatsı]
     */
    fun times(day: Calendar, lat: Double, lng: Double): LongArray {
        val mid = day.clone() as Calendar
        mid.set(Calendar.HOUR_OF_DAY, 0)
        mid.set(Calendar.MINUTE, 0)
        mid.set(Calendar.SECOND, 0)
        mid.set(Calendar.MILLISECOND, 0)

        val tz = TimeZone.getDefault().getOffset(mid.timeInMillis + 12 * 3600000L) / 3600000.0
        val jd0 = julian(
            mid.get(Calendar.YEAR),
            mid.get(Calendar.MONTH) + 1,
            mid.get(Calendar.DAY_OF_MONTH)
        ) - lng / (15.0 * 24.0)

        var h = doubleArrayOf(5.0, 6.0, 12.0, 13.0, 18.0, 18.0)
        for (it in 0 until 2) {
            val n = DoubleArray(6)
            n[0] = angT(jd0, lat, 18.0, h[0] / 24.0, true)
            n[1] = angT(jd0, lat, 0.833, h[1] / 24.0, true)
            n[2] = midDay(jd0, h[2] / 24.0)
            val dc = sunPos(jd0 + h[3] / 24.0)[0]
            n[3] = angT(jd0, lat, -acotD(1.0 + tanD(abs(lat - dc))), h[3] / 24.0, false)
            n[4] = angT(jd0, lat, 0.833, h[4] / 24.0, false)
            n[5] = angT(jd0, lat, 17.0, h[5] / 24.0, false)
            h = n
        }

        val out = LongArray(6)
        for (i in 0 until 6) {
            val hr = h[i] + tz - lng / 15.0 + TEMKIN[i] / 60.0
            out[i] = mid.timeInMillis + Math.round(hr * 60) * 60000L
        }
        return out
    }

    data class KerahatPeriod(
        val name: String,
        val start: Long,
        val end: Long
    )

    fun getKerahatPeriods(times: LongArray): List<KerahatPeriod> {
        val gunes = times[1]
        val ogle = times[2]
        val aksam = times[4]
        val m45 = 45 * 60 * 1000L
        return listOf(
            KerahatPeriod("Güneş doğarken", gunes, gunes + m45),
            KerahatPeriod("Öğle öncesi (İstiva)", ogle - m45, ogle),
            KerahatPeriod("Güneş batarken", aksam - m45, aksam)
        )
    }

    data class CurrentVakitInfo(
        val activeIndex: Int,
        val nextIndex: Int,
        val nextName: String,
        val nextTime: Long,
        val remainingMillis: Long,
        val isKerahat: Boolean,
        val kerahatName: String?,
        val kerahatRemainingMillis: Long
    )

    fun getCurrentInfo(timesToday: LongArray, timesTomorrow: LongArray, now: Long = System.currentTimeMillis()): CurrentVakitInfo {
        var active = 5 // Default Yatsı from previous night if before İmsak
        var next = 0
        var nextTime = timesToday[0]

        if (now < timesToday[0]) {
            active = 5
            next = 0
            nextTime = timesToday[0]
        } else if (now < timesToday[1]) {
            active = 0
            next = 1
            nextTime = timesToday[1]
        } else if (now < timesToday[2]) {
            active = 1
            next = 2
            nextTime = timesToday[2]
        } else if (now < timesToday[3]) {
            active = 2
            next = 3
            nextTime = timesToday[3]
        } else if (now < timesToday[4]) {
            active = 3
            next = 4
            nextTime = timesToday[4]
        } else if (now < timesToday[5]) {
            active = 4
            next = 5
            nextTime = timesToday[5]
        } else {
            active = 5
            next = 0
            nextTime = timesTomorrow[0]
        }

        val kerahatList = getKerahatPeriods(timesToday)
        val activeKerahat = kerahatList.find { now in it.start until it.end }

        return CurrentVakitInfo(
            activeIndex = active,
            nextIndex = next,
            nextName = NAMES[next],
            nextTime = nextTime,
            remainingMillis = max(0L, nextTime - now),
            isKerahat = activeKerahat != null,
            kerahatName = activeKerahat?.name,
            kerahatRemainingMillis = if (activeKerahat != null) max(0L, activeKerahat.end - now) else 0L
        )
    }
}
