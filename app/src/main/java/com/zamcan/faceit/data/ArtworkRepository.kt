package com.zamcan.faceit.data

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class LocalArtwork(
    val id: String,
    val title: String,
    val fileName: String,
    val mimeType: String,
    val width: Int,
    val height: Int,
    val importedAt: Long
)

/**
 * Copies user-selected artwork into FaceIt's private files directory.
 * No external storage permission and no network access are required.
 */
class ArtworkRepository(context: Context) {
    private val app = context.applicationContext
    private val directory = File(app.filesDir, "artwork").apply { mkdirs() }
    private val prefs = app.getSharedPreferences("faceit_artwork", Context.MODE_PRIVATE)

    fun list(): List<LocalArtwork> {
        val raw = prefs.getString("items", "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val artwork = LocalArtwork(
                        id = item.getString("id"),
                        title = item.getString("title"),
                        fileName = item.getString("fileName"),
                        mimeType = item.getString("mimeType"),
                        width = item.getInt("width"),
                        height = item.getInt("height"),
                        importedAt = item.getLong("importedAt")
                    )
                    if (File(directory, artwork.fileName).isFile) add(artwork)
                }
            }
        }.getOrDefault(emptyList())
    }

    @Synchronized
    fun importImage(context: Context, uri: Uri): LocalArtwork {
        val resolver = context.contentResolver
        val displayName = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
            ?: "FaceIt artwork"
        val extensionMime = when (displayName.substringAfterLast('.', "").lowercase()) {
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "webp" -> "image/webp"
            else -> null
        }
        val mime = resolver.getType(uri)?.lowercase() ?: extensionMime
            ?: throw IllegalArgumentException("The selected file is not a recognised image.")
        require(mime in SUPPORTED_TYPES) { "Choose a PNG, JPEG or WebP image." }

        val id = UUID.randomUUID().toString()
        val extension = when (mime) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val fileName = "$id.$extension"
        val target = File(directory, fileName)
        var total = 0L

        try {
            val input = resolver.openInputStream(uri)
                ?: throw IllegalArgumentException("FaceIt could not open this image.")
            input.use { source ->
                FileOutputStream(target).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val count = source.read(buffer)
                        if (count < 0) break
                        total += count
                        require(total <= MAX_BYTES) { "Images must be 30 MB or smaller." }
                        output.write(buffer, 0, count)
                    }
                    output.fd.sync()
                }
            }

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(target.absolutePath, bounds)
            require(bounds.outWidth >= 320 && bounds.outHeight >= 320) {
                "This image is too small. Choose an image at least 320 × 320 pixels."
            }
            require(bounds.outWidth <= 20000 && bounds.outHeight <= 20000) {
                "This image has unsupported dimensions."
            }

            val artwork = LocalArtwork(
                id = id,
                title = displayName.substringBeforeLast('.', displayName).take(80),
                fileName = fileName,
                mimeType = mime,
                width = bounds.outWidth,
                height = bounds.outHeight,
                importedAt = System.currentTimeMillis()
            )
            val updated = list() + artwork
            val array = JSONArray()
            updated.forEach { item ->
                array.put(JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("fileName", item.fileName)
                    put("mimeType", item.mimeType)
                    put("width", item.width)
                    put("height", item.height)
                    put("importedAt", item.importedAt)
                })
            }
            check(prefs.edit().putString("items", array.toString()).commit()) {
                "Could not save the artwork catalogue."
            }
            return artwork
        } catch (error: Throwable) {
            target.delete()
            throw error
        }
    }

    fun fileFor(fileName: String): File? {
        if (fileName.isBlank() || fileName.contains('/') || fileName.contains('\\')) return null
        return File(directory, fileName).takeIf { it.isFile }
    }

    @Synchronized
    fun delete(id: String): Boolean {
        val current = list()
        val item = current.firstOrNull { it.id == id } ?: return false
        val removed = current.filterNot { it.id == id }
        val array = JSONArray()
        removed.forEach { item ->
            array.put(JSONObject().apply {
                put("id", item.id); put("title", item.title); put("fileName", item.fileName)
                put("mimeType", item.mimeType); put("width", item.width)
                put("height", item.height); put("importedAt", item.importedAt)
            })
        }
        if (!prefs.edit().putString("items", array.toString()).commit()) return false
        File(directory, item.fileName).delete()
        return true
    }

    companion object {
        private const val MAX_BYTES = 30L * 1024L * 1024L
        private val SUPPORTED_TYPES = setOf("image/png", "image/jpeg", "image/webp")
    }
}
