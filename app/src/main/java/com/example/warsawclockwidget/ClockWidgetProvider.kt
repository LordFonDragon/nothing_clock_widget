package com.example.warsawclockwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*

class ClockWidgetProvider : AppWidgetProvider() {
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update each widget
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // Get the layout for the widget
            val views = RemoteViews(context.packageName, R.layout.clock_widget)
            
            // Set the current time
            val calendar = Calendar.getInstance()
            val hour = SimpleDateFormat("hh", Locale.getDefault()).format(calendar.time)
            val minute = SimpleDateFormat("mm", Locale.getDefault()).format(calendar.time)
            val amPm = SimpleDateFormat("a", Locale.getDefault()).format(calendar.time).lowercase()
            
            views.setTextViewText(R.id.hour_text, hour)
            views.setTextViewText(R.id.minute_text, minute)
            views.setTextViewText(R.id.am_pm_text, amPm)
            
            // Set the location text (could be dynamic based on actual location)
            views.setTextViewText(R.id.location_text, "Warszawa")
            
            // Set up click intent to open user's preferred clock app
            val prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
            val clockPackage = prefs.getString("clockApp", "com.android.deskclock")
            
            val intent = clockPackage?.let {
                context.packageManager.getLaunchIntentForPackage(it) ?: 
                // Fallback to system clock if preferred clock app not found
                context.packageManager.getLaunchIntentForPackage("com.android.deskclock")
            } ?: Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent)
            
            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
