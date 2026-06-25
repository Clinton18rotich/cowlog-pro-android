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

val MRN_CATEGORIES = linkedMapOf(
    "🧱 Cement & Binders" to listOf("Expired/old stock cement", "Lumpy/hardened cement", "Wrong type delivered (OPC vs PPC)", "Damaged bags — water ingress"),
    "🔩 Reinforcement Steel" to listOf("Wrong diameter/grade", "Excessive rust/corrosion", "Bent/buckled bars", "Wrong length delivered", "No mill certificate"),
    "🧱 Blocks & Bricks" to listOf("Cracked/broken blocks", "Wrong size/type", "Under-strength (fails test)", "Uneven edges/poor finish", "Wrong colour batch"),
    "🪨 Aggregates" to listOf("Wrong size/grading", "Contaminated with silt/clay", "Contains organic matter", "Wrong source (not approved)", "Reactive aggregates (ASR risk)"),
    "🪵 Timber" to listOf("Not treated as specified", "Wrong species/grade", "Excessive knots/warping", "Moisture content too high", "Insect/fungal attack"),
    "💧 Plumbing Materials" to listOf("Wrong pipe material/size", "Fittings not matching pipes", "Damaged/cracked pipes", "No test certificates", "Counterfeit branded items"),
    "⚡ Electrical" to listOf("Wrong cable size", "Not to KEBS standard", "Damaged insulation", "No test certificate", "Wrong voltage rating"),
    "🎨 Finishes" to listOf("Wrong colour/shade", "Damaged/opened tins", "Expired paint", "Wrong tile batch/shade", "Chipped/broken tiles"),
    "🏠 Roofing" to listOf("Wrong gauge/thickness", "Pre-rusted sheets", "Wrong profile/colour", "Damaged during transport", "Missing accessories"),
    "📦 General" to listOf("Not to approved sample", "No delivery note", "No test certificate", "Quantity short delivered", "Damaged during offloading")
)

@Composable
fun MaterialRejectionScreen(appData: AppData, settings: ProjectSettings, navController: NavController, onUpdate: (AppData) -> Unit) {
    var showForm by remember { mutableStateOf(false) }
    var selectedCat by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf("") }
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    Scaffold(
        
        
        floatingActionButton = { FloatingActionButton(onClick = { showForm = true; selectedCat = ""; selectedItem = "" }, containerColor = Color(0xFFFF453A)) { Text("+", color = Color.White) } }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 12.dp)) {
            if (appData.materialRejections.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("📦", fontSize = 48.sp); Text("No rejections", color = Color.Gray) }
                }
            } else {
                LazyColumn {
                    items(appData.materialRejections.sortedByDescending { it.timestamp }) { mr ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { navController.navigate("mrndoc/${mr.id}") }, colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))) {
                            Column(modifier = Modifier.padding(12.dp)) { Text("MRN-${mr.id.take(6)}: ${mr.material}", fontWeight = FontWeight.Bold); Text("${mr.quantityRejected} · ${mr.supplier}", fontSize = 11.sp, color = Color.Gray) }
                        }
                    }
                }
            }
        }
    }
    
    if (showForm) {
        var material by remember { mutableStateOf("") }
        var qty by remember { mutableStateOf("") }
        var supplier by remember { mutableStateOf("") }
        var reason by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showForm = false },
            title = { Text(if (selectedItem.isEmpty()) "Reject Material" else selectedItem.take(40), fontSize = 13.sp) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                    if (selectedItem.isEmpty()) {
                        Text("Select Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF453A))
                        MRN_CATEGORIES.keys.forEach { cat ->
                            TextButton(onClick = { selectedCat = cat }, modifier = Modifier.fillMaxWidth()) {
                                Text(cat, fontSize = 10.sp, color = if (selectedCat == cat) Color(0xFFFF453A) else Color.Gray)
                            }
                        }
                        if (selectedCat.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Select Reason:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF453A))
                            MRN_CATEGORIES[selectedCat]?.forEach { item ->
                                TextButton(onClick = { selectedItem = item; reason = item }, modifier = Modifier.fillMaxWidth()) {
                                    Text(item, fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }
                    OutlinedTextField(value = material, onValueChange = { material = it }, label = { Text("Material *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity Rejected") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = supplier, onValueChange = { supplier = it }, label = { Text("Supplier") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                }
            },
            confirmButton = { Button(onClick = { if (material.isNotBlank()) { val n = appData.copy(); n.materialRejections.add(MaterialRejection(id = UUID.randomUUID().toString(), date = today, material = material, quantityRejected = qty, supplier = supplier, detailedReason = reason, timestamp = System.currentTimeMillis())); onUpdate(n); showForm = false; selectedCat = ""; selectedItem = "" } }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A))) { Text("Save") } },
            dismissButton = { Button(onClick = { showForm = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("Cancel") } }
        )
    }
}
