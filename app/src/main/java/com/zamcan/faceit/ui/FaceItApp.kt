package com.zamcan.faceit.ui

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.snap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zamcan.faceit.data.CompositionLibrary
import com.zamcan.faceit.data.EditHistory
import com.zamcan.faceit.data.ArtworkRepository
import com.zamcan.faceit.graphics.PhotoComposer
import com.zamcan.faceit.data.PhotoBitmaps
import com.zamcan.faceit.data.PhotoLibrary
import com.zamcan.faceit.data.PhotoWallpaper
import com.zamcan.faceit.data.CompositionPresets
import com.zamcan.faceit.data.EnvironmentStore
import com.zamcan.faceit.data.PersonalEnvironment
import com.zamcan.faceit.data.ThemeWallpaperMode
import com.zamcan.faceit.data.WallpaperPolicy
import com.zamcan.faceit.data.applyingTheme
import com.zamcan.faceit.themes.FaceTheme
import com.zamcan.faceit.themes.FaceThemes
import com.zamcan.faceit.graphics.WallpaperComposition
import com.zamcan.faceit.graphics.WallpaperRenderer
import com.zamcan.faceit.ui.theme.LocalFaceItColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Material finishes the studio offers. Drawn procedurally; no raster tiles ship. */
private val MATERIAL_FINISHES = listOf(
    "Marble", "Travertine", "Glazed ceramic", "Natural stone", "Brushed metal",
    "Frosted glass", "Basalt", "Sandstone"
)

private enum class Destination(val label: String) {
    Discover("Discover"),
    Themes("Themes"),
    Create("Create"),
    Library("Library"),
    Tools("Tools")
}

/**
 * Which style each studio category is currently showing as applied.
 *
 * The studio asks this so a card can say "Applied" instead of "Apply" — a small
 * thing, but the difference between a preview shelf and a settings surface.
 */
private fun appliedStyleFor(category: StudioCategory, environment: PersonalEnvironment): String? =
    when (category) {
        StudioCategory.THEMES, StudioCategory.FEMININE -> environment.themeId
        StudioCategory.KEYPAD -> "keypad-${environment.keypadDesignId}"
        StudioCategory.WRITING -> "writing-${environment.writingStyleId}"
        else -> null
    }

internal enum class ToolsPage(val label: String) {
    Home("Tools"),
    NameStudio("Name studio"),
    KeyboardDesigns("Keyboard designs"),
    Vault("Private Vault"),
    Sms("SMS composer"),
    Personalisation("Style studio")
}

@Composable
fun FaceItApp() {
    val context = LocalContext.current
    val setThemePreset = com.zamcan.faceit.ui.theme.LocalSetFaceItThemePresetId.current
    val store = remember { EnvironmentStore(context) }
    val artworkRepository = remember { ArtworkRepository(context) }
    val sceneLibrary = remember { CompositionLibrary(context) }
    var libraryPage by remember { mutableStateOf("Artwork") }
    var showGallery by remember { mutableStateOf(false) }
    var environment by remember { mutableStateOf(store.load()) }
    var destination by remember { mutableStateOf(Destination.Discover) }
    var toolsPage by remember { mutableStateOf(ToolsPage.Home) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    // AGENTS.md §13/§17: one runtime policy decides whether motion may animate.
    val motionPolicy = rememberMotionPolicy()

    fun update(next: PersonalEnvironment) {
        environment = next
        store.save(next)
    }

    // A studio choice becomes real state here, and the message naming the theme is
    // the only confirmation the user gets — so it names the theme, not "saved".
    val applyStudioStyle: (StudioStyle) -> Unit = remember(environment) {
        { style ->
            when (style.category) {
                StudioCategory.THEMES, StudioCategory.FEMININE -> {
                    // Feminine cards carry a "feminine-" prefix so the two shelves can
                    // hold the same theme without id collisions.
                    val theme = FaceThemes.find(style.id.removePrefix("feminine-"))
                    setThemePreset(theme.id)
                    update(
                        environment.applyingTheme(
                            theme,
                            keepWallpaper = environment.wallpaperMode == ThemeWallpaperMode.KEEP
                        )
                    )
                    scope.launch { snackbarHostState.showSnackbar("${theme.name} applied") }
                }
                StudioCategory.KEYPAD -> {
                    update(environment.copy(keypadDesignId = style.id.removePrefix("keypad-")))
                    scope.launch { snackbarHostState.showSnackbar("${style.title} keypad selected") }
                }
                StudioCategory.WRITING -> {
                    update(environment.copy(writingStyleId = style.id.removePrefix("writing-")))
                    scope.launch { snackbarHostState.showSnackbar("${style.title} writing style selected") }
                }
                else -> Unit
            }
        }
    }

    // The background is always a photograph now. It is decoded off the main
    // thread and released as soon as the selection changes, so browsing the
    // gallery cannot accumulate full-size bitmaps.
    val photoBitmaps = remember { PhotoBitmaps(context) }
    var curatedBackground by remember { mutableStateOf<Bitmap?>(null) }

    // A photograph the user picked wins; otherwise the theme's own photograph is
    // shown, so a fresh install still has a real background. The name engine is
    // composed on top here, which is why every name field is a key: typing a
    // letter has to redraw the surface behind the words.
    LaunchedEffect(environment) {
        curatedBackground = withContext(Dispatchers.IO) {
            WallpaperComposition.compose(context, environment, 540, 1200)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            curatedBackground = null
            photoBitmaps.trimMemory()
        }
    }

    fun applyWallpaper(targetFlags: Int) {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val bitmap = WallpaperComposition.composeWallpaper(context, environment)
                        ?: error("The composition could not be rendered")
                    try {
                        WallpaperManager.getInstance(context).setBitmap(bitmap, null, true, targetFlags)
                    } finally {
                        bitmap.recycle()
                    }
                }
            }
            result.onSuccess {
                snackbarHostState.showSnackbar("Wallpaper applied")
            }.onFailure { error ->
                snackbarHostState.showSnackbar(
                    error.localizedMessage?.let { "Wallpaper error: $it" } ?: "Unable to apply wallpaper"
                )
            }
        }
    }

    fun applyPhoto(photo: PhotoWallpaper, targetFlags: Int) {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val source = photoBitmaps.fullSize(photo, 1440)
                        ?: error("The bundled photograph could not be decoded")
                    try {
                        val composed = PhotoComposer.compose(1080, 2400, source)
                        try {
                            WallpaperManager.getInstance(context)
                                .setBitmap(composed, null, true, targetFlags)
                        } finally {
                            composed.recycle()
                        }
                    } finally {
                        source.recycle()
                    }
                }
            }
            result.onSuccess {
                snackbarHostState.showSnackbar(if (photo.synthetic) "${photo.subject} applied — generated artwork"
                    else "${photo.subject} applied — photo by ${photo.creator}")
            }.onFailure {
                snackbarHostState.showSnackbar("Unable to apply this photograph")
            }
        }
    }

    BackHandler(enabled = showGallery || destination != Destination.Discover || toolsPage != ToolsPage.Home) {
        if (showGallery) {
            showGallery = false
        } else if (destination == Destination.Tools && toolsPage != ToolsPage.Home) {
            toolsPage = ToolsPage.Home
        } else {
            destination = Destination.Discover
        }
    }

    CompositionLocalProvider(LocalMotionPolicy provides motionPolicy) {
    FaceItBackdrop(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                FaceItNavigation(
                    destination = destination,
                    onSelect = { selected ->
                        if (selected == Destination.Tools) toolsPage = ToolsPage.Home
                        destination = selected
                    }
                )
            }
        ) { padding ->
            Crossfade(
                targetState = destination,
                modifier = Modifier.fillMaxSize(),
                animationSpec = if (motionPolicy.animationsEnabled) {
                    androidx.compose.animation.core.tween(durationMillis = 220)
                } else {
                    snap()
                },
                label = "destination"
            ) { current ->
                when (current) {
                    Destination.Discover -> DiscoverScreen(
                        environment = environment,
                        artworkFile = artworkRepository.fileFor(environment.selectedArtworkFile.orEmpty()),
                        artworkObjectFile = artworkRepository.fileFor(environment.selectedObjectFile.orEmpty()),
                        modifier = Modifier.padding(padding),
                        photoCount = PhotoLibrary.all(context).size,
                        photoSelected = environment.curatedPhotoId?.let { id ->
                            PhotoLibrary.find(context, id)?.subject
                        } ?: PhotoLibrary.find(
                            context,
                            FaceThemes.find(environment.themeId).wallpaperId
                        )?.subject,
                        curatedBackground = curatedBackground,
                        onOpenGallery = { showGallery = true },
                        onCreate = { destination = Destination.Create },
                        onPreset = { preset ->
                            update(preset.copy(useProceduralBackground = true))
                            scope.launch { snackbarHostState.showSnackbar("Loaded ${preset.name}") }
                        },
                        onApply = { flags -> applyWallpaper(flags) }
                    )

                    Destination.Create -> CompositionScreen(
                        initial = environment,
                        artworkFile = artworkRepository.fileFor(environment.selectedArtworkFile.orEmpty()),
                        objectArtworkFile = artworkRepository.fileFor(environment.selectedObjectFile.orEmpty()),
                        modifier = Modifier.padding(padding),
                        onSave = { scene ->
                            scope.launch {
                                val result = withContext(Dispatchers.IO) { runCatching { sceneLibrary.save(scene) } }
                                result.onSuccess {
                                    update(scene)
                                    snackbarHostState.showSnackbar("${scene.name} saved to your scene library")
                                }.onFailure { snackbarHostState.showSnackbar(it.message ?: "Unable to save scene") }
                            }
                        },
                        onOpenLibrary = { destination = Destination.Library }
                    )

                    Destination.Themes -> VisualStudioScreen(
                        selectedThemeId = environment.themeId,
                        appliedStyleId = { category -> appliedStyleFor(category, environment) },
                        onStyleChosen = applyStudioStyle,
                        modifier = Modifier.padding(padding)
                    )

                    Destination.Library -> Column(Modifier.padding(padding).fillMaxSize()) {
                        SegmentedChoice(
                            options = listOf("Artwork" to "Artwork", "Scenes" to "Saved scenes"),
                            selected = libraryPage,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                            onSelected = { libraryPage = it }
                        )
                        if (libraryPage == "Scenes") {
                            CompositionLibraryScreen(
                                modifier = Modifier.weight(1f),
                                onRestore = { scene ->
                                    setThemePreset(scene.themeId)
                                    update(scene)
                                    destination = Destination.Discover
                                }
                            )
                        } else {
                        ArtworkStudioScreen(
                        selectedFileName = environment.selectedArtworkFile,
                        selectedObjectFileName = environment.selectedObjectFile,
                        modifier = Modifier.weight(1f),
                        onSelected = { selected, layer ->
                            update(
                                if (layer == "Object") {
                                    environment.copy(selectedObjectFile = selected.fileName)
                                } else {
                                    environment.copy(selectedArtworkFile = selected.fileName, nameGroundId = null)
                                }
                            )
                            destination = Destination.Discover
                            scope.launch { snackbarHostState.showSnackbar("${selected.title} set as ${layer.lowercase()}") }
                        },
                        onClearLayer = { layer ->
                            update(
                                if (layer == "Object") {
                                    environment.copy(selectedObjectFile = null)
                                } else {
                                    environment.copy(selectedArtworkFile = null)
                                }
                            )
                        }
                    )
                        }
                    }

                    Destination.Tools -> if (toolsPage == ToolsPage.NameStudio) {
                        // Drawn by the shell rather than the hub, because the studio
                        // edits the live environment; the hub only routes.
                        NameStudioScreen(
                            environment = environment,
                            onChange = { update(it) },
                            onOpenLibrary = { showGallery = true },
                            modifier = Modifier.padding(padding)
                        )
                    } else {
                        ToolsHubScreen(
                            page = toolsPage,
                            modifier = Modifier.padding(padding),
                            onOpen = { if (it == ToolsPage.Personalisation) destination = Destination.Themes else toolsPage = it },
                            onBack = { toolsPage = ToolsPage.Home }
                        )
                    }
                }
            }
        }


        // The photograph gallery, drawn above the workspace so it keeps the same
        // backdrop instead of stacking a second theme on top of it.
        if (showGallery) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(LocalFaceItColors.current.backdropTop)
            ) {
                PhotoGalleryScreen(
                    modifier = Modifier.fillMaxSize().padding(top = 24.dp),
                    selectedId = environment.curatedPhotoId,
                    onSelect = { photo ->
                        update(environment.copy(curatedPhotoId = photo.id, selectedArtworkFile = null, useProceduralBackground = false,
                            nameGroundId = if (environment.nameEnabled) photo.id else null))
                        showGallery = false
                        scope.launch {
                            snackbarHostState.showSnackbar("${photo.subject} set as background")
                        }
                    },
                    onApply = { photo, flags -> applyPhoto(photo, flags) },
                    onBack = { showGallery = false }
                )
            }
        }
    }
}
}

@Composable
private fun FaceItNavigation(
    destination: Destination,
    onSelect: (Destination) -> Unit
) {
    val colors = LocalFaceItColors.current
    FaceItGlass(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
        tone = GlassTone.Raised,
        shape = RoundedCornerShape(28.dp),
        contentPadding = 0.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            windowInsets = WindowInsets(0, 0, 0, 0),
            tonalElevation = 0.dp
        ) {
            Destination.entries.forEach { item ->
                NavigationBarItem(
                    selected = destination == item,
                    onClick = { onSelect(item) },
                    icon = {
                        Icon(
                            imageVector = when (item) {
                                Destination.Discover -> Icons.Rounded.Explore
                                Destination.Create -> Icons.Rounded.Tune
                                Destination.Themes -> Icons.Rounded.Palette
                                Destination.Library -> Icons.Rounded.Collections
                                Destination.Tools -> Icons.Rounded.Settings
                            },
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(item.label, style = MaterialTheme.typography.labelSmall)
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.violet,
                        selectedTextColor = colors.violet,
                        indicatorColor = colors.violet.copy(alpha = .18f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun DiscoverScreen(
    environment: PersonalEnvironment,
    artworkFile: java.io.File?,
    artworkObjectFile: java.io.File?,
    modifier: Modifier,
    photoCount: Int,
    photoSelected: String?,
    curatedBackground: Bitmap?,
    onOpenGallery: () -> Unit,
    onCreate: () -> Unit,
    onPreset: (PersonalEnvironment) -> Unit,
    onApply: (Int) -> Unit
) {
    val colors = LocalFaceItColors.current
    var showApplyDialog by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier.fillMaxSize()) {
    val canvasHeight = (maxHeight * .52f).coerceIn(260.dp, 520.dp)
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FaceItWordmark(Modifier.weight(1f))
            StatusPill(text = "OFFLINE", icon = Icons.Rounded.Lock, tint = colors.mint)
        }

        // Artwork is the home surface: no marketing hero or stacked dashboard cards.
        Box(
            modifier = Modifier.fillMaxWidth().height(canvasHeight)
                .clip(MaterialTheme.shapes.extraLarge)
                .border(1.dp, colors.border, MaterialTheme.shapes.extraLarge)
        ) {
            CompositionPreview(
                environment = environment,
                artworkFile = artworkFile,
                objectArtworkFile = artworkObjectFile,
                curatedBackground = curatedBackground,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = .18f), Color.Transparent, Color.Transparent, Color.Black.copy(alpha = .74f))
                    )
                )
            )
            StatusPill(
                text = "LIVE COMPOSITION",
                modifier = Modifier.align(Alignment.TopStart).padding(13.dp),
                tint = Color.White
            )
            Column(Modifier.align(Alignment.BottomStart).padding(18.dp)) {
                Text(environment.name, style = MaterialTheme.typography.headlineSmall, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${environment.background}  ·  ${environment.mainObject}  ·  ${environment.material}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = .82f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Button(onClick = { showApplyDialog = true }, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(17.dp)) {
                Icon(Icons.Rounded.Wallpaper, contentDescription = null, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(7.dp))
                Text("Apply")
            }
            OutlinedButton(
                onClick = onCreate,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(17.dp),
                border = BorderStroke(1.dp, colors.border),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
            ) {
                Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(7.dp))
                Text("Edit layers")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LayerChip(Modifier.weight(1f), "SCENE", environment.background, Icons.Rounded.Image, onCreate)
            LayerChip(Modifier.weight(1f), "OBJECT", environment.mainObject, Icons.Rounded.Layers, onCreate)
            LayerChip(Modifier.weight(1f), "FINISH", environment.material, Icons.Rounded.Wallpaper, onCreate)
        }

        FaceItGlass(modifier = Modifier.fillMaxWidth(), onClick = onOpenGallery, tone = GlassTone.Accent, contentPadding = 12.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = colors.mint, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Photograph library", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "$photoCount wallpapers · photos & generated artwork",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (photoSelected != null) {
                        Text(
                            "Showing: $photoSelected",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.mint
                        )
                    }
                }
                Text("OPEN", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp), color = colors.mint)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Your scenes", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            Text("EDITABLE", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(CompositionPresets.all, key = { it.name }) { preset ->
                PresetCard(preset = preset, active = preset.name == environment.name, onClick = { onPreset(preset) })
            }
        }
    }

    }

    if (showApplyDialog) {
        ApplyWallpaperDialog(onDismiss = { showApplyDialog = false }, onApply = { flags -> showApplyDialog = false; onApply(flags) })
    }
}

@Composable
private fun LayerChip(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val colors = LocalFaceItColors.current
    Surface(onClick = onClick, modifier = modifier.height(64.dp), shape = RoundedCornerShape(17.dp), color = colors.card, border = BorderStroke(1.dp, colors.border)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 9.dp, vertical = 8.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = colors.violet, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(5.dp))
                Text(label, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = .8.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(value, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun PresetCard(
    preset: PersonalEnvironment,
    active: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalFaceItColors.current
    Surface(
        onClick = onClick,
        modifier = Modifier.width(196.dp),
        shape = MaterialTheme.shapes.large,
        color = colors.card,
        border = BorderStroke(
            width = if (active) 1.5.dp else 1.dp,
            color = if (active) colors.violet.copy(alpha = .65f) else colors.border
        )
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(112.dp)) {
                GradientTile(category = preset.background, modifier = Modifier.fillMaxSize())
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = .55f))
                            )
                        )
                )
                if (active) {
                    StatusPill(
                        text = "ACTIVE",
                        modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
                        tint = colors.mint
                    )
                }
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
                )
            }
            Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    text = "${preset.mainObject} · ${preset.material}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun ApplyWallpaperDialog(
    onDismiss: () -> Unit,
    onApply: (Int) -> Unit
) {
    val colors = LocalFaceItColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.card,
        shape = MaterialTheme.shapes.extraLarge,
        title = {
            Column {
                Text("Apply wallpaper", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Choose where FaceIt should apply this composition.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ApplyTargetRow(
                    icon = Icons.Rounded.Home,
                    title = "Home screen",
                    description = "Keep the lock screen unchanged.",
                    onClick = { onApply(WallpaperManager.FLAG_SYSTEM) }
                )
                ApplyTargetRow(
                    icon = Icons.Rounded.Lock,
                    title = "Lock screen",
                    description = "Keep the home screen unchanged.",
                    onClick = { onApply(WallpaperManager.FLAG_LOCK) }
                )
                ApplyTargetRow(
                    icon = Icons.Rounded.Smartphone,
                    title = "Home and lock",
                    description = "Apply the same composition to both.",
                    onClick = {
                        onApply(WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ApplyTargetRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    val colors = LocalFaceItColors.current
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = colors.cardRaised,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = colors.violet, modifier = Modifier.size(21.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CompositionScreen(
    initial: PersonalEnvironment,
    artworkFile: java.io.File?,
    objectArtworkFile: java.io.File?,
    modifier: Modifier,
    onSave: (PersonalEnvironment) -> Unit,
    onOpenLibrary: () -> Unit
) {
    val colors = LocalFaceItColors.current
    val context = LocalContext.current
    val photoBitmaps = remember { PhotoBitmaps(context) }
    var history by remember(initial) { mutableStateOf(EditHistory(initial)) }
    val value = history.current
    fun change(next: PersonalEnvironment) { history = history.record(next) }
    var gestureBase by remember(initial) { mutableStateOf<EditHistory<PersonalEnvironment>?>(null) }
    fun adjust(next: PersonalEnvironment) {
        if (gestureBase == null) gestureBase = history
        history = history.copy(current = next)
    }
    fun finishAdjustment() {
        gestureBase?.let { history = it.record(history.current) }
        gestureBase = null
    }
    var studioBackground by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(value) {
        studioBackground = withContext(Dispatchers.IO) {
            WallpaperComposition.compose(context, value, 540, 1200)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            studioBackground = null
            photoBitmaps.trimMemory()
        }
    }

    val activePreset = CompositionPresets.all.firstOrNull {
        it.name == value.name &&
            it.background == value.background &&
            it.mainObject == value.mainObject &&
            it.material == value.material
    }
    val dirty = value != initial

    Column(modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                ScreenHeader(
                    eyebrow = "Composition studio",
                    title = "Build your environment",
                    description = "Change one layer without resetting the others."
                )
            }

            item {
                OutlinedTextField(
                    value = value.name,
                    onValueChange = { change(value.copy(name = it.take(80))) },
                    label = { Text("Scene name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { history = history.undo() }, enabled = history.canUndo) { Text("Undo") }
                    TextButton(onClick = { history = history.redo() }, enabled = history.canRedo) { Text("Redo") }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 240.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .border(1.dp, colors.border, MaterialTheme.shapes.extraLarge)
                ) {
                    CompositionPreview(
                        environment = value,
                        artworkFile = artworkFile,
                        objectArtworkFile = objectArtworkFile,
                        curatedBackground = studioBackground,
                        modifier = Modifier.fillMaxSize()
                    )
                    StatusPill(
                        text = "Live preview",
                        modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                        tint = colors.mint
                    )
                }
            }

            item {
                FaceItCard {
                    ChoiceChips(
                        title = "Preset",
                        supporting = "Start from a composed baseline. Your saved artwork stays in Library.",
                        options = CompositionPresets.all.map { it.name },
                        selected = activePreset?.name ?: "",
                        onSelected = { name ->
                            CompositionPresets.find(name)?.let { preset ->
                                change(value.copy(name = preset.name, background = preset.background,
                                    mainObject = preset.mainObject, material = preset.material, useProceduralBackground = true))
                            }
                        }
                    )
                }
            }

            item {
                FaceItCard {
                    if (value.selectedArtworkFile == null) {
                        ChoiceChips(
                            title = "Background",
                            options = listOf(
                                "Football",
                                "Heritage architecture",
                                "Nature",
                                "Geometry",
                                "Cityscape",
                                "Automotive"
                            ),
                            selected = value.background,
                            onSelected = { change(value.copy(background = it, useProceduralBackground = true, nameGroundId = null)) }
                        )
                    } else {
                        LayerRow("IMPORTED BACKGROUND", "Using your artwork", Icons.Rounded.Image)
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onOpenLibrary,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("Manage imported artwork") }
                    }
                }
            }

            item {
                FaceItCard {
                    if (value.selectedObjectFile == null) {
                        ChoiceChips(
                            title = "Studio object",
                            supporting = "Drawn procedurally on the device, never sampled from a raster pack.",
                            options = listOf(
                                "Lion sculpture",
                                "Geometric crystal",
                                "Stethoscope",
                                "Mechanical sculpture",
                                "None"
                            ),
                            selected = value.mainObject,
                            onSelected = { change(value.copy(mainObject = it)) }
                        )
                        if (value.mainObject != "None") {
                            Spacer(Modifier.height(18.dp))
                            HorizontalDivider(color = colors.border.copy(alpha = .7f))
                            Spacer(Modifier.height(16.dp))
                            ChoiceChips(
                                title = "Material",
                                supporting = "Material is drawn procedurally, so it stays crisp at any size.",
                                options = MATERIAL_FINISHES,
                                selected = value.material,
                                onSelected = { change(value.copy(material = it)) }
                            )
                        }
                    } else {
                        LayerRow("IMPORTED OBJECT", "Using your artwork", Icons.Rounded.Layers)
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onOpenLibrary,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("Manage imported artwork") }
                    }
                }
            }

            item {
                FaceItCard {
                    Text("Object placement", style = MaterialTheme.typography.titleMedium)
                    Text("Move, scale and fade the object independently of the background.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Scale", style = MaterialTheme.typography.labelLarge)
                    Slider(value = value.objectScale, onValueChange = { adjust(value.copy(objectScale = it)) }, onValueChangeFinished = { finishAdjustment() },
                        valueRange = .25f..2f, modifier = Modifier.semantics { contentDescription = "Object scale" })
                    Text("Horizontal position", style = MaterialTheme.typography.labelLarge)
                    Slider(value = value.objectX, onValueChange = { adjust(value.copy(objectX = it)) }, onValueChangeFinished = { finishAdjustment() },
                        modifier = Modifier.semantics { contentDescription = "Object horizontal position" })
                    Text("Vertical position", style = MaterialTheme.typography.labelLarge)
                    Slider(value = value.objectY, onValueChange = { adjust(value.copy(objectY = it)) }, onValueChangeFinished = { finishAdjustment() },
                        modifier = Modifier.semantics { contentDescription = "Object vertical position" })
                    Text("Opacity", style = MaterialTheme.typography.labelLarge)
                    Slider(value = value.objectOpacity, onValueChange = { adjust(value.copy(objectOpacity = it)) }, onValueChangeFinished = { finishAdjustment() },
                        modifier = Modifier.semantics { contentDescription = "Object opacity" })
                    TextButton(onClick = { change(value.copy(objectScale = 1f, objectX = .5f, objectY = .38f, objectOpacity = 1f)) }) {
                        Text("Reset object placement")
                    }
                    if (value.selectedArtworkFile != null) {
                        HorizontalDivider()
                        Text("Imported background crop", style = MaterialTheme.typography.titleMedium)
                        Text("Choose the part of your image that fills the canvas.", style = MaterialTheme.typography.bodySmall)
                        Slider(value = value.cropX, onValueChange = { adjust(value.copy(cropX = it)) }, onValueChangeFinished = { finishAdjustment() },
                            modifier = Modifier.semantics { contentDescription = "Background horizontal crop" })
                        Slider(value = value.cropY, onValueChange = { adjust(value.copy(cropY = it)) }, onValueChangeFinished = { finishAdjustment() },
                            modifier = Modifier.semantics { contentDescription = "Background vertical crop" })
                        TextButton(onClick = { change(value.copy(cropX = .5f, cropY = .5f)) }) { Text("Centre crop") }
                    }
                }
            }

            item {
                InfoNote(
                    text = "Saving writes this environment to FaceIt's private storage on this device. Applying a wallpaper renders a new image and never replaces your stored artwork.",
                    tint = colors.ice
                )
            }
        }

        Surface(
            color = colors.card,
            border = BorderStroke(1.dp, colors.border.copy(alpha = .7f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (dirty) "Unsaved changes" else "Ready to save a copy",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = if (dirty) colors.warning else colors.success
                    )
                    Text(
                        "Saved to this device only",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = { onSave(value) },
                    enabled = value.name.isNotBlank(),
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Save")
                }
            }
        }
    }
}



