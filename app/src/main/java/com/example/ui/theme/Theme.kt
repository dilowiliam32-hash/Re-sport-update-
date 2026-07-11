package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ZeSportAccent,
    secondary = ZeSportAmber,
    tertiary = ZeSportBlue,
    background = ZeSportBg,
    surface = ZeSportSurface,
    onPrimary = ZeSportText,
    onSecondary = ZeSportBg,
    onBackground = ZeSportText,
    onSurface = ZeSportText,
    surfaceVariant = ZeSportSurface2,
    outline = ZeSportLine
)

@Composable
fun ZeSportTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
