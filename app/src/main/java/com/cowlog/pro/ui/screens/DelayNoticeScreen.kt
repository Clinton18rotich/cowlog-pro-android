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

val DELAY_CATEGORIES = linkedMapOf(
    "🌧️ Weather" to listOf("Heavy rain — work stopped", "Flooding of site", "High winds — crane operations halted", "Extreme heat — concreting suspended"),
    "🚛 Material Supply" to listOf("Late delivery of reinforcement steel", "Late delivery of cement", "Late delivery of concrete (ready mix)", "Late delivery of blocks/stone", "Material shortage in market"),
    "👷 Labour" to listOf("Labour shortage", "Labour strike/dispute", "Key personnel absent", "Subcontractor not mobilized"),
    "🔧 Equipment" to listOf("Tower crane breakdown", "Concrete pump breakdown", "Generator failure", "Excavator/plant breakdown", "Equipment not mobilized on time"),
    "📐 Design/Client" to listOf("Late design change from Engineer", "Delayed drawing approval", "Client variation order", "Late response to RFI", "Delayed inspection by Engineer"),
    "⚖️ Contractual" to listOf("Delayed interim payment", "Dispute over variation", "Delay in site possession", "Suspension of works by Engineer"),
    "🏗️ Others" to listOf("Utility relocation delay", "Archaeological discovery", "Community unrest/demonstration", "Force majeure event")
)

@Composable
fun DelayNoticeScreen(appData: AppData, settings: ProjectSettings, navController: NavController, onUpdate: (AppData) -> Unit) {
    var showForm by remember { mutableStateOf(false) }
    var selectedCat by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf("") }
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    Scaffold(
        topBar = { TopBar("🚧 Delay Notices", navController) },
        bottomBar = { BottomNavBar(navController, "delaynotices") },
        floatingActionButton = { FloatingActionButton(onClick = { showForm = true; selectedCat = ""; selectedItem = "" }, containerColor = Color(0xFFFF9F0A)) { Text("+") } }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 12.dp)) {
            if (appData.delayNotices.isEmpty() && appData.delays.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("🚧", fontSize = 48.sp); Text("No Delay Notices", color = Color.Gray) }
                }
            } else {
                LazyColumn {
                    items((appData.delayNotices + appData.delays.map { d -> DelayNotice(id = d.id, date = d.date, cause = d.cause, durationDays = d.duration, location = d.location, timestamp = d.timestamp) }).sortedByDescending { it.timestamp }) { d ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { if (d.id.startsWith("dn-")) navController.navigate("dndoc/${d.id}") }, colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))) {
                            Column(modifier = Modifier.padding(12.dp)) { Text(d.cause, fontWeight = FontWeight.Bold); Text("${d.durationDays} days · ${d.location}", fontSize = 11.sp, color = Color.Gray) }
                        }
                    }
                }
            }
        }
    }
    
    if (showForm) {
        var cause by remember { mutableStateOf("") }
        var days by remember { mutableStateOf("1") }
        var location by remember { mutableStateOf(settings.defaultLocation) }
        
        AlertDialog(
            onDismissRequest = { showForm = false },
            title = { Text(if (selectedItem.isEmpty()) "Log Delay" else selectedItem.take(40), fontSize = 13.sp) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                    if (selectedItem.isEmpty()) {
                        Text("Select Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9F0A))
                        DELAY_CATEGORIES.keys.forEach { cat ->
                            TextButton(onClick = { selectedCat = cat }, modifier = Modifier.fillMaxWidth()) {
                                Text(cat, fontSize = 10.sp, color = if (selectedCat == cat) Color(0xFFFF9F0A) else Color.Gray)
                            }
                        }
                        if (selectedCat.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Select Cause:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9F0A))
                            DELAY_CATEGORIES[selectedCat]?.forEach { item ->
                                TextButton(onClick = { selectedItem = item; cause = item }, modifier = Modifier.fillMaxWidth()) {
                                    Text(item, fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }
                    OutlinedTextField(value = cause, onValueChange = { cause = it }, label = { Text("Cause *") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)
                    OutlinedTextField(value = days, onValueChange = { days = it }, label = { Text("Duration (Days)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
            },
            confirmButton = { Button(onClick = { if (cause.isNotBlank()) { val n = appData.copy(); n.delayNotices.add(DelayNotice(id = "dn-${UUID.randomUUID()}", date = today, cause = cause, durationDays = days, location = location, timestamp = System.currentTimeMillis())); onUpdate(n); showForm = false; selectedCat = ""; selectedItem = "" } }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9F0A))) { Text("Save") } },
            dismissButton = { Button(onClick = { showForm = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("Cancel") } }
        )
    }
}
