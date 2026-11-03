package com.zamcan.faceit.ui

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zamcan.faceit.ui.theme.LocalFaceItColors

internal enum class StudioCategory(val label: String) {
    THEMES("Themes"),
    WIDGETS("Widgets"),
    ICONS("Icons"),
    APPS("Apps"),
    KEYBOARDS("Keyboards"),
    CONTROLS("Controls"),
    CHARGING("Charging"),
    DISPLAY("Display")
}

internal data class StudioStyle(
    val id: String,
    val title: String,
    val category: StudioCategory,
    val detail: String,
    val accent: Long,
    val surface: Long,
    val capability: String
)

internal object PersonalisationCatalogue {
    val styles = listOf(
        StudioStyle("theme-pearl-bloom", "Pearl Bloom", StudioCategory.THEMES, "Warm pearl, petal accents and soft glass", 0xFFC77F9B, 0xFFF4E8E8, "Face It theme preset"),
        StudioStyle("theme-cat-garden", "Cat Garden", StudioCategory.THEMES, "Playful cats, botanical tones and bright details", 0xFFDA8C62, 0xFFF6E8D9, "Face It theme preset"),
        StudioStyle("theme-niqab-atelier", "Niqab Atelier", StudioCategory.THEMES, "A refined draped-frame visual language", 0xFFB68A9D, 0xFF2C202A, "Face It theme preset"),
        StudioStyle("theme-rose-glass", "Rose Glass", StudioCategory.THEMES, "Blush reflections over a translucent finish", 0xFFE6A3B7, 0xFFF7EAF0, "Face It theme preset"),
        StudioStyle("theme-obsidian", "Obsidian", StudioCategory.THEMES, "Quiet dark surfaces with a satin highlight", 0xFFB5A4D8, 0xFF24232D, "Face It theme preset"),

        StudioStyle("widget-obsidian", "Obsidian Glass", StudioCategory.WIDGETS, "Native clock widget finish", 0xFFA78BFA, 0xFF252832, "Installed clock widget"),
        StudioStyle("widget-ceramic", "Warm Ceramic", StudioCategory.WIDGETS, "Native clock widget finish", 0xFF9A7653, 0xFFE7E0D3, "Installed clock widget"),
        StudioStyle("widget-mint", "Pearl Mint", StudioCategory.WIDGETS, "Native clock widget finish", 0xFF76DCCB, 0xFF233737, "Installed clock widget"),

        StudioStyle("icon-petal", "Petal Icons", StudioCategory.ICONS, "Soft rounded tiles with floral colour", 0xFFC77F9B, 0xFFF4E8E8, "Icon concept preview"),
        StudioStyle("icon-cat", "Little Companions", StudioCategory.ICONS, "Cute animal-inspired app symbols", 0xFFDA8C62, 0xFFF6E8D9, "Icon concept preview"),
        StudioStyle("icon-crystal", "Crystal Line", StudioCategory.ICONS, "Precise luminous outlines", 0xFF9C8BE0, 0xFF242330, "Icon concept preview"),
        StudioStyle("icon-satin", "Satin Mono", StudioCategory.ICONS, "Quiet monochrome with tactile depth", 0xFFB8B0A5, 0xFF343238, "Icon concept preview"),

        StudioStyle("apps-niqab", "Niqab-inspired frame", StudioCategory.APPS, "A custom draped frame around an app-grid preview", 0xFFB68A9D, 0xFF2C202A, "In-app launcher concept"),
        StudioStyle("apps-petal", "Petal Home", StudioCategory.APPS, "A soft, colourful home-screen composition", 0xFFC77F9B, 0xFFF4E8E8, "In-app launcher concept"),
        StudioStyle("apps-cat", "Cat Garden", StudioCategory.APPS, "A playful animal-led app presentation", 0xFFDA8C62, 0xFFF6E8D9, "In-app launcher concept"),

        StudioStyle("keyboard-gallery", "Keyboard design gallery", StudioCategory.KEYBOARDS, "Choose and try the separate key-style designs", 0xFF9C8BE0, 0xFF242330, "In-app visual designs"),

        StudioStyle("controls-rose", "Rose Ceramic", StudioCategory.CONTROLS, "Warm ceramic tiles and clear control states", 0xFFC77F9B, 0xFFF4E8E8, "Control-panel concept"),
        StudioStyle("controls-glass", "Frosted Glass", StudioCategory.CONTROLS, "Translucent tiles with restrained highlights", 0xFF9ABDC1, 0xFF26373B, "Control-panel concept"),
        StudioStyle("controls-mint", "Pearl Mint", StudioCategory.CONTROLS, "Cool mint accents over deep stone", 0xFF76DCCB, 0xFF233737, "Control-panel concept"),

        StudioStyle("charge-aurora", "Aurora Pulse", StudioCategory.CHARGING, "A luminous, energetic charging ring", 0xFF8CCFC2, 0xFF17272B, "Charging animation preview"),
        StudioStyle("charge-rose", "Rose Bloom", StudioCategory.CHARGING, "A warm pulse with a soft bloom", 0xFFE6A3B7, 0xFF34232D, "Charging animation preview"),
        StudioStyle("charge-cat", "Little Spark", StudioCategory.CHARGING, "A playful charging motif for the cute collection", 0xFFFFB86B, 0xFF34251F, "Charging animation preview"),

        StudioStyle("display-veil", "Niqab-inspired display", StudioCategory.DISPLAY, "A customisable draped frame for a display preview", 0xFFB68A9D, 0xFF2C202A, "In-app display concept"),
        StudioStyle("display-edge", "Luminous Edge", StudioCategory.DISPLAY, "A subtle edge-light treatment", 0xFF9C8BE0, 0xFF242330, "In-app display concept"),
        StudioStyle("display-pearl", "Pearl Matte", StudioCategory.DISPLAY, "A calm, light material-led display", 0xFFC77F9B, 0xFFF4E8E8, "In-app display concept")
    )

    val frameColours = listOf(
        "Onyx" to 0xFF29232B,
        "Rose" to 0xFFB87991,
        "Plum" to 0xFF6E4B68,
        "Pearl" to 0xFFE9DDE0,
        "Sage" to 0xFF758D7C,
        "Sand" to 0xFFC5AE91
    )

    fun byCategory(category: StudioCategory) = styles.filter { it.category == category }
    fun find(id: String) = styles.firstOrNull { it.id == id }
}

internal class PersonalisationStudioPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("faceit_personalisation_studio", Context.MODE_PRIVATE)
    fun selected(category: StudioCategory): String? = prefs.getString("selected_${category.name}", null)
    fun select(category: StudioCategory, id: String) {
        require(PersonalisationCatalogue.find(id)?.category == category)
        prefs.edit().putString("selected_${category.name}", id).apply()
    }
    fun frameColour(): String = prefs.getString("frame_colour", "Onyx") ?: "Onyx"
    fun setFrameColour(name: String) {
        require(PersonalisationCatalogue.frameColours.any { it.first == name })
        prefs.edit().putString("frame_colour", name).apply()
    }
}

@Composable
internal fun PersonalisationStudioScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onOpenKeyboardDesigns: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { PersonalisationStudioPreferences(context) }
    val colors = LocalFaceItColors.current
    var category by remember { mutableStateOf(StudioCategory.THEMES) }
    var selectedIds by remember {
        mutableStateOf(StudioCategory.entries.associateWith { prefs.selected(it) })
    }
    var frameColour by remember { mutableStateOf(prefs.frameColour()) }
    var pulse by remember { mutableStateOf(false) }
    val currentStyles = PersonalisationCatalogue.byCategory(category)
    val selected = PersonalisationCatalogue.find(selectedIds[category] ?: "")
        ?: currentStyles.first()
    val isFrame = selected.id == "apps-niqab" || selected.id == "display-veil" || selected.id == "theme-niqab-atelier"
    val frameHex = PersonalisationCatalogue.frameColours.first { it.first == frameColour }.second
    val selectedAccent = Color(selected.accent)
    val pulseProgress by animateFloatAsState(
        targetValue = if (pulse) 1f else 0f,
        animationSpec = tween(durationMillis = 850),
        label = "charging-preview"
    )

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("PERSONALISE YOUR WORLD", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp), color = colors.violet)
                Text("Style studio", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            }
            Surface(onClick = onBack, shape = CircleShape, color = colors.card, border = BorderStroke(1.dp, colors.border)) {
                Text("Done", Modifier.padding(horizontal = 15.dp, vertical = 10.dp), style = MaterialTheme.typography.labelLarge)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            StudioCategory.entries.forEach { item ->
                val active = category == item
                Surface(
                    onClick = { category = item },
                    shape = CircleShape,
                    color = if (active) colors.violet.copy(alpha = .17f) else colors.card,
                    border = BorderStroke(1.dp, if (active) colors.violet.copy(alpha = .55f) else colors.border)
                ) {
                    Text(item.label, Modifier.padding(horizontal = 13.dp, vertical = 9.dp), style = MaterialTheme.typography.labelMedium, color = if (active) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(selected.title, style = MaterialTheme.typography.titleLarge)
                Text(selected.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Surface(shape = CircleShape, color = colors.cardRaised, border = BorderStroke(1.dp, colors.border)) {
                Text(selected.capability.uppercase(), Modifier.padding(horizontal = 9.dp, vertical = 7.dp), style = MaterialTheme.typography.labelSmall, color = colors.mint)
            }
        }

        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            StudioDevicePreview(
                style = selected,
                frameColour = Color(frameHex),
                chargingProgress = pulseProgress,
                onChargingPulse = { pulse = !pulse }
            )
        }

        if (isFrame) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("FRAME COLOUR", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(13.dp), verticalAlignment = Alignment.CenterVertically) {
                    PersonalisationCatalogue.frameColours.forEach { (name, value) ->
                        val active = frameColour == name
                        Surface(
                            onClick = { frameColour = name; prefs.setFrameColour(name) },
                            shape = CircleShape,
                            color = Color(value),
                            modifier = Modifier.size(if (active) 36.dp else 30.dp),
                            border = BorderStroke(if (active) 2.dp else 1.dp, if (active) colors.violet else colors.border)
                        ) {
                            if (active) Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Check, contentDescription = name, tint = if (name == "Pearl" || name == "Sand") Color.DarkGray else Color.White, modifier = Modifier.size(17.dp))
                            }
                        }
                    }
                    Text(frameColour, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (category == StudioCategory.KEYBOARDS) {
            Button(onClick = onOpenKeyboardDesigns, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Rounded.Keyboard, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Open keyboard designs")
            }
        } else if (category == StudioCategory.CHARGING) {
            OutlinedButton(onClick = { pulse = !pulse }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Rounded.BatteryChargingFull, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Preview charging pulse")
            }
        } else {
            Button(
                onClick = {
                    prefs.select(category, selected.id)
                    selectedIds = selectedIds + (category to selected.id)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (selectedIds[category] == selected.id) "Selected on this device" else "Save this design")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            currentStyles.forEach { style ->
                val active = selected.id == style.id
                Surface(
                    onClick = {
                        selectedIds = selectedIds + (category to style.id)
                        if (category != StudioCategory.KEYBOARDS) prefs.select(category, style.id)
                        if (style.id == "apps-niqab" || style.id == "display-veil" || style.id == "theme-niqab-atelier") {
                            prefs.setFrameColour(frameColour)
                        }
                    },
                    shape = RoundedCornerShape(15.dp),
                    color = if (active) colors.violet.copy(alpha = .13f) else colors.card,
                    border = BorderStroke(if (active) 1.5.dp else 1.dp, if (active) colors.violet.copy(alpha = .65f) else colors.border)
                ) {
                    Column(Modifier.width(112.dp).padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Box(
                            Modifier.fillMaxWidth().height(42.dp).clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(Color(style.accent), Color(style.surface)))),
                            contentAlignment = Alignment.Center
                        ) {
                            if (active) Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White)
                        }
                        Text(style.title, style = MaterialTheme.typography.labelMedium, maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }

        Text(
            when (category) {
                StudioCategory.WIDGETS -> "The three clock finishes below are connected to the installed Face It clock widget. Android controls widget placement and resizing."
                StudioCategory.KEYBOARDS -> "These are selectable visual designs and isolated demos inside Face It. They do not replace your phone keyboard."
                StudioCategory.ICONS, StudioCategory.APPS, StudioCategory.CONTROLS, StudioCategory.CHARGING, StudioCategory.DISPLAY -> "This is an in-app design preview. Android launchers and system UI control whether third-party icon packs, app grids, quick controls or charging screens can be applied."
                StudioCategory.THEMES -> "Saved here as a Face It style preset. It does not silently replace Android's system theme."
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StudioDevicePreview(
    style: StudioStyle,
    frameColour: Color,
    chargingProgress: Float,
    onChargingPulse: () -> Unit
) {
    val base = Color(style.surface)
    val accent = Color(style.accent)
    val darkInk = if (style.surface < 0xFF777777) Color(0xFFF8F4F7) else Color(0xFF342A31)
    val isNiqab = style.id == "apps-niqab" || style.id == "display-veil" || style.id == "theme-niqab-atelier"
    Surface(
        modifier = Modifier.width(224.dp).aspectRatio(.53f),
        shape = RoundedCornerShape(32.dp),
        color = base,
        border = BorderStroke(2.dp, accent.copy(alpha = .65f)),
        shadowElevation = 18.dp
    ) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(base, base.copy(alpha = .88f))))) {
            when (style.category) {
                StudioCategory.CHARGING -> ChargingDisplay(accent, base, darkInk, chargingProgress, onChargingPulse)
                StudioCategory.CONTROLS -> ControlPanelPreview(accent, base, darkInk)
                StudioCategory.WIDGETS -> WidgetPreview(accent, base, darkInk, style.title)
                StudioCategory.KEYBOARDS -> KeyboardPreview(accent, base, darkInk)
                StudioCategory.ICONS -> IconGridPreview(accent, base, darkInk, style.id)
                else -> {
                    Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("9:41", color = darkInk, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.weight(1f))
                            Text("●  ●  ▰", color = darkInk.copy(alpha = .7f), style = MaterialTheme.typography.labelSmall)
                        }
                        if (style.category == StudioCategory.THEMES || style.category == StudioCategory.APPS || style.category == StudioCategory.DISPLAY) {
                            Text("Personal space", color = darkInk, style = MaterialTheme.typography.titleMedium)
                            Surface(shape = RoundedCornerShape(18.dp), color = accent.copy(alpha = .22f), border = BorderStroke(1.dp, accent.copy(alpha = .45f))) {
                                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                    Text("Today", color = darkInk.copy(alpha = .75f), style = MaterialTheme.typography.labelSmall)
                                    Text("A little more you.", color = darkInk, style = MaterialTheme.typography.titleSmall)
                                    Text("12°  ·  Clear skies", color = darkInk.copy(alpha = .72f), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            AppGrid(accent, darkInk, style.id)
                        }
                    }
                    if (isNiqab) {
                        Canvas(Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val veil = Path().apply {
                                moveTo(0f, 0f)
                                lineTo(w, 0f)
                                lineTo(w, h * .27f)
                                cubicTo(w * .78f, h * .21f, w * .72f, h * .13f, w * .5f, h * .17f)
                                cubicTo(w * .28f, h * .13f, w * .22f, h * .21f, 0f, h * .27f)
                                close()
                            }
                            drawPath(veil, frameColour.copy(alpha = .94f))
                            drawPath(veil, Color.White.copy(alpha = .24f), style = Stroke(width = 1.2.dp.toPx()))
                            drawRoundRect(frameColour.copy(alpha = .68f), topLeft = androidx.compose.ui.geometry.Offset(0f, h * .18f), size = androidx.compose.ui.geometry.Size(w * .12f, h * .7f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .08f))
                            drawRoundRect(frameColour.copy(alpha = .68f), topLeft = androidx.compose.ui.geometry.Offset(w * .88f, h * .18f), size = androidx.compose.ui.geometry.Size(w * .12f, h * .7f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .08f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppGrid(accent: Color, ink: Color, styleId: String) {
    val iconBackgrounds = listOf(accent, Color(0xFF9FC5B4), Color(0xFFE8C0C8), Color(0xFFC5B7E4))
    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
        repeat(2) { row ->
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                repeat(4) { column ->
                    val index = row * 4 + column
                    val bg = if (styleId == "apps-cat") iconBackgrounds[(index + 1) % iconBackgrounds.size] else iconBackgrounds[index % iconBackgrounds.size]
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(shape = RoundedCornerShape(if (styleId == "apps-niqab") 15.dp else 13.dp), color = bg, modifier = Modifier.size(32.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = .35f))) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when (index % 4) {
                                        0 -> Icons.Rounded.Apps
                                        1 -> if (styleId == "apps-cat") Icons.Rounded.Pets else Icons.Rounded.Collections
                                        2 -> Icons.Rounded.Favorite
                                        else -> Icons.Rounded.Settings
                                    },
                                    contentDescription = null,
                                    tint = Color(0xFF352D38),
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                        Text(listOf("Photos", "Friends", "Notes", "More")[index % 4], color = ink.copy(alpha = .8f), style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp), maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun IconGridPreview(accent: Color, base: Color, ink: Color, styleId: String) {
    Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Icon collection", color = ink, style = MaterialTheme.typography.titleMedium)
        AppGrid(accent, ink, styleId)
        Text("Colour · shape · finish", color = ink.copy(alpha = .7f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun WidgetPreview(accent: Color, base: Color, ink: Color, title: String) {
    Column(Modifier.fillMaxSize().padding(15.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Clock widget", color = ink, style = MaterialTheme.typography.titleMedium)
        Surface(shape = RoundedCornerShape(18.dp), color = accent.copy(alpha = .18f), border = BorderStroke(1.dp, accent.copy(alpha = .65f))) {
            Column(Modifier.fillMaxWidth().padding(14.dp)) {
                Text("MONDAY, 03 NOV", color = ink.copy(alpha = .7f), style = MaterialTheme.typography.labelSmall)
                Text("09:41", color = ink, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Light)
                Text(title, color = accent, style = MaterialTheme.typography.labelSmall)
            }
        }
        Text("Add from your Android widget picker", color = ink.copy(alpha = .75f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ControlPanelPreview(accent: Color, base: Color, ink: Color) {
    Column(Modifier.fillMaxSize().padding(15.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        Text("Quick controls", color = ink, style = MaterialTheme.typography.titleMedium)
        listOf("Wi-Fi", "Bluetooth", "Torch", "Do not disturb").forEachIndexed { index, label ->
            Surface(shape = RoundedCornerShape(15.dp), color = if (index < 2) accent.copy(alpha = .22f) else ink.copy(alpha = .08f), border = BorderStroke(1.dp, accent.copy(alpha = .32f))) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (index == 2) Icons.Rounded.FlashOn else Icons.Rounded.Tune, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(label, color = ink, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun KeyboardPreview(accent: Color, base: Color, ink: Color) {
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Key appearance", color = ink, style = MaterialTheme.typography.titleMedium)
        listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM").forEach { line ->
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                line.forEach { key ->
                    Surface(shape = RoundedCornerShape(6.dp), color = accent.copy(alpha = .24f), border = BorderStroke(1.dp, accent.copy(alpha = .45f))) {
                        Text(key.toString(), Modifier.padding(horizontal = 5.dp, vertical = 9.dp), color = ink, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        Text("In-app visual preview", color = ink.copy(alpha = .7f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ChargingDisplay(accent: Color, base: Color, ink: Color, progress: Float, onPulse: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("POWERING UP", color = ink.copy(alpha = .7f), style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.6.sp))
        Spacer(Modifier.height(24.dp))
        Box(Modifier.size(132.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(accent.copy(alpha = .13f + progress * .16f), radius = size.minDimension * (.45f + progress * .025f))
                drawArc(accent.copy(alpha = .24f), -90f, 360f, false, style = Stroke(12.dp.toPx()))
                drawArc(accent, -90f, 270f + progress * 90f, false, style = Stroke(12.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.BatteryChargingFull, contentDescription = null, tint = accent, modifier = Modifier.size(30.dp))
                Text("86%", color = ink, style = MaterialTheme.typography.headlineMedium)
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Charging beautifully", color = ink, style = MaterialTheme.typography.titleSmall)
        Text("A custom visual concept", color = ink.copy(alpha = .7f), style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(22.dp))
        OutlinedButton(onClick = onPulse, border = BorderStroke(1.dp, accent), colors = ButtonDefaults.outlinedButtonColors(contentColor = ink)) {
            Icon(Icons.Rounded.FlashOn, contentDescription = null)
            Spacer(Modifier.width(7.dp))
            Text("Pulse preview")
        }
    }
}
