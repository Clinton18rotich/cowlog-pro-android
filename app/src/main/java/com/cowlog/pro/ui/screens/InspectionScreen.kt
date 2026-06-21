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

// Complete inspection checklists for ALL construction phases
val INSPECTION_PHASES = linkedMapOf(
    "🚛 1. MOBILIZATION & SETUP" to mapOf(
        "Site Possession" to listOf("Site handed over by Client", "Boundaries confirmed", "Access roads OK", "Possession certificate signed", "Photos taken"),
        "Site Setup" to listOf("Site office complete", "Material storage ready", "Water connected", "Power connected", "Security in place", "First aid station ready"),
        "Hoarding & Signage" to listOf("Hoarding 2.4m high", "Stable & wind-resistant", "Project signboard installed", "Emergency contacts displayed"),
        "Survey Equipment" to listOf("Total Station calibrated", "Auto Level checked", "GPS verified", "Calibration certs filed")
    ),
    "📐 2. SETTING OUT" to mapOf(
        "Control Points" to listOf("Primary controls set", "TBM established", "Controls protected", "Cross-checked by surveyor"),
        "Grid Lines" to listOf("Grid on profiles", "Pegs at intersections", "Nails/saw cuts on profiles", "Checked against drgs"),
        "Building Corners" to listOf("Corners marked", "Offset pegs set", "Diagonals checked ±5mm", "Levels transferred"),
        "Drainage Setting Out" to listOf("Road centerline set", "Drainage alignment OK", "Invert levels on profiles", "MH positions marked")
    ),
    "🕳️ 3. EXCAVATION" to mapOf(
        "Bulk Excavation" to listOf("Depth to formation", "Bottom firm", "Safety batter 1:1", "Spoil >2m from edge", "Dewatering working"),
        "Trench — Foundations" to listOf("Width as drg", "Depth to bearing strata", "Bottom level verified", "Loose material removed"),
        "Trench — Drainage" to listOf("Gradient min 1:60", "Width correct", "Bottom trimmed", "Granular bedding 150mm"),
        "Backfill" to listOf("Selected material", "150mm layers", "Compacted >95% MDD", "No debris", "Test done"),
        "Murram Blinding" to listOf("Good quality murram", "100-150mm layers", "Compacted", "±10mm tolerance", "Surface sealed")
    ),
    "🧱 4. SUBSTRUCTURE" to mapOf(
        "Blinding" to listOf("50mm C15/20", "Level ±5mm", "Smooth surface", "Cured 24hrs"),
        "DPM" to listOf("1200 gauge", "Laps 300mm taped", "Turned up 150mm", "No punctures", "Sealed at pipes"),
        "Foundation Reinf" to listOf("Bar sizes correct", "Spacing checked", "Cover 50mm", "Chairs at 600mm", "Clean & rust-free"),
        "Starter Bars" to listOf("Position ±5mm", "Bar sizes correct", "Projection 40x bar dia", "Tied securely", "Cover maintained"),
        "Foundation Concrete" to listOf("Grade C25/30+", "Slump 50-75mm", "Compacted properly", "Level checked", "Cubes taken"),
        "Ground Beam" to listOf("Formwork level", "Reinf correct", "Cover 40mm", "Starter bars tied", "Concrete placed"),
        "GFS — Hardcore" to listOf("50-75mm stone", "Leveled at 2m grid", "Compacted 4 passes", "Dust blinded", "±10mm tolerance"),
        "GFS — DPM" to listOf("1200 gauge", "Laps 300mm taped", "Turned up 150mm", "No punctures", "Pipes sealed"),
        "GFS — Blinding" to listOf("50mm C15/20", "Level pegs set", "±5mm tolerance", "Falls to drainage", "Cured"),
        "GFS — BRC" to listOf("Mesh type correct", "Chairs @600mm", "Cover 40mm", "Laps 300mm tied", "Clean"),
        "GFS — Formwork" to listOf("Edge forms to thickness", "FFL set with water level", "Diagonals checked", "Step-downs formed", "Release agent"),
        "GFS — Concrete" to listOf("Grade C25/30", "Slump tested", "Level pins @2m", "Vibrated properly", "Beam screed", "Cubes taken"),
        "Anti-Termite" to listOf("Chemical approved", "Coverage complete", "Certificate issued", "Re-entry observed")
    ),
    "🏗️ 5. SUPERSTRUCTURE — CONCRETE" to mapOf(
        "Column Kicker" to listOf("Height 75-150mm", "Position correct", "Top roughened", "Cured"),
        "Column Reinf" to listOf("Bar sizes correct", "Links spacing OK", "Lap length correct", "Cover 25mm", "Starter bars clean"),
        "Column Shutters" to listOf("Plumb both ways", "Props at 45°", "Clamps @600mm", "Tie rods with sleeves", "Release agent", "Inspection opening"),
        "Column Concrete" to listOf("Grade confirmed", "Slump test", "Pour <1.5m/lift", "Vibrate 300mm spacing", "No segregation", "Cubes taken"),
        "Beam Soffit" to listOf("Level with water level", "Props adequate", "Camber if specified", "Clean before rebar"),
        "Beam Sides" to listOf("Plumb both sides", "String line alignment", "Clamps @600mm", "Stop-ends vertical", "Release agent"),
        "Beam Reinf" to listOf("Main bars correct", "Stirrups spacing OK", "Laps at right positions", "Cover 25mm", "Continuity through columns"),
        "Beam Concrete" to listOf("Grade confirmed", "300-400mm layers", "Vibrate @300mm spacing", "Flow under rebar", "Level top", "Cubes taken"),
        "Slab Formwork" to listOf("Decking type correct", "Props on grid", "Level at 1.5m grid", "Camber if specified", "Edge forms set", "Release agent"),
        "Slab Reinf" to listOf("Main bars correct", "Distribution bars OK", "Top mesh over supports", "Cover 20mm", "Chairs between meshes"),
        "Slab Concrete" to listOf("Grade confirmed", "Slump test", "Pour sequence", "Vibrate @400mm grid", "Beam screed", "Falls checked", "Cubes taken"),
        "Striking Time" to listOf("Sides: 24-48hrs", "Soffits: 7 days min", "Props: 14-21 days", "Back-propping installed", "No damage on removal")
    ),
    "🧱 6. WALLING" to mapOf(
        "Blockwork — GF" to listOf("Block type correct", "Mortar 1:4", "Plumb ±3mm/storey", "Level ±5mm/5m", "Bond: half-bond", "Wall ties @450mm", "DPC at base"),
        "Hoop Iron" to listOf("25mm x 1.6mm galv", "Every 3rd course", "Laps 300mm", "Clout nails at laps", "Around corners 300mm return"),
        "Wall Stiffeners" to listOf("Max 4m c/c", "Reinf tied to columns", "Blockwork toothed", "Concrete filled", "Curing"),
        "Ring Beam" to listOf("Top of wall", "Formwork level", "4Y12 + R6 stirrups", "C25/30", "Anchorage to columns", "Curing 7 days"),
        "Lintels" to listOf("Bearing 150mm each side", "Reinf as schedule", "Level & aligned", "Props until cured", "Precast: date marked"),
        "Movement Joints" to listOf("Location as drg", "Width 10-20mm", "Filler placed", "Sealant applied", "Bond breaker used")
    ),
    "🔩 7. STRUCTURAL STEEL" to mapOf(
        "Fabrication" to listOf("Members as drgs", "Welds inspected", "Dimensions checked", "Shop primer applied"),
        "Erection" to listOf("Alignment checked", "Plumb ±5mm", "Bolts torqued", "Base plates grouted", "Temp bracing"),
        "Connections" to listOf("Bolts fully tightened", "Welds: NDT arranged", "End plates full contact", "Touch-up paint on site welds"),
        "Fire Protection" to listOf("System correct", "Thickness as rating", "Application per spec", "Certificate issued")
    ),
    "🏠 8. ROOFING" to mapOf(
        "Timber Structure" to listOf("Timber treated", "Sizes correct", "Trusses braced", "Wall plate anchored @600mm", "Bracing complete"),
        "Steel Structure" to listOf("Trusses as drg", "Purlins spaced correctly", "Bracing complete", "Anchorage adequate"),
        "Underlay" to listOf("Type correct", "Laps 150mm", "Draped between rafters", "Counter battens"),
        "Tile Roofing" to listOf("Tile type correct", "Battens spacing OK", "Nailing correct", "Ridge fixed", "Valley lined"),
        "Sheet Roofing" to listOf("Sheet type correct", "Laps correct", "Fasteners with washers", "Ridge cap fixed", "Flashings done"),
        "Gutters" to listOf("Material correct", "Falls 1:120", "Brackets at spacing", "Joints sealed", "Tested with water")
    ),
    "💧 9. WATERPROOFING" to mapOf(
        "Roof WP" to listOf("Surface prepared", "Primer applied", "Membrane correct", "Laps >100mm", "Detailing at pipes", "Ponding test"),
        "Tanking" to listOf("Surface clean", "Fillets at corners", "Membrane continuous", "Protection board", "Drainage behind"),
        "Wet Areas" to listOf("Falls correct", "Primer applied", "Membrane to walls 150mm", "Corners reinforced", "Ponding test 24hrs")
    ),
    "🚿 10. PLUMBING & DRAINAGE" to mapOf(
        "Below Ground Drainage" to listOf("Pipe type correct", "Gradient min 1:60", "Bedding 150mm", "Joints tested", "Backfill selected"),
        "Manholes" to listOf("Position correct", "Benching formed", "Channels smooth", "Step irons @300mm", "Cover grade correct"),
        "Supply Pipework" to listOf("Material correct", "Sizes as design", "Clipped @1.2m", "Stop valves installed", "Hot pipes insulated"),
        "Pressure Test" to listOf("1.5x working pressure", "Held 1 hour", "No leaks", "Gauge calibrated", "Certificate signed"),
        "Sanitary Ware" to listOf("Fixtures as schedule", "Fixed securely", "Level & aligned", "Sealant applied", "Function tested")
    ),
    "⚡ 11. ELECTRICAL" to mapOf(
        "Containment" to listOf("Conduit type correct", "Route as drg", "Supports @1.2m", "Draw wire installed", "Fire barriers"),
        "Cabling" to listOf("Cable type correct", "Size as schedule", "No damage", "Labels on", "IR test before term"),
        "DB & Circuits" to listOf("DB location correct", "MCB/RCD ratings OK", "Circuit schedule done", "Earth bonding"),
        "Testing" to listOf("IR >1MΩ", "Continuity OK", "RCD trip <40ms", "Earth loop OK", "Certificate issued")
    ),
    "🎨 12. PLASTERING & SCREEDING" to mapOf(
        "Internal Plaster" to listOf("Background raked", "Mesh at joints", "Screeds plumb @1.5m", "Undercoat 1:4 10-12mm", "Finish 1:6 5-8mm", "Curing between coats"),
        "External Render" to listOf("Background dampened", "Bellcast at DPC", "Mesh at dissimilar", "Scratch coat 1:4 10mm", "Float coat 1:6 10mm", "Curing protected"),
        "Floor Screed" to listOf("Substrate primed", "DPC/DPM turned up", "Mix 1:4", "Level pegs set", "Falls to drainage", "Curing 7 days")
    ),
    "🧱 13. TILING & FINISHES" to mapOf(
        "Floor Tiling" to listOf("Tile type correct", "Adhesive OK", "Grout consistent", "Level ±3mm", "Falls to drains", "No hollow spots"),
        "Wall Tiling" to listOf("Tile type correct", "Adhesive OK", "Grout consistent", "Alignment checked", "Trim at edges"),
        "PVC Flooring" to listOf("Subfloor dry <5% MC", "Adhesive correct", "Joints welded", "Coving at walls", "No bubbles")
    ),
    "🎨 14. PAINTING" to mapOf(
        "Internal Walls" to listOf("Surface sanded", "Filler on cracks", "Primer one coat", "Undercoat one coat", "Finish correct", "No runs/sags"),
        "Internal Ceilings" to listOf("Surface primed", "Undercoat done", "Finish uniform", "No roller marks"),
        "External Painting" to listOf("Surface clean & dry", "Weather suitable", "Primer/sealer done", "Finish uniform", "No defects at 3m")
    ),
    "🚪 15. JOINERY" to mapOf(
        "Door Frames" to listOf("Size correct", "Plumb & square", "Anchored min 3/jamb", "Holdfasts in blockwork", "Fire doors labeled"),
        "Door Leaves" to listOf("Size correct", "Type as schedule", "Ironmongery fitted", "Operation smooth", "Clearances correct"),
        "Window Frames" to listOf("Size correct", "Plumb level square", "Anchored @600mm", "DPC at sill", "Weep holes clear"),
        "Glazing" to listOf("Glass type correct", "Thickness OK", "Setting blocks in", "Gasket continuous", "No cracks", "Safety glass where req"),
        "Skirting & Architraves" to listOf("Material correct", "Fixed @600mm", "Mitred corners", "Nail holes filled", "Consistent margin")
    ),
    "🏡 16. EXTERNAL WORKS" to mapOf(
        "Road Sub-base" to listOf("Subgrade compacted", "Material correct", "Thickness OK", "Compacted >95%", "Camber 2-3%", "DCP test done"),
        "Road Surfacing" to listOf("Prime coat applied", "Asphalt temp >130°C", "Thickness OK", "Rolled properly", "Joints sealed", "Core samples"),
        "Paving" to listOf("Sub-base compacted", "Bedding 25-50mm", "Pavers correct", "Pattern OK", "Joint sand filled", "Falls correct"),
        "Boundary Wall" to listOf("Foundation OK", "Blockwork as spec", "Columns at spacing", "Coping sloped", "Gate posts grouted"),
        "Landscaping" to listOf("Topsoil 150mm min", "Levels correct", "Planting done", "Irrigation working", "Mulching applied")
    ),
    "🔥 17. SERVICES COMMISSIONING" to mapOf(
        "Water Supply" to listOf("All fixtures work", "No leaks", "Pressure adequate", "Hot water OK", "Tanks filled"),
        "Drainage" to listOf("All traps primed", "Flow test OK", "No blockages", "MHs accessible", "Covers seated"),
        "Electrical" to listOf("All circuits live", "RCD tested", "Earthing verified", "All lights work", "Certificate issued"),
        "Fire System" to listOf("Alarm tested all zones", "Detectors functional", "Sounders audible", "Extinguishers in place", "Panel operational")
    ),
    "✓ 18. SNAGGING & HANDOVER" to mapOf(
        "Internal Snagging" to listOf("All rooms inspected", "Walls: no defects", "Ceilings: no stains", "Floors: no cracks", "Doors/windows: OK"),
        "External Snagging" to listOf("Roof: no leaks", "Walls: no cracks", "Paving: no settlement", "Drainage: flowing", "Gates: smooth"),
        "Cleaning" to listOf("General cleaning done", "Windows cleaned", "Sanitary polished", "Floors mopped", "Rubbish removed"),
        "Documentation" to listOf("As-built drgs received", "O&M manuals received", "Warranties collected", "Test certs filed", "Keys labeled"),
        "Handover" to listOf("Client walkthrough done", "Defects list signed", "Taking Over Certificate signed", "Keys handed over", "DLP commenced")
    )
)

@Composable
fun InspectionScreen(appData: AppData, settings: ProjectSettings, navController: NavController, onUpdate: (AppData) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    var selectedPhase by remember { mutableStateOf("") }
    var selectedChecklist by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = { TopBar("✅ Inspections", navController) },
        bottomBar = { BottomNavBar(navController, "inspections") },
        floatingActionButton = { FloatingActionButton(onClick = { showMenu = true; selectedPhase = ""; selectedChecklist = "" }, containerColor = Color(0xFF30D158)) { Text("+", fontSize = 24.sp, color = Color.White) } }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 12.dp)) {
            if (appData.inspections.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("✅", fontSize = 48.sp); Text("No inspections yet", color = Color.Gray); Text("Tap + to start", fontSize = 12.sp, color = Color.DarkGray) }
                }
            } else {
                LazyColumn {
                    items(appData.inspections.sortedByDescending { it.timestamp }) { insp ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(insp.checklistType, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Surface(color = if (insp.result == "pass") Color(0x4430D158) else Color(0x44FF453A), shape = MaterialTheme.shapes.small) {
                                        Text(insp.result.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, color = if (insp.result == "pass") Color(0xFF30D158) else Color(0xFFFF453A))
                                    }
                                }
                                Text("${insp.items.count { it.passed }}/${insp.items.size} passed | ${insp.location}", fontSize = 10.sp, color = Color.Gray)
                                Text("${SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(Date(insp.timestamp))}", fontSize = 9.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Inspection selection dialog
    if (showMenu) {
        var checks by remember { mutableStateOf(listOf<Boolean>()) }
        
        AlertDialog(
            onDismissRequest = { showMenu = false },
            title = { Text(if (selectedChecklist.isEmpty()) "Select Inspection" else selectedChecklist, fontSize = 13.sp) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp).verticalScroll(rememberScrollState())) {
                    if (selectedChecklist.isEmpty()) {
                        // Phase selection
                        INSPECTION_PHASES.keys.forEach { phase ->
                            TextButton(onClick = { selectedPhase = phase }, modifier = Modifier.fillMaxWidth()) {
                                Text(phase, fontSize = 10.sp, color = if (selectedPhase == phase) Color(0xFF30D158) else Color.Gray)
                            }
                        }
                        if (selectedPhase.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Select Checklist:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF30D158))
                            INSPECTION_PHASES[selectedPhase]?.keys?.forEach { checklist ->
                                TextButton(onClick = {
                                    selectedChecklist = checklist
                                    val items = INSPECTION_PHASES[selectedPhase]?.get(checklist) ?: emptyList()
                                    checks = items.map { true }
                                }, modifier = Modifier.fillMaxWidth()) {
                                    Text(checklist, fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    } else {
                        // Checklist items
                        val items = INSPECTION_PHASES[selectedPhase]?.get(selectedChecklist) ?: emptyList()
                        items.forEachIndexed { i, item ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Checkbox(checked = checks.getOrElse(i) { true }, onCheckedChange = { if (i < checks.size) { val new = checks.toMutableList(); new[i] = it; checks = new } })
                                Text(item, fontSize = 10.sp, color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            val items = INSPECTION_PHASES[selectedPhase]?.get(selectedChecklist) ?: emptyList()
                            val inspItems = items.mapIndexed { i, item -> ChecklistItem(label = item, passed = checks.getOrElse(i) { true }) }
                            val passed = inspItems.count { it.passed }
                            val newData = appData.copy()
                            newData.inspections.add(Inspection(id = UUID.randomUUID().toString(), checklistType = selectedChecklist, location = settings.defaultLocation, items = inspItems, result = if (passed == items.size) "pass" else "fail", timestamp = System.currentTimeMillis()))
                            onUpdate(newData)
                            showMenu = false
                            selectedPhase = ""; selectedChecklist = ""
                        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158))) {
                            Text("Save Inspection", color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showMenu = false; selectedPhase = ""; selectedChecklist = "" }) { Text("Cancel") } }
        )
    }
}
