package com.sondercafe.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * App theme. Follows the system light/dark setting. Dynamic (wallpaper-based) colour is left
 * off on purpose, so the café's brand colours look the same on every phone.
 */
@Composable
fun SonderCafeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = SonderTypography,
        content = content
    )
}
