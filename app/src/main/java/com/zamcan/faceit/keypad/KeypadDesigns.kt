package com.zamcan.faceit.keypad

import com.zamcan.faceit.keyboard.KeyGeometry
import com.zamcan.faceit.keyboard.KeyMaterial

/**
 * Keypad designs — the number-pad appearance family.
 *
 * This replaces the old standalone Dialer tab. The product-owner direction is
 * that a dial pad is a *design*, not a screen: users browse the keypad styles,
 * preview them, and pick one to go with their theme, exactly as they do for
 * keyboard designs. Face It does not place calls and does not claim to reskin the
 * phone maker's dialler. A future in-app keypad surface would reuse these presets;
 * nothing here is a telephony integration.
 *
 * AGENTS.md §9 still governs the visual bar: raised, individually separated,
 * tablet-like keys with real press depth, and digits that stay instantly legible.
 */

/** What the pad is used for when a theme shows it. Presentation only. */
enum class KeypadPurpose(val label: String) {
    NUMBER_PAD("Number pad"),
    CALCULATOR("Calculator"),
    PASSCODE("Passcode"),
    SPEED_DIAL_STYLE("Quick dial look")
}

data class KeypadDesign(
    val id: String,
    val name: String,
    val family: String,
    val description: String,
    val geometry: KeyGeometry,
    val material: KeyMaterial,
    /** Key surface, legend and accent, 0xAARRGGBB. */
    val keySurface: Long,
    val legend: Long,
    val accent: Long,
    val boardBackground: Long,
    val keyGapDp: Int,
    val keyHeightDp: Int,
    val cornerRadiusDp: Int,
    /** How far a key sinks on press, in dp. Zero means a colour-only response. */
    val pressDepthDp: Int,
    val purpose: KeypadPurpose
) {
    /**
     * The digits must read instantly, so legends are checked against the key
     * surface with the same WCAG maths used for keyboard designs.
     */
    val legendContrast: Double
        get() = com.zamcan.faceit.themes.ThemeContrast.ratio(legend, keySurface)

    val accentContrast: Double
        get() = com.zamcan.faceit.themes.ThemeContrast.ratio(accent, boardBackground)
}

object KeypadDesigns {
    val all: List<KeypadDesign> = listOf(
        KeypadDesign(
            id = "ceramic_sculpt",
            name = "Ceramic Sculpt",
            family = "Ceramic",
            description = "Raised ceramic caps with a warm edge highlight and a 2 dp press sink.",
            geometry = KeyGeometry.SCULPTED_CAP,
            material = KeyMaterial.MATTE_CERAMIC,
            keySurface = 0xFF25211C,
            legend = 0xFFFBF3E8,
            accent = 0xFFF0C4A2,
            boardBackground = 0xFF14110D,
            keyGapDp = 10,
            keyHeightDp = 62,
            cornerRadiusDp = 22,
            pressDepthDp = 2,
            purpose = KeypadPurpose.NUMBER_PAD
        ),
        KeypadDesign(
            id = "frosted_pad",
            name = "Frosted Pad",
            family = "Glass",
            description = "Translucent keys that frost over the wallpaper beneath them.",
            geometry = KeyGeometry.PILLOW,
            material = KeyMaterial.FROSTED_GLASS,
            keySurface = 0xFF1E2C2A,
            legend = 0xFFF2FAF6,
            accent = 0xFF76DCCB,
            boardBackground = 0xFF0E1A18,
            keyGapDp = 8,
            keyHeightDp = 60,
            cornerRadiusDp = 26,
            pressDepthDp = 1,
            purpose = KeypadPurpose.NUMBER_PAD
        ),
        KeypadDesign(
            id = "stone_pad",
            name = "Stone Pad",
            family = "Stone",
            description = "Heavy stone blocks with a shallow shadow and wide spacing.",
            geometry = KeyGeometry.SLAB,
            material = KeyMaterial.NATURAL_STONE,
            keySurface = 0xFF2C2A26,
            legend = 0xFFF7F0E2,
            accent = 0xFFDFBE7C,
            boardBackground = 0xFF14120E,
            keyGapDp = 12,
            keyHeightDp = 64,
            cornerRadiusDp = 14,
            pressDepthDp = 3,
            purpose = KeypadPurpose.CALCULATOR
        ),
        KeypadDesign(
            id = "pebble_pad",
            name = "Pebble Pad",
            family = "Soft",
            description = "Round pebble keys for one-handed reaching, with a soft mint accent.",
            geometry = KeyGeometry.PEBBLE,
            material = KeyMaterial.SOFT_MATTE,
            keySurface = 0xFF223330,
            legend = 0xFFF4FFFB,
            accent = 0xFFA8D8C8,
            boardBackground = 0xFF101C1A,
            keyGapDp = 12,
            keyHeightDp = 58,
            cornerRadiusDp = 29,
            pressDepthDp = 2,
            purpose = KeypadPurpose.PASSCODE
        ),
        KeypadDesign(
            id = "titanium_pad",
            name = "Titanium Pad",
            family = "Metal",
            description = "Brushed titanium keys, cool specular edge, no colour accent.",
            geometry = KeyGeometry.KEYSTONE,
            material = KeyMaterial.BRUSHED_METAL,
            keySurface = 0xFF2A2F35,
            legend = 0xFFF0F4F8,
            accent = 0xFF91B9F4,
            boardBackground = 0xFF111418,
            keyGapDp = 6,
            keyHeightDp = 60,
            cornerRadiusDp = 12,
            pressDepthDp = 1,
            purpose = KeypadPurpose.NUMBER_PAD
        ),
        KeypadDesign(
            id = "night_pad",
            name = "Night Pad",
            family = "Glass",
            description = "Deep glass with a single violet leading edge on the call key.",
            geometry = KeyGeometry.SCULPTED_CAP,
            material = KeyMaterial.FROSTED_GLASS,
            keySurface = 0xFF1B1730,
            legend = 0xFFF8F5FF,
            accent = 0xFFB9A2F5,
            boardBackground = 0xFF0A0910,
            keyGapDp = 9,
            keyHeightDp = 62,
            cornerRadiusDp = 20,
            pressDepthDp = 2,
            purpose = KeypadPurpose.NUMBER_PAD
        ),
        KeypadDesign(
            id = "bloom_pad",
            name = "Bloom Pad",
            family = "Ladies",
            description = "Blush keys with a rose accent and a gentle 3 dp sink.",
            geometry = KeyGeometry.PILLOW,
            material = KeyMaterial.SOFT_MATTE,
            keySurface = 0xFF34222B,
            legend = 0xFFFFF2F5,
            accent = 0xFFF2A0B6,
            boardBackground = 0xFF1A1014,
            keyGapDp = 11,
            keyHeightDp = 60,
            cornerRadiusDp = 24,
            pressDepthDp = 3,
            purpose = KeypadPurpose.PASSCODE
        ),
        KeypadDesign(
            id = "paper_pad",
            name = "Paper Pad",
            family = "Light",
            description = "The one light design: warm paper keys with ink legends.",
            geometry = KeyGeometry.SLAB,
            material = KeyMaterial.SOFT_MATTE,
            keySurface = 0xFFFFFFFF,
            legend = 0xFF1E1B16,
            accent = 0xFF2F6F62,
            boardBackground = 0xFFF2EDE4,
            keyGapDp = 10,
            keyHeightDp = 62,
            cornerRadiusDp = 18,
            pressDepthDp = 1,
            purpose = KeypadPurpose.CALCULATOR
        )
    )

    val families: List<String> get() = all.map { it.family }.distinct()

    fun find(id: String?): KeypadDesign = all.firstOrNull { it.id == id } ?: all.first()

    fun byFamily(family: String?): List<KeypadDesign> =
        if (family == null) all else all.filter { it.family == family }

    /** The key faces of a standard pad, in reading order. */
    val faces: List<String> = listOf(
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#"
    )

    /** Letter hints under the digits, as on a physical keypad. */
    val letters: Map<String, String> = mapOf(
        "2" to "ABC", "3" to "DEF", "4" to "GHI", "5" to "JKL", "6" to "MNO",
        "7" to "PQRS", "8" to "TUV", "9" to "WXYZ", "0" to "+"
    )
}
