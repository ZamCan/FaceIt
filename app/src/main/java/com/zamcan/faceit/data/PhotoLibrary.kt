package com.zamcan.faceit.data

import android.content.Context
import org.json.JSONObject

/**
 * The photographic wallpaper pack: real photographs under CC0, public domain or
 * CC BY licences, bundled with the app.
 *
 * This is now the only bundled wallpaper pack. The drawn artwork pack that used
 * to ship alongside it was removed on instruction, so every wallpaper in the app
 * is a photograph — which changes what the app owes the people who made them.
 *
 * A photograph belongs to its photographer, and we may only ship it because a
 * licence permits it. CC BY and CC BY-SA both require credit and a licence link
 * wherever the work is used, so every entry here carries its creator, its licence
 * and a link back to the source page, and the UI shows them.
 *
 * Some entries are share-alike. Those are flagged rather than hidden, because a
 * share-alike obligation can affect how a modified work may be distributed and
 * that is a release decision, not something to discover later.
 */
data class PhotoWallpaper(
    val id: String,
    val category: String,
    val subject: String,
    val title: String,
    val creator: String,
    val creatorUrl: String,
    val licence: String,
    val licenceVersion: String,
    val licenceUrl: String,
    val source: String,
    val shareAlike: Boolean,
    val asset: String,
    val thumbnail: String,
    val width: Int,
    val height: Int,
    /**
     * True when this image was generated for the app rather than photographed by
     * someone. Generated work is still CC0 and still bundled, but it is marked so
     * the gallery never presents it as a photograph with an author.
     */
    val synthetic: Boolean = false
) {
    /** The credit line shown under a photograph, in the form the licence expects. */
    val credit: String
        get() = "$title — $creator, ${licence.uppercase()} $licenceVersion"

    /** Human-readable category, for the gallery's filter row. */
    val categoryLabel: String
        get() = category.replaceFirstChar { it.uppercase() }

    /**
     * Whether this photograph may be bundled at all.
     *
     * The pipeline already refuses non-commercial, no-derivatives and
     * all-rights-reserved files, but the check is repeated where the app reads
     * the index: a licence mistake that reaches a shipped build is a legal
     * problem, and it is cheaper to drop the tile than to trust the generator.
     */
    val isShippable: Boolean
        get() {
            val name = licence.lowercase()
            if (SHIPPABLE.none { name == it || name.startsWith("$it-") }) return false
            val tokens = name.split('-', '_', ' ')
            if (tokens.any { it == "nc" || it == "nd" || it == "gfdl" }) return false
            if (synthetic) return licenceUrl.isNotBlank()
            return creator.isNotBlank() && licenceUrl.isNotBlank()
        }

    private companion object {
        val SHIPPABLE = listOf("cc0", "pdm", "publicdomain", "cc-by", "cc-by-sa")
    }
}

/**
 * Reads [assets/photos/photo_index.json].
 *
 * Parsing is done once and cached; a malformed or absent index yields an empty
 * list rather than a crash, so an app built without the photo pack still runs —
 * it simply shows no photographic wallpapers.
 */
object PhotoLibrary {

    private var cached: List<PhotoWallpaper>? = null

    fun all(context: Context): List<PhotoWallpaper> {
        cached?.let { return it }
        val parsed = runCatching { parse(read(context)) }.getOrDefault(emptyList())
        cached = parsed
        return parsed
    }

    fun byCategory(context: Context, category: String?): List<PhotoWallpaper> {
        val list = all(context)
        return if (category.isNullOrBlank()) list else list.filter { it.category == category }
    }

    fun categories(context: Context): List<String> =
        all(context).map { it.category }.distinct().sorted()

    fun find(context: Context, id: String?): PhotoWallpaper? =
        all(context).firstOrNull { it.id == id }

    /** True when any bundled photograph carries a share-alike obligation. */
    fun hasShareAlike(context: Context): Boolean = all(context).any { it.shareAlike }

    private fun read(context: Context): String =
        context.assets.open("photos/photo_index.json").bufferedReader().use { it.readText() }

    /** Parse an index body. Pure, so it can be tested without an Android context. */
    internal fun parse(raw: String): List<PhotoWallpaper> {
        val root = JSONObject(raw)
        val photos = root.optJSONArray("photos") ?: return emptyList()
        val parsed = buildList {
            for (index in 0 until photos.length()) {
                val item = photos.optJSONObject(index) ?: continue
                val id = item.optString("id")
                val asset = item.optString("asset")
                if (id.isBlank() || asset.isBlank()) continue
                add(
                    PhotoWallpaper(
                        id = id,
                        category = item.optString("category", "other"),
                        subject = item.optString("subject", ""),
                        title = item.optString("title", id),
                        creator = item.optString("creator", "Unknown"),
                        creatorUrl = item.optString("creatorUrl", ""),
                        licence = item.optString("licence", "cc-by"),
                        licenceVersion = item.optString("licenceVersion", ""),
                        licenceUrl = item.optString("licenceUrl", ""),
                        source = item.optString("source", ""),
                        shareAlike = item.optBoolean("shareAlike", false),
                        asset = asset,
                        thumbnail = item.optString("thumbnail", asset),
                        width = item.optInt("width", 0),
                        height = item.optInt("height", 0),
                        synthetic = item.optBoolean("synthetic", false)
                    )
                )
            }
        }
        // An incomplete credit or an unusable licence is dropped here rather than
        // shown and apologised for later.
        return parsed.filter { it.isShippable }
    }
}
