package com.smartstack.widget.sample

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/**
 * Sample Weather Widget Provider
 * 
 * Displays weather information in a simple format.
 * This is one of the sample widgets that can be added to the stack.
 */
class WeatherWidgetProvider : AppWidgetProvider() {
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateWeatherWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    private fun updateWeatherWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, android.R.layout.simple_list_item_1)
        
        // Simulate weather data (in a real app, this would come from a weather API)
        val temperature = "22°C"
        val condition = "☀️ Sunny"
        
        views.setTextViewText(android.R.id.text1, "$condition $temperature")
        
        // Set click intent to open weather app
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setClassName("com.google.android.apps.weather", "com.google.android.apps.weather.WeatherActivity")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        views.setOnClickPendingIntent(android.R.id.text1, pendingIntent)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
} 