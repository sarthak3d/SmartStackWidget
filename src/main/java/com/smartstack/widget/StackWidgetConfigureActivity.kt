package com.smartstack.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartstack.widget.data.StackedWidget
import com.smartstack.widget.data.WidgetStackManager
import com.smartstack.widget.ui.theme.SmartStackWidgetTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StackWidgetConfigureActivity : ComponentActivity() {
    
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var stackManager: WidgetStackManager
    private val scope = CoroutineScope(Dispatchers.IO)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get the widget ID from the intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        
        stackManager = WidgetStackManager(this)
        
        setContent {
            SmartStackWidgetTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ConfigureScreen()
                }
            }
        }
    }
    
    @Composable
    fun ConfigureScreen() {
        val context = LocalContext.current
        var availableWidgets by remember { mutableStateOf<List<StackedWidget>>(emptyList()) }
        var selectedWidgets by remember { mutableStateOf<List<StackedWidget>>(emptyList()) }
        var smartRotationEnabled by remember { mutableStateOf(true) }
        var rotationInterval by remember { mutableStateOf(30) }
        
        // Load current configuration
        LaunchedEffect(Unit) {
            scope.launch {
                selectedWidgets = stackManager.getWidgetStack(appWidgetId)
                availableWidgets = getAvailableWidgets()
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Configure Smart Stack Widget",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Customize your widget stack and settings",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Smart Rotation Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Smart Rotation",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable automatic widget rotation")
                        Switch(
                            checked = smartRotationEnabled,
                            onCheckedChange = { smartRotationEnabled = it }
                        )
                    }
                    
                    if (smartRotationEnabled) {
                        Text("Rotation interval (minutes):")
                        Slider(
                            value = rotationInterval.toFloat(),
                            onValueChange = { rotationInterval = it.toInt() },
                            valueRange = 5f..120f,
                            steps = 23
                        )
                        Text("$rotationInterval minutes")
                    }
                }
            }
            
            // Widget Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Available Widgets",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(availableWidgets) { widget ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(widget.displayName)
                                Checkbox(
                                    checked = selectedWidgets.contains(widget),
                                    onCheckedChange = { checked ->
                                        selectedWidgets = if (checked) {
                                            selectedWidgets + widget
                                        } else {
                                            selectedWidgets - widget
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { finish() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = {
                        scope.launch {
                            // Save configuration
                            stackManager.setWidgetStack(appWidgetId, selectedWidgets)
                            
                            // Update the widget
                            val appWidgetManager = AppWidgetManager.getInstance(context)
                            val intent = Intent(context, StackWidgetProvider::class.java).apply {
                                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                            }
                            context.sendBroadcast(intent)
                            
                            // Return result
                            val resultValue = Intent().apply {
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            }
                            setResult(Activity.RESULT_OK, resultValue)
                            finish()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Configuration")
                }
            }
        }
    }
    
    private suspend fun getAvailableWidgets(): List<StackedWidget> {
        return listOf(
            StackedWidget("com.smartstack.widget.sample.ClockWidgetProvider", "Clock Widget"),
            StackedWidget("com.smartstack.widget.sample.WeatherWidgetProvider", "Weather Widget"),
            StackedWidget("com.smartstack.widget.sample.CalendarWidgetProvider", "Calendar Widget"),
            StackedWidget("com.smartstack.widget.sample.BatteryWidgetProvider", "Battery Widget"),
            StackedWidget("com.smartstack.widget.sample.MusicWidgetProvider", "Music Widget")
        )
    }
} 