package com.cowlog.pro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cowlog.pro.data.*
import com.cowlog.pro.ui.components.DateFilterBar
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DelayScreen(
    appData: AppData,
    settings: ProjectSettings,
    navController: NavController,
    onUpdate: (AppData) -> Unit
) {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var filterDate by remember { mutableStateOf(today) }
    val displayDelays = appData.delays.filter { it.date == filterDate.take(10) }
    var showForm by remember { mutableStateOf(false) }

    Scaffold(
        
        
        floatingActionButton = { FloatingActionButton(onClick = { showForm = true }, containerColor = Color(0xFFFF9F0A)) { Text("+", fontSize = 24.sp, color = Color.White) } }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 12.dp)) {
            DateFilterBar(onToday = { filterDate = today }, onAll = { filterDate = "" })
            if (filterDate.isNotEmpty()) Text("${displayDelays.size} delays on ${filterDate.take(10)}", fontSize = 10.sp, color = Color(0xFF0A84FF), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))

            if (displayDelays.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("⏰", fontSize = 48.sp); Text("No delays recorded", color = Color.Gray); TextButton(onClick = { showForm = true }) { Text("+ Log Delay") } }
                }
            } else {
                LazyColumn {
                    items(displayDelays.sortedByDescending { it.timestamp }) { delay ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(delay.cause, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                Text("Duration: ${delay.duration} days | Impact: ${delay.impact}", fontSize = 11.sp, color = Color.Gray)
                                Text("📍 ${delay.location} | ${SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(delay.timestamp))}", fontSize = 10.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        var cause by remember { mutableStateOf("") }
        var duration by remember { mutableStateOf("1") }
        var impact by remember { mutableStateOf("Major") }
        var location by remember { mutableStateOf(settings.defaultLocation) }

        AlertDialog(
            onDismissRequest = { showForm = false },
            title = { Text("Log Delay", fontSize = 14.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = cause, onValueChange = { cause = it }, label = { Text("Cause") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration (days)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Minor", "Major", "Critical").forEach { s ->
                            FilterChip(selected = impact == s, onClick = { impact = s }, label = { Text(s, fontSize = 10.sp) })
                        }
                    }
                    OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
            },
            confirmButton = { Button(onClick = {
                if (cause.isNotBlank()) {
                    val n = appData.copy()
                    n.delays.add(Delay(id = UUID.randomUUID().toString(), date = filterDate.ifEmpty { today }.take(10), cause = cause, duration = duration, impact = impact, location = location, timestamp = System.currentTimeMillis()))
                    onUpdate(n)
                    showForm = false
                }
            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9F0A))) { Text("Save", color = Color.Black) } },
            dismissButton = { Button(onClick = { showForm = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("Cancel") } }
        )
    }
}
