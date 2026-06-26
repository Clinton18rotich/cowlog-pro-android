package com.cowlog.pro.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cowlog.pro.data.ProjectSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: ProjectSettings,
    navController: NavController,
    onUpdate: (ProjectSettings) -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("cowlog_settings", Context.MODE_PRIVATE)
    
    var projectName by remember { mutableStateOf(prefs.getString("project_name", "") ?: "") }
    var contractorName by remember { mutableStateOf(prefs.getString("contractor_name", "") ?: "") }
    var contractNo by remember { mutableStateOf(prefs.getString("contract_no", "") ?: "") }
    var cowName by remember { mutableStateOf(prefs.getString("cow_name", "") ?: "") }
    var defaultLocation by remember { mutableStateOf(prefs.getString("default_location", "Main Site") ?: "Main Site") }
    var geminiKey by remember { mutableStateOf(prefs.getString("gemini_key", "") ?: "") }

    fun save() {
        prefs.edit()
            .putString("project_name", projectName)
            .putString("contractor_name", contractorName)
            .putString("contract_no", contractNo)
            .putString("cow_name", cowName)
            .putString("default_location", defaultLocation)
            .putString("gemini_key", geminiKey)
            .apply()
        
        // Update the settings object
        val updated = settings.copy(
            projectName = projectName,
            contractorName = contractorName,
            contractNo = contractNo,
            cowName = cowName,
            defaultLocation = defaultLocation,
            geminiKey = geminiKey
        )
        onUpdate(updated)
        Toast.makeText(context, "✅ Settings saved", Toast.LENGTH_SHORT).show()
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("⚙️ Project Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9F0A))
            
            OutlinedTextField(value = projectName, onValueChange = { projectName = it }, 
                label = { Text("Project Name") }, leadingIcon = { Icon(Icons.Default.Home, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            
            OutlinedTextField(value = contractorName, onValueChange = { contractorName = it },
                label = { Text("Contractor Name") }, leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            
            OutlinedTextField(value = contractNo, onValueChange = { contractNo = it },
                label = { Text("Contract Number") }, leadingIcon = { Icon(Icons.Default.Info, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            
            OutlinedTextField(value = cowName, onValueChange = { cowName = it },
                label = { Text("Clerk of Works Name") }, leadingIcon = { Icon(Icons.Default.Star, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            
            OutlinedTextField(value = defaultLocation, onValueChange = { defaultLocation = it },
                label = { Text("Default Location") }, leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            
            OutlinedTextField(value = geminiKey, onValueChange = { geminiKey = it },
                label = { Text("Gemini API Key (optional)") }, leadingIcon = { Icon(Icons.Default.Lock, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true)

            Spacer(modifier = Modifier.height(8.dp))
            
            Button(onClick = { save() }, modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9F0A))) {
                Text("💾 Save Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}
