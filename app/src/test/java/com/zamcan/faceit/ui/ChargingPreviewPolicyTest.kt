package com.zamcan.faceit.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChargingPreviewPolicyTest {

    @Test
    fun chargingAnimationRequiresTheChargingShelf() {
        StudioCategory.entries
            .filterNot { it == StudioCategory.CHARGING }
            .forEach { category ->
                assertFalse(
                    "$category must remain static",
                    shouldAnimateChargingPreview(category, isResumed = true, animationsEnabled = true)
                )
            }
    }

    @Test
    fun chargingAnimationRequiresTheScreenToBeResumed() {
        assertFalse(
            shouldAnimateChargingPreview(
                StudioCategory.CHARGING,
                isResumed = false,
                animationsEnabled = true
            )
        )
    }

    @Test
    fun chargingAnimationHonoursReducedMotionAndBatteryPolicy() {
        assertFalse(
            shouldAnimateChargingPreview(
                StudioCategory.CHARGING,
                isResumed = true,
                animationsEnabled = false
            )
        )
    }

    @Test
    fun chargingAnimationRunsOnlyWhenAllConditionsAllowIt() {
        assertTrue(
            shouldAnimateChargingPreview(
                StudioCategory.CHARGING,
                isResumed = true,
                animationsEnabled = true
            )
        )
    }

    @Test
    fun phaseAdvancesAndWrapsIntoUnitInterval() {
        assertEquals(0.518f, nextChargingPreviewPhase(0.5f), 0.0001f)
        assertEquals(0.008f, nextChargingPreviewPhase(0.99f), 0.0001f)
    }
}
