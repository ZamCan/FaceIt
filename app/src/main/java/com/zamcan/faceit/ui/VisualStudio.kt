package com.zamcan.faceit.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.zamcan.faceit.charging.ChargingMotif
import com.zamcan.faceit.charging.ChargingStyles
import com.zamcan.faceit.graphics.IconGlyph
import com.zamcan.faceit.keypad.KeypadDesigns
import com.zamcan.faceit.text.WritingStyles
import com.zamcan.faceit.themes.FaceTheme
import com.zamcan.faceit.themes.FaceThemes
import com.zamcan.faceit.themes.FrameColours
import com.zamcan.faceit.themes.VeilStyle
import com.zamcan.faceit.ui.theme.LocalFaceItColors
import com.zamcan.faceit.ui.LocalMotionPolicy
import com.zamcan.faceit.widget.FaceItClockWidgetStyles
import com.zamcan.faceit.launcher.FaceItHomeActivity
import kotlinx.coroutines.delay

/**
 * Visual studio — the single home for every look in Face It.
 *
 * Eleven categories, one shared [StudioStyle] model, one device preview. The
 * catalogue is composed from the real engines rather than hand-written cards:
 * themes come from [FaceThemes], icons from [IconGlyph], keypads from
 * [KeypadDesigns], charging from [ChargingStyles], writing from [WritingStyles]
 * and the clock widget from [FaceItClockWidgetStyles]. That means anything the
 * user can select here is a thing the app can genuinely draw — the preview is not
 * a mock-up of a feature, it is the feature at phone scale.
 *
 * Capability labels are deliberately blunt. Face It applies its own surfaces and
 * can set a wallpaper; it cannot reskin Android's icon pack, quick settings or
 * charging screen, and the copy says so on every category that would imply it.
 */

internal enum class StudioCategory(val label: String, val blurb: String) {
    THEMES("Themes", "Complete looks: surfaces, icons, keypad, widget and charge, together."),
    FEMININE("Feminine", "Colour, cats, flowers, jewellery and pearl — one creative universe, never an assumption."),
    ICONS("Icons", "Draughted app symbols. Generic forms, no real app's artwork."),
    APPS("Apps", "How the app grid is framed and laid out."),
    WIDGETS("Widgets", "The finishes the real home-screen clock widget supports."),
    KEYBOARDS("Keyboards", "Visual key designs and demos. Face It is not a system keyboard."),
    KEYPAD("Keypad", "Number-pad appearance. A design, not a dialler."),
    CONTROLS("Controls", "Control-panel appearance concepts."),
    CHARGING("Charging", "Charging animations, previewed in-app."),
    DISPLAY("Display", "Screen treatments, including the draped frame."),
    WRITING("Writing", "Message and caption typography.");

    val group: String
        get() = when (this) {
            THEMES, FEMININE -> "Look"
            ICONS, APPS -> "Home"
            WIDGETS, KEYPAD -> "Furniture"
            KEYBOARDS, WRITING -> "Text"
            CONTROLS, CHARGING, DISPLAY -> "System"
        }
}

/**
 * One selectable style. Every field is derived from a real engine, so a card can
 * never advertise a colour the renderer cannot produce.
 */
internal data class StudioStyle(
    val id: String,
    val title: String,
    val category: StudioCategory,
    val detail: String,
    val accent: Long,
    val surface: Long,
    val ink: Long,
    val capability: String,
    /** Which glyph the card and the preview show, when the category draws icons. */
    val glyph: IconGlyph = IconGlyph.HOME,
    /** True where the product can actually apply it outside a preview. */
    val appliesForReal: Boolean = false,
    /** Set on Display cards: the drape shape that card previews. */
    val veilShape: VeilStyle? = null
)

internal object TopicCatalogue {

    private fun themeStyles(theme: FaceTheme) = StudioStyle(
        id = theme.id,
        title = theme.name,
        // Every theme appears in Themes; the feminine looks also appear in their own
        // collection, because a shared user should be able to find them either way.
        category = StudioCategory.THEMES,
        detail = theme.description,
        accent = theme.accent,
        surface = theme.surface,
        ink = theme.textPrimary,
        capability = "Face It theme · in-app surfaces",
        glyph = FaceThemes.showcaseGlyphs.first(),
        appliesForReal = false
    )

    /** Icons are generated per theme, so every theme contributes a full 34-glyph set. */
    private fun iconStyles(theme: FaceTheme) = listOf(
        StudioStyle(
            id = "icons-${theme.id}",
            title = "${theme.name} icons",
            category = StudioCategory.ICONS,
            detail = "34 glyphs on ${theme.iconTile.name.lowercase().replace('_', ' ')} tiles",
            accent = theme.accent,
            surface = theme.iconTileColor,
            ink = theme.iconGlyphColor,
            capability = "In-app icon family",
            glyph = IconGlyph.CAT,
            appliesForReal = false
        )
    )

    private fun appStyles(theme: FaceTheme) = StudioStyle(
        id = "apps-${theme.id}",
        title = "${theme.name} grid",
        category = StudioCategory.APPS,
        detail = if (theme.veilStyle == VeilStyle.NONE) {
            "Clean grid, no frame"
        } else {
            "Grid inside a ${theme.veilStyle.name.lowercase().replace('_', ' ')} frame"
        },
        accent = theme.accent,
        surface = theme.surface,
        ink = theme.textPrimary,
        capability = "Optional Android Home launcher",
        glyph = IconGlyph.HOME,
        appliesForReal = false
    )

    private val widgetStyles = FaceItClockWidgetStyles.all.map { style ->
        StudioStyle(
            id = "widget-${style.id}",
            title = style.label,
            category = StudioCategory.WIDGETS,
            detail = "${style.family} finish · ${(style.timeContrast * 10).toInt() / 10.0}:1 time contrast",
            accent = style.date.toLong() and 0xFFFFFFFFL,
            surface = style.background.toLong() and 0xFFFFFFFFL,
            ink = style.time.toLong() and 0xFFFFFFFFL,
            capability = "Installed clock widget · real",
            glyph = IconGlyph.CLOCK,
            appliesForReal = true
        )
    }

    private val keypadStyles = KeypadDesigns.all.map { pad ->
        StudioStyle(
            id = "keypad-${pad.id}",
            title = pad.name,
            category = StudioCategory.KEYPAD,
            detail = pad.description,
            accent = pad.accent,
            surface = pad.boardBackground,
            ink = pad.legend,
            capability = "In-app keypad design",
            glyph = IconGlyph.PHONE,
            appliesForReal = false
        )
    }

    private val keyboardStyles = com.zamcan.faceit.keyboard.KeyboardDesignCatalogue.all.map { design ->
        StudioStyle(
            id = "keyboard-${design.id}",
            title = design.name,
            category = StudioCategory.KEYBOARDS,
            detail = design.description,
            accent = 0xFF000000L or design.highlight.toLong(),
            surface = 0xFF1B1B22,
            ink = 0xFFF6F4FA,
            capability = "In-app visual design",
            glyph = IconGlyph.SEARCH,
            appliesForReal = false
        )
    }

    /** The feminine collection: the Ladies and Cute families, as their own shelf. */
    private val feminineStyles = FaceThemes.all
        .filter { it.family == "Ladies" || it.family == "Cute" }
        .map { theme ->
            themeStyles(theme).copy(
                id = "feminine-${theme.id}",
                category = StudioCategory.FEMININE,
                capability = "Face It theme · in-app surfaces"
            )
        }

    private val chargingStyles = ChargingStyles.all.map { charge ->
        StudioStyle(
            id = "charge-${charge.id}",
            title = charge.label,
            category = StudioCategory.CHARGING,
            detail = charge.description,
            accent = charge.progressColor,
            surface = charge.trackColor,
            ink = 0xFFF6F2EE,
            capability = "In-app charging preview",
            glyph = IconGlyph.CHARGE,
            appliesForReal = false
        )
    }

    private val displayStyles: List<StudioStyle> = buildList {
        add(
            StudioStyle(
                id = "display-none",
                title = "No frame",
                category = StudioCategory.DISPLAY,
                detail = "The plain grid. No drape, no border, nothing over the icons.",
                accent = 0xFF6C6C7A,
                surface = 0xFF1C1C22,
                ink = 0xFFF4F2F8,
                capability = "In-app display concept",
                glyph = IconGlyph.HOME,
                veilShape = VeilStyle.NONE
            )
        )
        listOf(
            VeilStyle.SOFT_DRAPE to ("Soft drape" to "Curtains of shading down both sides, opening in the middle."),
            VeilStyle.VEIL_FRAME to ("Veil frame" to "A band across the top and a rounded border around the grid."),
            VeilStyle.LAYERED_FOLD to ("Layered fold" to "Three folded bands, each catching a little more light."),
            VeilStyle.OPEN_ARCH to ("Open arch" to "A single arch over the grid with one lit point at its crown."),
            VeilStyle.TWIN_CURVE to ("Twin curve" to "Two curves rising from the base, meeting under the dock."),
            VeilStyle.PLEAT to ("Pleat" to "Even folds of shading down both edges, fading before the labels."),
            VeilStyle.CORNER_TAB to ("Corner tab" to "Four brackets holding the grid; every edge centre stays clear."),
            VeilStyle.DOCK_BAND to ("Dock band" to "A soft band behind the dock with one thin lit rule above it."),
            VeilStyle.RIBBON to ("Ribbon" to "A single diagonal ribbon of colour across the icons.")
        ).forEach { (shape, copy) ->
            add(
                StudioStyle(
                    id = "display-${shape.name.lowercase()}",
                    title = copy.first,
                    category = StudioCategory.DISPLAY,
                    detail = copy.second,
                    accent = 0xFFB68A9D,
                    surface = 0xFF2C202A,
                    ink = 0xFFF6EEF2,
                    capability = "In-app display concept",
                    glyph = IconGlyph.HOME,
                    veilShape = shape
                )
            )
        }
    }

    private val controlStyles = listOf(
        StudioStyle(
            "controls-glass", "Frosted Glass", StudioCategory.CONTROLS,
            "Translucent tiles, restrained highlight, 12 dp corner",
            0xFF9ABDC1, 0xFF26373B, 0xFFF2FAFA, "Control-panel concept", IconGlyph.SETTINGS
        ),
        StudioStyle(
            "controls-rose", "Rose Ceramic", StudioCategory.CONTROLS,
            "Warm ceramic tiles with a clear pressed state",
            0xFFE6A3B7, 0xFFF6ECEF, 0xFF2B2026, "Control-panel concept", IconGlyph.SETTINGS
        ),
        StudioStyle(
            "controls-mint", "Mint Stone", StudioCategory.CONTROLS,
            "Cool mint accent over deep stone",
            0xFF76DCCB, 0xFF22332F, 0xFFF0FAF6, "Control-panel concept", IconGlyph.SETTINGS
        ),
        StudioStyle(
            "controls-onyx", "Onyx Edge", StudioCategory.CONTROLS,
            "Near-black tiles separated by a single lit edge",
            0xFF8F8AA8, 0xFF121115, 0xFFF4F2F8, "Control-panel concept", IconGlyph.SETTINGS
        ),
        StudioStyle(
            "controls-sand", "Sand Paper", StudioCategory.CONTROLS,
            "Matte sand tiles, no glow at all",
            0xFFC5AE91, 0xFFF2EBDF, 0xFF26221B, "Control-panel concept", IconGlyph.SETTINGS
        )
    )

    private val writingStyles = WritingStyles.all.map { writing ->
        StudioStyle(
            id = "writing-${writing.id}",
            title = writing.name,
            category = StudioCategory.WRITING,
            detail = "${writing.tone.label} · ${writing.surface.label.lowercase()}",
            accent = 0xFF8FA8C0,
            surface = 0xFF1B1B22,
            ink = 0xFFF4F2F8,
            capability = "Real typography · not look-alike text",
            glyph = IconGlyph.MESSAGE,
            appliesForReal = false
        )
    }

    val styles: List<StudioStyle> = run {
        val base = FaceThemes.all
        base.map(::themeStyles) +
            feminineStyles +
            base.flatMap(::iconStyles) +
            base.map(::appStyles) +
            keyboardStyles +
            displayStyles +
            widgetStyles +
            keypadStyles +
            chargingStyles +
            controlStyles +
            writingStyles
    }

    fun byCategory(category: StudioCategory): List<StudioStyle> =
        styles.filter { it.category == category }

    fun find(id: String?): StudioStyle? = styles.firstOrNull { it.id == id }

    fun themeFor(style: StudioStyle): FaceTheme? {
        val themeId = when (style.category) {
            StudioCategory.THEMES -> style.id
            StudioCategory.FEMININE -> style.id.removePrefix("feminine-")
            StudioCategory.ICONS -> style.id.removePrefix("icons-")
            StudioCategory.APPS -> style.id.removePrefix("apps-")
            else -> return null
        }
        return FaceThemes.find(themeId)
    }
}

/**
 * The charging canvas is the only continuously animated shelf in the studio.
 * Keep its eligibility and phase arithmetic deterministic so lifecycle and
 * reduced-motion regressions can be covered without an Android device.
 */
internal fun shouldAnimateChargingPreview(
    category: StudioCategory,
    isResumed: Boolean,
    animationsEnabled: Boolean
): Boolean = category == StudioCategory.CHARGING && isResumed && animationsEnabled

internal fun nextChargingPreviewPhase(current: Float): Float =
    (current + 0.018f) % 1f

/** Resolve the frame without overriding an explicit user choice of None. */
internal fun resolveStudioFrame(
    theme: FaceTheme,
    frameColourId: String,
    shapeOverride: VeilStyle?
): com.zamcan.faceit.themes.ResolvedFrame {
    val themeFrame = com.zamcan.faceit.themes.DisplayFrameResolver.resolve(frameColourId, theme)
    if (shapeOverride == null) return themeFrame
    return themeFrame.copy(
        style = if (frameColourId == FrameColours.NONE_ID) VeilStyle.NONE else shapeOverride
    )
}

/** A preview choice is still a user choice; only real widget instances own their own style. */
internal fun shouldPersistStudioSelection(style: StudioStyle): Boolean = !style.appliesForReal

/** Resolve stale or cross-category ids safely after catalogue changes or app upgrades. */
internal fun resolveSelectedStyleId(category: StudioCategory, storedId: String?): String =
    TopicCatalogue.find(storedId)
        ?.takeIf { it.category == category }
        ?.id
        ?: TopicCatalogue.byCategory(category).first().id

/** Persisted studio choices: one selected style per category, plus the frame colour. */
internal class StudioPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("faceit_personalisation_studio", Context.MODE_PRIVATE)

    fun selected(category: StudioCategory): String? = prefs.getString("selected_${category.name}", null)

    fun select(category: StudioCategory, id: String) {
        require(TopicCatalogue.find(id)?.category == category) {
            "$id is not a ${category.name} style"
        }
        prefs.edit().putString("selected_${category.name}", id).apply()
    }

    fun frameColour(): String = prefs.getString("frame_colour", FrameColours.NONE_ID) ?: FrameColours.NONE_ID

    fun setFrameColour(name: String) {
        require(name == FrameColours.NONE_ID || FrameColours.find(name) != null) { "unknown frame colour $name" }
        prefs.edit().putString("frame_colour", name).apply()
    }
}

@Composable
internal fun VisualStudioScreen(
    selectedThemeId: String?,
    appliedStyleId: (StudioCategory) -> String?,
    onStyleChosen: (StudioStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { StudioPreferences(context) }
    val colors = LocalFaceItColors.current
    var category by remember { mutableStateOf(StudioCategory.THEMES) }
    var selectedId by remember(category) {
        mutableStateOf(resolveSelectedStyleId(category, prefs.selected(category)))
    }
    var frameColour by remember { mutableStateOf(prefs.frameColour()) }
    var chargingPhase by remember { mutableFloatStateOf(0f) }

    val motionPolicy = LocalMotionPolicy.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var studioResumed by remember(lifecycleOwner) {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> studioResumed = true
                Lifecycle.Event.ON_PAUSE,
                Lifecycle.Event.ON_STOP,
                Lifecycle.Event.ON_DESTROY -> studioResumed = false
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val styles = remember(category) { TopicCatalogue.byCategory(category) }
    val selected = TopicCatalogue.find(selectedId) ?: styles.first()
    val theme = remember(selected.id) { TopicCatalogue.themeFor(selected) } ?: FaceThemes.find(selectedThemeId)

    LaunchedEffect(category, studioResumed, motionPolicy.animationsEnabled) {
        // Animate only while this screen is resumed and Android's motion policy
        // allows it. Leaving the app, opening another screen, battery saver,
        // reduced system animation or thermal pressure suspends the ticker.
        if (shouldAnimateChargingPreview(category, studioResumed, motionPolicy.animationsEnabled)) {
            while (true) {
                delay(70)
                chargingPhase = nextChargingPreviewPhase(chargingPhase)
            }
        } else {
            chargingPhase = 0f
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Text(
                "ONE STUDIO",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.6.sp),
                color = colors.violet
            )
            Text(
                "Style studio",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                selected.category.blurb,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(14.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(StudioCategory.entries, key = { it.name }) { entry ->
                CategoryChip(entry.label, entry == category) { category = entry }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---------------------------------------------------------- device preview
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            DeviceCanvas(
                style = selected,
                theme = theme,
                frameColourId = frameColour,
                chargingPhase = chargingPhase,
                onFrameTap = {
                    // Tapping the frame if it is on turns it off, and vice versa: the
                    // quickest possible way to answer "do I want this?".
                    frameColour = if (frameColour == FrameColours.NONE_ID) "Onyx" else FrameColours.NONE_ID
                    prefs.setFrameColour(frameColour)
                }
            )
        }

        Spacer(Modifier.height(14.dp))

        Column(Modifier.padding(horizontal = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(selected.title, style = MaterialTheme.typography.titleLarge)
                    Text(
                        selected.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = if (selected.appliesForReal) colors.mint.copy(alpha = .16f) else colors.cardRaised,
                    border = BorderStroke(1.dp, if (selected.appliesForReal) colors.mint.copy(alpha = .5f) else colors.border)
                ) {
                    Text(
                        selected.capability.uppercase(),
                        Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected.appliesForReal) colors.mint else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (category == StudioCategory.DISPLAY || category == StudioCategory.APPS) {
                FrameBar(
                    current = frameColour,
                    onPick = {
                        frameColour = it
                        prefs.setFrameColour(it)
                    }
                )
                Spacer(Modifier.height(12.dp))
            }

            ApplyButton(
                label = when {
                    selected.appliesForReal -> "Add the widget to see this finish"
                    category == StudioCategory.CHARGING -> "Previewing the animation above"
                    category == StudioCategory.WRITING -> "Apply in the SMS composer"
                    category == StudioCategory.APPS -> "Open Face It Home"
                    else -> "Apply ${selected.title}"
                },
                enabled = !selected.appliesForReal && category != StudioCategory.CHARGING,
                alreadyApplied = appliedStyleId(category) == selected.id
            ) {
                prefs.select(category, selected.id)
                if (category == StudioCategory.APPS) {
                    context.startActivity(Intent(context, FaceItHomeActivity::class.java))
                } else {
                    onStyleChosen(selected)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "IN THIS COLLECTION · ${styles.size}",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            items(styles, key = { it.id }) { style ->
                StyleCard(
                    style = style,
                    active = style.id == selected.id,
                    onClick = {
                        selectedId = style.id
                        if (shouldPersistStudioSelection(style)) {
                            prefs.select(style.category, style.id)
                        }
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Column(Modifier.padding(horizontal = 20.dp)) {
            Text(
                when (category) {
                    StudioCategory.THEMES, StudioCategory.FEMININE ->
                        "Applied inside Face It: surfaces, icons, keypad, widget suggestion and charge animation. It does not recolour Android or other apps."
                    StudioCategory.WIDGETS ->
                        "These nine finishes are the ones the native Face It clock widget really supports. Pick one when you add a widget; changing it here does not alter a widget already on your home screen."
                    StudioCategory.KEYBOARDS ->
                        "Visual key designs and isolated demos. Face It never becomes your system keyboard."
                    StudioCategory.KEYPAD ->
                        "Appearance presets for a number pad. Face It does not place calls and does not replace your phone's dialler."
                    StudioCategory.WRITING ->
                        "Real font families, tracking, leading and slant — the words stay ordinary text, so they remain searchable and readable by screen readers."
                    StudioCategory.APPS ->
                        "Face It Home is an optional Android launcher. It becomes your Home screen only after you choose Set as Home and accept Android's role prompt. Open Home settings to switch back at any time."
                    StudioCategory.CONTROLS ->
                        "An in-app concept. Android's own quick settings are controlled by the system and cannot be reskinned by an ordinary app."
                    StudioCategory.CHARGING ->
                        "Previewed in-app. Android does not let an ordinary app replace the system charging screen."
                    StudioCategory.DISPLAY ->
                        "Screen treatment previews. Wallpaper application is the one display change Face It performs for real, and only when you ask."
                    StudioCategory.ICONS ->
                        "An in-app icon family preview. Android launchers control whether third-party icon packs can be applied."
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CategoryChip(label: String, active: Boolean, onClick: () -> Unit) {
    val colors = LocalFaceItColors.current
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (active) colors.violet.copy(alpha = .17f) else colors.card,
        border = BorderStroke(1.dp, if (active) colors.violet.copy(alpha = .55f) else colors.border)
    ) {
        Text(
            label,
            Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (active) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun ApplyButton(
    label: String,
    enabled: Boolean,
    alreadyApplied: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalFaceItColors.current
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        color = when {
            !enabled -> colors.cardRaised
            alreadyApplied -> colors.mint.copy(alpha = .18f)
            else -> colors.violet.copy(alpha = .20f)
        },
        border = BorderStroke(
            1.dp,
            when {
                !enabled -> colors.border
                alreadyApplied -> colors.mint.copy(alpha = .55f)
                else -> colors.violet.copy(alpha = .55f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (alreadyApplied) {
                Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                if (alreadyApplied) "Applied" else label,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FrameBar(current: String, onPick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "DRAPED FRAME",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FrameSwatch("None", 0x00000000, current == "None", onPick)
            FrameColours.all.forEach { colour ->
                FrameSwatch(colour.id, colour.argb, current == colour.id, onPick)
            }
        }
        Text(
            if (current == "None") {
                "Off. Turn it on to frame the app grid in a drape of your chosen colour."
            } else {
                "$current drape, drawn as shading behind the grid. Choose None to switch it off."
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FrameSwatch(id: String, argb: Long, active: Boolean, onPick: (String) -> Unit) {
    val colors = LocalFaceItColors.current
    val isNone = id == "None"
    Box(
        Modifier
            // A 48 dp target stays comfortable even when the visible swatch is smaller.
            .size(48.dp)
            .toggleable(value = active, role = Role.RadioButton) { onPick(id) }
            .semantics { contentDescription = if (isNone) "No frame" else "$id frame" },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .padding(4.dp)
                .size(if (active) 40.dp else 34.dp)
                .clip(CircleShape)
                .background(if (isNone) colors.card else Color(argb))
                .border(
                    if (active) 2.dp else 1.dp,
                    if (active) colors.violet else colors.border,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isNone) {
                Text("—", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else if (active) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = if (argb and 0xFFFFFF > 0xBBBBBB) Color(0xFF241D24) else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun StyleCard(style: StudioStyle, active: Boolean, onClick: () -> Unit) {
    val colors = LocalFaceItColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (active) colors.violet.copy(alpha = .13f) else colors.card,
        border = BorderStroke(
            if (active) 1.5.dp else 1.dp,
            if (active) colors.violet.copy(alpha = .65f) else colors.border
        )
    ) {
        Column(
            Modifier.width(116.dp).padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Brush.linearGradient(listOf(Color(style.accent), Color(style.surface)))),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.size(26.dp)) {
                    drawStudioGlyph(style.glyph, Color(style.ink), Color(style.accent))
                }
                if (active) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(15.dp)
                    )
                }
            }
            Text(
                style.title,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---------------------------------------------------------------- device canvas

/**
 * A phone-shaped preview. Every pixel inside it is drawn from the selected style,
 * so what you see is the renderer's actual output at reduced scale.
 */
@Composable
private fun DeviceCanvas(
    style: StudioStyle,
    theme: FaceTheme,
    frameColourId: String,
    chargingPhase: Float,
    onFrameTap: () -> Unit
) {
    val base = Color(style.surface)
    val accent = Color(style.accent)
    val ink = Color(style.ink)
    Surface(
        modifier = Modifier
            .width(196.dp)
            .aspectRatio(.50f),
        shape = RoundedCornerShape(34.dp),
        color = base,
        border = BorderStroke(2.dp, accent.copy(alpha = .55f)),
        shadowElevation = 18.dp
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(base, base.copy(alpha = .92f))))
        ) {
            when (style.category) {
                StudioCategory.CHARGING -> ChargingStage(
                    styleId = style.id,
                    accent = accent,
                    base = base,
                    ink = ink,
                    phase = chargingPhase
                )
                StudioCategory.WIDGETS -> WidgetStage(accent, base, ink, style.title)
                StudioCategory.KEYPAD -> KeypadStage(accent, base, ink)
                StudioCategory.KEYBOARDS -> KeyboardStage(accent, base, ink)
                StudioCategory.CONTROLS -> ControlsStage(accent, base, ink)
                StudioCategory.WRITING -> WritingStage(accent, base, ink, style)
                StudioCategory.ICONS -> IconsStage(accent, base, ink, theme)
                else -> HomeStage(
                    theme = theme,
                    accent = accent,
                    ink = ink,
                    frameColourId = frameColourId,
                    shapeOverride = style.veilShape,
                    onFrameTap = onFrameTap
                )
            }
        }
    }
}

@Composable
private fun HomeStage(
    theme: FaceTheme,
    accent: Color,
    ink: Color,
    frameColourId: String,
    shapeOverride: VeilStyle?,
    onFrameTap: () -> Unit
) {
    // A shape card selects geometry; the colour bar independently decides
    // whether that geometry is visible. "None" always means no frame.
    val resolved = resolveStudioFrame(theme, frameColourId, shapeOverride)
    Column(Modifier.fillMaxSize().padding(13.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("9:41", color = ink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("●●  ▮", color = ink.copy(alpha = .6f), fontSize = 8.sp)
        }
        Text("Personal space", color = ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = accent.copy(alpha = .20f),
            border = BorderStroke(1.dp, accent.copy(alpha = .40f))
        ) {
            Column(Modifier.fillMaxWidth().padding(11.dp)) {
                Text("Monday, 3 Nov", color = ink.copy(alpha = .7f), fontSize = 8.sp)
                Text("A little more you.", color = ink, fontSize = 11.sp)
                Text("12° · clear skies", color = ink.copy(alpha = .7f), fontSize = 8.sp)
            }
        }
        IconGrid(theme, ink)
        Spacer(Modifier.weight(1f))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { index ->
                Box(
                    Modifier
                        .size(if (index == 2) 30.dp else 24.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(accent.copy(alpha = if (index == 2) .34f else .16f))
                )
            }
        }
    }
    if (resolved.enabled) {
        DrapeFrame(
            style = resolved.style,
            colour = Color(resolved.colour),
            modifier = Modifier
                .fillMaxSize()
                .semantics { contentDescription = "Draped frame, tap to switch off" }
        )
    }
}

/**
 * The draped frame itself: pure geometry.
 *
 * Five shapes, each drawn from curves and arcs. No figure, no face, no garment.
 * The frame is translucent so the icons stay legible underneath it — a frame that
 * hid the content would be a mask, and a mask is the one thing this must not be.
 */
@Composable
internal fun DrapeFrame(style: VeilStyle, colour: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        when (style) {
            VeilStyle.NONE -> Unit
            VeilStyle.SOFT_DRAPE -> {
                val left = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w * 0.40f, 0f)
                    cubicTo(w * 0.18f, h * 0.20f, w * 0.32f, h * 0.44f, w * 0.04f, h * 0.60f)
                    lineTo(0f, h * 0.60f)
                    close()
                }
                drawPath(left, colour.copy(alpha = .26f))
                val right = Path().apply {
                    moveTo(w, 0f)
                    lineTo(w * 0.60f, 0f)
                    cubicTo(w * 0.82f, h * 0.20f, w * 0.68f, h * 0.44f, w * 0.96f, h * 0.60f)
                    lineTo(w, h * 0.60f)
                    close()
                }
                drawPath(right, colour.copy(alpha = .26f))
            }
            VeilStyle.VEIL_FRAME -> {
                val band = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w, 0f)
                    lineTo(w, h * 0.15f)
                    cubicTo(w * 0.78f, h * 0.02f, w * 0.22f, h * 0.02f, 0f, h * 0.15f)
                    close()
                }
                drawPath(band, colour.copy(alpha = .34f))
                drawRoundRect(
                    color = colour.copy(alpha = .34f),
                    topLeft = Offset(w * 0.035f, h * 0.13f),
                    size = Size(w * 0.93f, h * 0.80f),
                    cornerRadius = CornerRadius(w * 0.18f, w * 0.18f),
                    style = Stroke(width = w * 0.014f)
                )
            }
            VeilStyle.LAYERED_FOLD -> {
                listOf(0.08f to 0.16f, 0.19f to 0.22f, 0.30f to 0.28f).forEach { (start, alpha) ->
                    val fold = Path().apply {
                        moveTo(0f, h * start)
                        cubicTo(w * 0.34f, h * (start - .09f), w * 0.66f, h * (start + .11f), w, h * start)
                        lineTo(w, h * (start + .09f))
                        cubicTo(w * 0.66f, h * (start + .20f), w * 0.34f, h * start, 0f, h * (start + .09f))
                        close()
                    }
                    drawPath(fold, colour.copy(alpha = alpha))
                }
            }
            VeilStyle.OPEN_ARCH -> {
                val arch = Path().apply {
                    moveTo(w * 0.12f, h * 0.28f)
                    cubicTo(w * 0.12f, h * 0.05f, w * 0.88f, h * 0.05f, w * 0.88f, h * 0.28f)
                }
                drawPath(arch, colour.copy(alpha = .60f), style = Stroke(width = w * 0.020f))
                drawCircle(colour.copy(alpha = .34f), w * 0.024f, Offset(w * 0.5f, h * 0.070f))
                drawCircle(colour.copy(alpha = .20f), w * 0.05f, Offset(w * 0.5f, h * 0.070f))
            }
            VeilStyle.PLEAT -> {
                val pleats = 6
                val band = w * 0.20f
                repeat(pleats) { i ->
                    val alpha = 0.30f - i * 0.035f
                    val xLeft = i * (band / pleats) + w * 0.012f
                    val xRight = w - xLeft - w * 0.030f
                    drawRect(
                        colour.copy(alpha = alpha),
                        topLeft = Offset(xLeft, 0f),
                        size = Size(w * 0.022f, h * (0.86f - i * 0.05f))
                    )
                    drawRect(
                        colour.copy(alpha = alpha * 0.8f),
                        topLeft = Offset(xRight, h * (0.04f + i * 0.04f)),
                        size = Size(w * 0.022f, h * (0.82f - i * 0.05f))
                    )
                }
            }
            VeilStyle.CORNER_TAB -> {
                val inset = w * 0.045f
                val arm = w * 0.16f
                val thick = w * 0.016f
                val tint = colour.copy(alpha = .55f)
                // top-left, top-right, bottom-left, bottom-right, as two bars each
                listOf(
                    Offset(inset, inset) to Offset(1f, 1f),
                    Offset(w - inset, inset) to Offset(-1f, 1f),
                    Offset(inset, h - inset) to Offset(1f, -1f),
                    Offset(w - inset, h - inset) to Offset(-1f, -1f)
                ).forEach { (corner, dir) ->
                    drawLine(
                        tint, corner, Offset(corner.x + arm * dir.x, corner.y),
                        strokeWidth = thick, cap = StrokeCap.Round
                    )
                    drawLine(
                        tint, corner, Offset(corner.x, corner.y + arm * dir.y),
                        strokeWidth = thick, cap = StrokeCap.Round
                    )
                }
            }
            VeilStyle.DOCK_BAND -> {
                val band = Path().apply {
                    moveTo(0f, h * 0.80f)
                    cubicTo(w * 0.30f, h * 0.76f, w * 0.70f, h * 0.76f, w, h * 0.80f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(band, colour.copy(alpha = .30f))
                drawLine(
                    colour.copy(alpha = .55f),
                    Offset(w * 0.10f, h * 0.795f),
                    Offset(w * 0.90f, h * 0.795f),
                    strokeWidth = w * 0.006f,
                    cap = StrokeCap.Round
                )
            }
            VeilStyle.RIBBON -> {
                val ribbon = Path().apply {
                    moveTo(-w * 0.10f, h * 0.20f)
                    lineTo(w * 0.55f, -h * 0.05f)
                    lineTo(w * 1.10f, h * 0.72f)
                    lineTo(w * 0.45f, h * 1.05f)
                    close()
                }
                drawPath(ribbon, colour.copy(alpha = .18f))
                drawLine(
                    colour.copy(alpha = .40f),
                    Offset(w * 0.04f, h * 0.13f),
                    Offset(w * 0.96f, h * 0.83f),
                    strokeWidth = w * 0.008f,
                    cap = StrokeCap.Round
                )
            }
            VeilStyle.TWIN_CURVE -> {
                val left = Path().apply {
                    moveTo(0f, h)
                    cubicTo(w * 0.08f, h * 0.78f, w * 0.34f, h * 0.72f, w * 0.5f, h * 0.84f)
                    lineTo(w * 0.5f, h)
                    close()
                }
                drawPath(left, colour.copy(alpha = .32f))
                val right = Path().apply {
                    moveTo(w, h)
                    cubicTo(w * 0.92f, h * 0.78f, w * 0.66f, h * 0.72f, w * 0.5f, h * 0.84f)
                    lineTo(w * 0.5f, h)
                    close()
                }
                drawPath(right, colour.copy(alpha = .22f))
            }
        }
    }
}

// ---------------------------------------------------------------- stages
// Each stage draws one category at phone scale using the real catalogues.

/**
 * Glyph set for a theme's app grid.
 *
 * The families are not decorative: a Ladies or Cute theme shows the cat, flower,
 * heart and ring vocabulary; a Sport theme shows the pitch and running set; a
 * Heritage theme shows the arch, bank and lantern set. Same renderer, different
 * vocabulary, so forty themes do not all look like the same phone.
 */
@Composable
private fun glyphsFor(theme: FaceTheme): List<IconGlyph> = remember(theme.id) {
    when (theme.family) {
        "Ladies" -> listOf(
            IconGlyph.HEART, IconGlyph.FLOWER, IconGlyph.CAT, IconGlyph.RING,
            IconGlyph.GIFT, IconGlyph.CAMERA, IconGlyph.CHAT, IconGlyph.MUSIC
        )
        "Cute" -> listOf(
            IconGlyph.CAT, IconGlyph.PAW, IconGlyph.BUTTERFLY, IconGlyph.STAR,
            IconGlyph.HEART, IconGlyph.FOOD, IconGlyph.MUSIC, IconGlyph.CAMERA
        )
        "Sport" -> listOf(
            IconGlyph.GAME, IconGlyph.RUN, IconGlyph.BICYCLE, IconGlyph.WEIGHT,
            IconGlyph.TROPHY, IconGlyph.MEDAL, IconGlyph.TICKET, IconGlyph.MAP
        )
        "Heritage" -> listOf(
            IconGlyph.BANK, IconGlyph.BOOK, IconGlyph.MAP, IconGlyph.CLOCK,
            IconGlyph.COFFEE, IconGlyph.TOOLS, IconGlyph.MAIL, IconGlyph.GLOBE
        )
        "Nature" -> listOf(
            IconGlyph.LEAF, IconGlyph.TREE, IconGlyph.FLOWER, IconGlyph.SUN,
            IconGlyph.MOON, IconGlyph.WEATHER, IconGlyph.MAP, IconGlyph.BUTTERFLY
        )
        "Abstract" -> listOf(
            IconGlyph.STAR, IconGlyph.SEARCH, IconGlyph.HOME, IconGlyph.SETTINGS,
            IconGlyph.BROWSER, IconGlyph.CHARGE, IconGlyph.RULER, IconGlyph.BRUSH
        )
        "Calm" -> listOf(
            IconGlyph.LEAF, IconGlyph.MOON, IconGlyph.COFFEE, IconGlyph.BOOK,
            IconGlyph.CAMERA, IconGlyph.MUSIC, IconGlyph.WEATHER, IconGlyph.CLOCK
        )
        "Science" -> listOf(
            IconGlyph.PILL, IconGlyph.RULER, IconGlyph.NOTE, IconGlyph.PENCIL,
            IconGlyph.FILES, IconGlyph.SEARCH, IconGlyph.CLOCK, IconGlyph.CHARGE
        )
        "Technology" -> listOf(
            IconGlyph.CALCULATOR, IconGlyph.NOTE, IconGlyph.TOOLS, IconGlyph.LOCK,
            IconGlyph.BROWSER, IconGlyph.FILES, IconGlyph.SEARCH, IconGlyph.CHARGE
        )
        "Business" -> listOf(
            IconGlyph.BANK, IconGlyph.CALCULATOR, IconGlyph.NOTE, IconGlyph.PENCIL,
            IconGlyph.MAIL, IconGlyph.CLOCK, IconGlyph.GLOBE, IconGlyph.KEY
        )
        else -> listOf(
            IconGlyph.MESSAGE, IconGlyph.CAMERA, IconGlyph.MUSIC, IconGlyph.GALLERY,
            IconGlyph.CALENDAR, IconGlyph.MAIL, IconGlyph.BROWSER, IconGlyph.CLOCK
        )
    }
}

@Composable
private fun IconGrid(theme: FaceTheme, ink: Color, rows: Int = 2) {
    val glyphs = glyphsFor(theme)
    val tileColor = Color(theme.iconTileColor)
    val glyphColor = Color(theme.iconGlyphColor)
    val accent = Color(theme.accent)
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        repeat(rows) { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                repeat(4) { column ->
                    val glyph = glyphs[(row * 4 + column) % glyphs.size]
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .size(30.dp)
                                .clip(tileShape(theme))
                                .background(tileBrush(theme, tileColor)),
                            contentAlignment = Alignment.Center
                        ) {
                            // The finish is drawn, not just coloured: glass gets a
                            // top highlight, stone a lit edge, jewel a facet line.
                            // Without this all eight finishes look like the same
                            // rounded square with a different corner radius.
                            Canvas(Modifier.fillMaxSize()) {
                                drawTileFinish(theme.iconTile, tileColor, accent)
                            }
                            Canvas(Modifier.fillMaxSize().padding(7.dp)) {
                                drawStudioGlyph(glyph, glyphColor, accent)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun tileShape(theme: FaceTheme): androidx.compose.ui.graphics.Shape = when (theme.iconTile) {
    com.zamcan.faceit.graphics.IconTile.PEBBLE_CIRCLE -> CircleShape
    com.zamcan.faceit.graphics.IconTile.CERAMIC_SQUIRCLE -> RoundedCornerShape(16.dp)
    com.zamcan.faceit.graphics.IconTile.GLASS_ROUNDED -> RoundedCornerShape(13.dp)
    com.zamcan.faceit.graphics.IconTile.FROSTED_ROUNDED -> RoundedCornerShape(18.dp)
    com.zamcan.faceit.graphics.IconTile.STONE_ROUNDED -> RoundedCornerShape(10.dp)
    com.zamcan.faceit.graphics.IconTile.MATTE_SQUARE -> RoundedCornerShape(5.dp)
    com.zamcan.faceit.graphics.IconTile.JEWEL_OCTAGON -> RoundedCornerShape(4.dp)
    com.zamcan.faceit.graphics.IconTile.SATIN_ROUNDED -> RoundedCornerShape(20.dp)
    com.zamcan.faceit.graphics.IconTile.HEX_CUT -> RoundedCornerShape(6.dp)
    com.zamcan.faceit.graphics.IconTile.PILL_TALL -> RoundedCornerShape(percent = 50)
    com.zamcan.faceit.graphics.IconTile.ARCH_SOFT -> RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp)
    com.zamcan.faceit.graphics.IconTile.BRILLIANT -> RoundedCornerShape(2.dp)
}

/**
 * The tile's base surface. Flat finishes stay flat; the glass and satin finishes get
 * a directional wash so the tile catches light the way its name promises.
 */
private fun tileBrush(theme: FaceTheme, base: Color): Brush = when (theme.iconTile) {
    com.zamcan.faceit.graphics.IconTile.GLASS_ROUNDED -> Brush.verticalGradient(
        listOf(base.copy(alpha = .96f), base.copy(alpha = .76f))
    )
    com.zamcan.faceit.graphics.IconTile.FROSTED_ROUNDED -> Brush.verticalGradient(
        listOf(base.copy(alpha = .90f), base.copy(alpha = .68f))
    )
    com.zamcan.faceit.graphics.IconTile.SATIN_ROUNDED -> Brush.verticalGradient(
        listOf(base.copy(alpha = .92f), base.copy(alpha = .82f))
    )
    com.zamcan.faceit.graphics.IconTile.CERAMIC_SQUIRCLE -> Brush.verticalGradient(
        listOf(Color.White.copy(alpha = .12f), Color.Transparent)
    )
    com.zamcan.faceit.graphics.IconTile.BRILLIANT,
    com.zamcan.faceit.graphics.IconTile.HEX_CUT -> Brush.verticalGradient(
        listOf(base.copy(alpha = .94f), base.copy(alpha = .74f))
    )
    else -> Brush.verticalGradient(listOf(base, base))
}

/**
 * Draws the material signature of a tile finish on top of its surface: a specular
 * band for glass, a single lit edge for stone, a facet crease for jewel, a rim for
 * ceramic, and nothing at all for matte. Small at 30 dp, but it is what makes eight
 * themes look like eight materials rather than eight corner radii.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTileFinish(
    tile: com.zamcan.faceit.graphics.IconTile,
    base: Color,
    accent: Color
) {
    val w = size.width
    val h = size.height
    val light = Color.White
    when (tile) {
        com.zamcan.faceit.graphics.IconTile.GLASS_ROUNDED -> {
            drawRect(
                Brush.verticalGradient(
                    listOf(light.copy(alpha = .26f), light.copy(alpha = .02f)),
                    startY = 0f,
                    endY = h * 0.62f
                ),
                size = androidx.compose.ui.geometry.Size(w, h * 0.62f)
            )
            drawLine(
                light.copy(alpha = .30f), Offset(0f, 0.8f), Offset(w, 0.8f),
                strokeWidth = 1.2f
            )
        }
        com.zamcan.faceit.graphics.IconTile.FROSTED_ROUNDED -> {
            drawRect(
                light.copy(alpha = .14f),
                size = androidx.compose.ui.geometry.Size(w, h * 0.34f)
            )
        }
        com.zamcan.faceit.graphics.IconTile.STONE_ROUNDED -> {
            // one lit edge along the top, one shadow along the base
            drawLine(light.copy(alpha = .22f), Offset(w * .12f, 0.9f), Offset(w * .88f, 0.9f),
                strokeWidth = 1.6f)
            drawLine(Color.Black.copy(alpha = .22f), Offset(w * .12f, h - 0.9f),
                Offset(w * .88f, h - 0.9f), strokeWidth = 1.6f)
        }
        com.zamcan.faceit.graphics.IconTile.CERAMIC_SQUIRCLE -> {
            drawCircle(
                light.copy(alpha = .10f), radius = w * 0.58f,
                center = Offset(w * 0.32f, h * 0.24f)
            )
        }
        com.zamcan.faceit.graphics.IconTile.JEWEL_OCTAGON -> {
            drawLine(accent.copy(alpha = .40f), Offset(0f, h), Offset(w, 0f), strokeWidth = 1.2f)
            drawLine(light.copy(alpha = .18f), Offset(0f, h * 0.72f), Offset(w * 0.28f, h),
                strokeWidth = 1f)
        }
        com.zamcan.faceit.graphics.IconTile.SATIN_ROUNDED -> {
            drawLine(
                light.copy(alpha = .20f), Offset(w * 0.18f, h * 0.16f),
                Offset(w * 0.82f, h * 0.16f), strokeWidth = h * 0.10f
            )
        }
        com.zamcan.faceit.graphics.IconTile.PEBBLE_CIRCLE -> {
            drawCircle(light.copy(alpha = .16f), radius = w * 0.30f,
                center = Offset(w * 0.34f, h * 0.30f))
        }
        com.zamcan.faceit.graphics.IconTile.MATTE_SQUARE -> Unit
        com.zamcan.faceit.graphics.IconTile.HEX_CUT -> {
            drawLine(light.copy(alpha = .24f), Offset(0f, h * 0.5f), Offset(w * 0.5f, 0f),
                strokeWidth = 1.4f)
            drawLine(light.copy(alpha = .24f), Offset(w * 0.5f, 0f), Offset(w, h * 0.5f),
                strokeWidth = 1.4f)
        }
        com.zamcan.faceit.graphics.IconTile.PILL_TALL -> {
            drawCircle(light.copy(alpha = .16f), radius = w * 0.26f,
                center = Offset(w * 0.5f, h * 0.28f))
        }
        com.zamcan.faceit.graphics.IconTile.ARCH_SOFT -> {
            drawLine(accent.copy(alpha = .35f), Offset(w * 0.5f, 0f), Offset(w * 0.5f, h * 0.34f),
                strokeWidth = 1.2f)
        }
        com.zamcan.faceit.graphics.IconTile.BRILLIANT -> {
            drawLine(light.copy(alpha = .26f), Offset(0f, h * 0.5f), Offset(w, h * 0.5f),
                strokeWidth = 1.2f)
            drawLine(light.copy(alpha = .18f), Offset(w * 0.5f, 0f), Offset(w * 0.5f, h),
                strokeWidth = 1f)
        }
    }
}

@Composable
private fun IconsStage(accent: Color, base: Color, ink: Color, theme: FaceTheme) {
    Column(Modifier.fillMaxSize().padding(13.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("${theme.name} icons", color = ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        IconGrid(theme, ink, rows = 3)
        Spacer(Modifier.weight(1f))
        Text(
            "${theme.iconTile.name.lowercase().replace('_', ' ')} · ${IconGlyph.entries.size} glyphs",
            color = ink.copy(alpha = .68f),
            fontSize = 8.sp
        )
    }
}

@Composable
private fun WidgetStage(accent: Color, base: Color, ink: Color, title: String) {
    Column(Modifier.fillMaxSize().padding(15.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        Text("FaceIt clock", color = ink, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = base.copy(alpha = .85f),
            border = BorderStroke(1.dp, accent.copy(alpha = .55f))
        ) {
            Column(Modifier.fillMaxWidth().padding(14.dp)) {
                Text("MONDAY · 3 NOV", color = accent, fontSize = 7.sp, letterSpacing = 1.sp)
                Text("09:41", color = ink, fontSize = 32.sp, fontWeight = FontWeight.Light)
                Text(title, color = ink.copy(alpha = .72f), fontSize = 8.sp)
            }
        }
        Text("Real home-screen widget", color = ink.copy(alpha = .66f), fontSize = 8.sp)
    }
}

@Composable
private fun KeypadStage(accent: Color, base: Color, ink: Color) {
    val design = KeypadDesigns.find("ceramic_sculpt")
    Column(
        Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Number pad", color = ink, fontSize = 11.sp)
        Spacer(Modifier.height(2.dp))
        KeypadDesigns.faces.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                row.forEach { face ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(design.cornerRadiusDp.dp / 2))
                            .background(Color(design.keySurface))
                            .border(1.dp, accent.copy(alpha = .26f), RoundedCornerShape(design.cornerRadiusDp.dp / 2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(face, color = Color(design.legend), fontSize = 13.sp)
                            KeypadDesigns.letters[face]?.let {
                                Text(it, color = accent, fontSize = 5.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyboardStage(accent: Color, base: Color, ink: Color) {
    Column(
        Modifier.fillMaxSize().padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM").forEach { line ->
            Row(horizontalArrangement = Arrangement.spacedBy(2.5.dp)) {
                line.forEach { key ->
                    Box(
                        Modifier
                            .size(width = 15.dp, height = 22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(accent.copy(alpha = .22f))
                            .border(1.dp, accent.copy(alpha = .38f), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(key.toString(), color = ink, fontSize = 8.sp)
                    }
                }
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(22.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(accent.copy(alpha = .18f)),
            contentAlignment = Alignment.Center
        ) {
            Text("space", color = ink.copy(alpha = .7f), fontSize = 7.sp)
        }
    }
}

@Composable
private fun ControlsStage(accent: Color, base: Color, ink: Color) {
    val active = listOf(true, true, false, false)
    val labels = listOf("Wi-Fi", "Torch", "Bluetooth", "Focus")
    Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text("Controls", color = ink, fontSize = 12.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            repeat(2) { column ->
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    repeat(2) { row ->
                        val index = column * 2 + row
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (active[index]) accent.copy(alpha = .30f) else ink.copy(alpha = .08f)
                                )
                                .border(1.dp, accent.copy(alpha = .30f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(labels[index], color = ink, fontSize = 8.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WritingStage(accent: Color, base: Color, ink: Color, style: StudioStyle) {
    val writing = WritingStyles.find(style.id.removePrefix("writing-"))
    Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Message", color = ink.copy(alpha = .7f), fontSize = 8.sp)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = ink.copy(alpha = .06f),
            border = BorderStroke(1.dp, accent.copy(alpha = .30f))
        ) {
            Text(
                writing.sample,
                Modifier.padding(11.dp),
                color = ink,
                fontFamily = writing.family,
                fontStyle = if (writing.italic) FontStyle.Italic else FontStyle.Normal,
                letterSpacing = writing.letterSpacingEm.sp,
                lineHeight = (12 * writing.lineHeightMultiple).sp,
                fontSize = (10 * writing.scale).sp
            )
        }
        Text(writing.name, color = accent, fontSize = 8.sp)
    }
}

@Composable
private fun ChargingStage(
    styleId: String,
    accent: Color,
    base: Color,
    ink: Color,
    phase: Float
) {
    val charging = ChargingStyles.find(styleId.removePrefix("charge-"))
    Column(
        Modifier.fillMaxSize().padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("CHARGING", color = ink.copy(alpha = .62f), fontSize = 7.sp, letterSpacing = 1.6.sp)
        Spacer(Modifier.height(18.dp))
        Box(Modifier.size(118.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                drawChargingMotif(charging.motif, Color(charging.progressColor), Color(charging.sparkColor), 0.62f, phase)
            }
            Text("62%", color = ink, fontSize = 22.sp, fontWeight = FontWeight.Light)
        }
        Spacer(Modifier.height(14.dp))
        Text(charging.label, color = ink, fontSize = 10.sp)
    }
}

/**
 * The motif renderer at preview scale.
 *
 * Deliberately the same geometry as
 * [com.zamcan.faceit.charging.ChargingRenderer] — the Android-canvas version is for
 * the widget, this is for Compose — so the two cannot drift into different looks.
 */
private fun DrawScope.drawChargingMotif(
    motif: ChargingMotif,
    progress: Color,
    spark: Color,
    level: Float,
    phase: Float
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    val r = minOf(w, h) * 0.42f
    val track = progress.copy(alpha = .14f)
    when (motif) {
        ChargingMotif.RING, ChargingMotif.PULSE, ChargingMotif.AURORA -> {
            drawCircle(track, r, Offset(cx, cy), style = Stroke(r * 0.16f))
            drawArc(
                if (motif == ChargingMotif.AURORA) spark else progress,
                -90f, 359.9f * level, false,
                topLeft = Offset(cx - r, cy - r),
                size = Size(r * 2, r * 2),
                style = Stroke(r * 0.16f)
            )
        }
        ChargingMotif.CAT -> {
            val head = Path().apply {
                moveTo(cx, cy + r * 0.80f)
                cubicTo(cx - r * 0.86f, cy + r * 0.80f, cx - r * 0.86f, cy - r * 0.30f, cx - r * 0.55f, cy - r * 0.40f)
                lineTo(cx - r * 0.62f, cy - r * 0.88f)
                lineTo(cx - r * 0.20f, cy - r * 0.56f)
                lineTo(cx + r * 0.20f, cy - r * 0.56f)
                lineTo(cx + r * 0.62f, cy - r * 0.88f)
                lineTo(cx + r * 0.55f, cy - r * 0.40f)
                cubicTo(cx + r * 0.86f, cy - r * 0.30f, cx + r * 0.86f, cy + r * 0.80f, cx, cy + r * 0.80f)
                close()
            }
            drawPath(head, track)
            drawPath(head, progress.copy(alpha = .55f))
            val open = (1f - level).coerceAtLeast(0.12f)
            drawOval(spark, Offset(cx - r * 0.32f, cy - r * 0.09f * open), Size(r * 0.17f, r * 0.19f * open))
            drawOval(spark, Offset(cx + r * 0.15f, cy - r * 0.09f * open), Size(r * 0.17f, r * 0.19f * open))
        }
        ChargingMotif.HEART -> {
            val heart = Path().apply {
                moveTo(cx, cy + r * 0.70f)
                cubicTo(cx - r * 1.02f, cy + r * 0.08f, cx - r * 0.70f, cy - r * 0.80f, cx, cy - r * 0.32f)
                cubicTo(cx + r * 0.70f, cy - r * 0.80f, cx + r * 1.02f, cy + r * 0.08f, cx, cy + r * 0.70f)
                close()
            }
            drawPath(heart, track)
            drawPath(heart, progress.copy(alpha = .60f))
            drawOval(spark.copy(alpha = .55f), Offset(cx - r * 0.50f, cy - r * 0.52f), Size(r * 0.26f, r * 0.22f))
        }
        ChargingMotif.BUTTERFLY -> {
            val spread = 0.35f + 0.65f * level
            val upper = Path().apply {
                moveTo(cx, cy - r * 0.08f)
                cubicTo(cx - r * 1.05f * spread, cy - r * 1.00f, cx - r * 1.05f * spread, cy + r * 0.20f, cx, cy + r * 0.14f)
                close()
            }
            val lower = Path().apply {
                moveTo(cx, cy + r * 0.14f)
                cubicTo(cx - r * 0.88f * spread, cy + r * 0.28f, cx - r * 0.60f * spread, cy + r * 0.98f, cx, cy + r * 0.54f)
                close()
            }
            drawPath(upper, progress.copy(alpha = .70f))
            drawPath(lower, spark.copy(alpha = .55f))
            drawLine(spark, Offset(cx, cy - r * 0.20f), Offset(cx, cy + r * 0.58f), r * 0.05f)
        }
        ChargingMotif.STARFIELD -> {
            repeat(16) { index ->
                val angle = (index / 16f) * 6.283f
                val ring = r * (0.52f + 0.44f * ((index % 4) / 4f))
                val lit = (index / 16f) <= level
                drawCircle(
                    if (lit) progress else spark.copy(alpha = .28f),
                    r * if (lit) .062f else .038f,
                    Offset(
                        cx + (ring * kotlin.math.cos(angle)).toFloat(),
                        cy + (ring * kotlin.math.sin(angle) * 0.9f).toFloat()
                    )
                )
            }
            drawCircle(spark.copy(alpha = .30f), r * 0.96f, Offset(cx, cy), style = Stroke(r * 0.02f))
        }
        ChargingMotif.ORBIT -> {
            drawCircle(progress.copy(alpha = .22f), r * 0.44f, Offset(cx, cy))
            drawCircle(progress, r * 0.30f, Offset(cx, cy))
            drawOval(
                spark.copy(alpha = .70f),
                Offset(cx - r, cy - r * 0.40f),
                Size(r * 2, r * 0.80f),
                style = Stroke(r * 0.05f)
            )
        }
        ChargingMotif.GARDEN -> {
            repeat(4) { index ->
                val open = level >= (index + 1) / 4f - 0.001f
                val angle = Math.toRadians(-90.0 + index * 90.0)
                val dist = r * if (open) 0.44f else 0.24f
                drawCircle(
                    if (open) progress else spark.copy(alpha = .34f),
                    r * if (open) .28f else .20f,
                    Offset(
                        cx + (dist * kotlin.math.cos(angle)).toFloat(),
                        cy + (dist * kotlin.math.sin(angle)).toFloat()
                    )
                )
            }
            drawCircle(spark, r * 0.15f, Offset(cx, cy))
        }
        else -> {
            drawCircle(track, r, Offset(cx, cy), style = Stroke(r * 0.15f))
            drawArc(
                progress, -90f, 359.9f * level, false,
                topLeft = Offset(cx - r, cy - r),
                size = Size(r * 2, r * 2),
                style = Stroke(r * 0.15f)
            )
        }
    }
}

/**
 * Compose twin of the relevant part of
 * [com.zamcan.faceit.graphics.IconRenderer]. Kept minimal on purpose: the preview
 * needs the silhouette to read at 30 dp, not every detail the canvas renderer draws.
 */
internal fun DrawScope.drawStudioGlyph(glyph: IconGlyph, ink: Color, accent: Color) {
    val w = size.width
    val h = size.height
    val stroke = w * 0.10f
    when (glyph) {
        IconGlyph.CAT -> {
            val head = Path().apply {
                moveTo(w * 0.5f, h * 0.92f)
                cubicTo(w * 0.10f, h * 0.92f, w * 0.10f, h * 0.42f, w * 0.28f, h * 0.34f)
                lineTo(w * 0.22f, h * 0.04f)
                lineTo(w * 0.48f, h * 0.22f)
                lineTo(w * 0.72f, h * 0.04f)
                lineTo(w * 0.78f, h * 0.34f)
                cubicTo(w * 0.94f, h * 0.42f, w * 0.90f, h * 0.92f, w * 0.5f, h * 0.92f)
                close()
            }
            drawPath(head, ink)
            drawCircle(accent, w * 0.07f, Offset(w * 0.36f, h * 0.56f))
            drawCircle(accent, w * 0.07f, Offset(w * 0.64f, h * 0.56f))
        }
        IconGlyph.MESSAGE -> {
            drawRoundRect(ink, Offset(0f, h * 0.08f), Size(w, h * 0.64f), CornerRadius(w * 0.18f), style = Stroke(stroke))
            drawPath(
                Path().apply {
                    moveTo(w * 0.26f, h * 0.70f)
                    lineTo(w * 0.20f, h)
                    lineTo(w * 0.46f, h * 0.72f)
                    close()
                },
                ink,
                style = Stroke(stroke)
            )
            repeat(3) { drawCircle(accent, w * 0.05f, Offset(w * (0.30f + it * 0.20f), h * 0.40f)) }
        }
        IconGlyph.CAMERA -> {
            drawRoundRect(ink, Offset(0f, h * 0.24f), Size(w, h * 0.68f), CornerRadius(w * 0.16f), style = Stroke(stroke))
            drawRoundRect(ink, Offset(w * 0.28f, h * 0.08f), Size(w * 0.40f, h * 0.18f), CornerRadius(w * 0.05f), style = Stroke(stroke))
            drawCircle(ink, w * 0.18f, Offset(w * 0.5f, h * 0.60f), style = Stroke(stroke))
            drawCircle(accent, w * 0.07f, Offset(w * 0.5f, h * 0.60f))
        }
        IconGlyph.HEART -> {
            drawPath(
                Path().apply {
                    moveTo(w * 0.5f, h * 0.92f)
                    cubicTo(-w * 0.06f, h * 0.48f, w * 0.16f, 0f, w * 0.5f, h * 0.26f)
                    cubicTo(w * 0.84f, 0f, w * 1.06f, h * 0.48f, w * 0.5f, h * 0.92f)
                    close()
                },
                ink
            )
            drawCircle(accent, w * 0.09f, Offset(w * 0.36f, h * 0.38f))
        }
        IconGlyph.FLOWER -> {
            repeat(6) { index ->
                val angle = Math.toRadians(index * 60.0)
                drawCircle(
                    ink,
                    w * 0.19f,
                    Offset(
                        (w * 0.5f + w * 0.27f * Math.cos(angle)).toFloat(),
                        (h * 0.5f + h * 0.27f * Math.sin(angle)).toFloat()
                    )
                )
            }
            drawCircle(accent, w * 0.14f, Offset(w * 0.5f, h * 0.5f))
        }
        IconGlyph.CHARGE -> {
            drawRoundRect(ink, Offset(w * 0.26f, h * 0.06f), Size(w * 0.48f, h * 0.88f), CornerRadius(w * 0.10f))
            drawPath(
                Path().apply {
                    moveTo(w * 0.56f, h * 0.18f)
                    lineTo(w * 0.34f, h * 0.56f)
                    lineTo(w * 0.48f, h * 0.56f)
                    lineTo(w * 0.42f, h * 0.86f)
                    lineTo(w * 0.68f, h * 0.46f)
                    lineTo(w * 0.52f, h * 0.46f)
                    close()
                },
                accent
            )
        }
        IconGlyph.CLOCK -> {
            drawCircle(ink, w * 0.44f, Offset(w * 0.5f, h * 0.5f), style = Stroke(stroke))
            drawLine(ink, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.5f, h * 0.24f),
                strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(ink, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.70f, h * 0.60f),
                strokeWidth = stroke, cap = StrokeCap.Round)
        }
        IconGlyph.MUSIC -> {
            drawLine(ink, Offset(w * 0.66f, h * 0.06f), Offset(w * 0.66f, h * 0.68f),
                strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(ink, Offset(w * 0.66f, h * 0.06f), Offset(w * 0.94f, h * 0.14f),
                strokeWidth = stroke, cap = StrokeCap.Round)
            drawCircle(ink, w * 0.13f, Offset(w * 0.52f, h * 0.72f))
            drawCircle(ink, w * 0.13f, Offset(w * 0.80f, h * 0.62f))
        }
        IconGlyph.GALLERY -> {
            drawRoundRect(ink, Offset(0f, h * 0.08f), Size(w, h * 0.92f), CornerRadius(w * 0.12f), style = Stroke(stroke))
            drawCircle(accent, w * 0.075f, Offset(w * 0.72f, h * 0.30f))
        }
        IconGlyph.CALENDAR -> {
            drawRoundRect(ink, Offset(0f, h * 0.16f), Size(w, h * 0.80f), CornerRadius(w * 0.12f), style = Stroke(stroke))
            drawLine(ink, Offset(0f, h * 0.38f), Offset(w, h * 0.38f),
                strokeWidth = stroke)
            drawCircle(accent, w * 0.09f, Offset(w * 0.5f, h * 0.64f))
        }
        IconGlyph.HOME -> {
            drawPath(
                Path().apply {
                    moveTo(w * 0.06f, h * 0.48f)
                    lineTo(w * 0.5f, h * 0.06f)
                    lineTo(w * 0.94f, h * 0.48f)
                },
                ink,
                style = Stroke(stroke)
            )
            drawRoundRect(ink, Offset(w * 0.18f, h * 0.44f), Size(w * 0.64f, h * 0.52f), CornerRadius(w * 0.06f), style = Stroke(stroke))
            drawRoundRect(accent, Offset(w * 0.40f, h * 0.64f), Size(w * 0.20f, h * 0.32f), CornerRadius(w * 0.04f))
        }
        IconGlyph.PHONE -> {
            drawPath(
                Path().apply {
                    moveTo(w * 0.12f, h * 0.10f)
                    lineTo(w * 0.38f, h * 0.26f)
                    lineTo(w * 0.26f, h * 0.52f)
                    lineTo(w * 0.52f, h * 0.78f)
                    lineTo(w * 0.78f, h * 0.66f)
                    lineTo(w * 0.92f, h * 0.92f)
                    lineTo(w * 0.62f, h * 0.98f)
                    lineTo(w * 0.04f, h * 0.40f)
                    close()
                },
                ink
            )
        }
        IconGlyph.SETTINGS -> {
            drawCircle(ink, w * 0.42f, Offset(w * 0.5f, h * 0.5f), style = Stroke(stroke))
            drawCircle(accent, w * 0.16f, Offset(w * 0.5f, h * 0.5f))
        }
        IconGlyph.MAIL -> {
            drawRoundRect(ink, Offset(0f, h * 0.18f), Size(w, h * 0.64f),
                CornerRadius(w * 0.12f), style = Stroke(stroke))
            drawPath(
                Path().apply {
                    moveTo(w * 0.04f, h * 0.22f)
                    lineTo(w * 0.5f, h * 0.58f)
                    lineTo(w * 0.96f, h * 0.22f)
                },
                ink, style = Stroke(stroke)
            )
        }
        IconGlyph.BROWSER -> {
            drawCircle(ink, w * 0.44f, Offset(w * 0.5f, h * 0.5f), style = Stroke(stroke))
            drawOval(accent, Offset(w * 0.30f, h * 0.10f), Size(w * 0.40f, h * 0.80f),
                style = Stroke(stroke * 0.6f))
            drawLine(accent, Offset(w * 0.08f, h * 0.5f), Offset(w * 0.92f, h * 0.5f),
                strokeWidth = stroke * 0.6f)
        }
        IconGlyph.WEATHER -> {
            drawCircle(accent, w * 0.17f, Offset(w * 0.36f, h * 0.34f))
            val cloud = Path().apply {
                moveTo(w * 0.18f, h * 0.76f)
                cubicTo(w * 0.04f, h * 0.76f, w * 0.06f, h * 0.52f, w * 0.24f, h * 0.52f)
                cubicTo(w * 0.28f, h * 0.28f, w * 0.62f, h * 0.28f, w * 0.66f, h * 0.52f)
                cubicTo(w * 0.88f, h * 0.50f, w * 0.92f, h * 0.76f, w * 0.74f, h * 0.76f)
                close()
            }
            drawPath(cloud, ink)
        }
        IconGlyph.FILES -> {
            val folder = Path().apply {
                moveTo(w * 0.06f, h * 0.82f)
                lineTo(w * 0.06f, h * 0.24f)
                lineTo(w * 0.40f, h * 0.24f)
                lineTo(w * 0.48f, h * 0.36f)
                lineTo(w * 0.94f, h * 0.36f)
                lineTo(w * 0.94f, h * 0.82f)
                close()
            }
            drawPath(folder, ink, style = Stroke(stroke))
            drawLine(accent, Offset(w * 0.24f, h * 0.58f), Offset(w * 0.76f, h * 0.58f),
                strokeWidth = stroke * 0.7f)
        }
        IconGlyph.PAW -> {
            repeat(3) { index ->
                val angle = Math.toRadians((200 + index * 35).toDouble())
                drawCircle(accent, w * 0.07f, Offset(
                    w * (0.32f + index * 0.18f),
                    h * (0.40f - 0.06f * kotlin.math.cos(angle).toFloat())
                ))
            }
            drawCircle(ink, w * 0.20f, Offset(w * 0.34f, h * 0.66f))
            drawCircle(ink, w * 0.20f, Offset(w * 0.66f, h * 0.66f))
        }
        IconGlyph.LEAF -> {
            drawPath(
                Path().apply {
                    moveTo(w * 0.22f, h * 0.86f)
                    cubicTo(w * 0.10f, h * 0.34f, w * 0.52f, h * 0.06f, w * 0.88f, h * 0.14f)
                    cubicTo(w * 0.90f, h * 0.62f, w * 0.56f, h * 0.90f, w * 0.22f, h * 0.86f)
                    close()
                },
                ink
            )
            drawLine(accent, Offset(w * 0.24f, h * 0.84f), Offset(w * 0.80f, h * 0.22f),
                strokeWidth = stroke * 0.7f)
        }
        IconGlyph.TREE -> {
            drawLine(accent, Offset(w * 0.5f, h * 0.92f), Offset(w * 0.5f, h * 0.46f),
                strokeWidth = stroke * 1.1f)
            val crown = Path().apply {
                moveTo(w * 0.5f, h * 0.06f)
                lineTo(w * 0.82f, h * 0.50f)
                lineTo(w * 0.18f, h * 0.50f)
                close()
            }
            drawPath(crown, ink)
            drawPath(
                Path().apply {
                    moveTo(w * 0.5f, h * 0.18f)
                    lineTo(w * 0.94f, h * 0.70f)
                    lineTo(w * 0.06f, h * 0.70f)
                    close()
                },
                ink, style = Stroke(stroke * 0.8f)
            )
        }
        IconGlyph.BUTTERFLY -> {
            val leftWing = Path().apply {
                moveTo(w * 0.5f, h * 0.84f)
                cubicTo(w * 0.02f, h * 0.84f, w * 0.04f, h * 0.20f, w * 0.48f, h * 0.40f)
                close()
            }
            val rightWing = Path().apply {
                moveTo(w * 0.5f, h * 0.84f)
                cubicTo(w * 0.98f, h * 0.84f, w * 0.96f, h * 0.20f, w * 0.52f, h * 0.40f)
                close()
            }
            drawPath(leftWing, ink)
            drawPath(rightWing, accent)
            drawLine(ink, Offset(w * 0.5f, h * 0.34f), Offset(w * 0.5f, h * 0.92f),
                strokeWidth = stroke * 0.7f, cap = StrokeCap.Round)
        }
        IconGlyph.STAR -> {
            val outer = w * 0.46f
            val inner = w * 0.19f
            val star = Path().apply {
                repeat(10) { index ->
                    val r = if (index % 2 == 0) outer else inner
                    val a = Math.toRadians(-90.0 + index * 36.0)
                    val x = w * 0.5f + r * kotlin.math.cos(a).toFloat()
                    val y = h * 0.5f + r * kotlin.math.sin(a).toFloat()
                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(star, ink)
            drawCircle(accent, w * 0.07f, Offset(w * 0.5f, h * 0.5f))
        }
        IconGlyph.CLOCK -> {
            drawCircle(ink, w * 0.44f, Offset(w * 0.5f, h * 0.5f), style = Stroke(stroke))
            drawLine(ink, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.5f, h * 0.22f),
                strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(accent, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.72f, h * 0.62f),
                strokeWidth = stroke, cap = StrokeCap.Round)
        }
        IconGlyph.SEARCH -> {
            drawCircle(ink, w * 0.30f, Offset(w * 0.42f, h * 0.40f), style = Stroke(stroke))
            drawLine(ink, Offset(w * 0.64f, h * 0.64f), Offset(w * 0.92f, h * 0.92f),
                strokeWidth = stroke * 1.2f, cap = StrokeCap.Round)
        }
        IconGlyph.GIFT -> {
            drawRoundRect(ink, Offset(w * 0.10f, h * 0.36f), Size(w * 0.80f, h * 0.52f),
                CornerRadius(w * 0.10f), style = Stroke(stroke))
            drawLine(accent, Offset(w * 0.5f, h * 0.36f), Offset(w * 0.5f, h * 0.88f),
                strokeWidth = stroke)
            drawCircle(ink, w * 0.15f, Offset(w * 0.36f, h * 0.24f), style = Stroke(stroke * 0.8f))
            drawCircle(ink, w * 0.15f, Offset(w * 0.64f, h * 0.24f), style = Stroke(stroke * 0.8f))
        }
        IconGlyph.RING -> {
            drawCircle(ink, w * 0.28f, Offset(w * 0.5f, h * 0.62f), style = Stroke(stroke))
            val jewel = Path().apply {
                moveTo(w * 0.5f, h * 0.06f)
                lineTo(w * 0.70f, h * 0.26f)
                lineTo(w * 0.5f, h * 0.42f)
                lineTo(w * 0.30f, h * 0.26f)
                close()
            }
            drawPath(jewel, accent)
        }
        IconGlyph.COFFEE -> {
            drawRoundRect(ink, Offset(w * 0.12f, h * 0.34f), Size(w * 0.56f, h * 0.48f),
                CornerRadius(w * 0.10f), style = Stroke(stroke))
            drawCircle(accent, w * 0.16f, Offset(w * 0.78f, h * 0.54f), style = Stroke(stroke * 0.9f))
            repeat(2) { index ->
                drawLine(
                    accent,
                    Offset(w * (0.28f + index * 0.20f), h * 0.30f),
                    Offset(w * (0.34f + index * 0.20f), h * 0.10f),
                    strokeWidth = stroke * 0.6f, cap = StrokeCap.Round
                )
            }
        }
        IconGlyph.BAG -> {
            drawRoundRect(ink, Offset(w * 0.10f, h * 0.34f), Size(w * 0.80f, h * 0.54f),
                CornerRadius(w * 0.12f), style = Stroke(stroke))
            val handle = Path().apply {
                moveTo(w * 0.32f, h * 0.36f)
                cubicTo(w * 0.32f, h * 0.04f, w * 0.68f, h * 0.04f, w * 0.68f, h * 0.36f)
            }
            drawPath(handle, accent, style = Stroke(stroke))
        }
        IconGlyph.SUN -> {
            drawCircle(accent, w * 0.20f, Offset(w * 0.5f, h * 0.5f))
            repeat(8) { index ->
                val a = Math.toRadians(index * 45.0)
                val cx = kotlin.math.cos(a).toFloat()
                val cy = kotlin.math.sin(a).toFloat()
                drawLine(
                    ink,
                    Offset(w * 0.5f + cx * w * 0.30f, h * 0.5f + cy * h * 0.30f),
                    Offset(w * 0.5f + cx * w * 0.44f, h * 0.5f + cy * h * 0.44f),
                    strokeWidth = stroke * 0.7f, cap = StrokeCap.Round
                )
            }
        }
        IconGlyph.MOON -> {
            val crescent = Path().apply {
                moveTo(w * 0.62f, h * 0.08f)
                cubicTo(w * 0.18f, h * 0.16f, w * 0.16f, h * 0.82f, w * 0.64f, h * 0.92f)
                cubicTo(w * 0.34f, h * 0.70f, w * 0.34f, h * 0.30f, w * 0.62f, h * 0.08f)
                close()
            }
            drawPath(crescent, ink)
            drawCircle(accent, w * 0.05f, Offset(w * 0.74f, h * 0.30f))
            drawCircle(accent, w * 0.04f, Offset(w * 0.84f, h * 0.54f))
        }
        IconGlyph.BOOK -> {
            drawRoundRect(ink, Offset(w * 0.12f, h * 0.14f), Size(w * 0.70f, h * 0.72f),
                CornerRadius(w * 0.06f), style = Stroke(stroke))
            drawLine(accent, Offset(w * 0.24f, h * 0.14f), Offset(w * 0.24f, h * 0.86f),
                strokeWidth = stroke * 0.8f)
            repeat(2) { index ->
                drawLine(accent, Offset(w * 0.38f, h * (0.36f + index * 0.18f)),
                    Offset(w * 0.70f, h * (0.36f + index * 0.18f)), strokeWidth = stroke * 0.5f)
            }
        }
        IconGlyph.PERSON -> {
            drawCircle(ink, w * 0.19f, Offset(w * 0.5f, h * 0.30f), style = Stroke(stroke))
            val shoulders = Path().apply {
                moveTo(w * 0.14f, h * 0.92f)
                cubicTo(w * 0.16f, h * 0.60f, w * 0.84f, h * 0.60f, w * 0.86f, h * 0.92f)
            }
            drawPath(shoulders, ink, style = Stroke(stroke))
        }
        IconGlyph.FOOD -> {
            drawCircle(ink, w * 0.36f, Offset(w * 0.5f, h * 0.56f), style = Stroke(stroke))
            drawCircle(accent, w * 0.18f, Offset(w * 0.5f, h * 0.56f))
            drawLine(ink, Offset(w * 0.14f, h * 0.16f), Offset(w * 0.34f, h * 0.36f),
                strokeWidth = stroke * 0.7f, cap = StrokeCap.Round)
            drawLine(ink, Offset(w * 0.86f, h * 0.16f), Offset(w * 0.66f, h * 0.36f),
                strokeWidth = stroke * 0.7f, cap = StrokeCap.Round)
        }
        IconGlyph.FLIGHT -> {
            val plane = Path().apply {
                moveTo(w * 0.06f, h * 0.56f)
                lineTo(w * 0.94f, h * 0.28f)
                lineTo(w * 0.60f, h * 0.60f)
                lineTo(w * 0.66f, h * 0.92f)
                lineTo(w * 0.44f, h * 0.66f)
                close()
            }
            drawPath(plane, ink)
            drawCircle(accent, w * 0.06f, Offset(w * 0.72f, h * 0.36f))
        }
        IconGlyph.MAP -> {
            val sheet = Path().apply {
                moveTo(w * 0.06f, h * 0.22f)
                lineTo(w * 0.36f, h * 0.12f)
                lineTo(w * 0.66f, h * 0.24f)
                lineTo(w * 0.94f, h * 0.14f)
                lineTo(w * 0.94f, h * 0.80f)
                lineTo(w * 0.66f, h * 0.90f)
                lineTo(w * 0.36f, h * 0.78f)
                lineTo(w * 0.06f, h * 0.88f)
                close()
            }
            drawPath(sheet, ink, style = Stroke(stroke))
            drawLine(accent, Offset(w * 0.36f, h * 0.14f), Offset(w * 0.36f, h * 0.78f),
                strokeWidth = stroke * 0.6f)
            drawLine(accent, Offset(w * 0.66f, h * 0.24f), Offset(w * 0.66f, h * 0.88f),
                strokeWidth = stroke * 0.6f)
        }
        IconGlyph.GAME -> {
            drawRoundRect(ink, Offset(w * 0.06f, h * 0.30f), Size(w * 0.88f, h * 0.42f),
                CornerRadius(w * 0.16f), style = Stroke(stroke))
            drawLine(accent, Offset(w * 0.26f, h * 0.51f), Offset(w * 0.40f, h * 0.51f),
                strokeWidth = stroke * 0.8f, cap = StrokeCap.Round)
            drawLine(accent, Offset(w * 0.33f, h * 0.44f), Offset(w * 0.33f, h * 0.58f),
                strokeWidth = stroke * 0.8f, cap = StrokeCap.Round)
            drawCircle(accent, w * 0.055f, Offset(w * 0.70f, h * 0.44f))
            drawCircle(accent, w * 0.055f, Offset(w * 0.80f, h * 0.56f))
        }
        IconGlyph.CONTACT, IconGlyph.PERSON -> {
            drawCircle(ink, w * 0.19f, Offset(w * 0.5f, h * 0.30f), style = Stroke(stroke))
            drawPath(
                Path().apply {
                    moveTo(w * 0.14f, h * 0.92f)
                    cubicTo(w * 0.16f, h * 0.60f, w * 0.84f, h * 0.60f, w * 0.86f, h * 0.92f)
                },
                ink, style = Stroke(stroke)
            )
        }
        IconGlyph.PEOPLE -> {
            drawCircle(accent, w * 0.14f, Offset(w * 0.32f, h * 0.32f), style = Stroke(stroke * 0.8f))
            drawCircle(ink, w * 0.16f, Offset(w * 0.68f, h * 0.28f), style = Stroke(stroke))
            drawPath(
                Path().apply {
                    moveTo(w * 0.04f, h * 0.90f)
                    cubicTo(w * 0.06f, h * 0.62f, w * 0.56f, h * 0.62f, w * 0.58f, h * 0.90f)
                },
                accent, style = Stroke(stroke * 0.8f)
            )
            drawPath(
                Path().apply {
                    moveTo(w * 0.46f, h * 0.90f)
                    cubicTo(w * 0.48f, h * 0.58f, w * 0.94f, h * 0.58f, w * 0.96f, h * 0.90f)
                },
                ink, style = Stroke(stroke)
            )
        }
        IconGlyph.WALLET -> {
            drawRoundRect(ink, Offset(w * 0.08f, h * 0.28f), Size(w * 0.84f, h * 0.50f),
                CornerRadius(w * 0.10f), style = Stroke(stroke))
            drawRoundRect(accent, Offset(w * 0.60f, h * 0.44f), Size(w * 0.28f, h * 0.18f),
                CornerRadius(w * 0.05f))
            drawCircle(ink, w * 0.04f, Offset(w * 0.74f, h * 0.53f))
        }
        IconGlyph.CHAT, IconGlyph.MESSAGE -> {
            drawRoundRect(ink, Offset(0f, h * 0.10f), Size(w, h * 0.62f),
                CornerRadius(w * 0.18f), style = Stroke(stroke))
            drawPath(
                Path().apply {
                    moveTo(w * 0.26f, h * 0.70f)
                    lineTo(w * 0.20f, h)
                    lineTo(w * 0.46f, h * 0.72f)
                    close()
                },
                ink, style = Stroke(stroke)
            )
            repeat(3) { drawCircle(accent, w * 0.05f, Offset(w * (0.30f + it * 0.20f), h * 0.41f)) }
        }
        IconGlyph.VIDEO -> {
            drawRoundRect(ink, Offset(w * 0.04f, h * 0.26f), Size(w * 0.62f, h * 0.48f),
                CornerRadius(w * 0.10f), style = Stroke(stroke))
            drawPath(
                Path().apply {
                    moveTo(w * 0.70f, h * 0.50f)
                    lineTo(w * 0.96f, h * 0.30f)
                    lineTo(w * 0.96f, h * 0.70f)
                    close()
                },
                accent
            )
        }
        IconGlyph.VOICE -> {
            drawRoundRect(ink, Offset(w * 0.38f, h * 0.08f), Size(w * 0.24f, h * 0.48f),
                CornerRadius(w * 0.12f), style = Stroke(stroke))
            drawArc(
                accent, 0f, 180f, false,
                topLeft = Offset(w * 0.24f, h * 0.34f), size = Size(w * 0.52f, h * 0.44f),
                style = Stroke(stroke)
            )
            drawLine(ink, Offset(w * 0.5f, h * 0.80f), Offset(w * 0.5f, h * 0.94f),
                strokeWidth = stroke, cap = StrokeCap.Round)
        }
        IconGlyph.SOS -> {
            drawCircle(accent, w * 0.40f, Offset(w * 0.5f, h * 0.5f), style = Stroke(stroke))
            drawLine(ink, Offset(w * 0.34f, h * 0.34f), Offset(w * 0.66f, h * 0.66f),
                strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(ink, Offset(w * 0.66f, h * 0.34f), Offset(w * 0.34f, h * 0.66f),
                strokeWidth = stroke, cap = StrokeCap.Round)
        }
        IconGlyph.DRINK -> {
            val cup = Path().apply {
                moveTo(w * 0.22f, h * 0.28f)
                lineTo(w * 0.78f, h * 0.28f)
                lineTo(w * 0.68f, h * 0.92f)
                lineTo(w * 0.32f, h * 0.92f)
                close()
            }
            drawPath(cup, ink, style = Stroke(stroke))
            drawLine(accent, Offset(w * 0.26f, h * 0.46f), Offset(w * 0.74f, h * 0.46f),
                strokeWidth = stroke * 0.7f)
            drawLine(accent, Offset(w * 0.5f, h * 0.06f), Offset(w * 0.5f, h * 0.24f),
                strokeWidth = stroke * 0.7f, cap = StrokeCap.Round)
        }
        IconGlyph.PILL -> {
            drawRoundRect(ink, Offset(w * 0.14f, h * 0.36f), Size(w * 0.72f, h * 0.30f),
                CornerRadius(h * 0.15f), style = Stroke(stroke))
            drawLine(accent, Offset(w * 0.5f, h * 0.38f), Offset(w * 0.5f, h * 0.64f),
                strokeWidth = stroke * 0.8f)
        }
        IconGlyph.RUN -> {
            drawCircle(ink, w * 0.11f, Offset(w * 0.60f, h * 0.20f))
            drawLine(ink, Offset(w * 0.54f, h * 0.34f), Offset(w * 0.40f, h * 0.58f),
                strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(ink, Offset(w * 0.40f, h * 0.58f), Offset(w * 0.58f, h * 0.72f),
                strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(accent, Offset(w * 0.58f, h * 0.72f), Offset(w * 0.46f, h * 0.94f),
                strokeWidth = stroke * 0.8f, cap = StrokeCap.Round)
            drawLine(accent, Offset(w * 0.40f, h * 0.58f), Offset(w * 0.26f, h * 0.80f),
                strokeWidth = stroke * 0.8f, cap = StrokeCap.Round)
            drawLine(accent, Offset(w * 0.54f, h * 0.40f), Offset(w * 0.76f, h * 0.52f),
                strokeWidth = stroke * 0.8f, cap = StrokeCap.Round)
        }
        IconGlyph.CYCLE -> {
            drawCircle(ink, w * 0.24f, Offset(w * 0.30f, h * 0.68f), style = Stroke(stroke))
            drawCircle(ink, w * 0.24f, Offset(w * 0.70f, h * 0.68f), style = Stroke(stroke))
            drawLine(accent, Offset(w * 0.30f, h * 0.68f), Offset(w * 0.48f, h * 0.34f),
                strokeWidth = stroke * 0.7f, cap = StrokeCap.Round)
            drawLine(accent, Offset(w * 0.48f, h * 0.34f), Offset(w * 0.70f, h * 0.68f),
                strokeWidth = stroke * 0.7f, cap = StrokeCap.Round)
        }
        IconGlyph.WEIGHT -> {
            drawRoundRect(ink, Offset(w * 0.16f, h * 0.44f), Size(w * 0.68f, h * 0.46f),
                CornerRadius(w * 0.10f), style = Stroke(stroke))
            drawArc(
                accent, 200f, 140f, false,
                topLeft = Offset(w * 0.28f, h * 0.16f), size = Size(w * 0.44f, h * 0.44f),
                style = Stroke(stroke * 0.8f)
            )
        }
        IconGlyph.CAR -> {
            val body = Path().apply {
                moveTo(w * 0.08f, h * 0.70f)
                lineTo(w * 0.16f, h * 0.46f)
                lineTo(w * 0.34f, h * 0.32f)
                lineTo(w * 0.68f, h * 0.32f)
                lineTo(w * 0.84f, h * 0.46f)
                lineTo(w * 0.92f, h * 0.70f)
                close()
            }
            drawPath(body, ink, style = Stroke(stroke))
            drawCircle(accent, w * 0.09f, Offset(w * 0.30f, h * 0.74f))
            drawCircle(accent, w * 0.09f, Offset(w * 0.70f, h * 0.74f))
        }
        IconGlyph.SHOP -> {
            val awning = Path().apply {
                moveTo(w * 0.10f, h * 0.30f)
                lineTo(w * 0.24f, h * 0.12f)
                lineTo(w * 0.76f, h * 0.12f)
                lineTo(w * 0.90f, h * 0.30f)
                close()
            }
            drawPath(awning, accent)
            drawPath(
                Path().apply {
                    moveTo(w * 0.16f, h * 0.34f)
                    lineTo(w * 0.16f, h * 0.88f)
                    lineTo(w * 0.84f, h * 0.88f)
                    lineTo(w * 0.84f, h * 0.34f)
                },
                ink, style = Stroke(stroke)
            )
        }
        IconGlyph.BANK -> {
            val roof = Path().apply {
                moveTo(w * 0.5f, h * 0.08f)
                lineTo(w * 0.94f, h * 0.34f)
                lineTo(w * 0.06f, h * 0.34f)
                close()
            }
            drawPath(roof, ink)
            repeat(3) { index ->
                drawLine(accent, Offset(w * (0.28f + index * 0.22f), h * 0.42f),
                    Offset(w * (0.28f + index * 0.22f), h * 0.82f), strokeWidth = stroke * 0.8f)
            }
            drawLine(ink, Offset(w * 0.10f, h * 0.88f), Offset(w * 0.90f, h * 0.88f),
                strokeWidth = stroke)
        }
        IconGlyph.TICKET -> {
            val stub = Path().apply {
                moveTo(w * 0.08f, h * 0.32f)
                lineTo(w * 0.92f, h * 0.32f)
                lineTo(w * 0.92f, h * 0.64f)
                lineTo(w * 0.08f, h * 0.64f)
                close()
            }
            drawPath(stub, ink, style = Stroke(stroke))
            drawLine(accent, Offset(w * 0.70f, h * 0.32f), Offset(w * 0.70f, h * 0.64f),
                strokeWidth = stroke * 0.6f)
            repeat(2) { index ->
                drawLine(accent, Offset(w * 0.20f, h * (0.42f + index * 0.12f)),
                    Offset(w * 0.52f, h * (0.42f + index * 0.12f)), strokeWidth = stroke * 0.5f)
            }
        }
        IconGlyph.PIN -> {
            drawPath(
                Path().apply {
                    moveTo(w * 0.5f, h * 0.94f)
                    cubicTo(w * 0.10f, h * 0.58f, w * 0.14f, h * 0.10f, w * 0.5f, h * 0.10f)
                    cubicTo(w * 0.86f, h * 0.10f, w * 0.90f, h * 0.58f, w * 0.5f, h * 0.94f)
                    close()
                },
                ink
            )
            drawCircle(accent, w * 0.11f, Offset(w * 0.5f, h * 0.38f))
        }
        IconGlyph.CHEF -> {
            drawCircle(ink, w * 0.15f, Offset(w * 0.32f, h * 0.30f), style = Stroke(stroke * 0.8f))
            drawCircle(ink, w * 0.17f, Offset(w * 0.50f, h * 0.24f), style = Stroke(stroke * 0.8f))
            drawCircle(ink, w * 0.15f, Offset(w * 0.68f, h * 0.30f), style = Stroke(stroke * 0.8f))
            drawRoundRect(ink, Offset(w * 0.18f, h * 0.44f), Size(w * 0.64f, h * 0.48f),
                CornerRadius(w * 0.08f), style = Stroke(stroke))
            drawLine(accent, Offset(w * 0.28f, h * 0.66f), Offset(w * 0.72f, h * 0.66f),
                strokeWidth = stroke * 0.7f)
        }
        IconGlyph.BASKET -> {
            drawRoundRect(ink, Offset(w * 0.08f, h * 0.40f), Size(w * 0.84f, h * 0.52f),
                CornerRadius(w * 0.10f), style = Stroke(stroke))
            drawArc(accent, 180f, 180f, false,
                topLeft = Offset(w * 0.20f, h * 0.06f), size = Size(w * 0.60f, h * 0.60f),
                style = Stroke(stroke * 0.8f))
            drawLine(accent, Offset(w * 0.18f, h * 0.66f), Offset(w * 0.82f, h * 0.66f),
                strokeWidth = stroke * 0.6f)
        }
        IconGlyph.TOOLS -> {
            drawLine(ink, Offset(w * 0.18f, h * 0.88f), Offset(w * 0.82f, h * 0.16f),
                strokeWidth = stroke, cap = StrokeCap.Round)
            drawRoundRect(accent, Offset(w * 0.66f, h * 0.08f), Size(w * 0.26f, h * 0.20f),
                CornerRadius(w * 0.04f))
            drawCircle(ink, w * 0.13f, Offset(w * 0.18f, h * 0.84f), style = Stroke(stroke))
        }
        IconGlyph.CALCULATOR -> {
            drawRoundRect(ink, Offset(w * 0.16f, h * 0.06f), Size(w * 0.68f, h * 0.88f),
                CornerRadius(w * 0.08f), style = Stroke(stroke))
            drawRoundRect(accent, Offset(w * 0.24f, h * 0.14f), Size(w * 0.52f, h * 0.22f),
                CornerRadius(w * 0.03f))
            repeat(3) { row ->
                repeat(3) { col ->
                    drawCircle(ink, w * 0.045f,
                        Offset(w * (0.30f + col * 0.20f), h * (0.50f + row * 0.16f)))
                }
            }
        }
        IconGlyph.NOTE -> {
            drawRoundRect(ink, Offset(w * 0.14f, h * 0.06f), Size(w * 0.72f, h * 0.88f),
                CornerRadius(w * 0.06f), style = Stroke(stroke))
            repeat(4) { index ->
                drawLine(accent, Offset(w * 0.24f, h * (0.24f + index * 0.16f)),
                    Offset(w * 0.74f, h * (0.24f + index * 0.16f)), strokeWidth = stroke * 0.5f)
            }
        }
        IconGlyph.LOCK -> {
            drawRoundRect(ink, Offset(w * 0.20f, h * 0.42f), Size(w * 0.60f, h * 0.52f),
                CornerRadius(w * 0.08f), style = Stroke(stroke))
            drawArc(ink, 180f, 180f, false,
                topLeft = Offset(w * 0.28f, h * 0.10f), size = Size(w * 0.44f, h * 0.50f),
                style = Stroke(stroke))
            drawCircle(accent, w * 0.07f, Offset(w * 0.5f, h * 0.66f))
        }
        IconGlyph.UNLOCK -> {
            drawRoundRect(ink, Offset(w * 0.20f, h * 0.42f), Size(w * 0.60f, h * 0.52f),
                CornerRadius(w * 0.08f), style = Stroke(stroke))
            drawArc(ink, 150f, 150f, false,
                topLeft = Offset(w * 0.28f, h * 0.10f), size = Size(w * 0.44f, h * 0.50f),
                style = Stroke(stroke))
            drawCircle(accent, w * 0.07f, Offset(w * 0.5f, h * 0.66f))
        }
        IconGlyph.KEY -> {
            drawCircle(ink, w * 0.17f, Offset(w * 0.30f, h * 0.36f), style = Stroke(stroke))
            drawLine(ink, Offset(w * 0.42f, h * 0.50f), Offset(w * 0.88f, h * 0.90f),
                strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(accent, Offset(w * 0.68f, h * 0.72f), Offset(w * 0.80f, h * 0.60f),
                strokeWidth = stroke * 0.7f, cap = StrokeCap.Round)
        }
        IconGlyph.TROPHY -> {
            drawPath(
                Path().apply {
                    moveTo(w * 0.26f, h * 0.12f)
                    lineTo(w * 0.74f, h * 0.12f)
                    lineTo(w * 0.66f, h * 0.52f)
                    lineTo(w * 0.34f, h * 0.52f)
                    close()
                }, ink
            )
            drawLine(ink, Offset(w * 0.5f, h * 0.52f), Offset(w * 0.5f, h * 0.82f),
                strokeWidth = stroke)
            drawLine(accent, Offset(w * 0.32f, h * 0.86f), Offset(w * 0.68f, h * 0.86f),
                strokeWidth = stroke)
        }
        IconGlyph.MEDAL -> {
            drawCircle(ink, w * 0.22f, Offset(w * 0.5f, h * 0.40f), style = Stroke(stroke))
            drawCircle(accent, w * 0.11f, Offset(w * 0.5f, h * 0.40f))
            drawPath(
                Path().apply {
                    moveTo(w * 0.34f, h * 0.58f)
                    lineTo(w * 0.26f, h * 0.94f)
                    lineTo(w * 0.5f, h * 0.80f)
                    lineTo(w * 0.74f, h * 0.94f)
                    lineTo(w * 0.66f, h * 0.58f)
                }, ink
            )
        }
        IconGlyph.RULER -> {
            drawRoundRect(ink, Offset(w * 0.06f, h * 0.34f), Size(w * 0.88f, h * 0.28f),
                CornerRadius(w * 0.04f), style = Stroke(stroke))
            repeat(4) { index ->
                val x = w * (0.24f + index * 0.17f)
                drawLine(accent, Offset(x, h * 0.38f), Offset(x, h * 0.50f),
                    strokeWidth = stroke * 0.5f)
            }
        }
        IconGlyph.BRUSH -> {
            drawLine(ink, Offset(w * 0.20f, h * 0.86f), Offset(w * 0.56f, h * 0.30f),
                strokeWidth = stroke, cap = StrokeCap.Round)
            drawPath(
                Path().apply {
                    moveTo(w * 0.50f, h * 0.28f)
                    lineTo(w * 0.90f, h * 0.10f)
                    lineTo(w * 0.94f, h * 0.48f)
                    close()
                }, accent
            )
        }
        IconGlyph.PENCIL -> {
            drawPath(
                Path().apply {
                    moveTo(w * 0.22f, h * 0.90f)
                    lineTo(w * 0.32f, h * 0.82f)
                    lineTo(w * 0.82f, h * 0.22f)
                    lineTo(w * 0.70f, h * 0.10f)
                    lineTo(w * 0.20f, h * 0.66f)
                    close()
                }, ink
            )
            drawLine(accent, Offset(w * 0.22f, h * 0.90f), Offset(w * 0.14f, h * 0.98f),
                strokeWidth = stroke * 0.7f, cap = StrokeCap.Round)
        }
        IconGlyph.PLANE_TAIL -> {
            drawPath(
                Path().apply {
                    moveTo(w * 0.30f, h * 0.90f)
                    lineTo(w * 0.58f, h * 0.10f)
                    lineTo(w * 0.90f, h * 0.34f)
                    close()
                }, ink
            )
            drawLine(accent, Offset(w * 0.20f, h * 0.98f), Offset(w * 0.86f, h * 0.44f),
                strokeWidth = stroke * 0.7f, cap = StrokeCap.Round)
        }
        IconGlyph.BICYCLE -> {
            drawCircle(ink, w * 0.20f, Offset(w * 0.26f, h * 0.66f), style = Stroke(stroke))
            drawCircle(ink, w * 0.20f, Offset(w * 0.74f, h * 0.66f), style = Stroke(stroke))
            drawPath(
                Path().apply {
                    moveTo(w * 0.26f, h * 0.66f)
                    lineTo(w * 0.46f, h * 0.34f)
                    lineTo(w * 0.74f, h * 0.66f)
                    lineTo(w * 0.26f, h * 0.66f)
                }, accent, style = Stroke(stroke * 0.7f)
            )
        }
        IconGlyph.GLOBE -> {
            drawCircle(ink, w * 0.38f, Offset(w * 0.5f, h * 0.5f), style = Stroke(stroke))
            drawOval(accent, Offset(w * 0.34f, h * 0.12f), Size(w * 0.32f, h * 0.76f),
                style = Stroke(stroke * 0.6f))
            drawLine(accent, Offset(w * 0.12f, h * 0.5f), Offset(w * 0.88f, h * 0.5f),
                strokeWidth = stroke * 0.6f)
        }
        else -> {
            // A neutral rounded square with an accent dot, so an unmapped glyph still
            // reads as an app symbol rather than an empty hole.
            drawRoundRect(ink, Offset(0f, 0f), Size(w, h), CornerRadius(w * 0.24f), style = Stroke(stroke))
            drawCircle(accent, w * 0.16f, Offset(w * 0.5f, h * 0.5f))
        }
    }
}
