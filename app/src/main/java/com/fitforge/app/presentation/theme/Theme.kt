package com.fitforge.app.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ForgeRed,
    onPrimary = PureWhite,
    secondary = ElectricBlue,
    onSecondary = PureWhite,
    tertiary = VictoryGreen,
    onTertiary = IronBlack,
    background = IronBlack,
    onBackground = PureWhite,
    surface = Color(0xFF2D2D2F),
    onSurface = PureWhite,
    error = Error,
    onError = PureWhite
)

private val LightColorScheme = lightColorScheme(
    primary = ForgeRed,
    onPrimary = PureWhite,
    secondary = ElectricBlue,
    onSecondary = PureWhite,
    tertiary = VictoryGreen,
    onTertiary = IronBlack,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    outline = Outline,
    error = Error,
    onError = PureWhite
)

@Composable
fun FitForgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
