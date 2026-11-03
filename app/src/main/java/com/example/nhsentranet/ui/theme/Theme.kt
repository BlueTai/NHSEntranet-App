package com.example.nhsentranet.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = WebTealBrand,
    onPrimary = Color.White,
    secondary = WebCharcoalText,
    onSecondary = Color.White,
    tertiary = NewtonNavy,
    background = WebPageBg,
    surface = Color.White,
    onBackground = WebCharcoalText,
    onSurface = WebCharcoalText,
    error = NewtonRed
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkTealBrand,
    onPrimary = Color.Black,
    secondary = DarkCharcoalText,
    onSecondary = Color.Black,
    tertiary = NewtonNavy,
    background = DarkPageBg,
    surface = DarkSurface,
    onBackground = DarkCharcoalText,
    onSurface = DarkCharcoalText,
    error = NewtonRed
)

@Composable
fun NewtonHouseTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (isDarkTheme) DarkHeaderCyan.toArgb() else NewtonNavy.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
