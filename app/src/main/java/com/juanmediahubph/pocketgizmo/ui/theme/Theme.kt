package com.juanmediahubph.pocketgizmo.ui.theme

import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class ToolPalette(
    val container: Color,
    val accent: Color,
)

data class GizmoPalette(
    val torch: ToolPalette,
    val sos: ToolPalette,
    val screenLight: ToolPalette,
    val compass: ToolPalette,
    val level: ToolPalette,
    val stopwatch: ToolPalette,
    val isDark: Boolean,
)

val LightGizmoPalette = GizmoPalette(
    torch = ToolPalette(container = Color(0xFFFFF0C2), accent = Color(0xFF7A4F00)),
    sos = ToolPalette(container = Color(0xFFFFDAD6), accent = Color(0xFFA51C15)),
    screenLight = ToolPalette(container = Color(0xFFD6EEFF), accent = Color(0xFF005B8F)),
    compass = ToolPalette(container = Color(0xFFC9F2ED), accent = Color(0xFF005C5E)),
    level = ToolPalette(container = Color(0xFFDFF3D0), accent = Color(0xFF2F5E13)),
    stopwatch = ToolPalette(container = Color(0xFFEADDFF), accent = Color(0xFF5B3FA0)),
    isDark = false,
)

val DarkGizmoPalette = GizmoPalette(
    torch = ToolPalette(container = Color(0xFF4A3A0A), accent = Color(0xFFFFD27A)),
    sos = ToolPalette(container = Color(0xFF4A1E1A), accent = Color(0xFFFFB4AB)),
    screenLight = ToolPalette(container = Color(0xFF0C374D), accent = Color(0xFF8FD3FF)),
    compass = ToolPalette(container = Color(0xFF0E3B38), accent = Color(0xFF80D4D5)),
    level = ToolPalette(container = Color(0xFF223A16), accent = Color(0xFFB8E892)),
    stopwatch = ToolPalette(container = Color(0xFF322A4A), accent = Color(0xFFD0BCFF)),
    isDark = true,
)

val LocalGizmoPalette = staticCompositionLocalOf { LightGizmoPalette }

private val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    surfaceContainerLowest = LightSurfaceLowest,
    surfaceContainerLow = LightSurfaceLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceHigh,
    surfaceContainerHighest = LightSurfaceHighest,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,
    inversePrimary = LightInversePrimary,
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    surfaceContainerLowest = DarkSurfaceLowest,
    surfaceContainerLow = DarkSurfaceLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceHigh,
    surfaceContainerHighest = DarkSurfaceHighest,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    inversePrimary = DarkInversePrimary,
)

private val PocketShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(36.dp),
)

@Composable
fun PocketGizmoTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val palette = if (darkTheme) DarkGizmoPalette else LightGizmoPalette
    CompositionLocalProvider(LocalGizmoPalette provides palette) {
        androidx.compose.material3.MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = PocketTypography,
            shapes = PocketShapes,
            content = content,
        )
    }
}
