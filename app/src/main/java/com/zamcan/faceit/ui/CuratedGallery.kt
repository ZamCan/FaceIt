package com.zamcan.faceit.ui

import android.graphics.Bitmap
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zamcan.faceit.data.CuratedArtwork
import com.zamcan.faceit.data.CuratedLibrary
import com.zamcan.faceit.ui.theme.LocalFaceItColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Curated artwork gallery over the bundled pack.
 *
 * Ninety-nine of these wallpapers are generated graphic artwork shipped offline;
 * the gallery never downloads anything and never claims a pack it does not have.
 * Thumbnails are decoded off the main thread at the size actually displayed, and
 * the full-resolution file is decoded only when a wallpaper is applied.
 */
@Composable
fun CuratedGalleryScreen(
    modifier: Modifier = Modifier,
    selectedId: String?,
    onSelect: (CuratedArtwork) -> Unit,
    onApply: (CuratedArtwork) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalFaceItColors.current
    val library = remember { CuratedLibrary(context) }
    var category by remember { mutableStateOf<String?>(null) }
    val items = remember(category) { library.byCategory(category) }

    Column(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScreenHeader(
                eyebrow = "Discover",
                title = "Curated artwork",
                description = "${library.wallpapers().size} bundled wallpapers, offline and rights-recorded.",
                onBack = onBack
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                CategoryChip("All", category == null) { category = null }
            }
            val categories = library.categories()
            items(categories, key = { it }) { name ->
                CategoryChip(name, category == name) {
                    category = if (category == name) null else name
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 152.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.id }) { artwork ->
                ArtworkTile(
                    library = library,
                    artwork = artwork,
                    selected = artwork.id == selectedId,
                    onSelect = { onSelect(artwork) },
                    onApply = { onApply(artwork) }
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalFaceItColors.current
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) colors.violet.copy(alpha = .16f) else colors.card,
        border = BorderStroke(1.dp, if (selected) colors.violet.copy(alpha = .5f) else colors.border)
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

@Composable
private fun ArtworkTile(
    library: CuratedLibrary,
    artwork: CuratedArtwork,
    selected: Boolean,
    onSelect: () -> Unit,
    onApply: () -> Unit
) {
    val colors = LocalFaceItColors.current
    var bitmap by remember(artwork.id) { mutableStateOf<Bitmap?>(null) }
    var failed by remember(artwork.id) { mutableStateOf(false) }

    LaunchedEffect(artwork.id) {
        val decoded = withContext(Dispatchers.IO) { library.thumbnail(artwork, 320) }
        if (decoded == null) failed = true else bitmap = decoded
    }

    FaceItGlass(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "${artwork.title}, ${artwork.category} wallpaper"
            },
        tone = if (selected) GlassTone.Accent else GlassTone.Standard,
        contentPadding = 8.dp
    ) {
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
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Selected wallpaper",
                    tint = androidx.compose.ui.graphics.Color(0xFF071411),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(22.dp)
                        .background(colors.mint, CircleShape)
                        .padding(3.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = artwork.title,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = artwork.category,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onSelect, contentPadding = PaddingValues(horizontal = 4.dp)) {
                Text(if (selected) "Selected" else "Use", fontSize = 12.sp)
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onApply, contentPadding = PaddingValues(horizontal = 4.dp)) {
                Icon(Icons.Rounded.Wallpaper, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Apply", fontSize = 12.sp)
            }
        }
    }
}
