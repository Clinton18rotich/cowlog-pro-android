package com.cowlog.pro.ui.screens

import androidx.compose.foundation.clickable
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
import com.cowlog.pro.ui.components.DateFilterBar
import java.text.SimpleDateFormat
import java.util.*

// NCR Categories — linked to construction phases
val NCR_CATEGORIES = linkedMapOf(
    "🕳️ Excavation & Earthworks" to listOf("Excavation too deep/shallow", "Wrong trench width", "Unsafe batter/slope", "Poor backfill compaction", "Murram quality poor"),
    "🧱 Substructure" to listOf("Blinding too thin/uneven", "DPM punctured/not lapped", "Wrong rebar size/spacing", "Insufficient cover", "Honeycomb in foundation", "Starter bars misplaced", "GFS level wrong", "BRC not chaired", "Formwork moved during pour"),
    "🏗️ Concrete Frame" to listOf("Honeycomb in column/beam/slab", "Wrong concrete grade used", "Insufficient vibration", "Formwork not plumb", "Rebar spacing wrong", "Cover insufficient", "Cold joint formed", "Curing not done", "Striking too early", "Cracks in concrete"),
    "🧱 Walling" to listOf("Wrong mortar mix ratio", "Blockwork not plumb", "Hoop iron missing/misplaced", "Wall ties not installed", "DPC missing/damaged", "Ring beam reinforcement wrong", "Lintels not bearing properly", "Movement joints not formed"),
    "🔩 Steel Structure" to listOf("Wrong steel grade", "Bolts not torqued", "Welds defective", "Alignment out of tolerance", "Base plates not grouted", "Fire protection missing"),
    "🏠 Roofing" to listOf("Underlay not lapped", "Wrong tile/sheet type", "Insufficient fixings", "Ridge not sealed", "Gutters not falling", "Flashings missing/loose"),
    "💧 Waterproofing" to listOf("Surface not prepared", "Primer not applied", "Membrane not lapped", "Ponding test failed", "Detailing at pipes wrong", "Protection board missing"),
    "🚿 Plumbing" to listOf("Wrong pipe material/size", "Gradient insufficient", "Joints leaking", "Pressure test failed", "Sanitary ware not level", "No water bar at joints"),
    "⚡ Electrical" to listOf("Wrong cable size", "Conduit not supported", "No fire barriers", "IR test failed", "RCD not tripping", "Circuit schedule wrong"),
    "🎨 Finishes" to listOf("Plaster cracking/hollow", "Tiles drummy/uneven", "Paint runs/sags", "Wrong paint colour/sheen", "Floor screed cracked", "Skirting gaps"),
    "🚪 Joinery" to listOf("Door/window not square", "Wrong size installed", "Ironmongery missing/wrong", "Glazing cracked", "Gasket not continuous", "Operation stiff"),
    "🏡 External Works" to listOf("Road levels wrong", "Compaction insufficient", "Paving uneven", "Boundary wall cracking", "Drainage not connected"),
    "✓ Snagging" to listOf("Incomplete work", "Defective work", "Missing item", "Not to specification", "Cleaning not done", "Documentation missing"),
    "⚠️ H&S" to listOf("PPE not worn", "Scaffolding unsafe", "No first aid", "Fire extinguisher missing", "Unsafe excavation", "No safety signage"),
    "📋 General" to listOf("Not to specification", "Not to drawing", "Poor workmanship", "Delayed work", "Unapproved material used", "Work without approval")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NCRScreen(appData: AppData, settings: ProjectSettings, navController: NavController, onUpdate: (AppData) -> Unit) {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var filterDate by remember { mutableStateOf("") }
    val displayNCRs = if (filterDate.length >= 10) appData.ncrs.filter { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp)) == filterDate.take(10) } else appData.ncrs
    
    var showForm by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedNCR by remember { mutableStateOf("") }

    Scaffold(
        
        
        floatingActionButton = { FloatingActionButton(onClick = { showForm = true; selectedCategory = ""; selectedNCR = "" }, containerColor = Color(0xFFFF453A)) { Text("+", fontSize = 24.sp, color = Color.White) } }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 12.dp)) {
            DateFilterBar(onToday = { filterDate = today }, onAll = { filterDate = "" })
            if (filterDate.isNotEmpty()) Text("${displayNCRs.size} NCRs on ${filterDate.take(10)}", fontSize = 10.sp, color = Color(0xFF0A84FF), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
            
            if (displayNCRs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("🚨", fontSize = 48.sp); Text("No NCRs raised", color = Color.Gray) }
                }
            } else {
                LazyColumn {
                    items(displayNCRs.sortedByDescending { it.timestamp }) { ncr ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { navController.navigate("ncrDoc/${ncr.id}") }, colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("NCR-${ncr.id.take(6)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Surface(color = if (ncr.status == "open") Color(0x44FF453A) else Color(0x4430D158), shape = MaterialTheme.shapes.small) {
                                        Text(ncr.status.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, color = if (ncr.status == "open") Color(0xFFFF453A) else Color(0xFF30D158))
                                    }
                                }
                                Text(ncr.title, fontSize = 12.sp)
                                Text("${ncr.location} · ${ncr.severity} · ${SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(ncr.timestamp))}", fontSize = 10.sp, color = Color.Gray)
                                Text("📄 Tap to view document →", fontSize = 9.sp, color = Color(0xFF0A84FF))
                            TextButton(onClick = { val newData = appData.copy(); newData.ncrs.removeAll { it.id == ncr.id }; onUpdate(newData) }) { Text("🗑️ Delete", fontSize = 9.sp, color = Color(0xFFFF453A)) }
                            }
                        }
                    }
                }
            }
        }
    }

    // NCR Form
    if (showForm) {
        var title by remember { mutableStateOf("") }
        var location by remember { mutableStateOf(settings.defaultLocation) }
        var severity by remember { mutableStateOf("Major") }
        var desc by remember { mutableStateOf("") }
        var action by remember { mutableStateOf("") }
        var responsible by remember { mutableStateOf(settings.contractorName) }

        AlertDialog(
            onDismissRequest = { showForm = false },
            title = { Text(if (selectedNCR.isEmpty()) "Raise NCR" else selectedNCR, fontSize = 13.sp) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp).verticalScroll(rememberScrollState())) {
                    if (selectedNCR.isEmpty()) {
                        Text("1. Select Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF453A))
                        NCR_CATEGORIES.keys.forEach { cat ->
                            TextButton(onClick = { selectedCategory = cat }, modifier = Modifier.fillMaxWidth()) {
                                Text(cat, fontSize = 10.sp, color = if (selectedCategory == cat) Color(0xFFFF453A) else Color.Gray)
                            }
                        }
                        if (selectedCategory.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("2. Select NCR Type:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF453A))
                            NCR_CATEGORIES[selectedCategory]?.forEach { ncrType ->
                                TextButton(onClick = { selectedNCR = ncrType; title = ncrType }, modifier = Modifier.fillMaxWidth()) {
                                    Text(ncrType, fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf("Minor", "Major", "Critical").forEach { s ->
                            FilterChip(selected = severity == s, onClick = { severity = s }, label = { Text(s, fontSize = 10.sp, color = if (severity == s) Color.White else Color.Gray) })
                        }
                    }
                    OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = responsible, onValueChange = { responsible = it }, label = { Text("Responsible Party") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description *") }, modifier = Modifier.fillMaxWidth(), maxLines = 4)
                    OutlinedTextField(value = action, onValueChange = { action = it }, label = { Text("Required Action *") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank() && desc.isNotBlank() && action.isNotBlank()) {
                        val newData = appData.copy()
                        newData.ncrs.add(NCR(id = UUID.randomUUID().toString(), title = title, severity = severity, location = location, responsibleParty = responsible, description = desc, actionRequired = action, status = "open", timestamp = System.currentTimeMillis()))
                        onUpdate(newData)
                        showForm = false; selectedCategory = ""; selectedNCR = ""
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A))) { Text("Raise NCR", color = Color.White) }
            },
            dismissButton = { Button(onClick = { showForm = false; selectedCategory = ""; selectedNCR = "" }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("Cancel") } }
        )
    }
}
