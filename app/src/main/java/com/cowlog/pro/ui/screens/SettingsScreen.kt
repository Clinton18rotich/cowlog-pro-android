package com.cowlog.pro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cowlog.pro.data.*
import com.cowlog.pro.ui.BottomNavBar
import com.cowlog.pro.ui.TopBar

@Composable
fun SettingsScreen(
    settings: ProjectSettings,
    navController: NavController,
    onUpdate: (ProjectSettings) -> Unit
) {
    var projectName by remember { mutableStateOf(settings.projectName) }
    var contractNo by remember { mutableStateOf(settings.contractNo) }
    var contractorName by remember { mutableStateOf(settings.contractorName) }
    var defaultLocation by remember { mutableStateOf(settings.defaultLocation) }
    var cowName by remember { mutableStateOf(settings.cowName) }
    var geminiKey by remember { mutableStateOf(settings.geminiKey) }
    
    Scaffold(
        topBar = { TopBar("⚙️ Settings", navController) },
        bottomBar = { BottomNavBar(navController, "settings") }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = projectName, onValueChange = { projectName = it }, label = { Text("Project Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = contractNo, onValueChange = { contractNo = it }, label = { Text("Contract No") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = contractorName, onValueChange = { contractorName = it }, label = { Text("Contractor") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = defaultLocation, onValueChange = { defaultLocation = it }, label = { Text("Default Location") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = cowName, onValueChange = { cowName = it }, label = { Text("Clerk of Works Name") }, modifier = Modifier.fillMaxWidth())
            
            OutlinedTextField(
                value = geminiKey,
                onValueChange = { geminiKey = it },
                label = { Text("🤖 Gemini API Key") },
                placeholder = { Text("AIzaSy... from aistudio.google.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Text("Get free API key at aistudio.google.com", fontSize = 9.sp, color = Color.Gray)
            
            Button(
                onClick = {
                    onUpdate(settings.copy(
                        projectName = projectName,
                        contractNo = contractNo,
                        contractorName = contractorName,
                        defaultLocation = defaultLocation,
                        cowName = cowName,
                        geminiKey = geminiKey
                    ))
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))
            ) { Text("💾 Save Settings") }
            
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))
            ) { Text("← Back") }
        }
    }
}
