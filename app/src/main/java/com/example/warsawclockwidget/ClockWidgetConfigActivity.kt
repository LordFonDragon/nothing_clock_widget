package com.example.warsawclockwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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

        // If launched without extras (e.g. via app icon), finish gracefully.
        if (intent.extras == null) {
            Log.w("ClockWidgetConfig", "No extras found; finishing configuration.")
            finish()
            return
        }

        setContentView(R.layout.activity_config)
        // Default result if user cancels.
        setResult(Activity.RESULT_CANCELED)

        // Retrieve the widget ID.
        appWidgetId = intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.e("ClockWidgetConfig", "Invalid widget ID; finishing activity.")
            finish()
            return
        }

        val clockApps = getClockApps()
        val listView = findViewById<ListView>(R.id.clock_apps_list)
        if (listView == null) {
            Log.e("ClockWidgetConfig", "ListView (R.id.clock_apps_list) not found in layout.")
            finish()
            return
        }

        val adapter = object : ArrayAdapter<ClockApp>(
            this,
            android.R.layout.simple_list_item_2,
            android.R.id.text1,
            clockApps
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(android.R.layout.simple_list_item_2, parent, false)
                val item = getItem(position)
                view.findViewById<TextView>(android.R.id.text1).text = item?.name ?: "Unknown"
                view.findViewById<TextView>(android.R.id.text2).text = item?.packageName ?: ""
                return view
            }
        }
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedApp = clockApps[position]
            getSharedPreferences("WidgetPrefs", MODE_PRIVATE).edit().apply {
                putString("clockApp", selectedApp.packageName)
                apply()
            }

            try {
                val appWidgetManager = AppWidgetManager.getInstance(this)
                ClockWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId)
            } catch (e: Exception) {
                Log.e("ClockWidgetConfig", "Error updating widget", e)
            }

            // Return OK with the widget ID.
            val resultIntent = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun getClockApps(): List<ClockApp> {
        val pm = packageManager
        // Use literal string for the clock category.
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory("android.intent.category.APP_CLOCK")
        }
        val apps = mutableListOf<ClockApp>()
        // Add the system clock if available.
        try {
            val info = pm.getApplicationInfo("com.android.deskclock", 0)
            val label = pm.getApplicationLabel(info).toString()
            apps.add(ClockApp(label, "com.android.deskclock"))
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w("ClockWidgetConfig", "System clock not found.")
        }
        // Query other clock apps.
        val resolvedApps = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (res in resolvedApps) {
            val pkg = res.activityInfo.packageName
            if (pkg != "com.android.deskclock") {
                val label = res.loadLabel(pm).toString()
                apps.add(ClockApp(label, pkg))
            }
        }
        return apps
    }

    data class ClockApp(val name: String, val packageName: String)
}
