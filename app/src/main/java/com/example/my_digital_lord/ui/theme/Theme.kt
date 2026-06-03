package com.example.my_digital_lord.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

enum class AppTheme {
    SUNSET_NEON,
    RETRO_TERMINAL,
    ICE_GLITCH
}

val LocalAppTheme = staticCompositionLocalOf { AppTheme.SUNSET_NEON }

@Composable
fun MyDigitalLordTheme(
    theme: AppTheme = AppTheme.SUNSET_NEON,
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        AppTheme.SUNSET_NEON -> if (darkTheme) SunsetDarkScheme else SunsetLightScheme
        AppTheme.RETRO_TERMINAL -> if (darkTheme) TerminalDarkScheme else TerminalLightScheme
        AppTheme.ICE_GLITCH -> if (darkTheme) IceDarkScheme else IceLightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SunsetTypography,
        content = content
    )
}