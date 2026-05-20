package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueLight,
    secondary = SecondaryBlue,
    tertiary = PriorityMedium,
    background = OneUIBackgroundDark,
    surface = OneUISurfaceDark,
    onPrimary = OneUIBackgroundDark,
    onSecondary = OneUITextPrimaryDark,
    onBackground = OneUITextPrimaryDark,
    onSurface = OneUITextPrimaryDark,
    surfaceVariant = OneUISurfaceDark,
    onSurfaceVariant = OneUITextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryBlue,
    tertiary = PriorityMedium,
    background = OneUIBackground,
    surface = OneUISurface,
    onPrimary = OneUISurface,
    onSecondary = OneUITextPrimary,
    onBackground = OneUITextPrimary,
    onSurface = OneUITextPrimary,
    surfaceVariant = OneUISurface,
    onSurfaceVariant = OneUITextSecondary
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "system",
    themeColor: String = "blue",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    
    val useDynamicColor = (themeColor == "dynamic")
    
    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> {
            when (themeColor) {
                "violet" -> darkColorScheme(
                    primary = Color(0xFFA182FF),
                    secondary = Color(0xFFC4B2FF),
                    tertiary = PriorityMedium,
                    background = Color(0xFF0C0A0F),
                    surface = Color(0xFF16131C),
                    onPrimary = Color(0xFF000000),
                    onSecondary = Color(0xFFFFFFFF),
                    onBackground = Color(0xFFFFFFFF),
                    onSurface = Color(0xFFFFFFFF),
                    surfaceVariant = Color(0xFF16131C),
                    onSurfaceVariant = Color(0xFFCCCCCC)
                )
                "mint" -> darkColorScheme(
                    primary = Color(0xFF5FF3B4),
                    secondary = Color(0xFF9AFFD4),
                    tertiary = PriorityMedium,
                    background = Color(0xFF070A09),
                    surface = Color(0xFF111714),
                    onPrimary = Color(0xFF000000),
                    onSecondary = Color(0xFFFFFFFF),
                    onBackground = Color(0xFFFFFFFF),
                    onSurface = Color(0xFFFFFFFF),
                    surfaceVariant = Color(0xFF111714),
                    onSurfaceVariant = Color(0xFFBBE6CE)
                )
                "peach" -> darkColorScheme(
                    primary = Color(0xFFFF8B7D),
                    secondary = Color(0xFFFFB4AC),
                    tertiary = PriorityMedium,
                    background = Color(0xFF0D0A0A),
                    surface = Color(0xFF181211),
                    onPrimary = Color(0xFF000000),
                    onSecondary = Color(0xFFFFFFFF),
                    onBackground = Color(0xFFFFFFFF),
                    onSurface = Color(0xFFFFFFFF),
                    surfaceVariant = Color(0xFF181211),
                    onSurfaceVariant = Color(0xFFE6C4C1)
                )
                else -> DarkColorScheme
            }
        }
        else -> {
            when (themeColor) {
                "violet" -> lightColorScheme(
                    primary = Color(0xFF673AB7),
                    secondary = Color(0xFF512DA8),
                    tertiary = PriorityMedium,
                    background = Color(0xFFF6F3FC),
                    surface = Color(0xFFFFFFFF),
                    onPrimary = Color(0xFFFFFFFF),
                    onSecondary = Color(0xFF000000),
                    onBackground = Color(0xFF000000),
                    onSurface = Color(0xFF000000),
                    surfaceVariant = Color(0xFFFFFFFF),
                    onSurfaceVariant = Color(0xFF4A4A4A)
                )
                "mint" -> lightColorScheme(
                    primary = Color(0xFF00897B),
                    secondary = Color(0xFF00695C),
                    tertiary = PriorityMedium,
                    background = Color(0xFFEEF7F5),
                    surface = Color(0xFFFFFFFF),
                    onPrimary = Color(0xFFFFFFFF),
                    onSecondary = Color(0xFF000000),
                    onBackground = Color(0xFF000000),
                    onSurface = Color(0xFF000000),
                    surfaceVariant = Color(0xFFFFFFFF),
                    onSurfaceVariant = Color(0xFF4A4A4A)
                )
                "peach" -> lightColorScheme(
                    primary = Color(0xFFD84315),
                    secondary = Color(0xFFBF360C),
                    tertiary = PriorityMedium,
                    background = Color(0xFFFFF4F2),
                    surface = Color(0xFFFFFFFF),
                    onPrimary = Color(0xFFFFFFFF),
                    onSecondary = Color(0xFF000000),
                    onBackground = Color(0xFF000000),
                    onSurface = Color(0xFF000000),
                    surfaceVariant = Color(0xFFFFFFFF),
                    onSurfaceVariant = Color(0xFF4A4A4A)
                )
                else -> LightColorScheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
