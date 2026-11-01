package com.zamcan.faceit.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.zamcan.faceit.MainActivity
import com.zamcan.faceit.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** A genuine Android-hosted clock widget with per-widget saved appearance. */
class FaceItClockWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateWidget(context, manager, it) }
    }

    companion object {
        fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val preferences = context.getSharedPreferences("faceit_clock_widget_$widgetId", Context.MODE_PRIVATE)
            val style = FaceItClockWidgetStyles.find(preferences.getString("style", "Obsidian"))
            val views = RemoteViews(context.packageName, R.layout.faceit_clock_widget)
            views.setInt(R.id.faceit_widget_root, "setBackgroundColor", style.background)
            views.setTextColor(R.id.faceit_widget_time, style.time)
            views.setTextColor(R.id.faceit_widget_date, style.date)
            views.setTextViewText(
                R.id.faceit_widget_date,
                SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date())
            )
            val intent = Intent(context, MainActivity::class.java)
            val pending = PendingIntent.getActivity(
                context,
                widgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.faceit_widget_root, pending)
            manager.updateAppWidget(widgetId, views)
        }
    }
}
