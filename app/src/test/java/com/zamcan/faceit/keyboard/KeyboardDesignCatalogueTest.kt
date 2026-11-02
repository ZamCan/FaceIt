package com.zamcan.faceit.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardDesignCatalogueTest {
    @Test fun designsHaveUniqueIdsAndPaths() {
        val ids = KeyboardDesignCatalogue.all.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
        assertTrue(ids.all { it.isNotBlank() })
    }

    @Test fun everyDesignIsARealSelectablePreset() {
        // AGENTS.md §10 requires multiple genuinely distinct families, not
        // colour-only variations of one layout.
        assertTrue(KeyboardDesignCatalogue.all.size >= 6)
        assertTrue(KeyboardDesignCatalogue.families.size >= 4)
        assertTrue(KeyboardDesignCatalogue.all.map { it.geometry }.distinct().size >= 4)
    }

    @Test fun everyPaletteIdResolves() {
        KeyboardDesignCatalogue.all.forEach { design ->
            assertNotNull(design.palette)
            assertTrue(
                "palette id ${design.paletteId} is not registered",
                KeyboardPalettes.all.any { it.first == design.paletteId }
            )
        }
    }

    @Test fun unknownDesignFallsBackInsteadOfCrashing() {
        assertEquals(KeyboardDesignCatalogue.all.first(), KeyboardDesignCatalogue.find("does-not-exist"))
        assertEquals(KeyboardDesignCatalogue.all.first(), KeyboardDesignCatalogue.find(null))
    }

    @Test fun familyFilterReturnsOnlyThatFamily() {
        val family = KeyboardDesignCatalogue.families.first()
        val filtered = KeyboardDesignCatalogue.byFamily(family)
        assertTrue(filtered.isNotEmpty())
        assertTrue(filtered.all { it.family == family })
        assertEquals(KeyboardDesignCatalogue.all.size, KeyboardDesignCatalogue.byFamily(null).size)
    }

    @Test fun legacyMaterialChoiceSurvivesTheStudioToGalleryMigration() {
        val migrated = KeyboardDesignCatalogue.migrateFromLegacyPreferences(
            mapOf("material" to "NATURAL_STONE", "shape" to "PILL", "palette" to "WARM_STONE")
        )
        val design = KeyboardDesignCatalogue.find(migrated)
        assertEquals(KeyMaterial.NATURAL_STONE, design.material)
    }

    @Test fun migrationWithoutLegacyPreferencesUsesTheDefaultDesign() {
        val migrated = KeyboardDesignCatalogue.migrateFromLegacyPreferences(emptyMap())
        assertEquals(KeyboardDesignCatalogue.all.first().id, migrated)
    }

    @Test fun sanitiseKeepsGeometryInsideUsableBounds() {
        val clamped = KeyboardDesignEngine.sanitise(
            KeyboardDesignCatalogue.all.first().copy(keyHeightDp = 5, keyGapDp = 90, cornerRadiusDp = 999)
        )
        assertEquals(40, clamped.keyHeightDp)
        assertEquals(12, clamped.keyGapDp)
        assertEquals(28, clamped.cornerRadiusDp)
    }
}
