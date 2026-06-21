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

val MEETING_AGENDA = listOf(
    "Confirmation of Previous Minutes",
    "Progress Review — Current Status",
    "Quality & Inspections — NCRs, SIs, Tests",
    "Materials & Deliveries — Status & Lead Times",
    "Plant & Equipment — Availability & Breakdowns",
    "Health & Safety — Incidents, PPE, Audits",
    "Variations & Claims — Status",
    "Programme — Look-ahead, Delays, Recovery",
    "Subcontractors — Performance & Issues",
    "Information Required — RFIs, Drawings",
    "Any Other Business",
    "Date of Next Meeting"
)

@Composable
fun MeetingScreen(appData: AppData, settings: ProjectSettings, navController: NavController, onUpdate: (AppData) -> Unit) {
    var showForm by remember { mutableStateOf(false) }
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    Scaffold(
        topBar = { TopBar("📝 Meeting Minutes", navController) },
        bottomBar = { BottomNavBar(navController, "meetings") },
        floatingActionButton = { FloatingActionButton(onClick = { showForm = true }, containerColor = Color(0xFF0A84FF)) { Text("+", color = Color.White) } }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 12.dp)) {
            if (appData.meetings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("📝", fontSize = 48.sp); Text("No meeting minutes", color = Color.Gray) }
                }
            } else {
                LazyColumn {
                    items(appData.meetings.sortedByDescending { it.timestamp }) { m ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { navController.navigate("meetingdoc/${m.id}") }, colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))) {
                            Column(modifier = Modifier.padding(12.dp)) { Text("SPM-${m.id.take(6)}", fontWeight = FontWeight.Bold); Text("${m.venue} · ${m.date}", fontSize = 11.sp, color = Color.Gray) }
                        }
                    }
                }
            }
        }
    }
    
    if (showForm) {
        var venue by remember { mutableStateOf("Site Office") }
        var attendees by remember { mutableStateOf("") }
        var progress by remember { mutableStateOf("") }
        var quality by remember { mutableStateOf("") }
        var actions by remember { mutableStateOf("") }
        var nextDate by remember { mutableStateOf("") }
        var selectedAgenda by remember { mutableStateOf<List<String>>(emptyList()) }
        
        AlertDialog(
            onDismissRequest = { showForm = false },
            title = { Text("New Meeting Minutes") },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp).verticalScroll(rememberScrollState())) {
                    Text("Agenda Items (tap to select):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0A84FF))
                    MEETING_AGENDA.forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Checkbox(checked = selectedAgenda.contains(item), onCheckedChange = { if (it) selectedAgenda = selectedAgenda + item else selectedAgenda = selectedAgenda - item })
                            Text(item, fontSize = 10.sp, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = venue, onValueChange = { venue = it }, label = { Text("Venue") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = attendees, onValueChange = { attendees = it }, label = { Text("Attendees") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                    OutlinedTextField(value = progress, onValueChange = { progress = it }, label = { Text("Progress Update") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                    OutlinedTextField(value = quality, onValueChange = { quality = it }, label = { Text("Quality Review") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)
                    OutlinedTextField(value = actions, onValueChange = { actions = it }, label = { Text("Action Items") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                    OutlinedTextField(value = nextDate, onValueChange = { nextDate = it }, label = { Text("Next Meeting Date") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
            },
            confirmButton = { Button(onClick = { val n = appData.copy(); n.meetings.add(MeetingMinutes(id = UUID.randomUUID().toString(), date = today, venue = venue, attendees = attendees, progressUpdate = "$progress\nAgenda: ${selectedAgenda.joinToString()}", qualityReview = quality, actionItems = actions, nextMeetingDate = nextDate, timestamp = System.currentTimeMillis())); onUpdate(n); showForm = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))) { Text("Save") } },
            dismissButton = { Button(onClick = { showForm = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("Cancel") } }
        )
    }
}
