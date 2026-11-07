package com.zamcan.faceit.data

import org.json.JSONObject

enum class LightDirection(val label: String, val x: Float, val y: Float) {
    UPPER_LEFT("Upper left", .12f, .12f),
    OVERHEAD("Overhead", .5f, .04f),
    UPPER_RIGHT("Upper right", .88f, .12f),
    SIDE_LEFT("Left", 0f, .42f),
    SIDE_RIGHT("Right", 1f, .42f)
}

/** Independent, static scene illumination. Disabled for legacy appearance compatibility. */
data class SceneLighting(
    val enabled: Boolean = false,
    val direction: LightDirection = LightDirection.UPPER_LEFT,
    val intensity: Float = .35f,
    val softness: Float = .65f,
    val warmth: Float = 0f
) {
    fun toJson() = JSONObject().apply {
        put("enabled", enabled)
        put("direction", direction.name)
        put("intensity", intensity.toDouble())
        put("softness", softness.toDouble())
        put("warmth", warmth.toDouble())
    }

    companion object {
        fun fromJson(json: JSONObject?): SceneLighting {
            if (json == null) return SceneLighting()
            fun number(key: String, fallback: Float, min: Float = 0f, max: Float = 1f): Float =
                json.optDouble(key, fallback.toDouble()).toFloat().takeIf { it.isFinite() }
                    ?.coerceIn(min, max) ?: fallback
            return SceneLighting(
                enabled = json.optBoolean("enabled", false),
                direction = runCatching { LightDirection.valueOf(json.optString("direction")) }
                    .getOrDefault(LightDirection.UPPER_LEFT),
                intensity = number("intensity", .35f),
                softness = number("softness", .65f),
                warmth = number("warmth", 0f, -1f, 1f)
            )
        }
    }
}
