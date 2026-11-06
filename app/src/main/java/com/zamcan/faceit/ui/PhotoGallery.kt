package com.zamcan.faceit.ui

import android.graphics.Bitmap
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
    onApply: (PhotoWallpaper) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val bitmaps = remember { PhotoBitmaps(context) }
    var category by remember { mutableStateOf<String?>(null) }
    val photos = remember(category) { PhotoLibrary.byCategory(context, category) }
    val total = remember { PhotoLibrary.all(context).size }

    DisposableEffect(bitmaps) {
        onDispose { bitmaps.trimMemory() }
    }

    Column(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScreenHeader(
                eyebrow = "Photographs",
                title = "Real photography",
                description = buildString {
                    append("$total bundled photographs, credited to their makers. ")
                    append("Licensed for reuse — see each credit line.")
                },
                onBack = onBack
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { PhotoCategoryChip("All", category == null) { category = null } }
            items(PhotoLibrary.categories(context), key = { it }) { name ->
                PhotoCategoryChip(name.replaceFirstChar { it.uppercase() }, category == name) {
                    category = if (category == name) null else name
                }
            }
        }

        Spacer(Modifier.height(14.dp))

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
                    onApply = { onApply(photo) }
                )
            }
        }
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

@Composable
private fun PhotoTile(
    bitmaps: PhotoBitmaps,
    photo: PhotoWallpaper,
    selected: Boolean,
    onSelect: () -> Unit,
    onApply: () -> Unit
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
                contentDescription = "${photo.title} by ${photo.creator}, ${photo.licence} photograph"
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

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onSelect, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text("Preview", style = MaterialTheme.typography.labelMedium)
                }
                TextButton(onClick = onApply, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text("Set", style = MaterialTheme.typography.labelMedium, color = colors.mint)
                }
            }
        }
    }
}
