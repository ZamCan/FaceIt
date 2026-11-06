package com.zamcan.faceit.graphics

import android.content.Context
import android.graphics.Bitmap
import com.zamcan.faceit.data.PersonalEnvironment
import com.zamcan.faceit.data.PhotoBitmaps
import com.zamcan.faceit.data.PhotoLibrary

/**
 * The one place a screen background is composed.
 *
 * Three surfaces need to agree: the workspace, the launcher and the wallpaper the
 * user pins. They used to each do their own lookup, and the launcher could end up
 * showing a different background from the workspace. Everything now goes through
 * here, so "what the user chose" has exactly one meaning.
 *
 * Composition order: the ground (the photograph the name sits on, or the theme's
 * photograph), then the name engine on top. The name never changes the ground's
 * identity, so switching finish cannot lose the picture the user picked.
 */
object WallpaperComposition {

    /** Longest edge used when composing on screen. Wallpapers ask for more. */
    const val SCREEN_WIDTH = 1080

    /**
     * Compose what should be on screen right now.
     *
     * Returns null only when there is nothing at all to draw, which the caller
     * should treat as "leave the surface's own colour".
     */
    fun compose(context: Context, environment: PersonalEnvironment, width: Int, height: Int): Bitmap? =
        compose(
            environment = environment,
            width = width,
            height = height,
            // Decode the ground at composition size rather than screen size: the
            // background is then screen-independent, and a preview at 540 px costs
            // a quarter of what a full-size screen does.
            loadGround = { id, target -> loadGround(context, id, target) }
        )

    /** Compose without an Android context, given a way to fetch the ground bitmap. */
    fun compose(
        environment: PersonalEnvironment,
        width: Int,
        height: Int,
        loadGround: (id: String, targetWidthPx: Int) -> Bitmap?
    ): Bitmap? {
        val design = environment.nameDesignOrNull()
        val groundId = groundIdFor(environment)

        val ground = groundId?.let { id -> loadGround(id, width) }

        // No photograph and no name: nothing to compose, the caller keeps its own
        // surface. A name alone is still a composition, on the finish's backdrop.
        if (ground == null && design == null) return null

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)
        if (ground != null) {
            // The photograph is composed to the screen shape so it is never
            // stretched into a slit; the same treatment the pinned wallpaper gets.
            val shaped = PhotoComposer.compose(width, height, ground)
            canvas.drawBitmap(shaped, 0f, 0f, null)
            if (shaped !== ground) shaped.recycle()
        } else {
            canvas.drawColor(NameMonogram.backdrop(NameFinish.find(design?.finishId)))
            design?.let { NameMonogram.drawCircuitGround(canvas, width, height, it.displayText(), it.finish) }
        }
        design?.let { NameMonogram.draw(canvas, width, height, it) }
        return output
    }

    /**
     * Which bundled photograph sits behind the composition.
     *
     * When the name engine has a ground, that wins: the user chose it explicitly
     * for the name. Otherwise the usual rule applies — the photograph they picked,
     * then the theme's own.
     */
    fun groundIdFor(environment: PersonalEnvironment): String? =
        environment.nameDesignOrNull()?.groundId
            ?: com.zamcan.faceit.data.WallpaperPolicy.resolveId(
                environment,
                com.zamcan.faceit.themes.FaceThemes.find(environment.themeId)
            )

    /** Decode a bundled photograph at a bounded width. The only Android-specific part. */
    fun loadGround(context: Context, id: String, targetWidthPx: Int): Bitmap? =
        PhotoLibrary.find(context, id)?.let { photo ->
            PhotoBitmaps(context).fullSize(photo, targetWidthPx)
        }

    /**
     * Compose a full-resolution wallpaper for pinning with the system, or for the
     * "save a copy" path. Same rules as [compose]; the size is larger because it is
     * being written to storage rather than drawn to a surface.
     */
    fun composeWallpaper(
        context: Context,
        environment: PersonalEnvironment,
        width: Int = SCREEN_WIDTH,
        height: Int = 2400
    ): Bitmap? = compose(context, environment, width, height)
}
