package com.zamcan.faceit.ui.theme

import androidx.compose.ui.graphics.Color
import com.zamcan.faceit.themes.FaceTheme
import com.zamcan.faceit.themes.FaceThemes

data class FaceItThemePreset(
    val id: String,
    val label: String,
    val accentLight: Color,
    val accentDark: Color,
    val secondaryLight: Color,
    val secondaryDark: Color,
    val tertiaryLight: Color,
    val tertiaryDark: Color
)

object FaceItThemePresets {
    const val DEFAULT_ID = "theme-default"

    val all = listOf(
        FaceItThemePreset(DEFAULT_ID, "Face It", Color(0xFF5A3FD0), Color(0xFFC3B0FF), Color(0xFF0F7A6B), Color(0xFF76DCCB), Color(0xFF9A5B22), Color(0xFFF0C4A2)),
        FaceItThemePreset("theme-pearl-bloom", "Pearl Bloom", Color(0xFF98516F), Color(0xFFE7A9C0), Color(0xFF8A6475), Color(0xFFF0C9D6), Color(0xFF9A6B4B), Color(0xFFEBC4A8)),
        FaceItThemePreset("theme-cat-garden", "Cat Garden", Color(0xFF9A5738), Color(0xFFFFB887), Color(0xFF557C69), Color(0xFF9AD6B5), Color(0xFF80619A), Color(0xFFCDB6EA)),
        FaceItThemePreset("theme-niqab-atelier", "Niqab Atelier", Color(0xFF79556B), Color(0xFFD8A9C2), Color(0xFF746276), Color(0xFFBCA5C5), Color(0xFF987A62), Color(0xFFD8BDA4)),
        FaceItThemePreset("theme-rose-glass", "Rose Glass", Color(0xFF9C526D), Color(0xFFFFB3CA), Color(0xFF8B6A82), Color(0xFFE5B9D7), Color(0xFF8A6A4D), Color(0xFFE7C5A7)),
        FaceItThemePreset("theme-obsidian", "Obsidian", Color(0xFF55427F), Color(0xFFC6B2F2), Color(0xFF426C68), Color(0xFF86CFC1), Color(0xFF8A6447), Color(0xFFE5B994)),
        FaceItThemePreset("feminine-cat-garden", "Cat Garden", Color(0xFF9A5738), Color(0xFFFFB887), Color(0xFF557C69), Color(0xFF9AD6B5), Color(0xFF80619A), Color(0xFFCDB6EA)),
        FaceItThemePreset("feminine-petal-pop", "Petal Pop", Color(0xFF9C526D), Color(0xFFFFB3CA), Color(0xFF8B6A82), Color(0xFFE5B9D7), Color(0xFF8A6A4D), Color(0xFFE7C5A7)),
        FaceItThemePreset("feminine-pearl-atelier", "Pearl Atelier", Color(0xFF98516F), Color(0xFFE7A9C0), Color(0xFF8A6475), Color(0xFFF0C9D6), Color(0xFF9A6B4B), Color(0xFFEBC4A8)),
        FaceItThemePreset("feminine-lilac-luxe", "Lilac Luxe", Color(0xFF5A3FD0), Color(0xFFC3B0FF), Color(0xFF6D638E), Color(0xFFBCAFE2), Color(0xFF9A6B4B), Color(0xFFEBC4A8))
    )

    /**
     * Every complete theme in [FaceThemes] also becomes an app palette, so applying
     * a theme in the style studio really changes Face It's own surfaces. Dark
     * presets use the theme accent as-is; light presets use a darkened accent so
     * text on a pale surface keeps its contrast.
     */
    private val themePresets: List<FaceItThemePreset> = FaceThemes.all.map { theme ->
        FaceItThemePreset(
            id = theme.id,
            label = theme.name,
            accentLight = darken(theme.accent, 0.42f),
            accentDark = Color(theme.accent),
            secondaryLight = darken(theme.accentSecondary, 0.45f),
            secondaryDark = Color(theme.accentSecondary),
            tertiaryLight = darken(theme.border, 0.55f),
            tertiaryDark = Color(theme.border)
        )
    }

    private fun darken(argb: Long, amount: Float): Color {
        val r = ((argb shr 16) and 0xFF) * (1f - amount)
        val g = ((argb shr 8) and 0xFF) * (1f - amount)
        val b = (argb and 0xFF) * (1f - amount)
        return Color(r.toInt(), g.toInt(), b.toInt())
    }

    fun find(id: String): FaceItThemePreset = findOrNull(id) ?: all.first()
    fun findOrNull(id: String): FaceItThemePreset? =
        all.firstOrNull { it.id == id } ?: themePresets.firstOrNull { it.id == id }

    /** Looks up the preset for a [FaceTheme], used when a theme is applied. */
    fun forTheme(theme: FaceTheme): FaceItThemePreset =
        themePresets.firstOrNull { it.id == theme.id } ?: find(DEFAULT_ID)
}
