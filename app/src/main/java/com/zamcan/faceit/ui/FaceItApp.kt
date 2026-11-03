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
import com.zamcan.faceit.data.ArtworkRepository
import com.zamcan.faceit.data.CuratedArtwork
import com.zamcan.faceit.data.CompositionPresets
import com.zamcan.faceit.data.CuratedLibrary
import com.zamcan.faceit.data.EnvironmentStore
import com.zamcan.faceit.data.PersonalEnvironment
import com.zamcan.faceit.data.ThemeWallpaperMode
import com.zamcan.faceit.data.applyingTheme
import com.zamcan.faceit.graphics.WallpaperRenderer
import com.zamcan.faceit.ui.theme.LocalFaceItColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class Destination(val label: String) {
    Discover("Discover"),
    Themes("Themes"),
    Create("Create"),
    Library("Library"),
    Tools("Tools")
}

internal enum class ToolsPage(val label: String) {
    Home("Tools"),
    KeyboardDesigns("Keyboard designs"),
    Vault("Private Vault"),
    Sms("SMS composer")
}

@Composable
fun FaceItApp() {
    val context = LocalContext.current
    val store = remember { EnvironmentStore(context) }
    val artworkRepository = remember { ArtworkRepository(context) }
    val curatedLibrary = remember { CuratedLibrary(context) }
    var showGallery by remember { mutableStateOf(false) }
    var environment by remember { mutableStateOf(store.load()) }
    var destination by remember { mutableStateOf(Destination.Discover) }
    var toolsPage by remember { mutableStateOf(ToolsPage.Home) }
    var visualPage by remember { mutableStateOf(VisualPage.Themes) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    // AGENTS.md §13/§17: one runtime policy decides whether motion may animate.
    val motionPolicy = rememberMotionPolicy()

    fun update(next: PersonalEnvironment) {
        environment = next
        store.save(next)
    }

    // Curated layers are decoded off the main thread and released as soon as the
    // selection changes, so browsing cannot accumulate full-size bitmaps.
    var curatedBackground by remember { mutableStateOf<Bitmap?>(null) }
    var curatedObject by remember { mutableStateOf<Bitmap?>(null) }
    var materialTexture by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(environment.curatedArtworkId) {
        curatedBackground?.takeUnless { it.isRecycled }?.recycle()
        curatedBackground = null
        val id = environment.curatedArtworkId ?: return@LaunchedEffect
        curatedBackground = withContext(Dispatchers.IO) {
            curatedLibrary.find(id)?.let { curatedLibrary.fullSize(it, 1080) }
        }
    }
    LaunchedEffect(environment.curatedObjectId) {
        curatedObject?.takeUnless { it.isRecycled }?.recycle()
        curatedObject = null
        val id = environment.curatedObjectId ?: return@LaunchedEffect
        curatedObject = withContext(Dispatchers.IO) { curatedLibrary.objectArtwork(id, 900) }
    }
    LaunchedEffect(environment.material) {
        materialTexture?.takeUnless { it.isRecycled }?.recycle()
        materialTexture = null
        val finish = CuratedLibrary.textureForFinish(environment.material) ?: return@LaunchedEffect
        materialTexture = withContext(Dispatchers.IO) { curatedLibrary.texture(finish, 512) }
    }
    DisposableEffect(Unit) {
        onDispose {
            curatedBackground?.takeUnless { it.isRecycled }?.recycle()
            curatedObject?.takeUnless { it.isRecycled }?.recycle()
            materialTexture?.takeUnless { it.isRecycled }?.recycle()
            curatedLibrary.trimMemory()
        }
    }

    fun applyWallpaper(targetFlags: Int) {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val bitmap = WallpaperRenderer.render(
                        width = 1080,
                        height = 2400,
                        environment = environment,
                        artworkFile = artworkRepository.fileFor(environment.selectedArtworkFile.orEmpty()),
                        objectArtworkFile = artworkRepository.fileFor(environment.selectedObjectFile.orEmpty()),
                        curatedArtwork = curatedBackground,
                        curatedObject = curatedObject,
                        materialTexture = materialTexture
                    )
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

    fun applyCurated(artwork: CuratedArtwork, targetFlags: Int) {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val bitmap = curatedLibrary.fullSize(artwork, 1440)
                        ?: error("The bundled wallpaper could not be decoded")
                    try {
                        WallpaperManager.getInstance(context).setBitmap(bitmap, null, true, targetFlags)
                    } finally {
                        bitmap.recycle()
                    }
                }
            }
            result.onSuccess {
                snackbarHostState.showSnackbar("${artwork.title} applied")
            }.onFailure {
                snackbarHostState.showSnackbar("Unable to apply this wallpaper")
            }
        }
    }

    BackHandler(
        enabled = showGallery ||
            destination != Destination.Discover ||
            visualPage != VisualPage.Themes ||
            toolsPage != ToolsPage.Home
    ) {
        if (showGallery) {
            showGallery = false
        } else if (destination == Destination.Themes && visualPage != VisualPage.Themes) {
            visualPage = VisualPage.Themes
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
                        curatedCount = curatedLibrary.wallpapers().size,
                        curatedBackground = curatedBackground,
                        curatedObject = curatedObject,
                        materialTexture = materialTexture,
                        onOpenGallery = { showGallery = true },
                        onCreate = { destination = Destination.Create },
                        onPreset = { preset ->
                            update(preset)
                            scope.launch { snackbarHostState.showSnackbar("Loaded ${preset.name}") }
                        },
                        onApply = { flags -> applyWallpaper(flags) }
                    )

                    Destination.Create -> CompositionScreen(
                        initial = environment,
                        artworkFile = artworkRepository.fileFor(environment.selectedArtworkFile.orEmpty()),
                        objectArtworkFile = artworkRepository.fileFor(environment.selectedObjectFile.orEmpty()),
                        modifier = Modifier.padding(padding),
                        onSave = {
                            update(it)
                            scope.launch { snackbarHostState.showSnackbar("Environment saved on this device") }
                        },
                        onOpenLibrary = { destination = Destination.Library }
                    )

                    Destination.Themes -> VisualStudioScreen(
                        page = visualPage,
                        selectedThemeId = environment.themeId,
                        onOpen = { visualPage = it },
                        onThemeSelected = { theme ->
                            update(
                                environment.applyingTheme(
                                    theme,
                                    keepWallpaper = environment.wallpaperMode == ThemeWallpaperMode.KEEP
                                )
                            )
                            scope.launch {
                                snackbarHostState.showSnackbar("${theme.name} applied")
                            }
                        },
                        modifier = Modifier.padding(padding)
                    )

                    Destination.Library -> ArtworkStudioScreen(
                        selectedFileName = environment.selectedArtworkFile,
                        selectedObjectFileName = environment.selectedObjectFile,
                        modifier = Modifier.padding(padding),
                        onSelected = { selected, layer ->
                            update(
                                if (layer == "Object") {
                                    environment.copy(selectedObjectFile = selected.fileName)
                                } else {
                                    environment.copy(selectedArtworkFile = selected.fileName)
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

                    Destination.Tools -> ToolsHubScreen(
                        page = toolsPage,
                        modifier = Modifier.padding(padding),
                        onOpen = { toolsPage = it },
                        onBack = { toolsPage = ToolsPage.Home }
                    )
                }
            }
        }

        // Curated pack browser, drawn above the workspace so it keeps the same
        // backdrop instead of stacking a second theme on top of it.
        if (showGallery) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(LocalFaceItColors.current.backdropTop)
            ) {
                CuratedGalleryScreen(
                    modifier = Modifier.fillMaxSize().padding(top = 24.dp),
                    selectedId = environment.curatedArtworkId,
                    onSelect = { artwork ->
                        update(environment.copy(curatedArtworkId = artwork.id))
                        showGallery = false
                        scope.launch {
                            snackbarHostState.showSnackbar("${artwork.title} set as background")
                        }
                    },
                    onApply = { artwork -> applyCurated(artwork, WallpaperManager.FLAG_SYSTEM) },
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
    Column(Modifier.fillMaxWidth().background(colors.card)) {
        HorizontalDivider(color = colors.border)
        NavigationBar(
            containerColor = Color.Transparent,
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
                            contentDescription = item.label,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(item.label, style = MaterialTheme.typography.labelSmall)
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.violet,
                        selectedTextColor = colors.violet,
                        indicatorColor = colors.violet.copy(alpha = .13f),
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
    curatedCount: Int,
    curatedBackground: Bitmap?,
    curatedObject: Bitmap?,
    materialTexture: Bitmap?,
    onOpenGallery: () -> Unit,
    onCreate: () -> Unit,
    onPreset: (PersonalEnvironment) -> Unit,
    onApply: (Int) -> Unit
) {
    val colors = LocalFaceItColors.current
    var showApplyDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FaceItWordmark(Modifier.weight(1f))
                StatusPill(text = "On-device", icon = Icons.Rounded.Lock, tint = colors.mint)
            }
        }

        item {
            Column {
                Text(
                    text = "Your world,\ncomposed.",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Independent visual layers. One personal environment, built offline.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .border(1.dp, colors.border, MaterialTheme.shapes.extraLarge)
            ) {
                CompositionPreview(
                    environment = environment,
                    artworkFile = artworkFile,
                    objectArtworkFile = artworkObjectFile,
                    curatedBackground = curatedBackground,
                    curatedObject = curatedObject,
                    materialTexture = materialTexture,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = .72f))
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusPill(text = "Live preview", tint = colors.mint)
                    if (artworkFile != null || artworkObjectFile != null) {
                        StatusPill(text = "Your artwork", tint = colors.ice)
                    }
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(18.dp)
                ) {
                    Text(
                        text = environment.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${environment.background} · ${environment.mainObject} · ${environment.material}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = .78f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { showApplyDialog = true },
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(Icons.Rounded.Wallpaper, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Apply wallpaper", maxLines = 1)
                }
                OutlinedButton(
                    onClick = onCreate,
                    modifier = Modifier.height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, colors.border),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("Edit")
                }
            }
        }

        item {
            SectionHeader(
                title = "Layer stack",
                supporting = "Each layer stays independently editable.",
                actionLabel = "Edit",
                onAction = onCreate
            )
        }

        item {
            FaceItCard {
                LayerRow("BACKGROUND", environment.background, Icons.Rounded.Image)
                HorizontalDivider(color = colors.border.copy(alpha = .7f))
                LayerRow("MAIN OBJECT", environment.mainObject, Icons.Rounded.Layers)
                HorizontalDivider(color = colors.border.copy(alpha = .7f))
                LayerRow("MATERIAL", environment.material, Icons.Rounded.Wallpaper)
            }
        }

        item {
            FaceItGlass(onClick = onOpenGallery, tone = GlassTone.Accent) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(text = "Curated artwork", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "$curatedCount bundled wallpapers across heritage, sport, " +
                                "nature, science, technology, civic and design collections. All offline.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Rounded.Collections,
                        contentDescription = "Open curated artwork",
                        tint = colors.violet,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = "Start from a preset",
                supporting = "A composed starting point, still fully editable."
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(CompositionPresets.all, key = { it.name }) { preset ->
                    PresetCard(
                        preset = preset,
                        active = preset.name == environment.name,
                        onClick = { onPreset(preset) }
                    )
                }
            }
        }

        item {
            FaceItCard(contentPadding = 0.dp) {
                InfoNote(
                    text = if (artworkFile == null && artworkObjectFile == null) {
                        "Add your own background or object artwork in Library. Imports are copied into private, offline storage."
                    } else {
                        "Your imported artwork stays on this device and continues to work without a connection."
                    },
                    tint = colors.mint
                )
            }
        }
    }

    if (showApplyDialog) {
        ApplyWallpaperDialog(
            onDismiss = { showApplyDialog = false },
            onApply = { flags ->
                showApplyDialog = false
                onApply(flags)
            }
        )
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
private fun ApplyWallpaperDialog(
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
    val gallery = remember { CuratedLibrary(context) }
    var value by remember(initial) { mutableStateOf(initial) }
    var studioBackground by remember { mutableStateOf<Bitmap?>(null) }
    var studioObject by remember { mutableStateOf<Bitmap?>(null) }
    var studioTexture by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(value.curatedArtworkId) {
        studioBackground = withContext(Dispatchers.IO) {
            value.curatedArtworkId?.let { gallery.find(it) }?.let { gallery.fullSize(it, 540) }
        }
    }
    LaunchedEffect(value.curatedObjectId) {
        studioObject = withContext(Dispatchers.IO) {
            value.curatedObjectId?.let { gallery.objectArtwork(it, 400) }
        }
    }
    LaunchedEffect(value.material) {
        studioTexture = withContext(Dispatchers.IO) {
            CuratedLibrary.textureForFinish(value.material)?.let { gallery.texture(it, 256) }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            studioBackground?.takeUnless { it.isRecycled }?.recycle()
            studioObject?.takeUnless { it.isRecycled }?.recycle()
            studioTexture?.takeUnless { it.isRecycled }?.recycle()
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
                        curatedObject = studioObject,
                        materialTexture = studioTexture,
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
                            CompositionPresets.find(name)?.let { value = it }
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
                                "Islamic architecture",
                                "Nature",
                                "Geometry",
                                "Cityscape",
                                "Automotive"
                            ),
                            selected = value.background,
                            onSelected = { value = value.copy(background = it) }
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
                        CuratedObjectPicker(
                            selectedId = value.curatedObjectId,
                            onSelected = { value = value.copy(curatedObjectId = it) }
                        )
                        Spacer(Modifier.height(18.dp))
                        HorizontalDivider(color = colors.border.copy(alpha = .7f))
                        Spacer(Modifier.height(16.dp))
                        ChoiceChips(
                            title = "Studio object",
                            supporting = "The drawn recipe used when no curated object is selected.",
                            options = listOf(
                                "Lion sculpture",
                                "Geometric crystal",
                                "Stethoscope",
                                "Mechanical sculpture",
                                "None"
                            ),
                            selected = value.mainObject,
                            onSelected = { value = value.copy(mainObject = it) }
                        )
                        if (value.mainObject != "None" || value.curatedObjectId != null) {
                            Spacer(Modifier.height(18.dp))
                            HorizontalDivider(color = colors.border.copy(alpha = .7f))
                            Spacer(Modifier.height(16.dp))
                            ChoiceChips(
                                title = "Material",
                                supporting = "Curated finishes are real material tiles, rendered into the result.",
                                options = CuratedLibrary.TEXTURES.map { it.label },
                                selected = value.material,
                                onSelected = { value = value.copy(material = it) }
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
                        if (dirty) "Unsaved changes" else "All changes saved",
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
                    enabled = dirty,
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

@Composable
private fun CuratedObjectPicker(
    selectedId: String?,
    onSelected: (String?) -> Unit
) {
    val colors = LocalFaceItColors.current
    val context = LocalContext.current
    val library = remember { CuratedLibrary(context) }

    Text(text = "Curated object", style = MaterialTheme.typography.titleSmall)
    Text(
        text = "Ten object renders from the bundled pack, drawn as your main object.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(10.dp))

    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Surface(
                onClick = { onSelected(null) },
                shape = MaterialTheme.shapes.medium,
                color = if (selectedId == null) colors.violet.copy(alpha = .14f) else colors.cardRaised,
                border = BorderStroke(
                    1.dp,
                    if (selectedId == null) colors.violet.copy(alpha = .6f) else colors.border
                )
            ) {
                Box(Modifier.size(84.dp), contentAlignment = Alignment.Center) {
                    Text("None", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        items(CuratedLibrary.OBJECT_IDS) { id ->
            val selected = id == selectedId
            var thumb by remember(id) { mutableStateOf<Bitmap?>(null) }
            LaunchedEffect(id) {
                thumb = withContext(Dispatchers.IO) { library.objectArtwork(id, 200) }
            }
            Surface(
                onClick = { onSelected(if (selected) null else id) },
                shape = MaterialTheme.shapes.medium,
                color = if (selected) colors.violet.copy(alpha = .14f) else colors.cardRaised,
                border = BorderStroke(
                    if (selected) 1.5.dp else 1.dp,
                    if (selected) colors.violet.copy(alpha = .6f) else colors.border
                )
            ) {
                Box(
                    modifier = Modifier.size(84.dp).padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val current = thumb
                    if (current != null) {
                        Image(
                            bitmap = current.asImageBitmap(),
                            contentDescription = CuratedLibrary.objectLabel(id),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = colors.violet
                        )
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    if (selectedId != null) {
        TextButton(onClick = { onSelected(null) }) { Text("Clear curated object") }
    }
}
