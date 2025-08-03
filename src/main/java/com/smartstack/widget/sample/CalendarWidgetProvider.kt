package com.smartstack.widget.sample

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.util.*

/**
 * Sample Calendar Widget Provider
 * 
 * Displays calendar information in a simple format.
 * This is one of the sample widgets that can be added to the stack.
 */
class CalendarWidgetProvider : AppWidgetProvider() {
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateCalendarWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    private fun updateCalendarWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, android.R.layout.simple_list_item_1)
        
        val calendar = Calendar.getInstance()
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        
        views.setTextViewText(android.R.id.text1, "📅 $month/$dayOfMonth")
        
        // Set click intent to open calendar app
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setClassName("com.google.android.calendar", "com.android.calendar.AllInOneActivity")
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