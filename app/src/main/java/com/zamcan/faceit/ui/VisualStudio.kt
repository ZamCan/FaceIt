package com.zamcan.faceit.ui

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zamcan.faceit.charging.ChargingMotif
import com.zamcan.faceit.charging.ChargingStyle
import com.zamcan.faceit.charging.ChargingStyles
import com.zamcan.faceit.data.CuratedLibrary
import com.zamcan.faceit.graphics.IconGlyph
import com.zamcan.faceit.keypad.KeypadDesign
import com.zamcan.faceit.keypad.KeypadDesigns
import com.zamcan.faceit.themes.FaceTheme
import com.zamcan.faceit.themes.FaceThemes
import com.zamcan.faceit.themes.VeilStyle
import com.zamcan.faceit.ui.theme.LocalFaceItColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Visual studio: themes, app icons, keyboard designs, keypad designs and the
 * charging display, in one place.
 *
 * The previews here are drawn with Compose primitives rather than pre-rendered
 * images, so a theme costs no bundle bytes and every tone stays exactly the one
 * the theme declares. Nothing in this screen is a system overlay mock-up: the
 * charging preview is an in-app render, and the copy says so.
 */

internal enum class VisualPage(val label: String) {
    Themes("Themes"),
    Icons("Icons"),
    Keyboard("Keyboard"),
    Keypad("Keypad"),
    Charging("Charging")
}

@Composable
internal fun VisualStudioScreen(
    page: VisualPage,
    selectedThemeId: String?,
    onOpen: (VisualPage) -> Unit,
    onThemeSelected: (FaceTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(VisualPage.entries, key = { it.name }) { entry ->
                VisualChip(
                    label = entry.label,
                    selected = entry == page,
                    onClick = { onOpen(entry) }
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        when (page) {
            VisualPage.Themes -> ThemesPage(selectedThemeId, onThemeSelected)
            VisualPage.Icons -> IconsPage(FaceThemes.find(selectedThemeId))
            VisualPage.Keyboard -> KeyboardPage(FaceThemes.find(selectedThemeId))
            VisualPage.Keypad -> KeypadPage(FaceThemes.find(selectedThemeId))
            VisualPage.Charging -> ChargingPage(FaceThemes.find(selectedThemeId))
        }
    }
}

@Composable
private fun VisualChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalFaceItColors.current
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) colors.violet.copy(alpha = .16f) else colors.card,
        border = BorderStroke(1.dp, if (selected) colors.violet.copy(alpha = .5f) else colors.border)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.violet else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

// ---------------------------------------------------------------- themes

@Composable
private fun ThemesPage(selectedThemeId: String?, onSelected: (FaceTheme) -> Unit) {
    var family by remember { mutableStateOf<String?>(null) }
    val themes = remember(family) { FaceThemes.byFamily(family) }

    Column(Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { VisualChip("All", family == null) { family = null } }
            items(FaceThemes.families, key = { it }) { name ->
                VisualChip(name, family == name) { family = if (family == name) null else name }
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 168.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(themes, key = { it.id }) { theme ->
                ThemeTile(
                    theme = theme,
                    selected = theme.id == selectedThemeId,
                    onClick = { onSelected(theme) }
                )
            }
        }
    }
}

@Composable
private fun ThemeTile(theme: FaceTheme, selected: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    val library = remember { CuratedLibrary(context) }
    var wallpaper by remember(theme.wallpaperId) { mutableStateOf<Bitmap?>(null) }
    var chargingPhase by remember { mutableStateOf(0f) }

    LaunchedEffect(theme.wallpaperId) {
        wallpaper = withContext(Dispatchers.IO) {
            library.find(theme.wallpaperId)?.let { library.thumbnail(it, 260) }
        }
    }
    LaunchedEffect(theme.id) {
        // A slow, low-amplitude cycle so the charging preview reads as alive without
        // being a decorative distraction, and only while this tile is composed.
        while (true) {
            kotlinx.coroutines.delay(90)
            chargingPhase = (chargingPhase + 0.012f) % 1f
        }
    }
    DisposableEffect(theme.id) { onDispose { } }

    val surface = Color(theme.surface)
    val onSurface = Color(theme.textPrimary)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = surface,
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) Color(theme.accent) else Color(theme.border)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "${theme.name} theme, ${theme.family}" }
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.82f)
                    .clip(RoundedCornerShape(topStart = 23.dp, topEnd = 23.dp))
                    .background(Color(theme.background))
            ) {
                wallpaper?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                // icon grid over the wallpaper: three rows of themed tiles
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FaceThemes.showcaseGlyphs.chunked(4).take(3).forEach { row ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEach { glyph ->
                                ThemedIcon(
                                    theme = theme,
                                    glyph = glyph,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .weight(1f)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    ChargingPreview(
                        style = ChargingStyles.find(theme.chargingStyleId),
                        phase = chargingPhase,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )
                }
                if (theme.veilStyle != VeilStyle.NONE) {
                    VeilFrame(
                        style = theme.veilStyle,
                        colour = Color(theme.veilColor),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (selected) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Selected theme",
                        tint = Color(0xFF101015),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .size(20.dp)
                            .background(Color(theme.accent), CircleShape)
                            .padding(2.dp)
                    )
                }
            }
            Column(Modifier.padding(12.dp)) {
                Text(theme.name, style = MaterialTheme.typography.titleSmall, color = onSurface)
                Text(
                    theme.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(theme.textSecondary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * The frame drawn over the app grid.
 *
 * These are abstract drape and fold shapes: translucent curves that frame the
 * icons. No figure, face or garment is drawn — the motif is shading and geometry.
 */
@Composable
private fun VeilFrame(style: VeilStyle, colour: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val tint = colour.copy(alpha = 0.22f)
        val tintDeep = colour.copy(alpha = 0.34f)
        when (style) {
            VeilStyle.NONE -> Unit
            VeilStyle.SOFT_DRAPE -> {
                val left = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w * 0.42f, 0f)
                    cubicTo(w * 0.20f, h * 0.22f, w * 0.34f, h * 0.46f, w * 0.06f, h * 0.62f)
                    lineTo(0f, h * 0.62f)
                    close()
                }
                drawPath(left, tint)
                val right = Path().apply {
                    moveTo(w, 0f)
                    lineTo(w * 0.58f, 0f)
                    cubicTo(w * 0.80f, h * 0.22f, w * 0.66f, h * 0.46f, w * 0.94f, h * 0.62f)
                    lineTo(w, h * 0.62f)
                    close()
                }
                drawPath(right, tint)
            }
            VeilStyle.VEIL_FRAME -> {
                val frame = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w, 0f)
                    lineTo(w, h * 0.16f)
                    cubicTo(w * 0.78f, h * 0.02f, w * 0.22f, h * 0.02f, 0f, h * 0.16f)
                    close()
                }
                drawPath(frame, tintDeep)
                drawRoundRect(
                    color = colour.copy(alpha = 0.30f),
                    topLeft = Offset(w * 0.04f, h * 0.14f),
                    size = Size(w * 0.92f, h * 0.78f),
                    cornerRadius = CornerRadius(w * 0.20f, w * 0.20f),
                    style = Stroke(width = w * 0.016f)
                )
            }
            VeilStyle.LAYERED_FOLD -> {
                listOf(0.10f to 0.20f, 0.22f to 0.30f, 0.34f to 0.38f).forEach { (start, alpha) ->
                    val fold = Path().apply {
                        moveTo(0f, h * start)
                        cubicTo(w * 0.35f, h * (start - 0.10f), w * 0.65f, h * (start + 0.12f), w, h * start)
                        lineTo(w, h * (start + 0.10f))
                        cubicTo(w * 0.65f, h * (start + 0.22f), w * 0.35f, h * start, 0f, h * (start + 0.10f))
                        close()
                    }
                    drawPath(fold, colour.copy(alpha = alpha))
                }
            }
            VeilStyle.OPEN_ARCH -> {
                val arch = Path().apply {
                    moveTo(w * 0.10f, h * 0.30f)
                    cubicTo(w * 0.10f, h * 0.06f, w * 0.90f, h * 0.06f, w * 0.90f, h * 0.30f)
                }
                drawPath(arch, colour.copy(alpha = 0.55f), style = Stroke(width = w * 0.022f))
                drawCircle(
                    color = colour.copy(alpha = 0.30f),
                    radius = w * 0.026f,
                    center = Offset(w * 0.5f, h * 0.075f)
                )
            }
            VeilStyle.TWIN_CURVE -> {
                val left = Path().apply {
                    moveTo(0f, h)
                    cubicTo(w * 0.08f, h * 0.80f, w * 0.34f, h * 0.74f, w * 0.5f, h * 0.86f)
                    lineTo(w * 0.5f, h)
                    close()
                }
                drawPath(left, tintDeep)
                val right = Path().apply {
                    moveTo(w, h)
                    cubicTo(w * 0.92f, h * 0.80f, w * 0.66f, h * 0.74f, w * 0.5f, h * 0.86f)
                    lineTo(w * 0.5f, h)
                    close()
                }
                drawPath(right, tint)
            }
        }
    }
}

// ---------------------------------------------------------------- icons

@Composable
private fun IconsPage(theme: FaceTheme) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        ScreenHeader(
            eyebrow = "App icons",
            title = "${theme.name} icon set",
            description = "Twelve generic glyphs drawn on-device. No real app's icon, logo or trade dress is reproduced."
        )
        Spacer(Modifier.height(14.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(IconGlyph.entries, key = { it.name }) { glyph ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ThemedIcon(
                        theme = theme,
                        glyph = glyph,
                        modifier = Modifier.size(58.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = glyph.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * One themed icon tile.
 *
 * Mirrors [com.zamcan.faceit.graphics.IconRenderer] so the Compose preview and the
 * Android-canvas renderer agree on shape language without duplicating bitmaps.
 */
@Composable
private fun ThemedIcon(theme: FaceTheme, glyph: IconGlyph, modifier: Modifier = Modifier) {
    val tile = Color(theme.iconTileColor)
    val ink = Color(theme.iconGlyphColor)
    val accent = Color(theme.accent)
    val shape = when (theme.iconTile) {
        com.zamcan.faceit.graphics.IconTile.PEBBLE_CIRCLE -> CircleShape
        com.zamcan.faceit.graphics.IconTile.CERAMIC_SQUIRCLE -> RoundedCornerShape(34)
        com.zamcan.faceit.graphics.IconTile.GLASS_ROUNDED -> RoundedCornerShape(28)
        com.zamcan.faceit.graphics.IconTile.FROSTED_ROUNDED -> RoundedCornerShape(38)
        com.zamcan.faceit.graphics.IconTile.STONE_ROUNDED -> RoundedCornerShape(22)
        com.zamcan.faceit.graphics.IconTile.MATTE_SQUARE -> RoundedCornerShape(10)
    }
    Box(
        modifier = modifier
            .clip(shape)
            .background(tile)
            .semantics { contentDescription = glyph.name.lowercase() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize().padding(10.dp)) {
            drawGlyph(glyph, ink, accent)
        }
    }
}

private fun DrawScope.drawGlyph(glyph: IconGlyph, ink: Color, accent: Color) {
    val w = size.width
    val h = size.height
    val stroke = w * 0.09f
    when (glyph) {
        IconGlyph.PHONE -> {
            val path = Path().apply {
                moveTo(w * 0.12f, h * 0.10f)
                lineTo(w * 0.38f, h * 0.26f)
                lineTo(w * 0.26f, h * 0.52f)
                lineTo(w * 0.52f, h * 0.78f)
                lineTo(w * 0.78f, h * 0.66f)
                lineTo(w * 0.92f, h * 0.92f)
                lineTo(w * 0.62f, h * 0.98f)
                lineTo(w * 0.04f, h * 0.40f)
                close()
            }
            drawPath(path, ink)
        }
        IconGlyph.MESSAGE -> {
            drawRoundRect(
                ink, Offset(0f, h * 0.06f), Size(w, h * 0.64f),
                CornerRadius(w * 0.18f), style = Stroke(stroke)
            )
            val tail = Path().apply {
                moveTo(w * 0.26f, h * 0.64f)
                lineTo(w * 0.20f, h)
                lineTo(w * 0.46f, h * 0.66f)
                close()
            }
            drawPath(tail, ink, style = Stroke(stroke))
            repeat(3) { i -> drawCircle(accent, w * 0.055f, Offset(w * (0.30f + i * 0.20f), h * 0.38f)) }
        }
        IconGlyph.CAMERA -> {
            drawRoundRect(
                ink, Offset(0f, h * 0.24f), Size(w, h * 0.70f),
                CornerRadius(w * 0.16f), style = Stroke(stroke)
            )
            drawRoundRect(
                ink, Offset(w * 0.28f, h * 0.08f), Size(w * 0.40f, h * 0.18f),
                CornerRadius(w * 0.06f), style = Stroke(stroke)
            )
            drawCircle(ink, w * 0.19f, Offset(w * 0.5f, h * 0.60f), style = Stroke(stroke))
            drawCircle(accent, w * 0.07f, Offset(w * 0.5f, h * 0.60f))
        }
        IconGlyph.MUSIC -> {
            drawLine(ink, Offset(w * 0.66f, h * 0.04f), Offset(w * 0.66f, h * 0.68f), stroke)
            drawLine(ink, Offset(w * 0.66f, h * 0.04f), Offset(w * 0.94f, h * 0.14f), stroke)
            drawLine(ink, Offset(w * 0.94f, h * 0.14f), Offset(w * 0.94f, h * 0.58f), stroke)
            drawCircle(ink, w * 0.14f, Offset(w * 0.52f, h * 0.72f))
            drawCircle(ink, w * 0.14f, Offset(w * 0.80f, h * 0.62f))
        }
        IconGlyph.GALLERY -> {
            drawRoundRect(
                ink, Offset(0f, h * 0.08f), Size(w, h * 0.92f),
                CornerRadius(w * 0.12f), style = Stroke(stroke)
            )
            drawCircle(accent, w * 0.075f, Offset(w * 0.72f, h * 0.30f))
            val hills = Path().apply {
                moveTo(w * 0.10f, h * 0.92f)
                lineTo(w * 0.36f, h * 0.52f)
                lineTo(w * 0.58f, h * 0.92f)
                close()
            }
            drawPath(hills, ink)
        }
        IconGlyph.CLOCK -> {
            drawCircle(ink, w * 0.44f, Offset(w * 0.5f, h * 0.5f), style = Stroke(stroke))
            drawLine(ink, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.5f, h * 0.24f), stroke)
            drawLine(ink, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.70f, h * 0.60f), stroke)
        }
        IconGlyph.CALENDAR -> {
            drawRoundRect(
                ink, Offset(0f, h * 0.16f), Size(w, h * 0.80f),
                CornerRadius(w * 0.12f), style = Stroke(stroke)
            )
            drawLine(ink, Offset(0f, h * 0.36f), Offset(w, h * 0.36f), stroke)
            drawLine(ink, Offset(w * 0.30f, h * 0.04f), Offset(w * 0.30f, h * 0.22f), stroke)
            drawLine(ink, Offset(w * 0.70f, h * 0.04f), Offset(w * 0.70f, h * 0.22f), stroke)
            drawCircle(accent, w * 0.09f, Offset(w * 0.5f, h * 0.62f))
        }
        IconGlyph.MAIL -> {
            drawRoundRect(
                ink, Offset(0f, h * 0.20f), Size(w, h * 0.70f),
                CornerRadius(w * 0.10f), style = Stroke(stroke)
            )
            val flap = Path().apply {
                moveTo(w * 0.04f, h * 0.26f)
                lineTo(w * 0.5f, h * 0.56f)
                lineTo(w * 0.96f, h * 0.26f)
            }
            drawPath(flap, ink, style = Stroke(stroke))
        }
        IconGlyph.BROWSER -> {
            drawCircle(ink, w * 0.46f, Offset(w * 0.5f, h * 0.52f), style = Stroke(stroke))
            drawLine(ink, Offset(w * 0.06f, h * 0.52f), Offset(w * 0.94f, h * 0.52f), stroke)
            drawOval(
                ink, Offset(w * 0.30f, h * 0.06f), Size(w * 0.40f, h * 0.92f),
                style = Stroke(stroke)
            )
            drawCircle(accent, w * 0.07f, Offset(w * 0.32f, h * 0.34f))
        }
        IconGlyph.WEATHER -> {
            drawCircle(accent, w * 0.18f, Offset(w * 0.36f, h * 0.34f))
            val cloud = Path().apply {
                moveTo(w * 0.16f, h * 0.70f)
                cubicTo(w * 0.10f, h * 0.56f, w * 0.26f, h * 0.48f, w * 0.36f, h * 0.54f)
                cubicTo(w * 0.46f, h * 0.42f, w * 0.72f, h * 0.46f, w * 0.72f, h * 0.58f)
                cubicTo(w * 0.90f, h * 0.58f, w * 0.92f, h * 0.78f, w * 0.74f, h * 0.78f)
                lineTo(w * 0.24f, h * 0.78f)
                close()
            }
            drawPath(cloud, ink, style = Stroke(stroke))
        }
        IconGlyph.FILES -> {
            val folder = Path().apply {
                moveTo(0f, h * 0.30f)
                lineTo(w * 0.38f, h * 0.30f)
                lineTo(w * 0.48f, h * 0.42f)
                lineTo(w, h * 0.42f)
                lineTo(w, h * 0.94f)
                lineTo(0f, h * 0.94f)
                close()
            }
            drawPath(folder, ink, style = Stroke(stroke))
        }
        IconGlyph.SETTINGS -> {
            drawCircle(ink, w * 0.44f, Offset(w * 0.5f, h * 0.5f), style = Stroke(stroke))
            drawCircle(ink, w * 0.24f, Offset(w * 0.5f, h * 0.5f), style = Stroke(stroke))
            drawCircle(accent, w * 0.09f, Offset(w * 0.5f, h * 0.5f))
            repeat(8) { i ->
                val angle = Math.toRadians(i * 45.0)
                val inner = w * 0.44f
                val outer = w * 0.52f
                drawLine(
                    ink,
                    Offset((w * 0.5f + inner * Math.cos(angle)).toFloat(), (h * 0.5f + inner * Math.sin(angle)).toFloat()),
                    Offset((w * 0.5f + outer * Math.cos(angle)).toFloat(), (h * 0.5f + outer * Math.sin(angle)).toFloat()),
                    stroke
                )
            }
        }
    }
}

// ---------------------------------------------------------------- keyboard + keypad

@Composable
private fun KeyboardPage(theme: FaceTheme) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        ScreenHeader(
            eyebrow = "Keyboard designs",
            title = "Typing surfaces",
            description = "Visual presets only. Face It never becomes your system keyboard."
        )
        Spacer(Modifier.height(14.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(theme.surface))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val rows = listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM")
            rows.forEachIndexed { index, row ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    row.forEach { letter ->
                        KeyCap(theme, letter.toString(), Modifier.weight(1f))
                    }
                    if (index == 2) {
                        KeyCap(theme, "⌫", Modifier.weight(1.4f), accent = true)
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                KeyCap(theme, "123", Modifier.weight(1.2f))
                KeyCap(theme, "", Modifier.weight(4f))
                KeyCap(theme, "return", Modifier.weight(1.6f), accent = true)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Keys are a visual demonstration inside Face It. Nothing typed here is sent anywhere.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun KeypadPage(theme: FaceTheme) {
    val design = remember(theme.id) { KeypadDesigns.all[theme.id.hashCode().mod(KeypadDesigns.all.size)] }
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        ScreenHeader(
            eyebrow = "Keypad designs",
            title = "Number pads",
            description = "The keypad look from your theme. A design preset, not a phone dialler."
        )
        Spacer(Modifier.height(14.dp))
        FaceItGlass(contentPadding = 14.dp) {
            KeypadPreview(design, Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Face It does not place calls and does not replace your phone's dialler. These pads are appearance presets for the number-pad surface.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun KeypadPreview(design: KeypadDesign, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(design.keyGapDp.dp)) {
        KeypadDesigns.faces.chunked(3).forEach { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(design.keyGapDp.dp)
            ) {
                row.forEach { face ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(design.keyHeightDp.dp)
                            .clip(RoundedCornerShape(design.cornerRadiusDp.dp))
                            .background(Color(design.keySurface))
                            .border(
                                1.dp,
                                Color(design.accent).copy(alpha = 0.28f),
                                RoundedCornerShape(design.cornerRadiusDp.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                face,
                                fontSize = 22.sp,
                                color = Color(design.legend)
                            )
                            KeypadDesigns.letters[face]?.let {
                                Text(it, fontSize = 8.sp, color = Color(design.accent))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyCap(
    theme: FaceTheme,
    label: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false
) {
    Box(
        modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (accent) Color(theme.accent).copy(alpha = 0.28f) else Color(theme.surfaceRaised)),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 13.sp, color = Color(theme.textPrimary))
    }
}

// ---------------------------------------------------------------- charging

@Composable
private fun ChargingPage(theme: FaceTheme) {
    var level by remember { mutableStateOf(0.62f) }
    var phase by remember { mutableStateOf(0f) }
    val style = remember(theme.id) { ChargingStyles.find(theme.chargingStyleId) }

    LaunchedEffect(theme.id) {
        while (true) {
            kotlinx.coroutines.delay(60)
            phase = (phase + 0.02f) % 1f
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        ScreenHeader(
            eyebrow = "Charging",
            title = style.label,
            description = style.description
        )
        Spacer(Modifier.height(14.dp))
        ChargingPreview(
            style = style,
            phase = phase,
            level = level,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Preview the charge level:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf(0.1f, 0.28f, 0.5f, 0.75f, 1f)) { value ->
                VisualChip(
                    label = "${(value * 100).toInt()}%",
                    selected = kotlin.math.abs(level - value) < 0.01f
                ) { level = value }
            }
        }
        Spacer(Modifier.height(16.dp))
        InfoNote(
            text = "This is an in-app charging display. Android does not let an ordinary app replace the system charging screen, so Face It shows this view itself and reuses the same animation in its widget.",
            tint = LocalFaceItColors.current.ice
        )
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Rounded.Bolt, contentDescription = null, tint = Color(theme.accent), modifier = Modifier.size(18.dp))
            Text(
                "Style: ${style.motif.name.lowercase().replace('_', ' ')}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * The charging animation, drawn with Compose so it scales to any box.
 *
 * Kept deliberately close to [com.zamcan.faceit.charging.ChargingRenderer] so the
 * widget and the app do not drift apart.
 */
@Composable
private fun ChargingPreview(
    style: ChargingStyle,
    phase: Float,
    level: Float = 0.62f,
    modifier: Modifier = Modifier
) {
    val progress = Color(style.progressColor)
    val spark = Color(style.sparkColor)
    val track = Color(style.trackColor)
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = minOf(w, h) * 0.40f
        when (style.motif) {
            ChargingMotif.RING, ChargingMotif.PULSE -> {
                drawCircle(track.copy(alpha = 0.55f), r, Offset(cx, cy), style = Stroke(r * 0.18f))
                drawArc(
                    progress,
                    -90f, 359.9f * level, false,
                    topLeft = Offset(cx - r, cy - r),
                    size = Size(r * 2, r * 2),
                    style = Stroke(r * 0.18f)
                )
                if (style.motif == ChargingMotif.PULSE) {
                    drawCircle(
                        spark.copy(alpha = 0.30f),
                        r * (1.16f + 0.05f * kotlin.math.sin(phase * 6.283f)),
                        Offset(cx, cy),
                        style = Stroke(r * 0.05f)
                    )
                }
            }
            ChargingMotif.CAT -> {
                val head = Path().apply {
                    moveTo(cx, cy + r * 0.86f)
                    cubicTo(cx - r * 0.86f, cy + r * 0.86f, cx - r * 0.86f, cy - r * 0.30f, cx - r * 0.55f, cy - r * 0.42f)
                    lineTo(cx - r * 0.62f, cy - r * 0.90f)
                    lineTo(cx - r * 0.20f, cy - r * 0.58f)
                    lineTo(cx + r * 0.20f, cy - r * 0.58f)
                    lineTo(cx + r * 0.62f, cy - r * 0.90f)
                    lineTo(cx + r * 0.55f, cy - r * 0.42f)
                    cubicTo(cx + r * 0.86f, cy - r * 0.30f, cx + r * 0.86f, cy + r * 0.86f, cx, cy + r * 0.86f)
                    close()
                }
                drawPath(head, track.copy(alpha = 0.45f))
                clipRect(top = cy + r * 0.90f - r * 1.9f * level) {
                    drawPath(head, progress.copy(alpha = 0.55f))
                }
                val eyeOpen = (1f - level).coerceAtLeast(0.12f)
                drawOval(spark, Offset(cx - r * 0.34f, cy - r * 0.10f * eyeOpen), Size(r * 0.18f, r * 0.20f * eyeOpen))
                drawOval(spark, Offset(cx + r * 0.16f, cy - r * 0.10f * eyeOpen), Size(r * 0.18f, r * 0.20f * eyeOpen))
            }
            ChargingMotif.PETALS, ChargingMotif.AURORA, ChargingMotif.WAVE,
            ChargingMotif.LANTERN, ChargingMotif.PITCH, ChargingMotif.MOON,
            ChargingMotif.FILL, ChargingMotif.SPARK, ChargingMotif.PAPER -> {
                // Shared fallback: a segmented ring plus the level label.
                drawCircle(track.copy(alpha = 0.55f), r, Offset(cx, cy), style = Stroke(r * 0.16f))
                drawArc(
                    if (style.motif == ChargingMotif.AURORA) spark else progress,
                    -90f, 359.9f * level, false,
                    topLeft = Offset(cx - r, cy - r),
                    size = Size(r * 2, r * 2),
                    style = Stroke(r * 0.16f)
                )
                val wobble = 0.06f * kotlin.math.sin(phase * 6.283f)
                drawCircle(
                    progress.copy(alpha = 0.22f + wobble),
                    r * 0.62f,
                    Offset(cx, cy)
                )
            }
        }
    }
}
