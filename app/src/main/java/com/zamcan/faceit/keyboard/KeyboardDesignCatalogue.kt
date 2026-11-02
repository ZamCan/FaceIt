package com.zamcan.faceit.keyboard

import android.content.Context

/**
 * Keyboard Designs — selectable visual presets, not a system keyboard.
 *
 * AGENTS.md §10 is a binding product-owner decision: the keyboard feature is a
 * visual personalisation studio. These designs are Face It presets that can be
 * browsed, compared, favourited and demonstrated inside the app. They are not
 * an InputMethodService, they never intercept typing, and selecting one never
 * changes Gboard, Samsung Keyboard or any third-party keyboard.
 */

/** Distinct keycap geometries. Each one changes silhouette, not just colour. */
enum class KeyGeometry { SCULPTED_CAP, PEBBLE, SLAB, KEYSTONE, PILLOW }

data class KeyboardDesign(
    val id: String,
    val name: String,
    val family: String,
    val description: String,
    val geometry: KeyGeometry,
    val material: KeyMaterial,
    val paletteId: String,
    val font: KeyboardFont,
    val cornerRadiusDp: Int,
    val keyGapDp: Int,
    val keyHeightDp: Int,
    /** Secondary accent used by the preview's selected/return keys. */
    val highlight: Long
) {
    val palette: KeyboardPalette get() = KeyboardPalettes.byId(paletteId)
}

object KeyboardDesignCatalogue {
    val all: List<KeyboardDesign> = listOf(
        KeyboardDesign(
            id = "obsidian_ceramic",
            name = "Obsidian Ceramic",
            family = "Ceramic",
            description = "Deep obsidian keys in matte ceramic with a single violet accent.",
            geometry = KeyGeometry.SCULPTED_CAP,
            material = KeyMaterial.MATTE_CERAMIC,
            paletteId = KeyboardPalettes.OBSIDIAN_CERAMIC_ID,
            font = KeyboardFont.SANS,
            cornerRadiusDp = 14,
            keyGapDp = 6,
            keyHeightDp = 48,
            highlight = 0xB9A2F5
        ),
        KeyboardDesign(
            id = "porcelain_matte",
            name = "Porcelain Matte",
            family = "Soft matte",
            description = "Warm porcelain keys with soft cushion edges for light surfaces.",
            geometry = KeyGeometry.PILLOW,
            material = KeyMaterial.SOFT_MATTE,
            paletteId = KeyboardPalettes.PEARL_MINT_ID,
            font = KeyboardFont.ROUNDED,
            cornerRadiusDp = 18,
            keyGapDp = 7,
            keyHeightDp = 50,
            highlight = 0x357A67
        ),
        KeyboardDesign(
            id = "frosted_quartz",
            name = "Frosted Quartz",
            family = "Frosted glass",
            description = "Translucent quartz slabs with pearl-mint light passing through them.",
            geometry = KeyGeometry.SLAB,
            material = KeyMaterial.FROSTED_GLASS,
            paletteId = KeyboardPalettes.PEARL_MINT_ID,
            font = KeyboardFont.SANS,
            cornerRadiusDp = 10,
            keyGapDp = 5,
            keyHeightDp = 46,
            highlight = 0x357A67
        ),
        KeyboardDesign(
            id = "arctic_glass",
            name = "Arctic Glass",
            family = "Frosted glass",
            description = "Cold graphite glass with an ice-blue rim on every keycap.",
            geometry = KeyGeometry.KEYSTONE,
            material = KeyMaterial.FROSTED_GLASS,
            paletteId = KeyboardPalettes.GRAPHITE_ICE_ID,
            font = KeyboardFont.SANS,
            cornerRadiusDp = 12,
            keyGapDp = 6,
            keyHeightDp = 47,
            highlight = 0x91B9F4
        ),
        KeyboardDesign(
            id = "carrara_stone",
            name = "Carrara Stone",
            family = "Natural stone",
            description = "Cut-stone keys with visible grain and a warm ivory legend.",
            geometry = KeyGeometry.SCULPTED_CAP,
            material = KeyMaterial.NATURAL_STONE,
            paletteId = KeyboardPalettes.WARM_STONE_ID,
            font = KeyboardFont.SERIF,
            cornerRadiusDp = 9,
            keyGapDp = 6,
            keyHeightDp = 49,
            highlight = 0xD7A878
        ),
        KeyboardDesign(
            id = "basalt_pebble",
            name = "Basalt Pebble",
            family = "Natural stone",
            description = "Rounded basalt pebbles; tactile, muted and heavy under the thumb.",
            geometry = KeyGeometry.PEBBLE,
            material = KeyMaterial.NATURAL_STONE,
            paletteId = KeyboardPalettes.MIDNIGHT_ONYX_ID,
            font = KeyboardFont.ROUNDED,
            cornerRadiusDp = 24,
            keyGapDp = 8,
            keyHeightDp = 52,
            highlight = 0xD7A878
        ),
        KeyboardDesign(
            id = "brushed_titanium",
            name = "Brushed Titanium",
            family = "Brushed metal",
            description = "Fine brushed metal with crisp slab keys and an ice highlight.",
            geometry = KeyGeometry.SLAB,
            material = KeyMaterial.BRUSHED_METAL,
            paletteId = KeyboardPalettes.GRAPHITE_ICE_ID,
            font = KeyboardFont.SANS,
            cornerRadiusDp = 8,
            keyGapDp = 5,
            keyHeightDp = 46,
            highlight = 0x91B9F4
        ),
        KeyboardDesign(
            id = "gunmetal_pebble",
            name = "Gunmetal Pebble",
            family = "Brushed metal",
            description = "Dark machined keys with a narrow gap and deliberate weight.",
            geometry = KeyGeometry.PEBBLE,
            material = KeyMaterial.BRUSHED_METAL,
            paletteId = KeyboardPalettes.OBSIDIAN_CERAMIC_ID,
            font = KeyboardFont.MONOSPACE,
            cornerRadiusDp = 20,
            keyGapDp = 5,
            keyHeightDp = 50,
            highlight = 0xB9A2F5
        ),
        KeyboardDesign(
            id = "linen_soft",
            name = "Linen Soft",
            family = "Soft matte",
            description = "Quiet linen-tone keys, generous gaps, no highlights or noise.",
            geometry = KeyGeometry.PILLOW,
            material = KeyMaterial.SOFT_MATTE,
            paletteId = KeyboardPalettes.WARM_STONE_ID,
            font = KeyboardFont.SERIF,
            cornerRadiusDp = 16,
            keyGapDp = 9,
            keyHeightDp = 51,
            highlight = 0xD7A878
        ),
        KeyboardDesign(
            id = "midnight_onyx",
            name = "Midnight Onyx",
            family = "Ceramic",
            description = "Near-black keystone caps with a thin violet return key.",
            geometry = KeyGeometry.KEYSTONE,
            material = KeyMaterial.MATTE_CERAMIC,
            paletteId = KeyboardPalettes.MIDNIGHT_ONYX_ID,
            font = KeyboardFont.SANS,
            cornerRadiusDp = 6,
            keyGapDp = 4,
            keyHeightDp = 45,
            highlight = 0x8B6DF2
        )
    )

    val families: List<String> = all.map { it.family }.distinct()

    fun find(id: String?): KeyboardDesign = all.firstOrNull { it.id == id } ?: all.first()

    fun byFamily(family: String?): List<KeyboardDesign> =
        if (family == null) all else all.filter { it.family == family }

    fun migrateFromLegacyPreferences(prefs: Map<String, String?>): String {
        // Preserve a returning user's previous appearance choice rather than
        // resetting them to the default when the studio became a design gallery.
        val material = prefs["material"] ?: return all.first().id
        val shape = prefs["shape"]
        val palette = prefs["palette"]
        return all.firstOrNull { design ->
            design.material.name == material &&
                (palette == null || design.paletteId == palette) &&
                (shape == null || legacyShapeMatches(design.geometry, shape))
        }?.id ?: all.firstOrNull { it.material.name == material }?.id ?: all.first().id
    }

    private fun legacyShapeMatches(geometry: KeyGeometry, legacyShape: String): Boolean = when (legacyShape) {
        "PILL" -> geometry == KeyGeometry.PEBBLE || geometry == KeyGeometry.PILLOW
        "CIRCULAR" -> geometry == KeyGeometry.PEBBLE
        "SOFT_SQUARE" -> geometry == KeyGeometry.SLAB || geometry == KeyGeometry.SCULPTED_CAP
        "ROUNDED_RECTANGLE" -> geometry != KeyGeometry.PEBBLE
        else -> true
    }
}

/** Deterministic, unit-tested design rules. No Android dependencies. */
object KeyboardDesignEngine {
    /** WCAG relative luminance for a 0xRRGGBB colour. */
    fun relativeLuminance(colour: Long): Double {
        fun channel(value: Long): Double {
            val srgb = (value and 0xFF) / 255.0
            return if (srgb <= 0.03928) srgb / 12.92 else Math.pow((srgb + 0.055) / 1.055, 2.4)
        }
        return 0.2126 * channel((colour shr 16) and 0xFF) +
            0.7152 * channel((colour shr 8) and 0xFF) +
            0.0722 * channel(colour and 0xFF)
    }

    /** WCAG 2.1 contrast ratio, 1.0 .. 21.0. */
    fun contrastRatio(foreground: Long, background: Long): Double {
        val a = relativeLuminance(foreground)
        val b = relativeLuminance(background)
        val lighter = maxOf(a, b)
        val darker = minOf(a, b)
        return (lighter + 0.05) / (darker + 0.05)
    }

    /** Legends must meet AA for normal text on the key surface. */
    fun legendIsAccessible(palette: KeyboardPalette): Boolean =
        contrastRatio(palette.keyText, palette.keySurface) >= 4.5

    /** Accents are non-text UI, so the AA non-text threshold of 3.0 applies. */
    fun accentIsVisible(palette: KeyboardPalette): Boolean =
        contrastRatio(palette.accent, palette.background) >= 3.0

    /** Clamp a stored design so persisted or future values stay in a usable range. */
    fun sanitise(design: KeyboardDesign): KeyboardDesign = design.copy(
        keyHeightDp = design.keyHeightDp.coerceIn(40, 72),
        keyGapDp = design.keyGapDp.coerceIn(2, 12),
        cornerRadiusDp = design.cornerRadiusDp.coerceIn(0, 28)
    )
}

/**
 * Local persistence for the selected design and favourites.
 *
 * Stored in the same `faceit_keyboard` preference file the previous studio used,
 * so an upgrading user keeps their appearance choice.
 */
class KeyboardDesignPreferences(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("faceit_keyboard", Context.MODE_PRIVATE)

    fun selectedDesignId(): String = KeyboardDesignCatalogue.find(prefs.getString(KEY_SELECTED, null)).id

    fun select(designId: String) {
        prefs.edit().putString(KEY_SELECTED, KeyboardDesignCatalogue.find(designId).id).apply()
    }

    fun favourites(): Set<String> =
        prefs.getStringSet(KEY_FAVOURITES, emptySet()).orEmpty()
            .filter { id -> KeyboardDesignCatalogue.all.any { it.id == id } }
            .toSet()

    fun toggleFavourite(designId: String): Boolean {
        val current = favourites().toMutableSet()
        val added = if (current.contains(designId)) {
            current.remove(designId)
            false
        } else {
            current.add(designId)
            true
        }
        prefs.edit().putStringSet(KEY_FAVOURITES, current).apply()
        return added
    }

    /** Runs once: no-op after the first launch once a design has been selected. */
    fun migrateLegacyAppearance() {
        if (prefs.contains(KEY_SELECTED)) return
        val legacy = mapOf(
            "material" to prefs.getString("material", null),
            "shape" to prefs.getString("shape", null),
            "palette" to prefs.getString("palette", null)
        )
        prefs.edit()
            .putString(KEY_SELECTED, KeyboardDesignCatalogue.migrateFromLegacyPreferences(legacy))
            .apply()
    }

    private companion object {
        const val KEY_SELECTED = "selected_design"
        const val KEY_FAVOURITES = "favourite_designs"
    }
}
