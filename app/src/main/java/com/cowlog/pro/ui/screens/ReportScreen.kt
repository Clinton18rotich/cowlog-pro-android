package com.cowlog.pro.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cowlog.pro.data.*
import com.cowlog.pro.ui.BottomNavBar
import com.cowlog.pro.ui.TopBar
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportScreen(appData: AppData, settings: ProjectSettings, navController: NavController, onUpdate: (AppData) -> Unit) {
    val context = LocalContext.current
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val dt = appData.diary.filter { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp)) == today }
    val it = appData.inspections.filter { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp)) == today }
    val no = appData.ncrs.filter { it.status == "open" }
    val si = appData.instructions.filter { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp)) == today }
    val at = appData.attendance.filter { it.date == today }
    val lowMat = appData.materials.filter { it.currentStock <= it.minStock && it.minStock > 0 }
    val inspRate = if (it.isNotEmpty()) (it.count { i -> i.result == "pass" } * 100 / it.size) else 100
    val totalWorkers = at.sumOf { (it.count.toIntOrNull() ?: 0) }
    val rn = "COW/${settings.reportCounter}/${Calendar.getInstance().get(Calendar.YEAR)}"
    val rdate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Editable states
    var edProject by remember { mutableStateOf(settings.projectName) }
    var edContractor by remember { mutableStateOf(settings.contractorName) }
    var edContractNo by remember { mutableStateOf(settings.contractNo) }
    var edCow by remember { mutableStateOf(settings.cowName) }
    var edRemarks by remember { mutableStateOf("Work is progressing in accordance with the approved programme and specifications unless otherwise noted. Contractor is reminded to maintain quality standards and adhere to all safety requirements.") }
    var edHnS by remember { mutableStateOf("No specific H&S issues noted. Site appeared orderly. All personnel observed wearing mandatory PPE.") }

    val shareText = buildString {
        append("*CLERK OF WORKS — DAILY SITE REPORT*\n")
        append("══════════════════════════════\n\n")
        append("Report: $rn\nDate: $rdate\nProject: $edProject\n\n")
        append("Activities: ${dt.size} | Inspections: ${it.size} | Open NCRs: ${no.size} | Workers: $totalWorkers\n")
        append("\n— CoW Log Pro")
    }

    Scaffold(
        topBar = { TopBar("📄 Daily Report", navController) },
        bottomBar = { BottomNavBar(navController, "report") }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            // Action buttons
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(onClick = {
                    val pdfFile = generateFullKenyanPDF(context, appData, settings, edCow, edRemarks)
                    Toast.makeText(context, "PDF saved to Downloads", Toast.LENGTH_SHORT).show()
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))) { Text("📄 PDF", fontSize = 9.sp) }

                Button(onClick = {
                    val pdfFile = generateFullKenyanPDF(context, appData, settings, edCow, edRemarks)
                    val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, uri); putExtra(Intent.EXTRA_TEXT, shareText)
                        setPackage("com.whatsapp"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    try { context.startActivity(intent) } catch (e: Exception) { val i = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, shareText) }; context.startActivity(Intent.createChooser(i, "Share")) }
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))) { Text("📱", fontSize = 9.sp) }

                Button(onClick = {
                    val pdfFile = generateFullKenyanPDF(context, appData, settings, edCow, edRemarks)
                    val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = android.net.Uri.parse("mailto:"); putExtra(Intent.EXTRA_SUBJECT, "CoW Report — $rdate")
                        putExtra(Intent.EXTRA_TEXT, shareText); putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    try { context.startActivity(intent) } catch (e: Exception) { val i = Intent(Intent.ACTION_SENDTO).apply { data = android.net.Uri.parse("mailto:"); putExtra(Intent.EXTRA_SUBJECT, "CoW Report — $rdate"); putExtra(Intent.EXTRA_TEXT, shareText) }; context.startActivity(i) }
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("📧", fontSize = 9.sp) }

                Button(onClick = { onUpdate(appData) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("🔄", fontSize = 9.sp) }
            }

            // Status pills
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                StatPill("${dt.size}", "Activities", Color(0xFF0A84FF))
                StatPill("${inspRate}%", "Pass Rate", if (inspRate >= 80) Color(0xFF30D158) else Color(0xFFFF9F0A))
                StatPill("${no.size}", "Open NCRs", Color(0xFFFF453A))
                StatPill("$totalWorkers", "Workers", Color(0xFF30D158))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // A4 REPORT
            Surface(modifier = Modifier.fillMaxWidth().padding(4.dp), color = Color.White, shape = MaterialTheme.shapes.medium, shadowElevation = 8.dp) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // HEADER
                    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1A1A2E)) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CLERK OF WORKS — DAILY SITE REPORT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF78166), letterSpacing = 1.sp, textAlign = TextAlign.Center)
                            BasicTextField(value = edProject, onValueChange = { edProject = it }, textStyle = TextStyle(fontSize = 7.sp, color = Color(0xFFA0A0B8), textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFDE7)).padding(2.dp), singleLine = true)
                            Text("Republic of Kenya — Ministry of Public Works", fontSize = 6.sp, color = Color(0xFF636366))
                        }
                    }

                    // META
                    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFFF8F9FA)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            ReportMetaRow("REPORT NO:", rn, "DATE:", rdate)
                            ReportMetaRowEditable("PROJECT:", edProject, { edProject = it }, "CONTRACT NO:", edContractNo, { edContractNo = it })
                            ReportMetaRowEditable("CONTRACTOR:", edContractor, { edContractor = it }, "REPORTED BY:", edCow, { edCow = it })
                            ReportMetaRow("WEATHER:", "☀️ Sunny & Clear", "TEMP:", "26°C")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 1. WORK PROGRESS
                    ReportSection("1. WORK PROGRESS & OBSERVATIONS")
                    if (dt.isNotEmpty()) {
                        dt.forEachIndexed { i, e ->
                            Text("1.${i+1} ${e.title}${if (e.percentComplete.isNotEmpty()) " [${e.percentComplete}%]" else ""}", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("    📍 ${e.location} | 🕐 ${sdf.format(Date(e.timestamp))}", fontSize = 6.sp, color = Color.DarkGray)
                            Text("    ${e.description.take(200)}", fontSize = 6.sp, color = Color.Black)
                        }
                    } else { Text("    No diary entries recorded for today.", fontSize = 6.sp, color = Color.DarkGray) }

                    // 2. INSPECTIONS
                    ReportSection("2. INSPECTIONS & TESTS")
                    if (it.isNotEmpty()) {
                        Text("    Pass Rate: ${inspRate}% (${it.count { i -> i.result == "pass" }}/${it.size})", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        it.forEach { e ->
                            Text("    ${e.checklistType} — ${e.result.uppercase()}", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = if (e.result == "pass") Color(0xFF008800) else Color.Red)
                            Text("    📍 ${e.location} | ✅ ${e.items.count { it.passed }}/${e.items.size} passed", fontSize = 6.sp, color = Color.DarkGray)
                        }
                    } else { Text("    No inspections recorded for today.", fontSize = 6.sp, color = Color.DarkGray) }

                    // 3. NCRs
                    ReportSection("3. NON-CONFORMANCE REPORTS (NCRs)")
                    if (no.isNotEmpty()) {
                        Text("    Outstanding NCRs: ${no.size}", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                        no.forEach { ncr -> Text("    • NCR-${ncr.id.take(6)}: ${ncr.title} [${ncr.severity}] — ${ncr.location}", fontSize = 6.sp, color = Color.Black) }
                    } else { Text("    No NCRs raised today. No outstanding NCRs.", fontSize = 6.sp, color = Color.DarkGray) }

                    // 4. SITE INSTRUCTIONS
                    ReportSection("4. SITE INSTRUCTIONS")
                    if (si.isNotEmpty()) { si.forEach { i -> Text("    SI-${i.id.take(6)}: ${i.description.take(80)} (To: ${i.issuedTo})", fontSize = 6.sp, color = Color.Black) } }
                    else { Text("    No site instructions issued today.", fontSize = 6.sp, color = Color.DarkGray) }

                    // 5. PLANT & EQUIPMENT
                    ReportSection("5. PLANT & EQUIPMENT ON SITE")
                    val activePlant = appData.plantEquipment.filter { it.status != "Demobilized" }
                    if (activePlant.isNotEmpty()) { activePlant.take(5).forEach { p -> Text("    ${p.name} — ${p.status}", fontSize = 6.sp, color = Color.Black) } }
                    else { Text("    Refer to diary entries for equipment details.", fontSize = 6.sp, color = Color.DarkGray) }

                    // 6. LABOUR
                    ReportSection("6. LABOUR ON SITE")
                    if (totalWorkers > 0) {
                        Text("    Total Personnel: $totalWorkers", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        val cats = at.groupBy { it.category }.mapValues { it.value.sumOf { v -> v.count.toIntOrNull() ?: 0 } }
                        cats.forEach { (k, v) -> Text("    $k: $v", fontSize = 6.sp, color = Color.Black) }
                    } else { Text("    Refer to contractor daily labour returns.", fontSize = 6.sp, color = Color.DarkGray) }

                    // 7. MATERIALS
                    ReportSection("7. MATERIALS DELIVERED")
                    val todayDel = appData.materialLogs.filter { it.date == today && it.type == "delivery" }
                    if (todayDel.isNotEmpty()) { todayDel.take(4).forEach { log -> val mat = appData.materials.find { m -> m.id == log.materialId }; Text("    ${mat?.name ?: "Material"}: ${log.quantity} ${log.unit}" + if (log.supplier.isNotEmpty()) " from ${log.supplier}" else "", fontSize = 6.sp, color = Color.Black) } }
                    else { Text("    No major material deliveries recorded.", fontSize = 6.sp, color = Color.DarkGray) }
                    if (lowMat.isNotEmpty()) { Text("    ⚠️ LOW STOCK:", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color.Red); lowMat.forEach { m -> Text("    • ${m.name}: ${m.currentStock} ${m.unit}", fontSize = 6.sp, color = Color.Red) } }

                    // 8. H&S — EDITABLE
                    ReportSection("8. HEALTH & SAFETY OBSERVATIONS")
                    BasicTextField(value = edHnS, onValueChange = { edHnS = it }, textStyle = TextStyle(fontSize = 6.sp, color = Color.Black), modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFDE7)).padding(4.dp), minLines = 2)

                    // 9. GENERAL REMARKS — EDITABLE
                    ReportSection("9. GENERAL REMARKS")
                    BasicTextField(value = edRemarks, onValueChange = { edRemarks = it }, textStyle = TextStyle(fontSize = 6.sp, color = Color.Black), modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFDE7)).padding(4.dp), minLines = 2)

                    // SIGNATURES — EDITABLE
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color(0xFFE0E0E0), thickness = 2.dp)
                    Text("REPORT END — $rdate", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Divider(color = Color(0xFF1A1A1A), modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp))
                            BasicTextField(value = edCow, onValueChange = { edCow = it }, textStyle = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A), textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFDE7)).padding(2.dp), singleLine = true)
                            Text("Clerk of Works", fontSize = 6.sp, color = Color(0xFF888888))
                            Text("Date: ................", fontSize = 5.sp, color = Color(0xFF888888))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Divider(color = Color(0xFF1A1A1A), modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp))
                            BasicTextField(value = "................", onValueChange = {}, textStyle = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A), textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFDE7)).padding(2.dp), singleLine = true)
                            Text("Contractor's Representative", fontSize = 6.sp, color = Color(0xFF888888))
                            Text("Date: ................", fontSize = 5.sp, color = Color(0xFF888888))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatPill(value: String, label: String, color: Color) {
    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1C1C1E), shape = MaterialTheme.shapes.small) {
        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 7.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ReportMetaRow(l1: String, v1: String, l2: String, v2: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) { Text("$l1 ", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555)); Text(v1.ifEmpty { "___________" }, fontSize = 6.sp, color = Color.Black) }
        Row(modifier = Modifier.fillMaxWidth()) { Text("$l2 ", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555)); Text(v2.ifEmpty { "___________" }, fontSize = 6.sp, color = Color.Black) }
    }
}

@Composable
fun ReportMetaRowEditable(l1: String, v1: String, on1: (String) -> Unit, l2: String, v2: String, on2: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) { Text("$l1 ", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555)); BasicTextField(value = v1, onValueChange = on1, textStyle = TextStyle(fontSize = 6.sp, color = Color.Black), modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFDE7)).padding(horizontal = 2.dp), singleLine = true) }
        Row(modifier = Modifier.fillMaxWidth()) { Text("$l2 ", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555)); BasicTextField(value = v2, onValueChange = on2, textStyle = TextStyle(fontSize = 6.sp, color = Color.Black), modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFDE7)).padding(horizontal = 2.dp), singleLine = true) }
    }
}

@Composable
fun ReportSection(title: String) {
    Spacer(modifier = Modifier.height(6.dp))
    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFFE8E8E8)) { Text(title, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), letterSpacing = 0.5.sp) }
    Divider(color = Color.Black, thickness = 0.5.dp)
}

fun generateFullKenyanPDF(ctx: Context, appData: AppData, s: ProjectSettings, cow: String, remarks: String): File {
    val pdf = PdfDocument()
    val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
    val c = page.canvas
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val dt = appData.diary.filter { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp)) == today }
    val no = appData.ncrs.filter { it.status == "open" }
    val at = appData.attendance.filter { it.date == today }

    c.drawRect(0f, 0f, 595f, 60f, Paint().apply { color = android.graphics.Color.parseColor("#1A1A2E") })
    val tp = Paint().apply { textSize = 13f; typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD); textAlign = Paint.Align.CENTER; color = android.graphics.Color.parseColor("#F78166") }
    val sp = Paint().apply { textSize = 8f; typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL); textAlign = Paint.Align.CENTER; color = android.graphics.Color.parseColor("#A0A0B8") }
    val hp = Paint().apply { textSize = 8f; typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD); color = android.graphics.Color.BLACK }
    val np = Paint().apply { textSize = 6.5f; typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL); color = android.graphics.Color.BLACK }
    val rn = "COW/${s.reportCounter}/${Calendar.getInstance().get(Calendar.YEAR)}"
    val rdate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())

    var y = 22f; c.drawText("CLERK OF WORKS — DAILY SITE REPORT", 297f, y, tp); y += 14
    c.drawText(s.projectName, 297f, y, sp); y += 26
    fun line(l: String, v: String) { c.drawText("$l $v", 40f, y, hp); y += 12 }
    line("REPORT NO:", rn); line("DATE:", rdate); line("PROJECT:", s.projectName)
    line("CONTRACTOR:", s.contractorName); line("REPORTED BY:", cow)
    y += 2; c.drawLine(40f, y, 555f, y, Paint().apply { color = android.graphics.Color.parseColor("#F78166"); strokeWidth = 1f }); y += 8
    c.drawText("1. WORK PROGRESS — ${dt.size} entries", 40f, y, hp); y += 12
    dt.take(6).forEach { e -> c.drawText("  • ${e.title} — ${e.location}", 40f, y, np); y += 10 }
    y += 4; c.drawText("2. INSPECTIONS", 40f, y, hp); y += 12
    val it = appData.inspections.filter { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp)) == today }
    it.forEach { i -> c.drawText("  • ${i.checklistType} — ${i.result.uppercase()}", 40f, y, np); y += 10 }
    y += 4; c.drawText("3. NCRs — Open: ${no.size}", 40f, y, hp); y += 12
    no.take(4).forEach { n -> c.drawText("  • ${n.title} [${n.severity}]", 40f, y, np); y += 10 }
    y += 4; c.drawText("4. LABOUR — Total: ${at.sumOf { (it.count.toIntOrNull() ?: 0) }}", 40f, y, hp); y += 12
    y += 4; c.drawText("5. H&S — Site orderly, PPE observed. No incidents.", 40f, y, hp); y += 12
    y += 4; c.drawText("6. REMARKS — $remarks", 40f, y, np); y += 16
    c.drawText("SIGNED: ________________     DATE: ________________", 40f, y, np); y += 12
    c.drawText("$cow — Clerk of Works", 40f, y, np); y += 12
    c.drawText("COUNTERSIGNED: ________________     DATE: ________________", 40f, y, np)

    pdf.finishPage(page)
    val f = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "CoW-Report-${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}.pdf")
    pdf.writeTo(FileOutputStream(f)); pdf.close()
    return f
}
