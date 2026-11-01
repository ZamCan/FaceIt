package com.zamcan.faceit.resource

import org.junit.Assert.assertEquals
import org.junit.Test

class AdaptiveResourcePolicyTest {
    @Test fun hiddenSurfaceNeverKeepsRendererAlive() {
        val result = AdaptiveResourcePolicy.decide(
            ResourceProfile.IMMERSIVE,
            DeviceConditions(surfaceVisible = false, interactive3dRequested = true)
        )
        assertEquals(RenderPermission.SUSPENDED, result)
    }

    @Test fun unselectedThreeDFeatureRemainsSuspended() {
        val result = AdaptiveResourcePolicy.decide(
            ResourceProfile.IMMERSIVE,
            DeviceConditions(surfaceVisible = true, interactive3dRequested = false)
        )
        assertEquals(RenderPermission.SUSPENDED, result)
    }

    @Test fun essentialProfileNeverStartsInteractiveRenderer() {
        val result = AdaptiveResourcePolicy.decide(
            ResourceProfile.ESSENTIAL,
            DeviceConditions(surfaceVisible = true, interactive3dRequested = true)
        )
        assertEquals(RenderPermission.STATIC_ONLY, result)
    }

    @Test fun severeThermalPressureFallsBackToStatic() {
        val result = AdaptiveResourcePolicy.decide(
            ResourceProfile.IMMERSIVE,
            DeviceConditions(
                surfaceVisible = true,
                interactive3dRequested = true,
                thermalPressure = ThermalPressure.SEVERE
            )
        )
        assertEquals(RenderPermission.STATIC_ONLY, result)
    }

    @Test fun batterySaverReducesRenderingQuality() {
        val result = AdaptiveResourcePolicy.decide(
            ResourceProfile.IMMERSIVE,
            DeviceConditions(
                surfaceVisible = true,
                interactive3dRequested = true,
                batterySaver = true
            )
        )
        assertEquals(RenderPermission.REDUCED, result)
    }

    @Test fun balancedProfileUsesStandardRenderingInNormalConditions() {
        val result = AdaptiveResourcePolicy.decide(
            ResourceProfile.BALANCED,
            DeviceConditions(surfaceVisible = true, interactive3dRequested = true)
        )
        assertEquals(RenderPermission.STANDARD, result)
    }

    @Test fun immersiveProfileGetsHighQualityOnlyWhenConditionsAllow() {
        val result = AdaptiveResourcePolicy.decide(
            ResourceProfile.IMMERSIVE,
            DeviceConditions(surfaceVisible = true, interactive3dRequested = true)
        )
        assertEquals(RenderPermission.HIGH_QUALITY, result)
    }
}
