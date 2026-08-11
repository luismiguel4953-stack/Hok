package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GamingColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF003840),
    onPrimaryContainer = CyberCyan,
    secondary = CyberRed,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF400018),
    onSecondaryContainer = CyberRed,
    tertiary = CyberGreen,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF003820),
    onTertiaryContainer = CyberGreen,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = Color(0xFF1E2838)
)

@Composable
fun GameTurboTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GamingColorScheme,
        typography = Typography,
        content = content
    )
}

