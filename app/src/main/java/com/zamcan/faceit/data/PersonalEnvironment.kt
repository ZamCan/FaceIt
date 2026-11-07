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
    /**
     * Bundled photograph id from `assets/photos`.
     *
     * The drawn artwork pack and its object renders were removed on instruction;
     * every wallpaper that ships with the app is now a photograph.
     */
    val curatedPhotoId: String? = null,
    /** Explicitly show the editable procedural scene instead of a theme photograph. */
    val useProceduralBackground: Boolean = false,
    val objectScale: Float = 1f,
    val objectX: Float = .5f,
    val objectY: Float = .38f,
    val objectOpacity: Float = 1f,
    val cropX: Float = .5f,
    val cropY: Float = .5f,
    val lighting: SceneLighting = SceneLighting(),
    /**
     * The name engine. Whatever the user types is set at render time in one of
     * the metal finishes, so a name is never a bundled picture and never a
     * look-alike character. Off, the wallpaper is just the photograph.
     */
    val nameEnabled: Boolean = false,
    val nameText: String = "Naomi",
    val nameFinishId: String = "rose_gold",
    val nameScriptId: String = "script",
    val nameSettingId: String = "normal",
    /** Vertical position of the name, 0 = top of the screen, 1 = bottom. */
    val nameAnchor: Float = 0.42f,
    val namePlaque: Boolean = true,
    /** Photograph the name sits on, or null for the finish's own backdrop. */
    val nameGroundId: String? = null,
    /** Ordered composition layers; the renderer draws these back to front. */
    val layers: List<CompositionLayer> = CompositionLayer.defaults()
) {
    /** The name to draw, or null when the user has it switched off. */
    fun nameDesignOrNull(): com.zamcan.faceit.graphics.NameDesign? =
        if (!nameEnabled) null else com.zamcan.faceit.graphics.NameDesign(
            text = nameText,
            finishId = nameFinishId,
            scriptId = nameScriptId,
            settingId = nameSettingId,
            anchor = nameAnchor,
            uppercase = true,
            plaque = namePlaque,
            groundId = nameGroundId
        )

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
        put("curatedPhotoId", curatedPhotoId)
        put("useProceduralBackground", useProceduralBackground)
        put("objectScale", objectScale.toDouble())
        put("objectX", objectX.toDouble())
        put("objectY", objectY.toDouble())
        put("objectOpacity", objectOpacity.toDouble())
        put("cropX", cropX.toDouble())
        put("cropY", cropY.toDouble())
        put("lighting", lighting.toJson())
        put("nameEnabled", nameEnabled)
        put("nameText", nameText)
        put("nameFinishId", nameFinishId)
        put("nameScriptId", nameScriptId)
        put("nameSettingId", nameSettingId)
        put("nameAnchor", nameAnchor.toDouble())
        put("namePlaque", namePlaque)
        put("nameGroundId", nameGroundId)
        put("layers", org.json.JSONArray().apply { layers.forEach { put(it.toJson()) } })
    }.toString()

    companion object {
        private fun boundedFloat(json: JSONObject, key: String, fallback: Float, min: Float, max: Float): Float =
            json.optDouble(key, fallback.toDouble()).toFloat().takeIf { it.isFinite() }?.coerceIn(min, max) ?: fallback

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
                    // Older saved environments carry `curatedArtworkId` and
                    // `curatedObjectId` from the removed pack. They are dropped
                    // rather than migrated: the files no longer exist, and a
                    // dangling id would resolve to no wallpaper at all.
                    useProceduralBackground = json.optBoolean("useProceduralBackground", false),
                    objectScale = boundedFloat(json, "objectScale", 1f, .25f, 2f),
                    objectX = boundedFloat(json, "objectX", .5f, 0f, 1f),
                    objectY = boundedFloat(json, "objectY", .38f, 0f, 1f),
                    objectOpacity = boundedFloat(json, "objectOpacity", 1f, 0f, 1f),
                    cropX = boundedFloat(json, "cropX", .5f, 0f, 1f),
                    cropY = boundedFloat(json, "cropY", .5f, 0f, 1f),
                    lighting = SceneLighting.fromJson(json.optJSONObject("lighting")),
                    nameEnabled = json.optBoolean("nameEnabled", false),
                    nameText = json.optString("nameText", "Naomi"),
                    nameFinishId = json.optString("nameFinishId", "rose_gold"),
                    nameScriptId = json.optString("nameScriptId", "script"),
                    nameSettingId = json.optString("nameSettingId", "normal"),
                    nameAnchor = json.optDouble("nameAnchor", 0.42).toFloat()
                        .coerceIn(0.08f, 0.92f),
                    namePlaque = json.optBoolean("namePlaque", true),
                    nameGroundId = json.optString("nameGroundId", "")
                        .takeIf { it.isNotBlank() && it != "null" },
                    curatedPhotoId = json.optString("curatedPhotoId", "")
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

