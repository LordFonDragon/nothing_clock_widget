package com.example.warsawclockwidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import java.util.*

class UpdateWidgetService : Service() {
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        updateWidgets()
        scheduleNextUpdate()
        return START_STICKY
    }
    
    private fun updateWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(this, ClockWidgetProvider::class.java)
        )
        
        if (appWidgetIds.isNotEmpty()) {
            // Update all active widgets
            for (appWidgetId in appWidgetIds) {
                ClockWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId)
            }
        }
    }
    
    private fun scheduleNextUpdate() {
        // Schedule the next update at the next minute
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, 1)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, UpdateWidgetService::class.java)
        val pendingIntent = PendingIntent.getService(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Schedule the alarm
        alarmManager.setExact(
            AlarmManager.RTC,
            calendar.timeInMillis,
            pendingIntent
        )
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
