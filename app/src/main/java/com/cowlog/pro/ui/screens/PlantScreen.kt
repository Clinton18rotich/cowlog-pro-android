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
import com.cowlog.pro.ui.components.DateFilterBar
import java.text.SimpleDateFormat
import java.util.*

val PLANT_CATEGORIES = linkedMapOf(
    "🏗️ LIFTING EQUIPMENT" to listOf("Tower Crane — Static", "Tower Crane — Mobile", "Mobile Crane — 20T", "Mobile Crane — 50T", "Mobile Crane — 100T", "Crawler Crane", "Truck Mounted Crane", "Overhead Crane", "Chain Block / Hoist", "Winch"),
    "🚜 EARTHMOVING" to listOf("Excavator — 20T (Tracked)", "Excavator — 30T (Tracked)", "Excavator — Wheeled", "Backhoe Loader (JCB)", "Bulldozer — D6", "Bulldozer — D8", "Wheel Loader", "Skid Steer Loader", "Motor Grader", "Tractor with Trailer", "Tractor with Ripper"),
    "🚛 HAULAGE" to listOf("Dump Truck — 10m³", "Dump Truck — 20m³", "Tipper Truck — 7T", "Tipper Truck — 15T", "Articulated Dump Truck (ADT)", "Lowbed Trailer", "Flatbed Truck", "Pickup — 4x4", "Lorry — 7T"),
    "🔄 CONCRETE EQUIPMENT" to listOf("Concrete Mixer — 1 Bag (Diesel)", "Concrete Mixer — 1 Bag (Electric)", "Concrete Mixer — 2 Bag", "Concrete Pan Mixer", "Ready Mix Truck (Transit Mixer)", "Concrete Pump — Static", "Concrete Pump — Boom (28m)", "Concrete Pump — Boom (36m)", "Poker Vibrator — 40mm", "Poker Vibrator — 60mm", "Poker Vibrator — 80mm", "Beam Screed / Vibrating Screed", "Power Trowel — Walk-behind", "Power Trowel — Ride-on", "Concrete Bucket — 0.5m³", "Concrete Bucket — 1m³"),
    "📏 COMPACTION" to listOf("Roller — Smooth Drum 10T", "Roller — Smooth Drum 15T", "Roller — Padfoot/Sheepfoot", "Vibratory Roller — 2.5T", "Vibratory Roller — 5T", "Plate Compactor — 100kg", "Plate Compactor — 200kg", "Rammer / Jumping Jack", "Pneumatic Roller", "Tandem Roller"),
    "🔧 STEEL & FORMWORK" to listOf("Bar Bending Machine — 40mm", "Bar Bending Machine — 50mm", "Bar Cutting Machine — 40mm", "Bar Cutting Machine — 50mm", "Welding Machine — Arc", "Welding Machine — MIG", "Welding Generator", "Steel Shutters — Column", "Steel Shutters — Beam", "Steel Shutters — Slab", "H-Frames / Acrow Props — No.1 (1.2-2.0m)", "H-Frames / Acrow Props — No.2 (2.0-3.0m)", "H-Frames / Acrow Props — No.3 (3.0-4.0m)", "Plywood Shutters — 18mm", "Steel Pans / Decking", "Grinding Machine — Angle", "Cutting Torch — Oxy-acetylene"),
    "🏭 POWER GENERATION" to listOf("Generator — 5kVA (Petrol)", "Generator — 10kVA (Diesel)", "Generator — 25kVA (Diesel)", "Generator — 50kVA (Diesel)", "Generator — 100kVA (Diesel)", "Generator — 200kVA (Diesel)", "Welding Generator — 15kVA", "Lighting Tower — 6kW", "Solar Panel Setup — Site Office"),
    "💧 WATER & PUMPS" to listOf("Water Pump — 2\" (Petrol)", "Water Pump — 3\" (Diesel)", "Water Pump — 4\" (Diesel)", "Submersible Pump — 2\"", "Submersible Pump — 4\"", "Submersible Pump — 6\"", "Water Bowser — 5000L", "Water Bowser — 10000L", "Water Tank — 5000L", "Water Tank — 10000L", "Pressure Washer — Petrol", "Pressure Washer — Electric"),
    "🏗️ SCAFFOLDING & ACCESS" to listOf("Scaffolding — Steel Tube (per set)", "Scaffolding — Cuplock System", "Scaffolding Boards — Timber", "Scaffolding Boards — Steel", "Mobile Scaffold Tower", "Scissor Lift — 8m", "Scissor Lift — 12m", "Boom Lift — Articulated", "Ladder — Aluminium 3m", "Ladder — Aluminium 5m"),
    "🔨 DEMOLITION & BREAKING" to listOf("Jack Hammer — Electric", "Jack Hammer — Pneumatic", "Breaker — Hydraulic (Excavator mounted)", "Concrete Crusher Attachment", "Demolition Robot", "Hydraulic Splitter"),
    "📐 SURVEY & TESTING" to listOf("Total Station", "Auto Level / Dumpy Level", "GPS Rover", "Theodolite", "Measuring Wheel", "Laser Distance Meter", "Water Level (Hose Pipe)", "Concrete Test Hammer (Schmidt)", "Slump Cone Set", "Cube Moulds — 150mm", "DCP (Dynamic Cone Penetrometer)", "Nuclear Density Gauge", "Sand Replacement Test Kit"),
    "🪚 WOODWORKING" to listOf("Circular Saw — Bench", "Circular Saw — Handheld", "Table Saw", "Planer / Thicknesser", "Router — Electric", "Jigsaw", "Nail Gun — Pneumatic", "Staple Gun", "Belt Sander"),
    "⚡ ELECTRICAL TOOLS" to listOf("Hammer Drill", "Rotary Hammer (SDS)", "Angle Grinder — 4.5\"", "Angle Grinder — 9\"", "Core Drilling Machine", "Threading Machine — Pipe", "Cable Pulling Winch", "Crimping Tool — Hydraulic"),
    "🔥 OTHER" to listOf("Water Cart / Bowser", "Fuel Tank — 1000L", "Fuel Tank — 5000L", "Site Dumper — 1T", "Forklift — 3T", "Forklift — 5T", "Telehandler", "Concrete Batching Plant — Mobile", "Stone Crusher — Portable", "Asphalt Paver", "Asphalt Roller", "Tar Boiler / Bitumen Sprayer", "Road Marking Machine")
)

@Composable
fun PlantScreen(appData: AppData, settings: ProjectSettings, navController: NavController, onUpdate: (AppData) -> Unit) {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var filterDate by remember { mutableStateOf(today) }
    val displayLogs = appData.plantDailyLogs.filter { it.date == filterDate.take(10) }
    val totalHours = displayLogs.sumOf { (it.hoursWorked.toDoubleOrNull() ?: 0.0).toInt() }
    val activePlant = appData.plantEquipment.filter { it.status != "Demobilized" }.size
    val workingToday = displayLogs.filter { (it.hoursWorked.toDoubleOrNull() ?: 0.0) > 0 }.size

    var showAdd by remember { mutableStateOf(false) }
    var showLog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        
        
        floatingActionButton = { FloatingActionButton(onClick = { showAdd = true }, containerColor = Color(0xFF0A84FF)) { Text("+", fontSize = 24.sp, color = Color.White) } }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 12.dp)) {
            DateFilterBar(onToday = { filterDate = today }, onAll = { filterDate = "" })
            if (filterDate.isNotEmpty()) Text("${displayLogs.size} logs on ${filterDate.take(10)}", fontSize = 10.sp, color = Color(0xFF0A84FF), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                StatP("${activePlant}", "On Site", Color(0xFF0A84FF))
                StatP("$workingToday", "Working", Color(0xFF30D158))
                StatP("${activePlant - workingToday}", "Idle", Color(0xFFFF9F0A))
                StatP("${totalHours}h", "Hours", Color(0xFF30D158))
            }

            if (appData.plantEquipment.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("🚜", fontSize = 48.sp); Text("No plant registered", color = Color.Gray); TextButton(onClick = { showAdd = true }) { Text("+ Add Plant") } }
                }
            } else {
                LazyColumn {
                    items(appData.plantEquipment.sortedBy { it.name }) { plant ->
                        val todayLog = displayLogs.find { it.plantId == plant.id }
                        val sc = when (plant.status) { "Working" -> Color(0xFF30D158); "Idle" -> Color(0xFFFF9F0A); "Under Repair" -> Color(0xFFFF453A); else -> Color.Gray }
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(plant.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Type: ${plant.type} | Owner: ${plant.owner}", fontSize = 9.sp, color = Color.Gray)
                                    }
                                    Surface(color = sc.copy(alpha = 0.2f), shape = MaterialTheme.shapes.small) { Text(plant.status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, color = sc) }
                                }
                                if (todayLog != null) {
                                    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Today: ${todayLog.hoursWorked}h worked | ${todayLog.hoursIdle}h idle", fontSize = 10.sp, color = Color(0xFF0A84FF))
                                        if (todayLog.fuelIssued.isNotEmpty()) Text("⛽ ${todayLog.fuelIssued}L", fontSize = 10.sp, color = Color(0xFFFF9F0A))
                                    }
                                }
                                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Button(onClick = { showLog = plant.id }, modifier = Modifier.height(28.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))) { Text("Log Hours", fontSize = 9.sp) }
                                    Button(onClick = { val n = appData.copy(); val i = n.plantEquipment.indexOfFirst { it.id == plant.id }; if (i >= 0) { val ns = when (plant.status) { "Working" -> "Idle"; "Idle" -> "Under Repair"; "Under Repair" -> "Working"; else -> "Working" }; n.plantEquipment[i] = n.plantEquipment[i].copy(status = ns); onUpdate(n) } }, modifier = Modifier.height(28.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("Status", fontSize = 9.sp) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        var name by remember { mutableStateOf("") }; var type by remember { mutableStateOf("") }
        var selectedCat by remember { mutableStateOf("") }; var owner by remember { mutableStateOf(settings.contractorName) }
        var dateOn by remember { mutableStateOf(today) }
        AlertDialog(onDismissRequest = { showAdd = false }, title = { Text("Add Plant/Equipment") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (type.isEmpty()) { Text("Select Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0A84FF))
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).verticalScroll(rememberScrollState())) { PLANT_CATEGORIES.keys.forEach { cat -> TextButton(onClick = { selectedCat = cat }, modifier = Modifier.fillMaxWidth()) { Text(cat, fontSize = 10.sp, color = Color.White) } } }
                }
                if (selectedCat.isNotEmpty() && type.isEmpty()) { Text("Select ${selectedCat.take(20)}:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0A84FF))
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).verticalScroll(rememberScrollState())) { PLANT_CATEGORIES[selectedCat]?.forEach { item -> TextButton(onClick = { name = item; type = selectedCat.take(20) }, modifier = Modifier.fillMaxWidth()) { Text(item, fontSize = 10.sp, color = Color.White) } } }
                }
                if (name.isNotEmpty()) Text("Selected: $name", fontSize = 11.sp, color = Color(0xFF30D158))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Equipment Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = owner, onValueChange = { owner = it }, label = { Text("Owner") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = dateOn, onValueChange = { dateOn = it }, label = { Text("Date on Site") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }},
            confirmButton = { Button(onClick = { if (name.isNotBlank()) { val n = appData.copy(); n.plantEquipment.add(PlantEquipment(id = UUID.randomUUID().toString(), name = name, type = type.ifEmpty { selectedCat }, owner = owner, dateOnSite = dateOn, status = "Working", timestamp = System.currentTimeMillis())); onUpdate(n); showAdd = false } }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))) { Text("Add") } },
            dismissButton = { Button(onClick = { showAdd = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("Cancel") } }
        )
    }

    if (showLog != null) {
        val plant = appData.plantEquipment.find { it.id == showLog } ?: return
        var hw by remember { mutableStateOf("8") }; var hi by remember { mutableStateOf("0") }; var reason by remember { mutableStateOf("") }; var fuel by remember { mutableStateOf("") }; var op by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { showLog = null }, title = { Text("Log — ${plant.name}") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = hw, onValueChange = { hw = it }, label = { Text("Hours Worked") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = hi, onValueChange = { hi = it }, label = { Text("Hours Idle") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Idle Reason") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = fuel, onValueChange = { fuel = it }, label = { Text("Fuel (Litres)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = op, onValueChange = { op = it }, label = { Text("Operator") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }},
            confirmButton = { Button(onClick = { val n = appData.copy(); n.plantDailyLogs.add(PlantDailyLog(id = UUID.randomUUID().toString(), plantId = plant.id, date = filterDate.ifEmpty { today }.take(10), hoursWorked = hw, hoursIdle = hi, idleReason = reason, fuelIssued = fuel, operatorName = op, timestamp = System.currentTimeMillis())); onUpdate(n); showLog = null }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))) { Text("Save") } },
            dismissButton = { Button(onClick = { showLog = null }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("Cancel") } }
        )
    }
}

@Composable
fun StatP(value: String, label: String, color: Color) {
    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1C1C1E), shape = MaterialTheme.shapes.small) {
        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 8.sp, color = Color.Gray)
        }
    }
}
