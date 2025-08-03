package com.smartstack.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.smartstack.widget.data.WidgetStackManager
import com.smartstack.widget.service.StackWidgetService
import com.smartstack.widget.ui.theme.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Smart Stack Widget Provider
 * 
 * This widget provider creates a stackable widget container that can host multiple widgets
 * in a swipeable format, similar to iOS Smart Stack. The widget supports:
 * - Swipeable navigation between stacked widgets
 * - Smart rotation based on time and usage patterns
 * - Persistent widget state across reboots
 * - Drag and drop reordering (when supported)
 * - Android 15+ optimizations with adaptive sizing
 */
class StackWidgetProvider : AppWidgetProvider() {
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var stackManager: WidgetStackManager
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        stackManager = WidgetStackManager(context)
        
        appWidgetIds.forEach { appWidgetId ->
            updateStackWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    private fun updateStackWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Create the main widget layout
        val views = RemoteViews(context.packageName, R.layout.widget_stack_container)
        
        // Set up the stack service for widget content
        val intent = Intent(context, StackWidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        
        views.setRemoteAdapter(R.id.stack_widget_list, intent)
        views.setEmptyView(R.id.stack_widget_list, R.id.stack_widget_empty)
        
        // Set up click intents for widget interaction
        val clickIntent = Intent(context, StackWidgetProvider::class.java).apply {
            action = ACTION_WIDGET_CLICK
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        
        val clickPendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        views.setOnClickPendingIntent(R.id.stack_widget_container, clickPendingIntent)
        
        // Set up configuration intent
        val configIntent = Intent(context, StackWidgetConfigureActivity::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        
        val configPendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            configIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        views.setOnClickPendingIntent(R.id.stack_widget_config_button, configPendingIntent)
        
        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.stack_widget_list)
        
        // Schedule smart rotation updates
        scope.launch {
            stackManager.scheduleSmartRotation(context, appWidgetId)
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            ACTION_WIDGET_CLICK -> {
                val appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )
                
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    handleWidgetClick(context, appWidgetId)
                }
            }
            ACTION_SWIPE_LEFT -> {
                val appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )
                
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    scope.launch {
                        stackManager.rotateToNextWidget(context, appWidgetId)
                        updateStackWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
                    }
                }
            }
            ACTION_SWIPE_RIGHT -> {
                val appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )
                
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    scope.launch {
                        stackManager.rotateToPreviousWidget(context, appWidgetId)
                        updateStackWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
                    }
                }
            }
        }
    }
    
    private fun handleWidgetClick(context: Context, appWidgetId: Int) {
        // Handle widget tap - could open the current widget's app or show options
        scope.launch {
            stackManager.handleWidgetTap(context, appWidgetId)
        }
    }
    
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        
        stackManager = WidgetStackManager(context)
        appWidgetIds.forEach { appWidgetId ->
            scope.launch {
                stackManager.removeWidgetStack(appWidgetId)
            }
        }
    }
    
    override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)
        
        stackManager = WidgetStackManager(context)
        oldWidgetIds.forEachIndexed { index, oldId ->
            scope.launch {
                stackManager.restoreWidgetStack(oldId, newWidgetIds[index])
            }
        }
    }
    
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        
        // Initialize the stack manager when the first widget is added
        stackManager = WidgetStackManager(context)
        scope.launch {
            stackManager.initialize()
        }
    }
    
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        
        // Clean up when all widgets are removed
        scope.launch {
            stackManager.cleanup()
        }
    }
    
    companion object {
        const val ACTION_WIDGET_CLICK = "com.smartstack.widget.ACTION_WIDGET_CLICK"
        const val ACTION_SWIPE_LEFT = "com.smartstack.widget.ACTION_SWIPE_LEFT"
        const val ACTION_SWIPE_RIGHT = "com.smartstack.widget.ACTION_SWIPE_RIGHT"
    }
} 