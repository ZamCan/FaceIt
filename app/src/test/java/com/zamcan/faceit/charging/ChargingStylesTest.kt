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
        // Not a ratio: an animation catalogue does not need to grow linearly with
        // the theme gallery. What matters is that there is a real choice on offer and
        // that the themes spread across it instead of piling onto one or two.
        val defaults = FaceThemes.all.map { it.chargingStyleId }
        assertTrue("themes lean on too few defaults", defaults.toSet().size >= 20)
        val mostUsed = defaults.groupingBy { it }.eachCount().values.max()
        assertTrue(
            "one charging style is the default for $mostUsed themes",
            mostUsed <= maxOf(4, FaceThemes.all.size / 7)
        )
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
