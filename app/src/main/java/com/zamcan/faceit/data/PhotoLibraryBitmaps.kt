package com.zamcan.faceit.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache

/**
 * Bitmap access for the bundled photograph pack — the only wallpaper pack.
 *
 * Photographs are far heavier than the drawn tiles this app used to ship, so this
 * exists to keep them out of the heap unless they are on screen: thumbnails are
 * decoded sampled to the display width and held in a small LRU, and the
 * full-size file is decoded only when a wallpaper is actually applied.
 *
 * Decoding always reads the bounds first and samples from there, so a 1920 px
 * JPEG never lands in memory to fill a 320 px tile.
 */
class PhotoBitmaps(private val context: Context) {

    private val cache = object : LruCache<String, Bitmap>(MAX_CACHE_BYTES) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.allocationByteCount
    }

    fun thumbnail(photo: PhotoWallpaper, targetWidthPx: Int = 320): Bitmap? {
        val key = "${photo.id}@$targetWidthPx"
        cache.get(key)?.let { if (!it.isRecycled) return it }
        val bitmap = decode(photo.thumbnail, targetWidthPx) ?: return null
        cache.put(key, bitmap)
        return bitmap
    }

    /** Full-size decode, for applying the wallpaper or filling the preview. */
    fun fullSize(photo: PhotoWallpaper, targetWidthPx: Int = 1440): Bitmap? =
        decode(photo.asset, targetWidthPx)

    /**
     * Decode any bundled asset at a bounded sample size.
     *
     * Kept public so a caller holding a path rather than a [PhotoWallpaper] — the
     * launcher, for instance — still goes through the same sampling rule.
     */
    fun assetBitmap(path: String, targetWidthPx: Int): Bitmap? = decode(path, targetWidthPx)

    private fun decode(path: String, targetWidthPx: Int): Bitmap? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.assets.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0) return null

        var sample = 1
        while (bounds.outWidth / (sample * 2) >= targetWidthPx.coerceAtLeast(1)) {
            sample *= 2
        }
        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        context.assets.open(path).use { BitmapFactory.decodeStream(it, null, options) }
    }.getOrNull()

    fun trimMemory() {
        // Compose and launcher consumers may still display these bitmaps. Evict
        // only our references; recycling here invalidates a visible image.
        cache.evictAll()
    }

    private companion object {
        // Bound actual allocation, including unusually tall or high-density thumbnails.
        const val MAX_CACHE_BYTES = 8 * 1024 * 1024
    }
}
