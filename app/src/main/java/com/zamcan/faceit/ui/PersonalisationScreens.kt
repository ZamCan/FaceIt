package com.zamcan.faceit.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.zamcan.faceit.keyboard.KeyMaterial
import com.zamcan.faceit.keyboard.KeyShape
import com.zamcan.faceit.keyboard.KeyboardFont
import com.zamcan.faceit.keyboard.KeyboardLayout
import com.zamcan.faceit.messaging.SmsTransportHandoff
import com.zamcan.faceit.vault.LocalVaultStore

private val panelColor = androidx.compose.ui.graphics.Color(0xFF20232B)
private val mutedColor = androidx.compose.ui.graphics.Color(0xFFA9AAB4)

@Composable
fun KeyboardStudioScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("faceit_keyboard", Context.MODE_PRIVATE) }
    var shape by remember { mutableStateOf(prefs.getString("shape", KeyShape.ROUNDED_RECTANGLE.name)!!) }
    var material by remember { mutableStateOf(prefs.getString("material", KeyMaterial.MATTE_CERAMIC.name)!!) }
    var font by remember { mutableStateOf(prefs.getString("font", KeyboardFont.SYSTEM.name)!!) }
    var layout by remember { mutableStateOf(prefs.getString("layout", KeyboardLayout.QWERTY.name)!!) }
    var palette by remember { mutableStateOf(prefs.getString("palette", "OBSIDIAN_CERAMIC")!!) }
    var haptics by remember { mutableStateOf(prefs.getBoolean("haptics", true)) }
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        Text("Keyboard studio", style = MaterialTheme.typography.headlineMedium)
        Text("Configure FaceIt's own keyboard. Android requires you to enable and select it explicitly.", color = mutedColor)
        PreferenceChips("Key shape", KeyShape.entries.map { it.name.replace('_', ' ') }, shape.replace('_', ' ')) {
            shape = it.replace(' ', '_')
        }
        PreferenceChips("Material finish", KeyMaterial.entries.map { it.name.replace('_', ' ') }, material.replace('_', ' ')) {
            material = it.replace(' ', '_')
        }
        PreferenceChips("Typography", KeyboardFont.entries.map { it.name }, font) { font = it }
        PreferenceChips("Layout", KeyboardLayout.entries.map { it.name }, layout) { layout = it }
        PreferenceChips("Colour palette", listOf("OBSIDIAN_CERAMIC", "WARM_STONE", "PEARL_MINT", "GRAPHITE_ICE"), palette) { palette = it }
        FilterChip(selected = haptics, onClick = { haptics = !haptics }, label = { Text(if (haptics) "Haptics on" else "Haptics off") })
        Button(onClick = {
            prefs.edit().putString("shape", shape).putString("material", material)
                .putString("font", font).putString("layout", layout).putString("palette", palette)
                .putBoolean("haptics", haptics).apply()
            Toast.makeText(context, "Keyboard appearance saved", Toast.LENGTH_SHORT).show()
        }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Rounded.SaveAlt, null); Text(" Save keyboard style") }
        OutlinedButton(onClick = {
            context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Rounded.Keyboard, null); Text(" Enable FaceIt in Android Settings") }
        OutlinedButton(onClick = {
            val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.showInputMethodPicker()
        }, modifier = Modifier.fillMaxWidth()) { Text("Select FaceIt as active keyboard") }
        Text("Privacy: this keyboard does not need internet access and does not save typed text. Review the keyboard's behaviour before using it for passwords or sensitive data.", color = mutedColor, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PreferenceChips(title: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            options.forEach { option ->
                FilterChip(selected = option.equals(selected, true), onClick = { onSelected(option) }, label = { Text(option) })
            }
        }
    }
}

@Composable
fun VaultScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val vault = remember { LocalVaultStore(context) }
    var ids by remember { mutableStateOf(vault.listIds()) }
    var selected by remember { mutableStateOf<LocalVaultStore.VaultItem?>(null) }
    var pendingExport by remember { mutableStateOf<ByteArray?>(null) }
    val importPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            val size = context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
            require(size in 1..(25L * 1024 * 1024)) { "Choose a file no larger than 25 MiB" }
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("Unable to read selected file")
            require(bytes.size <= 25 * 1024 * 1024) { "File exceeds the 25 MiB limit" }
            val name = uri.lastPathSegment?.substringAfterLast('/')?.take(120)?.ifBlank { "Private file" } ?: "Private file"
            vault.store(name, bytes)
            ids = vault.listIds()
            Toast.makeText(context, "Encrypted file added to Vault", Toast.LENGTH_SHORT).show()
        }.onFailure { Toast.makeText(context, it.localizedMessage ?: "Vault import failed", Toast.LENGTH_LONG).show() }
    }
    val exportPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        val bytes = pendingExport
        if (uri != null && bytes != null) runCatching {
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) } ?: error("Unable to write export")
            Toast.makeText(context, "Vault item exported", Toast.LENGTH_SHORT).show()
        }.onFailure { Toast.makeText(context, it.localizedMessage ?: "Export failed", Toast.LENGTH_LONG).show() }
        pendingExport = null
    }
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Private Vault", style = MaterialTheme.typography.headlineMedium)
        Text("Files are encrypted on this device with Android Keystore. They are not uploaded or included in Android cloud backup.", color = mutedColor)
        Button(onClick = { importPicker.launch(arrayOf("*/*")) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.FileOpen, null); Text(" Import file into Vault")
        }
        Text("Encrypted items · ${ids.size}", style = MaterialTheme.typography.titleMedium)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ids, key = { it }) { id ->
                Card(colors = CardDefaults.cardColors(containerColor = panelColor), shape = RoundedCornerShape(14.dp)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("Encrypted vault item")
                            Text(id.take(8) + "…", color = mutedColor, style = MaterialTheme.typography.bodySmall)
                        }
                        OutlinedButton(onClick = {
                            runCatching { vault.open(id) }.onSuccess { selected = it }
                                .onFailure { Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show() }
                        }) { Text("Open") }
                        androidx.compose.material3.IconButton(onClick = {
                            vault.delete(id); ids = vault.listIds()
                            if (selected?.id == id) selected = null
                        }) { Icon(Icons.Rounded.Delete, contentDescription = "Delete encrypted item") }
                    }
                }
            }
        }
        selected?.let { item ->
            Card(colors = CardDefaults.cardColors(containerColor = panelColor)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.displayName, style = MaterialTheme.typography.titleMedium)
                    Text("${item.bytes.size} bytes · decrypted only in this session", color = mutedColor)
                    Button(onClick = {
                        pendingExport = item.bytes
                        exportPicker.launch(item.displayName.ifBlank { "vault-export.bin" })
                    }) { Icon(Icons.Rounded.SaveAlt, null); Text(" Export decrypted copy") }
                }
            }
        }
        Text("Keep a separate safe copy of anything important. If the Android Keystore key is lost, encrypted items may become unrecoverable.", color = mutedColor, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun SmsComposerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var recipient by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("SMS composer", style = MaterialTheme.typography.headlineMedium)
        Text("Write and preview your message here, then hand it to your chosen SMS app. Carrier SMS sends plain text, not font or colour styling.", color = mutedColor)
        OutlinedTextField(value = recipient, onValueChange = { recipient = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Recipient phone number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)
        OutlinedTextField(value = body, onValueChange = { body = it }, modifier = Modifier.fillMaxWidth().weight(1f), label = { Text("Message") }, minLines = 5)
        Text("${body.length} characters · final SMS body is passed through unchanged", color = mutedColor, style = MaterialTheme.typography.bodySmall)
        Button(onClick = {
            runCatching { SmsTransportHandoff.openComposer(context, recipient.trim(), body) }
                .onFailure { Toast.makeText(context, it.localizedMessage ?: "Could not open SMS app", Toast.LENGTH_LONG).show() }
        }, enabled = recipient.isNotBlank() && body.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.Send, null); Text(" Continue in SMS app")
        }
    }
}


@Composable
fun ToolsHubScreen(
    modifier: Modifier = Modifier,
    page: String,
    onKeyboard: () -> Unit,
    onVault: () -> Unit,
    onSms: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier.fillMaxSize()) {
        when (page) {
            "Keyboard" -> KeyboardStudioScreen(Modifier.weight(1f))
            "Vault" -> VaultScreen(Modifier.weight(1f))
            "SMS" -> SmsComposerScreen(Modifier.weight(1f))
            else -> Column(Modifier.weight(1f).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Personalisation tools", style = MaterialTheme.typography.headlineMedium)
                Text("Native tools connected to Android's supported workflows.", color = mutedColor)
                Button(onClick = onKeyboard, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.Keyboard, null); Text(" Keyboard studio")
                }
                Button(onClick = onVault, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.Lock, null); Text(" Private Vault")
                }
                Button(onClick = onSms, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.Send, null); Text(" SMS composer")
                }
            }
        }
        if (page != "Home") {
            androidx.compose.material3.TextButton(onClick = onBack, modifier = Modifier.padding(start = 12.dp)) {
                Text("Back to tools")
            }
        }
    }
}
