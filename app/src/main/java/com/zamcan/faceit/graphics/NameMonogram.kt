package com.zamcan.faceit.graphics

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * The name engine: whatever the user types, drawn in the finish they picked.
 *
 * This replaces the idea of shipping one picture per name. A bundled picture of
 * "Naomi" is a picture of one person's name; the product owner asked for the same
 * treatment with an editable name, and the honest way to do that is to set the
 * text at render time and spend the artwork budget on the finish instead.
 *
 * Letterforms are the device's own fonts. Two reasons, and both are product
 * reasons rather than shortcuts:
 *
 *  * A name is personal data. It can be any script, any length, any diacritic.
 *    Hand-drawn letterforms for a fixed alphabet would mangle half the names the
 *    app is likely to be given in Tanzania and the region — "Ng'ang'a", "Qureshi",
 *    "Šimon", "佐藤" — where a font falls back and still sets them correctly.
 *  * Real glyphs stay real text. They can be selected, read aloud by a screen
 *    reader and copied into a message, which is the same rule the writing styles
 *    follow (see `WritingStyles`), rather than the Unicode look-alike trick that
 *    turns a message into noise on the other person's phone.
 *
 * What is engineered here is the light: the metal, the neon, the glow, the
 * engraved plaque. That is what makes a name look like it cost money.
 */

/** The finish a name is rendered in. Colours are the whole point of the shelf. */
enum class NameFinish(
    val id: String,
    val label: String,
    /** Warm or cool; used to pick a matching backdrop tint. */
    val base: Int,
    val highlight: Int,
    val shadow: Int,
    /** Channel colour for the glowing variants, and the bloom strength. */
    val glow: Int,
    val glowRadius: Float
) {
    ROSE_GOLD("rose_gold", "Rose gold", 0xFFB4755E.toInt(), 0xFFFFE2CE.toInt(), 0xFF4A2218.toInt(), 0xFFFFB383.toInt(), 26f),
    GOLD("gold", "Gold", 0xFFB78A34.toInt(), 0xFFFFF0B8.toInt(), 0xFF4A3208.toInt(), 0xFFFFC24A.toInt(), 26f),
    CHAMPAGNE("champagne", "Champagne", 0xFFCBB08A.toInt(), 0xFFFFF7E6.toInt(), 0xFF54402A.toInt(), 0xFFFFDFA0.toInt(), 22f),
    PLATINUM("platinum", "Platinum", 0xFFAFB6C0.toInt(), 0xFFFFFFFF.toInt(), 0xFF3A3F48.toInt(), 0xFFDDE6F2.toInt(), 22f),
    OBSIDIAN("obsidian", "Obsidian", 0xFF2A2A32.toInt(), 0xFF8E93A2.toInt(), 0xFF050506.toInt(), 0xFF6E7790.toInt(), 12f),
    BLUSH("blush", "Blush", 0xFFE4A0A8.toInt(), 0xFFFFEDEF.toInt(), 0xFF6E2F38.toInt(), 0xFFFFA9BC.toInt(), 24f),
    CORAL("coral", "Coral", 0xFFE2684F.toInt(), 0xFFFFD9C8.toInt(), 0xFF6B1E14.toInt(), 0xFFFF8A5C.toInt(), 26f),
    TANGERINE("tangerine", "Tangerine", 0xFFE08228.toInt(), 0xFFFFE6B8.toInt(), 0xFF6B3A06.toInt(), 0xFFFF9E3C.toInt(), 26f),
    LILAC("lilac", "Lilac", 0xFFB49BD0.toInt(), 0xFFF7EDFF.toInt(), 0xFF3D2C52.toInt(), 0xFFCBAAF5.toInt(), 26f),
    MINT("mint", "Mint", 0xFF8FCFB8.toInt(), 0xFFEFFBF4.toInt(), 0xFF1E4A3C.toInt(), 0xFF9BE8CC.toInt(), 24f),
    TURQUOISE("turquoise", "Turquoise", 0xFF3FB3B0.toInt(), 0xFFD2FBF6.toInt(), 0xFF0B3D3E.toInt(), 0xFF4FE0DA.toInt(), 26f),
    SAPPHIRE("sapphire", "Sapphire", 0xFF3F63C4.toInt(), 0xFFD8E4FF.toInt(), 0xFF101F52.toInt(), 0xFF5F8BFF.toInt(), 28f),
    EMERALD("emerald", "Emerald", 0xFF2E9A6A.toInt(), 0xFFD6FBE7.toInt(), 0xFF0A3A26.toInt(), 0xFF45D48F.toInt(), 26f),
    RUBY("ruby", "Ruby", 0xFFB02A46.toInt(), 0xFFFFD6DE.toInt(), 0xFF470A16.toInt(), 0xFFFF4D72.toInt(), 28f),
    IVORY("ivory", "Ivory", 0xFFE8E1CF.toInt(), 0xFFFFFFFF.toInt(), 0xFF5A5342.toInt(), 0xFFFFF3D0.toInt(), 16f);

    companion object {
        fun find(id: String?): NameFinish = entries.firstOrNull { it.id == id } ?: ROSE_GOLD
    }
}

/** A script voice for the name. Families only — nothing is bundled, so nothing is unlicensed. */
enum class NameScript(val id: String, val label: String, val cursive: Boolean) {
    SCRIPT("script", "Script", true),
    SERIF("serif", "Serif", false),
    SANS("sans", "Sans", false),
    MONO("mono", "Mono", false);

    fun typeface(bold: Boolean, italic: Boolean): Typeface {
        val base = when (this) {
            SCRIPT -> Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            SERIF -> Typeface.SERIF
            SANS -> Typeface.SANS_SERIF
            MONO -> Typeface.MONOSPACE
        }
        var style = Typeface.NORMAL
        if (bold) style = style or Typeface.BOLD
        if (italic || (this == SCRIPT)) style = style or Typeface.ITALIC
        return Typeface.create(base, style)
    }

    companion object {
        fun find(id: String?): NameScript = entries.firstOrNull { it.id == id } ?: SCRIPT
    }
}

/** How confident the type is. Tracking and slant are per-script because they read differently. */
enum class NameSetting(val id: String, val label: String) {
    NORMAL("normal", "Normal"),
    WIDE("wide", "Wide"),
    TIGHT("tight", "Tight");

    fun tracking(script: NameScript): Float = when (this) {
        NORMAL -> if (script.cursive) 0.04f else 0.02f
        WIDE -> 0.18f
        TIGHT -> -0.02f
    }

    companion object {
        fun find(id: String?): NameSetting = entries.firstOrNull { it.id == id } ?: NORMAL
    }
}

/** Everything the renderer needs, small, serialisable, and independent of Compose. */
data class NameDesign(
    val text: String = "Naomi",
    val finishId: String = NameFinish.ROSE_GOLD.id,
    val scriptId: String = NameScript.SCRIPT.id,
    val settingId: String = NameSetting.NORMAL.id,
    /** Where the name sits vertically, 0 = top of the screen, 1 = bottom. */
    val anchor: Float = 0.42f,
    val uppercase: Boolean = true,
    /** Draw the engraved panel behind the type. */
    val plaque: Boolean = true,
    /** Photograph id used as the ground, or null for the finish's own backdrop. */
    val groundId: String? = null
) {
    val finish: NameFinish get() = NameFinish.find(finishId)
    val script: NameScript get() = NameScript.find(scriptId)
    val setting: NameSetting get() = NameSetting.find(settingId)

    /** What actually gets set: the user's words, normalised, never substituted. */
    fun displayText(): String {
        val trimmed = text.replace(Regex("\\s+"), " ").trim()
        if (trimmed.isEmpty()) return "Your name"
        return if (uppercase) trimmed.uppercase() else trimmed
    }

    /** Long names shrink instead of being clipped or wrapped mid-word. */
    fun effectiveLength(): Int = displayText().length
}

/**
 * Renders a [NameDesign] at any size.
 *
 * The composition is built at the caller's resolution so the type stays sharp at
 * 1080 px or at wallpaper size; nothing here is a fixed-size asset. Costs are
 * bounded: three text passes, one plaque pass and one radial vignette, with the
 * bloom done by blurred copies of the type itself rather than by post-processing
 * the whole screen.
 */
object NameMonogram {

    /** Fraction of the width the name may occupy, after anchors and margins. */
    private const val MARGIN_FRACTION = 0.86f

    /**
     * Draw [design] over an existing canvas, e.g. a photograph.
     *
     * [groundWidthPx] and [groundHeightPx] describe the surface being drawn on;
     * the caller has already painted whatever sits behind the name.
     */
    fun draw(canvas: Canvas, width: Int, height: Int, design: NameDesign) {
        require(width > 0 && height > 0)
        val finish = design.finish
        val text = design.displayText()
        val paint = textPaint(design, width, height)

        if (design.plaque) drawPlaque(canvas, width, height, design.anchor, finish)

        val centreX = width / 2f
        val baselineY = height * design.anchor + paint.textSize * 0.34f
        paint.textAlign = Paint.Align.CENTER
        paint.letterSpacing = design.setting.tracking(design.script)

        // Bloom first: two blurred copies of the same glyphs, then the sharp metal
        // over the top. Blurring the type rather than the frame keeps the cost flat
        // and the halo tied to the letters instead of to the whole screen.
        if (finish.glowRadius > 0f) {
            val bloom = Paint(paint).apply {
                shader = null
                color = finish.glow
                style = Paint.Style.FILL
                maskFilter = BlurMaskFilter(
                    paint.textSize * finish.glowRadius / 100f,
                    BlurMaskFilter.Blur.NORMAL
                )
                alpha = 150
            }
            canvas.drawText(text, centreX, baselineY, bloom)
            val wide = Paint(bloom).apply {
                maskFilter = BlurMaskFilter(paint.textSize * finish.glowRadius / 34f, BlurMaskFilter.Blur.NORMAL)
                alpha = 90
            }
            canvas.drawText(text, centreX, baselineY, wide)
        }

        // The metal: a vertical ramp from shadow through base to highlight and back,
        // which is what reads as a polished surface rather than as coloured paint.
        paint.shader = metalShader(centreX, baselineY, paint.textSize, finish)
        canvas.drawText(text, centreX, baselineY, paint)

        // A single specular line across the cap height, clipped to the glyphs, so the
        // letters look struck in metal instead of filled with a gradient.
        val specular = Paint(paint).apply {
            shader = highlightShader(centreX, baselineY, paint.textSize, finish)
            style = Paint.Style.FILL
            alpha = 210
        }
        canvas.drawText(text, centreX, baselineY, specular)
    }

    /** Render straight to a bitmap, for the studio preview and for wallpaper pinning. */
    fun render(width: Int, height: Int, design: NameDesign): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(backdrop(design.finish))
        draw(canvas, width, height, design)
        return bitmap
    }

    /** The flat colour a finish sits on when there is no photograph behind it. */
    fun backdrop(finish: NameFinish): Int = when (finish) {
        NameFinish.IVORY -> 0xFF14120E.toInt()
        NameFinish.MINT, NameFinish.TURQUOISE, NameFinish.EMERALD -> 0xFF06120F.toInt()
        NameFinish.BLUSH, NameFinish.CORAL, NameFinish.RUBY -> 0xFF12060910.toInt() or 0
        NameFinish.LILAC -> 0xFF0C0913.toInt()
        NameFinish.SAPPHIRE, NameFinish.PLATINUM -> 0xFF07090F.toInt()
        else -> 0xFF08080A.toInt()
    }

    private fun textPaint(design: NameDesign, width: Int, height: Int): Paint {
        val script = design.script
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = script.typeface(bold = design.uppercase, italic = script.cursive)
            color = Color.WHITE
        }
        // Fit the name to the screen: measure at a reference size once, then scale.
        paint.textSize = 100f
        val measured = paint.measureText(design.displayText())
        val target = width * MARGIN_FRACTION
        val fitted = (100f * target / max(1f, measured)).coerceAtMost(height * 0.30f)
        // Long names should not overflow the plaque, so the fit wins over a floor.
        paint.textSize = fitted.coerceAtLeast(width * 0.02f)
        return paint
    }

    private fun metalShader(centreX: Float, baseline: Float, size: Float, finish: NameFinish): Shader {
        val top = baseline - size * 0.86f
        val bottom = baseline + size * 0.24f
        return LinearGradient(
            centreX, top, centreX, bottom,
            intArrayOf(finish.shadow, finish.base, finish.highlight, finish.base, finish.shadow),
            floatArrayOf(0f, 0.28f, 0.5f, 0.74f, 1f),
            Shader.TileMode.CLAMP
        )
    }

    private fun highlightShader(centreX: Float, baseline: Float, size: Float, finish: NameFinish): Shader {
        val top = baseline - size * 0.72f
        return LinearGradient(
            centreX, top, centreX, top + size * 0.34f,
            intArrayOf(Color.TRANSPARENT, finish.highlight, Color.TRANSPARENT),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
    }

    /**
     * A soft engraved plate behind the type: darker than the ground, with a hairline
     * of the finish's own light along its top edge. Optional, because some grounds
     * are already composed and a plate would only fight them.
     */
    private fun drawPlaque(canvas: Canvas, width: Int, height: Int, anchor: Float, finish: NameFinish) {
        val bandHeight = height * 0.20f
        val top = (height * anchor - bandHeight * 0.62f).coerceIn(0f, height - bandHeight)
        val rect = RectF(0f, top, width.toFloat(), top + bandHeight)
        val glass = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                width / 2f, rect.centerY(), width * 0.62f,
                intArrayOf(0x55FFFFFF, 0x18FFFFFF, 0x00000000),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(rect, glass)

        val hairline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = finish.glow
            alpha = 96
            strokeWidth = max(1f, height * 0.0012f)
            style = Paint.Style.STROKE
            shader = LinearGradient(
                rect.left, rect.top, rect.right, rect.top,
                intArrayOf(Color.TRANSPARENT, finish.highlight, Color.TRANSPARENT),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawLine(rect.left, rect.top, rect.right, rect.top, hairline)
        canvas.drawLine(rect.left, rect.bottom, rect.right, rect.bottom, hairline)
    }

    private fun Path.rounded(rect: RectF, radius: Float): Path = apply {
        addRoundRect(rect, radius, radius, Path.Direction.CW)
    }

    /**
     * The circuit-etched panel the reference image wears, drawn procedurally.
     *
     * Deliberately deterministic per [seed] (the name), so the same name always
     * gets the same etching — a signature rather than wallpaper noise.
     */
    fun drawCircuitGround(canvas: Canvas, width: Int, height: Int, seed: String, finish: NameFinish) {
        val ink = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = finish.base
            alpha = 46
            strokeWidth = max(1f, width * 0.0016f)
        }
        val glow = Paint(ink).apply { color = finish.glow; alpha = 26 }
        var state = seed.hashCode().toLong() or 1L
        fun next(): Float {
            state = state * 6364136223846793005L + 1442695040888963407L
            return ((state ushr 33) and 0xFFFF).toFloat() / 0xFFFF
        }
        val columns = 6
        val rows = 12
        val cellW = width / columns.toFloat()
        val cellH = height / rows.toFloat()
        repeat(rows) { row ->
            repeat(columns) { column ->
                val left = column * cellW + cellW * 0.12f
                val top = row * cellH + cellH * 0.12f
                val rect = RectF(left, top, left + cellW * 0.76f, top + cellH * 0.70f)
                if (next() > 0.42f) {
                    canvas.drawPath(Path().rounded(rect, cellH * 0.12f), if (next() > 0.6f) glow else ink)
                    val trace = Path().apply {
                        moveTo(rect.left, rect.centerY())
                        lineTo(rect.left - cellW * 0.1f, rect.centerY())
                    }
                    canvas.drawPath(trace, ink)
                }
                if (next() > 0.72f) {
                    canvas.drawCircle(rect.right, rect.top, cellH * 0.06f, ink)
                }
            }
        }
        // A faint warm wash from above, so the panel has light rather than being flat.
        canvas.drawRect(
            0f, 0f, width.toFloat(), height.toFloat(),
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    intArrayOf(0x00000000, 0x00000000, 0x66000000),
                    null, Shader.TileMode.CLAMP
                )
            }
        )
        // A soft inner shadow at the corners: the panel is inset, not printed on top.
        val corners = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                width / 2f, height * 0.46f, max(width, height) * 0.72f,
                intArrayOf(0x00000000, 0x00000000, 0x88000000),
                floatArrayOf(0f, 0.6f, 1f), Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), corners)
    }

    /**
     * Size hint for a scene: how much of the screen height the name may take.
     * Kept public so the studio can reserve room without measuring text.
     */
    fun reservedBandFraction(): Float = 0.20f

    /** Deterministic twinkle offset for a given finish, used by the animated preview. */
    fun shimmerAlpha(finish: NameFinish, phase: Float): Int {
        val wave = (sin((phase * 2 * Math.PI).toDouble()) + 1.0) / 2.0
        return (min(255.0, 40.0 + wave * 150.0 * (finish.glowRadius / 26f))).toInt()
    }
}
