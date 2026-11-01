package com.zamcan.faceit.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zamcan.faceit.data.ArtworkRepository
import com.zamcan.faceit.data.LocalArtwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ArtworkStudioScreen(selectedFileName: String?, selectedObjectFileName: String?, onSelected: (LocalArtwork, String) -> Unit) {
    val context = LocalContext.current
    val repository = remember { ArtworkRepository(context) }
    val scope = rememberCoroutineScope()
    var artwork by remember { mutableStateOf(repository.list()) }
    var importing by remember { mutableStateOf(false) }
    var targetLayer by remember { mutableStateOf("Background") }

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
                    Toast.makeText(context, error.message ?: "Unable to import this image", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(22.dp))
        Text("Your artwork", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Text(
            "Import images from your phone. FaceIt copies them into private offline storage.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Text("Import this image as", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = targetLayer == "Background", onClick = { targetLayer = "Background" }, label = { Text("Background") })
            FilterChip(selected = targetLayer == "Object", onClick = { targetLayer = "Object" }, label = { Text("Main object") })
        }
        if (targetLayer == "Object") {
            Text(
                "Transparent PNG artwork works best for a clean sculptural layer.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Button(
            onClick = { picker.launch(arrayOf("image/png", "image/jpeg", "image/webp")) },
            enabled = !importing,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text(if (importing) "Importing image…" else "Import artwork from device")
        }
        Spacer(Modifier.height(16.dp))
        Text("Imported on this device · " + artwork.size, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        if (artwork.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Image, null, modifier = Modifier.size(38.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Your local collection is empty", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Import a PNG, JPEG or WebP image to use it as a real wallpaper.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(artwork, key = { it.id }) { item ->
                    val file = repository.fileFor(item.fileName)
                    val isSelected = if (targetLayer == "Object") item.fileName == selectedObjectFileName else item.fileName == selectedFileName
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onSelected(item, targetLayer) },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (file != null) ArtworkThumbnail(file)
                            Spacer(Modifier.size(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.title, fontWeight = FontWeight.SemiBold)
                                Text(
                                    item.width.toString() + " × " + item.height + " · " + item.mimeType.substringAfter('/'),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    if (isSelected) "Selected as " + targetLayer.lowercase() else "Tap to use as " + targetLayer.lowercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (isSelected) Icon(Icons.Rounded.CheckCircle, contentDescription = "Selected")
                        }
                    }
                }
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
            modifier = modifier.size(width = 84.dp, height = 112.dp),
            contentScale = ContentScale.Crop
        )
        DisposableEffect(bitmap) { onDispose { bitmap.recycle() } }
    }
}

private fun decodeThumbnail(file: File): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    while (bounds.outWidth / (sample * 2) > 240 && bounds.outHeight / (sample * 2) > 320) sample *= 2
    return BitmapFactory.decodeFile(
        file.absolutePath,
        BitmapFactory.Options().apply { inSampleSize = sample; inPreferredConfig = Bitmap.Config.RGB_565 }
    )
}

@Composable
fun ArtworkHero(file: File, modifier: Modifier = Modifier) {
    val bitmap = remember(file.absolutePath, file.lastModified()) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) null else {
            var sample = 1
            while (bounds.outWidth / (sample * 2) > 1200 && bounds.outHeight / (sample * 2) > 1800) sample *= 2
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

@Composable
fun CompositionPreview(
    environment: com.zamcan.faceit.data.PersonalEnvironment,
    artworkFile: File?,
    objectArtworkFile: File?,
    modifier: Modifier = Modifier
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(environment, artworkFile?.absolutePath, artworkFile?.lastModified(), objectArtworkFile?.absolutePath, objectArtworkFile?.lastModified()) {
        val next = withContext(Dispatchers.Default) {
            com.zamcan.faceit.graphics.WallpaperRenderer.render(540, 960, environment, artworkFile, objectArtworkFile)
        }
        val previous = bitmap
        bitmap = next
        previous?.recycle()
    }
    DisposableEffect(Unit) {
        onDispose { bitmap?.recycle() }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "Live preview of the FaceIt composition",
            modifier = modifier.clip(RoundedCornerShape(28.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier.clip(RoundedCornerShape(28.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
