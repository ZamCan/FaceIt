package com.zamcan.faceit.ui.theme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
@Composable
fun FaceItTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(
        primary = Color(0xFFA78BFA), secondary = Color(0xFF76DCCB),
        background = Color(0xFF111318), surface = Color(0xFF252832),
        onPrimary = Color(0xFF111318), onBackground = Color(0xFFE9E8EF), onSurface = Color(0xFFE9E8EF)
    ), content = content)
}
