package com.stitchilyas.vakitvedua

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.stitchilyas.vakitvedua.data.local.AppPreferences
import com.stitchilyas.vakitvedua.data.local.DataLoader
import com.stitchilyas.vakitvedua.notification.PrayerNotificationScheduler
import com.stitchilyas.vakitvedua.ui.navigation.MainScreen
import com.stitchilyas.vakitvedua.ui.theme.VakitVeDuaTheme
import com.stitchilyas.vakitvedua.util.LocationHelper
import android.widget.Toast

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Preload assets in background / app start
        DataLoader.loadAll(this)
        com.stitchilyas.vakitvedua.ads.AdManager.initialize(this)

        val prefs = AppPreferences(this)

        // Açılışta sessiz konum doğrulaması: il/ilçe değiştiyse otomatik güncelle
        maybeAutoVerifyLocation(prefs)

        // Namaz vakti bildirimlerini kur; Android 13+ izin gerekir
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_POST_NOTIFICATIONS
            )
        } else {
            PrayerNotificationScheduler.reschedule(this)
        }

        setContent {
            var currentThemeMode by remember { mutableIntStateOf(prefs.themeMode) }

            VakitVeDuaTheme(themeMode = currentThemeMode) {
                MainScreen(
                    prefs = prefs,
                    onThemeChanged = { newMode ->
                        currentThemeMode = newMode
                    }
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_POST_NOTIFICATIONS &&
            grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        ) {
            PrayerNotificationScheduler.reschedule(this)
        }
    }

    /**
     * Konum izni zaten verilmişse açılışta sessizce konum doğrulaması yapar.
     * Cihazın bulunduğu il/ilçe, kayıtlı seçimden farklıysa otomatik güncelleştirir.
     * İzin verilmemişse hiçbir şey yapmaz (izin isteme yalnızca kullanıcı aksiyonuyla).
     */
    private fun maybeAutoVerifyLocation(prefs: AppPreferences) {
        if (!LocationHelper.hasPermission(this)) return
        if (!LocationHelper.isLocationEnabled(this)) return

        val cities = DataLoader.loadCities(this)
        if (cities.isEmpty()) return

        LocationHelper.verifyLocationSilently(this, cities) { result ->
            if (result is LocationHelper.LocationResult.Success) {
                val cityChanged = result.cityName != prefs.cityName
                val districtChanged = result.districtName != prefs.districtName
                if (cityChanged || districtChanged) {
                    prefs.cityName = result.cityName
                    prefs.districtName = result.districtName
                    prefs.latitude = result.latitude
                    prefs.longitude = result.longitude
                    PrayerNotificationScheduler.reschedule(this)

                    val locationText = if (result.districtName.isNotEmpty())
                        "${result.cityName}, ${result.districtName}" else result.cityName
                    Toast.makeText(
                        this,
                        "Konumunuz güncellendi: $locationText",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_POST_NOTIFICATIONS = 1001
    }
}
