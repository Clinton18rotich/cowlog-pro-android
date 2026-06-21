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
import com.cowlog.pro.ui.BottomNavBar
import com.cowlog.pro.ui.TopBar
import java.text.SimpleDateFormat
import java.util.*

// RFI Categories with pre-written questions
val RFI_CATEGORIES = linkedMapOf(
    "📐 STRUCTURAL" to listOf(
        "Clarify reinforcement detail at column C__ / beam B__ junction",
        "Confirm concrete grade for ___ (currently specified as ___)",
        "Clarify foundation depth at Grid __ — drawing shows __mm but ground conditions differ",
        "Confirm size and spacing of ___ reinforcement as drawing is unclear",
        "Clarify construction joint locations for ___ slab/beam",
        "Request structural engineer's approval for proposed rebar substitution",
        "Confirm load-bearing capacity of ___ wall for mounting ___",
        "Clarify camber requirements for beam spanning ___m"
    ),
    "🏗️ ARCHITECTURAL" to listOf(
        "Clarify floor finish type for ___ room — schedule shows ___ but notes say ___",
        "Confirm ceiling height at ___ area — RCP shows ___mm but section shows ___mm",
        "Clarify wall finish for ___ area — drawing notes are unclear",
        "Request door/window schedule for ___ block",
        "Clarify colour scheme for ___ area — no specification provided",
        "Confirm tile size and pattern for ___ area",
        "Clarify skirting detail at ___ junction",
        "Request elevation detail for ___ wall"
    ),
    "🚿 PLUMBING" to listOf(
        "Clarify pipe sizing for ___ supply line — calculated demand vs drawing differ",
        "Confirm drainage invert levels at manhole ___ — conflicting information",
        "Request plumbing schematic for ___ floor",
        "Clarify water tank capacity and location — not specified on drawings",
        "Confirm sanitary ware model/specification for ___",
        "Request grease trap detail for kitchen drainage",
        "Clarify rainwater harvesting system components and sizing",
        "Confirm hot water system specification — solar vs electric"
    ),
    "⚡ ELECTRICAL" to listOf(
        "Clarify cable sizing for ___ circuit — calculated load vs schedule differ",
        "Confirm distribution board location for ___ floor",
        "Request lighting layout for ___ area — not shown on drawings",
        "Clarify emergency lighting requirements for ___",
        "Confirm socket outlet heights for ___ area",
        "Request single-line diagram for ___ panel",
        "Clarify earthing system design — rod vs grid vs mat",
        "Confirm fire alarm device locations for ___ floor"
    ),
    "🌀 MECHANICAL / HVAC" to listOf(
        "Clarify AC unit capacity for ___ room — heat load calculation needed",
        "Confirm ductwork routing through ___ corridor — space constraints",
        "Request mechanical ventilation specification for ___ area",
        "Clarify kitchen extraction hood specification and duct routing",
        "Confirm chiller plant location and capacity",
        "Request equipment schedule for ___ plant room"
    ),
    "🔥 FIRE SERVICES" to listOf(
        "Clarify sprinkler head spacing for ___ area — coverage unclear",
        "Confirm fire hose reel locations for ___ floor",
        "Request fire escape route drawing for ___ block",
        "Clarify fire door rating requirements for ___ doors",
        "Confirm fire alarm zone plan for ___ building",
        "Request cause & effect matrix for fire alarm system"
    ),
    "🧱 MATERIALS" to listOf(
        "Request approval for substitute material ___ in place of ___",
        "Clarify specification for ___ material — drawing vs specification differ",
        "Confirm source/supplier approval for ___ material",
        "Request material sample approval for ___",
        "Clarify finishing standard for ___ material — exposed/covered",
        "Confirm delivery schedule for long-lead items"
    ),
    "📋 CONTRACTUAL" to listOf(
        "Clarify scope of works for ___ — not clearly defined in contract",
        "Request clarification on variation procedure for ___",
        "Confirm interim payment valuation date and format",
        "Clarify defects liability period commencement date",
        "Request approved list of subcontractors for ___ package",
        "Confirm insurance requirements for ___ works"
    ),
    "🦺 HEALTH & SAFETY" to listOf(
        "Request site-specific H&S plan for ___ activity",
        "Clarify PPE requirements for ___ works",
        "Request risk assessment for ___ operation",
        "Confirm emergency assembly point locations",
        "Request method statement approval for ___ works"
    ),
    "📄 GENERAL" to listOf(
        "Request latest revision of drawing ___ — current version may be superseded",
        "Clarify discrepancy between drawing ___ and specification clause ___",
        "Request missing detail/section for ___",
        "Confirm site instruction SI-___ has been received and understood",
        "Request meeting to discuss ___ issue",
        "Clarify handover procedure and documentation requirements"
    )
)

@Composable
fun RFIScreen(appData: AppData, settings: ProjectSettings, navController: NavController, onUpdate: (AppData) -> Unit) {
    var showForm by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedQuestion by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = { TopBar("📤 RFIs", navController) },
        bottomBar = { BottomNavBar(navController, "rfi") },
        floatingActionButton = { FloatingActionButton(onClick = { showForm = true; selectedCategory = ""; selectedQuestion = "" }, containerColor = Color(0xFFBF5AF2)) { Text("+", fontSize = 24.sp, color = Color.White) } }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 12.dp)) {
            if (appData.rfis.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("📤", fontSize = 48.sp); Text("No RFIs", color = Color.Gray); TextButton(onClick = { showForm = true }) { Text("+ New RFI") } }
                }
            } else {
                LazyColumn {
                    items(appData.rfis.sortedByDescending { it.timestamp }) { rfi ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { navController.navigate("rfiDoc/${rfi.id}") }, colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(rfi.subject, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                    Surface(color = if (rfi.status == "open") Color(0x44FF9F0A) else Color(0x4430D158), shape = MaterialTheme.shapes.small) {
                                        Text(rfi.status.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, color = if (rfi.status == "open") Color(0xFFFF9F0A) else Color(0xFF30D158))
                                    }
                                }
                                Text("To: ${rfi.sentTo} | ${SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(rfi.timestamp))}", fontSize = 10.sp, color = Color.Gray)
                                Text(rfi.question.take(100), fontSize = 11.sp, color = Color.White)
                                if (rfi.response.isNotEmpty()) { Text("📩 ${rfi.response.take(80)}", fontSize = 10.sp, color = Color(0xFF30D158)) }
                                Text("📄 Tap to view document →", fontSize = 9.sp, color = Color(0xFF0A84FF))
                            }
                        }
                    }
                }
            }
        }
    }
    
    // RFI Form
    if (showForm) {
        var subject by remember { mutableStateOf("") }
        var question by remember { mutableStateOf("") }
        var sentTo by remember { mutableStateOf("Engineer") }
        
        AlertDialog(
            onDismissRequest = { showForm = false },
            title = { Text(if (selectedQuestion.isEmpty()) "New RFI" else selectedCategory, fontSize = 13.sp) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp).verticalScroll(rememberScrollState())) {
                    
                    if (selectedQuestion.isEmpty()) {
                        Text("1. Select Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBF5AF2))
                        RFI_CATEGORIES.keys.forEach { cat ->
                            TextButton(onClick = { selectedCategory = cat }, modifier = Modifier.fillMaxWidth()) {
                                Text(cat, fontSize = 10.sp, color = if (selectedCategory == cat) Color(0xFFBF5AF2) else Color.Gray)
                            }
                        }
                        if (selectedCategory.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("2. Select Question:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBF5AF2))
                            RFI_CATEGORIES[selectedCategory]?.forEach { q ->
                                TextButton(onClick = { selectedQuestion = q; subject = q.take(60); question = q }, modifier = Modifier.fillMaxWidth()) {
                                    Text(q, fontSize = 9.sp, color = Color.White)
                                }
                            }
                        }
                    }
                    
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = sentTo, onValueChange = { sentTo = it }, label = { Text("Sent To") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = question, onValueChange = { question = it }, label = { Text("Question *") }, modifier = Modifier.fillMaxWidth(), maxLines = 5)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (subject.isNotBlank() && question.isNotBlank()) {
                        val newData = appData.copy()
                        newData.rfis.add(RFI(id = UUID.randomUUID().toString(), subject = subject, sentTo = sentTo, question = question, status = "open", timestamp = System.currentTimeMillis()))
                        onUpdate(newData)
                        showForm = false; selectedCategory = ""; selectedQuestion = ""
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBF5AF2))) { Text("Submit RFI", color = Color.White) }
            },
            dismissButton = { Button(onClick = { showForm = false; selectedCategory = ""; selectedQuestion = "" }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("Cancel") } }
        )
    }
}
