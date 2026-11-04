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

enum class ChargingMotif {
    RING, PULSE, FILL, SPARK, PETALS, CAT, MOON, LANTERN, PITCH, AURORA, PAPER, WAVE,
    HEART, BUTTERFLY, STARFIELD, ORBIT, GARDEN,
    CROWN, JEWEL, LEAF, COLUMN, RUNWAY, TIDE, PORTAL, SHATTER,
    STAIR, HOURGLASS, TUMBLER, MANDALA, CHEVRON, COIL, PRISM, BEACON
}

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
        ),
        ChargingStyle(
            "crown_gold", "Crown gold", ChargingMotif.CROWN,
            0xFF241C10, 0xFFDFBE7C, 0xFF8FC8A8,
            "A crown fills point by point, then its band lights."
        ),
        ChargingStyle(
            "jewel_charge", "Jewel charge", ChargingMotif.JEWEL,
            0xFF1A1426, 0xFFC9A2F0, 0xFF8FE0CE,
            "Facets kindle one after another around a cut stone."
        ),
        ChargingStyle(
            "leaf_fill", "Leaf fill", ChargingMotif.LEAF,
            0xFF15251C, 0xFF8FD8A8, 0xFFF0C88A,
            "A leaf veins from the stem outward as the level rises."
        ),
        ChargingStyle(
            "column_charge", "Column charge", ChargingMotif.COLUMN,
            0xFF232018, 0xFFD8C08C, 0xFF8FB8C8,
            "Fluting lights from the base up, like a lit colonnade."
        ),
        ChargingStyle(
            "runway", "Runway", ChargingMotif.RUNWAY,
            0xFF15171C, 0xFF9FB4D0, 0xFFFFC86A,
            "Runway edge lights march toward the horizon."
        ),
        ChargingStyle(
            "tide", "Tide", ChargingMotif.TIDE,
            0xFF0A1F28, 0xFF5FD0D8, 0xFFB8DCC0,
            "A tide line rises with a low surf edge."
        ),
        ChargingStyle(
            "portal", "Portal", ChargingMotif.PORTAL,
            0xFF161222, 0xFFA97BFF, 0xFF5FE0C8,
            "A ring portal widens inward as the charge fills."
        ),
        ChargingStyle(
            "shatter", "Shatter", ChargingMotif.SHATTER,
            0xFF1A1A20, 0xFFCFD6E6, 0xFF9FA8BF,
            "Cracked facets fill from the centre out."
        ),
        ChargingStyle(
            "silk_rose", "Silk rose", ChargingMotif.PULSE,
            0xFF2E2026, 0xFFF0A8C0, 0xFFFFD9A8,
            "A rose satin sheen circling a fine ring."
        ),
        ChargingStyle(
            "aurora_green", "Aurora green", ChargingMotif.AURORA,
            0xFF0E2018, 0xFF8FE0B8, 0xFF7FB8F0,
            "Green ribbons breathing behind the number."
        ),
        ChargingStyle(
            "paper_gold", "Paper gold", ChargingMotif.PAPER,
            0xFFF4EEE0, 0xFF8A6A3F, 0xFF2F6F62,
            "Cream paper bars with brown ink and a gold rule."
        ),
        ChargingStyle(
            "gem_sapphire", "Gem sapphire", ChargingMotif.JEWEL,
            0xFF0E1730, 0xFF6FA8F0, 0xFFCFD8A0,
            "Deep sapphire facets with a cool highlight."
        ),
        ChargingStyle(
            "pearl_ring", "Pearl ring", ChargingMotif.RING,
            0xFF2A2A30, 0xFFE8E4EC, 0xFFC8B8A8,
            "A fine pearl ring with a slow leading edge."
        ),
        ChargingStyle(
            "coral_pulse", "Coral pulse", ChargingMotif.PULSE,
            0xFF2B1D1B, 0xFFF08A78, 0xFFF0C8A0,
            "Warm coral breathing outward."
        ),
        ChargingStyle(
            "pine_fill", "Pine fill", ChargingMotif.FILL,
            0xFF14251C, 0xFF6FCB9A, 0xFFD8E0A8,
            "A pine-glass tank filling from the base."
        ),
        ChargingStyle(
            "ivory_bars", "Ivory bars", ChargingMotif.PAPER,
            0xFFF5F1E8, 0xFF5A544A, 0xFF2F6F62,
            "Ivory paper bars with a teal rule."
        ),
        ChargingStyle(
            "midnight_orbit", "Midnight orbit", ChargingMotif.ORBIT,
            0xFF0F1626, 0xFF7F9FD8, 0xFFCFD8A0,
            "A pale moon crossing a deep blue orbit."
        ),
        ChargingStyle(
            "sakura_petals", "Sakura petals", ChargingMotif.PETALS,
            0xFF301E24, 0xFFF2B8C8, 0xFFF0D8A8,
            "Five sakura petals, one per fifth of the charge."
        ),
        ChargingStyle(
            "rose_halo", "Rose halo", ChargingMotif.HEART,
            0xFF2E1D25, 0xFFF0A8C0, 0xFFFFD9A8,
            "A heart fills beat by beat, with a soft halo behind it."
        ),
        ChargingStyle(
            "pearl_bloom", "Pearl bloom", ChargingMotif.HEART,
            0xFF3A2A2E, 0xFFE8BFCE, 0xFFC6E4DA,
            "Pearl sheen on a slowly filling heart. Quiet, not sugary."
        ),
        ChargingStyle(
            "flutter_fill", "Flutter fill", ChargingMotif.BUTTERFLY,
            0xFF232034, 0xFFC9A7E8, 0xFF9BE0C8,
            "Wings widen as the level climbs, then settle at full."
        ),
        ChargingStyle(
            "glyph_petals", "Glyph petals", ChargingMotif.GARDEN,
            0xFF1F2A24, 0xFF8FD3B5, 0xFFF2C48E,
            "Four petals, one per quarter. Leaves appear as you pass each."
        ),
        ChargingStyle(
            "catnap_curl", "Catnap curl", ChargingMotif.CAT,
            0xFF2B2420, 0xFFE6B892, 0xFFA9D6C6,
            "A curled kitten, tail tightening as the battery fills."
        ),
        ChargingStyle(
            "gem_heart", "Gem heart", ChargingMotif.HEART,
            0xFF241A2C, 0xFFD9A6E8, 0xFF8FC8E8,
            "Faceted heart with a violet interior light."
        ),
        ChargingStyle(
            "paw_steps", "Paw steps", ChargingMotif.GARDEN,
            0xFF26221C, 0xFFDCC08C, 0xFF9AD2BE,
            "Paw prints appear along a path, one per ten percent."
        ),
        ChargingStyle(
            "starfield", "Starfield", ChargingMotif.STARFIELD,
            0xFF0E1526, 0xFF8FB6FF, 0xFFE9D8A8,
            "Stars kindle one by one against a deep sky."
        ),
        ChargingStyle(
            "orbit_map", "Orbit map", ChargingMotif.ORBIT,
            0xFF141B2A, 0xFF7FD4C8, 0xFFD9A9E0,
            "A moon orbits a planet; the ring is the progress."
        ),
        ChargingStyle(
            "paper_heart", "Paper heart", ChargingMotif.HEART,
            0xFFF2EAE0, 0xFFB8607A, 0xFF3C6E62,
            "Folded paper heart on cream. The one light charging motif."
        ),
        ChargingStyle(
            "harvest_gold", "Harvest gold", ChargingMotif.GARDEN,
            0xFF2B2418, 0xFFDCB878, 0xFF8FC8A8,
            "A wheat and leaf motif warming from the base upward."
        ),
        ChargingStyle(
            "silk_pulse", "Silk pulse", ChargingMotif.PULSE,
            0xFF2E2430, 0xFFCBA8D8, 0xFFF0C8B8,
            "A slow satin sheen travelling around a thin ring."
        ),
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
        textColor: Long = 0xFFFFFFFF,
        percentLabel: String? = null,
        ChargingStyle(
            "stair_gold", "Stair gold", ChargingMotif.STAIR,
            0xFF241C10, 0xFFDFBE7C, 0xFF8FC8A8,
            "Steps light one by one toward the top."
        ),
        ChargingStyle(
            "hour_charge", "Hour charge", ChargingMotif.HOURGLASS,
            0xFF1C1720, 0xFFC9A2F0, 0xFF9FE0C8,
            "Sand falls into the lower half as the charge grows."
        ),
        ChargingStyle(
            "tumbler", "Tumbler", ChargingMotif.TUMBLER,
            0xFF14202A, 0xFF5FC8D8, 0xFFF0D8A0,
            "A glass fills evenly with a lit rim."
        ),
        ChargingStyle(
            "mandala", "Mandala", ChargingMotif.MANDALA,
            0xFF1A1420, 0xFFD8A8F0, 0xFFF0C88A,
            "Rings and spokes draw themselves in order."
        ),
        ChargingStyle(
            "chevron", "Chevron", ChargingMotif.CHEVRON,
            0xFF161A1E, 0xFF9FC8E8, 0xFFF0C080,
            "Chevrons march upward with the level."
        ),
        ChargingStyle(
            "coil", "Coil", ChargingMotif.COIL,
            0xFF1B1618, 0xFFE0A8B8, 0xFFA8D8C8,
            "A spring tightens as the charge climbs."
        ),
        ChargingStyle(
            "prism", "Prism", ChargingMotif.PRISM,
            0xFF12161C, 0xFFA8C0E8, 0xFFF0C8A0,
            "White light splits through the prism at every step."
        ),
        ChargingStyle(
            "beacon", "Beacon", ChargingMotif.BEACON,
            0xFF0E1418, 0xFF8FD8D0, 0xFFF0C88A,
            "Two beams sweep from a lit base."
        ),
        ChargingStyle(
            "silk_ivory", "Silk ivory", ChargingMotif.PULSE,
            0xFFF2EADF, 0xFF8A6A3F, 0xFF2F6F62,
            "Ivory silk with a brown ink ring."
        ),
        ChargingStyle(
            "graphite_ring", "Graphite ring", ChargingMotif.RING,
            0xFF1E2024, 0xFFD8DCE4, 0xFF9FA8BF,
            "Graphite with a single bright ring."
        ),
        ChargingStyle(
            "jade_fill", "Jade fill", ChargingMotif.FILL,
            0xFF0E201A, 0xFF6FD8B4, 0xFFE0E8A8,
            "Jade liquid rising behind the number."
        ),
        ChargingStyle(
            "marigold", "Marigold", ChargingMotif.PETALS,
            0xFF2A1C0E, 0xFFF0B860, 0xFFF0E0A8,
            "Marigold petals, warm and even."
        ),
        ChargingStyle(
            "pearl_stair", "Pearl stair", ChargingMotif.STAIR,
            0xFFF2EFEA, 0xFF7A6A5A, 0xFF3F6B60,
            "Pearl steps with a stone ink."
        ),
        ChargingStyle(
            "orchid_hour", "Orchid hour", ChargingMotif.HOURGLASS,
            0xFF2A1830, 0xFFE0A8F0, 0xFF9FE0C8,
            "Orchid sand in a dark glass."
        ),
        ChargingStyle(
            "copper_coil", "Copper coil", ChargingMotif.COIL,
            0xFF2A1A12, 0xFFE8A878, 0xFF9FC8B8,
            "Burnished copper winding tight."
        ),
        ChargingStyle(
            "ice_prism", "Ice prism", ChargingMotif.PRISM,
            0xFF101A22, 0xFF9FD8F0, 0xFFF0C8A0,
            "Cold light through a clear prism."
        )
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



            ChargingMotif.STAIR -> {
                val steps = 5
                val stepW = (radius * 1.7f) / steps
                val stepH = (radius * 1.5f) / steps
                for (i in 0 until steps) {
                    val lit = p >= (i + 1) / steps.toFloat()
                    val left = centreX - radius * 0.85f + i * stepW
                    val topY = centreY + radius * 0.75f - (i + 1) * stepH
                    fill.color = if (lit) style.progressColor.toInt()
                    else withAlpha(style.sparkColor.toInt(), 55)
                    canvas.drawRect(RectF(left, topY, left + stepW * 0.92f, centreY + radius * 0.78f), fill)
                }
                stroke.color = withAlpha(style.trackColor.toInt(), 140)
                stroke.strokeWidth = radius * 0.03f
                canvas.drawLine(
                    centreX - radius * 0.9f, centreY + radius * 0.82f,
                    centreX + radius * 0.9f, centreY + radius * 0.82f, stroke
                )
            }

            ChargingMotif.HOURGLASS -> {
                val glass = Path().apply {
                    moveTo(centreX - radius * 0.55f, centreY - radius * 0.80f)
                    lineTo(centreX + radius * 0.55f, centreY - radius * 0.80f)
                    lineTo(centreX + radius * 0.10f, centreY)
                    lineTo(centreX + radius * 0.55f, centreY + radius * 0.80f)
                    lineTo(centreX - radius * 0.55f, centreY + radius * 0.80f)
                    lineTo(centreX - radius * 0.10f, centreY)
                    close()
                }
                stroke.color = mix(style.trackColor, Color.WHITE, 0.14f)
                stroke.strokeWidth = radius * 0.05f
                canvas.drawPath(glass, stroke)
                canvas.save()
                canvas.clipPath(glass)
                val drained = (p * 2f).coerceAtMost(1f)
                fill.color = withAlpha(style.progressColor.toInt(), 210)
                val lowerTop = centreY + radius * 0.80f - radius * 0.80f * drained
                canvas.drawRect(
                    RectF(centreX - radius, lowerTop, centreX + radius, centreY + radius * 0.82f), fill
                )
                fill.color = withAlpha(style.sparkColor.toInt(), 160)
                val upperBottom = centreY - radius * 0.80f + radius * 0.80f * drained
                canvas.drawRect(
                    RectF(centreX - radius, centreY - radius * 0.82f, centreX + radius, upperBottom), fill
                )
                canvas.restore()
            }

            ChargingMotif.TUMBLER -> {
                val glass = RectF(
                    centreX - radius * 0.52f, centreY - radius * 0.72f,
                    centreX + radius * 0.52f, centreY + radius * 0.72f
                )
                stroke.color = mix(style.trackColor, Color.WHITE, 0.16f)
                stroke.strokeWidth = radius * 0.05f
                canvas.drawRoundRect(glass, radius * 0.10f, radius * 0.10f, stroke)
                val level = glass.bottom - glass.height() * p
                fill.color = withAlpha(style.progressColor.toInt(), 205)
                canvas.drawRect(RectF(glass.left, level, glass.right, glass.bottom), fill)
                fill.color = withAlpha(style.sparkColor.toInt(), 190)
                canvas.drawRect(RectF(glass.left, level, glass.right, level + radius * 0.055f), fill)
            }

            ChargingMotif.MANDALA -> {
                val rings = 4
                val spokes = 12
                for (i in 0 until rings) {
                    val r = radius * (0.30f + i * 0.22f)
                    stroke.color = if (p >= (i + 1) / rings.toFloat()) style.progressColor.toInt()
                    else withAlpha(style.sparkColor.toInt(), 60)
                    stroke.strokeWidth = radius * 0.03f
                    canvas.drawCircle(centreX, centreY, r, stroke)
                }
                for (i in 0 until spokes) {
                    val a = Math.toRadians(i * (360.0 / spokes))
                    val reach = radius * (0.28f + 0.62f * p)
                    stroke.color = withAlpha(style.sparkColor.toInt(), 150)
                    stroke.strokeWidth = radius * 0.02f
                    canvas.drawLine(
                        centreX + (radius * 0.18f * kotlin.math.cos(a)).toFloat(),
                        centreY + (radius * 0.18f * kotlin.math.sin(a)).toFloat(),
                        centreX + (reach * kotlin.math.cos(a)).toFloat(),
                        centreY + (reach * kotlin.math.sin(a)).toFloat(),
                        stroke
                    )
                }
                fill.color = withAlpha(style.progressColor.toInt(), 200)
                canvas.drawCircle(centreX, centreY, radius * 0.12f, fill)
            }

            ChargingMotif.CHEVRON -> {
                val count = 5
                for (i in 0 until count) {
                    val t = i / count.toFloat()
                    val y = centreY + radius * 0.62f - radius * 1.24f * t
                    val lit = p >= (i + 1) / count.toFloat() - 0.001f
                    val chevron = Path().apply {
                        moveTo(centreX - radius * 0.62f, y + radius * 0.20f)
                        lineTo(centreX, y)
                        lineTo(centreX + radius * 0.62f, y + radius * 0.20f)
                    }
                    stroke.color = if (lit) style.progressColor.toInt()
                    else withAlpha(style.sparkColor.toInt(), 70)
                    stroke.strokeWidth = radius * 0.09f
                    canvas.drawPath(chevron, stroke)
                }
            }

            ChargingMotif.COIL -> {
                val turns = 5
                val coil = Path().apply {
                    moveTo(centreX - radius * 0.62f, centreY + radius * 0.62f)
                    for (i in 0 until turns) {
                        val y = centreY + radius * 0.62f - radius * (1.24f / turns) * (i + 0.5f)
                        cubicTo(
                            centreX - radius * 0.62f, y - radius * 0.14f,
                            centreX + radius * 0.62f, y - radius * 0.14f,
                            centreX + radius * 0.62f, y
                        )
                    }
                }
                stroke.color = withAlpha(style.sparkColor.toInt(), 80)
                stroke.strokeWidth = radius * 0.10f
                canvas.drawPath(coil, stroke)
                canvas.save()
                canvas.clipRect(
                    RectF(
                        centreX - radius, centreY + radius * 0.64f - radius * 1.30f * p,
                        centreX + radius, centreY + radius * 0.68f
                    )
                )
                stroke.color = style.progressColor.toInt()
                canvas.drawPath(coil, stroke)
                canvas.restore()
            }

            ChargingMotif.PRISM -> {
                val prism = Path().apply {
                    moveTo(centreX, centreY - radius * 0.62f)
                    lineTo(centreX + radius * 0.70f, centreY + radius * 0.52f)
                    lineTo(centreX - radius * 0.70f, centreY + radius * 0.52f)
                    close()
                }
                stroke.color = mix(style.trackColor, Color.WHITE, 0.18f)
                stroke.strokeWidth = radius * 0.05f
                canvas.drawPath(prism, stroke)
                canvas.save()
                canvas.clipPath(prism)
                fill.color = withAlpha(style.progressColor.toInt(), 190)
                canvas.drawRect(
                    RectF(
                        centreX - radius, centreY + radius * 0.52f - radius * 1.2f * p,
                        centreX + radius, centreY + radius * 0.54f
                    ),
                    fill
                )
                canvas.restore()
                for (i in 0 until 3) {
                    stroke.color = withAlpha(
                        when (i) {
                            0 -> style.progressColor.toInt()
                            1 -> style.sparkColor.toInt()
                            else -> Color.WHITE
                        },
                        150
                    )
                    stroke.strokeWidth = radius * 0.03f
                    canvas.drawLine(
                        centreX + radius * 0.30f, centreY + radius * 0.10f,
                        centreX + radius * (0.86f + 0.06f * i), centreY + radius * (0.02f + 0.22f * i),
                        stroke
                    )
                }
            }

            ChargingMotif.BEACON -> {
                fill.color = withAlpha(style.trackColor.toInt(), 200)
                canvas.drawCircle(centreX, centreY + radius * 0.52f, radius * 0.30f, fill)
                fill.color = withAlpha(style.progressColor.toInt(), 220)
                canvas.drawCircle(centreX, centreY + radius * 0.52f, radius * 0.22f * (0.4f + 0.6f * p), fill)
                val sweep = 360f * p
                for (side in -1..1) {
                    val a0 = (sweep * side) - 90f
                    stroke.color = withAlpha(style.sparkColor.toInt(), 120)
                    stroke.strokeWidth = radius * 0.10f
                    canvas.drawArc(
                        RectF(
                            centreX - radius * 0.62f, centreY - radius * 0.10f,
                            centreX + radius * 0.62f, centreY + radius * 1.06f
                        ),
                        a0, 22f, true, stroke
                    )
                }
            }

            ChargingMotif.CROWN -> {
                val points = 5
                val crown = Path().apply {
                    moveTo(centreX - radius * 0.78f, centreY + radius * 0.42f)
                    moveTo(centreX - radius * 0.78f, centreY + radius * 0.42f)
                    lineTo(centreX - radius * 0.78f, centreY - radius * 0.22f)
                    lineTo(centreX - radius * 0.38f, centreY + radius * 0.10f)
                    lineTo(centreX, centreY - radius * 0.56f)
                    lineTo(centreX + radius * 0.38f, centreY + radius * 0.10f)
                    lineTo(centreX + radius * 0.78f, centreY - radius * 0.22f)
                    lineTo(centreX + radius * 0.78f, centreY + radius * 0.42f)
                    close()
                }
                stroke.color = mix(style.trackColor, Color.WHITE, 0.16f)
                stroke.strokeWidth = radius * 0.07f
                canvas.drawPath(crown, stroke)
                canvas.save()
                val lit = centreY + radius * 0.44f - radius * 1.05f * p
                canvas.clipRect(RectF(centreX - radius, lit, centreX + radius, centreY + radius * 0.5f))
                fill.color = style.progressColor.toInt()
                canvas.drawPath(crown, fill)
                canvas.restore()
                // three stones on the band, lighting in order
                repeat(points - 2) { i ->
                    val on = p > (i + 1) / points.toFloat()
                    fill.color = if (on) style.sparkColor.toInt()
                    else withAlpha(style.sparkColor.toInt(), 60)
                    canvas.drawCircle(
                        centreX + radius * (-0.40f + i * 0.40f),
                        centreY + radius * 0.26f,
                        radius * 0.07f, fill
                    )
                }
            }

            ChargingMotif.JEWEL -> {
                val facets = 6
                val cx = centreX
                val cy = centreY
                for (i in 0 until facets) {
                    val a0 = -90f + i * (360f / facets)
                    val a1 = a0 + (360f / facets)
                    val lit = p >= (i + 1) / facets.toFloat() - 0.001f
                    val tri = Path().apply {
                        moveTo(cx, cy)
                        lineTo(cx + (radius * kotlin.math.cos(Math.toRadians(a0.toDouble()))).toFloat(),
                               cy + (radius * kotlin.math.sin(Math.toRadians(a0.toDouble()))).toFloat())
                        lineTo(cx + (radius * kotlin.math.cos(Math.toRadians(a1.toDouble()))).toFloat(),
                               cy + (radius * kotlin.math.sin(Math.toRadians(a1.toDouble()))).toFloat())
                        close()
                    }
                    fill.color = if (lit) style.progressColor.toInt()
                    else withAlpha(style.sparkColor.toInt(), 55)
                    canvas.drawPath(tri, fill)
                    stroke.color = withAlpha(style.trackColor.toInt(), 150)
                    stroke.strokeWidth = radius * 0.02f
                    canvas.drawPath(tri, stroke)
                }
                fill.color = withAlpha(style.sparkColor.toInt(), 200)
                canvas.drawCircle(cx, cy, radius * 0.12f, fill)
            }

            ChargingMotif.LEAF -> {
                val leaf = Path().apply {
                    moveTo(centreX, centreY + radius * 0.80f)
                    cubicTo(centreX - radius * 0.95f, centreY + radius * 0.30f,
                            centreX - radius * 0.55f, centreY - radius * 0.85f,
                            centreX, centreY - radius * 0.80f)
                    cubicTo(centreX + radius * 0.55f, centreY - radius * 0.85f,
                            centreX + radius * 0.95f, centreY + radius * 0.30f,
                            centreX, centreY + radius * 0.80f)
                    close()
                }
                stroke.color = mix(style.trackColor, Color.WHITE, 0.16f)
                stroke.strokeWidth = radius * 0.06f
                canvas.drawPath(leaf, stroke)
                canvas.save()
                val level = centreY + radius * 0.84f - radius * 1.68f * p
                canvas.clipRect(RectF(centreX - radius, level, centreX + radius, centreY + radius))
                fill.color = style.progressColor.toInt()
                canvas.drawPath(leaf, fill)
                canvas.restore()
                stroke.color = style.sparkColor.toInt()
                stroke.strokeWidth = radius * 0.045f
                canvas.drawLine(centreX, centreY + radius * 0.70f, centreX, centreY - radius * 0.62f * p, stroke)
                repeat(3) { i ->
                    val y = centreY + radius * (0.30f - i * 0.34f)
                    if (p > (i + 1) / 4f) {
                        canvas.drawLine(centreX - radius * 0.30f, y, centreX, y - radius * 0.12f, stroke)
                        canvas.drawLine(centreX + radius * 0.30f, y, centreX, y - radius * 0.12f, stroke)
                    }
                }
            }

            ChargingMotif.COLUMN -> {
                val columns = 3
                val w = radius * 0.34f
                val gap = w * 0.55f
                val total = columns * w + (columns - 1) * gap
                for (i in 0 until columns) {
                    val left = centreX - total / 2f + i * (w + gap)
                    val body = RectF(left, centreY - radius * 0.70f, left + w, centreY + radius * 0.70f)
                    fill.color = mix(style.trackColor, Color.WHITE, 0.10f)
                    canvas.drawRect(body, fill)
                    canvas.save()
                    val level = body.bottom - body.height() * p
                    canvas.clipRect(RectF(body.left, level, body.right, body.bottom))
                    fill.color = style.progressColor.toInt()
                    canvas.drawRect(body, fill)
                    canvas.restore()
                    stroke.color = withAlpha(style.sparkColor.toInt(), 200)
                    stroke.strokeWidth = w * 0.16f
                    canvas.drawLine(left, body.top, left + w, body.top, stroke)
                    canvas.drawLine(left, body.bottom, left + w, body.bottom, stroke)
                }
            }

            ChargingMotif.RUNWAY -> {
                val vanishY = centreY - radius * 0.20f
                stroke.color = withAlpha(style.sparkColor.toInt(), 70)
                stroke.strokeWidth = radius * 0.03f
                canvas.drawLine(centreX - radius * 1.1f, centreY + radius * 0.95f, centreX, vanishY, stroke)
                canvas.drawLine(centreX + radius * 1.1f, centreY + radius * 0.95f, centreX, vanishY, stroke)
                repeat(6) { i ->
                    val t = (i + 1) / 6f
                    val y = centreY + radius * 0.95f - (radius * 1.15f) * t
                    val half = radius * 1.1f * (1f - t)
                    val lit = t <= p
                    fill.color = if (lit) style.progressColor.toInt()
                    else withAlpha(style.sparkColor.toInt(), 45)
                    canvas.drawCircle(centreX - half, y, radius * 0.05f * (1.2f - t * 0.6f), fill)
                    canvas.drawCircle(centreX + half, y, radius * 0.05f * (1.2f - t * 0.6f), fill)
                }
            }

            ChargingMotif.TIDE -> {
                val tank = RectF(centreX - radius, centreY - radius * 0.7f, centreX + radius, centreY + radius * 0.7f)
                stroke.color = mix(style.trackColor, Color.WHITE, 0.12f)
                stroke.strokeWidth = radius * 0.05f
                canvas.drawRoundRect(tank, radius * 0.2f, radius * 0.2f, stroke)
                val level = tank.bottom - tank.height() * p
                fill.color = withAlpha(style.progressColor.toInt(), 210)
                canvas.drawRect(RectF(tank.left, level, tank.right, tank.bottom), fill)
                val surf = Path().apply {
                    moveTo(tank.left, level)
                    var x = tank.left
                    while (x <= tank.right) {
                        lineTo(x, level + radius * 0.05f * kotlin.math.sin((x / radius) * 4f + phase * 6.283f))
                        x += radius * 0.06f
                    }
                    lineTo(tank.right, tank.bottom)
                    lineTo(tank.left, tank.bottom)
                    close()
                }
                fill.color = withAlpha(style.sparkColor.toInt(), 150)
                canvas.drawPath(surf, fill)
                canvas.drawCircle(centreX, centreY - radius * 0.30f, radius * 0.10f, fill)
            }

            ChargingMotif.PORTAL -> {
                repeat(4) { i ->
                    val t = (i + 1) / 4f
                    val on = p >= t - 0.001f
                    stroke.color = if (on) style.progressColor.toInt()
                    else withAlpha(style.sparkColor.toInt(), 55)
                    stroke.strokeWidth = radius * (0.16f - i * 0.03f)
                    canvas.drawCircle(centreX, centreY, radius * (1f - i * 0.22f), stroke)
                }
                fill.color = withAlpha(style.sparkColor.toInt(), 190)
                canvas.drawCircle(centreX, centreY, radius * 0.16f * p.coerceAtLeast(0.2f), fill)
            }

            ChargingMotif.SHATTER -> {
                val shards = 9
                for (i in 0 until shards) {
                    val a0 = i * (360f / shards)
                    val a1 = a0 + (360f / shards) - 6f
                    val reach = radius * (0.35f + 0.65f * p) * (0.75f + 0.25f * ((i * 37) % 10) / 10f)
                    val shard = Path().apply {
                        moveTo(centreX, centreY)
                        lineTo(centreX + (reach * kotlin.math.cos(Math.toRadians(a0.toDouble()))).toFloat(),
                               centreY + (reach * kotlin.math.sin(Math.toRadians(a0.toDouble()))).toFloat())
                        lineTo(centreX + (reach * kotlin.math.cos(Math.toRadians(a1.toDouble()))).toFloat(),
                               centreY + (reach * kotlin.math.sin(Math.toRadians(a1.toDouble()))).toFloat())
                        close()
                    }
                    fill.color = withAlpha(
                        if (i % 2 == 0) style.progressColor.toInt() else style.sparkColor.toInt(),
                        170
                    )
                    canvas.drawPath(shard, fill)
                    stroke.color = withAlpha(style.trackColor.toInt(), 160)
                    stroke.strokeWidth = radius * 0.02f
                    canvas.drawPath(shard, stroke)
                }
            }

            ChargingMotif.HEART -> {
                val heart = Path().apply {
                    moveTo(centreX, centreY + radius * 0.72f)
                    cubicTo(
                        centreX - radius * 1.05f, centreY + radius * 0.10f,
                        centreX - radius * 0.72f, centreY - radius * 0.82f,
                        centreX, centreY - radius * 0.34f
                    )
                    cubicTo(
                        centreX + radius * 0.72f, centreY - radius * 0.82f,
                        centreX + radius * 1.05f, centreY + radius * 0.10f,
                        centreX, centreY + radius * 0.72f
                    )
                    close()
                }
                stroke.color = mix(style.trackColor, Color.WHITE, 0.16f)
                stroke.strokeWidth = radius * 0.07f
                canvas.drawPath(heart, stroke)
                canvas.save()
                val level = centreY + radius * 0.76f - radius * 1.6f * p
                canvas.clipRect(RectF(centreX - radius * 1.1f, level, centreX + radius * 1.1f, centreY + radius * 0.8f))
                fill.color = style.progressColor.toInt()
                canvas.drawPath(heart, fill)
                canvas.restore()
                if (style.motif == ChargingMotif.HEART) {
                    // one specular streak so the fill reads as liquid, not vinyl
                    fill.color = withAlpha(style.sparkColor.toInt(), 150)
                    canvas.save()
                    canvas.clipPath(heart)
                    canvas.drawOval(
                        RectF(centreX - radius * 0.52f, centreY - radius * 0.56f,
                              centreX - radius * 0.16f, centreY - radius * 0.22f), fill
                    )
                    canvas.restore()
                }
            }

            ChargingMotif.BUTTERFLY -> {
                // wings open with the level: at 0 % the fold is shut, at 100 % it is wide
                val spread = 0.35f + 0.65f * p
                val upper = Path().apply {
                    moveTo(centreX, centreY - radius * 0.10f)
                    cubicTo(
                        centreX - radius * 1.10f * spread, centreY - radius * 1.05f,
                        centreX - radius * 1.10f * spread, centreY + radius * 0.22f,
                        centreX, centreY + radius * 0.16f
                    )
                    close()
                }
                val lower = Path().apply {
                    moveTo(centreX, centreY + radius * 0.16f)
                    cubicTo(
                        centreX - radius * 0.92f * spread, centreY + radius * 0.30f,
                        centreX - radius * 0.62f * spread, centreY + radius * 1.02f,
                        centreX, centreY + radius * 0.58f
                    )
                    close()
                }
                val mirror = Path(upper).apply { transform(android.graphics.Matrix().apply { preScale(-1f, 1f, centreX, 0f) }) }
                val mirrorLower = Path(lower).apply { transform(android.graphics.Matrix().apply { preScale(-1f, 1f, centreX, 0f) }) }
                fill.color = style.progressColor.toInt()
                listOf(upper, mirror, lower, mirrorLower).forEach { canvas.drawPath(it, fill) }
                fill.color = withAlpha(style.sparkColor.toInt(), 170)
                listOf(upper, mirror).forEach { canvas.drawPath(it, fill) }
                stroke.color = style.sparkColor.toInt()
                stroke.strokeWidth = radius * 0.06f
                canvas.drawLine(centreX, centreY - radius * 0.22f, centreX, centreY + radius * 0.62f, stroke)
            }

            ChargingMotif.STARFIELD -> {
                fill.color = style.trackColor.toInt()
                val count = 24
                for (i in 0 until count) {
                    val angle = (i / count.toFloat()) * 6.283f
                    val ring = radius * (0.55f + 0.42f * ((i % 5) / 5f))
                    val x = centreX + ring * kotlin.math.cos(angle)
                    val y = centreY + ring * kotlin.math.sin(angle) * 0.9f
                    val lit = (i / count.toFloat()) <= p
                    fill.color = if (lit) style.progressColor.toInt()
                    else withAlpha(style.sparkColor.toInt(), 45)
                    val size = radius * if (lit) 0.075f else 0.045f
                    canvas.drawCircle(x, y, size, fill)
                    if (lit) {
                        fill.color = withAlpha(style.sparkColor.toInt(), 120)
                        canvas.drawCircle(x, y, size * 2.1f, fill)
                    }
                }
                stroke.color = withAlpha(style.sparkColor.toInt(), 120)
                stroke.strokeWidth = radius * 0.025f
                canvas.drawCircle(centreX, centreY, radius * (0.96f), stroke)
            }

            ChargingMotif.ORBIT -> {
                fill.color = withAlpha(style.progressColor.toInt(), 90)
                canvas.drawCircle(centreX, centreY, radius * 0.42f, fill)
                fill.color = style.progressColor.toInt()
                canvas.drawCircle(centreX, centreY, radius * 0.30f, fill)
                stroke.color = mix(style.trackColor, Color.WHITE, 0.18f)
                stroke.strokeWidth = radius * 0.05f
                canvas.save()
                canvas.rotate(-18f, centreX, centreY)
                canvas.drawOval(
                    RectF(centreX - radius, centreY - radius * 0.42f,
                          centreX + radius, centreY + radius * 0.42f), stroke
                )
                stroke.color = style.sparkColor.toInt()
                stroke.strokeWidth = radius * 0.09f
                val sweep = RectF(centreX - radius, centreY - radius * 0.42f,
                                  centreX + radius, centreY + radius * 0.42f)
                canvas.drawArc(sweep, -90f, 359.9f * p, false, stroke)
                val angle = Math.toRadians((-90.0 + 359.9 * p))
                val moonX = centreX + (radius * Math.cos(angle)).toFloat()
                val moonY = centreY + (radius * 0.42f * Math.sin(angle)).toFloat()
                fill.color = style.sparkColor.toInt()
                canvas.drawCircle(moonX, moonY, radius * 0.12f, fill)
                canvas.restore()
            }

            ChargingMotif.GARDEN -> {
                val petals = 4
                for (i in 0 until petals) {
                    val threshold = (i + 1) / petals.toFloat()
                    val open = p >= threshold - 0.001f
                    val angle = Math.toRadians(-90.0 + i * 90.0)
                    val dist = radius * if (open) 0.46f else 0.26f
                    val px = centreX + (dist * kotlin.math.cos(angle)).toFloat()
                    val py = centreY + (dist * kotlin.math.sin(angle)).toFloat()
                    fill.color = if (open) style.progressColor.toInt()
                    else withAlpha(style.sparkColor.toInt(), 60)
                    canvas.drawCircle(px, py, radius * if (open) 0.30f else 0.22f, fill)
                }
                fill.color = style.sparkColor.toInt()
                canvas.drawCircle(centreX, centreY, radius * 0.16f, fill)
                stroke.color = withAlpha(style.sparkColor.toInt(), 150)
                stroke.strokeWidth = radius * 0.035f
                canvas.drawLine(centreX, centreY, centreX - radius * 0.9f, centreY + radius * 0.9f, stroke)
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
