package com.zamcan.faceit.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class FaceItClockWidgetConfigActivity : ComponentActivity() {
    private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        val preferences = getSharedPreferences("faceit_clock_widget_$widgetId", MODE_PRIVATE)
        val initialStyle = FaceItClockWidgetStyles.find(preferences.getString("style", "Obsidian")).id
        setContent {
            MaterialTheme {
                WidgetStyleConfig(
                    initialStyle = initialStyle,
                    onCancel = { finish() },
                    onSave = { style ->
                        preferences.edit().putString("style", style.id).apply()
                        FaceItClockWidgetProvider.updateWidget(
                            this,
                            AppWidgetManager.getInstance(this),
                            widgetId
                        )
                        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                        setResult(Activity.RESULT_OK, result)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun WidgetStyleConfig(
    initialStyle: String,
    onCancel: () -> Unit,
    onSave: (FaceItClockStyle) -> Unit
) {
    var selected by remember { mutableStateOf(FaceItClockWidgetStyles.find(initialStyle)) }
    Column(
        Modifier.fillMaxSize().padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("FaceIt clock", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Text("Choose a material finish for this home-screen widget.")
        FaceItClockWidgetStyles.all.forEach { style ->
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { selected = style },
                color = Color(style.background),
                shape = RoundedCornerShape(20.dp),
                border = if (selected.id == style.id) androidx.compose.foundation.BorderStroke(2.dp, Color(style.date)) else null
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("09:41", color = Color(style.time), style = MaterialTheme.typography.headlineLarge)
                    Text(style.label, color = Color(style.date), fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
            Button(onClick = { onSave(selected) }, modifier = Modifier.weight(1f)) { Text("Add widget") }
        }
    }
}
