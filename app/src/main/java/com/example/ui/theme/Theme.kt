package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightPurpleColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = PurpleOnPrimary,
    primaryContainer = PurplePrimaryContainer,
    onPrimaryContainer = PurpleOnPrimaryContainer,
    secondary = PurpleSecondary,
    background = PurpleBackground,
    surface = PurpleSurface,
    surfaceVariant = PurpleSurfaceVariant,
    error = CoralError
)

private val DarkPurpleColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = Color(0xFFD1C4E9),
    secondary = PurpleSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    error = CoralError
)

private val PinkColorScheme = lightColorScheme(
    primary = PinkPrimary,
    onPrimary = Color.White,
    primaryContainer = PinkPrimaryContainer,
    onPrimaryContainer = PinkOnPrimaryContainer,
    secondary = SoftRose,
    background = Color(0xFFFFF7F9),
    surface = Color.White,
    surfaceVariant = Color(0xFFFCEBF1)
)

private val BlueColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BluePrimaryContainer,
    onPrimaryContainer = BlueOnPrimaryContainer,
    secondary = NeonCyan,
    background = Color(0xFFF3F9FD),
    surface = Color.White,
    surfaceVariant = Color(0xFFE8F3FA)
)

@Composable
fun HeenaAITheme(
    themeName: String = "purple",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeName) {
        "dark" -> DarkPurpleColorScheme
        "light" -> LightPurpleColorScheme
        "pink" -> PinkColorScheme
        "blue" -> BlueColorScheme
        "pastel" -> LightPurpleColorScheme.copy(
            primary = Color(0xFF9575CD),
            primaryContainer = Color(0xFFEDE7F6),
            background = Color(0xFFFAF8FF)
        )
        else -> if (darkTheme) DarkPurpleColorScheme else LightPurpleColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
