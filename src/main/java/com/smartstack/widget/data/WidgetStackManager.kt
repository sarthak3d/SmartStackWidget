package com.smartstack.widget.data

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.smartstack.widget.StackWidgetProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.*

/**
 * Widget Stack Manager
 * 
 * Manages the state and data for widget stacks, including:
 * - Widget ordering and persistence
 * - Smart rotation based on time and usage patterns
 * - Widget stack configuration
 * - Cross-reboot state restoration
 */
class WidgetStackManager(private val context: Context) {
    
    private val dataStore: DataStore<Preferences> = context.dataStore
    
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "widget_stack_data")
        
        // Preference keys
        private val WIDGET_STACK_ORDER = stringPreferencesKey("widget_stack_order")
        private val WIDGET_CURRENT_INDEX = intPreferencesKey("widget_current_index")
        private val WIDGET_LAST_UPDATE = longPreferencesKey("widget_last_update")
        private val WIDGET_USAGE_PATTERN = stringPreferencesKey("widget_usage_pattern")
        private val SMART_ROTATION_ENABLED = booleanPreferencesKey("smart_rotation_enabled")
        private val ROTATION_INTERVAL = intPreferencesKey("rotation_interval_minutes")
    }
    
    /**
     * Initialize the widget stack manager
     */
    suspend fun initialize() {
        // Set default values if not already set
        dataStore.edit { preferences ->
            if (!preferences.contains(SMART_ROTATION_ENABLED)) {
                preferences[SMART_ROTATION_ENABLED] = true
            }
            if (!preferences.contains(ROTATION_INTERVAL)) {
                preferences[ROTATION_INTERVAL] = 30 // 30 minutes default
            }
        }
    }
    
    /**
     * Get the current widget stack for a specific widget ID
     */
    suspend fun getWidgetStack(widgetId: Int): List<StackedWidget> {
        val stackOrderJson = dataStore.data.map { preferences ->
            preferences[WIDGET_STACK_ORDER] ?: "[]"
        }.first()
        
        return try {
            // Parse the JSON stack order and filter for this widget ID
            // For simplicity, we'll use a basic format: "widgetId:component1,component2,component3"
            val widgetStacks = stackOrderJson.split(";")
            val widgetStack = widgetStacks.find { it.startsWith("$widgetId:") }
            
            widgetStack?.substringAfter(":")?.split(",")?.mapNotNull { componentName ->
                createStackedWidget(componentName.trim())
            } ?: getDefaultWidgetStack()
        } catch (e: Exception) {
            getDefaultWidgetStack()
        }
    }
    
    /**
     * Set the widget stack for a specific widget ID
     */
    suspend fun setWidgetStack(widgetId: Int, widgets: List<StackedWidget>) {
        val currentStacks = dataStore.data.map { preferences ->
            preferences[WIDGET_STACK_ORDER] ?: ""
        }.first()
        
        val widgetStackString = widgets.joinToString(",") { it.componentName }
        val newStackOrder = if (currentStacks.isEmpty()) {
            "$widgetId:$widgetStackString"
        } else {
            val otherStacks = currentStacks.split(";").filter { !it.startsWith("$widgetId:") }
            (otherStacks + "$widgetId:$widgetStackString").joinToString(";")
        }
        
        dataStore.edit { preferences ->
            preferences[WIDGET_STACK_ORDER] = newStackOrder
        }
    }
    
    /**
     * Get the current widget index for a specific widget ID
     */
    suspend fun getCurrentWidgetIndex(widgetId: Int): Int {
        return dataStore.data.map { preferences ->
            preferences[WIDGET_CURRENT_INDEX] ?: 0
        }.first()
    }
    
    /**
     * Set the current widget index for a specific widget ID
     */
    suspend fun setCurrentWidgetIndex(widgetId: Int, index: Int) {
        dataStore.edit { preferences ->
            preferences[WIDGET_CURRENT_INDEX] = index
        }
    }
    
    /**
     * Rotate to the next widget in the stack
     */
    suspend fun rotateToNextWidget(context: Context, widgetId: Int) {
        val stack = getWidgetStack(widgetId)
        if (stack.isNotEmpty()) {
            val currentIndex = getCurrentWidgetIndex(widgetId)
            val nextIndex = (currentIndex + 1) % stack.size
            setCurrentWidgetIndex(widgetId, nextIndex)
            
            // Update the widget
            updateWidget(context, widgetId)
        }
    }
    
    /**
     * Rotate to the previous widget in the stack
     */
    suspend fun rotateToPreviousWidget(context: Context, widgetId: Int) {
        val stack = getWidgetStack(widgetId)
        if (stack.isNotEmpty()) {
            val currentIndex = getCurrentWidgetIndex(widgetId)
            val previousIndex = if (currentIndex > 0) currentIndex - 1 else stack.size - 1
            setCurrentWidgetIndex(widgetId, previousIndex)
            
            // Update the widget
            updateWidget(context, widgetId)
        }
    }
    
    /**
     * Handle smart rotation based on time and usage patterns
     */
    suspend fun scheduleSmartRotation(context: Context, widgetId: Int) {
        val isEnabled = dataStore.data.map { preferences ->
            preferences[SMART_ROTATION_ENABLED] ?: true
        }.first()
        
        if (!isEnabled) return
        
        val currentTime = System.currentTimeMillis()
        val lastUpdate = dataStore.data.map { preferences ->
            preferences[WIDGET_LAST_UPDATE] ?: 0L
        }.first()
        
        val rotationInterval = dataStore.data.map { preferences ->
            preferences[ROTATION_INTERVAL] ?: 30
        }.first() * 60 * 1000 // Convert to milliseconds
        
        if (currentTime - lastUpdate > rotationInterval) {
            // Time for smart rotation
            performSmartRotation(context, widgetId)
            
            dataStore.edit { preferences ->
                preferences[WIDGET_LAST_UPDATE] = currentTime
            }
        }
    }
    
    /**
     * Perform smart rotation based on time and usage patterns
     */
    private suspend fun performSmartRotation(context: Context, widgetId: Int) {
        val stack = getWidgetStack(widgetId)
        if (stack.isEmpty()) return
        
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        // Smart rotation logic based on time of day
        val targetIndex = when {
            hour in 6..9 -> 0 // Morning - show clock/weather
            hour in 10..17 -> 1 // Day - show calendar/tasks
            hour in 18..21 -> 2 // Evening - show entertainment
            else -> 3 // Night - show minimal info
        }
        
        val safeIndex = targetIndex.coerceIn(0, stack.size - 1)
        setCurrentWidgetIndex(widgetId, safeIndex)
        updateWidget(context, widgetId)
    }
    
    /**
     * Handle widget tap
     */
    suspend fun handleWidgetTap(context: Context, widgetId: Int) {
        val stack = getWidgetStack(widgetId)
        val currentIndex = getCurrentWidgetIndex(widgetId)
        
        if (currentIndex < stack.size) {
            val currentWidget = stack[currentIndex]
            
            // Launch the widget's associated app or perform widget-specific action
            try {
                val intent = context.packageManager.getLaunchIntentForPackage(currentWidget.packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback to main app
                val mainIntent = Intent(context, Class.forName("com.smartstack.widget.MainActivity"))
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(mainIntent)
            }
        }
    }
    
    /**
     * Remove widget stack data
     */
    suspend fun removeWidgetStack(widgetId: Int) {
        val currentStacks = dataStore.data.map { preferences ->
            preferences[WIDGET_STACK_ORDER] ?: ""
        }.first()
        
        val newStackOrder = currentStacks.split(";")
            .filter { !it.startsWith("$widgetId:") }
            .joinToString(";")
        
        dataStore.edit { preferences ->
            preferences[WIDGET_STACK_ORDER] = newStackOrder
        }
    }
    
    /**
     * Restore widget stack after reboot
     */
    suspend fun restoreWidgetStack(oldWidgetId: Int, newWidgetId: Int) {
        val stack = getWidgetStack(oldWidgetId)
        setWidgetStack(newWidgetId, stack)
        removeWidgetStack(oldWidgetId)
    }
    
    /**
     * Update the widget UI
     */
    private fun updateWidget(context: Context, widgetId: Int) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val intent = Intent(context, StackWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
        }
        context.sendBroadcast(intent)
    }
    
    /**
     * Clean up resources
     */
    suspend fun cleanup() {
        // Clean up any temporary data or scheduled tasks
    }
    
    /**
     * Get default widget stack
     */
    private fun getDefaultWidgetStack(): List<StackedWidget> {
        return listOf(
            StackedWidget("com.smartstack.widget.sample.ClockWidgetProvider", "Clock Widget"),
            StackedWidget("com.smartstack.widget.sample.WeatherWidgetProvider", "Weather Widget"),
            StackedWidget("com.smartstack.widget.sample.CalendarWidgetProvider", "Calendar Widget")
        )
    }
    
    /**
     * Create a stacked widget from component name
     */
    private fun createStackedWidget(componentName: String): StackedWidget? {
        return try {
            val className = Class.forName(componentName)
            StackedWidget(componentName, className.simpleName)
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Data class representing a widget in the stack
 */
data class StackedWidget(
    val componentName: String,
    val displayName: String,
    val packageName: String = componentName.substringBeforeLast(".")
) 