package com.example.warsawclockwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*

class ClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        val prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
        if (!prefs.contains("clockApp")) {
            prefs.edit().putString("clockApp", "com.android.deskclock").apply()
        }

        context.startService(Intent(context, UpdateWidgetService::class.java))
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.clock_widget)
            val calendar = Calendar.getInstance()

            val is24Hour = DateFormat.is24HourFormat(context)
            val hourFormat = if (is24Hour) "HH" else "hh"
            val hour = SimpleDateFormat(hourFormat, Locale.getDefault()).format(calendar.time)

            views.setTextViewText(R.id.widget_hour, hour)

            if (is24Hour) {
                views.setViewVisibility(R.id.am_pm_text, View.GONE)
            } else {
                val amPm = SimpleDateFormat("a", Locale.getDefault()).format(calendar.time)
                views.setTextViewText(R.id.am_pm_text, amPm)
                views.setViewVisibility(R.id.am_pm_text, View.VISIBLE)
            }

            val intent = Intent(context, ClockWidgetConfigActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
