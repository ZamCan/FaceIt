package com.zamcan.faceit.keypad

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Keypad designs replaced the standalone Dialer tab, so the pad is a design
 * catalogue now and its guarantees are the ones any catalogue needs: unique
 * presets, readable digits, and a complete set of key faces.
 */
class KeypadDesignsTest {

    @Test
    fun thereAreEnoughDesignsToBeACatalogue() {
        assertTrue("only ${KeypadDesigns.all.size} keypads", KeypadDesigns.all.size >= 20)
        assertTrue("expected several families", KeypadDesigns.families.size >= 5)
    }

    @Test
    fun designsHaveUniqueIds() {
        val ids = KeypadDesigns.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun everyDigitIsInstantlyReadable() {
        KeypadDesigns.all.forEach { design ->
            assertTrue(
                "${design.name}: legend contrast ${design.legendContrast} on key surface",
                design.legendContrast >= 4.5
            )
        }
    }

    @Test
    fun accentStaysVisibleOnTheBoard() {
        KeypadDesigns.all.forEach { design ->
            assertTrue(
                "${design.name}: accent contrast ${design.accentContrast}",
                design.accentContrast >= 3.0
            )
        }
    }

    @Test
    fun everyPadHasTheTwelveStandardFaces() {
        assertEquals(12, KeypadDesigns.faces.size)
        assertEquals(KeypadDesigns.faces.toSet().size, KeypadDesigns.faces.size)
        listOf("0", "1", "9", "*", "#").forEach { face ->
            assertTrue("missing $face", KeypadDesigns.faces.contains(face))
        }
    }

    @Test
    fun touchTargetsStayUsable() {
        KeypadDesigns.all.forEach { design ->
            assertTrue("${design.name} key height", design.keyHeightDp >= 48)
            assertTrue("${design.name} key gap", design.keyGapDp in 2..16)
            assertTrue("${design.name} press depth", design.pressDepthDp in 0..4)
        }
    }

    @Test
    fun unknownDesignFallsBackInsteadOfCrashing() {
        assertEquals(KeypadDesigns.all.first().id, KeypadDesigns.find(null).id)
        assertEquals(KeypadDesigns.all.first().id, KeypadDesigns.find("nope").id)
    }

    @Test
    fun thereIsALightPadForLightThemes() {
        // Light themes need a light board or the pad fights the wallpaper. There
        // are now several, and every one of them must keep dark legends.
        val light = KeypadDesigns.all.filter { design ->
            com.zamcan.faceit.themes.ThemeContrast.ratio(design.boardBackground, 0xFFFFFFFFL) < 1.6
        }
        assertTrue("no light keypad board", light.size >= 3)
        light.forEach { design ->
            assertTrue(
                "${design.name}: legend ${design.legendContrast} on a light board",
                design.legendContrast >= 4.5
            )
        }
    }

    @Test
    fun everyPurposeIsRepresentedSoTheGalleryHasNoEmptyShelf() {
        KeypadPurpose.entries.forEach { purpose ->
            assertTrue(
                "no keypad for $purpose",
                KeypadDesigns.all.any { it.purpose == purpose }
            )
        }
    }
}
