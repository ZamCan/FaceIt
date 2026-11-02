package com.zamcan.faceit.data

import org.json.JSONObject

/**
 * One independently editable layer of a saved composition.
 *
 * AGENTS.md §6 requires that changing one layer never resets another, so every
 * layer owns its own source and its own settings rather than sharing a single
 * "theme" object. The renderer walks this list back to front.
 */
enum class LayerKind(val label: String) {
    BACKGROUND("Background"),
    MATERIAL("Material finish"),
    OBJECT("Main object"),
    LIGHT("Lighting")
}

data class CompositionLayer(
    val kind: LayerKind,
    val enabled: Boolean = true,
    /** Curated asset id, imported file name, or a built-in recipe name. */
    val source: String? = null,
    /** How [source] should be resolved by the renderer. */
    val sourceType: LayerSource = LayerSource.BUILT_IN,
    val intensity: Float = 1f
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("kind", kind.name)
        put("enabled", enabled)
        put("source", source)
        put("sourceType", sourceType.name)
        put("intensity", intensity.toDouble())
    }

    companion object {
        fun fromJson(json: JSONObject) = CompositionLayer(
            kind = runCatching { LayerKind.valueOf(json.getString("kind")) }
                .getOrDefault(LayerKind.BACKGROUND),
            enabled = json.optBoolean("enabled", true),
            source = json.optString("source", "").takeIf { it.isNotBlank() && it != "null" },
            sourceType = runCatching { LayerSource.valueOf(json.getString("sourceType")) }
                .getOrDefault(LayerSource.BUILT_IN),
            intensity = json.optDouble("intensity", 1.0).toFloat().coerceIn(0f, 1f)
        )

        fun defaults(): List<CompositionLayer> = listOf(
            CompositionLayer(LayerKind.BACKGROUND),
            CompositionLayer(LayerKind.MATERIAL),
            CompositionLayer(LayerKind.OBJECT),
            CompositionLayer(LayerKind.LIGHT)
        )
    }
}

enum class LayerSource { BUILT_IN, CURATED, IMPORTED }
