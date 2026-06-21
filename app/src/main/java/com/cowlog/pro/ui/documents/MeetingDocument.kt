package com.cowlog.pro.ui.documents

import android.content.Context; import android.content.Intent
import android.graphics.*; import android.graphics.pdf.PdfDocument
import android.os.Environment; import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment; import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color; import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle; import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign; import androidx.compose.ui.unit.dp; import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cowlog.pro.data.*
import java.io.File; import java.io.FileOutputStream
import java.text.SimpleDateFormat; import java.util.*

@Composable
fun MeetingDocument(appData: AppData, meetingId: String, navController: NavController) {
    val m = appData.meetings.find { it.id == meetingId } ?: return
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("cowlog", Context.MODE_PRIVATE)
    val settings = remember {
        val json = prefs.getString("settings", null)
        if (json != null) try { com.google.gson.Gson().fromJson(json, ProjectSettings::class.java) } catch (_: Exception) { ProjectSettings() } else ProjectSettings()
    }
    var edAtt by remember { mutableStateOf(m.attendees) }; var edProg by remember { mutableStateOf(m.progressUpdate) }
    var edQual by remember { mutableStateOf(m.qualityReview) }; var edAct by remember { mutableStateOf(m.actionItems) }
    var edNext by remember { mutableStateOf(m.nextMeetingDate) }; var edCow by remember { mutableStateOf(settings.cowName) }
    val spmNo = "SPM/${m.id.take(6)}/${Calendar.getInstance().get(Calendar.YEAR)}"
    
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Button(onClick = { navController.popBackStack() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("←") }
            Button(onClick = { generateMeetingPDF(context, m, settings, edAtt, edProg, edQual, edAct, edNext, edCow); Toast.makeText(context, "PDF saved", Toast.LENGTH_SHORT).show() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))) { Text("📄") }
        }
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(8.dp)) {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = MaterialTheme.shapes.medium, shadowElevation = 8.dp) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1A1A2E)) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SITE PROGRESS MEETING MINUTES", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF78166), letterSpacing = 1.sp, textAlign = TextAlign.Center)
                            Text(settings.projectName.ifEmpty { "Project" }, fontSize = 8.sp, color = Color(0xFFA0A0B8))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    KenDocField("MEETING NO:", spmNo); KenDocField("DATE:", m.date); KenDocField("VENUE:", m.venue)
                    KenDocField("PROJECT:", settings.projectName); KenDocField("CONTRACTOR:", settings.contractorName)
                    KenDocSection("ATTENDANCE:", edAtt) { edAtt = it }
                    KenDocSection("PROGRESS REVIEW:", edProg) { edProg = it }
                    KenDocSection("QUALITY & INSPECTIONS:", edQual) { edQual = it }
                    KenDocSection("ACTION ITEMS:", edAct) { edAct = it }
                    KenDocSection("NEXT MEETING:", edNext) { edNext = it }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        KenSig(edCow, { edCow = it }, "Clerk of Works", Modifier.weight(1f))
                        KenSig("................", {}, "Resident Engineer", Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

fun generateMeetingPDF(ctx: Context, m: MeetingMinutes, s: ProjectSettings, att: String, prog: String, qual: String, act: String, next: String, cow: String) {
    val pdf = PdfDocument()
    val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
    val c = page.canvas
    c.drawRect(0f, 0f, 595f, 55f, Paint().apply { color = android.graphics.Color.parseColor("#1A1A2E") })
    val tp = Paint().apply { textSize = 14f; typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD); textAlign = Paint.Align.CENTER; color = android.graphics.Color.parseColor("#F78166") }
    val hp = Paint().apply { textSize = 9f; typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD) }
    val np = Paint().apply { textSize = 7f; typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL) }
    var y = 22f; c.drawText("SITE PROGRESS MEETING MINUTES", 297f, y, tp); y += 35
    fun line(l: String, v: String) { c.drawText("$l $v", 40f, y, hp); y += 12 }
    line("MEETING NO:", "SPM/${m.id.take(6)}/${Calendar.getInstance().get(Calendar.YEAR)}")
    line("DATE:", m.date); line("VENUE:", m.venue); line("PROJECT:", s.projectName)
    line("ATTENDEES:", att); line("PROGRESS:", prog); line("ACTIONS:", act)
    c.drawText("COW: $cow     RE: ________________     CONTRACTOR: ________________", 40f, y + 12, np)
    pdf.finishPage(page)
    val f = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SPM-${m.id.take(6)}.pdf")
    pdf.writeTo(FileOutputStream(f)); pdf.close()
}
