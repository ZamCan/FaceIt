package com.zamcan.faceit.ui

import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.zamcan.faceit.resource.ThermalPressure
import com.zamcan.faceit.ui.theme.LocalFaceItColors

/**
 * Runtime visual-motion policy.
 *
 * AGENTS.md §13 and §17 require reduced-motion support that actually changes
 * behaviour. Compose offers no built-in "reduce motion" switch, so FaceIt
 * resolves the same signals Android exposes: the system animation scale, the
 * battery-saver flag and (on API 29+) the platform thermal status. When any of
 * them says "slow down", the app renders static surfaces and skips press
 * animation, instead of pretending to respect the setting.
 */
@Immutable
data class MotionPolicy(
    val animationsEnabled: Boolean,
    val reason: String?,
    val thermalPressure: ThermalPressure
) {
    val staticOnly: Boolean get() = !animationsEnabled
}

val LocalMotionPolicy = staticCompositionLocalOf {
    MotionPolicy(animationsEnabled = true, reason = null, thermalPressure = ThermalPressure.NONE)
}

/** Reads the platform signals once and keeps thermal status current while composed. */
@Composable
fun rememberMotionPolicy(): MotionPolicy {
    val context = LocalContext.current
    var thermal by remember { mutableStateOf(readThermalPressure(context)) }
    var animations by remember { mutableStateOf(readAnimationsEnabled(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                animations = readAnimationsEnabled(context)
                thermal = readThermalPressure(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val reason = when {
        thermal == ThermalPressure.SEVERE -> "Thermal status is severe"
        thermal == ThermalPressure.MODERATE -> "Device is warm"
        !animations -> "Reduced motion is on"
        else -> null
    }
    return MotionPolicy(
        animationsEnabled = animations && thermal < ThermalPressure.MODERATE,
        reason = reason,
        thermalPressure = thermal
    )
}

private fun readAnimationsEnabled(context: android.content.Context): Boolean {
    val animatorScale = runCatching {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    }.getOrDefault(1f)
    val powerManager = ContextCompat.getSystemService(context, PowerManager::class.java)
    val batterySaver = powerManager?.isPowerSaveMode == true
    return animatorScale > 0f && !batterySaver
}

fun readThermalPressure(context: android.content.Context): ThermalPressure {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return ThermalPressure.NONE
    val powerManager = ContextCompat.getSystemService(context, PowerManager::class.java) ?: return ThermalPressure.NONE
    return try {
        when (powerManager.currentThermalStatus) {
            PowerManager.THERMAL_STATUS_NONE,
            PowerManager.THERMAL_STATUS_LIGHT -> ThermalPressure.NONE
            PowerManager.THERMAL_STATUS_MODERATE -> ThermalPressure.MODERATE
            else -> ThermalPressure.SEVERE
        }
    } catch (_: Throwable) {
        ThermalPressure.NONE
    }
}

/**
 * The shared glass surface from AGENTS.md §4.
 *
 * One component, one set of tokens, real interaction states. Every card in the
 * product goes through here so depth, highlight, tint and press response cannot
 * drift between screens. The "glass" is composed from layered translucency,
 * an edge reflection and a directional specular, not a blur filter over a
 * photograph — it stays legible over busy artwork and costs almost nothing on
 * low-end phones.
 */
enum class GlassTone { Standard, Raised, Sunken, Accent }

@Composable
fun FaceItGlass(
    modifier: Modifier = Modifier,
    tone: GlassTone = GlassTone.Standard,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp),
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalFaceItColors.current
    val base = when (tone) {
        GlassTone.Standard -> colors.card
        GlassTone.Raised -> colors.cardRaised
        GlassTone.Sunken -> colors.cardSunken
        GlassTone.Accent -> colors.violet.copy(alpha = if (colors.isDark) .13f else .08f)
    }
    val tintAlpha = if (colors.isDark) .72f else .92f
    val topTint = if (colors.isDark) Color.White.copy(alpha = .07f) else Color.White.copy(alpha = .75f)
    val bottomTint = if (colors.isDark) Color.Black.copy(alpha = .16f) else Color.Black.copy(alpha = .03f)
    val borderColor = if (tone == GlassTone.Accent) {
        colors.violet.copy(alpha = .42f)
    } else {
        colors.border
    }

    val glassModifier = modifier
        .clip(shape)
        .background(base.copy(alpha = tintAlpha))
        .background(Brush.verticalGradient(listOf(topTint, Color.Transparent, bottomTint)))
        .drawWithCache {
            val stroke = 1.dp.toPx()
            val specular = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (colors.isDark) .30f else .85f),
                    Color.Transparent,
                    Color.Transparent
                ),
                start = Offset(0f, 0f),
                end = Offset(size.width * .85f, size.height * .9f)
            )
            onDrawWithContent {
                drawContent()
                // top-edge reflection: the single cue that separates glass from a flat card
                drawLine(
                    brush = specular,
                    start = Offset(stroke, stroke),
                    end = Offset(size.width - stroke, stroke),
                    strokeWidth = stroke
                )
            }
        }
        .border(BorderStroke(1.dp, borderColor), shape)

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = glassModifier,
            shape = shape,
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    } else {
        Column(
            modifier = glassModifier.padding(contentPadding),
            content = content
        )
    }
}

/** A glass surface that hosts arbitrary content instead of a column. */
@Composable
fun FaceItGlassBox(
    modifier: Modifier = Modifier,
    tone: GlassTone = GlassTone.Standard,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp),
    border: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = LocalFaceItColors.current
    val base = when (tone) {
        GlassTone.Standard -> colors.card
        GlassTone.Raised -> colors.cardRaised
        GlassTone.Sunken -> colors.cardSunken
        GlassTone.Accent -> colors.violet.copy(alpha = if (colors.isDark) .13f else .08f)
    }
    val decorated = modifier
        .clip(shape)
        .background(base.copy(alpha = if (colors.isDark) .72f else .92f))
        .background(
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = if (colors.isDark) .07f else .75f),
                    Color.Transparent,
                    Color.Black.copy(alpha = if (colors.isDark) .16f else .03f)
                )
            )
        )
    Box(
        modifier = if (border) {
            decorated.border(BorderStroke(1.dp, colors.border), shape)
        } else {
            decorated
        },
        content = content
    )
}
