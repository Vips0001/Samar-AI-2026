package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import java.util.Calendar

enum class AppThemeMode(val title: String, val description: String) {
    SYSTEM("System Default", "Follows device dark/light setting"),
    TIME_BASED("Auto (Day / Night)", "Light from 7 AM - 7 PM, Dark at night"),
    SAMAR_BRANDED("Samar Cosmic", "Customizable accent with cosmic obsidian dark"),
    DARK("Pure Dark", "Deep midnight slate colors"),
    LIGHT("Clean Light", "Crisp, bright high-contrast theme")
}

enum class ChatFontSize(val label: String, val scaleFactor: Float) {
    SMALL("Small", 0.85f),
    MEDIUM("Medium (Default)", 1.0f),
    LARGE("Large", 1.15f),
    EXTRA_LARGE("Extra Large", 1.30f)
}

val SamarAccentPresets = listOf(
    Color(0xFF00F2FE) to "Electric Cyan",
    Color(0xFF6366F1) to "Neon Indigo",
    Color(0xFF10B981) to "Emerald Teal",
    Color(0xFFF43F5E) to "Cyber Rose",
    Color(0xFFF59E0B) to "Solar Amber",
    Color(0xFFA855F7) to "Cosmic Purple",
    Color(0xFF06B6D4) to "Aqua Marine",
    Color(0xFFEC4899) to "Sakura Pink"
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

private val SamarColorScheme = darkColorScheme(
    primary = SamarPrimary,
    onPrimary = SamarOnPrimary,
    primaryContainer = SamarPrimaryContainer,
    onPrimaryContainer = SamarOnPrimaryContainer,
    secondary = SamarSecondary,
    onSecondary = SamarOnSecondary,
    secondaryContainer = SamarSecondaryContainer,
    onSecondaryContainer = SamarOnSecondaryContainer,
    tertiary = SamarTertiary,
    background = SamarBackground,
    onBackground = SamarOnBackground,
    surface = SamarSurface,
    onSurface = SamarOnSurface,
    surfaceVariant = SamarSurfaceVariant,
    onSurfaceVariant = SamarOnSurfaceVariant,
    outline = SamarOutline
)

@Composable
fun SamarTheme(
    themeMode: AppThemeMode = AppThemeMode.SAMAR_BRANDED,
    customAccentColor: Color = SamarPrimary,
    fontSize: ChatFontSize = ChatFontSize.MEDIUM,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()

    val isNightTime = {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        hour >= 19 || hour < 7
    }()

    val colorScheme: ColorScheme = when (themeMode) {
        AppThemeMode.SYSTEM -> {
            if (isSystemDark) DarkColorScheme else LightColorScheme
        }
        AppThemeMode.TIME_BASED -> {
            if (isNightTime) DarkColorScheme else LightColorScheme
        }
        AppThemeMode.LIGHT -> LightColorScheme
        AppThemeMode.DARK -> DarkColorScheme
        AppThemeMode.SAMAR_BRANDED -> {
            SamarColorScheme.copy(
                primary = customAccentColor,
                primaryContainer = customAccentColor.copy(alpha = 0.2f),
                onPrimaryContainer = customAccentColor
            )
        }
    }

    // Scale typography according to user preference
    val scale = fontSize.scaleFactor
    val baseTypography = Typography
    val scaledTypography = Typography(
        displayLarge = baseTypography.displayLarge.copy(fontSize = (baseTypography.displayLarge.fontSize.value * scale).sp),
        displayMedium = baseTypography.displayMedium.copy(fontSize = (baseTypography.displayMedium.fontSize.value * scale).sp),
        displaySmall = baseTypography.displaySmall.copy(fontSize = (baseTypography.displaySmall.fontSize.value * scale).sp),
        headlineLarge = baseTypography.headlineLarge.copy(fontSize = (baseTypography.headlineLarge.fontSize.value * scale).sp),
        headlineMedium = baseTypography.headlineMedium.copy(fontSize = (baseTypography.headlineMedium.fontSize.value * scale).sp),
        headlineSmall = baseTypography.headlineSmall.copy(fontSize = (baseTypography.headlineSmall.fontSize.value * scale).sp),
        titleLarge = baseTypography.titleLarge.copy(fontSize = (baseTypography.titleLarge.fontSize.value * scale).sp),
        titleMedium = baseTypography.titleMedium.copy(fontSize = (baseTypography.titleMedium.fontSize.value * scale).sp),
        titleSmall = baseTypography.titleSmall.copy(fontSize = (baseTypography.titleSmall.fontSize.value * scale).sp),
        bodyLarge = baseTypography.bodyLarge.copy(fontSize = (baseTypography.bodyLarge.fontSize.value * scale).sp),
        bodyMedium = baseTypography.bodyMedium.copy(fontSize = (baseTypography.bodyMedium.fontSize.value * scale).sp),
        bodySmall = baseTypography.bodySmall.copy(fontSize = (baseTypography.bodySmall.fontSize.value * scale).sp),
        labelLarge = baseTypography.labelLarge.copy(fontSize = (baseTypography.labelLarge.fontSize.value * scale).sp),
        labelMedium = baseTypography.labelMedium.copy(fontSize = (baseTypography.labelMedium.fontSize.value * scale).sp),
        labelSmall = baseTypography.labelSmall.copy(fontSize = (baseTypography.labelSmall.fontSize.value * scale).sp)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content
    )
}
