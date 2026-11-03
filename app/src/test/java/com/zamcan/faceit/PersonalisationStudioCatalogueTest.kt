package com.zamcan.faceit

import com.zamcan.faceit.ui.PersonalisationCatalogue
import com.zamcan.faceit.ui.StudioCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalisationStudioCatalogueTest {
    @Test fun everyStudioHasAUniqueStableId() {
        val ids = PersonalisationCatalogue.styles.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test fun everyStudioCategoryHasAtLeastOneDesign() {
        StudioCategory.entries.forEach { category ->
            assertTrue("$category has no designs", PersonalisationCatalogue.byCategory(category).isNotEmpty())
        }
    }

    @Test fun niqabInspiredFrameHasSixIndependentColourChoices() {
        assertEquals(6, PersonalisationCatalogue.frameColours.size)
        assertEquals(setOf("Onyx", "Rose", "Plum", "Pearl", "Sage", "Sand"), PersonalisationCatalogue.frameColours.map { it.first }.toSet())
    }

    @Test fun feminineCollectionIncludesCuteAnimalAndColourfulDesigns() {
        assertNotNull(PersonalisationCatalogue.find("theme-cat-garden"))
        assertNotNull(PersonalisationCatalogue.find("icon-cat"))
        assertNotNull(PersonalisationCatalogue.find("icon-petal"))
        assertNotNull(PersonalisationCatalogue.find("feminine-cat-garden"))
        assertNotNull(PersonalisationCatalogue.find("feminine-niqab-frame"))
        assertTrue(PersonalisationCatalogue.find("theme-cat-garden")!!.accent != PersonalisationCatalogue.find("theme-obsidian")!!.accent)
    }

    @Test fun systemBoundariesAreVisibleInCapabilityLabels() {
        assertEquals("In-app launcher concept", PersonalisationCatalogue.find("apps-niqab")!!.capability)
        assertEquals("Charging animation preview", PersonalisationCatalogue.find("charge-aurora")!!.capability)
        assertEquals("In-app visual designs", PersonalisationCatalogue.find("keyboard-gallery")!!.capability)
    }
}
