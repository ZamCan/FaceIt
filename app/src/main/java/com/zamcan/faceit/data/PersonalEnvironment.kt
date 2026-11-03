package com.zamcan.faceit.data

import android.content.Context
import org.json.JSONObject

data class PersonalEnvironment(
    val name: String = "The Stone Guardian",
    val background: String = "Football",
    val mainObject: String = "Lion sculpture",
    val material: String = "Natural stone",
    val motion: String = "Subtle",
    val widgetsEnabled: Boolean = true,
    val iconsEnabled: Boolean = true,
    /**
     * Legacy field kept only so previously saved environments still load.
     * Keypad appearance is now [keypadDesignId]; nothing reads this for rendering.
     */
    @Deprecated("Use keypadDesignId. Retained for loading old saved environments.")
    val dialerStyle: String = "Ceramic Sculpt",
    val resourceProfile: String = "Balanced",
    /** Selected complete theme; every field below can still be overridden on its own. */
    val themeId: String = "pearl_mint",
    val wallpaperMode: ThemeWallpaperMode = ThemeWallpaperMode.FOLLOW_THEME,
    /** Keypad appearance preset. Presentation only — Face It is not a dialler. */
    val keypadDesignId: String = "ceramic_sculpt",
    /** Writing style for the SMS composer. Real typography, not look-alike glyphs. */
    val writingStyleId: String = "plain",
    /** Named frame colour for the draped display frame, or "None" to turn it off. */
    val frameColourId: String = "None",
    val selectedArtworkFile: String? = null,
    val selectedObjectFile: String? = null,
    /** Curated bundled wallpaper id from the shipped pack, or null. */
    val curatedArtworkId: String? = null,
    /** Curated object artwork id from `assets/objects`, or null. */
    val curatedObjectId: String? = null,
    /** Ordered composition layers; the renderer draws these back to front. */
    val layers: List<CompositionLayer> = CompositionLayer.defaults()
) {
    fun toJson() = JSONObject().apply {
        put("name", name)
        put("background", background)
        put("mainObject", mainObject)
        put("material", material)
        put("motion", motion)
        put("widgetsEnabled", widgetsEnabled)
        put("iconsEnabled", iconsEnabled)
        put("dialerStyle", dialerStyle)
        put("resourceProfile", resourceProfile)
        put("themeId", themeId)
        put("wallpaperMode", wallpaperMode.name)
        put("keypadDesignId", keypadDesignId)
        put("writingStyleId", writingStyleId)
        put("frameColourId", frameColourId)
        put("selectedArtworkFile", selectedArtworkFile)
        put("selectedObjectFile", selectedObjectFile)
        put("curatedArtworkId", curatedArtworkId)
        put("curatedObjectId", curatedObjectId)
        put("layers", org.json.JSONArray().apply { layers.forEach { put(it.toJson()) } })
    }.toString()

    companion object {
        fun fromJson(raw: String?): PersonalEnvironment {
            if (raw.isNullOrBlank()) return PersonalEnvironment()
            return runCatching {
                val json = JSONObject(raw)
                PersonalEnvironment(
                    name = json.optString("name", "The Stone Guardian"),
                    background = json.optString("background", "Football"),
                    mainObject = json.optString("mainObject", "Lion sculpture"),
                    material = json.optString("material", "Natural stone"),
                    motion = json.optString("motion", "Subtle"),
                    widgetsEnabled = json.optBoolean("widgetsEnabled", true),
                    iconsEnabled = json.optBoolean("iconsEnabled", true),
                    dialerStyle = json.optString("dialerStyle", "Ceramic Sculpt"),
                    resourceProfile = json.optString("resourceProfile", "Balanced"),
                    themeId = json.optString("themeId", "pearl_mint"),
                    wallpaperMode = runCatching {
                        ThemeWallpaperMode.valueOf(json.optString("wallpaperMode", "FOLLOW_THEME"))
                    }.getOrDefault(ThemeWallpaperMode.FOLLOW_THEME),
                    keypadDesignId = json.optString("keypadDesignId", "ceramic_sculpt"),
                    writingStyleId = json.optString("writingStyleId", "plain"),
                    frameColourId = json.optString("frameColourId", "None"),
                    selectedArtworkFile = json.optString("selectedArtworkFile", "")
                        .takeIf { it.isNotBlank() && it != "null" },
                    selectedObjectFile = json.optString("selectedObjectFile", "")
                        .takeIf { it.isNotBlank() && it != "null" },
                    curatedArtworkId = json.optString("curatedArtworkId", "")
                        .takeIf { it.isNotBlank() && it != "null" },
                    curatedObjectId = json.optString("curatedObjectId", "")
                        .takeIf { it.isNotBlank() && it != "null" },
                    layers = json.optJSONArray("layers")?.let { array ->
                        buildList {
                            for (i in 0 until array.length()) {
                                add(CompositionLayer.fromJson(array.getJSONObject(i)))
                            }
                        }
                    }?.takeIf { it.isNotEmpty() } ?: CompositionLayer.defaults()
                )
            }.getOrDefault(PersonalEnvironment())
        }
    }
}

class EnvironmentStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("faceit_local", Context.MODE_PRIVATE)
    fun load() = PersonalEnvironment.fromJson(prefs.getString("environment", null))
    fun save(value: PersonalEnvironment) {
        prefs.edit().putString("environment", value.toJson()).apply()
    }
}
