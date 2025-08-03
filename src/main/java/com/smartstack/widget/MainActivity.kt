package com.smartstack.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartstack.widget.ui.theme.SmartStackWidgetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartStackWidgetTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val appWidgetManager = remember { AppWidgetManager.getInstance(context) }
    val stackWidgetComponent = remember { ComponentName(context, StackWidgetProvider::class.java) }
    
    val stackWidgetIds by remember {
        derivedStateOf {
            appWidgetManager.getAppWidgetIds(stackWidgetComponent)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Smart Stack Widget",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Create a customizable widget stack inspired by iOS Smart Stack",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Widget Status",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = if (stackWidgetIds.isNotEmpty()) {
                        "${stackWidgetIds.size} stack widget(s) active"
                    } else {
                        "No stack widgets added to home screen"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Button(
            onClick = {
                // Add stack widget to home screen
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Stack Widget to Home Screen")
        }
        
        OutlinedButton(
            onClick = {
                // Open widget configuration
                if (stackWidgetIds.isNotEmpty()) {
                    val intent = Intent(context, StackWidgetConfigureActivity::class.java)
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, stackWidgetIds[0])
                    context.startActivity(intent)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = stackWidgetIds.isNotEmpty()
        ) {
            Text("Configure Stack Widget")
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "Features:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("• Swipeable widget stack")
            Text("• Smart rotation based on time/usage")
            Text("• Drag & drop reordering")
            Text("• Persistent widget state")
            Text("• Android 15+ optimizations")
        }
    }
} 