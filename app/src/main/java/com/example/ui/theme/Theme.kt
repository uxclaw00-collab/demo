package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BentoGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF234414),
    onPrimaryContainer = BentoGreenLight,
    secondary = GoldenHoney,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF472A00),
    onSecondaryContainer = Color(0xFFFFDCC1),
    tertiary = BentoTextMuted,
    background = Color(0xFF131512),
    surface = Color(0xFF1A1C18),
    onBackground = Color(0xFFE1E4D5),
    onSurface = Color(0xFFE1E4D5),
    surfaceVariant = Color(0xFF2C3028),
    onSurfaceVariant = Color(0xFFC4C8B8),
    outline = BentoBorderStrong
)

private val LightColorScheme = lightColorScheme(
    primary = BentoGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = BentoTileSage,
    onPrimaryContainer = BentoOnGreenContainer,
    secondary = WarmSpice,
    onSecondary = Color.White,
    secondaryContainer = BentoSurfaceVariant,
    onSecondaryContainer = BentoTextPrimary,
    tertiary = BentoTextMuted,
    background = BentoBackground,
    surface = BentoSurface,
    onBackground = BentoTextPrimary,
    onSurface = BentoTextPrimary,
    surfaceVariant = BentoSurfaceVariant,
    onSurfaceVariant = BentoTextSecondary,
    outline = BentoBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

