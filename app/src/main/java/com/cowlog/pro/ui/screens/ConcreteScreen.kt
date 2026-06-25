package com.cowlog.pro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.cowlog.pro.ui.BottomNavBar
import com.cowlog.pro.ui.TopBar
import java.text.SimpleDateFormat
import java.util.*

// Concrete grades with typical applications
val CONCRETE_GRADES = listOf(
    "C15/20 — Blinding", "C20/25 — Light duty", "C25/30 — General RC (slabs, beams, columns)",
    "C30/37 — Heavy duty / Water-retaining", "C35/45 — Precast elements",
    "C40/50 — High strength / Bridge works", "C50/60 — Special structures"
)

// Pour locations
val POUR_LOCATIONS = listOf(
    "Foundations — Pad Footing", "Foundations — Strip Footing", "Ground Floor Slab",
    "Column — Ground Floor", "Column — 1st Floor", "Column — 2nd Floor", "Column — 3rd Floor",
    "Beam — Ground Floor", "Beam — 1st Floor", "Beam — 2nd Floor",
    "Suspended Slab — 1st Floor", "Suspended Slab — 2nd Floor", "Suspended Slab — Roof",
    "Staircase", "Retaining Wall", "Lift Pit", "Septic Tank", "Underground Tank",
    "Boundary Wall Foundation", "Drainage Works", "External Paving"
)

@Composable
fun ConcreteScreen(appData: AppData, settings: ProjectSettings, navController: NavController, onUpdate: (AppData) -> Unit) {
    var showAdd by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf<String?>(null) }
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    // Calculate overdue cubes
    val overdue = appData.concrete.filter { cube ->
        try {
            val pourDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(cube.pourDate) ?: Date()
            val daysSince = (Date().time - pourDate.time) / (1000 * 60 * 60 * 24)
            daysSince >= 28 && cube.result28 == null
        } catch (e: Exception) { false }
    }
    val pending7 = appData.concrete.filter { cube ->
        try {
            val pourDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(cube.pourDate) ?: Date()
            val daysSince = (Date().time - pourDate.time) / (1000 * 60 * 60 * 24)
            daysSince >= 7 && cube.result7 == null
        } catch (e: Exception) { false }
    }
    val pending28 = appData.concrete.filter { cube ->
        try {
            val pourDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(cube.pourDate) ?: Date()
            val daysSince = (Date().time - pourDate.time) / (1000 * 60 * 60 * 24)
            daysSince >= 28 && cube.result28 == null
        } catch (e: Exception) { false }
    }
    val completedToday = appData.concrete.count { cube ->
        cube.result28 != null && cube.result28 >= (cube.grade.split("/").firstOrNull()?.replace("C", "")?.toDoubleOrNull() ?: 25.0)
    }
    
    Scaffold(
        
        
        floatingActionButton = { FloatingActionButton(onClick = { showAdd = true }, containerColor = Color(0xFFBF5AF2)) { Text("+", fontSize = 24.sp, color = Color.White) } }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 12.dp)) {
            // Dashboard
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                StatCube("${appData.concrete.size}", "Total", Color(0xFFBF5AF2))
                StatCube("${pending7.size}", "7-Day Due", Color(0xFFFF9F0A))
                StatCube("${pending28.size}", "28-Day Due", if (pending28.isNotEmpty()) Color(0xFFFF453A) else Color(0xFF30D158))
                StatCube("$completedToday", "Passed", Color(0xFF30D158))
            }
            
            // Overdue alert
            if (overdue.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0x33FF453A))) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("⚠️ Overdue 28-Day Tests (${overdue.size})", fontWeight = FontWeight.Bold, color = Color(0xFFFF453A), fontSize = 12.sp)
                        overdue.forEach { c ->
                            val pourDate = try { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(c.pourDate) } catch (e: Exception) { null }
                            val daysAgo = if (pourDate != null) (Date().time - pourDate.time) / (1000 * 60 * 60 * 24) else 0L
                            Text("${c.cubeId} — Pour: ${c.pourDate} (${daysAgo} days ago) — ${c.grade} — 📍 ${c.location}", fontSize = 10.sp, color = Color(0xFFFF453A))
                        }
                    }
                }
            }
            
            // Cubes list
            if (appData.concrete.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("🧪", fontSize = 48.sp); Text("No cubes registered", color = Color.Gray); TextButton(onClick = { showAdd = true }) { Text("+ Register Cube") } }
                }
            } else {
                LazyColumn {
                    items(appData.concrete.sortedByDescending { it.pourDate }) { cube ->
                        val r7 = cube.result7; val r14 = cube.result14; val r28 = cube.result28
                        val allDone = r28 != null
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), colors = CardDefaults.cardColors(containerColor = if (allDone) Color(0xFF1A2E1A) else Color(0xFF1C1C1E))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(cube.cubeId, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Surface(color = if (allDone) Color(0x4430D158) else Color(0x44FF9F0A), shape = MaterialTheme.shapes.small) {
                                        Text(if (allDone) "COMPLETE" else "PENDING", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 9.sp, color = if (allDone) Color(0xFF30D158) else Color(0xFFFF9F0A))
                                    }
                                }
                                Text("Pour: ${cube.pourDate} | ${cube.grade} | 📍 ${cube.location}", fontSize = 10.sp, color = Color.Gray)
                                
                                // Results grid
                                Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                        Text("7-Day", fontSize = 8.sp, color = Color.Gray)
                                        Text(r7?.toString() ?: "—", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (r7 != null && r7 >= 0.7 * (cube.grade.split("/").firstOrNull()?.replace("C","")?.toDoubleOrNull() ?: 25.0)) Color(0xFF30D158) else Color.White)
                                        if (r7 == null) TextButton(onClick = { showResult = cube.id + "_7" }, modifier = Modifier.height(24.dp)) { Text("+ Add", fontSize = 8.sp) }
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                        Text("14-Day", fontSize = 8.sp, color = Color.Gray)
                                        Text(r14?.toString() ?: "—", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        if (r14 == null) TextButton(onClick = { showResult = cube.id + "_14" }, modifier = Modifier.height(24.dp)) { Text("+ Add", fontSize = 8.sp) }
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                        Text("28-Day", fontSize = 8.sp, color = Color.Gray)
                                        Text(r28?.toString() ?: "—", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (r28 != null && r28 >= (cube.grade.split("/").firstOrNull()?.replace("C","")?.toDoubleOrNull() ?: 25.0)) Color(0xFF30D158) else if (r28 != null) Color(0xFFFF453A) else Color.White)
                                        if (r28 == null) TextButton(onClick = { showResult = cube.id + "_28" }, modifier = Modifier.height(24.dp)) { Text("+ Add", fontSize = 8.sp) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Add Cube Dialog
    if (showAdd) {
        var cubeId by remember { mutableStateOf("CUBE-${Date().time.toString().takeLast(6)}") }
        var pourDate by remember { mutableStateOf(today) }
        var location by remember { mutableStateOf(settings.defaultLocation) }
        var grade by remember { mutableStateOf("C25/30") }
        var showGradePicker by remember { mutableStateOf(false) }
        var showLocPicker by remember { mutableStateOf(false) }
        var slump by remember { mutableStateOf("") }
        
        AlertDialog(onDismissRequest = { showAdd = false }, title = { Text("Register Concrete Cube") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = cubeId, onValueChange = { cubeId = it }, label = { Text("Cube ID") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = pourDate, onValueChange = { pourDate = it }, label = { Text("Pour Date (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                
                TextButton(onClick = { showLocPicker = !showLocPicker }) { Text(if (location.isEmpty()) "Select Location ▼" else "📍 $location", fontSize = 11.sp) }
                if (showLocPicker) {
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp).verticalScroll(rememberScrollState())) {
                        POUR_LOCATIONS.forEach { l -> TextButton(onClick = { location = l; showLocPicker = false }) { Text(l, fontSize = 10.sp) } }
                    }
                }
                
                TextButton(onClick = { showGradePicker = !showGradePicker }) { Text(if (grade.isEmpty()) "Select Grade ▼" else "Grade: $grade", fontSize = 11.sp) }
                if (showGradePicker) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        CONCRETE_GRADES.forEach { g -> TextButton(onClick = { grade = g.take(6); showGradePicker = false }) { Text(g, fontSize = 10.sp) } }
                    }
                }
                
                OutlinedTextField(value = slump, onValueChange = { slump = it }, label = { Text("Slump (mm)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("e.g., 75") })
            }},
            confirmButton = { Button(onClick = { if (cubeId.isNotBlank()) { val n = appData.copy(); n.concrete.add(ConcreteCube(id = UUID.randomUUID().toString(), cubeId = cubeId, pourDate = pourDate, location = location, grade = grade, timestamp = System.currentTimeMillis())); onUpdate(n); showAdd = false } }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBF5AF2))) { Text("Register") } },
            dismissButton = { Button(onClick = { showAdd = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("Cancel") } }
        )
    }
    
    // Add Result Dialog
    if (showResult != null) {
        val parts = showResult!!.split("_")
        val cubeId = parts[0]
        val day = parts[1].toIntOrNull() ?: 7
        var result by remember { mutableStateOf("") }
        
        AlertDialog(onDismissRequest = { showResult = null }, title = { Text("Add ${day}-Day Result") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Cube: ${appData.concrete.find { it.id == cubeId }?.cubeId ?: ""}", fontSize = 11.sp, color = Color.Gray)
                OutlinedTextField(value = result, onValueChange = { result = it }, label = { Text("Strength (N/mm²) *") }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("e.g., 32.5") })
                Text("Required: ${appData.concrete.find { it.id == cubeId }?.grade?.split("/")?.firstOrNull()?.replace("C","") ?: "25"} N/mm² at 28 days", fontSize = 9.sp, color = Color.Gray)
            }},
            confirmButton = { Button(onClick = { if (result.isNotBlank()) { val n = appData.copy(); val i = n.concrete.indexOfFirst { it.id == cubeId }; if (i >= 0) { val r = result.toDoubleOrNull(); if (r != null) { n.concrete[i] = when (day) { 7 -> n.concrete[i].copy(result7 = r); 14 -> n.concrete[i].copy(result14 = r); 28 -> n.concrete[i].copy(result28 = r); else -> n.concrete[i] } } }; onUpdate(n); showResult = null } }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBF5AF2))) { Text("Save") } },
            dismissButton = { Button(onClick = { showResult = null }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("Cancel") } }
        )
    }
}

@Composable
fun StatCube(value: String, label: String, color: Color) {
    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1C1C1E), shape = MaterialTheme.shapes.small) {
        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 8.sp, color = Color.Gray)
        }
    }
}
