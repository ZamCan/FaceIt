package com.zamcan.faceit.data

import android.content.Context
import android.graphics.Bitmap

/**
 * Which wallpaper is showing, decided in one place.
 *
 * Two packs used to exist — generated artwork and photographs — and every screen
 * that painted a wallpaper re-implemented the lookup, which is how the launcher
 * and the workspace could disagree about what the user had chosen. There is now
 * one pack, so there is one rule, and it lives here.
 *
 * Order of precedence: a wallpaper the user imported themselves, then a
 * photograph they picked from the bundled pack, then the theme's own photograph.
 */
object WallpaperPolicy {

    fun resolveId(environment: PersonalEnvironment, theme: FaceTheme): String? =
        environment.selectedArtworkFile?.takeIf { it.isNotBlank() }
            ?: environment.curatedPhotoId?.takeIf { it.isNotBlank() }
            ?: theme.wallpaperId.takeIf { it.isNotBlank() }

    /**
     * Load the wallpaper for [id] at a bounded size.
     *
     * Imported files arrive as a [java.io.File] path from the repository, and
     * bundled photographs come from the asset pack; the caller does not need to
     * know which, because the id prefix says so.
     */
    fun load(context: Context, id: String?, targetWidthPx: Int): Bitmap? {
        val key = id?.takeIf { it.isNotBlank() } ?: return null
        val photos = PhotoBitmaps(context)
        val bundled = PhotoLibrary.find(context, key)
        if (bundled != null) return photos.fullSize(bundled, targetWidthPx)
        return ArtworkRepository(context).list()
            .firstOrNull { it.id == key }
            ?.let { ArtworkRepository(context).fileFor(it.fileName) }
            ?.takeIf { it.isFile }
            ?.let { file ->
                android.graphics.BitmapFactory.decodeFile(
                    file.absolutePath,
                    android.graphics.BitmapFactory.Options().apply { inSampleSize = 2 }
                )
            }
    }
}
