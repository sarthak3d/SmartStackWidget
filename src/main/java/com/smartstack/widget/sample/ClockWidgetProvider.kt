package com.smartstack.widget.sample

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*

/**
 * Sample Clock Widget Provider
 * 
 * Displays the current time in a simple format.
 * This is one of the sample widgets that can be added to the stack.
 */
class ClockWidgetProvider : AppWidgetProvider() {
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateClockWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    private fun updateClockWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, android.R.layout.simple_list_item_1)
        
        // Get current time
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault())
            .format(Date())
        
        views.setTextViewText(android.R.id.text1, "🕐 $currentTime")
        
        // Set click intent to open clock app
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setClassName("com.android.deskclock", "com.android.deskclock.DeskClock")
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