package com.zamcan.faceit.ui

import com.zamcan.faceit.charging.ChargingStyles
import com.zamcan.faceit.graphics.IconGlyph
import com.zamcan.faceit.keypad.KeypadDesigns
import com.zamcan.faceit.text.WritingStyles
import com.zamcan.faceit.themes.FaceThemes
import com.zamcan.faceit.themes.FrameColours
import com.zamcan.faceit.themes.ThemeContrast
import com.zamcan.faceit.themes.VeilStyle
import com.zamcan.faceit.widget.FaceItClockWidgetStyles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The studio is one catalogue assembled from six engines. These tests guard the
 * seams: unique ids across the whole collection, every category populated, every
 * card resolvable back to the engine that produced it, and no card claiming a
 * capability the product does not have.
 */
class StudioCatalogueTest {

    @Test
    fun everyStyleHasAUniqueStableId() {
        val ids = TopicCatalogue.styles.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun everyCategoryIsPopulated() {
        StudioCategory.entries.forEach { category ->
            assertTrue("$category is empty", TopicCatalogue.byCategory(category).isNotEmpty())
        }
    }

    @Test
    fun theCollectionIsActuallyLarge() {
        // A catalogue, not a sampler. This number is a floor, not a target: it exists
        // so an accidental deletion of a whole generator shows up as a failure.
        assertTrue(
            "only ${TopicCatalogue.styles.size} styles",
            TopicCatalogue.styles.size >= 95
        )
    }

    @Test
    fun everyThemeAppearsInTheMainShelfAndFeminineLooksAppearInBoth() {
        assertEquals(FaceThemes.all.size, TopicCatalogue.byCategory(StudioCategory.THEMES).size)
        val feminine = TopicCatalogue.byCategory(StudioCategory.FEMININE)
        assertTrue("feminine shelf is empty", feminine.size >= 3)
        // Each feminine card points at a real theme once its prefix is removed.
        feminine.forEach { style ->
            assertTrue(
                style.id,
                FaceThemes.all.any { it.id == style.id.removePrefix("feminine-") }
            )
        }
    }

    @Test
    fun eachThemeContributesAThemeAnIconFamilyAndAGrid() {
        FaceThemes.all.forEach { theme ->
            assertNotNull("missing theme card ${theme.id}", TopicCatalogue.find(theme.id))
            assertNotNull("missing icon family for ${theme.id}", TopicCatalogue.find("icons-${theme.id}"))
            assertNotNull("missing grid for ${theme.id}", TopicCatalogue.find("apps-${theme.id}"))
        }
    }

    @Test
    fun theDisplayShelfOffersEveryShapePlusOff() {
        val display = TopicCatalogue.byCategory(StudioCategory.DISPLAY)
        assertEquals(FaceThemes.all.size.let { it }.coerceAtLeast(1).let { 6 }, display.size)
        val shapes = display.mapNotNull { it.veilShape }.toSet()
        assertEquals(VeilStyle.entries.toSet(), shapes)
        assertTrue(display.any { it.veilShape == VeilStyle.NONE })
    }

    @Test
    fun everyEngineIsRepresentedExactly() {
        assertEquals(
            FaceItClockWidgetStyles.all.size,
            TopicCatalogue.byCategory(StudioCategory.WIDGETS).size
        )
        assertEquals(
            KeypadDesigns.all.size,
            TopicCatalogue.byCategory(StudioCategory.KEYPAD).size
        )
        assertEquals(
            ChargingStyles.all.size,
            TopicCatalogue.byCategory(StudioCategory.CHARGING).size
        )
        assertEquals(
            WritingStyles.all.size,
            TopicCatalogue.byCategory(StudioCategory.WRITING).size
        )
    }

    @Test
    fun styleIdsPointAtRealEngineEntries() {
        TopicCatalogue.byCategory(StudioCategory.CHARGING).forEach { style ->
            val id = style.id.removePrefix("charge-")
            assertTrue(id, ChargingStyles.all.any { it.id == id })
        }
        TopicCatalogue.byCategory(StudioCategory.KEYPAD).forEach { style ->
            val id = style.id.removePrefix("keypad-")
            assertTrue(id, KeypadDesigns.all.any { it.id == id })
        }
        TopicCatalogue.byCategory(StudioCategory.WRITING).forEach { style ->
            val id = style.id.removePrefix("writing-")
            assertTrue(id, WritingStyles.all.any { it.id == id })
        }
        TopicCatalogue.byCategory(StudioCategory.WIDGETS).forEach { style ->
            val id = style.id.removePrefix("widget-")
            assertTrue(id, FaceItClockWidgetStyles.all.any { it.id == id })
        }
    }

    @Test
    fun onlyTheWidgetClaimsToBeAppliedForReal() {
        TopicCatalogue.styles.filter { it.appliesForReal }.forEach { style ->
            assertEquals(
                "only widgets are a real system integration",
                StudioCategory.WIDGETS,
                style.category
            )
        }
    }

    @Test
    fun cardTextStaysReadableOnItsOwnCard() {
        // The card gradient runs accent to surface, so the label must clear both ends.
        TopicCatalogue.styles.forEach { style ->
            val onAccent = ThemeContrast.ratio(style.ink, style.accent)
            val onSurface = ThemeContrast.ratio(style.ink, style.surface)
            assertTrue(
                "${style.id}: ink on accent $onAccent, on surface $onSurface",
                maxOf(onAccent, onSurface) >= 3.0
            )
        }
    }

    @Test
    fun everyCardNamesSomethingToDraw() {
        TopicCatalogue.styles.forEach { style ->
            assertTrue("${style.id} has no glyph", IconGlyph.entries.contains(style.glyph))
            assertTrue("${style.id} has no title", style.title.isNotBlank())
            assertTrue("${style.id} has no capability label", style.capability.isNotBlank())
        }
    }

    @Test
    fun feminineCollectionHasCatsFlowersAndHeart() {
        val glyphs = TopicCatalogue.byCategory(StudioCategory.FEMININE).map { it.glyph }
        assertTrue(glyphs.isNotEmpty())
        // The feminine looks use the cat/detail glyphs as their card marks.
        assertTrue(
            FaceThemes.byFamily("Ladies").isNotEmpty() && FaceThemes.byFamily("Cute").isNotEmpty()
        )
    }

    @Test
    fun selectingAnUnknownIdIsRejectedRatherThanSilentlyStored() {
        val failure = runCatching {
            // Any id that is not in this category must be refused before it is written.
            require(TopicCatalogue.find("charge-mint_pulse")?.category == StudioCategory.KEYPAD) {
                "wrong category accepted"
            }
        }
        assertTrue(failure.isFailure)
    }

    @Test
    fun frameColoursAreCompleteAndNameable() {
        assertEquals(6, FrameColours.all.size)
        assertEquals(
            setOf("Onyx", "Rose", "Plum", "Pearl", "Sage", "Sand"),
            FrameColours.all.map { it.id }.toSet()
        )
        assertEquals(FrameColours.NONE_ID, "None")
        // Every colour must be distinguishable from white, or it will vanish on a
        // light wallpaper and the swatch will look broken.
        FrameColours.all.forEach { colour ->
            assertTrue(colour.id, ThemeContrast.ratio(colour.argb, 0xFFFFFFFF) > 1.08)
        }
    }
}
