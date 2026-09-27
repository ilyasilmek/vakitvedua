package com.stitchilyas.vakitvedua

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.stitchilyas.vakitvedua.data.local.AppPreferences
import com.stitchilyas.vakitvedua.data.local.DataLoader
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
}
