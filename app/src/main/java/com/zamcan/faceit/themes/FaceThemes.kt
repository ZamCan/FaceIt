package com.zamcan.faceit.themes

import com.zamcan.faceit.graphics.IconGlyph
import com.zamcan.faceit.graphics.IconTile
import com.zamcan.faceit.keyboard.KeyboardDesignCatalogue

/**
 * Complete themes: one selection that drives the wallpaper, the widget finish,
 * the icon set, the keyboard design, the charging animation and the frame
 * treatment used when browsing apps.
 *
 * AGENTS.md §5 requires that a user can change one layer without losing the
 * others, so a theme is a *bundle of suggestions*, not a locked set: every field
 * here maps to an independently overridable setting in [com.zamcan.faceit.data.PersonalEnvironment].
 *
 * Colour rules, applied to every theme below and checked by unit test:
 *  - body text on the surface meets WCAG AA (>= 4.5:1);
 *  - accent against the surface meets the non-text threshold (>= 3.0:1);
 *  - the wallpaper's base tone is dark enough that white status-bar icons read,
 *    or the theme declares a light surface and uses dark icons.
 */
data class FaceTheme(
    val id: String,
    val name: String,
    /** Family shown as a grouping chip in the gallery. */
    val family: String,
    val description: String,

    // ---- surfaces ----
    val background: Long,
    val surface: Long,
    val surfaceRaised: Long,
    val border: Long,
    val textPrimary: Long,
    val textSecondary: Long,
    val accent: Long,
    val accentSecondary: Long,

    // ---- paired content ----
    /** Curated wallpaper id from the shipped pack. */
    val wallpaperId: String,
    /** Keyboard design id from [KeyboardDesignCatalogue]; never a system keyboard. */
    val keyboardDesignId: String,
    val iconTile: IconTile,
    val iconTileColor: Long,
    val iconGlyphColor: Long,
    val widgetStyleId: String,
    val chargingStyleId: String,
    /** Frame drawn behind the app list. */
    val veilStyle: VeilStyle,
    val veilColor: Long,
    /** Dark surfaces expect light status-bar icons. */
    val isDark: Boolean
) {
    /** Snapshot of the palette for contrast checks and previews. */
    val contrastOnSurface: Double get() = ThemeContrast.ratio(textPrimary, surface)
    val contrastAccentOnSurface: Double get() = ThemeContrast.ratio(accent, surface)
    val contrastSecondaryOnSurface: Double get() = ThemeContrast.ratio(textSecondary, surface)
}

/**
 * The frame treatment used where the app list is displayed.
 *
 * These are abstract drape and fold forms — layered translucent curves that
 * frame the grid and can be recoloured. No figure, face or clothing is depicted;
 * the motif is shading and geometry, which is what a personalisation frame needs
 * to be and what keeps it respectful to draw.
 */
enum class VeilStyle(val label: String, val description: String) {
    NONE("None", "The app list sits directly on your wallpaper."),
    SOFT_DRAPE("Soft drape", "Two translucent folds sweep in from the top corners and settle behind the grid."),
    VEIL_FRAME("Veil frame", "A single arched curtain of colour frames the app list on three sides."),
    LAYERED_FOLD("Layered fold", "Three offset folds overlap like fabric, each a step lighter than the last."),
    OPEN_ARCH("Open arch", "An arch opens above the grid, leaving the sides clear for long labels."),
    TWIN_CURVE("Twin curve", "Symmetrical curves rise from the bottom corners and meet below the dock.")
}

object FaceThemes {
    private const val INK = 0xFF101015
    private const val WHITE = 0xFFF7F5F2

    val all: List<FaceTheme> = listOf(
        FaceTheme(
            id = "pearl_mint",
            name = "Pearl Mint",
            family = "Calm",
            description = "Warm pearl surfaces, mint accent, cool ceramic keys. The default.",
            background = 0xFF0E1A18,
            surface = 0xFF14211F,
            surfaceRaised = 0xFF1B2C29,
            border = 0xFF2C4240,
            textPrimary = 0xFFF2FAF6,
            textSecondary = 0xFFA9C6BE,
            accent = 0xFF76DCCB,
            accentSecondary = 0xFFE8D8B0,
            wallpaperId = "nature-015",
            keyboardDesignId = "porcelain_matte",
            iconTile = IconTile.FROSTED_ROUNDED,
            iconTileColor = 0xFF1D302C,
            iconGlyphColor = 0xFFEAF7F2,
            widgetStyleId = "Mint",
            chargingStyleId = "mint_pulse",
            veilStyle = VeilStyle.SOFT_DRAPE,
            veilColor = 0xFF76DCCB,
            isDark = true
        ),
        FaceTheme(
            id = "rose_quartz",
            name = "Rose Quartz",
            family = "Ladies",
            description = "Blush quartz and soft gold, with pebble icons for a lighter touch.",
            background = 0xFF1A1014,
            surface = 0xFF241820,
            surfaceRaised = 0xFF31212A,
            border = 0xFF4A3040,
            textPrimary = 0xFFFCEEF2,
            textSecondary = 0xFFD3A9B6,
            accent = 0xFFE9A6BE,
            accentSecondary = 0xFFF0CC9B,
            wallpaperId = "heritage-013",
            keyboardDesignId = "frosted_quartz",
            iconTile = IconTile.PEBBLE_CIRCLE,
            iconTileColor = 0xFF3A2530,
            iconGlyphColor = 0xFFFFF0F4,
            widgetStyleId = "Ceramic",
            chargingStyleId = "rose_bloom",
            veilStyle = VeilStyle.VEIL_FRAME,
            veilColor = 0xFFE9A6BE,
            isDark = true
        ),
        FaceTheme(
            id = "lavender_cat",
            name = "Lavender Cat",
            family = "Ladies",
            description = "Lilac glass, cat-ear accents and a soft violet glow on charge.",
            background = 0xFF151129,
            surface = 0xFF1E1937,
            surfaceRaised = 0xFF2A2349,
            border = 0xFF3E3566,
            textPrimary = 0xFFF4F0FF,
            textSecondary = 0xFFBEB0E0,
            accent = 0xFFC0A8FF,
            accentSecondary = 0xFFFFD3E6,
            wallpaperId = "form-009",
            keyboardDesignId = "obsidian_ceramic",
            iconTile = IconTile.GLASS_ROUNDED,
            iconTileColor = 0xFF2B2450,
            iconGlyphColor = 0xFFF6F2FF,
            widgetStyleId = "Obsidian",
            chargingStyleId = "purr_charge",
            veilStyle = VeilStyle.LAYERED_FOLD,
            veilColor = 0xFFC0A8FF,
            isDark = true
        ),
        FaceTheme(
            id = "sakura",
            name = "Sakura",
            family = "Ladies",
            description = "Pale blossom pink over deep plum, with a paper-quiet matte finish.",
            background = 0xFF1F1116,
            surface = 0xFF2A1820,
            surfaceRaised = 0xFF3A222C,
            border = 0xFF52303B,
            textPrimary = 0xFFFFF2F5,
            textSecondary = 0xFFDCAEB8,
            accent = 0xFFF2A0B6,
            accentSecondary = 0xFFFFE0B8,
            wallpaperId = "design-006",
            keyboardDesignId = "linen_soft",
            iconTile = IconTile.MATTE_SQUARE,
            iconTileColor = 0xFF43262F,
            iconGlyphColor = 0xFFFFF4F6,
            widgetStyleId = "Ceramic",
            chargingStyleId = "petal_fill",
            veilStyle = VeilStyle.TWIN_CURVE,
            veilColor = 0xFFF2A0B6,
            isDark = true
        ),
        FaceTheme(
            id = "cat_nap",
            name = "Cat Nap",
            family = "Cute",
            description = "Cream and cocoa with soft rounded everything, and a sleeping-cat charge bar.",
            background = 0xFF1C1712,
            surface = 0xFF26201A,
            surfaceRaised = 0xFF342C24,
            border = 0xFF4A3F33,
            textPrimary = 0xFFFBF3E8,
            textSecondary = 0xFFCFBBA6,
            accent = 0xFFF0C4A2,
            accentSecondary = 0xFFA8D8C8,
            wallpaperId = "civic-002",
            keyboardDesignId = "frosted_quartz",
            iconTile = IconTile.PEBBLE_CIRCLE,
            iconTileColor = 0xFF3B3128,
            iconGlyphColor = 0xFFFFF6EA,
            widgetStyleId = "Ceramic",
            chargingStyleId = "cat_nap",
            veilStyle = VeilStyle.OPEN_ARCH,
            veilColor = 0xFFF0C4A2,
            isDark = true
        ),
        FaceTheme(
            id = "midnight_cat",
            name = "Midnight Cat",
            family = "Cute",
            description = "Ink-blue night with teal eyes and a moonlit charge glow.",
            background = 0xFF08101A,
            surface = 0xFF101C2A,
            surfaceRaised = 0xFF182838,
            border = 0xFF22394D,
            textPrimary = 0xFFEFF7FF,
            textSecondary = 0xFF9FBBD2,
            accent = 0xFF6FE3D0,
            accentSecondary = 0xFFB9C8FF,
            wallpaperId = "tech-004",
            keyboardDesignId = "obsidian_ceramic",
            iconTile = IconTile.GLASS_ROUNDED,
            iconTileColor = 0xFF16273A,
            iconGlyphColor = 0xFFF2FAFF,
            widgetStyleId = "Obsidian",
            chargingStyleId = "moon_charge",
            veilStyle = VeilStyle.SOFT_DRAPE,
            veilColor = 0xFF6FE3D0,
            isDark = true
        ),
        FaceTheme(
            id = "heritage_gold",
            name = "Heritage Gold",
            family = "Heritage",
            description = "Sandstone, deep teal and restrained gold — architectural, not ornamental.",
            background = 0xFF14110C,
            surface = 0xFF1E1A13,
            surfaceRaised = 0xFF2A241A,
            border = 0xFF3E3626,
            textPrimary = 0xFFF7F0E2,
            textSecondary = 0xFFC4B394,
            accent = 0xFFDFBE7C,
            accentSecondary = 0xFF87C7B7,
            wallpaperId = "heritage-001",
            keyboardDesignId = "carrara_stone",
            iconTile = IconTile.STONE_ROUNDED,
            iconTileColor = 0xFF2C2519,
            iconGlyphColor = 0xFFFBF4E6,
            widgetStyleId = "Ceramic",
            chargingStyleId = "gold_fill",
            veilStyle = VeilStyle.OPEN_ARCH,
            veilColor = 0xFFDFBE7C,
            isDark = true
        ),
        FaceTheme(
            id = "cairo_night",
            name = "Cairo Night",
            family = "Heritage",
            description = "Indigo night with warm window-light accents.",
            background = 0xFF0A0F1C,
            surface = 0xFF121B2E,
            surfaceRaised = 0xFF1B2840,
            border = 0xFF2A3C5C,
            textPrimary = 0xFFF0F4FF,
            textSecondary = 0xFF9DAEC9,
            accent = 0xFFE4B268,
            accentSecondary = 0xFF7E93C4,
            wallpaperId = "heritage-005",
            keyboardDesignId = "basalt_pebble",
            iconTile = IconTile.CERAMIC_SQUIRCLE,
            iconTileColor = 0xFF1C2C48,
            iconGlyphColor = 0xFFF4F7FF,
            widgetStyleId = "Obsidian",
            chargingStyleId = "lantern_glow",
            veilStyle = VeilStyle.VEIL_FRAME,
            veilColor = 0xFFE4B268,
            isDark = true
        ),
        FaceTheme(
            id = "stadium_night",
            name = "Stadium Night",
            family = "Sport",
            description = "Floodlit pitch green under a night sky, with chalk-line accents.",
            background = 0xFF08120E,
            surface = 0xFF0F1D17,
            surfaceRaised = 0xFF152A22,
            border = 0xFF224034,
            textPrimary = 0xFFF2FAF5,
            textSecondary = 0xFF9FBFAE,
            accent = 0xFF5FA98C,
            accentSecondary = 0xFFF0E6C8,
            wallpaperId = "sport-001",
            keyboardDesignId = "basalt_pebble",
            iconTile = IconTile.MATTE_SQUARE,
            iconTileColor = 0xFF16302A,
            iconGlyphColor = 0xFFF4FFF8,
            widgetStyleId = "Obsidian",
            chargingStyleId = "pitch_fill",
            veilStyle = VeilStyle.NONE,
            veilColor = 0xFF5FA98C,
            isDark = true
        ),
        FaceTheme(
            id = "obsidian_violet",
            name = "Obsidian Violet",
            family = "Abstract",
            description = "Near-black obsidian with a single violet lift. The product's own voice.",
            background = 0xFF0A0910,
            surface = 0xFF141220,
            surfaceRaised = 0xFF1E1A2E,
            border = 0xFF2E2844,
            textPrimary = 0xFFF4F0FA,
            textSecondary = 0xFFA99CC4,
            accent = 0xFFB9A2F5,
            accentSecondary = 0xFF76DCCB,
            wallpaperId = "form-001",
            keyboardDesignId = "obsidian_ceramic",
            iconTile = IconTile.GLASS_ROUNDED,
            iconTileColor = 0xFF201B33,
            iconGlyphColor = 0xFFF8F5FF,
            widgetStyleId = "Obsidian",
            chargingStyleId = "violet_ring",
            veilStyle = VeilStyle.LAYERED_FOLD,
            veilColor = 0xFFB9A2F5,
            isDark = true
        ),
        FaceTheme(
            id = "aurora_sky",
            name = "Aurora Sky",
            family = "Nature",
            description = "Northern light green over a cold blue night, with flowing charge curves.",
            background = 0xFF061019,
            surface = 0xFF0D1B27,
            surfaceRaised = 0xFF142836,
            border = 0xFF1F3E4F,
            textPrimary = 0xFFEEFAFF,
            textSecondary = 0xFF9CBECC,
            accent = 0xFF76E0C0,
            accentSecondary = 0xFF8FB6FF,
            wallpaperId = "nature-005",
            keyboardDesignId = "porcelain_matte",
            iconTile = IconTile.FROSTED_ROUNDED,
            iconTileColor = 0xFF123040,
            iconGlyphColor = 0xFFF2FDFF,
            widgetStyleId = "Mint",
            chargingStyleId = "aurora_flow",
            veilStyle = VeilStyle.TWIN_CURVE,
            veilColor = 0xFF76E0C0,
            isDark = true
        ),
        FaceTheme(
            id = "porcelain_day",
            name = "Porcelain Day",
            family = "Calm",
            description = "A genuinely light theme: warm paper surface with ink text.",
            background = 0xFFF2EDE4,
            surface = 0xFFFBF8F2,
            surfaceRaised = 0xFFFFFFFF,
            border = 0xFFDDD3C4,
            textPrimary = 0xFF1E1B16,
            textSecondary = 0xFF6B6257,
            accent = 0xFF2F6F62,
            accentSecondary = 0xFF9A6B3F,
            wallpaperId = "civic-004",
            keyboardDesignId = "porcelain_matte",
            iconTile = IconTile.CERAMIC_SQUIRCLE,
            iconTileColor = 0xFFEDE5D8,
            iconGlyphColor = 0xFF23201A,
            widgetStyleId = "Ceramic",
            chargingStyleId = "paper_fill",
            veilStyle = VeilStyle.SOFT_DRAPE,
            veilColor = 0xFF9A6B3F,
            isDark = false
        )
    )

    val families: List<String> get() = all.map { it.family }.distinct()

    fun find(id: String?): FaceTheme = all.firstOrNull { it.id == id } ?: all.first()

    fun byFamily(family: String?): List<FaceTheme> =
        if (family == null) all else all.filter { it.family == family }

    /** Every glyph the icon set must be able to draw, in grid order. */
    val showcaseGlyphs = IconGlyph.entries
}

/** WCAG maths shared by the theme checks and the tests. */
object ThemeContrast {
    fun relativeLuminance(colour: Long): Double {
        fun channel(value: Long): Double {
            val srgb = (value and 0xFF) / 255.0
            return if (srgb <= 0.03928) srgb / 12.92 else Math.pow((srgb + 0.055) / 1.055, 2.4)
        }
        return 0.2126 * channel((colour shr 16) and 0xFF) +
            0.7152 * channel((colour shr 8) and 0xFF) +
            0.0722 * channel(colour and 0xFF)
    }

    fun ratio(foreground: Long, background: Long): Double {
        val a = relativeLuminance(foreground)
        val b = relativeLuminance(background)
        return (maxOf(a, b) + 0.05) / (minOf(a, b) + 0.05)
    }
}

/** Links a theme to the keyboard design it suggests, without coupling the two. */
object ThemeKeyboardBridge {
    fun designFor(theme: FaceTheme) =
        KeyboardDesignCatalogue.all.firstOrNull { it.id == theme.keyboardDesignId }
            ?: KeyboardDesignCatalogue.all.first()
}
