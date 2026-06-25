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

// SI Categories with pre-written instructions
val SI_CATEGORIES = linkedMapOf(
    "🛑 STOP WORK" to listOf(
        "Stop all work immediately — unsafe conditions observed",
        "Stop excavation — unsafe batter/slope, risk of collapse",
        "Stop concreting — wrong grade/ mix being used",
        "Stop blockwork — mortar mix ratio incorrect",
        "Stop roofing — unsafe working at height",
        "Stop electrical work — unsafe live connections"
    ),
    "⚠️ RECTIFY DEFECTIVE WORK" to listOf(
        "Rectify honeycomb on column/beam/slab — chip out & re-cast with grout",
        "Rectify cracked blockwork — take down & re-build affected area",
        "Rectify hollow plaster — hack off & re-plaster",
        "Rectify drummy tiles — remove & re-fix with correct adhesive",
        "Rectify leaking pipe joint — undo & re-seal properly",
        "Rectify uneven floor screed — grind down / re-screed",
        "Rectify paint runs/sags — sand & re-paint",
        "Rectify misaligned door/window — remove & re-install plumb"
    ),
    "📋 COMPLY WITH SPECIFICATION" to listOf(
        "Use specified mortar mix 1:4 (cement:sand) — not current mix",
        "Use specified concrete grade C__/__ — not what is being used",
        "Install hoop iron every 3rd course as per specification",
        "Provide specified cover to reinforcement — currently insufficient",
        "Use approved materials only — current material not approved",
        "Follow approved method statement — currently deviating",
        "Install wall ties at specified spacing 450mm c/c",
        "Apply curing as specified — 7 days minimum"
    ),
    "🚀 ACCELERATE PROGRESS" to listOf(
        "Increase labour on site to meet programme deadlines",
        "Deploy additional equipment to accelerate works",
        "Work extended hours (subject to approval) to recover delay",
        "Increase formwork/shuttering to accelerate concrete cycle",
        "Submit recovery programme within 48 hours",
        "Open additional work fronts to improve progress"
    ),
    "🧱 MATERIALS" to listOf(
        "Remove rejected materials from site immediately",
        "Submit material samples for approval before bulk delivery",
        "Provide test certificates for delivered materials",
        "Store materials properly — currently exposed to weather",
        "Cease using expired/out-of-date materials",
        "Replace substandard materials with approved equivalents"
    ),
    "🦺 HEALTH & SAFETY" to listOf(
        "All personnel must wear mandatory PPE (helmet, boots, vest)",
        "Install guardrails & toe boards on all scaffolding immediately",
        "Provide safe access/egress to excavation — ladders required",
        "Secure site perimeter — unauthorized access observed",
        "Provide first aid kit & trained first aider on site",
        "Remove debris & tripping hazards from work areas",
        "Display safety signage at all required locations",
        "Conduct toolbox talk before work commences tomorrow"
    ),
    "📐 SETTING OUT / QUALITY" to listOf(
        "Re-check setting out before proceeding — discrepancies noted",
        "Verify levels with approved TBM — current levels appear incorrect",
        "Provide surveyor's report for completed setting out",
        "Carry out joint inspection before next pour",
        "Submit quality control records for completed works",
        "Engage approved testing laboratory for required tests"
    ),
    "📄 DOCUMENTATION" to listOf(
        "Submit method statement for approval before commencing work",
        "Provide shop drawings for review & approval",
        "Submit material approval requests with samples",
        "Provide as-built drawings for completed sections",
        "Submit test results & certificates within 24 hours",
        "Maintain up-to-date site diary & records"
    ),
    "💧 WATERPROOFING" to listOf(
        "Apply primer before membrane — currently skipped",
        "Lap membrane minimum 100mm at all joints",
        "Provide fillets at all internal corners before waterproofing",
        "Carry out ponding test for minimum 24 hours before covering",
        "Protect waterproofing membrane with protection board immediately",
        "Seal all penetrations through waterproofing with approved detail"
    ),
    "🚿 PLUMBING & DRAINAGE" to listOf(
        "Check pipe gradient — minimum 1:60 for 100mm pipe",
        "Pressure test all supply pipework at 1.5x working pressure",
        "Install vent pipes as per approved plumbing drawings",
        "Provide rodding eyes at all changes of direction",
        "Water test below-ground drainage before backfilling",
        "Install water bars at all construction joints in water-retaining structures"
    ),
    "⚡ ELECTRICAL" to listOf(
        "Install fire barriers where conduits pass through compartment walls",
        "Label all cables at both ends before termination",
        "Carry out insulation resistance test before energizing",
        "Install RCD protection on all socket circuits (30mA)",
        "Earth bond all metallic services as per regulations",
        "Provide circuit schedule inside distribution board"
    ),
    "🔩 STRUCTURAL STEEL" to listOf(
        "Torque all bolts to specified values & mark as tightened",
        "Carry out NDT on site welds as specified",
        "Apply touch-up paint to all site welds & damaged areas",
        "Grout under base plates within 24 hours of erection",
        "Provide temporary bracing until structure is fully connected",
        "Submit steel erection method statement before commencement"
    ),
    "🏠 ROOFING" to listOf(
        "Install underlay with correct laps (150mm min) before tiling",
        "Fix ridge tiles mechanically — not just mortar bedded",
        "Install fascia & soffit boards before roof covering",
        "Provide fall arrest/edge protection for roofing work",
        "Test gutters & downpipes with water before handover",
        "Install flashings at all roof/wall junctions"
    ),
    "🎨 FINISHES" to listOf(
        "Apply primer/undercoat before finish coat — currently skipped",
        "Sand between coats for smooth finish",
        "Protect finished surfaces from damage by following trades",
        "Use approved paint colour & sheen as per schedule",
        "Clean surfaces thoroughly before painting — dust & dirt present",
        "Apply minimum number of coats as per specification"
    ),
    "🚪 JOINERY" to listOf(
        "Check door frame dimensions against schedule before installation",
        "Install ironmongery as per approved schedule — not substitutes",
        "Fire doors must be installed with approved hinges & seals",
        "Glazing must be safety glass where required by regulations",
        "Window frames must be installed with DPC at sill",
        "All joinery to be protected from weather until building is watertight"
    ),
    "🏡 EXTERNAL WORKS" to listOf(
        "Compact road sub-base to minimum 95% MDD before next layer",
        "Maintain correct camber/crossfall on road surfaces",
        "Install kerbs before laying asphalt/wearing course",
        "Backfill service trenches with approved material in layers",
        "Construct boundary wall to approved design & height",
        "Provide adequate drainage behind retaining walls"
    ),
    "🏗️ GENERAL" to listOf(
        "Clean site and remove all debris before weekend/holiday",
        "Protect completed works from damage by following trades",
        "Coordinate with other contractors to avoid clashes",
        "Secure site against theft — materials going missing",
        "Provide adequate weather protection for ongoing works",
        "Display project signboard with current information",
        "Attend site meeting on specified date & time"
    )
)

@Composable
fun SIScreen(appData: AppData, settings: ProjectSettings, navController: NavController, onUpdate: (AppData) -> Unit) {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var filterDate by remember { mutableStateOf("") }
    val displaySIs = if (filterDate.length >= 10) appData.instructions.filter { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp)) == filterDate.take(10) } else appData.instructions

    var showForm by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedInstruction by remember { mutableStateOf("") }

    Scaffold(
        
        
        floatingActionButton = { FloatingActionButton(onClick = { showForm = true; selectedCategory = ""; selectedInstruction = "" }, containerColor = Color(0xFFFF9500)) { Text("+", fontSize = 24.sp, color = Color.White) } }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 12.dp)) {
            DateFilterBar(onToday = { filterDate = today }, onAll = { filterDate = "" })
            if (filterDate.isNotEmpty()) Text("${displaySIs.size} SIs on ${filterDate.take(10)}", fontSize = 10.sp, color = Color(0xFF0A84FF), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))

            if (displaySIs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("📝", fontSize = 48.sp); Text("No Site Instructions", color = Color.Gray) }
                }
            } else {
                LazyColumn {
                    items(displaySIs.sortedByDescending { it.timestamp }) { si ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { navController.navigate("siDoc/${si.id}") }, colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("SI-${si.id.take(6)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Surface(color = if (si.status == "open") Color(0x44FF9500) else Color(0x4430D158), shape = MaterialTheme.shapes.small) {
                                        Text(si.status.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, color = if (si.status == "open") Color(0xFFFF9500) else Color(0xFF30D158))
                                    }
                                }
                                Text(si.description.take(80), fontSize = 11.sp, color = Color.White)
                                Text("To: ${si.issuedTo} · ${SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(si.timestamp))}", fontSize = 10.sp, color = Color.Gray)
                                Text("📄 Tap to view document →", fontSize = 9.sp, color = Color(0xFF0A84FF))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        var desc by remember { mutableStateOf("") }
        var issuedTo by remember { mutableStateOf(settings.contractorName) }
        var loc by remember { mutableStateOf(settings.defaultLocation) }

        AlertDialog(
            onDismissRequest = { showForm = false },
            title = { Text(if (selectedInstruction.isEmpty()) "Issue SI" else selectedInstruction, fontSize = 13.sp) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp).verticalScroll(rememberScrollState())) {
                    if (selectedInstruction.isEmpty()) {
                        SI_CATEGORIES.keys.forEach { cat ->
                            TextButton(onClick = { selectedCategory = cat }, modifier = Modifier.fillMaxWidth()) {
                                Text(cat, fontSize = 10.sp, color = if (selectedCategory == cat) Color(0xFFFF9500) else Color.Gray)
                            }
                        }
                        if (selectedCategory.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            SI_CATEGORIES[selectedCategory]?.forEach { inst ->
                                TextButton(onClick = { selectedInstruction = inst; desc = inst }, modifier = Modifier.fillMaxWidth()) {
                                    Text(inst, fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description *") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                    OutlinedTextField(value = issuedTo, onValueChange = { issuedTo = it }, label = { Text("Issued To") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = loc, onValueChange = { loc = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (desc.isNotBlank()) {
                        val newData = appData.copy()
                        newData.instructions.add(SiteInstruction(id = UUID.randomUUID().toString(), description = desc, issuedTo = issuedTo, location = loc, status = "open", timestamp = System.currentTimeMillis()))
                        onUpdate(newData)
                        showForm = false; selectedCategory = ""; selectedInstruction = ""
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9500))) { Text("Issue SI", color = Color.White) }
            },
            dismissButton = { Button(onClick = { showForm = false; selectedCategory = ""; selectedInstruction = "" }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("Cancel") } }
        )
    }
}
