package com.cowlog.pro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

val WORK_ITEMS = listOf(
    "Mobilization — Site Setup", "Mobilization — Hoarding",
    "Setting Out — Control Points", "Setting Out — Grid Lines", "Setting Out — Building Corners",
    "Bulk Excavation", "Trench Excavation — Foundations", "Trench Excavation — Drainage",
    "Backfill & Compaction", "Hardcore Filling", "Murram Blinding",
    "Blinding — 50mm", "DPM Installation", "Foundation Reinforcement", "Foundation Concreting",
    "Column Starter Bars", "Ground Beam Formwork", "Ground Beam Reinforcement", "Ground Beam Concreting",
    "GFS — Hardcore", "GFS — DPM", "GFS — Blinding", "GFS — BRC Placement", "GFS — Formwork", "GFS — Concreting",
    "Anti-Termite Treatment", "Substructure Backfill",
    "Column Reinf — GF", "Column Shutters — GF", "Column Concreting — GF",
    "Column Reinf — 1F", "Column Shutters — 1F", "Column Concreting — 1F",
    "Beam Formwork — GF", "Beam Reinf — GF", "Beam Concreting — GF",
    "Slab Formwork — GF", "Slab Reinf — GF", "Slab Concreting — GF",
    "Slab Formwork — Roof", "Slab Reinf — Roof", "Slab Concreting — Roof",
    "Staircase — Formwork", "Staircase — Reinf", "Staircase — Concreting",
    "Blockwork — GF", "Blockwork — 1F", "Hoop Iron Installation", "Wall Stiffeners",
    "Ring Beam Formwork", "Ring Beam Concreting", "Lintels — Precast", "Movement Joints",
    "Steel Fabrication", "Steel Erection", "Steel Decking", "Steel Fire Protection",
    "Roof Structure — Timber", "Roof Underlay", "Tile Roofing", "Sheet Roofing",
    "Ridge Capping", "Flashings", "Gutters & Downpipes",
    "Waterproofing — Roof", "Waterproofing — Wet Areas",
    "Below Ground Drainage", "Manhole Construction", "Supply Pipework",
    "Pressure Testing — Plumbing", "Sanitary Ware Installation",
    "Electrical — Containment", "Electrical — Cabling", "Electrical — DB Installation",
    "Electrical — Lighting", "Electrical — Power", "Electrical — Earthing",
    "Internal Plastering", "External Rendering", "Floor Screeding",
    "Floor Tiling", "Wall Tiling",
    "Painting — Internal Walls", "Painting — Ceilings", "Painting — External",
    "Door Frame Installation", "Door Leaf Installation", "Window Frame Installation",
    "Glazing", "Ironmongery", "Skirting & Architraves",
    "Suspended Ceiling — Grid", "Ceiling Tiles",
    "Road — Sub-base", "Road — Surfacing", "Paving", "Kerb & Channel",
    "Boundary Wall", "Gate Installation", "Landscaping",
    "Septic Tank — Excavation", "Septic Tank — Base", "Septic Tank — Walls",
    "Underground Tank — Base", "Underground Tank — Walls",
    "Water Supply Commissioning", "Drainage Commissioning", "Electrical Commissioning",
    "Snagging — Internal", "Snagging — External", "Cleaning", "Handover"
)

// Common site observations
val SITE_LOCATIONS = listOf(
    "📍 Site Wide / General",
    "📍 Block A - Ground Floor", "📍 Block A - First Floor", "📍 Block A - Second Floor",
    "📍 Block B - Ground Floor", "📍 Block B - First Floor", "📍 Block B - Second Floor",
    "📍 Block C - Ground Floor", "📍 Block C - First Floor",
    "📍 Main Entrance / Gate", "📍 Perimeter Wall / Fence",
    "📍 Parking Area", "📍 Driveway / Access Road",
    "📍 Excavation Area", "📍 Foundation / Substructure",
    "📍 Superstructure - Columns", "📍 Superstructure - Beams", "📍 Superstructure - Slabs",
    "📍 Staircase", "📍 Lift Shaft",
    "📍 Roof Level", "📍 Mechanical Floor / Plant Room",
    "📍 External Works - Paving", "📍 External Works - Drainage",
    "📍 External Works - Landscaping",
    "📍 Site Office", "📍 Material Storage Area", "📍 Workshop / Yard",
    "📍 Guard House", "📍 Water Tank / Tower",
    "📍 Septic Tank Area", "📍 Soak Pit Area",
)

val OBSERVATIONS = listOf(
    "✅ Work progressing well, within programme",
    "✅ Good quality workmanship observed",
    "✅ Materials delivered as per specification",
    "✅ Weather favourable, good productivity",
    "✅ All safety measures in place, PPE worn",
    "⚠️ Work slightly behind schedule — contractor to accelerate",
    "⚠️ Some quality issues noted — instructed contractor to rectify",
    "⚠️ Material shortage reported — delivery expected tomorrow",
    "⚠️ Equipment breakdown — repair underway",
    "⚠️ Labour shortage today — affecting progress",
    "🌧️ Work stopped due to heavy rain — resumed at ___",
    "🌧️ Rain affected concreting — extra curing applied",
    "❌ Work stopped — safety concern identified",
    "❌ Defective work rejected — NCR raised",
    "❌ Materials rejected — not to specification",
    "📋 Inspection carried out — see inspection report",
    "📋 Site meeting held — see minutes",
    "📋 Engineer's instruction received — complying",
    "🚚 Concrete delivery from ___ — ___ m³ delivered",
    "🚚 Steel delivery from ___ — ___ tonnes received",
    "🔧 Formwork being erected for ___",
    "🔧 Reinforcement being tied for ___",
    "💧 Curing ongoing — surfaces kept moist",
    "🧹 Site cleaning in progress",
    "📸 Photographs taken for record"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(appData: AppData, settings: ProjectSettings, navController: NavController, onUpdate: (AppData) -> Unit) {
    var showForm by remember { mutableStateOf(false) }
    var filterDate by remember { mutableStateOf("") }
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val filteredEntries = if (filterDate.length >= 10) {
        appData.diary.filter { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp)) == filterDate.take(10) }
    } else appData.diary

    Scaffold(
        topBar = { TopBar("📔 Site Diary", navController) },
        bottomBar = { BottomNavBar(navController, "diary") },
        floatingActionButton = { FloatingActionButton(onClick = { showForm = true }, containerColor = Color(0xFF0A84FF)) { Text("+", color = Color.White, fontSize = 24.sp) } }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                QuickBtn("📔 +") { showForm = true }; QuickBtn("✅") { navController.navigate("inspections") }
                QuickBtn("🚨") { navController.navigate("ncr") }; QuickBtn("📋") { navController.navigate("si") }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                QuickBtn("🚜") { navController.navigate("plant") }; QuickBtn("🧱") { navController.navigate("materials") }
                QuickBtn("👷") { navController.navigate("attendance") }; QuickBtn("📄") { navController.navigate("report") }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = filterDate, onValueChange = { filterDate = it }, label = { Text("Date: yyyy-MM-dd") }, modifier = Modifier.weight(1f).height(50.dp), singleLine = true, textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp))
                Button(onClick = { filterDate = today }, modifier = Modifier.height(36.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))) { Text("Today", fontSize = 9.sp) }
                Button(onClick = { filterDate = "" }, modifier = Modifier.height(36.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("All", fontSize = 9.sp) }
            }
            if (filterDate.isNotEmpty()) Text("${filteredEntries.size} entries on ${filterDate.take(10)}", fontSize = 10.sp, color = Color(0xFF0A84FF), modifier = Modifier.padding(vertical = 2.dp))

            if (filteredEntries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("📔", fontSize = 48.sp); Text("No diary entries", color = Color.Gray); Text("Tap + to log activities", fontSize = 12.sp, color = Color.DarkGray) }
                }
            } else {
                LazyColumn {
                    items(filteredEntries.sortedByDescending { it.timestamp }) { entry ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(entry.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                    if (entry.percentComplete.isNotEmpty()) {
                                        Surface(color = Color(0xFF0A84FF).copy(alpha = 0.2f), shape = MaterialTheme.shapes.small) {
                                            Text("${entry.percentComplete}%", fontSize = 10.sp, color = Color(0xFF0A84FF), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                                Text("📍 ${entry.location} | 🕐 ${SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(entry.timestamp))}", fontSize = 10.sp, color = Color.Gray)
                                Text(entry.description, fontSize = 12.sp, maxLines = 4)
                            }
                        }
                    }
                }
            }
        }
    }

    // Diary entry form with work items AND observations
    if (showForm) {
        var selectedWork by remember { mutableStateOf("") }
        var selectedObs by remember { mutableStateOf("") }
        var title by remember { mutableStateOf("") }
        var location by remember { mutableStateOf(settings.defaultLocation) }
        var showLocPicker by remember { mutableStateOf(false) }
        var description by remember { mutableStateOf("") }
        var percentComplete by remember { mutableStateOf("") }
        var showWorkPicker by remember { mutableStateOf(false) }
        var showObsPicker by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showForm = false },
            title = { Text("New Diary Entry", fontSize = 14.sp) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Work item picker
                    TextButton(onClick = { showWorkPicker = !showWorkPicker }) {
                        Text(if (selectedWork.isEmpty()) "📋 Select Work Item ▼" else "📋 $selectedWork", fontSize = 12.sp, color = Color(0xFF0A84FF))
                    }
                    if (showWorkPicker) {
                        Column(modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp).verticalScroll(rememberScrollState())) {
                            WORK_ITEMS.forEach { item ->
                                TextButton(onClick = { selectedWork = item; title = item; showWorkPicker = false }, modifier = Modifier.fillMaxWidth()) {
                                    Text(item, fontSize = 10.sp, color = if (selectedWork == item) Color(0xFF0A84FF) else Color.White)
                                }
                            }
                        }
                    }
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    TextButton(onClick = { showLocPicker = !showLocPicker }) {
                        Text(if (location.isEmpty()) "📍 Select Location ▼" else "📍 $location", fontSize = 12.sp, color = Color(0xFFFF9F0A))
                    }
                    if (showLocPicker) {
                        Column(modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp).verticalScroll(rememberScrollState())) {
                            SITE_LOCATIONS.forEach { loc ->
                                TextButton(onClick = { location = loc; showLocPicker = false }, modifier = Modifier.fillMaxWidth()) {
                                    Text(loc, fontSize = 10.sp, color = if (location == loc) Color(0xFFFF9F0A) else Color.White)
                                }
                            }
                        }
                    }

                    // Observations picker
                    TextButton(onClick = { showObsPicker = !showObsPicker }) {
                        Text(if (selectedObs.isEmpty()) "📝 Select Observation ▼" else "📝 $selectedObs", fontSize = 12.sp, color = Color(0xFF30D158))
                    }
                    if (showObsPicker) {
                        Column(modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp).verticalScroll(rememberScrollState())) {
                            OBSERVATIONS.forEach { obs ->
                                TextButton(onClick = { selectedObs = obs; description = if (description.isEmpty()) obs else "$description\n$obs"; showObsPicker = false }, modifier = Modifier.fillMaxWidth()) {
                                    Text(obs, fontSize = 10.sp, color = if (selectedObs == obs) Color(0xFF30D158) else Color.White)
                                }
                            }
                        }
                    }

                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description *") }, modifier = Modifier.fillMaxWidth(), maxLines = 4, placeholder = { Text("Work Progress & Observations — describe what happened...") })
                    Text("% Complete:", fontSize = 11.sp, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        listOf("0", "10", "25", "50", "75", "90", "100").forEach { pct ->
                            FilterChip(selected = percentComplete == pct, onClick = { percentComplete = pct }, label = { Text("$pct%", fontSize = 10.sp) })
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        val newEntry = DiaryEntry(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            activityType = selectedWork.ifEmpty { "Work in Progress" },
                            location = location,
                            description = description,
                            percentComplete = percentComplete,
                            issuesIdentified = selectedObs,
                            timestamp = System.currentTimeMillis()
                        )
                        val newData = appData.copy(); newData.diary.add(newEntry); onUpdate(newData); showForm = false
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))) { Text("Save", color = Color.White) }
            },
            dismissButton = { Button(onClick = { showForm = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("Cancel") } }
        )
    }
}

@Composable
fun QuickBtn(label: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = MaterialTheme.shapes.small, color = Color(0xFF2C2C2E)) {
        Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 13.sp)
    }
}
