package com.zamcan.faceit.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FaceItThemePresetsTest {
    @Test fun everyThemeHasUniqueId() {
        val ids = FaceItThemePresets.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test fun everyStyleStudioThemeHasAnAppPalette() {
        listOf("theme-pearl-bloom", "theme-cat-garden", "theme-niqab-atelier", "theme-rose-glass", "theme-obsidian", "feminine-cat-garden", "feminine-petal-pop", "feminine-pearl-atelier", "feminine-lilac-luxe")
            .forEach { id -> assertNotNull(FaceItThemePresets.findOrNull(id)) }
    }

    @Test fun defaultThemeIsAvailableAndUnknownIdsFallBackSafely() {
        assertNotNull(FaceItThemePresets.findOrNull(FaceItThemePresets.DEFAULT_ID))
        assertTrue(FaceItThemePresets.find("missing").id == FaceItThemePresets.DEFAULT_ID)
    }
}
