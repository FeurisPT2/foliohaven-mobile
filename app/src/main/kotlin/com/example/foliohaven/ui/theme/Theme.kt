package com.example.foliohaven.ui.theme

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

// FolioHaven Luxury Dark Scheme (Ground truth from replica/brand.md)
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryTerracotta,
    onPrimary = Color.White,
    primaryContainer = PrimaryPressed,
    onPrimaryContainer = PrimaryLight,
    secondary = PrimaryHover,
    onSecondary = Color.White,
    secondaryContainer = CardSurface,
    onSecondaryContainer = TextParchment,
    tertiary = GoldFoilDeboss,
    onTertiary = CanvasDark,
    background = CanvasDark,
    onBackground = TextParchment,
    surface = SurfaceEspresso,
    onSurface = TextParchment,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextLinen,
    outline = BorderCharcoal,
    outlineVariant = BorderSubtle
)

// FolioHaven Warm Sepia Light Scheme
private val LightColorScheme = lightColorScheme(
    primary = PrimaryTerracotta,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEDD5),
    onPrimaryContainer = PrimaryPressed,
    secondary = PrimaryHover,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3ECE0),
    onSecondaryContainer = TextDarkPrimary,
    tertiary = Color(0xFF9A7B38),
    onTertiary = Color.White,
    background = CanvasLight,
    onBackground = TextDarkPrimary,
    surface = SurfaceLight,
    onSurface = TextDarkPrimary,
    surfaceVariant = Color(0xFFEBE0D2),
    onSurfaceVariant = TextDarkSecondary,
    outline = BorderLight,
    outlineVariant = Color(0xFFE2D6C5)
)

@Composable
fun FolioHavenComposeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep brand identity consistent (no generic dynamic wallpaper override)
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
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
