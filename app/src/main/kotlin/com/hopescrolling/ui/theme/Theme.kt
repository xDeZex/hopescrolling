package com.hopescrolling.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val OledDarkColorScheme = darkColorScheme(
    background = OledBlack,
    surface = Surface,
    primary = Primary,
    onPrimary = OnPrimary,
    onBackground = OnBackground,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    error = Error,
)

@Composable
fun HopescrollingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OledDarkColorScheme,
        content = content,
    )
}
