package com.metroapp.lima.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = MetroGreenPrimary,
    onPrimary = SurfaceLight,
    primaryContainer = MetroGreenContainer,
    onPrimaryContainer = MetroOnGreenContainer,
    secondary = MetroBlue,
    onSecondary = SurfaceLight,
    secondaryContainer = MetroBlueContainer,
    onSecondaryContainer = Color(0xFF001D36),
    error = MetroRed,
    errorContainer = MetroRedContainer,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = OutlineLight
)

@Composable
fun MetroAppTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = MetroGreenPrimary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
