package com.zamcan.faceit.charging

import com.zamcan.faceit.themes.FaceThemes
import com.zamcan.faceit.themes.ThemeContrast
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChargingStylesTest {

    @Test
    fun thereAreEnoughStylesToGiveTheUserAChoice() {
        assertTrue("only ${ChargingStyles.all.size} styles", ChargingStyles.all.size >= 30)
        // The catalogue must not be a one-per-theme list: whatever the theme count,
        // the user needs alternatives to choose between. That is the real guarantee,
        // and it does not need to be rewritten every time the theme gallery grows.
        val perTheme = ChargingStyles.all.size.toFloat() / FaceThemes.all.size
        assertTrue(
            "only ${"%.2f".format(perTheme)} styles per theme",
            perTheme >= 0.6f
        )
        val defaults = FaceThemes.all.map { it.chargingStyleId }.toSet()
        assertTrue("themes lean on too few defaults", defaults.size >= 10)
    }

    @Test
    fun stylesHaveUniqueIds() {
        val ids = ChargingStyles.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun everyThemeChargingStyleExists() {
        FaceThemes.all.forEach { theme ->
            assertTrue(
                "${theme.name} -> ${theme.chargingStyleId}",
                ChargingStyles.all.any { it.id == theme.chargingStyleId }
            )
        }
    }

    @Test
    fun progressIsVisibleAgainstTheTrack() {
        ChargingStyles.all.forEach { style ->
            val ratio = ThemeContrast.ratio(style.progressColor, style.trackColor)
            assertTrue("${style.label}: ${ratio}", ratio >= 2.0)
        }
    }

    @Test
    fun everyStyleExplainsItselfInPlainLanguage() {
        ChargingStyles.all.forEach { style ->
            assertTrue("${style.label} description too short", style.description.length > 25)
            assertTrue("${style.label} label blank", style.label.isNotBlank())
        }
    }

    @Test
    fun unknownStyleFallsBackInsteadOfCrashing() {
        assertEquals(ChargingStyles.all.first().id, ChargingStyles.find("nope").id)
    }
}
