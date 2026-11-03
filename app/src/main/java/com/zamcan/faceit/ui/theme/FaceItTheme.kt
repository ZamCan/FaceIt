package com.zamcan.faceit.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.zamcan.faceit.data.EnvironmentStore
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun FaceItTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences("faceit_personalisation_studio", Context.MODE_PRIVATE)
    }
    var presetId by remember {
        mutableStateOf(preferences.getString("faceit_theme_preset", FaceItThemePresets.DEFAULT_ID) ?: FaceItThemePresets.DEFAULT_ID)
    }
    // If the environment records a theme the palette is not showing, the environment
    // wins: a stale preference must never leave the app in the wrong colours.
    LaunchedEffect(Unit) {
        val environmentTheme = EnvironmentStore(context).load().themeId
        if (environmentTheme != FaceItThemePresets.DEFAULT_ID &&
            FaceItThemePresets.findOrNull(environmentTheme) != null &&
            environmentTheme != presetId
        ) {
            preferences.edit().putString("faceit_theme_preset", environmentTheme).apply()
            presetId = environmentTheme
        }
    }
    val preset = FaceItThemePresets.find(presetId)
    val accent = if (darkTheme) preset.accentDark else preset.accentLight
    val secondary = if (darkTheme) preset.secondaryDark else preset.secondaryLight
    val tertiary = if (darkTheme) preset.tertiaryDark else preset.tertiaryLight
    val baseColors = if (darkTheme) DarkFaceItColors else LightFaceItColors
    val colors = baseColors.copy(violet = accent, mint = secondary, peach = tertiary, ice = secondary)
    val baseScheme = if (darkTheme) FaceItDarkScheme else FaceItLightScheme
    val scheme = baseScheme.copy(
        primary = accent,
        primaryContainer = accent.copy(alpha = if (darkTheme) .28f else .16f),
        secondary = secondary,
        secondaryContainer = secondary.copy(alpha = if (darkTheme) .24f else .18f),
        tertiary = tertiary
    )
    val setPreset: (String) -> Unit = { id ->
        if (FaceItThemePresets.findOrNull(id) != null) {
            preferences.edit().putString("faceit_theme_preset", id).apply()
            presetId = id
        }
    }
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window ?: return@SideEffect
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalFaceItColors provides colors,
        LocalFaceItThemePresetId provides preset.id,
        LocalSetFaceItThemePresetId provides setPreset
    ) {
        MaterialTheme(colorScheme = scheme, typography = FaceItTypography, shapes = FaceItShapes, content = content)
    }
}

val LocalFaceItThemePresetId = staticCompositionLocalOf { FaceItThemePresets.DEFAULT_ID }
val LocalSetFaceItThemePresetId = staticCompositionLocalOf<(String) -> Unit> { {} }

private fun Context.findActivity(): Activity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return current as? Activity
}
