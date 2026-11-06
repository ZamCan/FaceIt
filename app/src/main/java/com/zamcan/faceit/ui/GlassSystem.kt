package com.zamcan.faceit.ui

import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
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
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    val motion = LocalMotionPolicy.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && motion.animationsEnabled) .985f else 1f,
        animationSpec = tween(if (motion.animationsEnabled) 140 else 0),
        label = "glassPress"
    )
    val glassModifier = modifier
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .faceItGlassFinish(tone, shape, border = true)

    if (onClick != null) {
        Surface(
            onClick = onClick,
            interactionSource = interactions,
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
    Box(
        modifier = modifier.faceItGlassFinish(tone, shape, border),
        content = content
    )
}

/** A single optical treatment for cards, previews and the navigation dock. */
@Composable
private fun Modifier.faceItGlassFinish(tone: GlassTone, shape: Shape, border: Boolean): Modifier {
    val colors = LocalFaceItColors.current
    val reduced = LocalMotionPolicy.current.staticOnly
    val base = when (tone) {
        GlassTone.Standard -> colors.card
        GlassTone.Raised -> colors.cardRaised
        GlassTone.Sunken -> colors.cardSunken
        GlassTone.Accent -> colors.card
    }
    val edge = if (tone == GlassTone.Accent) colors.violet.copy(alpha = .44f) else colors.border
    val elevation = if (reduced || tone == GlassTone.Sunken) 0.dp else if (tone == GlassTone.Raised) 10.dp else 4.dp
    return this
        .shadow(elevation, shape, clip = false)
        .clip(shape)
        // Keep a substantial neutral substrate so text remains readable over artwork.
        .background(base.copy(alpha = if (colors.isDark) .94f else .97f))
        .drawWithCache {
            val wash = Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = if (colors.isDark) .09f else .66f),
                    colors.violet.copy(alpha = if (tone == GlassTone.Accent) .10f else .025f),
                    Color.Transparent,
                    Color.Black.copy(alpha = if (colors.isDark) .10f else .025f)
                ),
                start = Offset.Zero,
                end = Offset(size.width * .8f, size.height)
            )
            val reflection = Brush.horizontalGradient(
                listOf(Color.Transparent, Color.White.copy(alpha = if (colors.isDark) .48f else .95f), Color.Transparent)
            )
            onDrawWithContent {
                drawRect(wash)
                drawContent()
                if (border) {
                    drawLine(reflection, Offset(24.dp.toPx(), 1.dp.toPx()),
                        Offset((size.width - 24.dp.toPx()).coerceAtLeast(24.dp.toPx()), 1.dp.toPx()),
                        strokeWidth = 1.dp.toPx())
                }
            }
        }
        .then(if (border) Modifier.border(BorderStroke(1.dp, edge), shape) else Modifier)
}
