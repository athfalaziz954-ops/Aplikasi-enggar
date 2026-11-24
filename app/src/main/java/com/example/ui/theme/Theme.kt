package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val WarungLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldSoft,
    onPrimaryContainer = EmeraldDeep,
    secondary = SkyAccent,
    onSecondary = Color.White,
    secondaryContainer = SkyGlow,
    onSecondaryContainer = Slate900,
    tertiary = AmberStatus,
    onTertiary = Color.White,
    tertiaryContainer = AmberSoft,
    onTertiaryContainer = Slate900,
    error = CoralDanger,
    onError = Color.White,
    errorContainer = CoralSoft,
    onErrorContainer = CoralDanger,
    background = PearlWhite,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = SoftIce,
    onSurfaceVariant = Slate700,
    outline = GlassBorderBright,
    outlineVariant = Slate400.copy(alpha = 0.3f)
)

val WarungShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun WarungKasirTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WarungLightColorScheme,
        typography = WarungTypography,
        shapes = WarungShapes,
        content = content
    )
}
