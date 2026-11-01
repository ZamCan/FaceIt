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
    val dialerStyle: String = "Ceramic Sculpt",
    val resourceProfile: String = "Balanced",
    val selectedArtworkFile: String? = null
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
        put("selectedArtworkFile", selectedArtworkFile)
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
                    selectedArtworkFile = json.optString("selectedArtworkFile", "")
                        .takeIf { it.isNotBlank() && it != "null" }
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
