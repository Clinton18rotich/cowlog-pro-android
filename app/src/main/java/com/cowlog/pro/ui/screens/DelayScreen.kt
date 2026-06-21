package com.cowlog.pro.ui.screens

import androidx.compose.foundation.layout.*
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
fun DelayScreen(
    appData: AppData,
    settings: ProjectSettings,
    navController: NavController,
    onUpdate: (AppData) -> Unit
) {
    Scaffold(
        topBar = { TopBar("Delays", navController) },
        bottomBar = { BottomNavBar(navController, "delays") }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🔧", fontSize = 48.sp)
                Text("Delays", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}
