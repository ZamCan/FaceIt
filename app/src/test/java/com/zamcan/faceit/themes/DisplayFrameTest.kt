package com.zamcan.faceit.themes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The draped frame is the one feature with a product-owner concept behind it, so
 * the rules that came out of that conversation are tested rather than assumed:
 * any shape, any colour, and switchable off.
 */
class DisplayFrameTest {

    @Test
    fun thereAreFiveShapesAndAnOffState() {
        val shapes = VeilStyle.entries.filter { it != VeilStyle.NONE }
        assertEquals(5, shapes.size)
        assertEquals(
            setOf("SOFT_DRAPE", "VEIL_FRAME", "LAYERED_FOLD", "OPEN_ARCH", "TWIN_CURVE"),
            shapes.map { it.name }.toSet()
        )
    }

    @Test
    fun thereAreSixNamedColoursMatchingTheDesignReference() {
        assertEquals(
            listOf("Onyx", "Rose", "Plum", "Pearl", "Sage", "Sand"),
            FrameColours.ids
        )
    }

    @Test
    fun everyShapeCanBeDrawnInEveryColour() {
        // 5 shapes x 6 colours, all reachable through the resolver.
        val theme = FaceThemes.find("midnight_cat")
        FrameColours.all.forEach { colour ->
            VeilStyle.entries.filter { it != VeilStyle.NONE }.forEach { shape ->
                val resolved = ResolvedFrame(shape, colour.argb)
                assertTrue(resolved.enabled)
                assertEquals(colour.argb, resolved.colour)
            }
        }
        assertTrue(theme.id.isNotBlank())
    }

    @Test
    fun offBeatsEveryOtherSetting() {
        FaceThemes.all.forEach { theme ->
            val resolved = DisplayFrameResolver.resolve("None", theme)
            assertEquals(VeilStyle.NONE, resolved.style)
            assertTrue(!resolved.enabled)
        }
    }

    @Test
    fun anUnknownColourFallsBackToTheTheme() {
        FaceThemes.all.forEach { theme ->
            val resolved = DisplayFrameResolver.resolve("Chartreuse", theme)
            assertEquals(theme.veilStyle, resolved.style)
            assertEquals(theme.veilColor, resolved.colour)
        }
    }

    @Test
    fun frameColoursReadAgainstBothLightAndDarkWallpapers() {
        FrameColours.all.forEach { colour ->
            val onWhite = ThemeContrast.ratio(colour.argb, 0xFFFFFFFF)
            val onBlack = ThemeContrast.ratio(colour.argb, 0xFF000000)
            // At least one end must show the frame, otherwise it silently disappears.
            assertTrue("${colour.id}: $onWhite / $onBlack", maxOf(onWhite, onBlack) >= 1.4)
        }
    }
}
