package com.stitchilyas.vakitvedua.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = TealAccent,
    onPrimary = SurfaceDark,
    primaryContainer = TealSoftDark,
    onPrimaryContainer = TealAccent,
    secondary = BrassGoldLight,
    onSecondary = SurfaceDark,
    background = BgDark,
    onBackground = TextDarkPrimary,
    surface = SurfaceDark,
    onSurface = TextDarkPrimary,
    surfaceVariant = LineDark,
    onSurfaceVariant = TextDarkSecondary,
    outline = LineDark
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = SurfaceLight,
    primaryContainer = TealSoft,
    onPrimaryContainer = TealPrimary,
    secondary = BrassGold,
    onSecondary = SurfaceLight,
    background = BgLight,
    onBackground = TextLightPrimary,
    surface = SurfaceLight,
    onSurface = TextLightPrimary,
    surfaceVariant = LineLight,
    onSurfaceVariant = TextLightSecondary,
    outline = LineLight
)

@Composable
fun VakitVeDuaTheme(
    themeMode: Int = 0, // 0: System, 1: Light, 2: Dark
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
