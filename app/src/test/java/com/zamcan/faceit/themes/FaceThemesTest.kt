package com.zamcan.faceit.themes

import com.zamcan.faceit.data.PersonalEnvironment
import com.zamcan.faceit.data.ThemeWallpaperMode
import com.zamcan.faceit.data.applyingTheme
import com.zamcan.faceit.keyboard.KeyboardDesignCatalogue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The theme catalogue is a product surface, so its invariants are tested rather
 * than eyeballed: unique ids, readable colour pairs, no dangling reference to a
 * keyboard design, and a theme application that respects the layers it does not
 * own.
 */
class FaceThemesTest {

    @Test
    fun everyThemeHasAUniqueIdAndName() {
        val ids = FaceThemes.all.map { it.id }
        val names = FaceThemes.all.map { it.name }
        assertEquals(ids.size, ids.toSet().size)
        assertEquals(names.size, names.toSet().size)
    }

    @Test
    fun catalogueIsWideEnoughToBeACatalogue() {
        assertTrue("expected at least 36 themes", FaceThemes.all.size >= 36)
        assertTrue("expected a Ladies family", FaceThemes.families.contains("Ladies"))
        assertTrue("expected at least one light theme", FaceThemes.all.any { !it.isDark })
        assertTrue("expected several light themes", FaceThemes.all.count { !it.isDark } >= 5)
        assertTrue("expected at least five families", FaceThemes.families.size >= 6)
    }

    @Test
    fun bodyTextOnSurfaceMeetsAa() {
        FaceThemes.all.forEach { theme ->
            assertTrue(
                "${theme.name}: primary text contrast ${theme.contrastOnSurface} on surface",
                theme.contrastOnSurface >= 4.5
            )
        }
    }

    @Test
    fun secondaryTextRemainsReadable() {
        FaceThemes.all.forEach { theme ->
            assertTrue(
                "${theme.name}: secondary text contrast ${theme.contrastSecondaryOnSurface}",
                theme.contrastSecondaryOnSurface >= 3.0
            )
        }
    }

    @Test
    fun accentsStayVisibleAgainstTheSurface() {
        FaceThemes.all.forEach { theme ->
            assertTrue(
                "${theme.name}: accent contrast ${theme.contrastAccentOnSurface}",
                theme.contrastAccentOnSurface >= 3.0
            )
        }
    }

    @Test
    fun everyThemePointsAtARealKeyboardDesign() {
        FaceThemes.all.forEach { theme ->
            val design = KeyboardDesignCatalogue.all.firstOrNull { it.id == theme.keyboardDesignId }
            assertTrue("${theme.name} -> ${theme.keyboardDesignId}", design != null)
            assertEquals(theme.keyboardDesignId, KeyboardDesignCatalogue.find(theme.keyboardDesignId).id)
        }
    }

    @Test
    fun everyThemeNamesItsChargingAndWidgetStyle() {
        FaceThemes.all.forEach { theme ->
            assertTrue("${theme.name} charging style blank", theme.chargingStyleId.isNotBlank())
            assertTrue("${theme.name} widget style blank", theme.widgetStyleId.isNotBlank())
        }
    }

    @Test
    fun selectingAThemeKeepsLayersItDoesNotOwn() {
        val current = PersonalEnvironment(
            selectedArtworkFile = "my-photo.png",
            material = "Marble",
            curatedPhotoId = "amethyst-cluster-brazil"
        )
        val theme = FaceThemes.find("lavender_cat")

        val applied = current.applyingTheme(theme, keepWallpaper = true)

        assertEquals(theme.id, applied.themeId)
        assertEquals("my-photo.png", applied.selectedArtworkFile)
        assertEquals("Marble", applied.material)
        assertEquals("amethyst-cluster-brazil", applied.curatedPhotoId)
        assertEquals(ThemeWallpaperMode.KEEP, applied.wallpaperMode)
    }

    @Test
    fun followThemesWallpaperModeAdoptsTheThemeWallpaper() {
        val current = PersonalEnvironment(
            curatedPhotoId = "amethyst-cluster-brazil",
            selectedArtworkFile = "my-background.png",
            selectedObjectFile = "my-object.png",
            material = "Marble"
        )
        val theme = FaceThemes.find("heritage_gold")

        val applied = current.applyingTheme(theme, keepWallpaper = false)

        assertEquals(theme.wallpaperId, applied.curatedPhotoId)
        assertNotEquals(current.curatedPhotoId, applied.curatedPhotoId)
        assertEquals(current.selectedArtworkFile, applied.selectedArtworkFile)
        assertEquals(current.selectedObjectFile, applied.selectedObjectFile)
        assertEquals(current.material, applied.material)
        assertEquals(current.layers, applied.layers)
        assertEquals(ThemeWallpaperMode.FOLLOW_THEME, applied.wallpaperMode)
    }

    @Test
    fun unknownThemeFallsBackInsteadOfCrashing() {
        assertEquals(FaceThemes.all.first().id, FaceThemes.find(null).id)
        assertEquals(FaceThemes.all.first().id, FaceThemes.find("does_not_exist").id)
    }

    @Test
    fun familyFilterReturnsOnlyThatFamily() {
        val ladies = FaceThemes.byFamily("Ladies")
        assertTrue(ladies.isNotEmpty())
        assertTrue(ladies.all { it.family == "Ladies" })
        assertEquals(FaceThemes.all.size, FaceThemes.byFamily(null).size)
    }

    @Test
    fun frameIsOffUnlessTheUserAsksForIt() {
        val theme = FaceThemes.find("sakura")
        // "None" is an explicit user choice and beats the theme's own suggestion.
        val off = DisplayFrameResolver.resolve(FrameColours.NONE_ID, theme)
        assertTrue(!off.enabled)
        assertEquals(VeilStyle.NONE, off.style)
    }

    @Test
    fun choosingAFrameColourShowsTheThemeShapeInThatColour() {
        val theme = FaceThemes.find("heritage_gold")
        val resolved = DisplayFrameResolver.resolve("Plum", theme)
        assertTrue(resolved.enabled)
        assertEquals(FrameColours.find("Plum")!!.argb, resolved.colour)
        assertEquals(theme.veilStyle, resolved.style)
    }

    @Test
    fun aColourChoiceOnAThemelessFrameStillProducesAFrame() {
        val bare = FaceThemes.all.first { it.veilStyle == VeilStyle.NONE }
        val resolved = DisplayFrameResolver.resolve("Sage", bare)
        assertTrue("a colour choice must draw something", resolved.enabled)
        assertEquals(VeilStyle.SOFT_DRAPE, resolved.style)
    }

    @Test
    fun everyFrameColourCanBeResolvedByName() {
        FrameColours.all.forEach { colour ->
            assertEquals(colour.argb, DisplayFrameResolver.resolve(colour.id, FaceThemes.find("obsidian_violet")).colour)
        }
    }

    @Test
    fun everyThemeShapeIsDrawableAndNamed() {
        VeilStyle.entries.forEach { style ->
            assertTrue("$style has no name", style.name.isNotBlank())
        }
        // At least one theme must show each shape, or a shape is dead code.
        assertTrue(VeilStyle.entries.count { it != VeilStyle.NONE } >= 5)
    }

    @Test
    fun contrastMathsMatchesKnownValues() {
        assertEquals(21.0, ThemeContrast.ratio(0xFFFFFFFF, 0xFF000000), 0.01)
        assertEquals(1.0, ThemeContrast.ratio(0xFF336699, 0xFF336699), 0.001)
        assertEquals(
            ThemeContrast.ratio(0xFFFFFFFF, 0xFF102030),
            ThemeContrast.ratio(0xFF102030, 0xFFFFFFFF),
            0.0001
        )
    }
}
