package com.hopescrolling.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val OledTealColorScheme = darkColorScheme(
    background = OledBlack,
    surface = OledBlack,
    primary = Primary,
    onPrimary = OnPrimary,
    onBackground = OnBackground,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    outlineVariant = OutlineVariant,
    error = Error,
)

@Composable
fun HopescrollingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OledTealColorScheme,
        content = content,
    )
}
