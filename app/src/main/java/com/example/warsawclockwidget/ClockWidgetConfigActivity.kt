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

        if (intent.extras == null) {
            Log.w("ClockWidgetConfig", "No extras found; finishing configuration.")
            finish()
            return
        }

        setContentView(R.layout.activity_config)
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val clockAppIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_APP_CLOCK)  // Corrected intent category
        }

        val clockApps = packageManager.queryIntentActivities(clockAppIntent, 0)
        val appNames = clockApps.map { it.loadLabel(packageManager).toString() }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, appNames)

        val listView = findViewById<ListView>(R.id.clock_app_list)
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedApp = clockApps[position].activityInfo.packageName
            val prefs = getSharedPreferences("WidgetPrefs", MODE_PRIVATE)
            prefs.edit().putString("clockApp", selectedApp).apply()

            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }

            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }
}
