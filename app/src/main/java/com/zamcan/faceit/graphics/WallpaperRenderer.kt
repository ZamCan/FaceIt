package com.zamcan.faceit.graphics
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import com.zamcan.faceit.data.PersonalEnvironment
import kotlin.math.cos
import kotlin.math.sin

/** Offline procedural renderer. Curated photographic PNG packs are managed separately. */
object WallpaperRenderer {
    fun render(width: Int, height: Int, environment: PersonalEnvironment): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val scale = width / 1080f
        fun px(v: Float) = v * scale
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(0xFF171923.toInt(), 0xFF292B32.toInt(), 0xFF101B1A.toInt()), null, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bg)
        val horizon = height * .53f
        val lines = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x33E9E5D9; style = Paint.Style.STROKE; strokeWidth = px(3f) }
        canvas.drawRect(px(50f), horizon, width - px(50f), height - px(80f), lines)
        canvas.drawLine(width / 2f, horizon, width / 2f, height - px(80f), lines)
        canvas.drawCircle(width / 2f, (horizon + height - px(80f)) / 2f, px(115f), lines)
        for (i in 0..18) {
            val x = px(40f + i * 58f)
            canvas.drawLine(x, horizon - px(150f), x, horizon - px(15f),
                Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x18FFFFFF; strokeWidth = px(2f) })
        }
        val cx = width / 2f
        val cy = height * .39f
        val radius = px(245f)
        val manePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(cx-radius, cy-radius, cx+radius, cy+radius,
                intArrayOf(0xFFE5D8BC.toInt(), 0xFF9F9077.toInt(), 0xFF514B43.toInt()), null, Shader.TileMode.CLAMP)
        }
        val mane = Path()
        for (i in 0..48) {
            val angle = i * 2.0 * Math.PI / 48.0
            val r = radius * if (i % 4 == 0) 1f else .91f
            val x = cx + cos(angle).toFloat() * r
            val y = cy + sin(angle).toFloat() * r * 1.08f
            if (i == 0) mane.moveTo(x, y) else mane.lineTo(x, y)
        }
        mane.close()
        canvas.drawPath(mane, manePaint)
        val facePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(cx, cy-radius, cx, cy+radius, 0xFFE9DFC9.toInt(), 0xFF8F8068.toInt(), Shader.TileMode.CLAMP)
        }
        val face = Path().apply {
            moveTo(cx, cy-px(180f))
            cubicTo(cx-px(170f), cy-px(155f), cx-px(155f), cy+px(85f), cx, cy+px(190f))
            cubicTo(cx+px(155f), cy+px(85f), cx+px(170f), cy-px(155f), cx, cy-px(180f))
            close()
        }
        canvas.drawPath(face, facePaint)
        val detail = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF39332D.toInt(); style = Paint.Style.STROKE; strokeWidth = px(9f); strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(cx-px(72f), cy-px(30f), cx-px(20f), cy-px(18f), detail)
        canvas.drawLine(cx+px(20f), cy-px(18f), cx+px(72f), cy-px(30f), detail)
        canvas.drawCircle(cx, cy+px(54f), px(19f), Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF39332D.toInt() })
        canvas.drawLine(cx, cy+px(70f), cx, cy+px(112f), detail)
        val vignette = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, 0f, height.toFloat(),
                intArrayOf(0x33000000, 0x00000000, 0x66000000), null, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), vignette)
        return bitmap
    }
}
