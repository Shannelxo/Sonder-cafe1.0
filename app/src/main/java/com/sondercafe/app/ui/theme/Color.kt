package com.sondercafe.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Café palette: coffee browns on cream, with a muted green as the secondary colour.
// Text and button colour pairs were picked to meet WCAG AA contrast (4.5:1 for body text).

private val Coffee = Color(0xFF6F4E37)
private val Espresso = Color(0xFF3E2723)
private val Latte = Color(0xFFE2BFA0)
private val Cream = Color(0xFFFFF8F0)
private val Foam = Color(0xFFF2DFCC)
private val Sage = Color(0xFF4F6F55)
private val SageLight = Color(0xFFB5D1B9)

val LightColors = lightColorScheme(
    primary = Coffee,
    onPrimary = Color.White,
    primaryContainer = Foam,
    onPrimaryContainer = Color(0xFF2B1A10),
    secondary = Sage,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E8D9),
    onSecondaryContainer = Color(0xFF0D2013),
    background = Cream,
    onBackground = Espresso,
    surface = Cream,
    onSurface = Espresso,
    surfaceVariant = Color(0xFFEFE3D6),
    onSurfaceVariant = Color(0xFF51443A),
    outline = Color(0xFF84746A),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B)
)

val DarkColors = darkColorScheme(
    primary = Latte,
    onPrimary = Espresso,
    primaryContainer = Color(0xFF56392A),
    onPrimaryContainer = Foam,
    secondary = SageLight,
    onSecondary = Color(0xFF213527),
    secondaryContainer = Color(0xFF374C3C),
    onSecondaryContainer = Color(0xFFD7E8D9),
    background = Color(0xFF1E1916),
    onBackground = Color(0xFFECE0D8),
    surface = Color(0xFF1E1916),
    onSurface = Color(0xFFECE0D8),
    surfaceVariant = Color(0xFF51443A),
    onSurfaceVariant = Color(0xFFD6C3B6),
    outline = Color(0xFF9F8D82),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC)
)
