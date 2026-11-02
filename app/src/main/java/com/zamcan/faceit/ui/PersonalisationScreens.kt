package com.zamcan.faceit.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.provider.OpenableColumns
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zamcan.faceit.keyboard.KeyMaterial
import com.zamcan.faceit.keyboard.KeyShape
import com.zamcan.faceit.keyboard.KeyboardFont
import com.zamcan.faceit.keyboard.KeyboardLayout
import com.zamcan.faceit.keyboard.KeyboardPersonalizationEngine
import com.zamcan.faceit.messaging.SmsSegmentEstimator
import com.zamcan.faceit.messaging.SmsTransportHandoff
import com.zamcan.faceit.ui.theme.FaceItSpacing
import com.zamcan.faceit.ui.theme.LocalFaceItColors
import com.zamcan.faceit.vault.LocalVaultStore
import java.io.ByteArrayOutputStream
import java.io.InputStream

@Composable
internal fun ToolsHubScreen(
    page: ToolsPage,
    modifier: Modifier = Modifier,
    onOpen: (ToolsPage) -> Unit,
    onBack: () -> Unit
) {
    when (page) {
        ToolsPage.Home -> ToolsHomeScreen(modifier, onOpen)
        ToolsPage.Keyboard -> KeyboardStudioScreen(modifier, onBack)
        ToolsPage.Vault -> VaultScreen(modifier, onBack)
        ToolsPage.Sms -> SmsComposerScreen(modifier, onBack)
    }
}

@Composable
private fun ToolsHomeScreen(
    modifier: Modifier,
    onOpen: (ToolsPage) -> Unit
) {
    val colors = LocalFaceItColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ScreenHeader(
            eyebrow = "Tools",
            title = "Personalisation tools",
            description = "Native tools connected to Android's supported workflows."
        )

        ToolCard(
            icon = Icons.Rounded.Keyboard,
            title = "Keyboard studio",
            description = "Shape key geometry, material, typography and layout for FaceIt's own keyboard.",
            meta = "On-device IME",
            onClick = { onOpen(ToolsPage.Keyboard) }
        )
        ToolCard(
            icon = Icons.Rounded.Lock,
            title = "Private Vault",
            description = "Encrypt files with an Android Keystore key and open them only on this device.",
            meta = "AES-256-GCM",
            onClick = { onOpen(ToolsPage.Vault) }
        )
        ToolCard(
            icon = Icons.Rounded.Send,
            title = "SMS composer",
            description = "Write a message, estimate GSM-7 or UCS-2 segments, then hand it to your SMS app.",
            meta = "Plain-text handoff",
            onClick = { onOpen(ToolsPage.Sms) }
        )

        SectionHeader(
            title = "Android surfaces",
            supporting = "Where FaceIt plugs into the operating system."
        )

        FaceItCard {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(colors.cardRaised, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Widgets,
                        contentDescription = null,
                        tint = colors.violet,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Clock widget",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "Add FaceIt Clock from your launcher's widget picker. Each widget keeps its own material finish.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    StatusPill(text = "System-hosted", tint = colors.mint)
                }
            }
        }

        InfoNote(
            text = "FaceIt requests a permission only when you use the feature that needs it. Vault files, imported artwork and keyboard input stay on this device.",
            tint = colors.ice,
            icon = Icons.Rounded.Shield
        )
    }
}

private fun prettyEnum(value: String): String =
    value.lowercase().split('_').joinToString(" ") { part ->
        part.replaceFirstChar { it.uppercase() }
    }

private data class PaletteOption(val id: String, val label: String)

private val paletteOptions = listOf(
    PaletteOption("OBSIDIAN_CERAMIC", "Obsidian ceramic"),
    PaletteOption("WARM_STONE", "Warm stone"),
    PaletteOption("PEARL_MINT", "Pearl mint"),
    PaletteOption("GRAPHITE_ICE", "Graphite ice")
)

private data class PalettePreview(
    val background: Color,
    val key: Color,
    val text: Color
)

private fun palettePreview(id: String): PalettePreview = when (id) {
    "WARM_STONE" -> PalettePreview(Color(0xFF28231F), Color(0xFF51463C), Color(0xFFF5EBDD))
    "PEARL_MINT" -> PalettePreview(Color(0xFFE9ECE9), Color(0xFFFFFFFF), Color(0xFF252B29))
    "GRAPHITE_ICE" -> PalettePreview(Color(0xFF171B20), Color(0xFF303A43), Color(0xFFF0F4F7))
    else -> PalettePreview(Color(0xFF17191F), Color(0xFF30333C), Color(0xFFF3F1F7))
}

private fun fontFamilyFor(font: String): FontFamily = when (font) {
    "SERIF" -> FontFamily.Serif
    "MONOSPACE" -> FontFamily.Monospace
    else -> FontFamily.SansSerif
}

@Composable
private fun KeyboardStudioScreen(modifier: Modifier, onBack: () -> Unit) {
    val context = LocalContext.current
    val colors = LocalFaceItColors.current
    val prefs = remember { context.getSharedPreferences("faceit_keyboard", Context.MODE_PRIVATE) }
    var shape by remember { mutableStateOf(prefs.getString("shape", KeyShape.ROUNDED_RECTANGLE.name)!!) }
    var material by remember { mutableStateOf(prefs.getString("material", KeyMaterial.MATTE_CERAMIC.name)!!) }
    var font by remember { mutableStateOf(prefs.getString("font", KeyboardFont.SYSTEM.name)!!) }
    var layout by remember { mutableStateOf(prefs.getString("layout", KeyboardLayout.QWERTY.name)!!) }
    var palette by remember { mutableStateOf(prefs.getString("palette", "OBSIDIAN_CERAMIC")!!) }
    var haptics by remember { mutableStateOf(prefs.getBoolean("haptics", true)) }
    var saved by remember { mutableStateOf(true) }

    fun markChanged() {
        saved = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ScreenHeader(
            eyebrow = "Keyboard studio",
            title = "Key appearance",
            description = "Configure FaceIt's own keyboard. Android requires you to enable and select it explicitly.",
            onBack = onBack
        )

        FaceItCard(contentPadding = 14.dp) {
            KeyboardPreview(
                shape = shape,
                material = material,
                font = font,
                layout = layout,
                palette = palette
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Appearance preview",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                if (saved) {
                    StatusPill(text = "Saved", tint = colors.mint)
                } else {
                    StatusPill(text = "Unsaved", tint = colors.warning)
                }
            }
        }

        FaceItCard {
            ChoiceChips(
                title = "Key shape",
                options = KeyShape.entries.map { prettyEnum(it.name) },
                selected = prettyEnum(shape),
                onSelected = { label ->
                    KeyShape.entries.firstOrNull { prettyEnum(it.name) == label }?.let {
                        shape = it.name
                        markChanged()
                    }
                }
            )
            Spacer(Modifier.height(18.dp))
            ChoiceChips(
                title = "Material finish",
                options = KeyMaterial.entries.map { prettyEnum(it.name) },
                selected = prettyEnum(material),
                onSelected = { label ->
                    KeyMaterial.entries.firstOrNull { prettyEnum(it.name) == label }?.let {
                        material = it.name
                        markChanged()
                    }
                }
            )
            Spacer(Modifier.height(18.dp))
            ChoiceChips(
                title = "Typography",
                options = KeyboardFont.entries.map { prettyEnum(it.name) },
                selected = prettyEnum(font),
                onSelected = { label ->
                    KeyboardFont.entries.firstOrNull { prettyEnum(it.name) == label }?.let {
                        font = it.name
                        markChanged()
                    }
                }
            )
            Spacer(Modifier.height(18.dp))
            ChoiceChips(
                title = "Layout",
                options = KeyboardLayout.entries.map { it.name },
                selected = layout,
                onSelected = {
                    layout = it
                    markChanged()
                }
            )
            Spacer(Modifier.height(18.dp))
            ChoiceChips(
                title = "Colour palette",
                options = paletteOptions.map { it.label },
                selected = paletteOptions.firstOrNull { it.id == palette }?.label
                    ?: paletteOptions.first().label,
                onSelected = { label ->
                    paletteOptions.firstOrNull { it.label == label }?.let {
                        palette = it.id
                        markChanged()
                    }
                }
            )
        }

        FaceItCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Haptic feedback", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Vibrate briefly on key press when supported.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = haptics,
                    onCheckedChange = {
                        haptics = it
                        markChanged()
                    }
                )
            }
        }

        Button(
            onClick = {
                prefs.edit()
                    .putString("shape", shape)
                    .putString("material", material)
                    .putString("font", font)
                    .putString("layout", layout)
                    .putString("palette", palette)
                    .putBoolean("haptics", haptics)
                    .apply()
                saved = true
                Toast.makeText(context, "Keyboard appearance saved", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Rounded.SaveAlt, contentDescription = null, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(8.dp))
            Text("Save keyboard style")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Icon(Icons.Rounded.Keyboard, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Enable", maxLines = 1)
            }
            OutlinedButton(
                onClick = {
                    val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    manager.showInputMethodPicker()
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Text("Select keyboard", maxLines = 1)
            }
        }

        InfoNote(
            text = "This keyboard does not need internet access and does not save typed text. Review its behaviour before using it for passwords or sensitive data.",
            tint = colors.mint,
            icon = Icons.Rounded.Shield
        )
    }
}

@Composable
private fun KeyboardPreview(
    shape: String,
    material: String,
    font: String,
    layout: String,
    palette: String
) {
    val colors = LocalFaceItColors.current
    val preview = palettePreview(palette)
    val radius = when (shape) {
        "PILL" -> 28.dp
        "SOFT_SQUARE" -> 7.dp
        "CIRCULAR" -> 30.dp
        else -> 14.dp
    }
    val keyShape = RoundedCornerShape(radius)
    val keyStroke = when (material) {
        "FROSTED_GLASS" -> Color.White.copy(alpha = .45f)
        "NATURAL_STONE" -> Color(0xFF917D6C)
        "BRUSHED_METAL" -> Color(0xFF8995A4)
        "SOFT_MATTE" -> Color.White.copy(alpha = .08f)
        else -> Color.White.copy(alpha = .14f)
    }
    val rows = KeyboardPersonalizationEngine.mapCharacters(
        when (layout) {
            "QWERTZ" -> KeyboardLayout.QWERTZ
            "AZERTY" -> KeyboardLayout.AZERTY
            else -> KeyboardLayout.QWERTY
        }
    )
    Surface(
        shape = MaterialTheme.shapes.large,
        color = preview.background,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    row.forEach { key ->
                        PreviewKey(
                            label = key.toString(),
                            background = preview.key,
                            textColor = preview.text,
                            shape = keyShape,
                            border = keyStroke,
                            fontFamily = fontFamilyFor(font),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                PreviewKey(
                    label = "?123",
                    background = preview.key,
                    textColor = preview.text,
                    shape = keyShape,
                    border = keyStroke,
                    fontFamily = fontFamilyFor(font),
                    modifier = Modifier.weight(1f)
                )
                PreviewKey(
                    label = "space",
                    background = preview.key,
                    textColor = preview.text,
                    shape = keyShape,
                    border = keyStroke,
                    fontFamily = fontFamilyFor(font),
                    modifier = Modifier.weight(3f)
                )
                PreviewKey(
                    label = "↵",
                    background = preview.key,
                    textColor = preview.text,
                    shape = keyShape,
                    border = keyStroke,
                    fontFamily = fontFamilyFor(font),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PreviewKey(
    label: String,
    background: Color,
    textColor: Color,
    shape: RoundedCornerShape,
    border: Color,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .background(background, shape)
            .then(Modifier),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = shape,
            color = Color.Transparent,
            border = BorderStroke(0.75.dp, border)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = label,
                    color = textColor,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }
        }
    }
}

private fun readLimited(input: InputStream, maximum: Int): ByteArray {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(8192)
    var total = 0
    while (true) {
        val count = input.read(buffer)
        if (count < 0) break
        total += count
        require(total <= maximum) { "File exceeds the 25 MiB limit" }
        output.write(buffer, 0, count)
    }
    return output.toByteArray()
}

private fun formatBytes(bytes: Int): String = when {
    bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / (1024f * 1024f))
    bytes >= 1024 -> String.format("%.0f KB", bytes / 1024f)
    else -> "$bytes B"
}

@Composable
private fun VaultScreen(modifier: Modifier, onBack: () -> Unit) {
    val context = LocalContext.current
    val colors = LocalFaceItColors.current
    val vault = remember { LocalVaultStore(context) }
    var ids by remember { mutableStateOf(vault.listIds()) }
    var selected by remember { mutableStateOf<LocalVaultStore.VaultItem?>(null) }
    var pendingDelete by remember { mutableStateOf<String?>(null) }
    var pendingExport by remember { mutableStateOf<ByteArray?>(null) }

    val importPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            val size = context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
            require(size == -1L || size in 1..(25L * 1024 * 1024)) {
                "Choose a file no larger than 25 MiB"
            }
            val bytes = context.contentResolver.openInputStream(uri)?.use {
                readLimited(it, 25 * 1024 * 1024)
            } ?: error("Unable to read selected file")
            require(bytes.isNotEmpty()) { "The selected file is empty" }
            val name = context.contentResolver
                .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
                ?.take(120)
                ?.ifBlank { "Private file" }
                ?: "Private file"
            vault.store(name, bytes)
            ids = vault.listIds()
            Toast.makeText(context, "Encrypted file added to Vault", Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(
                context,
                it.localizedMessage ?: "Vault import failed",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val exportPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        val bytes = pendingExport
        if (uri != null && bytes != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                    ?: error("Unable to write export")
                Toast.makeText(context, "Vault item exported", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(
                    context,
                    it.localizedMessage ?: "Export failed",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        pendingExport = null
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 26.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenHeader(
                eyebrow = "Private Vault",
                title = "Encrypted storage",
                description = "Files are encrypted on this device with Android Keystore. They are not uploaded or included in Android cloud backup.",
                onBack = onBack,
                trailing = { StatusPill(text = "${ids.size} items", tint = colors.mint) }
            )
        }

        item {
            Button(
                onClick = { importPicker.launch(arrayOf("*/*")) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Rounded.FileOpen, contentDescription = null, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(8.dp))
                Text("Import file into Vault")
            }
        }

        if (ids.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Rounded.Lock,
                    title = "The Vault is empty",
                    body = "Import a document or image. FaceIt encrypts it before writing to private no-backup storage."
                )
            }
        } else {
            items(ids, key = { it }) { id ->
                FaceItCard(contentPadding = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(colors.cardRaised, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = colors.violet,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Encrypted item",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "${id.take(8)}…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                runCatching { vault.open(id) }.onSuccess { selected = it }.onFailure {
                                    Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
                                }
                            },
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(1.dp, colors.border)
                        ) { Text("Open") }
                        Spacer(Modifier.width(2.dp))
                        IconButton(onClick = { pendingDelete = id }) {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = "Delete encrypted item",
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            InfoNote(
                text = "Keep a separate safe copy of anything important. If the Android Keystore key is lost, encrypted items may become unrecoverable. FaceIt will not silently replace a missing key.",
                tint = colors.warning,
                icon = Icons.Rounded.Shield
            )
        }
    }

    selected?.let { item ->
        AlertDialog(
            onDismissRequest = { selected = null },
            containerColor = colors.card,
            shape = MaterialTheme.shapes.extraLarge,
            title = {
                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${formatBytes(item.bytes.size)} · decrypted only in this session")
                    Text(
                        "Exporting writes a clear copy to the destination you choose. Protect that copy yourself.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingExport = item.bytes
                        val safeName = item.displayName
                            .substringAfterLast('/')
                            .substringAfterLast('\\')
                            .take(120)
                            .ifBlank { "vault-export.bin" }
                        selected = null
                        exportPicker.launch(safeName)
                    },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Rounded.SaveAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export copy")
                }
            },
            dismissButton = {
                TextButton(onClick = { selected = null }) { Text("Close") }
            }
        )
    }

    pendingDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = colors.card,
            shape = MaterialTheme.shapes.extraLarge,
            title = { Text("Delete encrypted item?") },
            text = { Text("This removes the encrypted file from this device. It cannot be undone unless you exported a copy.") },
            confirmButton = {
                TextButton(onClick = {
                    vault.delete(id)
                    ids = vault.listIds()
                    if (selected?.id == id) selected = null
                    pendingDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SmsComposerScreen(modifier: Modifier, onBack: () -> Unit) {
    val context = LocalContext.current
    val colors = LocalFaceItColors.current
    var recipient by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var fontChoice by remember { mutableStateOf("Sans") }
    var colourChoice by remember { mutableStateOf("Pearl") }
    val messageColour = when (colourChoice) {
        "Mint" -> colors.mint
        "Violet" -> colors.violet
        "Warm" -> colors.peach
        else -> MaterialTheme.colorScheme.onSurface
    }
    val messageFont = when (fontChoice) {
        "Serif" -> FontFamily.Serif
        "Mono" -> FontFamily.Monospace
        else -> FontFamily.SansSerif
    }
    val estimate = SmsSegmentEstimator.estimate(body)
    val transportLabel = when (estimate.encoding) {
        SmsSegmentEstimator.Encoding.GSM_7 -> "GSM-7"
        SmsSegmentEstimator.Encoding.UCS_2 -> "UCS-2 · Unicode"
    }
    val capacity = if (estimate.segments <= 1) {
        estimate.singleSegmentCapacity
    } else {
        estimate.concatenatedSegmentCapacity
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ScreenHeader(
            eyebrow = "SMS composer",
            title = "Write and preview",
            description = "Prepare the message here, then hand the original plain text to your chosen SMS app.",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = recipient,
                onValueChange = { recipient = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Recipient number") },
                placeholder = { Text("+255 …") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.violet,
                    unfocusedBorderColor = colors.border
                )
            )

            ChoiceChips(
                title = "Preview font",
                options = listOf("Sans", "Serif", "Mono"),
                selected = fontChoice,
                onSelected = { fontChoice = it }
            )
            ChoiceChips(
                title = "Preview colour",
                options = listOf("Pearl", "Mint", "Violet", "Warm"),
                selected = colourChoice,
                onSelected = { colourChoice = it }
            )

            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                modifier = Modifier.fillMaxWidth().height(190.dp),
                label = { Text("Message") },
                placeholder = {
                    Text(
                        "Type your message",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                textStyle = TextStyle(
                    color = messageColour,
                    fontFamily = messageFont,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                ),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.card,
                    unfocusedContainerColor = colors.card,
                    focusedBorderColor = colors.violet,
                    unfocusedBorderColor = colors.border
                )
            )

            FaceItCard(contentPadding = 14.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SegmentMetric(
                        label = "ENCODING",
                        value = transportLabel,
                        modifier = Modifier.weight(1f)
                    )
                    SegmentMetric(
                        label = "SEGMENTS",
                        value = estimate.segments.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    SegmentMetric(
                        label = "CHARACTERS",
                        value = body.length.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Estimated capacity: $capacity units per ${if (estimate.segments <= 1) "single" else "joined"} segment. Carrier handling may vary.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            InfoNote(
                text = "Font and colour are editor previews only. Carrier SMS sends plain text, so your styling is not transmitted.",
                tint = colors.ice
            )
        }

        Button(
            onClick = {
                runCatching {
                    SmsTransportHandoff.openComposer(context, recipient.trim(), body)
                }.onFailure {
                    Toast.makeText(
                        context,
                        it.localizedMessage ?: "Could not open SMS app",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            enabled = recipient.isNotBlank() && body.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Rounded.Send, contentDescription = null, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(8.dp))
            Text("Continue in SMS app")
        }
    }
}

@Composable
private fun SegmentMetric(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = LocalFaceItColors.current
    Column(
        modifier = modifier
            .background(colors.cardRaised, MaterialTheme.shapes.small)
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
