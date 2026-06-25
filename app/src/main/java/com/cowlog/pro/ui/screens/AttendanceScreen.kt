package com.cowlog.pro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun AttendanceScreen(
    appData: AppData,
    settings: ProjectSettings,
    navController: NavController,
    onUpdate: (AppData) -> Unit
) {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var filterDate by remember { mutableStateOf(today) }
    val displayAttendance = appData.attendance.filter { it.date == filterDate.take(10) }
    val total = displayAttendance.sumOf { (it.count.toIntOrNull() ?: 0) }
    var showForm by remember { mutableStateOf(false) }

    Scaffold(
        
        
        floatingActionButton = { FloatingActionButton(onClick = { showForm = true }, containerColor = Color(0xFF0A84FF)) { Text("+", fontSize = 24.sp, color = Color.White) } }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            DateFilterBar(onToday = { filterDate = today }, onAll = { filterDate = "" })
            if (filterDate.isNotEmpty()) Text("${displayAttendance.size} records on ${filterDate.take(10)}", fontSize = 10.sp, color = Color(0xFF0A84FF), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))

            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Date: ${filterDate.ifEmpty { today }.take(10)}", fontSize = 10.sp, color = Color.Gray)
                    Text("Total Personnel: $total", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0A84FF))
                }
            }

            if (displayAttendance.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("👷", fontSize = 48.sp); Text("No attendance logged", color = Color.Gray); TextButton(onClick = { showForm = true }) { Text("+ Log Attendance") } }
                }
            } else {
                val grouped = displayAttendance.groupBy { it.category }
                LazyColumn {
                    items(grouped.keys.toList()) { cat ->
                        val items = grouped[cat] ?: emptyList()
                        val catTotal = items.sumOf { (it.count.toIntOrNull() ?: 0) }
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(cat, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text("$catTotal", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0A84FF))
                                }
                                items.forEach { Text("${it.name}: ${it.count}", fontSize = 11.sp, color = Color.Gray) }
                            TextButton(onClick = { val newData = appData.copy(); newData.attendance.removeAll { a -> items.any { it.id == a.id } }; onUpdate(newData) }) { Text("🗑️ Delete All", fontSize = 9.sp, color = Color(0xFFFF453A)) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        var category by remember { mutableStateOf("Skilled Labour") }
        var name by remember { mutableStateOf("") }
        var count by remember { mutableStateOf("1") }
        AlertDialog(
            onDismissRequest = { showForm = false },
            title = { Text("Log Attendance") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Staff", "Skilled", "Unskilled", "Subcontractor", "Visitor").forEach { c ->
                            FilterChip(selected = category.contains(c), onClick = { category = if (c == "Skilled") "Skilled Labour" else if (c == "Unskilled") "Unskilled Labour" else if (c == "Staff") "Contractor Staff" else c }, label = { Text(c, fontSize = 9.sp) })
                        }
                    }
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name/Trade") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = count, onValueChange = { count = it }, label = { Text("Count") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = { TextButton(onClick = { val n = appData.copy(); n.attendance.add(Attendance(id = UUID.randomUUID().toString(), date = today, category = category, name = name, count = count)); onUpdate(n); showForm = false }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { showForm = false }) { Text("Cancel") } }
        )
    }
}
