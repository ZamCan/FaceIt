package com.zamcan.faceit.ui

import android.provider.OpenableColumns
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import com.zamcan.faceit.messaging.SmsSegmentEstimator
import com.zamcan.faceit.messaging.SmsTransportHandoff
import com.zamcan.faceit.text.WritingStyle
import com.zamcan.faceit.text.WritingStyles
import com.zamcan.faceit.text.WritingSurface
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
        ToolsPage.KeyboardDesigns -> KeyboardDesignGalleryScreen(modifier, onBack)
        // The style studio moved to its own primary destination: it became the
        // largest surface in the product and did not belong behind a tools card.
        ToolsPage.Personalisation -> ToolsHomeScreen(modifier, onOpen)
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
            icon = Icons.Rounded.Widgets,
            title = "Clock widgets",
            description = "Nine clock finishes for the real home-screen widget. Adding one opens the widget picker's own configuration.",
            meta = "Real AppWidget",
            onClick = { onOpen(ToolsPage.Personalisation) }
        )
        ToolCard(
            icon = Icons.Rounded.Keyboard,
            title = "Keyboard designs",
            description = "Browse selectable key styles and try an isolated pressable demo.",
            meta = "Visual presets",
            onClick = { onOpen(ToolsPage.KeyboardDesigns) }
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
            text = "FaceIt requests a permission only when you use the feature that needs it. Vault files and imported artwork stay on this device, and keyboard designs are previews inside Face It only.",
            tint = colors.ice,
            icon = Icons.Rounded.Shield
        )
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
    bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024f * 1024f))
    bytes >= 1024 -> String.format(Locale.US, "%.0f KB", bytes / 1024f)
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
    // Writing styles are a real catalogue now: family, tracking, leading, slant and
    // casing, all applied to the text itself. No look-alike Unicode substitution.
    var writingId by remember { mutableStateOf(WritingStyles.all.first().id) }
    val writing = WritingStyles.find(writingId)
    val writingOptions = remember { WritingStyles.forSurface(WritingSurface.MESSAGE) }
    var colourChoice by remember { mutableStateOf("Pearl") }
    val messageColour = when (colourChoice) {
        "Mint" -> colors.mint
        "Violet" -> colors.violet
        "Warm" -> colors.peach
        else -> MaterialTheme.colorScheme.onSurface
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

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "WRITING STYLE",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(writingOptions, key = { it.id }) { style ->
                        val active = style.id == writingId
                        Surface(
                            onClick = { writingId = style.id },
                            shape = MaterialTheme.shapes.medium,
                            color = if (active) colors.violet.copy(alpha = .14f) else colors.card,
                            border = BorderStroke(
                                if (active) 1.5.dp else 1.dp,
                                if (active) colors.violet.copy(alpha = .6f) else colors.border
                            )
                        ) {
                            Column(Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                                Text(style.name, style = MaterialTheme.typography.labelMedium)
                                Text(
                                    style.render("Aa the cat"),
                                    fontFamily = style.family,
                                    fontStyle = if (style.italic) FontStyle.Italic else FontStyle.Normal,
                                    letterSpacing = style.letterSpacingEm.sp,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
                Text(
                    writing.sample,
                    fontFamily = writing.family,
                    fontStyle = if (writing.italic) FontStyle.Italic else FontStyle.Normal,
                    letterSpacing = writing.letterSpacingEm.sp,
                    lineHeight = (16 * writing.lineHeightMultiple).sp,
                    fontSize = (15 * writing.scale).sp,
                    color = colors.mint
                )
            }
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
                    fontFamily = writing.family,
                    fontStyle = if (writing.italic) FontStyle.Italic else FontStyle.Normal,
                    letterSpacing = writing.letterSpacingEm.sp,
                    fontSize = (16 * writing.scale).sp,
                    lineHeight = (24 * writing.lineHeightMultiple).sp
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
