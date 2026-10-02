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

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Preload assets in background / app start
        DataLoader.loadAll(this)
        com.stitchilyas.vakitvedua.ads.AdManager.initialize(this)

        val prefs = AppPreferences(this)

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

    companion object {
        private const val REQUEST_POST_NOTIFICATIONS = 1001
    }
}
