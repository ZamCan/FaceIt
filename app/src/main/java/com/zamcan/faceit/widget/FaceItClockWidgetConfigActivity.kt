package com.zamcan.faceit.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zamcan.faceit.ui.ScreenHeader
import com.zamcan.faceit.ui.theme.FaceItTheme
import com.zamcan.faceit.ui.theme.LocalFaceItColors

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
            FaceItTheme {
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
    val colors = LocalFaceItColors.current
    var selected by remember { mutableStateOf(FaceItClockWidgetStyles.find(initialStyle)) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ScreenHeader(
            eyebrow = "Home screen",
            title = "FaceIt clock",
            description = "Choose a material finish for this widget. Each widget keeps its own saved style."
        )

        FaceItClockWidgetStyles.all.forEach { style ->
            val active = selected.id == style.id
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = Color(style.background),
                border = BorderStroke(
                    width = if (active) 2.dp else 1.dp,
                    color = if (active) Color(style.date) else colors.border
                ),
                onClick = { selected = style }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "09:41",
                            color = Color(style.time),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Fri, 1 Nov",
                            color = Color(style.date),
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = style.label,
                            color = Color(style.time).copy(alpha = .72f),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    if (active) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Selected",
                            tint = Color(style.date)
                        )
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, colors.border)
            ) { Text("Cancel") }
            Button(
                onClick = { onSave(selected) },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = MaterialTheme.shapes.medium
            ) { Text("Add widget") }
        }

        Text(
            text = "The widget uses Android's live clock and date rendering, so it stays current without periodic background updates.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
