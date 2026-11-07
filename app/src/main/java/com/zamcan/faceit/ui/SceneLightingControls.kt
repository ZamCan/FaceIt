package com.zamcan.faceit.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.zamcan.faceit.data.LightDirection
import com.zamcan.faceit.data.SceneLighting

@Composable
fun SceneLightingControls(
    value: SceneLighting,
    onChange: (SceneLighting) -> Unit,
    onAdjust: (SceneLighting) -> Unit,
    onAdjustmentFinished: () -> Unit
) {
    FaceItCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Scene lighting", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            Switch(checked = value.enabled, onCheckedChange = { onChange(value.copy(enabled = it)) },
                modifier = Modifier.semantics { contentDescription = "Enable scene lighting" })
        }
        Text("Shape the light without changing your artwork, object or material.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (value.enabled) {
            ChoiceChips(title = "Light direction", options = LightDirection.entries.map { it.label },
                selected = value.direction.label,
                onSelected = { label -> onChange(value.copy(direction = LightDirection.entries.first { it.label == label })) })
            Text("Intensity", style = MaterialTheme.typography.labelLarge)
            Slider(value = value.intensity, onValueChange = { onAdjust(value.copy(intensity = it)) },
                onValueChangeFinished = onAdjustmentFinished,
                modifier = Modifier.semantics { contentDescription = "Light intensity" })
            Text("Softness", style = MaterialTheme.typography.labelLarge)
            Slider(value = value.softness, onValueChange = { onAdjust(value.copy(softness = it)) },
                onValueChangeFinished = onAdjustmentFinished,
                modifier = Modifier.semantics { contentDescription = "Light softness" })
            Text("Cool to warm", style = MaterialTheme.typography.labelLarge)
            Slider(value = value.warmth, onValueChange = { onAdjust(value.copy(warmth = it)) },
                valueRange = -1f..1f, onValueChangeFinished = onAdjustmentFinished,
                modifier = Modifier.semantics { contentDescription = "Light warmth" })
            TextButton(onClick = { onChange(SceneLighting()) }) { Text("Reset lighting") }
        }
    }
}
