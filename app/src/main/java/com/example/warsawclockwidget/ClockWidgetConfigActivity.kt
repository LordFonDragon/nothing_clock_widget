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
        setContentView(R.layout.activity_config)

        // Default result in case of cancellation
        setResult(Activity.RESULT_CANCELED)

        // Retrieve the App Widget ID from the intent extras.
        appWidgetId = intent.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // Get available clock apps.
        val clockApps = getClockApps()

        // Set up the list adapter.
        val listView = findViewById<ListView>(R.id.clock_apps_list)
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
                view.findViewById<TextView>(android.R.id.text1).text = item?.name
                view.findViewById<TextView>(android.R.id.text2).text = item?.packageName
                return view
            }
        }
        listView.adapter = adapter

        // When a clock app is selected, save the preference and update the widget.
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedApp = clockApps[position]
            getSharedPreferences("WidgetPrefs", MODE_PRIVATE)
                .edit()
                .putString("clockApp", selectedApp.packageName)
                .apply()

            // Update the widget with the selected app.
            try {
                val appWidgetManager = AppWidgetManager.getInstance(this)
                ClockWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId)
            } catch (e: Exception) {
                Log.e("ClockWidgetConfig", "Widget update failed", e)
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
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            // Use literal string for the clock category.
            addCategory("android.intent.category.APP_CLOCK")
        }
        val apps = mutableListOf<ClockApp>()

        // Attempt to add the system clock first.
        try {
            val info = pm.getApplicationInfo("com.android.deskclock", 0)
            val label = pm.getApplicationLabel(info).toString()
            apps.add(ClockApp(label, "com.android.deskclock"))
        } catch (e: PackageManager.NameNotFoundException) {
            // System clock not found.
        }

        // Query for other clock apps.
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
