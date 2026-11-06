package com.medspace.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import com.medspace.app.R

val AppFont = FontFamily(
    Font(R.font.tajawal_regular, FontWeight.Normal),
    Font(R.font.tajawal_medium, FontWeight.Medium),
    Font(R.font.tajawal_bold, FontWeight.Bold)
)

private val base = Typography()
private val AppTypography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = AppFont),
    displayMedium = base.displayMedium.copy(fontFamily = AppFont),
    displaySmall = base.displaySmall.copy(fontFamily = AppFont),
    headlineLarge = base.headlineLarge.copy(fontFamily = AppFont),
    headlineMedium = base.headlineMedium.copy(fontFamily = AppFont),
    headlineSmall = base.headlineSmall.copy(fontFamily = AppFont),
    titleLarge = base.titleLarge.copy(fontFamily = AppFont),
    titleMedium = base.titleMedium.copy(fontFamily = AppFont),
    titleSmall = base.titleSmall.copy(fontFamily = AppFont),
    bodyLarge = base.bodyLarge.copy(fontFamily = AppFont),
    bodyMedium = base.bodyMedium.copy(fontFamily = AppFont),
    bodySmall = base.bodySmall.copy(fontFamily = AppFont),
    labelLarge = base.labelLarge.copy(fontFamily = AppFont),
    labelMedium = base.labelMedium.copy(fontFamily = AppFont),
    labelSmall = base.labelSmall.copy(fontFamily = AppFont)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    secondary = Color(0xFF7C3AED),
    background = Color(0xFFF5F7FB),
    surface = Color.White,
    onBackground = Color(0xFF1E293B),
    onSurface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFFE8ECF6),
    onSurfaceVariant = Color(0xFF475569),
    error = Color(0xFFDC2626)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8B8CF8),
    onPrimary = Color(0xFF1B1B4B),
    secondary = Color(0xFFA78BFA),
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF273449),
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = Color(0xFFF87171)
)

@Composable
fun MedSpaceTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = AppTypography
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                content()
            }
        }
    }
}
