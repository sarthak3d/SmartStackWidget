package com.smartstack.widget.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smartstack.widget.data.WidgetStackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Boot Receiver
 * 
 * Handles widget restoration after device reboot.
 * This ensures that widget stacks are properly restored when the device starts up.
 */
class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                handleBootCompleted(context)
            }
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                handlePackageReplaced(context)
            }
        }
    }
    
    private fun handleBootCompleted(context: Context) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val stackManager = WidgetStackManager(context)
            
            // Initialize the stack manager
            stackManager.initialize()
            
            // Schedule widget updates for all existing widgets
            // This will be handled by the widget provider when it's restored
        }
    }
    
    private fun handlePackageReplaced(context: Context) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val stackManager = WidgetStackManager(context)
            
            // Re-initialize after app update
            stackManager.initialize()
        }
    }
} 