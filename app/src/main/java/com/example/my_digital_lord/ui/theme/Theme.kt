package com.example.my_digital_lord.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Pink,
    secondary = Cyan,
    tertiary = Yellow,
    background = Dark,
    surface = Surface,
    error = Error,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.White,
    primaryContainer = Pink.copy(alpha = 0.2f),
    secondaryContainer = Cyan.copy(alpha = 0.2f),
    tertiaryContainer = Yellow.copy(alpha = 0.2f),
    errorContainer = Error.copy(alpha = 0.3f),
    surfaceVariant = Color(0xFF2A2A3E)
)

private val LightColorScheme = lightColorScheme(
    primary = Pink,
    secondary = Color(0xFF009999),
    tertiary = Color(0xFFCC9900),
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    error = Error,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A),
    onError = Color.White,
    primaryContainer = Color(0xFFFFE0F0),
    secondaryContainer = Color(0xFFE0FFFF),
    tertiaryContainer = Color(0xFFFFFBE0),
    errorContainer = Color(0xFFFFE0E0),
    surfaceVariant = Color(0xFFF0F0F0)
)

@Composable
fun My_digital_lordTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    androidx.compose.material3.MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}