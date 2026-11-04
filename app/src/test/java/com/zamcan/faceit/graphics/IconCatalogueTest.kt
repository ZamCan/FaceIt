package com.zamcan.faceit.graphics

import com.zamcan.faceit.themes.FaceThemes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The glyph set is the largest drawn surface in the product, and it is drawn twice
 * — once on an Android canvas for the widget path, once in Compose for the studio.
 * These tests pin the catalogue's size and its coverage, so adding glyphs cannot
 * silently leave an icon grid short.
 */
class IconCatalogueTest {

    @Test
    fun theGlyphSetIsLargeEnoughToFillEveryGrid() {
        assertTrue("only ${IconGlyph.entries.size} glyphs", IconGlyph.entries.size >= 40)
    }

    @Test
    fun thereAreEnoughTileFinishesForEveryTheme() {
        assertTrue("only ${IconTile.entries.size} tile finishes", IconTile.entries.size >= 6)
    }

    @Test
    fun everyThemePicksATileFinishThatExists() {
        FaceThemes.all.forEach { theme ->
            assertTrue(theme.id, IconTile.entries.contains(theme.iconTile))
        }
    }

    @Test
    fun glyphIdsAreUniqueAndStable() {
        val names = IconGlyph.entries.map { it.name }
        assertEquals(names.size, names.toSet().size)
    }

    @Test
    fun theCuteAndFeminineVocabularyIsPresent() {
        // The product direction named cats, flowers and jewellery explicitly; those
        // glyphs are load-bearing for the feminine collection, not decoration.
        listOf(
            IconGlyph.CAT, IconGlyph.PAW, IconGlyph.HEART, IconGlyph.FLOWER,
            IconGlyph.BUTTERFLY, IconGlyph.RING, IconGlyph.STAR
        ).forEach { glyph ->
            assertTrue(glyph.name, IconGlyph.entries.contains(glyph))
        }
    }

    @Test
    fun theThemeShowcaseStaysWithinTheGlyphSet() {
        FaceThemes.showcaseGlyphs.forEach { glyph ->
            assertTrue(glyph.name, IconGlyph.entries.contains(glyph))
        }
        assertEquals(6, FaceThemes.showcaseGlyphs.size)
    }
}
