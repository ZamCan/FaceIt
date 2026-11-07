package com.zamcan.faceit.graphics

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.RadialGradient
import android.graphics.Color
import com.zamcan.faceit.data.SceneLighting
import com.zamcan.faceit.data.PersonalEnvironment
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

/** Offline layer compositor. Imported background artwork remains independent of the selected object layer. */
object WallpaperRenderer {
    fun render(
        width: Int,
        height: Int,
        environment: PersonalEnvironment,
        artworkFile: File? = null,
        objectArtworkFile: File? = null,
        curatedArtwork: Bitmap? = null
    ): Bitmap {
        require(width > 0 && height > 0)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val scale = width / 1080f
        fun px(value: Float) = value * scale

        // Every bundled wallpaper is a photograph now. It is composed to the
        // screen shape first — a landscape cropped to 9:20 would lose two thirds
        // of its width and go soft, so PhotoComposer fits it by width over a
        // blurred enlargement of itself.
        val photo = curatedArtwork?.takeIf { !it.isRecycled }
        val composed = photo?.let { PhotoComposer.compose(width, height, it) }
        val curated = composed
        val imported = curated ?: artworkFile?.takeIf { it.isFile }?.let { decodeForTarget(it, width, height) }
        if (imported != null) {
            drawCenterCrop(canvas, imported, width, height, environment.cropX, environment.cropY)
            if (imported !== curated) imported.recycle()
            if (composed != null) composed.recycle()
            canvas.drawRect(
                0f, 0f, width.toFloat(), height.toFloat(),
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(
                        0f, 0f, 0f, height.toFloat(),
                        intArrayOf(0x18000000, 0x00000000, 0x55000000),
                        null, Shader.TileMode.CLAMP
                    )
                }
            )
        } else {
            drawBackground(canvas, width, height, environment.background, ::px)
        }

        // The object layer and the material grain are both drawn procedurally
        // now: the bundled object renders and material tiles were part of the
        // removed artwork pack. A user's own imported object still wins.
        if (environment.objectOpacity < 1f) {
            canvas.saveLayerAlpha(0f, 0f, width.toFloat(), height.toFloat(),
                (environment.objectOpacity.coerceIn(0f, 1f) * 255).toInt())
        } else canvas.save()
        canvas.translate((environment.objectX - .5f) * width, (environment.objectY - .38f) * height)
        canvas.scale(environment.objectScale, environment.objectScale, width / 2f, height * .38f)
        val importedObject = objectArtworkFile?.takeIf { it.isFile }
            ?.let { decodeForObject(it, width, height) }
        if (importedObject != null) {
            drawImportedObject(canvas, importedObject, width, height)
            importedObject.recycle()
        } else {
            drawMainObject(canvas, width, height, environment.mainObject, environment.material, ::px)
        }
        canvas.restore()
        drawSceneLighting(canvas, width, height, environment.lighting)
        drawVignette(canvas, width, height)

        // The name engine draws last, on top of the ground and the object: it is
        // the user's own signature on the composition, so nothing covers it.
        environment.nameDesignOrNull()?.let { design ->
            NameMonogram.draw(canvas, width, height, design)
        }
        return bitmap
    }

    /** Bounded shader passes: no animation loop, blur allocation or external texture. */
    private fun drawSceneLighting(canvas: Canvas, width: Int, height: Int, lighting: SceneLighting) {
        if (!lighting.enabled || lighting.intensity <= 0f) return
        val intensity = lighting.intensity.coerceIn(0f, 1f)
        val warmth = lighting.warmth.coerceIn(-1f, 1f)
        val red = (255 - 65 * (-warmth).coerceAtLeast(0f)).toInt()
        val green = (255 - 35 * kotlin.math.abs(warmth)).toInt()
        val blue = (255 - 80 * warmth.coerceAtLeast(0f)).toInt()
        val radius = maxOf(width, height) * (.35f + lighting.softness.coerceIn(0f, 1f) * .8f)
        val highlight = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(width * lighting.direction.x, height * lighting.direction.y,
                radius, intArrayOf(Color.argb((intensity * 95).toInt(), red, green, blue), Color.TRANSPARENT),
                null, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), highlight)
        val shade = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(width * (1f - lighting.direction.x), height * (1f - lighting.direction.y),
                radius, intArrayOf(Color.argb((intensity * 65).toInt(), 0, 0, 0), Color.TRANSPARENT),
                null, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), shade)
    }

    private fun drawBackground(
        canvas: Canvas,
        width: Int,
        height: Int,
        category: String,
        px: (Float) -> Float
    ) {
        val colors = when {
            category.contains("Heritage", true) -> intArrayOf(0xFF172A31.toInt(), 0xFF806D55.toInt(), 0xFF1A2528.toInt())
            category.contains("Nature", true) -> intArrayOf(0xFF182F3B.toInt(), 0xFF71836E.toInt(), 0xFF273B32.toInt())
            category.contains("Geometry", true) -> intArrayOf(0xFF211B32.toInt(), 0xFF54445F.toInt(), 0xFF161923.toInt())
            category.contains("City", true) -> intArrayOf(0xFF202A3B.toInt(), 0xFF7B6C66.toInt(), 0xFF171B25.toInt())
            category.contains("Peak", true) || category.contains("Mountain", true) ->
                intArrayOf(0xFF150F1E.toInt(), 0xFFE0876A.toInt(), 0xFF2A1526.toInt())
            category.contains("Automotive", true) -> intArrayOf(0xFF1A2028.toInt(), 0xFF4E555C.toInt(), 0xFF101318.toInt())
            else -> intArrayOf(0xFF171923.toInt(), 0xFF292B32.toInt(), 0xFF101B1A.toInt())
        }
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), colors, null, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bg)

        when {
            category.contains("Heritage", true) -> drawMosqueSilhouette(canvas, width, height, px)
            category.contains("Nature", true) -> drawLandscape(canvas, width, height, px)
            category.contains("Geometry", true) -> drawGeometry(canvas, width, height, px)
            category.contains("City", true) -> drawCityscape(canvas, width, height, px)
            category.contains("Peak", true) || category.contains("Mountain", true) ->
                drawMountainSilhouette(canvas, width, height, px)
            category.contains("Automotive", true) -> drawRoad(canvas, width, height, px)
            else -> drawStadium(canvas, width, height, px)
        }
    }

    private fun drawStadium(canvas: Canvas, width: Int, height: Int, px: (Float) -> Float) {
        val horizon = height * .54f
        val stand = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x553C4549; style = Paint.Style.FILL }
        canvas.drawRect(0f, horizon - px(270f), width.toFloat(), horizon + px(25f), stand)
        for (row in 0..7) {
            val y = horizon - px(250f) + row * px(31f)
            canvas.drawLine(0f, y, width.toFloat(), y, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0x227E8B8C; strokeWidth = px(2f)
            })
        }
        val lights = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x66F1E8D2; strokeWidth = px(5f) }
        canvas.drawLine(px(110f), px(90f), px(110f), horizon - px(55f), lights)
        canvas.drawLine(width - px(110f), px(90f), width - px(110f), horizon - px(55f), lights)
        for (i in 0..4) {
            canvas.drawCircle(px(82f + i * 14f), px(90f), px(5f), Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xAAFFF1C8.toInt() })
            canvas.drawCircle(width - px(82f + i * 14f), px(90f), px(5f), Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xAAFFF1C8.toInt() })
        }
        val pitch = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x55D8D5BE; style = Paint.Style.STROKE; strokeWidth = px(3f) }
        canvas.drawRect(px(45f), horizon, width - px(45f), height - px(65f), pitch)
        canvas.drawLine(width / 2f, horizon, width / 2f, height - px(65f), pitch)
        canvas.drawCircle(width / 2f, (horizon + height - px(65f)) / 2f, px(112f), pitch)
    }

    private fun drawMosqueSilhouette(canvas: Canvas, width: Int, height: Int, px: (Float) -> Float) {
        val base = height * .67f
        val building = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xAA151F22.toInt() }
        canvas.drawRect(width * .22f, base - px(160f), width * .78f, base + px(180f), building)
        val dome = Path().apply {
            moveTo(width * .35f, base - px(155f))
            cubicTo(width * .35f, base - px(340f), width * .65f, base - px(340f), width * .65f, base - px(155f))
            close()
        }
        canvas.drawPath(dome, building)
        for (x in listOf(width * .18f, width * .82f)) {
            canvas.drawRect(x - px(22f), base - px(300f), x + px(22f), base + px(170f), building)
            canvas.drawCircle(x, base - px(300f), px(24f), building)
            canvas.drawLine(x, base - px(370f), x, base - px(325f), Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xAAE1C99C.toInt(); strokeWidth = px(5f)
            })
        }
        val arch = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF25343A.toInt() }
        for (i in 0..4) {
            val x = width * .29f + i * width * .105f
            canvas.drawRoundRect(RectF(x, base - px(100f), x + px(62f), base + px(70f)), px(31f), px(31f), arch)
        }
        canvas.drawRect(0f, base + px(180f), width.toFloat(), height.toFloat(), Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x6610191C })
    }

    private fun drawLandscape(canvas: Canvas, width: Int, height: Int, px: (Float) -> Float) {
        canvas.drawCircle(width * .76f, height * .22f, px(74f), Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x66F2D8A4 })
        val far = Path().apply {
            moveTo(0f, height * .62f); lineTo(width * .28f, height * .34f)
            lineTo(width * .48f, height * .59f); lineTo(width * .73f, height * .30f)
            lineTo(width.toFloat(), height * .62f); lineTo(width.toFloat(), height.toFloat())
            lineTo(0f, height.toFloat()); close()
        }
        canvas.drawPath(far, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF354B4A.toInt() })
        val near = Path().apply {
            moveTo(0f, height * .76f); lineTo(width * .35f, height * .53f)
            lineTo(width * .57f, height * .72f); lineTo(width * .84f, height * .48f)
            lineTo(width.toFloat(), height * .74f); lineTo(width.toFloat(), height.toFloat())
            lineTo(0f, height.toFloat()); close()
        }
        canvas.drawPath(near, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF1B302C.toInt() })
    }

    private fun drawGeometry(canvas: Canvas, width: Int, height: Int, px: (Float) -> Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x338E83A5; style = Paint.Style.STROKE; strokeWidth = px(2f) }
        val cx = width / 2f
        val cy = height * .48f
        for (i in 1..9) canvas.drawCircle(cx, cy, px(i * 82f), paint)
        for (i in 0..11) {
            val angle = i * Math.PI / 6.0
            canvas.drawLine(cx, cy, cx + cos(angle).toFloat() * width, cy + sin(angle).toFloat() * height, paint)
        }
    }

    /** A single cone under a graded sky: Kilimanjaro's shape, drawn not traced. */
    private fun drawMountainSilhouette(canvas: Canvas, width: Int, height: Int, px: (Float) -> Float) {
        val horizon = height * 0.58f
        val peak = height * 0.30f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, peak, 0f, horizon,
                intArrayOf(0xFFFFE7CE.toInt(), 0xFFC0705A.toInt(), 0xFF2A1A22.toInt()),
                null, Shader.TileMode.CLAMP
            )
        }
        val path = Path().apply {
            moveTo(0f, horizon)
            cubicTo(width * .18f, horizon * .96f, width * .34f, horizon * .84f, width * .44f, peak * 1.06f)
            cubicTo(width * .48f, peak * .94f, width * .56f, peak * .96f, width * .60f, peak * 1.10f)
            cubicTo(width * .74f, horizon * .86f, width * .90f, horizon * .95f, width.toFloat(), horizon)
            lineTo(width.toFloat(), horizon * 1.24f)
            lineTo(0f, horizon * 1.24f)
            close()
        }
        canvas.drawPath(path, paint)
        canvas.drawRect(
            0f, horizon * 1.2f, width.toFloat(), height.toFloat(),
            Paint().apply { color = 0xFF1A1018.toInt() }
        )
    }

    private fun drawCityscape(canvas: Canvas, width: Int, height: Int, px: (Float) -> Float) {
        val base = height * .68f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xAA151A24.toInt() }
        for (i in 0..10) {
            val left = i * width / 11f
            val buildingHeight = px((190 + (i * 73 % 210)).toFloat())
            canvas.drawRect(left, base - buildingHeight, left + width / 13f, height.toFloat(), paint)
        }
    }

    private fun drawRoad(canvas: Canvas, width: Int, height: Int, px: (Float) -> Float) {
        val road = Path().apply {
            moveTo(width * .38f, height * .49f); lineTo(width * .62f, height * .49f)
            lineTo(width * 1.12f, height.toFloat()); lineTo(width * -.12f, height.toFloat()); close()
        }
        canvas.drawPath(road, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF15191F.toInt() })
        val centre = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x77E7D8B3.toInt(); strokeWidth = px(5f) }
        canvas.drawLine(width / 2f, height * .55f, width / 2f, height.toFloat(), centre)
    }

    private fun drawMainObject(
        canvas: Canvas,
        width: Int,
        height: Int,
        objectName: String,
        material: String,
        px: (Float) -> Float
    ) {
        if (objectName.equals("None", true) || objectName.isBlank()) return
        val cx = width / 2f
        val cy = height * .39f
        val radius = px(245f)
        val palette = when {
            material.contains("Ceramic", true) -> intArrayOf(0xFFFFFDF7.toInt(), 0xFFE7E0D3.toInt(), 0xFFBEB5A6.toInt())
            material.contains("Glass", true) -> intArrayOf(0xFFD8E7E8.toInt(), 0xFF718F9A.toInt(), 0xFF334A56.toInt())
            material.contains("Metal", true) -> intArrayOf(0xFFD4D8DC.toInt(), 0xFF7A838C.toInt(), 0xFF353B43.toInt())
            material.contains("Marble", true) -> intArrayOf(0xFFF2EBDD.toInt(), 0xFFB6A993.toInt(), 0xFF716A5D.toInt())
            else -> intArrayOf(0xFFE5D8BC.toInt(), 0xFF9F9077.toInt(), 0xFF514B43.toInt())
        }
        val objectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(cx - radius, cy - radius, cx + radius, cy + radius, palette, null, Shader.TileMode.CLAMP)
        }
        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x55000000; maskFilter = android.graphics.BlurMaskFilter(px(28f), android.graphics.BlurMaskFilter.Blur.NORMAL) }
        canvas.drawOval(RectF(cx - radius * .78f, cy + radius * .82f, cx + radius * .78f, cy + radius * 1.02f), shadow)

        when {
            objectName.contains("Lion", true) -> drawLion(canvas, cx, cy, radius, px, objectPaint)
            objectName.contains("crystal", true) || objectName.contains("Geometric", true) ->
                drawCrystal(canvas, cx, cy, radius, px, objectPaint)
            objectName.contains("Stethoscope", true) -> drawStethoscope(canvas, cx, cy, radius, px, objectPaint)
            objectName.contains("Mechanical", true) -> drawMechanical(canvas, cx, cy, radius, px, objectPaint)
            else -> drawCrystal(canvas, cx, cy, radius, px, objectPaint)
        }
    }

    private fun drawLion(canvas: Canvas, cx: Float, cy: Float, radius: Float, px: (Float) -> Float, paint: Paint) {
        val mane = Path()
        for (i in 0..64) {
            val angle = i * 2.0 * Math.PI / 64.0
            val r = radius * if (i % 4 == 0) 1f else .91f
            val x = cx + cos(angle).toFloat() * r
            val y = cy + sin(angle).toFloat() * r * 1.08f
            if (i == 0) mane.moveTo(x, y) else mane.lineTo(x, y)
        }
        mane.close()
        canvas.drawPath(mane, paint)
        val face = Path().apply {
            moveTo(cx, cy - px(180f))
            cubicTo(cx - px(170f), cy - px(155f), cx - px(155f), cy + px(85f), cx, cy + px(190f))
            cubicTo(cx + px(155f), cy + px(85f), cx + px(170f), cy - px(155f), cx, cy - px(180f))
            close()
        }
        canvas.drawPath(face, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(cx, cy - radius, cx, cy + radius, 0xFFE9DFC9.toInt(), 0xFF8F8068.toInt(), Shader.TileMode.CLAMP)
        })
        val detail = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF39332D.toInt(); style = Paint.Style.STROKE; strokeWidth = px(9f); strokeCap = Paint.Cap.ROUND }
        canvas.drawLine(cx - px(72f), cy - px(30f), cx - px(20f), cy - px(18f), detail)
        canvas.drawLine(cx + px(20f), cy - px(18f), cx + px(72f), cy - px(30f), detail)
        canvas.drawCircle(cx, cy + px(54f), px(19f), Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF39332D.toInt() })
        canvas.drawLine(cx, cy + px(70f), cx, cy + px(112f), detail)
    }

    private fun drawCrystal(canvas: Canvas, cx: Float, cy: Float, radius: Float, px: (Float) -> Float, paint: Paint) {
        val crystal = Path().apply {
            moveTo(cx, cy - radius); lineTo(cx + radius * .58f, cy - radius * .18f)
            lineTo(cx + radius * .42f, cy + radius * .72f); lineTo(cx, cy + radius)
            lineTo(cx - radius * .42f, cy + radius * .72f); lineTo(cx - radius * .58f, cy - radius * .18f); close()
        }
        canvas.drawPath(crystal, paint)
        val facets = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x66FFFFFF; style = Paint.Style.STROKE; strokeWidth = px(3f) }
        canvas.drawLine(cx, cy - radius, cx, cy + radius, facets)
        canvas.drawLine(cx - radius * .58f, cy - radius * .18f, cx + radius * .42f, cy + radius * .72f, facets)
        canvas.drawLine(cx + radius * .58f, cy - radius * .18f, cx - radius * .42f, cy + radius * .72f, facets)
    }

    private fun drawStethoscope(canvas: Canvas, cx: Float, cy: Float, radius: Float, px: (Float) -> Float, paint: Paint) {
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = paletteColor(paint); style = Paint.Style.STROKE; strokeWidth = px(28f); strokeCap = Paint.Cap.ROUND }
        val path = Path().apply {
            moveTo(cx - radius * .48f, cy - radius * .65f)
            lineTo(cx - radius * .48f, cy + radius * .1f)
            cubicTo(cx - radius * .48f, cy + radius * .7f, cx + radius * .48f, cy + radius * .7f, cx + radius * .48f, cy + radius * .1f)
            lineTo(cx + radius * .48f, cy - radius * .65f)
        }
        canvas.drawPath(path, stroke)
        canvas.drawCircle(cx, cy + radius * .58f, px(50f), paint)
        canvas.drawCircle(cx - radius * .48f, cy - radius * .65f, px(34f), paint)
        canvas.drawCircle(cx + radius * .48f, cy - radius * .65f, px(34f), paint)
    }

    private fun drawMechanical(canvas: Canvas, cx: Float, cy: Float, radius: Float, px: (Float) -> Float, paint: Paint) {
        val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = paletteColor(paint); style = Paint.Style.STROKE; strokeWidth = px(58f) }
        canvas.drawCircle(cx, cy, radius * .72f, ring)
        val spokes = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = paletteColor(paint); strokeWidth = px(38f); strokeCap = Paint.Cap.ROUND }
        for (i in 0..7) {
            val angle = i * Math.PI / 4.0
            canvas.drawLine(
                cx + cos(angle).toFloat() * radius * .45f,
                cy + sin(angle).toFloat() * radius * .45f,
                cx + cos(angle).toFloat() * radius * .96f,
                cy + sin(angle).toFloat() * radius * .96f,
                spokes
            )
        }
        canvas.drawCircle(cx, cy, radius * .32f, paint)
    }

    private fun paletteColor(paint: Paint): Int = paint.color.takeIf { it != 0 } ?: 0xFFB9AE9C.toInt()

    private fun drawVignette(canvas: Canvas, width: Int, height: Int) {
        val vignette = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(0x22000000, 0x00000000, 0x66000000),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), vignette)
    }

    private fun decodeForTarget(file: File, width: Int, height: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val sample = sampleSize(bounds.outWidth, bounds.outHeight, width, height)
        return BitmapFactory.decodeFile(file.absolutePath, BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        })
    }

    private fun decodeForObject(file: File, width: Int, height: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val sample = sampleSize(
            bounds.outWidth, bounds.outHeight,
            (width * .68f).toInt().coerceAtLeast(1),
            (height * .42f).toInt().coerceAtLeast(1)
        )
        return BitmapFactory.decodeFile(file.absolutePath, BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        })
    }

    private fun drawImportedObject(canvas: Canvas, source: Bitmap, width: Int, height: Int) {
        val maxWidth = width * .68f
        val maxHeight = height * .42f
        val scale = minOf(maxWidth / source.width, maxHeight / source.height)
        val drawWidth = source.width * scale
        val drawHeight = source.height * scale
        val left = (width - drawWidth) / 2f
        val top = height * .38f - drawHeight / 2f
        val destination = RectF(left, top, left + drawWidth, top + drawHeight)
        canvas.drawBitmap(source, null, destination, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
    }

    /**
     * Lay a curated material tile over the finished composition.
     *
     * Using a BitmapShader keeps the cost constant no matter how large the
     * wallpaper is, and a fixed low alpha makes the finish a texture rather than
     * a filter over the user's artwork.
     */
    private fun sampleSize(sourceWidth: Int, sourceHeight: Int, targetWidth: Int, targetHeight: Int): Int {
        val pixelBudget = targetWidth.toLong() * targetHeight.toLong() * 4L
        var sample = 1
        while ((sourceWidth.toLong() / (sample * 2L)) *
            (sourceHeight.toLong() / (sample * 2L)) > pixelBudget
        ) sample *= 2
        return sample
    }

    private fun drawCenterCrop(canvas: Canvas, source: Bitmap, width: Int, height: Int, focusX: Float, focusY: Float) {
        val sourceRatio = source.width.toFloat() / source.height
        val targetRatio = width.toFloat() / height
        val sourceRect = if (sourceRatio > targetRatio) {
            val cropWidth = (source.height * targetRatio).toInt().coerceAtLeast(1)
            val left = ((source.width - cropWidth) * focusX.coerceIn(0f, 1f)).toInt()
            Rect(left, 0, left + cropWidth, source.height)
        } else {
            val cropHeight = (source.width / targetRatio).toInt().coerceAtLeast(1)
            val top = ((source.height - cropHeight) * focusY.coerceIn(0f, 1f)).toInt()
            Rect(0, top, source.width, top + cropHeight)
        }
        canvas.drawBitmap(source, sourceRect, Rect(0, 0, width, height), Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
    }
}

