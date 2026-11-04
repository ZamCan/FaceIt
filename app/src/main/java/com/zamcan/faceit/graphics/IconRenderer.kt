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
enum class IconGlyph {
    // everyday essentials
    PHONE, MESSAGE, CAMERA, MUSIC, GALLERY, CLOCK, CALENDAR, MAIL, BROWSER, WEATHER, FILES, SETTINGS,
    // cute companions and nature
    CAT, PAW, HEART, FLOWER, LEAF, TREE, BUTTERFLY, STAR,
    // objects and pastimes
    BOOK, GIFT, RING, COFFEE, BAG, SUN, MOON, MAP,
    // kit and controls
    CHARGE, GAME, CONTACT, WALLET, SEARCH, HOME,
    // life and people
    PERSON, PEOPLE, CHAT, VIDEO, VOICE, SOS,
    // food, drink and health
    FOOD, DRINK, PILL, RUN, CYCLE, WEIGHT,
    // travel and money
    FLIGHT, CAR, SHOP, BANK, TICKET, PIN
}

/** How the tile behind the glyph is finished. */
enum class IconTile {
    GLASS_ROUNDED, CERAMIC_SQUIRCLE, MATTE_SQUARE, PEBBLE_CIRCLE,
    FROSTED_ROUNDED, STONE_ROUNDED, JEWEL_OCTAGON, SATIN_ROUNDED
}

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
            IconGlyph.CAT -> drawCat(canvas, glyphBox, paint, fill, accent)
            IconGlyph.PAW -> drawPaw(canvas, glyphBox, fill, accent)
            IconGlyph.HEART -> drawHeart(canvas, glyphBox, fill, accent)
            IconGlyph.FLOWER -> drawFlower(canvas, glyphBox, fill, accent)
            IconGlyph.LEAF -> drawLeaf(canvas, glyphBox, paint, fill)
            IconGlyph.TREE -> drawTree(canvas, glyphBox, paint, fill, accent)
            IconGlyph.BUTTERFLY -> drawButterfly(canvas, glyphBox, fill, accent)
            IconGlyph.STAR -> drawStar(canvas, glyphBox, fill, accent)
            IconGlyph.BOOK -> drawBook(canvas, glyphBox, paint, accent)
            IconGlyph.GIFT -> drawGift(canvas, glyphBox, paint, fill, accent)
            IconGlyph.RING -> drawRing(canvas, glyphBox, paint, accent)
            IconGlyph.COFFEE -> drawCoffee(canvas, glyphBox, paint, accent)
            IconGlyph.BAG -> drawBag(canvas, glyphBox, paint, accent)
            IconGlyph.SUN -> drawSun(canvas, glyphBox, paint, fill)
            IconGlyph.MOON -> drawMoon(canvas, glyphBox, fill, accent)
            IconGlyph.MAP -> drawMap(canvas, glyphBox, paint, accent)
            IconGlyph.CHARGE -> drawCharge(canvas, glyphBox, fill, accent)
            IconGlyph.GAME -> drawGame(canvas, glyphBox, paint, fill, accent)
            IconGlyph.CONTACT -> drawContact(canvas, glyphBox, paint, fill, accent)
            IconGlyph.WALLET -> drawWallet(canvas, glyphBox, paint, accent)
            IconGlyph.SEARCH -> drawSearch(canvas, glyphBox, paint, accent)
            IconGlyph.HOME -> drawHome(canvas, glyphBox, paint, accent)
            IconGlyph.PERSON -> drawPerson(canvas, glyphBox, paint, fill, accent)
            IconGlyph.PEOPLE -> drawPeople(canvas, glyphBox, paint, fill, accent)
            IconGlyph.CHAT -> drawChat(canvas, glyphBox, paint, fill, accent)
            IconGlyph.VIDEO -> drawVideo(canvas, glyphBox, paint, fill, accent)
            IconGlyph.VOICE -> drawVoice(canvas, glyphBox, paint, fill, accent)
            IconGlyph.SOS -> drawSos(canvas, glyphBox, paint, fill, accent)
            IconGlyph.FOOD -> drawFood(canvas, glyphBox, paint, accent)
            IconGlyph.DRINK -> drawDrink(canvas, glyphBox, paint, accent)
            IconGlyph.PILL -> drawPill(canvas, glyphBox, fill, accent)
            IconGlyph.RUN -> drawRun(canvas, glyphBox, paint, fill, accent)
            IconGlyph.CYCLE -> drawCycle(canvas, glyphBox, paint, accent)
            IconGlyph.WEIGHT -> drawWeight(canvas, glyphBox, paint, fill, accent)
            IconGlyph.FLIGHT -> drawFlight(canvas, glyphBox, fill, accent)
            IconGlyph.CAR -> drawCar(canvas, glyphBox, paint, fill, accent)
            IconGlyph.SHOP -> drawShop(canvas, glyphBox, paint, accent)
            IconGlyph.BANK -> drawBank(canvas, glyphBox, paint, accent)
            IconGlyph.TICKET -> drawTicket(canvas, glyphBox, paint, accent, fill)
            IconGlyph.PIN -> drawPin(canvas, glyphBox, paint, fill, accent)
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
            IconTile.SATIN_ROUNDED -> canvas.drawRoundRect(box, box.width() * 0.42f, box.height() * 0.42f, paint)
            IconTile.JEWEL_OCTAGON -> {
                // A cut-corner tile: the shape reads as a set stone rather than a rounded square.
                val c = box.width() * 0.24f
                val path = Path().apply {
                    moveTo(box.left + c, box.top)
                    lineTo(box.right - c, box.top)
                    lineTo(box.right, box.top + c)
                    lineTo(box.right, box.bottom - c)
                    lineTo(box.right - c, box.bottom)
                    lineTo(box.left + c, box.bottom)
                    lineTo(box.left, box.bottom - c)
                    lineTo(box.left, box.top + c)
                    close()
                }
                canvas.drawPath(path, paint)
            }
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

    // ------------------------------------------------------------ companions
    // These are the shapes the "ladies turn" and the cute family asked for: cats,
    // paws, petals, jewels. They are drawn from primitives — a generic cat head is
    // not any real app's mascot.

    private fun drawCat(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint, accent: Paint) {
        val w = box.width()
        val head = Path().apply {
            moveTo(box.left + w * 0.50f, box.bottom - w * 0.04f)
            cubicTo(
                box.left + w * 0.12f, box.bottom - w * 0.04f,
                box.left + w * 0.10f, box.top + w * 0.44f,
                box.left + w * 0.28f, box.top + w * 0.34f
            )
            lineTo(box.left + w * 0.22f, box.top + w * 0.02f)
            lineTo(box.left + w * 0.48f, box.top + w * 0.20f)
            lineTo(box.left + w * 0.72f, box.top + w * 0.02f)
            lineTo(box.left + w * 0.78f, box.top + w * 0.34f)
            cubicTo(
                box.left + w * 0.94f, box.top + w * 0.44f,
                box.left + w * 0.90f, box.bottom - w * 0.04f,
                box.left + w * 0.50f, box.bottom - w * 0.04f
            )
            close()
        }
        canvas.drawPath(head, fill)
        canvas.drawPath(head, stroke)
        val eye = w * 0.075f
        canvas.drawCircle(box.left + w * 0.36f, box.top + w * 0.56f, eye, accent)
        canvas.drawCircle(box.left + w * 0.64f, box.top + w * 0.56f, eye, accent)
        // whiskers and nose: three strokes each side, enough to read as a cat at 24 dp
        canvas.drawLine(box.left + w * 0.50f, box.top + w * 0.68f, box.left + w * 0.50f, box.top + w * 0.74f, stroke)
        canvas.drawLine(box.left + w * 0.06f, box.top + w * 0.62f, box.left + w * 0.28f, box.top + w * 0.66f, stroke)
        canvas.drawLine(box.left + w * 0.72f, box.top + w * 0.66f, box.left + w * 0.94f, box.top + w * 0.62f, stroke)
    }

    private fun drawPaw(canvas: Canvas, box: RectF, fill: Paint, accent: Paint) {
        val w = box.width()
        val pad = Path().apply {
            addOval(
                RectF(
                    box.left + w * 0.24f, box.top + w * 0.46f,
                    box.left + w * 0.76f, box.top + w * 0.96f
                ),
                Path.Direction.CW
            )
        }
        canvas.drawPath(pad, fill)
        val toe = w * 0.115f
        listOf(0.22f, 0.43f, 0.62f, 0.80f).forEachIndexed { index, x ->
            val y = if (index == 0 || index == 3) 0.34f else 0.22f
            canvas.drawCircle(box.left + w * x, box.top + w * y, toe, fill)
        }
        canvas.drawCircle(box.left + w * 0.50f, box.top + w * 0.66f, w * 0.05f, accent)
    }

    private fun drawHeart(canvas: Canvas, box: RectF, fill: Paint, accent: Paint) {
        val w = box.width()
        val heart = Path().apply {
            moveTo(box.left + w * 0.50f, box.bottom - w * 0.06f)
            cubicTo(
                box.left - w * 0.06f, box.top + w * 0.48f,
                box.left + w * 0.16f, box.top + w * 0.02f,
                box.left + w * 0.50f, box.top + w * 0.26f
            )
            cubicTo(
                box.left + w * 0.84f, box.top + w * 0.02f,
                box.right + w * 0.06f, box.top + w * 0.48f,
                box.left + w * 0.50f, box.bottom - w * 0.06f
            )
            close()
        }
        canvas.drawPath(heart, fill)
        // a single specular streak: the difference between flat and polished
        val shine = Paint(fill).apply { color = accent.color }
        canvas.drawOval(
            RectF(
                box.left + w * 0.28f, box.top + w * 0.22f,
                box.left + w * 0.46f, box.top + w * 0.40f
            ),
            shine
        )
    }

    private fun drawFlower(canvas: Canvas, box: RectF, fill: Paint, accent: Paint) {
        val w = box.width()
        val cx = box.centerX()
        val cy = box.centerY()
        repeat(6) { index ->
            val angle = Math.toRadians(index * 60.0)
            val px = cx + (w * 0.27f * Math.cos(angle)).toFloat()
            val py = cy + (w * 0.27f * Math.sin(angle)).toFloat()
            canvas.drawCircle(px, py, w * 0.19f, fill)
        }
        canvas.drawCircle(cx, cy, w * 0.14f, accent)
    }

    private fun drawLeaf(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint) {
        val w = box.width()
        val leaf = Path().apply {
            moveTo(box.left + w * 0.50f, box.bottom)
            cubicTo(
                box.left + w * 0.04f, box.top + w * 0.72f,
                box.left + w * 0.22f, box.top + w * 0.02f,
                box.left + w * 0.50f, box.top
            )
            cubicTo(
                box.left + w * 0.78f, box.top + w * 0.02f,
                box.left + w * 0.96f, box.top + w * 0.72f,
                box.left + w * 0.50f, box.bottom
            )
            close()
        }
        canvas.drawPath(leaf, fill)
        canvas.drawPath(leaf, stroke)
        canvas.drawLine(
            box.left + w * 0.50f, box.top + w * 0.14f,
            box.left + w * 0.50f, box.bottom - w * 0.10f, stroke
        )
    }

    private fun drawTree(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint, accent: Paint) {
        val w = box.width()
        canvas.drawRect(
            box.left + w * 0.44f, box.top + w * 0.58f,
            box.left + w * 0.56f, box.bottom, fill
        )
        val crown = Path().apply {
            moveTo(box.left + w * 0.50f, box.top + w * 0.02f)
            lineTo(box.left + w * 0.05f, box.top + w * 0.64f)
            lineTo(box.left + w * 0.95f, box.top + w * 0.64f)
            close()
        }
        canvas.drawPath(crown, fill)
        canvas.drawPath(crown, stroke)
        canvas.drawCircle(box.left + w * 0.36f, box.top + w * 0.40f, w * 0.06f, accent)
        canvas.drawCircle(box.left + w * 0.64f, box.top + w * 0.48f, w * 0.06f, accent)
    }

    private fun drawButterfly(canvas: Canvas, box: RectF, fill: Paint, accent: Paint) {
        val w = box.width()
        val cx = box.centerX()
        val upper = Path().apply {
            moveTo(cx, box.top + w * 0.36f)
            cubicTo(cx - w * 0.52f, box.top - w * 0.02f, cx - w * 0.52f, box.top + w * 0.50f, cx, box.top + w * 0.52f)
            close()
        }
        val lower = Path().apply {
            moveTo(cx, box.top + w * 0.52f)
            cubicTo(cx - w * 0.44f, box.top + w * 0.54f, cx - w * 0.30f, box.bottom, cx, box.top + w * 0.72f)
            close()
        }
        val mirror = Path(upper).apply { transform(android.graphics.Matrix().apply { preScale(-1f, 1f, cx, 0f) }) }
        val mirrorLower = Path(lower).apply { transform(android.graphics.Matrix().apply { preScale(-1f, 1f, cx, 0f) }) }
        canvas.drawPath(upper, fill)
        canvas.drawPath(mirror, fill)
        canvas.drawPath(lower, fill)
        canvas.drawPath(mirrorLower, fill)
        canvas.drawRect(cx - w * 0.03f, box.top + w * 0.30f, cx + w * 0.03f, box.top + w * 0.74f, accent)
    }

    private fun drawStar(canvas: Canvas, box: RectF, fill: Paint, accent: Paint) {
        val w = box.width()
        val cx = box.centerX()
        val cy = box.centerY()
        val points = 5
        val path = Path()
        for (i in 0 until points * 2) {
            val r = if (i % 2 == 0) w * 0.48f else w * 0.21f
            val angle = Math.toRadians(-90.0 + i * (180.0 / points))
            val x = cx + (r * Math.cos(angle)).toFloat()
            val y = cy + (r * Math.sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawCircle(cx, cy, w * 0.07f, accent)
    }

    // ---------------------------------------------------------- objects
    private fun drawBook(canvas: Canvas, box: RectF, stroke: Paint, accent: Paint) {
        val w = box.width()
        val page = Path().apply {
            moveTo(box.left + w * 0.06f, box.top + w * 0.16f)
            cubicTo(box.left + w * 0.28f, box.top + w * 0.04f, box.left + w * 0.44f, box.top + w * 0.10f, box.left + w * 0.50f, box.top + w * 0.22f)
            cubicTo(box.left + w * 0.56f, box.top + w * 0.10f, box.left + w * 0.72f, box.top + w * 0.04f, box.left + w * 0.94f, box.top + w * 0.16f)
            lineTo(box.left + w * 0.94f, box.bottom - w * 0.10f)
            cubicTo(box.left + w * 0.72f, box.bottom - w * 0.22f, box.left + w * 0.56f, box.bottom - w * 0.16f, box.left + w * 0.50f, box.bottom - w * 0.04f)
            cubicTo(box.left + w * 0.44f, box.bottom - w * 0.16f, box.left + w * 0.28f, box.bottom - w * 0.22f, box.left + w * 0.06f, box.bottom - w * 0.10f)
            close()
        }
        canvas.drawPath(page, stroke)
        canvas.drawLine(box.left + w * 0.50f, box.top + w * 0.20f, box.left + w * 0.50f, box.bottom - w * 0.06f, accent)
    }

    private fun drawGift(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint, accent: Paint) {
        val w = box.width()
        canvas.drawRoundRect(
            RectF(box.left + w * 0.10f, box.top + w * 0.38f, box.right - w * 0.10f, box.bottom - w * 0.02f),
            w * 0.06f, w * 0.06f, fill
        )
        canvas.drawRoundRect(
            RectF(box.left + w * 0.04f, box.top + w * 0.30f, box.right - w * 0.04f, box.top + w * 0.44f),
            w * 0.05f, w * 0.05f, stroke
        )
        canvas.drawRect(
            box.left + w * 0.44f, box.top + w * 0.30f,
            box.left + w * 0.56f, box.bottom - w * 0.02f, accent
        )
        // ribbon loops: two arcs meeting at the knot
        canvas.drawArc(
            RectF(box.left + w * 0.18f, box.top + w * 0.02f, box.left + w * 0.50f, box.top + w * 0.32f),
            150f, 240f, false, stroke
        )
        canvas.drawArc(
            RectF(box.left + w * 0.50f, box.top + w * 0.02f, box.left + w * 0.82f, box.top + w * 0.32f),
            -30f, -240f, false, stroke
        )
    }

    private fun drawRing(canvas: Canvas, box: RectF, stroke: Paint, accent: Paint) {
        val w = box.width()
        canvas.drawCircle(
            box.centerX(), box.top + w * 0.60f, w * 0.32f, stroke
        )
        val gem = Path().apply {
            moveTo(box.centerX(), box.top + w * 0.02f)
            lineTo(box.centerX() + w * 0.20f, box.top + w * 0.18f)
            lineTo(box.centerX(), box.top + w * 0.36f)
            lineTo(box.centerX() - w * 0.20f, box.top + w * 0.18f)
            close()
        }
        canvas.drawPath(gem, accent)
    }

    private fun drawCoffee(canvas: Canvas, box: RectF, stroke: Paint, accent: Paint) {
        val w = box.width()
        val cup = Path().apply {
            moveTo(box.left + w * 0.14f, box.top + w * 0.34f)
            lineTo(box.right - w * 0.24f, box.top + w * 0.34f)
            lineTo(box.right - w * 0.32f, box.bottom - w * 0.06f)
            lineTo(box.left + w * 0.22f, box.bottom - w * 0.06f)
            close()
        }
        canvas.drawPath(cup, stroke)
        canvas.drawArc(
            RectF(box.right - w * 0.44f, box.top + w * 0.40f, box.right, box.top + w * 0.76f),
            -80f, 160f, false, stroke
        )
        // steam: two short curves, the detail that makes it read as hot
        canvas.drawArc(
            RectF(box.left + w * 0.30f, box.top + w * 0.02f, box.left + w * 0.48f, box.top + w * 0.30f),
            100f, 160f, false, accent
        )
        canvas.drawArc(
            RectF(box.left + w * 0.52f, box.top + w * 0.02f, box.left + w * 0.70f, box.top + w * 0.30f),
            100f, 160f, false, accent
        )
    }

    private fun drawBag(canvas: Canvas, box: RectF, stroke: Paint, accent: Paint) {
        val w = box.width()
        canvas.drawRoundRect(
            RectF(box.left + w * 0.10f, box.top + w * 0.32f, box.right - w * 0.10f, box.bottom - w * 0.04f),
            w * 0.10f, w * 0.10f, stroke
        )
        canvas.drawArc(
            RectF(box.left + w * 0.30f, box.top + w * 0.04f, box.right - w * 0.30f, box.top + w * 0.56f),
            180f, 180f, false, stroke
        )
        canvas.drawCircle(box.centerX(), box.top + w * 0.50f, w * 0.045f, accent)
    }

    private fun drawSun(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint) {
        val w = box.width()
        val cx = box.centerX()
        val cy = box.centerY()
        canvas.drawCircle(cx, cy, w * 0.22f, fill)
        repeat(8) { index ->
            val angle = Math.toRadians(index * 45.0)
            canvas.drawLine(
                cx + (w * 0.32f * Math.cos(angle)).toFloat(),
                cy + (w * 0.32f * Math.sin(angle)).toFloat(),
                cx + (w * 0.46f * Math.cos(angle)).toFloat(),
                cy + (w * 0.46f * Math.sin(angle)).toFloat(),
                stroke
            )
        }
    }

    private fun drawMoon(canvas: Canvas, box: RectF, fill: Paint, accent: Paint) {
        val w = box.width()
        val crescent = Path().apply {
            addOval(RectF(box.left + w * 0.16f, box.top + w * 0.02f, box.right - w * 0.16f, box.bottom - w * 0.02f), Path.Direction.CW)
        }
        val bite = Path().apply {
            addOval(RectF(box.left + w * 0.36f, box.top - w * 0.08f, box.right + w * 0.14f, box.top + w * 0.80f), Path.Direction.CW)
        }
        canvas.save()
        canvas.clipPath(crescent)
        canvas.drawPath(crescent, fill)
        canvas.drawPath(bite, Paint(fill).apply { color = 0x00000000; xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR) })
        canvas.restore()
        canvas.drawCircle(box.left + w * 0.26f, box.top + w * 0.74f, w * 0.05f, accent)
        canvas.drawCircle(box.left + w * 0.38f, box.top + w * 0.86f, w * 0.035f, accent)
    }

    private fun drawMap(canvas: Canvas, box: RectF, stroke: Paint, accent: Paint) {
        val w = box.width()
        val sheet = Path().apply {
            moveTo(box.left + w * 0.04f, box.top + w * 0.16f)
            lineTo(box.left + w * 0.36f, box.top + w * 0.04f)
            lineTo(box.left + w * 0.64f, box.top + w * 0.16f)
            lineTo(box.left + w * 0.96f, box.top + w * 0.04f)
            lineTo(box.left + w * 0.96f, box.bottom - w * 0.16f)
            lineTo(box.left + w * 0.64f, box.bottom - w * 0.04f)
            lineTo(box.left + w * 0.36f, box.bottom - w * 0.16f)
            lineTo(box.left + w * 0.04f, box.bottom - w * 0.04f)
            close()
        }
        canvas.drawPath(sheet, stroke)
        canvas.drawLine(box.left + w * 0.36f, box.top + w * 0.06f, box.left + w * 0.36f, box.bottom - w * 0.14f, stroke)
        canvas.drawLine(box.left + w * 0.64f, box.top + w * 0.18f, box.left + w * 0.64f, box.bottom - w * 0.06f, stroke)
        canvas.drawCircle(box.left + w * 0.50f, box.top + w * 0.44f, w * 0.09f, accent)
    }

    // ---------------------------------------------------------- kit
    private fun drawCharge(canvas: Canvas, box: RectF, fill: Paint, accent: Paint) {
        val w = box.width()
        canvas.drawRoundRect(
            RectF(box.left + w * 0.26f, box.top + w * 0.04f, box.right - w * 0.26f, box.bottom - w * 0.02f),
            w * 0.10f, w * 0.10f, fill
        )
        val bolt = Path().apply {
            moveTo(box.left + w * 0.56f, box.top + w * 0.16f)
            lineTo(box.left + w * 0.34f, box.top + w * 0.56f)
            lineTo(box.left + w * 0.48f, box.top + w * 0.56f)
            lineTo(box.left + w * 0.42f, box.top + w * 0.88f)
            lineTo(box.left + w * 0.68f, box.top + w * 0.46f)
            lineTo(box.left + w * 0.52f, box.top + w * 0.46f)
            close()
        }
        canvas.drawPath(bolt, accent)
    }

    private fun drawGame(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint, accent: Paint) {
        val w = box.width()
        val body = RectF(box.left + w * 0.02f, box.top + w * 0.28f, box.right - w * 0.02f, box.bottom - w * 0.18f)
        canvas.drawRoundRect(body, w * 0.30f, w * 0.30f, fill)
        canvas.drawRoundRect(body, w * 0.30f, w * 0.30f, stroke)
        canvas.drawCircle(box.left + w * 0.32f, body.centerY(), w * 0.09f, accent)
        canvas.drawCircle(box.right - w * 0.28f, body.centerY() - w * 0.10f, w * 0.055f, accent)
        canvas.drawCircle(box.right - w * 0.28f, body.centerY() + w * 0.10f, w * 0.055f, accent)
    }

    private fun drawContact(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint, accent: Paint) {
        val w = box.width()
        canvas.drawCircle(box.centerX(), box.top + w * 0.28f, w * 0.20f, fill)
        canvas.drawArc(
            RectF(box.left + w * 0.10f, box.top + w * 0.48f, box.right - w * 0.10f, box.bottom + w * 0.30f),
            180f, 180f, true, fill
        )
        canvas.drawArc(
            RectF(box.left + w * 0.10f, box.top + w * 0.48f, box.right - w * 0.10f, box.bottom + w * 0.30f),
            180f, 180f, true, stroke
        )
        canvas.drawCircle(box.centerX(), box.top + w * 0.28f, w * 0.20f, stroke)
        canvas.drawCircle(box.centerX(), box.top + w * 0.28f, w * 0.07f, accent)
    }

    private fun drawWallet(canvas: Canvas, box: RectF, stroke: Paint, accent: Paint) {
        val w = box.width()
        canvas.drawRoundRect(
            RectF(box.left + w * 0.04f, box.top + w * 0.24f, box.right - w * 0.04f, box.bottom - w * 0.10f),
            w * 0.14f, w * 0.14f, stroke
        )
        canvas.drawLine(
            box.left + w * 0.04f, box.top + w * 0.46f,
            box.right - w * 0.04f, box.top + w * 0.46f, stroke
        )
        canvas.drawCircle(box.right - w * 0.26f, box.top + w * 0.62f, w * 0.07f, accent)
    }

    private fun drawSearch(canvas: Canvas, box: RectF, stroke: Paint, accent: Paint) {
        val w = box.width()
        canvas.drawCircle(box.left + w * 0.44f, box.top + w * 0.42f, w * 0.30f, stroke)
        canvas.drawLine(
            box.left + w * 0.66f, box.top + w * 0.64f,
            box.left + w * 0.94f, box.top + w * 0.94f, stroke
        )
        canvas.drawCircle(box.left + w * 0.44f, box.top + w * 0.42f, w * 0.10f, accent)
    }

    private fun drawHome(canvas: Canvas, box: RectF, stroke: Paint, accent: Paint) {
        val w = box.width()
        val roof = Path().apply {
            moveTo(box.left + w * 0.06f, box.top + w * 0.48f)
            lineTo(box.centerX(), box.top + w * 0.06f)
            lineTo(box.right - w * 0.06f, box.top + w * 0.48f)
        }
        canvas.drawPath(roof, stroke)
        canvas.drawRoundRect(
            RectF(box.left + w * 0.18f, box.top + w * 0.44f, box.right - w * 0.18f, box.bottom - w * 0.04f),
            w * 0.06f, w * 0.06f, stroke
        )
        canvas.drawRoundRect(
            RectF(box.centerX() - w * 0.10f, box.top + w * 0.64f, box.centerX() + w * 0.10f, box.bottom - w * 0.04f),
            w * 0.04f, w * 0.04f, accent
        )
    }


    // ------------------------------------------------------------ life

    private fun drawPerson(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint, accent: Paint) {
        val w = box.width()
        canvas.drawCircle(box.centerX(), box.top + w * 0.26f, w * 0.19f, fill)
        canvas.drawCircle(box.centerX(), box.top + w * 0.26f, w * 0.19f, stroke)
        canvas.drawArc(
            RectF(box.left + w * 0.12f, box.top + w * 0.46f, box.right - w * 0.12f, box.bottom + w * 0.34f),
            180f, 180f, true, fill
        )
        canvas.drawArc(
            RectF(box.left + w * 0.12f, box.top + w * 0.46f, box.right - w * 0.12f, box.bottom + w * 0.34f),
            180f, 180f, true, stroke
        )
        canvas.drawCircle(box.centerX(), box.top + w * 0.26f, w * 0.06f, accent)
    }

    private fun drawPeople(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint, accent: Paint) {
        val w = box.width()
        // a pair, offset: one behind and quieter, one in front and lit
        canvas.drawCircle(box.left + w * 0.30f, box.top + w * 0.32f, w * 0.15f, fill)
        canvas.drawCircle(box.left + w * 0.30f, box.top + w * 0.32f, w * 0.15f, stroke)
        canvas.drawArc(
            RectF(box.left - w * 0.02f, box.top + w * 0.50f, box.left + w * 0.62f, box.bottom + w * 0.28f),
            180f, 180f, true, fill
        )
        canvas.drawCircle(box.right - w * 0.28f, box.top + w * 0.24f, w * 0.18f, fill)
        canvas.drawCircle(box.right - w * 0.28f, box.top + w * 0.24f, w * 0.18f, stroke)
        canvas.drawArc(
            RectF(box.left + w * 0.34f, box.top + w * 0.44f, box.right + w * 0.04f, box.bottom + w * 0.34f),
            180f, 180f, true, accent
        )
    }

    private fun drawChat(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint, accent: Paint) {
        val w = box.width()
        canvas.drawRoundRect(
            RectF(box.left, box.top + w * 0.10f, box.right - w * 0.10f, box.top + w * 0.62f),
            w * 0.20f, w * 0.20f, stroke
        )
        canvas.drawRoundRect(
            RectF(box.left + w * 0.24f, box.top + w * 0.44f, box.right, box.bottom - w * 0.06f),
            w * 0.20f, w * 0.20f, fill
        )
        canvas.drawRoundRect(
            RectF(box.left + w * 0.24f, box.top + w * 0.44f, box.right, box.bottom - w * 0.06f),
            w * 0.20f, w * 0.20f, stroke
        )
        canvas.drawCircle(box.left + w * 0.72f, box.top + w * 0.70f, w * 0.055f, accent)
    }

    private fun drawVideo(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint, accent: Paint) {
        val w = box.width()
        canvas.drawRoundRect(
            RectF(box.left, box.top + w * 0.26f, box.left + w * 0.68f, box.bottom - w * 0.18f),
            w * 0.16f, w * 0.16f, fill
        )
        canvas.drawRoundRect(
            RectF(box.left, box.top + w * 0.26f, box.left + w * 0.68f, box.bottom - w * 0.18f),
            w * 0.16f, w * 0.16f, stroke
        )
        val lens = Path().apply {
            moveTo(box.left + w * 0.72f, box.top + w * 0.42f)
            lineTo(box.right, box.top + w * 0.24f)
            lineTo(box.right, box.bottom - w * 0.16f)
            lineTo(box.left + w * 0.72f, box.bottom - w * 0.34f)
            close()
        }
        canvas.drawPath(lens, accent)
    }

    private fun drawVoice(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint, accent: Paint) {
        val w = box.width()
        canvas.drawRoundRect(
            RectF(box.centerX() - w * 0.14f, box.top, box.centerX() + w * 0.14f, box.top + w * 0.54f),
            w * 0.14f, w * 0.14f, fill
        )
        canvas.drawArc(
            RectF(box.centerX() - w * 0.34f, box.top + w * 0.16f, box.centerX() + w * 0.34f, box.top + w * 0.80f),
            0f, 180f, false, stroke
        )
        canvas.drawLine(box.centerX(), box.top + w * 0.80f, box.centerX(), box.bottom, stroke)
        canvas.drawLine(box.centerX() - w * 0.20f, box.bottom, box.centerX() + w * 0.20f, box.bottom, stroke)
        canvas.drawCircle(box.centerX(), box.top + w * 0.20f, w * 0.05f, accent)
    }

    private fun drawSos(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint, accent: Paint) {
        val w = box.width()
        canvas.drawCircle(box.centerX(), box.centerY(), w * 0.46f, fill)
        canvas.drawCircle(box.centerX(), box.centerY(), w * 0.46f, stroke)
        // three short marks: a distress pattern, not a copy of any SOS glyph
        repeat(3) { i ->
            canvas.drawLine(
                box.centerX() - w * 0.20f + i * w * 0.20f, box.centerY() - w * 0.14f,
                box.centerX() - w * 0.20f + i * w * 0.20f, box.centerY() + w * 0.14f,
                accent
            )
        }
    }

    // ------------------------------------------------------------ food and health

    private fun drawFood(canvas: Canvas, box: RectF, stroke: Paint, accent: Paint) {
        val w = box.width()
        canvas.drawArc(
            RectF(box.left, box.top + w * 0.10f, box.right, box.top + w * 0.78f),
            0f, 180f, false, stroke
        )
        canvas.drawLine(box.left, box.top + w * 0.44f, box.right, box.top + w * 0.44f, stroke)
        canvas.drawLine(box.left, box.top, box.left, box.top + w * 0.90f, accent)
        canvas.drawLine(box.right - w * 0.02f, box.top, box.right - w * 0.02f, box.top + w * 0.90f, accent)
        canvas.drawLine(box.right - w * 0.20f, box.top, box.right - w * 0.20f, box.top + w * 0.90f, accent)
    }

    private fun drawDrink(canvas: Canvas, box: RectF, stroke: Paint, accent: Paint) {
        val w = box.width()
        val cup = Path().apply {
            moveTo(box.left + w * 0.22f, box.top + w * 0.24f)
            lineTo(box.right - w * 0.22f, box.top + w * 0.24f)
            lineTo(box.centerX(), box.bottom - w * 0.02f)
            close()
        }
        canvas.drawPath(cup, stroke)
        canvas.drawLine(box.left + w * 0.30f, box.top + w * 0.10f, box.right - w * 0.30f, box.top + w * 0.10f, accent)
    }

    private fun drawPill(canvas: Canvas, box: RectF, fill: Paint, accent: Paint) {
        val w = box.width()
        canvas.save()
        canvas.rotate(-38f, box.centerX(), box.centerY())
        canvas.drawRoundRect(
            RectF(box.left + w * 0.06f, box.centerY() - w * 0.20f, box.right - w * 0.06f, box.centerY() + w * 0.20f),
            w * 0.20f, w * 0.20f, fill
        )
        canvas.drawRoundRect(
            RectF(box.left + w * 0.06f, box.centerY() - w * 0.20f, box.centerX(), box.centerY() + w * 0.20f),
            w * 0.20f, w * 0.20f, accent
        )
        canvas.restore()
    }

    private fun drawRun(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint, accent: Paint) {
        val w = box.width()
        canvas.drawCircle(box.left + w * 0.66f, box.top + w * 0.14f, w * 0.12f, fill)
        val stride = Path().apply {
            moveTo(box.centerX(), box.top + w * 0.30f)
            lineTo(box.left + w * 0.30f, box.top + w * 0.54f)
            lineTo(box.left + w * 0.44f, box.bottom - w * 0.02f)
        }
        canvas.drawPath(stride, stroke)
        val leg = Path().apply {
            moveTo(box.centerX(), box.top + w * 0.30f)
            lineTo(box.right - w * 0.18f, box.top + w * 0.60f)
        }
        canvas.drawPath(leg, stroke)
        canvas.drawLine(box.centerX(), box.top + w * 0.30f, box.left + w * 0.66f, box.top + w * 0.26f, accent)
    }

    private fun drawCycle(canvas: Canvas, box: RectF, stroke: Paint, accent: Paint) {
        val w = box.width()
        canvas.drawCircle(box.left + w * 0.26f, box.bottom - w * 0.26f, w * 0.22f, stroke)
        canvas.drawCircle(box.right - w * 0.26f, box.bottom - w * 0.26f, w * 0.22f, stroke)
        val frame = Path().apply {
            moveTo(box.left + w * 0.26f, box.bottom - w * 0.26f)
            lineTo(box.centerX() - w * 0.04f, box.top + w * 0.38f)
            lineTo(box.right - w * 0.26f, box.bottom - w * 0.26f)
        }
        canvas.drawPath(frame, accent)
        canvas.drawLine(box.centerX() - w * 0.04f, box.top + w * 0.38f, box.left + w * 0.40f, box.top + w * 0.38f, stroke)
    }

    private fun drawWeight(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint, accent: Paint) {
        val w = box.width()
        canvas.drawRoundRect(
            RectF(box.left + w * 0.08f, box.top + w * 0.30f, box.right - w * 0.08f, box.bottom - w * 0.04f),
            w * 0.18f, w * 0.18f, fill
        )
        canvas.drawRoundRect(
            RectF(box.left + w * 0.08f, box.top + w * 0.30f, box.right - w * 0.08f, box.bottom - w * 0.04f),
            w * 0.18f, w * 0.18f, stroke
        )
        canvas.drawArc(
            RectF(box.centerX() - w * 0.16f, box.top + w * 0.14f, box.centerX() + w * 0.16f, box.top + w * 0.44f),
            180f, 180f, false, stroke
        )
        canvas.drawLine(box.centerX(), box.top + w * 0.46f, box.centerX() + w * 0.10f, box.top + w * 0.34f, accent)
    }

    // ------------------------------------------------------------ travel and money

    private fun drawFlight(canvas: Canvas, box: RectF, fill: Paint, accent: Paint) {
        val w = box.width()
        canvas.save()
        canvas.rotate(-20f, box.centerX(), box.centerY())
        val plane = Path().apply {
            moveTo(box.centerX(), box.top + w * 0.02f)
            lineTo(box.centerX() + w * 0.10f, box.top + w * 0.44f)
            lineTo(box.right - w * 0.02f, box.centerY() + w * 0.10f)
            lineTo(box.right - w * 0.02f, box.centerY() + w * 0.22f)
            lineTo(box.centerX() + w * 0.10f, box.centerY() + w * 0.30f)
            lineTo(box.centerX() + w * 0.16f, box.bottom - w * 0.04f)
            lineTo(box.centerX() + w * 0.02f, box.bottom - w * 0.04f)
            lineTo(box.centerX() - w * 0.06f, box.centerY() + w * 0.30f)
            close()
        }
        canvas.drawPath(plane, fill)
        canvas.drawCircle(box.centerX() + w * 0.02f, box.top + w * 0.24f, w * 0.06f, accent)
        canvas.restore()
    }

    private fun drawCar(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint, accent: Paint) {
        val w = box.width()
        val body = Path().apply {
            moveTo(box.left + w * 0.04f, box.top + w * 0.68f)
            lineTo(box.left + w * 0.16f, box.top + w * 0.40f)
            lineTo(box.left + w * 0.34f, box.top + w * 0.24f)
            lineTo(box.right - w * 0.34f, box.top + w * 0.24f)
            lineTo(box.right - w * 0.16f, box.top + w * 0.40f)
            lineTo(box.right - w * 0.04f, box.top + w * 0.68f)
            close()
        }
        canvas.drawPath(body, fill)
        canvas.drawPath(body, stroke)
        canvas.drawCircle(box.left + w * 0.28f, box.bottom - w * 0.12f, w * 0.13f, stroke)
        canvas.drawCircle(box.right - w * 0.28f, box.bottom - w * 0.12f, w * 0.13f, stroke)
        canvas.drawCircle(box.left + w * 0.28f, box.bottom - w * 0.12f, w * 0.05f, accent)
        canvas.drawCircle(box.right - w * 0.28f, box.bottom - w * 0.12f, w * 0.05f, accent)
    }

    private fun drawShop(canvas: Canvas, box: RectF, stroke: Paint, accent: Paint) {
        val w = box.width()
        // awning: three scallops, so it reads as a shopfront rather than a box
        val awning = Path().apply {
            moveTo(box.left, box.top + w * 0.30f)
            lineTo(box.right, box.top + w * 0.30f)
            lineTo(box.right, box.top + w * 0.46f)
            cubicTo(box.right - w * 0.18f, box.top + w * 0.60f, box.right - w * 0.32f, box.top + w * 0.46f, box.centerX(), box.top + w * 0.52f)
            cubicTo(box.left + w * 0.32f, box.top + w * 0.46f, box.left + w * 0.18f, box.top + w * 0.60f, box.left, box.top + w * 0.46f)
            close()
        }
        canvas.drawPath(awning, accent)
        canvas.drawRoundRect(
            RectF(box.left + w * 0.12f, box.top + w * 0.52f, box.right - w * 0.12f, box.bottom),
            w * 0.05f, w * 0.05f, stroke
        )
        canvas.drawLine(box.centerX(), box.top + w * 0.52f, box.centerX(), box.bottom, stroke)
    }

    private fun drawBank(canvas: Canvas, box: RectF, stroke: Paint, accent: Paint) {
        val w = box.width()
        val pediment = Path().apply {
            moveTo(box.left + w * 0.04f, box.top + w * 0.30f)
            lineTo(box.centerX(), box.top + w * 0.04f)
            lineTo(box.right - w * 0.04f, box.top + w * 0.30f)
            close()
        }
        canvas.drawPath(pediment, stroke)
        canvas.drawLine(box.left, box.top + w * 0.34f, box.right, box.top + w * 0.34f, stroke)
        repeat(4) { i ->
            val x = box.left + w * (0.18f + i * 0.215f)
            canvas.drawLine(x, box.top + w * 0.40f, x, box.bottom - w * 0.10f, accent)
        }
        canvas.drawLine(box.left, box.bottom - w * 0.04f, box.right, box.bottom - w * 0.04f, stroke)
    }

    private fun drawTicket(canvas: Canvas, box: RectF, stroke: Paint, accent: Paint, fill: Paint) {
        val w = box.width()
        canvas.drawRoundRect(
            RectF(box.left, box.top + w * 0.22f, box.right, box.bottom - w * 0.22f),
            w * 0.12f, w * 0.12f, stroke
        )
        // tear line with two notches
        canvas.drawLine(box.left + w * 0.62f, box.top + w * 0.22f, box.left + w * 0.62f, box.bottom - w * 0.22f, stroke)
        canvas.drawCircle(box.left + w * 0.62f, box.top + w * 0.22f, w * 0.06f, fill)
        canvas.drawCircle(box.left + w * 0.62f, box.bottom - w * 0.22f, w * 0.06f, fill)
        canvas.drawCircle(box.left + w * 0.30f, box.centerY(), w * 0.08f, accent)
    }

    private fun drawPin(canvas: Canvas, box: RectF, stroke: Paint, fill: Paint, accent: Paint) {
        val w = box.width()
        val drop = Path().apply {
            moveTo(box.centerX(), box.bottom)
            cubicTo(box.left + w * 0.04f, box.top + w * 0.58f, box.left + w * 0.16f, box.top, box.centerX(), box.top)
            cubicTo(box.right - w * 0.16f, box.top, box.right - w * 0.04f, box.top + w * 0.58f, box.centerX(), box.bottom)
            close()
        }
        canvas.drawPath(drop, fill)
        canvas.drawPath(drop, stroke)
        canvas.drawCircle(box.centerX(), box.top + w * 0.34f, w * 0.11f, accent)
    }

}
