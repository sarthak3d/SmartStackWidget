package com.smartstack.widget.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.smartstack.widget.StackWidgetProvider
import com.smartstack.widget.data.WidgetStackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Widget Update Service
 * 
 * Handles periodic widget updates and smart rotation scheduling.
 * This service runs in the background to keep widgets up to date.
 */
class WidgetUpdateService : JobService() {
    
    private val scope = CoroutineScope(Dispatchers.IO)
    
    override fun onStartJob(params: JobParameters?): Boolean {
        scope.launch {
            try {
                val stackManager = WidgetStackManager(this@WidgetUpdateService)
                val appWidgetManager = AppWidgetManager.getInstance(this@WidgetUpdateService)
                val componentName = ComponentName(this@WidgetUpdateService, StackWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                
                // Update all stack widgets
                appWidgetIds.forEach { appWidgetId ->
                    // Perform smart rotation if needed
                    stackManager.scheduleSmartRotation(this@WidgetUpdateService, appWidgetId)
                    
                    // Update the widget
                    val intent = Intent(this@WidgetUpdateService, StackWidgetProvider::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                    }
                    sendBroadcast(intent)
                }
                
                jobFinished(params, false)
            } catch (e: Exception) {
                jobFinished(params, true) // Reschedule on failure
            }
        }
        
        return true // Job is running asynchronously
    }
    
    override fun onStopJob(params: JobParameters?): Boolean {
        // Return true to reschedule the job if it was stopped
        return true
    }
    
    companion object {
        /**
         * Schedule periodic widget updates
         */
        fun scheduleWidgetUpdates(context: Context) {
            // This would typically use WorkManager or AlarmManager for scheduling
            // For simplicity, we'll rely on the widget provider to handle updates
        }
        
        /**
         * Cancel scheduled widget updates
         */
        fun cancelWidgetUpdates(context: Context) {
            // Cancel any scheduled updates
        }
    }
} 