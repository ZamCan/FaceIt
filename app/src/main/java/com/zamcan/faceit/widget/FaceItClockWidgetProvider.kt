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

/** A genuine, system-hosted widget. Android controls its update lifecycle. */
class FaceItClockWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val views = RemoteViews(context.packageName, R.layout.faceit_clock_widget)
        val now = Date()
        views.setTextViewText(R.id.faceit_widget_time, SimpleDateFormat("HH:mm", Locale.getDefault()).format(now))
        views.setTextViewText(R.id.faceit_widget_date, SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(now))
        val intent = Intent(context, MainActivity::class.java)
        val pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.faceit_widget_root, pending)
        manager.updateAppWidget(ids, views)
    }
}