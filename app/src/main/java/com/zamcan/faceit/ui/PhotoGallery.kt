package com.zamcan.faceit.ui

import android.graphics.Bitmap
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.runtime.saveable.rememberSaveable
import com.zamcan.faceit.data.PhotoCatalogueQuery
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zamcan.faceit.data.PhotoBitmaps
import com.zamcan.faceit.data.PhotoLibrary
import com.zamcan.faceit.data.PhotoWallpaper
import com.zamcan.faceit.ui.theme.LocalFaceItColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The wallpaper gallery. Every wallpaper the app ships is here.
 *
 * These are other people's photographs. The credit is therefore part of the tile
 * rather than something to be found in a settings page: the licence that lets us
 * ship a photograph also requires the photographer's name to travel with it.
 * Nothing here is downloaded — every file is bundled and works with no network.
 */
@Composable
fun PhotoGalleryScreen(
    modifier: Modifier = Modifier,
    selectedId: String?,
    onSelect: (PhotoWallpaper) -> Unit,
    onApply: (PhotoWallpaper, Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val bitmaps = remember { PhotoBitmaps(context) }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var favouritesOnly by rememberSaveable { mutableStateOf(false) }
    val preferences = remember { context.getSharedPreferences("faceit_photo_favourites", android.content.Context.MODE_PRIVATE) }
    var favourites by remember { mutableStateOf(preferences.getStringSet("ids", emptySet())?.toSet() ?: emptySet()) }
    val all = remember { PhotoLibrary.all(context) }
    val photos = remember(category, query, favouritesOnly, favourites) {
        PhotoCatalogueQuery.filter(all, query, category, favouritesOnly, favourites)
    }
    var applying by remember { mutableStateOf<PhotoWallpaper?>(null) }
    var inspecting by remember { mutableStateOf<PhotoWallpaper?>(null) }
    val total = all.size

    DisposableEffect(bitmaps) {
        onDispose { bitmaps.trimMemory() }
    }

    Column(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScreenHeader(
                eyebrow = "Offline collection",
                title = "Wallpapers",
                description = buildString {
                    append("$total wallpapers: credited photography and labelled generated artwork. ")
                    append("Licensed for reuse — see each credit line.")
                },
                onBack = onBack
            )
        }

        OutlinedTextField(
            value = query, onValueChange = { query = it },
            label = { Text("Search wallpapers") }, singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(10.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { PhotoCategoryChip("Favourites", favouritesOnly) { favouritesOnly = !favouritesOnly } }
            item { PhotoCategoryChip("All", category == null) { category = null } }
            items(PhotoLibrary.categories(context), key = { it }) { name ->
                PhotoCategoryChip(name.replaceFirstChar { it.uppercase() }, category == name) {
                    category = if (category == name) null else name
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        if (photos.isEmpty()) {
            Text("No wallpapers match. Try another search or turn off Favourites.",
                modifier = Modifier.padding(20.dp), style = MaterialTheme.typography.bodyMedium)
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 168.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(photos, key = { it.id }) { photo ->
                PhotoTile(
                    bitmaps = bitmaps,
                    photo = photo,
                    selected = photo.id == selectedId,
                    onSelect = { onSelect(photo) },
                    onApply = { applying = photo },
                    onInspect = { inspecting = photo },
                    favourite = photo.id in favourites,
                    onFavourite = {
                        val next = if (photo.id in favourites) favourites - photo.id else favourites + photo.id
                        if (preferences.edit().putStringSet("ids", next).commit()) favourites = next
                        else android.widget.Toast.makeText(context, "Could not save favourite", android.widget.Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
    applying?.let { photo ->
        ApplyWallpaperDialog(onDismiss = { applying = null }, onApply = { flags ->
            applying = null; onApply(photo, flags)
        })
    }
    inspecting?.let { photo ->
        PhotoDetailsDialog(photo, bitmaps, onDismiss = { inspecting = null })
    }
}

@Composable
private fun PhotoCategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
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
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.violet else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PhotoTile(
    bitmaps: PhotoBitmaps,
    photo: PhotoWallpaper,
    selected: Boolean,
    onSelect: () -> Unit,
    onApply: () -> Unit,
    onInspect: () -> Unit,
    favourite: Boolean,
    onFavourite: () -> Unit
) {
    val colors = LocalFaceItColors.current
    var bitmap by remember(photo.id) { mutableStateOf<Bitmap?>(null) }
    var failed by remember(photo.id) { mutableStateOf(false) }

    LaunchedEffect(photo.id) {
        val decoded = withContext(Dispatchers.IO) { bitmaps.thumbnail(photo, 360) }
        if (decoded == null) failed = true else bitmap = decoded
    }

    FaceItGlass(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "${photo.title} by ${photo.creator}, ${photo.licence}, ${if (photo.synthetic) "generated artwork" else "photograph"}"
            },
        tone = if (selected) GlassTone.Accent else GlassTone.Standard,
        contentPadding = 8.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.cardRaised)
            ) {
                val current = bitmap
                when {
                    current != null -> Image(
                        bitmap = current.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    failed -> Text(
                        text = "Unavailable",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    else -> CircularProgressIndicator(
                        modifier = Modifier.size(20.dp).align(Alignment.Center),
                        strokeWidth = 2.dp,
                        color = colors.violet
                    )
                }

                IconButton(onClick = onFavourite, modifier = Modifier.align(Alignment.TopStart)
                    .padding(4.dp).background(Color(0xCC101318), CircleShape)) {
                    Icon(if (favourite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (favourite) "Remove favourite" else "Add favourite",
                        tint = Color.White)
                }
                if (selected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(22.dp)
                            .background(colors.mint, CircleShape)
                    )
                }

                // Share-alike is disclosed on the tile, not only in the credit file.
                if (photo.shareAlike) {
                    Text(
                        text = "BY-SA",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.violet,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(Color(0xCC0A0B0F), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = photo.subject.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (photo.synthetic) {
                    "Generated · ${photo.licence.uppercase()}"
                } else {
                    "${photo.creator} · ${photo.licence.uppercase()}"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onSelect, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text("Use", style = MaterialTheme.typography.labelMedium)
                }
                TextButton(onClick = onInspect, contentPadding = PaddingValues(horizontal = 4.dp)) {
                    Text("Details", style = MaterialTheme.typography.labelMedium)
                }
                TextButton(onClick = onApply, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text("Set", style = MaterialTheme.typography.labelMedium, color = colors.mint)
                }
            }
        }
    }
}


@Composable
private fun PhotoDetailsDialog(photo: PhotoWallpaper, bitmaps: PhotoBitmaps, onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    var bitmap by remember(photo.id) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(photo.id) { bitmap = withContext(Dispatchers.IO) { bitmaps.fullSize(photo, 720) } }
    fun openLink(url: String) {
        runCatching {
            require(url.startsWith("https://") || url.startsWith("http://"))
            uriHandler.openUri(url)
        }.onFailure {
            android.widget.Toast.makeText(context, "This link could not be opened.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(photo.title) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                bitmap?.let { image ->
                    Image(image.asImageBitmap(), contentDescription = photo.title,
                        modifier = Modifier.fillMaxWidth().height(240.dp), contentScale = ContentScale.Fit)
                }
                Text(if (photo.synthetic) "Generated artwork" else "Photograph by ${photo.creator}")
                Text(photo.credit, style = MaterialTheme.typography.bodySmall)
                Text("${photo.width} × ${photo.height}", style = MaterialTheme.typography.labelSmall)
                if (photo.shareAlike) Text("Share-alike licence: adaptations must follow the linked licence terms.",
                    style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { openLink(photo.licenceUrl) }) { Text("Read licence") }
                if (photo.source.isNotBlank()) {
                    TextButton(onClick = { openLink(photo.source) }) { Text("View original source") }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}

