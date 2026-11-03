package com.zamcan.faceit.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChargingPreviewPolicyTest {
    @Test
    fun chargingAnimationRequiresTheChargingShelf() {
        StudioCategory.entries.filterNot { it == StudioCategory.CHARGING }.forEach { category ->
            assertFalse(category.name, shouldAnimateChargingPreview(category, true, true))
        }
    }

    @Test
    fun chargingAnimationRequiresTheScreenToBeResumed() {
        assertFalse(shouldAnimateChargingPreview(StudioCategory.CHARGING, false, true))
    }

    @Test
    fun chargingAnimationHonoursReducedMotionAndBatteryPolicy() {
        assertFalse(shouldAnimateChargingPreview(StudioCategory.CHARGING, true, false))
    }

    @Test
    fun chargingAnimationRunsOnlyWhenAllConditionsAllowIt() {
        assertTrue(shouldAnimateChargingPreview(StudioCategory.CHARGING, true, true))
    }

    @Test
    fun phaseAdvancesAndWrapsIntoUnitInterval() {
        assertEquals(0.518f, nextChargingPreviewPhase(0.5f), 0.0001f)
        assertEquals(0.008f, nextChargingPreviewPhase(0.99f), 0.0001f)
    }

    @Test
    fun previewChoicesPersistButRealWidgetInstancesRemainIndependent() {
        TopicCatalogue.styles.filter { it.category != StudioCategory.WIDGETS }.forEach { style ->
            assertTrue(style.id, shouldPersistStudioSelection(style))
        }
        TopicCatalogue.byCategory(StudioCategory.WIDGETS).forEach { style ->
            assertFalse(style.id, shouldPersistStudioSelection(style))
        }
    }

    @Test
    fun staleAndCrossCategorySelectionsFallBackToTheFirstValidStyle() {
        StudioCategory.entries.forEach { category ->
            val first = TopicCatalogue.byCategory(category).first().id
            assertEquals(first, resolveSelectedStyleId(category, "removed-style-id"))
            val foreignId = StudioCategory.entries.first { it != category }
                .let { TopicCatalogue.byCategory(it).first().id }
            assertEquals(first, resolveSelectedStyleId(category, foreignId))
        }
    }

    @Test
    fun validStoredSelectionIsPreserved() {
        StudioCategory.entries.forEach { category ->
            TopicCatalogue.byCategory(category).forEach { style ->
                assertEquals(style.id, resolveSelectedStyleId(category, style.id))
            }
        }
    }
}
