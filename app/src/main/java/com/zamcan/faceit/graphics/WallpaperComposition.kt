package com.zamcan.faceit.graphics

import android.content.Context
import android.graphics.Bitmap
import com.zamcan.faceit.data.ArtworkRepository
import com.zamcan.faceit.data.CompositionBackground
import com.zamcan.faceit.data.CompositionBackgroundPolicy
import com.zamcan.faceit.data.PersonalEnvironment
import com.zamcan.faceit.data.PhotoBitmaps
import com.zamcan.faceit.data.PhotoLibrary
import com.zamcan.faceit.themes.FaceThemes
import java.io.File

/** One rendering path for the workspace, editor, optional launcher and applied wallpaper. */
object WallpaperComposition {
    const val SCREEN_WIDTH = 1080

    fun compose(context: Context, environment: PersonalEnvironment, width: Int, height: Int): Bitmap? {
        val repository = ArtworkRepository(context)
        return compose(
            environment, width, height,
            loadGround = { id, target -> loadGround(context, id, target) },
            importedBackground = repository.fileFor(environment.selectedArtworkFile.orEmpty()),
            importedObject = repository.fileFor(environment.selectedObjectFile.orEmpty())
        )
    }

    /** The loaded ground belongs to this invocation and is always released. */
    fun compose(
        environment: PersonalEnvironment,
        width: Int,
        height: Int,
        loadGround: (id: String, targetWidthPx: Int) -> Bitmap?,
        importedBackground: File? = null,
        importedObject: File? = null
    ): Bitmap? {
        require(width > 0 && height > 0)
        val selection = CompositionBackgroundPolicy.resolve(environment, FaceThemes.find(environment.themeId).wallpaperId)
        val ground = (selection as? CompositionBackground.Bundled)?.let { loadGround(it.id, width) }
        try {
            return WallpaperRenderer.render(
                width, height, environment,
                artworkFile = if (selection is CompositionBackground.Imported) importedBackground else null,
                objectArtworkFile = importedObject,
                curatedArtwork = ground
            )
        } finally {
            ground?.takeUnless { it.isRecycled }?.recycle()
        }
    }

    fun groundIdFor(environment: PersonalEnvironment): String? =
        (CompositionBackgroundPolicy.resolve(environment, FaceThemes.find(environment.themeId).wallpaperId)
            as? CompositionBackground.Bundled)?.id

    fun loadGround(context: Context, id: String, targetWidthPx: Int): Bitmap? =
        PhotoLibrary.find(context, id)?.let { PhotoBitmaps(context).fullSize(it, targetWidthPx) }

    fun composeWallpaper(context: Context, environment: PersonalEnvironment,
        width: Int = SCREEN_WIDTH, height: Int = 2400): Bitmap? =
        compose(context, environment, width, height)
}

