package com.zamcan.faceit.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zamcan.faceit.keyboard.KeyGeometry
import com.zamcan.faceit.keyboard.KeyMaterial
import com.zamcan.faceit.keyboard.KeyboardDesign
import com.zamcan.faceit.keyboard.KeyboardDesignCatalogue
import com.zamcan.faceit.keyboard.KeyboardDesignEngine
import com.zamcan.faceit.keyboard.KeyboardDesignPreferences
import com.zamcan.faceit.keyboard.KeyboardFont
import com.zamcan.faceit.ui.theme.LocalFaceItColors

/**
 * Keyboard Designs gallery.
 *
 * Browse, compare, favourite and select Face It keyboard visual presets, then
 * try an isolated demonstration. Nothing here types into another app: the demo
 * keys are local Compose surfaces with no text-input connection, no clipboard
 * access and no persistence, which is what AGENTS.md §10 requires.
 */

/** Palette values are stored as 0xRRGGBB, so alpha must be forced opaque. */
internal fun Long.asColor(): Color = Color(0xFF000000L or this)

@Composable
internal fun KeyboardDesignGalleryScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val context = LocalContext.current
    val colors = LocalFaceItColors.current
    val preferences = remember { KeyboardDesignPreferences(context) }
    LaunchedEffect(Unit) { preferences.migrateLegacyAppearance() }

    var selectedId by remember { mutableStateOf(preferences.selectedDesignId()) }
    var favourites by remember { mutableStateOf(preferences.favourites()) }
    var familyFilter by remember { mutableStateOf<String?>(null) }
    var showDemo by remember { mutableStateOf<KeyboardDesign?>(null) }

    val visible = KeyboardDesignCatalogue.byFamily(familyFilter)

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ScreenHeader(
                    eyebrow = "Keyboard designs",
                    title = "Choose a key style",
                    description = "Visual presets for Face It. Selecting a design changes this app only.",
                    onBack = onBack
                )
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FamilyChip(
                        label = "All designs",
                        selected = familyFilter == null,
                        onClick = { familyFilter = null }
                    )
                }
                items(KeyboardDesignCatalogue.families.size) { index ->
                    val family = KeyboardDesignCatalogue.families[index]
                    FamilyChip(
                        label = family,
                        selected = familyFilter == family,
                        onClick = { familyFilter = if (familyFilter == family) null else family }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 236.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visible, key = { it.id }) { design ->
                    DesignCard(
                        design = design,
                        selected = design.id == selectedId,
                        favourite = favourites.contains(design.id),
                        onSelect = {
                            selectedId = design.id
                            preferences.select(design.id)
                        },
                        onFavourite = {
                            favourites = if (preferences.toggleFavourite(design.id)) {
                                favourites + design.id
                            } else {
                                favourites - design.id
                            }
                        },
                        onPreview = { showDemo = design }
                    )
                }
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "These are Face It visual presets. They do not change Gboard, Samsung Keyboard or any other keyboard on your phone, and Face It does not install a system keyboard.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Your selected design is saved on this device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.mint
                        )
                    }
                }
            }
        }

        showDemo?.let { design ->
            KeyboardDemoSheet(design = design, onClose = { showDemo = null })
        }
    }
}

@Composable
private fun FamilyChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalFaceItColors.current
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) colors.violet.copy(alpha = .16f) else colors.card,
        border = BorderStroke(
            1.dp,
            if (selected) colors.violet.copy(alpha = .5f) else colors.border
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.violet else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DesignCard(
    design: KeyboardDesign,
    selected: Boolean,
    favourite: Boolean,
    onSelect: () -> Unit,
    onFavourite: () -> Unit,
    onPreview: () -> Unit
) {
    val colors = LocalFaceItColors.current
    FaceItGlass(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "${design.name}, ${design.family} key design" },
        tone = if (selected) GlassTone.Accent else GlassTone.Standard,
        contentPadding = 14.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = design.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = design.family.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.3.sp),
                    color = colors.violet
                )
            }
            IconButton(onClick = onFavourite) {
                Icon(
                    imageVector = if (favourite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = if (favourite) {
                        "Remove ${design.name} from favourites"
                    } else {
                        "Add ${design.name} to favourites"
                    },
                    tint = if (favourite) colors.violet else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        KeyboardMiniPreview(design)

        if (!KeyboardDesignEngine.legendIsAccessible(design.palette)) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Legend contrast is below the accessibility threshold in this palette",
                style = MaterialTheme.typography.labelSmall,
                color = colors.warning
            )
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = design.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onSelect,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                enabled = !selected
            ) {
                Text(if (selected) "Selected" else "Select")
            }
            TextButton(onClick = onPreview) {
                Text("Try demo")
            }
        }
    }
}

/** Compact, non-interactive rendition used in the gallery grid. */
@Composable
private fun KeyboardMiniPreview(design: KeyboardDesign) {
    val palette = design.palette
    val rows = listOf("qwertyuiop", "asdfghjkl", "zxcvbnm")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.6f)
            .clip(RoundedCornerShape(16.dp))
            .background(palette.background.asColor())
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(verticalArrangement = Arrangement.spacedBy((design.keyGapDp / 2).dp)) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy((design.keyGapDp / 2).dp)
                ) {
                    row.forEach { letter ->
                        MiniKey(design = design, letter = letter)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.MiniKey(design: KeyboardDesign, letter: Char) {
    val palette = design.palette
    val reveal = keyReveal(design.geometry)
    val shape = RoundedCornerShape((design.cornerRadiusDp * 0.55f).dp)
    Box(
        modifier = Modifier
            .weight(1f)
            .height((design.keyHeightDp * 0.60f).dp)
            .shadow(
                elevation = if (design.material == KeyMaterial.SOFT_MATTE) 0.dp else 1.5.dp,
                shape = shape,
                clip = false
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(listOf(reveal.first, palette.keySurface.asColor(), reveal.second))
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter.toString(),
            color = palette.keyText.asColor(),
            fontSize = 7.sp,
            fontFamily = fontFamilyForDesign(design.font),
            fontWeight = FontWeight.Medium
        )
    }
}

/** Geometry-specific top highlight and bottom shadow for a keycap. */
internal fun keyReveal(geometry: KeyGeometry): Pair<Color, Color> = when (geometry) {
    KeyGeometry.SCULPTED_CAP -> Color.White.copy(alpha = .16f) to Color.Black.copy(alpha = .30f)
    KeyGeometry.PEBBLE -> Color.White.copy(alpha = .10f) to Color.Black.copy(alpha = .24f)
    KeyGeometry.SLAB -> Color.White.copy(alpha = .07f) to Color.Black.copy(alpha = .34f)
    KeyGeometry.KEYSTONE -> Color.White.copy(alpha = .20f) to Color.Black.copy(alpha = .36f)
    KeyGeometry.PILLOW -> Color.White.copy(alpha = .13f) to Color.Black.copy(alpha = .16f)
}

internal fun fontFamilyForDesign(font: KeyboardFont): FontFamily = when (font) {
    KeyboardFont.SERIF -> FontFamily.Serif
    KeyboardFont.MONOSPACE -> FontFamily.Monospace
    KeyboardFont.ROUNDED -> FontFamily.SansSerif
    KeyboardFont.SANS -> FontFamily.SansSerif
    KeyboardFont.SYSTEM -> FontFamily.Default
}

/**
 * Isolated pressable demonstration. There is deliberately no TextField, no
 * InputConnection and no clipboard access: tapped letters accumulate in a local
 * preview strip that is discarded when the demo closes.
 */
@Composable
private fun KeyboardDemoSheet(design: KeyboardDesign, onClose: () -> Unit) {
    val colors = LocalFaceItColors.current
    val motion = LocalMotionPolicy.current
    var buffer by remember { mutableStateOf("") }
    val palette = design.palette
    val swallow = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = .58f))
            .clickable(interactionSource = swallow, indication = null, onClick = onClose),
        contentAlignment = Alignment.BottomCenter
    ) {
        FaceItGlass(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* consume taps so the scrim does not close the sheet */ }
                .semantics { contentDescription = "Keyboard design demonstration" },
            tone = GlassTone.Raised,
            shape = RoundedCornerShape(28.dp),
            contentPadding = 16.dp
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = colors.mint,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "DEMO — TYPES NOWHERE",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
                    color = colors.mint
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Exit",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.violet,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onClose)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(design.name, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Isolated inside Face It. Nothing you tap reaches another app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (motion.staticOnly) {
                Text(
                    text = "Reduced motion: press animation is off (${motion.reason}).",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.warning
                )
            }

            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.background.asColor().copy(alpha = .92f))
                    .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = buffer.ifEmpty { "Tap the demo keys…" },
                    color = palette.keyText.asColor().copy(alpha = if (buffer.isEmpty()) .45f else 1f),
                    fontFamily = fontFamilyForDesign(design.font),
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(12.dp))
            DemoKeyboard(
                design = design,
                animate = motion.animationsEnabled,
                onKey = { buffer = (buffer + it).takeLast(28) }
            )

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { buffer = "" }, enabled = buffer.isNotEmpty()) {
                    Text("Clear")
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onClose) {
                    Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Close demo")
                }
            }
        }
    }
}

@Composable
private fun DemoKeyboard(
    design: KeyboardDesign,
    animate: Boolean,
    onKey: (String) -> Unit
) {
    val palette = design.palette
    val rows = listOf(
        "qwertyuiop".map { it.toString() },
        "asdfghjkl".map { it.toString() },
        listOf("⇧") + "zxcvbnm".map { it.toString() } + listOf("⌫")
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(palette.background.asColor())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(design.keyGapDp.dp)
    ) {
        rows.forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(design.keyGapDp.dp)) {
                if (rowIndex == 2) Spacer(Modifier.weight(.5f))
                row.forEach { key ->
                    val isSpecial = key.length > 1
                    DemoKey(
                        design = design,
                        label = key,
                        isSpecial = isSpecial,
                        animate = animate,
                        modifier = if (isSpecial) Modifier.weight(1.5f) else Modifier.weight(1f),
                        onClick = { if (!isSpecial) onKey(key) }
                    )
                }
                if (rowIndex == 2) Spacer(Modifier.weight(.5f))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(design.keyGapDp.dp)) {
            DemoKey(design, "123", true, animate, Modifier.weight(1.5f)) {}
            DemoKey(design, "space", true, animate, Modifier.weight(4f)) { onKey(" ") }
            DemoKey(design, "return", true, animate, Modifier.weight(1.9f)) {}
        }
    }
}

@Composable
private fun DemoKey(
    design: KeyboardDesign,
    label: String,
    isSpecial: Boolean,
    animate: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val palette = design.palette
    val reveal = keyReveal(design.geometry)
    val interaction = remember { MutableInteractionSource() }
    val density = LocalDensity.current
    val liftPx = with(density) { 1.dp.toPx() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && animate) .96f else 1f,
        label = "demoKeyScale"
    )
    val surface = if (isSpecial) {
        palette.accent.asColor().copy(alpha = .22f)
    } else {
        palette.keySurface.asColor()
    }
    val shape = when (design.geometry) {
        KeyGeometry.PEBBLE -> RoundedCornerShape(percent = 50)
        KeyGeometry.PILLOW -> RoundedCornerShape((design.cornerRadiusDp + 6).dp)
        else -> RoundedCornerShape(design.cornerRadiusDp.dp)
    }

    Box(
        modifier = modifier
            .height(design.keyHeightDp.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = if (pressed && animate) liftPx else 0f
            }
            .shadow(elevation = if (pressed) 1.dp else 4.dp, shape = shape, clip = false)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(reveal.first, surface, reveal.second)))
            .border(1.dp, Color.White.copy(alpha = .06f), shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSpecial) palette.accent.asColor() else palette.keyText.asColor(),
            fontFamily = fontFamilyForDesign(design.font),
            fontWeight = FontWeight.Medium,
            fontSize = if (label.length > 1) 11.sp else 17.sp,
            maxLines = 1
        )
    }
}

/** Tools-hub icon for this studio, kept beside the gallery for cohesion. */
internal val KeyboardDesignIcon = Icons.Rounded.Keyboard
