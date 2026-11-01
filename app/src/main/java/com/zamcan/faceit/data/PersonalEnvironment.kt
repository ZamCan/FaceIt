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
    val resourceProfile: String = "Balanced"
) {
    fun toJson() = JSONObject().apply {
        put("name", name); put("background", background); put("mainObject", mainObject)
        put("material", material); put("motion", motion); put("widgetsEnabled", widgetsEnabled)
        put("iconsEnabled", iconsEnabled); put("dialerStyle", dialerStyle); put("resourceProfile", resourceProfile)
    }.toString()
    companion object {
        fun fromJson(raw: String?): PersonalEnvironment {
            if (raw.isNullOrBlank()) return PersonalEnvironment()
            return runCatching {
                val j = JSONObject(raw)
                PersonalEnvironment(
                    name = j.optString("name", "My FaceIt World"),
                    background = j.optString("background", "Football"),
                    mainObject = j.optString("mainObject", "Lion sculpture"),
                    material = j.optString("material", "Natural stone"),
                    motion = j.optString("motion", "Subtle"),
                    widgetsEnabled = j.optBoolean("widgetsEnabled", true),
                    iconsEnabled = j.optBoolean("iconsEnabled", true),
                    dialerStyle = j.optString("dialerStyle", "Ceramic Sculpt"),
                    resourceProfile = j.optString("resourceProfile", "Balanced")
                )
            }.getOrDefault(PersonalEnvironment())
        }
    }
}
class EnvironmentStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("faceit_local", Context.MODE_PRIVATE)
    fun load() = PersonalEnvironment.fromJson(prefs.getString("environment", null))
    fun save(value: PersonalEnvironment) { prefs.edit().putString("environment", value.toJson()).apply() }
}
