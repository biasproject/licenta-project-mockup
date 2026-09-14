package com.example.flowpeak.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

// ── Structura de culori personalizată ──────────────────────────
@Stable
class FlowPeakColors(
    val background: Color,
    val surface1: Color,
    val surface2: Color,
    val surface3: Color,
    val primary: Color,
    val accentPink: Color,
    val accentBlue: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val border: Color,
    val isDark: Boolean
)

val LightColors = FlowPeakColors(
    background = LightBg,
    surface1 = LightSurface1,
    surface2 = LightSurface2,
    surface3 = LightSurface3,
    primary = LightAccentOrange,
    accentPink = LightAccentPink,
    accentBlue = LightAccentBlue,
    textPrimary = LightTextPrimary,
    textMuted = LightTextMuted,
    border = LightBorder,
    isDark = false
)

val DarkColors = FlowPeakColors(
    background = DarkBg,
    surface1 = DarkSurface1,
    surface2 = DarkSurface2,
    surface3 = DarkSurface3,
    primary = DarkAccentPurple,
    accentPink = DarkAccentPink,
    accentBlue = DarkAccentBlue,
    textPrimary = DarkTextPrimary,
    textMuted = DarkTextMuted,
    border = DarkBorder,
    isDark = true
)

val LocalFlowPeakColors = staticCompositionLocalOf { DarkColors }

object FlowPeakTheme {
    val colors: FlowPeakColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFlowPeakColors.current
}

@Composable
fun FlowPeakTheme(
    isDarkMode: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (isDarkMode) DarkColors else LightColors

    val materialColorScheme = if (isDarkMode) {
        darkColorScheme(
            primary = colors.primary,
            secondary = colors.accentBlue,
            tertiary = colors.accentPink,
            background = colors.background,
            surface = colors.surface1,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            secondary = colors.accentBlue,
            tertiary = colors.accentPink,
            background = colors.background,
            surface = colors.surface1,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary
        )
    }

    CompositionLocalProvider(
        LocalFlowPeakColors provides colors
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = Typography(),
            content = content
        )
    }
}

