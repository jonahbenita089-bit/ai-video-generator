package com.videogpt.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF7C5CFF),
    secondary = androidx.compose.ui.graphics.Color(0xFF1ED7B5),
    background = androidx.compose.ui.graphics.Color(0xFF0F1117),
    surface = androidx.compose.ui.graphics.Color(0xFF171B24),
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    onBackground = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color.White,
)

private val LightColors = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF6A5CFF),
    secondary = androidx.compose.ui.graphics.Color(0xFF1DBA9C),
    background = androidx.compose.ui.graphics.Color(0xFFF5F7FA),
    surface = androidx.compose.ui.graphics.Color.White,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    onBackground = androidx.compose.ui.graphics.Color(0xFF111827),
    onSurface = androidx.compose.ui.graphics.Color(0xFF111827),
)

@Composable
fun VideoAiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
