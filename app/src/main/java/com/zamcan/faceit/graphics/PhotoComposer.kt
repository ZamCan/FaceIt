package com.zamcan.faceit.graphics

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader

/**
 * Turns a photograph into a phone-shaped wallpaper.
 *
 * A landscape photograph cropped to a 9:20 screen keeps only about a third of its
 * width — at the resolution this pack ships, that is roughly 576 usable pixels
 * stretched across 1080, which is visibly soft. Cropping harder to fill the frame
 * would solve the sharpness and ruin the picture.
 *
 * So the photograph is fitted by width at full sharpness and centred, and the
 * space above and below is filled with a blurred, darkened enlargement of the
 * same photograph. The result is the treatment a printed photograph gets on a
 * gallery wall: the image is never stretched, never cut to a slit, and the frame
 * around it comes from the image itself rather than from a flat colour.
 *
 * This is deliberately not a "fit" mode that leaves black bars. A wallpaper with
 * black bars looks like a bug; a wallpaper with a blurred extension of itself
 * looks composed.
 */
object PhotoComposer {

    /** Downscale factor for the blurred backdrop. Deliberately crude: the blur comes from the resample. */
    private const val BACKDROP_WIDTH = 24

    fun compose(width: Int, height: Int, photo: Bitmap): Bitmap {
        require(width > 0 && height > 0)
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        drawBackdrop(canvas, width, height, photo)
        drawPhotograph(canvas, width, height, photo)

        // A gentle vertical scrim keeps overlay text legible without flattening
        // the picture the way a heavy tint would.
        canvas.drawRect(
            0f, 0f, width.toFloat(), height.toFloat(),
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    intArrayOf(0x33000000, 0x00000000, 0x44000000),
                    null, Shader.TileMode.CLAMP
                )
            }
        )
        return output
    }

    /**
     * Blurred, darkened 9:16 enlargement filling the frame.
     *
     * Scaling a 24 px wide copy up to 1080 with filtering is a cheap gaussian-ish
     * blur, and it costs nothing on the main thread at this size.
     */
    private fun drawBackdrop(canvas: Canvas, width: Int, height: Int, photo: Bitmap) {
        val tinyWidth = BACKDROP_WIDTH
        val tinyHeight = (tinyWidth * photo.height / photo.width.toFloat())
            .toInt().coerceAtLeast(1)
        val tiny = Bitmap.createScaledBitmap(photo, tinyWidth, tinyHeight, true)
        val backdrop = Bitmap.createScaledBitmap(tiny, width, height, true)
        if (tiny !== photo) tiny.recycle()

        canvas.drawBitmap(backdrop, null, RectF(0f, 0f, width.toFloat(), height.toFloat()), null)
        backdrop.recycle()

        // Darken and desaturate-by-weight so the sharp photograph reads as the
        // subject and the backdrop as the room it hangs in.
        canvas.drawColor(0x73000000)
    }

    /** The photograph itself: fitted by width, centred, sharp, never stretched. */
    private fun drawPhotograph(canvas: Canvas, width: Int, height: Int, photo: Bitmap) {
        val scaledHeight = width * photo.height / photo.width.toFloat()
        val top = (height - scaledHeight) / 2f
        val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
        canvas.drawBitmap(
            photo,
            null,
            RectF(0f, top, width.toFloat(), top + scaledHeight),
            paint
        )
    }

    /**
     * Centre-crop variant, used where a fill-style wallpaper is wanted rather
     * than a framed one — the gallery preview, for instance, where a blurred
     * frame would read as a misplaced photo.
     */
    fun centreCrop(source: Bitmap, width: Int, height: Int): Bitmap {
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val scale = maxOf(width / source.width.toFloat(), height / source.height.toFloat())
        val scaledWidth = (source.width * scale).toInt()
        val scaledHeight = (source.height * scale).toInt()
        val left = (width - scaledWidth) / 2
        val top = (height - scaledHeight) / 2
        canvas.drawBitmap(source, Rect(0, 0, source.width, source.height), Rect(left, top, left + scaledWidth, top + scaledHeight), Paint(Paint.FILTER_BITMAP_FLAG))
        return output
    }
}
