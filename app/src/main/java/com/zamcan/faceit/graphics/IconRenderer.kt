package com.zamcan.faceit.graphics

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface

/**
 * Draws launcher-style icon glyphs on-device.
 *
 * Icons are drawn rather than shipped as PNGs: a twelve-theme catalogue with a
 * dozen icons each would add megabytes of bitmaps for shapes that are pure
 * geometry, and a drawn glyph is crisp at every density for free. Nothing here
 * reproduces a third-party app icon, logo or trade dress — each glyph is a
 * generic form (phone, message, camera, music) drawn from primitives.
 *
 * Every glyph is expressed in a normalised 0..1 box, so the same code renders an
 * adaptive-icon-sized tile or a 24 dp list row.
 */

/** A generic app glyph. Deliberately not a copy of any real app's icon. */
enum class IconGlyph { PHONE, MESSAGE, CAMERA, MUSIC, GALLERY, CLOCK, CALENDAR, MAIL, BROWSER, WEATHER, FILES, SETTINGS }

/** How the tile behind the glyph is finished. */
enum class IconTile { GLASS_ROUNDED, CERAMIC_SQUIRCLE, MATTE_SQUARE, PEBBLE_CIRCLE, FROSTED_ROUNDED, STONE_ROUNDED }

/**
 * Draws one icon into [canvas] filling [bounds].
 *
 * Callers pass resolved colours; this file owns no palette so it can be reused by
 * the theme catalogue, the widget and any future launcher preview.
 */
object IconRenderer {

    fun draw(
        canvas: Canvas,
        bounds: RectF,
        glyph: IconGlyph,
        tile: IconTile,
        tileColor: Int,
        glyphColor: Int,
        accentColor: Int,
        strokeScale: Float = 1f
    ) {
        val size = minOf(bounds.width(), bounds.height())
        val left = bounds.left + (bounds.width() - size) / 2f
        val top = bounds.top + (bounds.height() - size) / 2f

        drawTile(canvas, RectF(left, top, left + size, top + size), tile, tileColor)

        val inset = size * 0.26f
        val glyphBox = RectF(left + inset, top + inset, left + size - inset, top + size - inset)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = glyphColor
            style = Paint.Style.STROKE
            strokeWidth = size * 0.062f * strokeScale
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = glyphColor
            style = Paint.Style.FILL
        }
        val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.FILL
        }

        when (glyph) {
            IconGlyph.PHONE -> drawPhone(canvas, glyphBox, paint, fill)
            IconGlyph.MESSAGE -> drawMessage(canvas, glyphBox, paint, fill, accent)
            IconGlyph.CAMERA -> drawCamera(canvas, glyphBox, paint, fill, accent)
            IconGlyph.MUSIC -> drawMusic(canvas, glyphBox, paint, fill, accent)
            IconGlyph.GALLERY -> drawGallery(canvas, glyphBox, paint, fill, accent)
            IconGlyph.CLOCK -> drawClock(canvas, glyphBox, paint, fill)
            IconGlyph.CALENDAR -> drawCalendar(canvas, glyphBox, paint, fill, accent)
            IconGlyph.MAIL -> drawMail(canvas, glyphBox, paint, fill)
            IconGlyph.BROWSER -> drawBrowser(canvas, glyphBox, paint, fill, accent)
            IconGlyph.WEATHER -> drawWeather(canvas, glyphBox, paint, fill, accent)
            IconGlyph.FILES -> drawFiles(canvas, glyphBox, paint, fill)
            IconGlyph.SETTINGS -> drawSettings(canvas, glyphBox, paint, fill, accent)
        }
    }

    private fun drawTile(canvas: Canvas, box: RectF, tile: IconTile, colour: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = colour }
        when (tile) {
            IconTile.PEBBLE_CIRCLE -> canvas.drawOval(box, paint)
            IconTile.MATTE_SQUARE -> canvas.drawRect(box, paint)
            IconTile.CERAMIC_SQUIRCLE -> canvas.drawRoundRect(box, box.width() * 0.30f, box.height() * 0.30f, paint)
            IconTile.GLASS_ROUNDED -> canvas.drawRoundRect(box, box.width() * 0.26f, box.height() * 0.26f, paint)
            IconTile.FROSTED_ROUNDED -> canvas.drawRoundRect(box, box.width() * 0.34f, box.height() * 0.34f, paint)
            IconTile.STONE_ROUNDED -> canvas.drawRoundRect(box, box.width() * 0.22f, box.height() * 0.22f, paint)
        }
    }

    // ------------------------------------------------------------------ glyphs

    private fun drawPhone(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint) {
        val w = box.width()
        val handset = Path().apply {
            moveTo(box.left + w * 0.12f, box.top + w * 0.10f)
            lineTo(box.left + w * 0.38f, box.top + w * 0.26f)
            lineTo(box.left + w * 0.26f, box.top + w * 0.52f)
            lineTo(box.left + w * 0.52f, box.top + w * 0.78f)
            lineTo(box.left + w * 0.78f, box.top + w * 0.66f)
            lineTo(box.left + w * 0.92f, box.top + w * 0.92f)
            lineTo(box.left + w * 0.62f, box.top + w * 0.98f)
            lineTo(box.left + w * 0.04f, box.top + w * 0.40f)
            close()
        }
        canvas.drawPath(handset, fill)
    }

    private fun drawMessage(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint, accent: Paint) {
        val w = box.width()
        val bubble = RectF(box.left, box.top + w * 0.06f, box.right, box.top + w * 0.70f)
        canvas.drawRoundRect(bubble, w * 0.18f, w * 0.18f, stroke)
        val tail = Path().apply {
            moveTo(box.left + w * 0.26f, bubble.bottom - w * 0.02f)
            lineTo(box.left + w * 0.20f, box.bottom)
            lineTo(box.left + w * 0.46f, bubble.bottom - w * 0.02f)
            close()
        }
        canvas.drawPath(tail, stroke)
        val dot = w * 0.055f
        for (i in 0..2) {
            canvas.drawCircle(box.left + w * (0.30f + i * 0.20f), bubble.centerY(), dot, accent)
        }
    }

    private fun drawCamera(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint, accent: Paint) {
        val w = box.width()
        val body = RectF(box.left, box.top + w * 0.22f, box.right, box.bottom - w * 0.04f)
        canvas.drawRoundRect(body, w * 0.16f, w * 0.16f, stroke)
        val hump = RectF(box.left + w * 0.28f, box.top + w * 0.08f, box.left + w * 0.68f, box.top + w * 0.26f)
        canvas.drawRoundRect(hump, w * 0.06f, w * 0.06f, stroke)
        canvas.drawCircle(body.centerX(), body.centerY() + w * 0.03f, w * 0.19f, stroke)
        canvas.drawCircle(body.centerX(), body.centerY() + w * 0.03f, w * 0.07f, accent)
    }

    private fun drawMusic(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint, accent: Paint) {
        val w = box.width()
        val stem = Path().apply {
            moveTo(box.left + w * 0.66f, box.top + w * 0.04f)
            lineTo(box.left + w * 0.66f, box.top + w * 0.68f)
        }
        canvas.drawPath(stem, stroke)
        canvas.drawLine(
            box.left + w * 0.66f, box.top + w * 0.04f,
            box.left + w * 0.94f, box.top + w * 0.14f, stroke
        )
        canvas.drawLine(
            box.left + w * 0.94f, box.top + w * 0.14f,
            box.left + w * 0.94f, box.top + w * 0.58f, stroke
        )
        canvas.drawCircle(box.left + w * 0.52f, box.top + w * 0.72f, w * 0.14f, fill)
        canvas.drawCircle(box.left + w * 0.80f, box.top + w * 0.62f, w * 0.14f, fill)
        canvas.drawCircle(box.left + w * 0.52f, box.top + w * 0.72f, w * 0.14f, accent)
    }

    private fun drawGallery(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint, accent: Paint) {
        val w = box.width()
        canvas.drawRoundRect(RectF(box.left, box.top + w * 0.08f, box.right, box.bottom), w * 0.12f, w * 0.12f, stroke)
        canvas.drawCircle(box.left + w * 0.72f, box.top + w * 0.30f, w * 0.075f, accent)
        val hills = Path().apply {
            moveTo(box.left + w * 0.10f, box.bottom - w * 0.08f)
            lineTo(box.left + w * 0.36f, box.top + w * 0.52f)
            lineTo(box.left + w * 0.58f, box.bottom - w * 0.08f)
            close()
        }
        canvas.drawPath(hills, fill)
        val far = Path().apply {
            moveTo(box.left + w * 0.50f, box.bottom - w * 0.08f)
            lineTo(box.left + w * 0.74f, box.top + w * 0.60f)
            lineTo(box.right - w * 0.10f, box.bottom - w * 0.08f)
            close()
        }
        canvas.drawPath(far, stroke)
    }

    private fun drawClock(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint) {
        val w = box.width()
        val centre = box.centerX()
        val mid = box.centerY()
        canvas.drawCircle(centre, mid, w * 0.44f, stroke)
        canvas.drawLine(centre, mid, centre, mid - w * 0.26f, stroke)
        canvas.drawLine(centre, mid, centre + w * 0.20f, mid + w * 0.10f, stroke)
        canvas.drawCircle(centre, mid, w * 0.05f, fill)
    }

    private fun drawCalendar(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint, accent: Paint) {
        val w = box.width()
        val body = RectF(box.left, box.top + w * 0.16f, box.right, box.bottom - w * 0.04f)
        canvas.drawRoundRect(body, w * 0.12f, w * 0.12f, stroke)
        canvas.drawLine(body.left, body.top + w * 0.20f, body.right, body.top + w * 0.20f, stroke)
        canvas.drawLine(
            box.left + w * 0.30f, box.top + w * 0.04f,
            box.left + w * 0.30f, box.top + w * 0.22f, stroke
        )
        canvas.drawLine(
            box.left + w * 0.70f, box.top + w * 0.04f,
            box.left + w * 0.70f, box.top + w * 0.22f, stroke
        )
        canvas.drawCircle(body.centerX(), body.centerY() + w * 0.10f, w * 0.09f, accent)
    }

    private fun drawMail(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint) {
        val w = box.width()
        val body = RectF(box.left, box.top + w * 0.20f, box.right, box.bottom - w * 0.10f)
        canvas.drawRoundRect(body, w * 0.10f, w * 0.10f, stroke)
        val flap = Path().apply {
            moveTo(body.left + w * 0.04f, body.top + w * 0.06f)
            lineTo(body.centerX(), body.centerY() + w * 0.06f)
            lineTo(body.right - w * 0.04f, body.top + w * 0.06f)
        }
        canvas.drawPath(flap, stroke)
    }

    private fun drawBrowser(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint, accent: Paint) {
        val w = box.width()
        val disc = RectF(box.left, box.top + w * 0.02f, box.right, box.bottom)
        canvas.drawOval(disc, stroke)
        canvas.drawLine(disc.left + w * 0.06f, disc.centerY(), disc.right - w * 0.06f, disc.centerY(), stroke)
        val meridian = RectF(disc.centerX() - w * 0.18f, disc.top, disc.centerX() + w * 0.18f, disc.bottom)
        canvas.drawOval(meridian, stroke)
        canvas.drawCircle(disc.centerX() - w * 0.16f, disc.centerY() - w * 0.16f, w * 0.07f, accent)
    }

    private fun drawWeather(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint, accent: Paint) {
        val w = box.width()
        canvas.drawCircle(box.left + w * 0.36f, box.top + w * 0.34f, w * 0.18f, accent)
        val cloud = Path().apply {
            moveTo(box.left + w * 0.16f, box.top + w * 0.70f)
            cubicTo(
                box.left + w * 0.10f, box.top + w * 0.56f,
                box.left + w * 0.26f, box.top + w * 0.48f,
                box.left + w * 0.36f, box.top + w * 0.54f
            )
            cubicTo(
                box.left + w * 0.46f, box.top + w * 0.42f,
                box.left + w * 0.72f, box.top + w * 0.46f,
                box.left + w * 0.72f, box.top + w * 0.58f
            )
            cubicTo(
                box.left + w * 0.90f, box.top + w * 0.58f,
                box.left + w * 0.92f, box.top + w * 0.78f,
                box.left + w * 0.74f, box.top + w * 0.78f
            )
            lineTo(box.left + w * 0.24f, box.top + w * 0.78f)
            cubicTo(
                box.left + w * 0.14f, box.top + w * 0.78f,
                box.left + w * 0.12f, box.top + w * 0.74f,
                box.left + w * 0.16f, box.top + w * 0.70f
            )
            close()
        }
        canvas.drawPath(cloud, stroke)
    }

    private fun drawFiles(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint) {
        val w = box.width()
        val folder = Path().apply {
            moveTo(box.left, box.top + w * 0.30f)
            lineTo(box.left + w * 0.38f, box.top + w * 0.30f)
            lineTo(box.left + w * 0.48f, box.top + w * 0.42f)
            lineTo(box.right, box.top + w * 0.42f)
            lineTo(box.right, box.bottom - w * 0.06f)
            lineTo(box.left, box.bottom - w * 0.06f)
            close()
        }
        canvas.drawPath(folder, stroke)
    }

    private fun drawSettings(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint, accent: Paint) {
        val w = box.width()
        val centre = box.centerX()
        val mid = box.centerY()
        val outer = w * 0.46f
        val tooth = Path()
        for (i in 0 until 8) {
            val angle = Math.toRadians(i * 45.0)
            val inner = outer * 0.74f
            val half = Math.toRadians(12.0)
            tooth.moveTo(
                (centre + inner * Math.cos(angle - half)).toFloat(),
                (mid + inner * Math.sin(angle - half)).toFloat()
            )
            tooth.lineTo(
                (centre + outer * Math.cos(angle)).toFloat(),
                (mid + outer * Math.sin(angle)).toFloat()
            )
            tooth.lineTo(
                (centre + inner * Math.cos(angle + half)).toFloat(),
                (mid + inner * Math.sin(angle + half)).toFloat()
            )
        }
        canvas.drawPath(tooth, stroke)
        canvas.drawCircle(centre, mid, outer * 0.62f, stroke)
        canvas.drawCircle(centre, mid, outer * 0.22f, accent)
    }

    /** Draws a short wordmark-free legend, used only for tile previews. */
    fun drawTileOnly(canvas: Canvas, box: RectF, tile: IconTile, colour: Int) =
        drawTile(canvas, box, tile, colour)

    /** A typeface kept for future label support; no text is drawn on icons. */
    val labelTypeface: Typeface get() = Typeface.SANS_SERIF
}
