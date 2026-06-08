package com.neuro.photostudio.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.neuro.photostudio.data.ThemeMode

private val AppTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp)
)

private fun lightSchemeFor(accent: Color) = lightColorScheme(
    primary = accent,
    onPrimary = Color.White,
    primaryContainer = accent.copy(alpha = 0.16f).compositeOver(Color.White),
    onPrimaryContainer = darken(accent, 0.4f),
    secondary = accent.copy(alpha = 0.85f),
    background = Color(0xFFF7F6FB),
    onBackground = Color(0xFF1A1A22),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A22),
    surfaceVariant = Color(0xFFEDEBF4),
    onSurfaceVariant = Color(0xFF4A4A55),
    outline = Color(0xFFCAC7D6)
)

private fun darkSchemeFor(accent: Color) = darkColorScheme(
    primary = accent,
    onPrimary = Color.White,
    primaryContainer = darken(accent, 0.45f),
    onPrimaryContainer = lighten(accent, 0.4f),
    secondary = lighten(accent, 0.15f),
    background = Color(0xFF121018),
    onBackground = Color(0xFFEDEAF5),
    surface = Color(0xFF1B1924),
    onSurface = Color(0xFFEDEAF5),
    surfaceVariant = Color(0xFF2A2735),
    onSurfaceVariant = Color(0xFFBDB8CC),
    outline = Color(0xFF45424F)
)

@Composable
fun NeuroTheme(
    themeMode: ThemeMode,
    accentArgb: Long,
    dynamicColor: Boolean,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val accent = Color(accentArgb)
    val context = LocalContext.current

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> darkSchemeFor(accent)
        else -> lightSchemeFor(accent)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            val lightIcons = colorScheme.background.luminance() > 0.5f
            controller.isAppearanceLightStatusBars = lightIcons
            controller.isAppearanceLightNavigationBars = colorScheme.surface.luminance() > 0.5f
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

// ---- helpers ----
private fun Color.compositeOver(background: Color): Color {
    val a = alpha + background.alpha * (1 - alpha)
    if (a == 0f) return Color.Transparent
    val r = (red * alpha + background.red * background.alpha * (1 - alpha)) / a
    val g = (green * alpha + background.green * background.alpha * (1 - alpha)) / a
    val b = (blue * alpha + background.blue * background.alpha * (1 - alpha)) / a
    return Color(r, g, b, a)
}

private fun darken(color: Color, factor: Float): Color = Color(
    red = color.red * (1 - factor),
    green = color.green * (1 - factor),
    blue = color.blue * (1 - factor),
    alpha = color.alpha
)

private fun lighten(color: Color, factor: Float): Color = Color(
    red = color.red + (1 - color.red) * factor,
    green = color.green + (1 - color.green) * factor,
    blue = color.blue + (1 - color.blue) * factor,
    alpha = color.alpha
)
