package com.smartstack.widget.service

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.widget.RemoteViewsService
import com.smartstack.widget.data.StackedWidget
import com.smartstack.widget.data.WidgetStackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Stack Widget Service
 * 
 * Provides remote views for the widget stack, handling the display of
 * multiple widgets in a swipeable format using RemoteViews.
 */
class StackWidgetService : RemoteViewsService() {
    
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        
        return StackWidgetFactory(applicationContext, appWidgetId)
    }
}

/**
 * Remote Views Factory for the widget stack
 */
class StackWidgetFactory(
    private val context: android.content.Context,
    private val appWidgetId: Int
) : RemoteViewsService.RemoteViewsFactory {
    
    private val stackManager = WidgetStackManager(context)
    private val scope = CoroutineScope(Dispatchers.IO)
    private var widgetStack: List<StackedWidget> = emptyList()
    private var currentIndex: Int = 0
    
    override fun onCreate() {
        // Initialize the widget stack
        scope.launch {
            widgetStack = stackManager.getWidgetStack(appWidgetId)
            currentIndex = stackManager.getCurrentWidgetIndex(appWidgetId)
        }
    }
    
    override fun onDataSetChanged() {
        scope.launch {
            widgetStack = stackManager.getWidgetStack(appWidgetId)
            currentIndex = stackManager.getCurrentWidgetIndex(appWidgetId)
        }
    }
    
    override fun onDestroy() {
        // Clean up resources
    }
    
    override fun getCount(): Int {
        return widgetStack.size
    }
    
    override fun getViewAt(position: Int): android.widget.RemoteViews? {
        if (position >= widgetStack.size) return null
        
        val widget = widgetStack[position]
        
        // Create a remote view for the current widget
        val remoteViews = android.widget.RemoteViews(context.packageName, android.R.layout.simple_list_item_1)
        
        // Set the widget content based on the widget type
        when {
            widget.componentName.contains("ClockWidget") -> {
                return createClockWidgetView(widget)
            }
            widget.componentName.contains("WeatherWidget") -> {
                return createWeatherWidgetView(widget)
            }
            widget.componentName.contains("CalendarWidget") -> {
                return createCalendarWidgetView(widget)
            }
            else -> {
                return createDefaultWidgetView(widget)
            }
        }
    }
    
    override fun getLoadingView(): android.widget.RemoteViews? {
        return android.widget.RemoteViews(context.packageName, android.R.layout.simple_list_item_1).apply {
            setTextViewText(android.R.id.text1, "Loading...")
        }
    }
    
    override fun getViewTypeCount(): Int {
        return 1
    }
    
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    
    override fun hasStableIds(): Boolean {
        return true
    }
    
    /**
     * Create a clock widget view
     */
    private fun createClockWidgetView(widget: StackedWidget): android.widget.RemoteViews {
        val remoteViews = android.widget.RemoteViews(context.packageName, android.R.layout.simple_list_item_1)
        
        val currentTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date())
        
        remoteViews.setTextViewText(android.R.id.text1, "🕐 $currentTime")
        
        // Set click intent to open clock app
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
            addCategory(android.content.Intent.CATEGORY_LAUNCHER)
            setClassName("com.android.deskclock", "com.android.deskclock.DeskClock")
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            appWidgetId,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        remoteViews.setOnClickPendingIntent(android.R.id.text1, pendingIntent)
        
        return remoteViews
    }
    
    /**
     * Create a weather widget view
     */
    private fun createWeatherWidgetView(widget: StackedWidget): android.widget.RemoteViews {
        val remoteViews = android.widget.RemoteViews(context.packageName, android.R.layout.simple_list_item_1)
        
        // Simulate weather data (in a real app, this would come from a weather API)
        val temperature = "22°C"
        val condition = "☀️ Sunny"
        
        remoteViews.setTextViewText(android.R.id.text1, "$condition $temperature")
        
        // Set click intent to open weather app
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
            addCategory(android.content.Intent.CATEGORY_LAUNCHER)
            setClassName("com.google.android.apps.weather", "com.google.android.apps.weather.WeatherActivity")
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            appWidgetId,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        remoteViews.setOnClickPendingIntent(android.R.id.text1, pendingIntent)
        
        return remoteViews
    }
    
    /**
     * Create a calendar widget view
     */
    private fun createCalendarWidgetView(widget: StackedWidget): android.widget.RemoteViews {
        val remoteViews = android.widget.RemoteViews(context.packageName, android.R.layout.simple_list_item_1)
        
        val calendar = java.util.Calendar.getInstance()
        val dayOfMonth = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        
        remoteViews.setTextViewText(android.R.id.text1, "📅 $month/$dayOfMonth")
        
        // Set click intent to open calendar app
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
            addCategory(android.content.Intent.CATEGORY_LAUNCHER)
            setClassName("com.google.android.calendar", "com.android.calendar.AllInOneActivity")
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            appWidgetId,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        remoteViews.setOnClickPendingIntent(android.R.id.text1, pendingIntent)
        
        return remoteViews
    }
    
    /**
     * Create a default widget view
     */
    private fun createDefaultWidgetView(widget: StackedWidget): android.widget.RemoteViews {
        val remoteViews = android.widget.RemoteViews(context.packageName, android.R.layout.simple_list_item_1)
        
        remoteViews.setTextViewText(android.R.id.text1, "📱 ${widget.displayName}")
        
        // Set click intent to open the widget's app
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
            addCategory(android.content.Intent.CATEGORY_LAUNCHER)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            appWidgetId,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        remoteViews.setOnClickPendingIntent(android.R.id.text1, pendingIntent)
        
        return remoteViews
    }
} 