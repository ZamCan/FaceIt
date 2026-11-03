package com.zamcan.faceit.charging

import com.zamcan.faceit.themes.FaceThemes
import com.zamcan.faceit.themes.ThemeContrast
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChargingStylesTest {

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
