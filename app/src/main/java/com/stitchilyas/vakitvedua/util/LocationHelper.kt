package com.stitchilyas.vakitvedua.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.stitchilyas.vakitvedua.data.model.City
import com.stitchilyas.vakitvedua.data.model.District
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object LocationHelper {
    private const val TAG = "LocationHelper"

    sealed class LocationResult {
        data class Success(
            val latitude: Double,
            val longitude: Double,
            val cityName: String,
            val districtName: String
        ) : LocationResult()

        data class Error(val message: String) : LocationResult()
        object PermissionDenied : LocationResult()
        object GpsDisabled : LocationResult()
    }

    fun hasPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        val gpsEnabled = try {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (_: Exception) { false }
        val networkEnabled = try {
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (_: Exception) { false }
        return gpsEnabled || networkEnabled
    }

    /**
     * Calculates great-circle distance between two points in kilometers using Haversine formula.
     */
    fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Finds the nearest City and District from the database for given coordinates.
     */
    fun findNearestCityAndDistrict(
        userLat: Double,
        userLng: Double,
        cities: List<City>
    ): Pair<City, District?> {
        var closestCity = cities.firstOrNull() ?: City("İstanbul", 41.0082, 28.9784, emptyList())
        var closestDistrict: District? = null
        var minDistanceKm = Double.MAX_VALUE

        for (city in cities) {
            // Check city center distance
            val distToCity = haversineKm(userLat, userLng, city.lat, city.lng)
            if (distToCity < minDistanceKm) {
                minDistanceKm = distToCity
                closestCity = city
                closestDistrict = null
            }

            // Check all districts of this city
            for (district in city.districts) {
                val distToDistrict = haversineKm(userLat, userLng, district.lat, district.lng)
                if (distToDistrict < minDistanceKm) {
                    minDistanceKm = distToDistrict
                    closestCity = city
                    closestDistrict = district
                }
            }
        }
        return Pair(closestCity, closestDistrict)
    }

    /**
     * Fetches current device location using GPS / Network providers and matches to nearest Turkish city/district.
     */
    fun getCurrentLocation(
        context: Context,
        cities: List<City>,
        onResult: (LocationResult) -> Unit
    ) {
        if (!hasPermission(context)) {
            onResult(LocationResult.PermissionDenied)
            return
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            onResult(LocationResult.Error("Konum servisine ulaşılamadı."))
            return
        }

        if (!isLocationEnabled(context)) {
            onResult(LocationResult.GpsDisabled)
            return
        }

        // 1. Gather best last known location as fallback
        var bestLastLocation: Location? = null
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
        for (provider in providers) {
            try {
                val loc = locationManager.getLastKnownLocation(provider)
                if (loc != null) {
                    if (bestLastLocation == null || loc.time > bestLastLocation.time || loc.accuracy < bestLastLocation.accuracy) {
                        bestLastLocation = loc
                    }
                }
            } catch (e: SecurityException) {
                Log.w(TAG, "SecurityException reading last known location: ${e.message}")
            }
        }

        var isCompleted = false
        val handler = Handler(Looper.getMainLooper())

        fun completeWithLocation(loc: Location) {
            if (isCompleted) return
            isCompleted = true
            processAndResolveLocation(context, loc, cities, onResult)
        }

        // Timeout runnable: after 6 seconds, fall back to best last location if available
        val timeoutRunnable = Runnable {
            if (!isCompleted) {
                if (bestLastLocation != null) {
                    Log.d(TAG, "Location request timed out; falling back to best last known location.")
                    completeWithLocation(bestLastLocation)
                } else {
                    isCompleted = true
                    onResult(LocationResult.Error("Konum sinyali alınamadı. Lütfen açık alanda tekrar deneyin."))
                }
            }
        }
        handler.postDelayed(timeoutRunnable, 6000)

        // 2. Request fresh location
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val cancellationSignal = CancellationSignal()
                val provider = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    LocationManager.GPS_PROVIDER
                } else {
                    LocationManager.NETWORK_PROVIDER
                }

                locationManager.getCurrentLocation(
                    provider,
                    cancellationSignal,
                    ContextCompat.getMainExecutor(context)
                ) { location ->
                    handler.removeCallbacks(timeoutRunnable)
                    if (location != null) {
                        completeWithLocation(location)
                    } else if (bestLastLocation != null) {
                        completeWithLocation(bestLastLocation)
                    } else {
                        if (!isCompleted) {
                            isCompleted = true
                            onResult(LocationResult.Error("Konum sinyali alınamadı. Açık alanda tekrar deneyin."))
                        }
                    }
                }
                return
            } catch (e: SecurityException) {
                Log.w(TAG, "SecurityException on getCurrentLocation: ${e.message}")
            } catch (e: Exception) {
                Log.w(TAG, "Exception on getCurrentLocation: ${e.message}")
            }
        }

        // Fallback for pre-Android R or if getCurrentLocation fails
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                handler.removeCallbacks(timeoutRunnable)
                try {
                    locationManager.removeUpdates(this)
                } catch (_: Exception) {}
                completeWithLocation(location)
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        var listenerRegistered = false
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestSingleUpdate(
                    LocationManager.GPS_PROVIDER,
                    locationListener,
                    Looper.getMainLooper()
                )
                listenerRegistered = true
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestSingleUpdate(
                    LocationManager.NETWORK_PROVIDER,
                    locationListener,
                    Looper.getMainLooper()
                )
                listenerRegistered = true
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException registering single update: ${e.message}")
        } catch (e: Exception) {
            Log.w(TAG, "Exception registering single update: ${e.message}")
        }

        if (!listenerRegistered && bestLastLocation != null) {
            handler.removeCallbacks(timeoutRunnable)
            completeWithLocation(bestLastLocation)
        }
    }

    private fun processAndResolveLocation(
        context: Context,
        location: Location,
        cities: List<City>,
        onResult: (LocationResult) -> Unit
    ) {
        val userLat = location.latitude
        val userLng = location.longitude

        var geocodedCityName: String? = null
        var geocodedDistrictName: String? = null

        // Try Geocoder (supports online reverse geocoding to Turkish administrative areas)
        try {
            val geocoder = Geocoder(context, Locale("tr", "TR"))
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(userLat, userLng, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                geocodedCityName = address.adminArea ?: address.subAdminArea
                geocodedDistrictName = address.subAdminArea ?: address.subLocality ?: address.locality
                Log.d(TAG, "Geocoder resolved: city=$geocodedCityName, district=$geocodedDistrictName")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Geocoder failed: ${e.message}. Using mathematical nearest-neighbor matching.")
        }

        // Mathematical nearest-neighbor matching from offline database
        val (nearestCity, nearestDistrict) = findNearestCityAndDistrict(userLat, userLng, cities)

        // If Geocoder matched an existing city name in our DB, prioritize matching inside that city
        var finalCity = nearestCity
        var finalDistrict = nearestDistrict

        if (geocodedCityName != null) {
            val matchedCity = cities.find {
                it.name.equals(geocodedCityName, ignoreCase = true) ||
                geocodedCityName!!.contains(it.name, ignoreCase = true)
            }
            if (matchedCity != null) {
                finalCity = matchedCity
                if (geocodedDistrictName != null) {
                    val matchedDist = matchedCity.districts.find {
                        it.name.equals(geocodedDistrictName, ignoreCase = true) ||
                        geocodedDistrictName!!.contains(it.name, ignoreCase = true)
                    }
                    if (matchedDist != null) {
                        finalDistrict = matchedDist
                    }
                }
            }
        }

        val resolvedCityName = finalCity.name
        val resolvedDistrictName = finalDistrict?.name ?: ""

        Log.d(TAG, "Final resolved location: $resolvedCityName, $resolvedDistrictName ($userLat, $userLng)")

        onResult(
            LocationResult.Success(
                latitude = userLat,
                longitude = userLng,
                cityName = resolvedCityName,
                districtName = resolvedDistrictName
            )
        )
    }

    /**
     * Uygulama açılışında sessiz konum doğrulaması: cihaz konumunu alır, en yakın
     * il/ilçeyi bulur ve kayıtlı seçimden farklıysa [onResult] ile bildirir.
     * Konum alınamazsa hiçbir şey yapmaz (kullanıcıyı rahatsız etmez).
     */
    fun verifyLocationSilently(
        context: Context,
        cities: List<City>,
        onResult: (LocationResult) -> Unit
    ) {
        Thread {
            try {
                getCurrentLocation(context, cities) { result ->
                    android.os.Handler(Looper.getMainLooper()).post { onResult(result) }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Sessiz konum doğrulaması başarısız: ${e.message}")
            }
        }.apply {
            name = "SilentLocationVerifier"
            isDaemon = true
        }.start()
    }
}
