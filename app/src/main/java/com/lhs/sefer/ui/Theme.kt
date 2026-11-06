package com.lhs.sefer.ui

import android.app.Activity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.lhs.sefer.data.AppTheme

fun colorSchemeFor(theme: AppTheme): ColorScheme = when (theme) {
    // Warm paper, royal blue accent
    AppTheme.LIGHT -> lightColorScheme(
        primary = Color(0xFF1F4E9C),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD9E5FA),
        onPrimaryContainer = Color(0xFF0B2A5B),
        secondary = Color(0xFF8A6D1F),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF6E7B4),
        onSecondaryContainer = Color(0xFF3A2C00),
        background = Color(0xFFFBF8F1),
        onBackground = Color(0xFF1C1B1A),
        surface = Color(0xFFFBF8F1),
        onSurface = Color(0xFF1C1B1A),
        surfaceVariant = Color(0xFFE9E4D8),
        onSurfaceVariant = Color(0xFF4A473F),
        outline = Color(0xFF7B766A),
        outlineVariant = Color(0xFFCCC6B8),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF6F2E9),
        surfaceContainer = Color(0xFFF1EDE3),
        surfaceContainerHigh = Color(0xFFECE7DC),
        surfaceContainerHighest = Color(0xFFE6E1D5),
    )
    // Near-black, soft blue accent
    AppTheme.DARK -> darkColorScheme(
        primary = Color(0xFF9DBDF2),
        onPrimary = Color(0xFF06285A),
        primaryContainer = Color(0xFF1F3F72),
        onPrimaryContainer = Color(0xFFD9E5FA),
        secondary = Color(0xFFE3C46B),
        onSecondary = Color(0xFF3A2C00),
        secondaryContainer = Color(0xFF55440F),
        onSecondaryContainer = Color(0xFFF6E7B4),
        background = Color(0xFF111214),
        onBackground = Color(0xFFE4E2DE),
        surface = Color(0xFF111214),
        onSurface = Color(0xFFE4E2DE),
        surfaceVariant = Color(0xFF2A2B2F),
        onSurfaceVariant = Color(0xFFC6C5C0),
        outline = Color(0xFF8F8E89),
        outlineVariant = Color(0xFF3E3F43),
        surfaceContainerLowest = Color(0xFF0B0C0D),
        surfaceContainerLow = Color(0xFF17181B),
        surfaceContainer = Color(0xFF1C1D20),
        surfaceContainerHigh = Color(0xFF26272A),
        surfaceContainerHighest = Color(0xFF313235),
    )
    // Dim, warm purple twilight with an amber accent (easy on the eyes at night)
    AppTheme.DUSK -> darkColorScheme(
        primary = Color(0xFFF0B27A),
        onPrimary = Color(0xFF3A1D00),
        primaryContainer = Color(0xFF5E3D33),
        onPrimaryContainer = Color(0xFFFFDCC2),
        secondary = Color(0xFFC9ABE8),
        onSecondary = Color(0xFF2E1A47),
        secondaryContainer = Color(0xFF4A3A63),
        onSecondaryContainer = Color(0xFFEBDDFF),
        background = Color(0xFF2A2433),
        onBackground = Color(0xFFEADFEF),
        surface = Color(0xFF2A2433),
        onSurface = Color(0xFFEADFEF),
        surfaceVariant = Color(0xFF3B3346),
        onSurfaceVariant = Color(0xFFD0C4D6),
        outline = Color(0xFF9A8EA3),
        outlineVariant = Color(0xFF4A4054),
        surfaceContainerLowest = Color(0xFF221D2A),
        surfaceContainerLow = Color(0xFF2F2839),
        surfaceContainer = Color(0xFF342D3F),
        surfaceContainerHigh = Color(0xFF3E3649),
        surfaceContainerHighest = Color(0xFF494054),
    )
    // Pale blue-grey with a teal accent
    AppTheme.COOL -> lightColorScheme(
        primary = Color(0xFF0B6E7F),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFC6EBF2),
        onPrimaryContainer = Color(0xFF00363F),
        secondary = Color(0xFF3D5F9A),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD3E0FA),
        onSecondaryContainer = Color(0xFF0B2554),
        background = Color(0xFFEAF2F6),
        onBackground = Color(0xFF14262E),
        surface = Color(0xFFEAF2F6),
        onSurface = Color(0xFF14262E),
        surfaceVariant = Color(0xFFD6E4EA),
        onSurfaceVariant = Color(0xFF3A4E57),
        outline = Color(0xFF6B8089),
        outlineVariant = Color(0xFFB9CCD4),
        surfaceContainerLowest = Color(0xFFF6FAFC),
        surfaceContainerLow = Color(0xFFE3EDF2),
        surfaceContainer = Color(0xFFDCE8EE),
        surfaceContainerHigh = Color(0xFFD3E1E8),
        surfaceContainerHighest = Color(0xFFCBDBE3),
    )
}

@Composable
fun LhsTheme(theme: AppTheme, content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !theme.dark
            controller.isAppearanceLightNavigationBars = !theme.dark
        }
    }
    MaterialTheme(colorScheme = colorSchemeFor(theme), content = content)
}
