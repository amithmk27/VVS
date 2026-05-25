package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PurpleDarkPrimary,
    secondary = PurpleDarkSecondary,
    tertiary = GoldAccent,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkBackground,
    onSecondary = DarkBackground,
    onBackground = DarkTextLight,
    onSurface = DarkTextLight
)

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,                // Sage Green for main actions & buttons
    secondary = SoftGreen,                  // Soft Green for chips & badges
    tertiary = GoldAccent,                  // Gold Accent
    background = BackgroundCream,           // Light off-white background
    surface = SurfaceLavender,              // White card surfaces
    onPrimary = Color.White,
    onSecondary = TextDark,                 // Crisp reading text on soft green components
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Custom branding is preferred over dynamic device colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // Force LightColorScheme to satisfy "Avoid: Dark themes" design constraint
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
