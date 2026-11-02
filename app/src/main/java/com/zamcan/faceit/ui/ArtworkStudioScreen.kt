package com.zamcan.faceit.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.util.Locale
import com.zamcan.faceit.data.ArtworkRepository
import com.zamcan.faceit.data.LocalArtwork
import com.zamcan.faceit.ui.theme.FaceItSpacing
import com.zamcan.faceit.ui.theme.LocalFaceItColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ArtworkStudioScreen(
    selectedFileName: String?,
    selectedObjectFileName: String?,
    modifier: Modifier = Modifier,
    onSelected: (LocalArtwork, String) -> Unit,
    onClearLayer: (String) -> Unit
) {
    val context = LocalContext.current
    val colors = LocalFaceItColors.current
    val repository = remember { ArtworkRepository(context) }
    val scope = rememberCoroutineScope()
    var artwork by remember { mutableStateOf(repository.list()) }
    var importing by remember { mutableStateOf(false) }
    var targetLayer by remember { mutableStateOf("Background") }
    var pendingDelete by remember { mutableStateOf<LocalArtwork?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            importing = true
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching { repository.importImage(context, uri) }
                }
                importing = false
                result.onSuccess { imported ->
                    artwork = repository.list()
                    onSelected(imported, targetLayer)
                    Toast.makeText(context, "Artwork imported into FaceIt", Toast.LENGTH_SHORT).show()
                }.onFailure { error ->
                    Toast.makeText(
                        context,
                        error.message ?: "Unable to import this image",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    val currentLayerSelection = if (targetLayer == "Object") selectedObjectFileName else selectedFileName
    val totalBytes = remember(artwork) {
        artwork.sumOf { repository.fileFor(it.fileName)?.length() ?: 0L }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(18.dp))
        ScreenHeader(
            eyebrow = "Library",
            title = "Your artwork",
            description = "Imported images are copied into private, offline storage.",
            trailing = {
                StatusPill(text = "${artwork.size} items", tint = colors.mint)
            }
        )

        Spacer(Modifier.height(18.dp))

        SegmentedChoice(
            options = listOf("Background" to "Background", "Object" to "Main object"),
            selected = targetLayer,
            modifier = Modifier.fillMaxWidth(),
            onSelected = { targetLayer = it }
        )

        Spacer(Modifier.height(10.dp))
        Text(
            text = if (targetLayer == "Object") {
                "Transparent PNG artwork gives the cleanest sculptural layer."
            } else {
                "Use a portrait image for the best full-screen wallpaper crop."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { picker.launch(arrayOf("image/png", "image/jpeg", "image/webp")) },
                enabled = !importing,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                if (importing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null)
                }
                Spacer(Modifier.width(8.dp))
                Text(if (importing) "Importing…" else "Import artwork")
            }
            if (!currentLayerSelection.isNullOrBlank()) {
                OutlinedButton(
                    onClick = { onClearLayer(targetLayer) },
                    modifier = Modifier.height(52.dp),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Text("Clear")
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (artwork.isEmpty()) "No local artwork yet"
                else "${artwork.size} imported · ${formatBytes(totalBytes)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "PNG · JPEG · WebP",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(14.dp))

        if (artwork.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.Image,
                title = "Your local collection is empty",
                body = "Import a PNG, JPEG or WebP image to use it as a real wallpaper or sculptural object. Files never leave this device.",
                modifier = Modifier.fillMaxWidth(),
                action = {
                    Button(
                        onClick = { picker.launch(arrayOf("image/png", "image/jpeg", "image/webp")) },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Import artwork")
                    }
                }
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 156.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(artwork, key = { it.id }) { item ->
                    val file = repository.fileFor(item.fileName)
                    val selectedAsBackground = item.fileName == selectedFileName
                    val selectedAsObject = item.fileName == selectedObjectFileName
                    val isSelected = if (targetLayer == "Object") selectedAsObject else selectedAsBackground
                    ArtworkCard(
                        item = item,
                        file = file,
                        selected = isSelected,
                        usedAsBackground = selectedAsBackground,
                        usedAsObject = selectedAsObject,
                        onClick = { onSelected(item, targetLayer) },
                        onDelete = { pendingDelete = item }
                    )
                }
            }
        }
    }

    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = colors.card,
            shape = MaterialTheme.shapes.extraLarge,
            title = { Text("Remove artwork?") },
            text = {
                Text(
                    "“${item.title}” will be deleted from FaceIt's private storage. " +
                        "Wallpaper applied to your phone is not affected."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val wasBackground = item.fileName == selectedFileName
                    val wasObject = item.fileName == selectedObjectFileName
                    if (repository.delete(item.id)) {
                        artwork = repository.list()
                        if (wasBackground) onClearLayer("Background")
                        if (wasObject) onClearLayer("Object")
                        Toast.makeText(context, "Artwork removed", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Could not remove artwork", Toast.LENGTH_LONG).show()
                    }
                    pendingDelete = null
                }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Keep") }
            }
        )
    }
}

@Composable
private fun ArtworkCard(
    item: LocalArtwork,
    file: File?,
    selected: Boolean,
    usedAsBackground: Boolean,
    usedAsObject: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalFaceItColors.current
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = colors.card,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) colors.violet.copy(alpha = .7f) else colors.border
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.large)
                    .background(colors.cardRaised)
            ) {
                if (file != null) {
                    ArtworkThumbnail(file, Modifier.fillMaxSize())
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center).size(28.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = .5f))
                            )
                        )
                )
                Row(
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .background(colors.mint, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Selected",
                                tint = Color(0xFF071411),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(30.dp)
                            .background(colors.scrim, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Remove ${item.title}",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.width} × ${item.height}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = .72f)
                    )
                }
            }
            Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    text = when {
                        usedAsBackground && usedAsObject -> "Background + object"
                        usedAsBackground -> "Used as background"
                        usedAsObject -> "Used as main object"
                        selected -> "Selected"
                        else -> "Tap to use"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) colors.violet else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ArtworkThumbnail(file: File, modifier: Modifier = Modifier) {
    val bitmap = remember(file.absolutePath, file.lastModified()) { decodeThumbnail(file) }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Imported artwork preview",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
        DisposableEffect(bitmap) { onDispose { bitmap.recycle() } }
    }
}

private fun decodeThumbnail(file: File): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    val sample = previewSampleSize(bounds.outWidth, bounds.outHeight, 480, 480)
    return BitmapFactory.decodeFile(
        file.absolutePath,
        BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.RGB_565
        }
    )
}

@Composable
fun ArtworkHero(file: File, modifier: Modifier = Modifier) {
    val bitmap = remember(file.absolutePath, file.lastModified()) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) null else {
            val sample = previewSampleSize(bounds.outWidth, bounds.outHeight, 1200, 1800)
            BitmapFactory.decodeFile(
                file.absolutePath,
                BitmapFactory.Options().apply {
                    inSampleSize = sample
                    inPreferredConfig = Bitmap.Config.RGB_565
                }
            )
        }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Selected FaceIt wallpaper artwork",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
        DisposableEffect(bitmap) { onDispose { bitmap.recycle() } }
    }
}

private fun previewSampleSize(sourceWidth: Int, sourceHeight: Int, targetWidth: Int, targetHeight: Int): Int {
    val pixelBudget = targetWidth.toLong() * targetHeight.toLong() * 4L
    var sample = 1
    while ((sourceWidth.toLong() / (sample * 2L)) *
        (sourceHeight.toLong() / (sample * 2L)) > pixelBudget
    ) sample *= 2
    return sample
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1024L * 1024L -> String.format(Locale.US, "%.1f MB", bytes / (1024f * 1024f))
    bytes >= 1024L -> String.format(Locale.US, "%.0f KB", bytes / 1024f)
    else -> "$bytes B"
}

@Composable
fun CompositionPreview(
    environment: com.zamcan.faceit.data.PersonalEnvironment,
    artworkFile: File?,
    objectArtworkFile: File?,
    modifier: Modifier = Modifier,
    curatedBackground: Bitmap? = null,
    curatedObject: Bitmap? = null,
    materialTexture: Bitmap? = null
) {
    val colors = LocalFaceItColors.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(
        environment,
        artworkFile?.absolutePath,
        artworkFile?.lastModified(),
        objectArtworkFile?.absolutePath,
        objectArtworkFile?.lastModified(),
        curatedBackground,
        curatedObject,
        materialTexture
    ) {
        val next = withContext(Dispatchers.Default) {
            com.zamcan.faceit.graphics.WallpaperRenderer.render(
                width = 540,
                height = 960,
                environment = environment,
                artworkFile = artworkFile,
                objectArtworkFile = objectArtworkFile,
                curatedArtwork = curatedBackground,
                curatedObject = curatedObject,
                materialTexture = materialTexture
            )
        }
        val previous = bitmap
        bitmap = next
        previous?.recycle()
    }
    DisposableEffect(Unit) {
        onDispose { bitmap?.recycle() }
    }
    val current = bitmap
    if (current != null) {
        Image(
            bitmap = current.asImageBitmap(),
            contentDescription = "Live preview of the FaceIt composition",
            modifier = modifier.clip(MaterialTheme.shapes.extraLarge),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .clip(MaterialTheme.shapes.extraLarge)
                .background(colors.cardRaised),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(26.dp),
                strokeWidth = 2.dp,
                color = colors.violet
            )
        }
    }
}
