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
import java.text.SimpleDateFormat
import java.util.*

val SWO_CATEGORIES = linkedMapOf(
    "🛑 SAFETY — Immediate Danger" to listOf(
        "Unsafe scaffolding — missing guardrails/toe boards",
        "Excavation collapse risk — no shoring/battering",
        "Live electrical hazard — exposed wires",
        "Working at height without fall protection",
        "Unsafe crane/ lifting operation",
        "Confined space without permit"
    ),
    "⚠️ QUALITY — Major Defect" to listOf(
        "Structural failure risk — cracks/deformation observed",
        "Wrong concrete grade used — strength compromised",
        "Major honeycombing in structural element",
        "Foundation bearing capacity inadequate",
        "Reinforcement not as per design",
        "Waterproofing failure — active leak"
    ),
    "📋 NON-COMPLIANCE" to listOf(
        "Working without approved drawings",
        "Using unapproved materials",
        "Deviation from approved method statement",
        "Subcontractor not approved",
        "Works outside approved scope"
    ),
    "🚧 ENVIRONMENTAL" to listOf(
        "Pollution incident — spillage/discharge",
        "Dust/noise beyond permitted levels",
        "Damage to protected trees/vegetation",
        "Unauthorized waste disposal"
    ),
    "🏗️ GENERAL" to listOf(
        "Work being done in unsafe manner",
        "Site security compromised",
        "Unauthorized access to site",
        "Failure to comply with previous instructions"
    )
)

@Composable
fun StopWorkScreen(appData: AppData, settings: ProjectSettings, navController: NavController, onUpdate: (AppData) -> Unit) {
    var showForm by remember { mutableStateOf(false) }
    var selectedCat by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf("") }
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    Scaffold(
        
        
        floatingActionButton = { FloatingActionButton(onClick = { showForm = true; selectedCat = ""; selectedItem = "" }, containerColor = Color(0xFFFF453A)) { Text("+", color = Color.White) } }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 12.dp)) {
            if (appData.stopWorkOrders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("🛑", fontSize = 48.sp); Text("No Stop Work Orders", color = Color.Gray) }
                }
            } else {
                LazyColumn {
                    items(appData.stopWorkOrders.sortedByDescending { it.timestamp }) { swo ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { navController.navigate("swodoc/${swo.id}") }, colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("SWO-${swo.id.take(6)}", fontWeight = FontWeight.Bold, color = Color(0xFFFF453A))
                                Text(swo.reason.take(80), fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showForm) {
        var reason by remember { mutableStateOf("") }
        var location by remember { mutableStateOf(settings.defaultLocation) }
        var conditions by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showForm = false },
            title = { Text(if (selectedItem.isEmpty()) "Issue Stop Work Order" else selectedItem.take(40), fontSize = 13.sp) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp).verticalScroll(rememberScrollState())) {
                    if (selectedItem.isEmpty()) {
                        Text("Select Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF453A))
                        SWO_CATEGORIES.keys.forEach { cat ->
                            TextButton(onClick = { selectedCat = cat }, modifier = Modifier.fillMaxWidth()) {
                                Text(cat, fontSize = 10.sp, color = if (selectedCat == cat) Color(0xFFFF453A) else Color.Gray)
                            }
                        }
                        if (selectedCat.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Select Reason:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF453A))
                            SWO_CATEGORIES[selectedCat]?.forEach { item ->
                                TextButton(onClick = { selectedItem = item; reason = item }, modifier = Modifier.fillMaxWidth()) {
                                    Text(item, fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }
                    OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason *") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                    OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = conditions, onValueChange = { conditions = it }, label = { Text("Conditions for Resumption") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                }
            },
            confirmButton = { Button(onClick = { if (reason.isNotBlank()) { val n = appData.copy(); n.stopWorkOrders.add(StopWorkOrder(id = UUID.randomUUID().toString(), date = today, reason = reason, location = location, resumptionConditions = conditions, timestamp = System.currentTimeMillis())); onUpdate(n); showForm = false; selectedCat = ""; selectedItem = "" } }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A))) { Text("Issue Order") } },
            dismissButton = { Button(onClick = { showForm = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("Cancel") } }
        )
    }
}
