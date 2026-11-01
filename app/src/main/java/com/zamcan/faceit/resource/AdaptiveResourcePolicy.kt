package com.zamcan.faceit.resource

enum class ResourceProfile { ESSENTIAL, BALANCED, IMMERSIVE }
enum class ThermalPressure { NONE, LIGHT, MODERATE, SEVERE }
enum class RenderPermission { SUSPENDED, STATIC_ONLY, REDUCED, STANDARD, HIGH_QUALITY }

data class DeviceConditions(
    val surfaceVisible: Boolean,
    val interactive3dRequested: Boolean,
    val batterySaver: Boolean = false,
    val lowMemory: Boolean = false,
    val thermalPressure: ThermalPressure = ThermalPressure.NONE
)

/**
 * Deterministic resource policy. A feature must be visible and explicitly active
 * before it can receive permission to initialise an interactive renderer.
 */
object AdaptiveResourcePolicy {
    fun decide(profile: ResourceProfile, conditions: DeviceConditions): RenderPermission {
        if (!conditions.surfaceVisible || !conditions.interactive3dRequested) {
            return RenderPermission.SUSPENDED
        }
        if (profile == ResourceProfile.ESSENTIAL) return RenderPermission.STATIC_ONLY
        if (conditions.thermalPressure == ThermalPressure.SEVERE || conditions.lowMemory) {
            return RenderPermission.STATIC_ONLY
        }
        if (conditions.batterySaver ||
            conditions.thermalPressure == ThermalPressure.MODERATE ||
            conditions.thermalPressure == ThermalPressure.LIGHT
        ) {
            return RenderPermission.REDUCED
        }
        return if (profile == ResourceProfile.IMMERSIVE) {
            RenderPermission.HIGH_QUALITY
        } else {
            RenderPermission.STANDARD
        }
    }
}
