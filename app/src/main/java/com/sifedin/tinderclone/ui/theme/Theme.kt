package com.sifedin.tinderclone.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HolaDarkColorScheme = darkColorScheme(
    primary = HolaPinkPrimary,
    secondary = HolaPinkPrimary,
    tertiary = HolaPinkPrimary,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant
)

@Composable
fun HolaDatingAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HolaDarkColorScheme,
        typography = Typography,
        content = content
    )
}