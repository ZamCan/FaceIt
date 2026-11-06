package com.zamcan.faceit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zamcan.faceit.data.CompositionLibrary
import com.zamcan.faceit.data.PersonalEnvironment
import com.zamcan.faceit.data.SavedComposition

/** Persistent scenes are independent snapshots; deleting one never deletes imported files. */
@Composable
fun CompositionLibraryScreen(modifier: Modifier = Modifier, onRestore: (PersonalEnvironment) -> Unit) {
    val context = LocalContext.current
    val library = remember { CompositionLibrary(context) }
    var error by remember { mutableStateOf<String?>(null) }
    var scenes by remember {
        mutableStateOf(runCatching { library.list() }.getOrElse {
            error = it.message ?: "The saved scene library could not be read."
            emptyList()
        })
    }
    var renaming by remember { mutableStateOf<SavedComposition?>(null) }
    var deleting by remember { mutableStateOf<SavedComposition?>(null) }
    var name by remember { mutableStateOf("") }
    fun mutate(operation: () -> Unit) {
        runCatching { operation(); library.list() }
            .onSuccess { scenes = it; error = null }
            .onFailure { error = it.message ?: "The scene could not be updated." }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { ScreenHeader(title = "Saved scenes", description = "Your compositions, kept offline on this device.") }
        error?.let { message -> item { InfoNote(message, tint = MaterialTheme.colorScheme.error) } }
        if (scenes.isEmpty() && error == null) {
            item { Text("Save a scene from Create to build your personal collection.", style = MaterialTheme.typography.bodyLarge) }
        }
        items(scenes, key = { it.id }) { scene ->
            FaceItCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(scene.environment.name, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${scene.environment.background} · ${scene.environment.mainObject} · ${scene.environment.material}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(onClick = { onRestore(scene.environment) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Restore scene")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = { name = scene.environment.name; renaming = scene }) { Text("Rename") }
                        TextButton(onClick = { mutate { library.duplicate(scene.id) } }) { Text("Duplicate") }
                        TextButton(onClick = { deleting = scene }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }
    renaming?.let { scene ->
        AlertDialog(
            onDismissRequest = { renaming = null },
            title = { Text("Rename scene") },
            text = { OutlinedTextField(value = name, onValueChange = { name = it.take(80) },
                label = { Text("Scene name") }, singleLine = true) },
            confirmButton = {
                TextButton(enabled = name.isNotBlank(), onClick = {
                    mutate { library.rename(scene.id, name) }
                    renaming = null
                }) { Text("Save name") }
            },
            dismissButton = { TextButton(onClick = { renaming = null }) { Text("Cancel") } }
        )
    }
    deleting?.let { scene ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete saved scene?") },
            text = { Text("“${scene.environment.name}” will be removed from this library. Imported artwork and your active composition are kept.") },
            confirmButton = { TextButton(onClick = {
                mutate { library.delete(scene.id) }; deleting = null
            }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Keep") } }
        )
    }
}

