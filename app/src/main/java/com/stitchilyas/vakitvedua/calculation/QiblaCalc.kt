package com.stitchilyas.vakitvedua.calculation

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

object QiblaCalc {
    // Kaaba coordinates in Mecca
    const val KAABA_LAT = 21.4225
    const val KAABA_LNG = 39.8262

    private fun sinD(d: Double) = sin(Math.toRadians(d))
    private fun cosD(d: Double) = cos(Math.toRadians(d))
    private fun tanD(d: Double) = tan(Math.toRadians(d))
    private fun asinD(x: Double) = Math.toDegrees(asin(x.coerceIn(-1.0, 1.0)))
    private fun atan2D(y: Double, x: Double) = Math.toDegrees(atan2(y, x))

    private fun fix(a: Double, b: Double): Double {
        var v = a - b * floor(a / b)
        if (v < 0) v += b
        return v
    }

    /**
     * Calculates Qibla direction in degrees (0..360, where 0 is North, 90 is East)
     */
    fun calculateQibla(lat: Double, lng: Double): Double {
        val deltaLng = Math.toRadians(KAABA_LNG - lng)
        val latRad = Math.toRadians(lat)
        val kaabaLatRad = Math.toRadians(KAABA_LAT)

        val y = sin(deltaLng)
        val x = cos(latRad) * tan(kaabaLatRad) - sin(latRad) * cos(deltaLng)
        val qibla = Math.toDegrees(atan2(y, x))
        return fix(qibla, 360.0)
    }

    /**
     * Distance to Kaaba in kilometers
     */
    fun calculateDistanceKm(lat: Double, lng: Double): Double {
        val r = 6371.0 // Earth radius in km
        val p = sinD(lat) * sinD(KAABA_LAT) + cosD(lat) * cosD(KAABA_LAT) * cosD(KAABA_LNG - lng)
        return r * acos(p.coerceIn(-1.0, 1.0))
    }

    data class SunPosResult(val azimuth: Double, val elevation: Double)

    /**
     * Sun azimuth and elevation for daylight sun orientation method
     */
    fun getSunAzimuth(lat: Double, lng: Double, cal: Calendar = Calendar.getInstance()): SunPosResult {
        val jd = cal.timeInMillis / 86400000.0 + 2440587.5
        val sunPos = VakitCalc.sunPos(jd)
        val decl = sunPos[0]
        val eqt = sunPos[1]

        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = cal.timeInMillis
        }
        val utcH = utcCal.get(Calendar.HOUR_OF_DAY) +
                utcCal.get(Calendar.MINUTE) / 60.0 +
                utcCal.get(Calendar.SECOND) / 3600.0

        val ha = (utcH + lng / 15.0 + eqt - 12.0) * 15.0
        val el = asinD(sinD(lat) * sinD(decl) + cosD(lat) * cosD(decl) * cosD(ha))
        val az = fix(atan2D(sinD(ha), cosD(ha) * sinD(lat) - tanD(decl) * cosD(lat)) + 180.0, 360.0)
        return SunPosResult(azimuth = az, elevation = el)
    }
}
