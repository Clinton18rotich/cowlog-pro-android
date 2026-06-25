package com.cowlog.pro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.cowlog.pro.data.AppData
import com.cowlog.pro.data.DataStore
import com.cowlog.pro.data.ProjectSettings
import com.cowlog.pro.ui.Navigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val loadedData = DataStore.load(this)
        
        setContent {
            var appData by remember { mutableStateOf(loadedData) }
            val settings = ProjectSettings()

            MaterialTheme(colorScheme = darkColorScheme(
                primary = Color(0xFFFF9F0A),
                background = Color(0xFF0A0A0A),
                surface = Color(0xFF1C1C1E),
            )) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Navigation(
                        appData = appData,
                        settings = settings,
                        onUpdate = { newData ->
                            appData = newData
                            DataStore.save(this@MainActivity, newData)
                        }
                    )
                }
            }
        }
    }
}
