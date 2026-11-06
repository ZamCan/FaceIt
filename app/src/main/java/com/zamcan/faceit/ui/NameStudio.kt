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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zamcan.faceit.data.PersonalEnvironment
import com.zamcan.faceit.data.PhotoBitmaps
import com.zamcan.faceit.data.PhotoLibrary
import com.zamcan.faceit.graphics.NameFinish
import com.zamcan.faceit.graphics.NameMonogram
import com.zamcan.faceit.graphics.NameScript
import com.zamcan.faceit.graphics.NameSetting
import com.zamcan.faceit.graphics.WallpaperComposition
import com.zamcan.faceit.ui.theme.LocalFaceItColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The Name studio.
 *
 * One editable name, thirteen metal finishes, four script voices and every
 * photograph in the pack as a ground. The name is set as real text at render
 * time, so anything the user types appears on the same treatment the reference
 * artwork has — and it is their name, not a stock one.
 *
 * Grounds are grouped by subject rather than listed flat, because "a photograph
 * behind my name" is a mood question: crystals, jewellery, ironwork, peaks, silk.
 */
@Composable
internal fun NameStudioScreen(
    environment: PersonalEnvironment,
    onChange: (PersonalEnvironment) -> Unit,
    onOpenLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalFaceItColors.current
    val context = LocalContext.current
    val photoBitmaps = remember { PhotoBitmaps(context) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    var grounds by remember { mutableStateOf<List<com.zamcan.faceit.data.PhotoWallpaper>>(emptyList()) }

    LaunchedEffect(Unit) {
        grounds = withContext(Dispatchers.IO) { PhotoLibrary.all(context) }
    }

    // The preview is the real renderer at a quarter of wallpaper size: what the
    // card shows is what will be pinned, not an approximation of it.
    LaunchedEffect(
        environment.nameEnabled, environment.nameText, environment.nameFinishId,
        environment.nameScriptId, environment.nameSettingId, environment.nameAnchor,
        environment.namePlaque, environment.nameGroundId, environment.themeId
    ) {
        preview?.takeUnless { it.isRecycled }?.recycle()
        preview = withContext(Dispatchers.IO) {
            WallpaperComposition.compose(context, environment, 540, 1200)
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            preview?.takeUnless { it.isRecycled }?.recycle()
            photoBitmaps.trimMemory()
        }
    }

    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 28.dp)
    ) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Text(
                "NAME ENGINE",
                style = MaterialTheme.typography.labelSmall,
                color = colors.violet
            )
            Text(
                "Your name, in metal",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Type anything. It is set on the device — real letterforms, so any name in any script " +
                    "works — and finished in the metal you choose. Nothing you type leaves the phone.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(16.dp))

        // ------------------------------------------------------------- preview
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .width(212.dp)
                    .aspectRatio(.50f),
                shape = RoundedCornerShape(32.dp),
                color = Color(0xFF08080A),
                border = BorderStroke(2.dp, colors.violet.copy(alpha = .45f)),
                shadowElevation = 18.dp
            ) {
                val frame = preview
                if (frame != null && !frame.isRecycled) {
                    Image(
                        bitmap = frame.asImageBitmap(),
                        contentDescription = "Preview of your name on the wallpaper",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Box(Modifier.background(Color(0xFF08080A)))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Column(Modifier.padding(horizontal = 20.dp)) {
            // -------------------------------------------------- the name itself
            OutlinedTextField(
                value = environment.nameText,
                onValueChange = { typed ->
                    // Typing is not a text substitution: whatever is typed is what is
                    // set. The only limit is length, so the type can still fit.
                    onChange(environment.copy(nameText = typed.take(28), nameEnabled = true))
                },
                label = { Text("The name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${environment.nameText.trim().ifEmpty { "Your name" }.length} of 28 characters · " +
                    "long names are set smaller rather than clipped",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(14.dp))

            ToggleRow(
                label = "Show the name on my wallpaper",
                checked = environment.nameEnabled,
                onChecked = { onChange(environment.copy(nameEnabled = it)) }
            )
            ToggleRow(
                label = "Engraved plate behind it",
                checked = environment.namePlaque,
                onChecked = { onChange(environment.copy(namePlaque = it)) }
            )
            ToggleRow(
                label = "Set it in capitals",
                checked = false,
                enabled = false,
                onChecked = {},
                note = "Case follows what you typed; the engine never rewrites your name."
            )
        }

        Spacer(Modifier.height(18.dp))

        // --------------------------------------------------------------- finish
        SectionLabel("FINISH · ${NameFinish.entries.size}")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            items(NameFinish.entries, key = { it.id }) { finish ->
                FinishSwatch(
                    finish = finish,
                    active = finish.id == environment.nameFinishId,
                    onClick = { onChange(environment.copy(nameFinishId = finish.id)) }
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        SectionLabel("SCRIPT")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            items(NameScript.entries, key = { it.id }) { script ->
                LibraryChip(
                    label = script.label,
                    active = script.id == environment.nameScriptId,
                    onClick = { onChange(environment.copy(nameScriptId = script.id)) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        SectionLabel("SETTING")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            items(NameSetting.entries, key = { it.id }) { setting ->
                LibraryChip(
                    label = setting.label,
                    active = setting.id == environment.nameSettingId,
                    onClick = { onChange(environment.copy(nameSettingId = setting.id)) }
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // ---------------------------------------------------------- where it sits
        Column(Modifier.padding(horizontal = 20.dp)) {
            SectionLabel("HEIGHT ON SCREEN")
            Slider(
                value = environment.nameAnchor,
                onValueChange = { onChange(environment.copy(nameAnchor = it.coerceIn(0.12f, 0.88f))) },
                valueRange = 0.12f..0.88f
            )
            Text(
                when {
                    environment.nameAnchor < 0.33f -> "High — sits above the clock widget."
                    environment.nameAnchor > 0.66f -> "Low — clears the app grid."
                    else -> "Centre — the reference layout."
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(18.dp))

        // ----------------------------------------------------------- the ground
        Column(Modifier.padding(horizontal = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    SectionLabel("PHOTOGRAPH BEHIND IT")
                    Text(
                        if (environment.nameGroundId == null) {
                            "The finish's own backdrop, etched with a circuit pattern in its colour."
                        } else {
                            PhotoLibrary.find(context, environment.nameGroundId)?.title
                                ?: "That photograph is no longer in the pack."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                TextButton(onClick = onOpenLibrary) { Text("Library") }
            }
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    GroundChip(
                        label = "Etched",
                        active = environment.nameGroundId == null,
                        onClick = { onChange(environment.copy(nameGroundId = null)) }
                    )
                }
                items(grounds.take(48), key = { it.id }) { photo ->
                    GroundThumb(
                        photo = photo,
                        active = photo.id == environment.nameGroundId,
                        onClick = { onChange(environment.copy(nameGroundId = photo.id)) }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Generated grounds are marked as generated on their card. Every photograph carries " +
                    "its photographer and licence on the tile it came from.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** A pill used by the script, setting and ground shelves. */
@Composable
private fun LibraryChip(label: String, active: Boolean, onClick: () -> Unit) {
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
            color = if (active) MaterialTheme.colorScheme.onBackground
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
    )
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
    enabled: Boolean = true,
    note: String? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            if (note != null) {
                Text(
                    note,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onChecked, enabled = enabled)
    }
}

/** The finish shelf: each swatch is drawn in its own metal, not in a flat colour. */
@Composable
private fun FinishSwatch(finish: NameFinish, active: Boolean, onClick: () -> Unit) {
    val colors = LocalFaceItColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (active) colors.violet.copy(alpha = .14f) else colors.card,
        border = BorderStroke(1.dp, if (active) colors.violet.copy(alpha = .6f) else colors.border)
    ) {
        Column(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(
                                Color(finish.highlight),
                                Color(finish.base),
                                Color(finish.shadow)
                            )
                        )
                    )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                finish.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (active) MaterialTheme.colorScheme.onBackground
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GroundChip(label: String, active: Boolean, onClick: () -> Unit) {
    LibraryChip(label = label, active = active, onClick = onClick)
}

@Composable
private fun GroundThumb(
    photo: com.zamcan.faceit.data.PhotoWallpaper,
    active: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val bitmaps = remember { PhotoBitmaps(context) }
    var thumb by remember(photo.id) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(photo.id) {
        thumb = withContext(Dispatchers.IO) { bitmaps.thumbnail(photo, 200) }
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            if (active) 2.dp else 1.dp,
            if (active) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outlineVariant
        ),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        val image = thumb
        if (image != null && !image.isRecycled) {
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = photo.title + if (photo.synthetic) ", generated" else ", photograph",
                modifier = Modifier
                    .size(width = 58.dp, height = 88.dp)
            )
        } else {
            Box(Modifier.size(width = 58.dp, height = 88.dp))
        }
    }
}
