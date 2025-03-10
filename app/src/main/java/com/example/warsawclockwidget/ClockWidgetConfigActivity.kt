package com.example.warsawclockwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ClockWidgetConfigActivity : AppCompatActivity() {
    
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)
        
        // Set the result to CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED)
        
        // Find the widget id from the intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        
        val clockApps = getClockApps()
        val listView = findViewById<ListView>(R.id.clock_apps_list)
        
        listView.adapter = object : ArrayAdapter<ClockApp>(
            this,
            android.R.layout.simple_list_item_2,
            android.R.id.text1,
            clockApps
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(android.R.layout.simple_list_item_2, parent, false)
                
                val app = getItem(position)
                view.findViewById<TextView>(android.R.id.text1).text = app?.name
                view.findViewById<TextView>(android.R.id.text2).text = app?.packageName
                
                return view
            }
        }
        
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedApp = clockApps[position]
            
            // Save the selected app's package name
            getSharedPreferences("WidgetPrefs", MODE_PRIVATE).edit().apply {
                putString("clockApp", selectedApp.packageName)
                apply()
            }
            
            // Update the widget
            val appWidgetManager = AppWidgetManager.getInstance(this)
            ClockWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId)
            
            // Set the result OK and finish
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }
    
    private fun getClockApps(): List<ClockApp> {
        val packageManager = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_APP_CLOCK)
        
        val clockApps = mutableListOf<ClockApp>()
        
        // Add system clock first
        val systemClockIntent = packageManager.getLaunchIntentForPackage("com.android.deskclock")
        if (systemClockIntent != null) {
            val systemClockInfo = packageManager.getApplicationInfo("com.android.deskclock", 0)
            val systemClockName = packageManager.getApplicationLabel(systemClockInfo).toString()
            clockApps.add(ClockApp(systemClockName, "com.android.deskclock"))
        }
        
        // Add other clock apps
        val apps = packageManager.queryIntentActivities(mainIntent, PackageManager.MATCH_DEFAULT_ONLY)
        for (app in apps) {
            val packageName = app.activityInfo.packageName
            if (packageName != "com.android.deskclock") {
                val appName = app.loadLabel(packageManager).toString()
                clockApps.add(ClockApp(appName, packageName))
            }
        }
        
        return clockApps
    }
    
    data class ClockApp(val name: String, val packageName: String)
}
