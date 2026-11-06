package com.zamcan.faceit.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class SavedComposition(val id: String, val environment: PersonalEnvironment, val savedAt: Long)

object SavedCompositionCodec {
    fun encode(items: List<SavedComposition>): String = JSONArray().apply {
        items.forEach { item -> put(JSONObject().apply {
            put("id", item.id)
            put("environment", JSONObject(item.environment.toJson()))
            put("savedAt", item.savedAt)
        }) }
    }.toString()

    /** Malformed records are reported, never silently replaced by an empty collection. */
    fun decode(raw: String): List<SavedComposition> {
        val array = JSONArray(raw)
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            val id = item.getString("id")
            require(id.isNotBlank()) { "A saved scene has no identifier." }
            SavedComposition(id, PersonalEnvironment.fromJson(item.getJSONObject("environment").toString()),
                item.getLong("savedAt"))
        }.also { items -> require(items.map { it.id }.distinct().size == items.size) }
    }
}

class CompositionLibrary(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("faceit_saved_scenes", Context.MODE_PRIVATE)
    fun list(): List<SavedComposition> =
        SavedCompositionCodec.decode(preferences.getString("items", "[]") ?: "[]").sortedByDescending { it.savedAt }

    @Synchronized
    fun save(environment: PersonalEnvironment): SavedComposition {
        require(environment.name.isNotBlank()) { "Give your scene a name." }
        val items = list()
        require(items.size < 100) { "Your library has 100 scenes. Remove a scene before saving another." }
        val item = SavedComposition(UUID.randomUUID().toString(), environment, System.currentTimeMillis())
        write(items + item)
        return item
    }

    @Synchronized
    fun rename(id: String, name: String) {
        val title = name.trim()
        require(title.isNotEmpty() && title.length <= 80) { "Use a name between 1 and 80 characters." }
        val items = list()
        require(items.any { it.id == id }) { "This scene no longer exists." }
        write(items.map { if (it.id == id) it.copy(environment = it.environment.copy(name = title)) else it })
    }

    @Synchronized
    fun duplicate(id: String) {
        val item = list().firstOrNull { it.id == id } ?: error("This scene no longer exists.")
        save(item.environment.copy(name = (item.environment.name.take(73) + " (copy)")))
    }

    @Synchronized
    fun delete(id: String) = write(list().filterNot { it.id == id })

    private fun write(items: List<SavedComposition>) {
        check(preferences.edit().putString("items", SavedCompositionCodec.encode(items)).commit()) {
            "The scene library could not be saved. Your previous scenes were preserved."
        }
    }
}

