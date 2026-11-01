package com.zamcan.faceit.keyboard

/**
 * Deterministic, offline keyboard appearance model.
 *
 * These values describe a FaceIt input-method theme; they do not claim to
 * re-skin another keyboard or alter the Android system dialler.
 */
enum class KeyShape { ROUNDED_RECTANGLE, PILL, SOFT_SQUARE, CIRCULAR }
enum class KeyMaterial { MATTE_CERAMIC, FROSTED_GLASS, NATURAL_STONE, BRUSHED_METAL, SOFT_MATTE }
enum class KeyboardFont { SYSTEM, SANS, SERIF, MONOSPACE, ROUNDED }
enum class KeyboardLayout { QWERTY, QWERTZ, AZERTY }

data class KeyboardPalette(
    val background: Long,
    val keySurface: Long,
    val keyText: Long,
    val accent: Long,
    val secondaryText: Long
) {
    init {
        require(listOf(background, keySurface, keyText, accent, secondaryText).all { it in 0x000000L..0xFFFFFFL })
    }
}

data class KeyboardProfile(
    val shape: KeyShape = KeyShape.ROUNDED_RECTANGLE,
    val material: KeyMaterial = KeyMaterial.MATTE_CERAMIC,
    val font: KeyboardFont = KeyboardFont.SYSTEM,
    val layout: KeyboardLayout = KeyboardLayout.QWERTY,
    val keyHeightDp: Int = 48,
    val keyGapDp: Int = 6,
    val cornerRadiusDp: Int = 14,
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val palette: KeyboardPalette = KeyboardPalettes.OBSIDIAN_CERAMIC
) {
}

object KeyboardPalettes {
    val OBSIDIAN_CERAMIC = KeyboardPalette(0x17191FL, 0x30333CL, 0xF3F1F7L, 0xB9A2F5L, 0xB8BAC4L)
    val WARM_STONE = KeyboardPalette(0x28231FL, 0x51463CL, 0xF5EBDD, 0xD7A878L, 0xC9B9A8L)
    val PEARL_MINT = KeyboardPalette(0xE9ECE9L, 0xFFFFFFL, 0x252B29L, 0x4C9B86L, 0x69736FL)
    val GRAPHITE_ICE = KeyboardPalette(0x171B20L, 0x303A43L, 0xF0F4F7L, 0x91B9F4L, 0xB7C2CBL)
}

/** SMS transports plain text; visual styling is strictly an on-device editor concern. */
data class SmsDraft(val recipient: String, val body: String) {
    fun transportBody(): String = body
}

object KeyboardPersonalizationEngine {
    fun validate(profile: KeyboardProfile): KeyboardProfile = profile.copy(
        keyHeightDp = profile.keyHeightDp.coerceIn(40, 72),
        keyGapDp = profile.keyGapDp.coerceIn(2, 12),
        cornerRadiusDp = profile.cornerRadiusDp.coerceIn(0, 28)
    )

    fun mapCharacters(layout: KeyboardLayout): List<String> = when (layout) {
        KeyboardLayout.QWERTY -> listOf("qwertyuiop", "asdfghjkl", "zxcvbnm")
        KeyboardLayout.QWERTZ -> listOf("qwertzuiop", "asdfghjkl", "yxcvbnm")
        KeyboardLayout.AZERTY -> listOf("azertyuiop", "qsdfghjklm", "wxcvbn")
    }
}
