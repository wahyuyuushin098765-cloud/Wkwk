package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WarColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color.Black,
    primaryContainer = GoldDark,
    onPrimaryContainer = GoldSecondary,
    secondary = GoldSecondary,
    onSecondary = Color.Black,
    background = DarkBackground,
    onBackground = TextGold,
    surface = DarkSurface,
    onSurface = TextGold,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = DarkBorder,
    error = EnemyRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WarColorScheme,
        typography = Typography,
        content = content
    )
}
