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

// FolioHaven Luxury Dark Scheme - Full Material Design 3 Specification
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryTerracotta,
    onPrimary = Color.White,
    primaryContainer = PrimaryPressed,
    onPrimaryContainer = PrimaryLight,
    inversePrimary = PrimaryHover,
    secondary = PrimaryHover,
    onSecondary = Color.White,
    secondaryContainer = CardSurface,
    onSecondaryContainer = TextParchment,
    tertiary = GoldFoilDeboss,
    onTertiary = CanvasDark,
    tertiaryContainer = Color(0xFF4A3E1F),
    onTertiaryContainer = Color(0xFFFDE68A),
    background = CanvasDark,
    onBackground = TextParchment,
    surface = SurfaceEspresso,
    onSurface = TextParchment,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextLinen,
    surfaceTint = PrimaryTerracotta,
    surfaceContainer = CardSurface,
    surfaceContainerLow = SurfaceEspresso,
    surfaceContainerHigh = CardSurfaceHover,
    surfaceContainerHighest = Color(0xFF38312C),
    outline = BorderCharcoal,
    outlineVariant = BorderSubtle,
    error = DangerCrimson,
    onError = Color.White,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
    inverseSurface = TextParchment,
    inverseOnSurface = CanvasDark
)

// FolioHaven Warm Sepia Light Scheme - Full Material Design 3 Specification
private val LightColorScheme = lightColorScheme(
    primary = PrimaryTerracotta,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEDD5),
    onPrimaryContainer = PrimaryPressed,
    inversePrimary = PrimaryHover,
    secondary = PrimaryHover,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3ECE0),
    onSecondaryContainer = TextDarkPrimary,
    tertiary = Color(0xFF9A7B38),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF78350F),
    background = CanvasLight,
    onBackground = TextDarkPrimary,
    surface = SurfaceLight,
    onSurface = TextDarkPrimary,
    surfaceVariant = Color(0xFFEBE0D2),
    onSurfaceVariant = TextDarkSecondary,
    surfaceTint = PrimaryTerracotta,
    surfaceContainer = Color(0xFFF7EFE4),
    surfaceContainerLow = SurfaceLight,
    surfaceContainerHigh = Color(0xFFEFE4D6),
    surfaceContainerHighest = Color(0xFFE4D6C4),
    outline = BorderLight,
    outlineVariant = Color(0xFFE2D6C5),
    error = DangerCrimson,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
    inverseSurface = CanvasDark,
    inverseOnSurface = TextParchment
)

@Composable
fun FolioHavenComposeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
        shapes = FolioHavenShapes,
        content = content
    )
}
