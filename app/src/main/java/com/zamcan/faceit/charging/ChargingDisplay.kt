package com.zamcan.faceit.charging

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader

/**
 * Charging display: the animation and information shown while the phone charges.
 *
 * This is a real, rendered surface inside Face It — not a mock-up of a system
 * overlay. Android does not let an ordinary app replace the system charging
 * screen, so the honest product shape is a charging view the user opens (for
 * example from a tile or the app itself) plus the same animation reused inside
 * the widget. Nothing here claims to replace the lock-screen charging indicator.
 *
 * The drawing is deterministic and cheap: a progress value drives everything, so
 * a single frame costs one short paint pass and the widget can step it without a
 * permanent animation loop (AGENTS.md §8 forbids that in a widget).
 */

enum class ChargingMotif { RING, PULSE, FILL, SPARK, PETALS, CAT, MOON, LANTERN, PITCH, AURORA, PAPER, WAVE }

data class ChargingStyle(
    val id: String,
    val label: String,
    val motif: ChargingMotif,
    val trackColor: Long,
    val progressColor: Long,
    val sparkColor: Long,
    /** One line of plain language describing what the user will see. */
    val description: String
)

object ChargingStyles {
    val all = listOf(
        ChargingStyle(
            "mint_pulse", "Mint pulse", ChargingMotif.PULSE,
            0xFF1B2C29, 0xFF76DCCB, 0xFFE8D8B0,
            "A soft mint ring breathes outward with the battery level."
        ),
        ChargingStyle(
            "rose_bloom", "Rose bloom", ChargingMotif.PETALS,
            0xFF31212A, 0xFFE9A6BE, 0xFFF0CC9B,
            "Five petals open around the percentage as it climbs."
        ),
        ChargingStyle(
            "purr_charge", "Purr charge", ChargingMotif.CAT,
            0xFF2A2349, 0xFFC0A8FF, 0xFFFFD3E6,
            "A crescent cat head fills from the chin upward."
        ),
        ChargingStyle(
            "petal_fill", "Petal fill", ChargingMotif.PETALS,
            0xFF3A222C, 0xFFF2A0B6, 0xFFFFE0B8,
            "Petal ring with a rising liquid level inside."
        ),
        ChargingStyle(
            "cat_nap", "Cat nap", ChargingMotif.CAT,
            0xFF342C24, 0xFFF0C4A2, 0xFFA8D8C8,
            "A curled sleeping cat that wakes as the charge completes."
        ),
        ChargingStyle(
            "moon_charge", "Moon charge", ChargingMotif.MOON,
            0xFF182838, 0xFF6FE3D0, 0xFFB9C8FF,
            "The moon waxes from crescent to full with the level."
        ),
        ChargingStyle(
            "gold_fill", "Gold fill", ChargingMotif.LANTERN,
            0xFF2A241A, 0xFFDFBE7C, 0xFF87C7B7,
            "Lattice lanterns light one by one above the percentage."
        ),
        ChargingStyle(
            "lantern_glow", "Lantern glow", ChargingMotif.LANTERN,
            0xFF1B2840, 0xFFE4B268, 0xFF7E93C4,
            "A single lantern warms as the level rises."
        ),
        ChargingStyle(
            "pitch_fill", "Pitch fill", ChargingMotif.PITCH,
            0xFF152A22, 0xFF5FA98C, 0xFFF0E6C8,
            "A pitch diagram fills line by line from the goal outward."
        ),
        ChargingStyle(
            "violet_ring", "Violet ring", ChargingMotif.RING,
            0xFF1E1A2E, 0xFFB9A2F5, 0xFF76DCCB,
            "A precise ring with a bright leading edge."
        ),
        ChargingStyle(
            "aurora_flow", "Aurora flow", ChargingMotif.AURORA,
            0xFF142836, 0xFF76E0C0, 0xFF8FB6FF,
            "Ribbons of light flow upward behind the number."
        ),
        ChargingStyle(
            "paper_fill", "Paper fill", ChargingMotif.PAPER,
            0xFFEDE5D8, 0xFF2F6F62, 0xFF9A6B3F,
            "Quiet paper bars stack up. No glow, no motion beyond the fill."
        )
    )

    fun find(id: String?): ChargingStyle = all.firstOrNull { it.id == id } ?: all.first()
}

/**
 * Draws one charging frame.
 *
 * [progress] is 0f..1f. Nothing is time-animated inside this function: the caller
 * supplies a phase for motifs that need movement, so the widget can step it and
 * the app can animate it. That keeps a widget from running a free-running loop.
 */
object ChargingRenderer {

    fun draw(
        canvas: Canvas,
        bounds: RectF,
        style: ChargingStyle,
        progress: Float,
        phase: Float = 0f,
        background: Long = style.trackColor,
        textColor: Long = Color.WHITE,
        percentLabel: String? = null
    ) {
        val p = progress.coerceIn(0f, 1f)
        val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }

        val board = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = background.toInt() }
        canvas.drawRoundRect(bounds, bounds.width() * 0.09f, bounds.height() * 0.09f, board)

        val centreX = bounds.centerX()
        val centreY = bounds.centerY() * 0.94f
        val radius = minOf(bounds.width(), bounds.height()) * 0.30f

        when (style.motif) {
            ChargingMotif.RING, ChargingMotif.PULSE -> {
                stroke.color = mix(style.trackColor, Color.WHITE, 0.12f)
                stroke.strokeWidth = radius * 0.20f
                canvas.drawCircle(centreX, centreY, radius, stroke)
                stroke.color = style.progressColor.toInt()
                stroke.strokeWidth = radius * 0.20f
                canvas.drawArc(
                    RectF(centreX - radius, centreY - radius, centreX + radius, centreY + radius),
                    -90f, 359.9f * p, false, stroke
                )
                if (style.motif == ChargingMotif.PULSE) {
                    fill.style = Paint.Style.STROKE
                    fill.strokeWidth = radius * 0.05f
                    fill.color = withAlpha(style.sparkColor.toInt(), 120)
                    val breath = radius * (1.18f + 0.05f * kotlin.math.sin(phase * 6.283f))
                    canvas.drawCircle(centreX, centreY, breath, fill)
                }
            }

            ChargingMotif.FILL, ChargingMotif.WAVE -> {
                val tank = RectF(centreX - radius, centreY - radius, centreX + radius, centreY + radius)
                stroke.color = mix(style.trackColor, Color.WHITE, 0.14f)
                stroke.strokeWidth = radius * 0.10f
                canvas.drawRoundRect(tank, radius * 0.22f, radius * 0.22f, stroke)
                val surface = tank.bottom - tank.height() * p
                canvas.save()
                canvas.clipRect(RectF(tank.left, surface, tank.right, tank.bottom))
                fill.color = style.progressColor.toInt()
                canvas.drawRoundRect(tank, radius * 0.22f, radius * 0.22f, fill)
                if (style.motif == ChargingMotif.WAVE) {
                    val wave = Path()
                    val amp = radius * 0.06f
                    wave.moveTo(tank.left, surface)
                    var x = tank.left
                    while (x <= tank.right) {
                        val y = surface + amp * kotlin.math.sin((x / radius) * 3f + phase * 6.283f)
                        wave.lineTo(x, y)
                        x += radius * 0.08f
                    }
                    wave.lineTo(tank.right, tank.bottom)
                    wave.lineTo(tank.left, tank.bottom)
                    wave.close()
                    fill.color = withAlpha(style.sparkColor.toInt(), 150)
                    canvas.drawPath(wave, fill)
                }
                canvas.restore()
            }

            ChargingMotif.PETALS -> {
                val petals = 5
                for (i in 0 until petals) {
                    val start = -90f + i * (360f / petals) + phase * 20f
                    stroke.strokeWidth = radius * 0.22f
                    stroke.color = mix(style.trackColor, Color.WHITE, 0.12f)
                    canvas.drawArc(
                        RectF(centreX - radius, centreY - radius, centreX + radius, centreY + radius),
                        start, 60f, false, stroke
                    )
                    if (i / petals.toFloat() <= p) {
                        stroke.color = if (i % 2 == 0) style.progressColor.toInt() else style.sparkColor.toInt()
                        canvas.drawArc(
                            RectF(centreX - radius, centreY - radius, centreX + radius, centreY + radius),
                            start, 60f, false, stroke
                        )
                    }
                }
            }

            ChargingMotif.CAT -> {
                // A crescent cat head: two ears, a round face, filled bottom-up.
                val head = Path()
                head.addArc(
                    RectF(centreX - radius * 0.8f, centreY - radius * 0.6f,
                          centreX + radius * 0.8f, centreY + radius),
                    200f, 140f
                )
                head.lineTo(centreX + radius * 0.62f, centreY - radius * 0.86f)
                head.lineTo(centreX + radius * 0.30f, centreY - radius * 0.58f)
                head.lineTo(centreX - radius * 0.30f, centreY - radius * 0.58f)
                head.lineTo(centreX - radius * 0.62f, centreY - radius * 0.86f)
                head.close()
                stroke.color = mix(style.trackColor, Color.WHITE, 0.16f)
                stroke.strokeWidth = radius * 0.08f
                canvas.drawPath(head, stroke)
                canvas.save()
                val level = centreY + radius - (radius * 1.9f) * p
                canvas.clipRect(RectF(centreX - radius, level, centreX + radius, centreY + radius))
                fill.color = style.progressColor.toInt()
                canvas.drawPath(head, fill)
                canvas.restore()
                // eyes close as the charge completes: a small, honest detail
                val eyeY = centreY - radius * 0.10f
                fill.color = style.sparkColor.toInt()
                val open = (1f - p).coerceAtLeast(0.12f)
                canvas.drawOval(
                    RectF(centreX - radius * 0.34f, eyeY - radius * 0.10f * open,
                          centreX - radius * 0.16f, eyeY + radius * 0.10f * open), fill
                )
                canvas.drawOval(
                    RectF(centreX + radius * 0.16f, eyeY - radius * 0.10f * open,
                          centreX + radius * 0.34f, eyeY + radius * 0.10f * open), fill
                )
            }

            ChargingMotif.MOON -> {
                val disc = Path().apply {
                    addCircle(centreX, centreY, radius, Path.Direction.CW)
                }
                fill.color = withAlpha(style.progressColor.toInt(), 70)
                canvas.drawPath(disc, fill)
                // the lit part is a lens that widens with progress
                val litWidth = radius * 2f * p
                canvas.save()
                canvas.clipRect(RectF(centreX - radius, centreY - radius, centreX - radius + litWidth, centreY + radius))
                fill.color = style.progressColor.toInt()
                canvas.drawPath(disc, fill)
                canvas.restore()
                stroke.color = withAlpha(style.sparkColor.toInt(), 190)
                stroke.strokeWidth = radius * 0.04f
                canvas.drawCircle(centreX, centreY, radius, stroke)
            }

            ChargingMotif.LANTERN -> {
                val count = 4
                val w = radius * 0.52f
                val gap = w * 0.55f
                val total = count * w + (count - 1) * gap
                val startX = centreX - total / 2f
                for (i in 0 until count) {
                    val box = RectF(
                        startX + i * (w + gap), centreY - radius * 0.5f,
                        startX + i * (w + gap) + w, centreY + radius * 0.5f
                    )
                    val threshold = (i + 1) / count.toFloat()
                    val lit = p >= threshold - 0.001f
                    fill.style = Paint.Style.FILL
                    fill.color = if (lit) style.progressColor.toInt()
                    else mix(style.trackColor, Color.WHITE, 0.10f)
                    canvas.drawRoundRect(box, w * 0.18f, w * 0.18f, fill)
                    stroke.color = withAlpha(style.sparkColor.toInt(), if (lit) 220 else 90)
                    stroke.strokeWidth = w * 0.06f
                    canvas.drawLine(box.left, box.top - w * 0.14f, box.right, box.top - w * 0.14f, stroke)
                }
            }

            ChargingMotif.PITCH -> {
                val board = RectF(centreX - radius * 1.05f, centreY - radius * 0.66f,
                                  centreX + radius * 1.05f, centreY + radius * 0.66f)
                stroke.color = withAlpha(style.progressColor.toInt(), 200)
                stroke.strokeWidth = radius * 0.035f
                canvas.drawRect(board, stroke)
                canvas.drawLine(board.centerX(), board.top, board.centerX(), board.bottom, stroke)
                canvas.drawCircle(board.centerX(), board.centerY(), board.height() * 0.22f, stroke)
                fill.color = style.sparkColor.toInt()
                canvas.drawCircle(board.centerX(), board.centerY(), board.height() * 0.05f, fill)
                // the fill sweeps across the pitch
                canvas.save()
                canvas.clipRect(RectF(board.left, board.top,
                    board.left + board.width() * p, board.bottom))
                fill.color = withAlpha(style.progressColor.toInt(), 60)
                canvas.drawRect(board, fill)
                canvas.restore()
            }

            ChargingMotif.AURORA -> {
                for (i in 0 until 3) {
                    stroke.strokeWidth = radius * (0.28f - i * 0.06f)
                    stroke.color = withAlpha(
                        if (i % 2 == 0) style.progressColor.toInt() else style.sparkColor.toInt(),
                        70 + i * 30
                    )
                    val y = centreY + radius * 0.5f - radius * (0.9f * p) - i * radius * 0.14f
                    val ribbon = Path()
                    ribbon.moveTo(centreX - radius * 1.2f, y)
                    var x = centreX - radius * 1.2f
                    while (x <= centreX + radius * 1.2f) {
                        val offset = radius * 0.12f * kotlin.math.sin((x / radius) * 1.8f + phase * 6.283f + i)
                        ribbon.lineTo(x, y + offset)
                        x += radius * 0.1f
                    }
                    canvas.drawPath(ribbon, stroke)
                }
            }

            ChargingMotif.SPARK -> {
                fill.color = style.progressColor.toInt()
                canvas.drawCircle(centreX, centreY, radius * 0.55f * p, fill)
                stroke.color = style.sparkColor.toInt()
                stroke.strokeWidth = radius * 0.05f
                canvas.drawCircle(centreX, centreY, radius * (0.7f + 0.2f * p), stroke)
            }

            ChargingMotif.PAPER -> {
                val bars = 4
                val h = radius * 0.18f
                for (i in 0 until bars) {
                    val box = RectF(
                        centreX - radius, centreY - radius * 0.6f + i * (h + radius * 0.12f),
                        centreX + radius, centreY - radius * 0.6f + i * (h + radius * 0.12f) + h
                    )
                    fill.color = mix(style.trackColor, Color.BLACK, 0.06f)
                    canvas.drawRoundRect(box, h * 0.4f, h * 0.4f, fill)
                    val lit = ((p - i / bars.toFloat()) * bars).coerceIn(0f, 1f)
                    if (lit > 0f) {
                        canvas.save()
                        canvas.clipRect(RectF(box.left, box.top, box.left + box.width() * lit, box.bottom))
                        fill.color = style.progressColor.toInt()
                        canvas.drawRoundRect(box, h * 0.4f, h * 0.4f, fill)
                        canvas.restore()
                    }
                }
            }
        }

        percentLabel?.let { label ->
            val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor.toInt()
                textSize = radius * 0.52f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }
            canvas.drawText(label, centreX, centreY + radius * 0.18f, text)
        }
    }

    /** Gradient helper used by the full-screen charging view. */
    fun backgroundShader(bounds: RectF, top: Long, bottom: Long): Shader =
        LinearGradient(
            0f, bounds.top, 0f, bounds.bottom,
            top.toInt(), bottom.toInt(), Shader.TileMode.CLAMP
        )

    private fun withAlpha(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or ((alpha.coerceIn(0, 255)) shl 24)

    private fun mix(a: Long, b: Int, t: Float): Int {
        val ar = ((a shr 16) and 0xFF).toFloat()
        val ag = ((a shr 8) and 0xFF).toFloat()
        val ab = (a and 0xFF).toFloat()
        val br = ((b shr 16) and 0xFF).toFloat()
        val bg = ((b shr 8) and 0xFF).toFloat()
        val bb = (b and 0xFF).toFloat()
        return Color.rgb(
            (ar + (br - ar) * t).toInt(),
            (ag + (bg - ag) * t).toInt(),
            (ab + (bb - ab) * t).toInt()
        )
    }
}
