package com.cowlog.pro.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.cowlog.pro.data.*
import com.cowlog.pro.ui.BottomNavBar
import com.cowlog.pro.ui.TopBar
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(appData: AppData, settings: ProjectSettings, navController: NavController, onUpdate: (AppData) -> Unit) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var selectedDate by remember { mutableStateOf(dateFormat.format(Date())) }
    var showDatePicker by remember { mutableStateOf(false) }
    val cal = Calendar.getInstance()
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val rn = "COW/${settings.reportCounter}/${Calendar.getInstance().get(Calendar.YEAR)}"
    val rdate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(dateFormat.parse(selectedDate)!!)

    val dt = appData.diary.filter { dateFormat.format(Date(it.timestamp)) == selectedDate }
    val itInsp = appData.inspections.filter { dateFormat.format(Date(it.timestamp)) == selectedDate }
    val cubes = appData.concrete.filter { dateFormat.format(Date(it.timestamp)) == selectedDate }
    val si = appData.instructions.filter { dateFormat.format(Date(it.timestamp)) == selectedDate }
    val no = appData.ncrs.filter { it.status == "open" && dateFormat.format(Date(it.timestamp)) == selectedDate }
    val delays = appData.delays.filter { it.date == selectedDate }
    val todayDel = appData.materialLogs.filter { it.date == selectedDate }
    val at = appData.attendance.filter { it.date == selectedDate }
    val activePlant = appData.plantEquipment.filter { it.status == "working" || it.status == "idle" }
    val todayPlantLogs = appData.plantDailyLogs.filter { it.date == selectedDate }
    val activePlantToday = activePlant.filter { p -> todayPlantLogs.any { it.plantId == p.id } }
    val lowMat = appData.materials.filter { it.currentStock < it.minStock }
    val totalWorkers = at.sumOf { (it.count.toIntOrNull() ?: 0) }

    var edProject by remember { mutableStateOf(settings.projectName) }
    var edContractor by remember { mutableStateOf(settings.contractorName) }
    var edContractNo by remember { mutableStateOf(settings.contractNo) }
    var edCow by remember { mutableStateOf(settings.cowName) }
    var edDayNo by remember { mutableStateOf("") }; var edWeekNo by remember { mutableStateOf("") }
    var edWeather by remember { mutableStateOf("☐ Sunny  ☐ Cloudy  ☐ Rainy  ☐ Stormy") }
    var edTemp by remember { mutableStateOf("Min: ___°C   Max: ___°C") }
    var edRemarks by remember { mutableStateOf("Work is progressing in accordance with the approved programme and specifications unless otherwise noted. Contractor is reminded to maintain quality standards and adhere to all safety requirements.") }
    var edHnS by remember { mutableStateOf("No specific H&S issues noted. Site appeared orderly. All personnel observed wearing mandatory PPE.") }
    var edContractorRep by remember { mutableStateOf("") }
    var hsPpe by remember { mutableStateOf(true) }; var hsAccess by remember { mutableStateOf(true) }
    var hsScaffold by remember { mutableStateOf(true) }; var hsFirstAid by remember { mutableStateOf(true) }
    var hsFire by remember { mutableStateOf(true) }; var hsIncidents by remember { mutableStateOf(true) }
    var edIncidents by remember { mutableStateOf("") }; var edActions by remember { mutableStateOf("") }
    var workRows by remember { mutableStateOf(5) }; var plantRows by remember { mutableStateOf(4) }
    var matRows by remember { mutableStateOf(3) }; var inspRows by remember { mutableStateOf(3) }
    var ncrRows by remember { mutableStateOf(2) }; var siRows by remember { mutableStateOf(2) }
    var delayRows by remember { mutableStateOf(2) }

    if (showDatePicker) {
        val dps = rememberDatePickerState(initialSelectedDateMillis = dateFormat.parse(selectedDate)!!.time)
        DatePickerDialog(onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { dps.selectedDateMillis?.let { selectedDate = dateFormat.format(Date(it)) }; showDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }) { DatePicker(state = dps) }
    }

    Scaffold( bottomBar = { BottomNavBar(navController, "report") }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(8.dp)) {

            // DATE SELECTOR
            Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF2C2C2E), shape = MaterialTheme.shapes.small) {
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { cal.time = dateFormat.parse(selectedDate)!!; cal.add(Calendar.DAY_OF_MONTH, -1); selectedDate = dateFormat.format(cal.time) }) { Text("\u25C0", color = Color.White, fontSize = 14.sp) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { showDatePicker = true }) {
                        Text(selectedDate, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF78166))
                        Text(if (selectedDate == dateFormat.format(Date())) "TODAY" else rdate.take(20), fontSize = 8.sp, color = Color(0xFFA0A0B8))
                    }
                    TextButton(onClick = { cal.time = dateFormat.parse(selectedDate)!!; cal.add(Calendar.DAY_OF_MONTH, 1); val nd = dateFormat.format(cal.time); if (nd <= dateFormat.format(Date())) selectedDate = nd }) { Text("\u25B6", color = Color.White, fontSize = 14.sp) }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // 6 ACTION BUTTONS
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(onClick = { val file = generateReportPDF(context, appData, settings, edCow, edRemarks, edHnS, edWeather, edTemp, edContractorRep); Toast.makeText(context, "PDF saved: ${file.absolutePath}", Toast.LENGTH_LONG).show() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))) { Text("\uD83D\uDCC4", fontSize = 9.sp) }
                Button(onClick = { try { val file = generateReportPDF(context, appData, settings, edCow, edRemarks, edHnS, edWeather, edTemp, edContractorRep); val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file); val i = Intent(Intent.ACTION_SEND).apply { type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, uri); putExtra(Intent.EXTRA_TEXT, "CoW Daily Report - $rdate - $edProject"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }; context.startActivity(Intent.createChooser(i, "Share PDF")) } catch (e: Exception) { Toast.makeText(context, "Sharing failed", Toast.LENGTH_SHORT).show() } }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))) { Text("\uD83D\uDCF1", fontSize = 9.sp) }
                Button(onClick = { try { val file = generateReportPDF(context, appData, settings, edCow, edRemarks, edHnS, edWeather, edTemp, edContractorRep); val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file); val i = Intent(Intent.ACTION_SEND).apply { type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, uri); putExtra(Intent.EXTRA_SUBJECT, "CoW Daily Report — $rdate — $edProject"); putExtra(Intent.EXTRA_TEXT, "Please find attached the Clerk of Works Daily Site Report."); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }; context.startActivity(Intent.createChooser(i, "Share Report")) } catch (e: Exception) { Toast.makeText(context, "No email app", Toast.LENGTH_SHORT).show() } }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("\uD83D\uDCE7", fontSize = 9.sp) }
                Button(onClick = { val file = generateReportPDF(context, appData, settings, edCow, edRemarks, edHnS, edWeather, edTemp, edContractorRep); printPDF(context, file) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF636366))) { Text("\uD83D\uDDA8\uFE0F", fontSize = 9.sp) }
                Button(onClick = { val sr = SavedReport(id = "RPT-${UUID.randomUUID().toString().take(8)}", date = selectedDate, reportNo = rn, projectName = edProject, contractorName = edContractor, contractNo = edContractNo, cowName = edCow, contractorRep = edContractorRep, dayNo = edDayNo, weekNo = edWeekNo, weather = edWeather, temp = edTemp, remarks = edRemarks, hns = edHnS, diaryCount = dt.size, inspectionCount = itInsp.size, ncrCount = no.size, siCount = si.size, labourTotal = totalWorkers, plantCount = activePlantToday.ifEmpty { activePlant }.size, materialDeliveries = todayDel.size, delayCount = delays.size); appData.savedReports.add(sr); onUpdate(appData); Toast.makeText(context, "\u2705 Report saved for $selectedDate", Toast.LENGTH_SHORT).show() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9500))) { Text("\uD83D\uDCBE", fontSize = 9.sp) }
                Button(onClick = { onUpdate(appData) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("\uD83D\uDD04", fontSize = 9.sp) }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // HEADER
            Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1A1A2E), shape = MaterialTheme.shapes.small) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CLERK OF WORKS — DAILY SITE REPORT", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF78166), letterSpacing = 1.sp, textAlign = TextAlign.Center)
                    BasicTextField(value = edProject, onValueChange = { edProject = it }, textStyle = TextStyle(fontSize = 9.sp, color = Color(0xFFA0A0B8), textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFDE7)).padding(2.dp), singleLine = true)
                    Text("Republic of Kenya — Ministry of Public Works Format", fontSize = 7.sp, color = Color(0xFF636366))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // META
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = MaterialTheme.shapes.small, shadowElevation = 2.dp) {
                Column(modifier = Modifier.padding(8.dp)) {
                    ReportMetaRowEditable("CONTRACT NO:", edContractNo, { edContractNo = it }, "PROJECT:", edProject, { edProject = it })
                    ReportMetaRowEditable("CONTRACTOR:", edContractor, { edContractor = it }, "DATE:", rdate, {})
                    ReportMetaRowEditable("DAY NO:", edDayNo, { edDayNo = it }, "WEEK NO:", edWeekNo, { edWeekNo = it })
                    ReportMetaRow("REPORT NO:", rn, "REPORTED BY:", edCow)
                    ReportMetaRowEditable("WEATHER:", edWeather, { edWeather = it }, "TEMP:", edTemp, { edTemp = it })
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // 10 SECTIONS
            ReportSectionHeader("1.0 WORK IN PROGRESS"); WorkProgressTable(dt, workRows); Spacer(modifier = Modifier.height(8.dp))
            ReportSectionHeader("2.0 LABOUR ON SITE"); LabourTable(at, totalWorkers); Spacer(modifier = Modifier.height(8.dp))
            ReportSectionHeader("3.0 PLANT & EQUIPMENT ON SITE"); PlantTable(activePlantToday.ifEmpty { activePlant }, plantRows); Spacer(modifier = Modifier.height(8.dp))
            ReportSectionHeader("4.0 MATERIALS DELIVERED"); MaterialsTable(todayDel, appData, matRows); Spacer(modifier = Modifier.height(8.dp))
            ReportSectionHeader("5.0 INSPECTIONS, TESTS & CONCRETE CUBES"); InspectionsTable(itInsp, sdf, inspRows); ConcreteTable(cubes); Spacer(modifier = Modifier.height(8.dp))
            ReportSectionHeader("6.0 NON-CONFORMANCE REPORTS (NCRs)"); NCRTable(no, ncrRows); Spacer(modifier = Modifier.height(8.dp))
            ReportSectionHeader("7.0 SITE INSTRUCTIONS ISSUED"); SITable(si, siRows); Spacer(modifier = Modifier.height(8.dp))
            ReportSectionHeader("8.0 DELAYS / DISRUPTIONS"); DelaysTable(delays, delayRows); Spacer(modifier = Modifier.height(8.dp))
            ReportSectionHeader("9.0 HEALTH & SAFETY OBSERVATIONS"); HSandSChecklist(hsPpe, { hsPpe = it }, hsAccess, { hsAccess = it }, hsScaffold, { hsScaffold = it }, hsFirstAid, { hsFirstAid = it }, hsFire, { hsFire = it }, hsIncidents, { hsIncidents = it }, edIncidents, { edIncidents = it }, edActions, { edActions = it }); Spacer(modifier = Modifier.height(8.dp))
            ReportSectionHeader("10.0 GENERAL REMARKS")
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = MaterialTheme.shapes.small, shadowElevation = 1.dp) { BasicTextField(value = edRemarks, onValueChange = { edRemarks = it }, textStyle = TextStyle(fontSize = 7.sp, color = Color.Black), modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFDE7)).padding(8.dp), minLines = 3) }
            Spacer(modifier = Modifier.height(12.dp))

            // SIGNATURES
            Divider(color = Color(0xFF1A1A1A), thickness = 1.dp)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Divider(color = Color(0xFF1A1A1A), modifier = Modifier.fillMaxWidth(0.8f))
                    BasicTextField(value = edCow, onValueChange = { edCow = it }, textStyle = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A), textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth(0.8f).background(Color(0xFFFFFDE7)).padding(2.dp), singleLine = true)
                    Text("Clerk of Works", fontSize = 7.sp, color = Color(0xFF888888))
                    BasicTextField(value = "Date: $rdate", onValueChange = {}, textStyle = TextStyle(fontSize = 6.sp, color = Color(0xFF888888), textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth(0.8f), singleLine = true, readOnly = true)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Divider(color = Color(0xFF1A1A1A), modifier = Modifier.fillMaxWidth(0.8f))
                    BasicTextField(value = edContractorRep, onValueChange = { edContractorRep = it }, textStyle = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A), textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth(0.8f).background(Color(0xFFFFFDE7)).padding(2.dp), singleLine = true)
                    Text("Contractor's Representative", fontSize = 7.sp, color = Color(0xFF888888))
                    BasicTextField(value = "Date: ................", onValueChange = {}, textStyle = TextStyle(fontSize = 6.sp, color = Color(0xFF888888), textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth(0.8f), singleLine = true)
                }
            }
            Divider(color = Color(0xFF1A1A1A), thickness = 1.dp)
            Text("Generated by CoW Log Pro | $rn | $selectedDate | Republic of Kenya", fontSize = 6.sp, color = Color(0xFF888888), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// ====== REUSABLE COMPONENTS ======
@Composable fun ReportSectionHeader(title: String) { Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1A1A2E)) { Text(title, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), letterSpacing = 0.5.sp) } }
@Composable fun ReportMetaRow(l1: String, v1: String, l2: String, v2: String) { Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) { Row(modifier = Modifier.weight(1f)) { Text("$l1 ", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555)); Text(v1.ifEmpty { "___________" }, fontSize = 6.sp, color = Color.Black) }; Row(modifier = Modifier.weight(1f)) { Text("$l2 ", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555)); Text(v2.ifEmpty { "___________" }, fontSize = 6.sp, color = Color.Black) } } }
@Composable fun ReportMetaRowEditable(l1: String, v1: String, on1: (String) -> Unit, l2: String, v2: String, on2: (String) -> Unit) { Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) { Row(modifier = Modifier.weight(1f)) { Text("$l1 ", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555)); BasicTextField(value = v1, onValueChange = on1, textStyle = TextStyle(fontSize = 6.sp, color = Color.Black), modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFDE7)).padding(horizontal = 2.dp), singleLine = true) }; Row(modifier = Modifier.weight(1f)) { Text("$l2 ", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555)); BasicTextField(value = v2, onValueChange = on2, textStyle = TextStyle(fontSize = 6.sp, color = Color.Black), modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFDE7)).padding(horizontal = 2.dp), singleLine = true) } } }
@Composable fun ReportTableHeader(headers: List<String>) { Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFE8E8E8)).padding(vertical = 3.dp, horizontal = 4.dp)) { headers.forEach { h -> Text(h, fontSize = 5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333), modifier = Modifier.weight(1f)) } }; Divider(color = Color.Black, thickness = 0.5.dp) }
@Composable fun EditableCell(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) { BasicTextField(value = value, onValueChange = onValueChange, textStyle = TextStyle(fontSize = 5.sp, color = Color.Black), modifier = modifier.background(Color(0xFFFFFDE7)).padding(2.dp), singleLine = true) }
@Composable fun WorkProgressTable(dt: List<DiaryEntry>, emptyRows: Int) { Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = MaterialTheme.shapes.small, shadowElevation = 1.dp) { Column(modifier = Modifier.padding(4.dp)) { ReportTableHeader(listOf("Item", "Description of Work", "Location", "% Complete", "Remarks")); if (dt.isNotEmpty()) { dt.forEachIndexed { i, e -> Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) { Text("1.${i + 1}", fontSize = 5.sp, modifier = Modifier.weight(0.5f)); Column(modifier = Modifier.weight(2f)) { Text(e.title, fontSize = 5.sp, fontWeight = FontWeight.Bold); Text(e.description.take(60), fontSize = 4.sp, color = Color.DarkGray) }; Text(e.location, fontSize = 5.sp, modifier = Modifier.weight(1.2f)); EditableCell(e.percentComplete, {}, Modifier.weight(0.8f)); EditableCell(e.issuesIdentified, {}, Modifier.weight(1.5f)) }; Divider(color = Color(0xFFEEEEEE), thickness = 0.5.dp) } } else { repeat(emptyRows) { i -> Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) { Text("1.${i + 1}", fontSize = 5.sp, modifier = Modifier.weight(0.5f)); EditableCell("", {}, Modifier.weight(2f)); EditableCell("", {}, Modifier.weight(1.2f)); EditableCell("", {}, Modifier.weight(0.8f)); EditableCell("", {}, Modifier.weight(1.5f)) } } } } } }
@Composable fun LabourTable(at: List<Attendance>, total: Int) { val cats = listOf("Skilled", "Semi-Skilled", "Unskilled"); val catCounts = mutableMapOf<String, Int>(); at.forEach { e -> val c = e.category.ifEmpty { "Unskilled" }; catCounts[c] = (catCounts[c] ?: 0) + (e.count.toIntOrNull() ?: 0) }; Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = MaterialTheme.shapes.small, shadowElevation = 1.dp) { Column(modifier = Modifier.padding(4.dp)) { ReportTableHeader(listOf("Category", "Contractor", "Sub-Contractor", "Total")); cats.forEach { cat -> Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) { Text(cat, fontSize = 5.sp, modifier = Modifier.weight(1f)); EditableCell((catCounts[cat]?.toString() ?: ""), {}, Modifier.weight(1f)); EditableCell("", {}, Modifier.weight(1f)); EditableCell("", {}, Modifier.weight(1f)) } }; Divider(color = Color.Black, thickness = 0.5.dp); Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF5F5F5)).padding(vertical = 3.dp, horizontal = 4.dp)) { Text("TOTAL", fontSize = 5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text("", modifier = Modifier.weight(1f)); Text("", modifier = Modifier.weight(1f)); Text("$total", fontSize = 5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)) } } } }
@Composable fun PlantTable(plant: List<PlantEquipment>, emptyRows: Int) { Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = MaterialTheme.shapes.small, shadowElevation = 1.dp) { Column(modifier = Modifier.padding(4.dp)) { ReportTableHeader(listOf("Item", "Qty", "Working", "Idle", "Remarks")); if (plant.isNotEmpty()) { plant.take(6).forEach { p -> Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) { Text(p.name, fontSize = 5.sp, modifier = Modifier.weight(2f)); EditableCell("1", {}, Modifier.weight(0.8f)); EditableCell(if (p.status == "working") "\u2713" else "", {}, Modifier.weight(0.8f)); EditableCell(if (p.status == "idle") "\u2713" else "", {}, Modifier.weight(0.8f)); EditableCell("", {}, Modifier.weight(1.6f)) } } } else { repeat(emptyRows) { Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) { EditableCell("", {}, Modifier.weight(2f)); EditableCell("", {}, Modifier.weight(0.8f)); EditableCell("", {}, Modifier.weight(0.8f)); EditableCell("", {}, Modifier.weight(0.8f)); EditableCell("", {}, Modifier.weight(1.6f)) } } } } } }
@Composable fun MaterialsTable(todayDel: List<MaterialLog>, appData: AppData, emptyRows: Int) { Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = MaterialTheme.shapes.small, shadowElevation = 1.dp) { Column(modifier = Modifier.padding(4.dp)) { ReportTableHeader(listOf("Material", "Quantity", "Supplier", "Delivery Note", "Remarks")); if (todayDel.isNotEmpty()) { todayDel.take(4).forEach { log -> val mat = appData.materials.find { it.id == log.materialId }; Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) { Text(mat?.name ?: "Material", fontSize = 5.sp, modifier = Modifier.weight(1.5f)); Text("${log.quantity} ${log.unit}", fontSize = 5.sp, modifier = Modifier.weight(1f)); EditableCell(log.supplier, {}, Modifier.weight(1.5f)); EditableCell(log.deliveryNote, {}, Modifier.weight(1f)); EditableCell("", {}, Modifier.weight(1f)) } } } else { repeat(emptyRows) { Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) { EditableCell("", {}, Modifier.weight(1.5f)); EditableCell("", {}, Modifier.weight(1f)); EditableCell("", {}, Modifier.weight(1.5f)); EditableCell("", {}, Modifier.weight(1f)); EditableCell("", {}, Modifier.weight(1f)) } } }; val low = appData.materials.filter { it.currentStock < it.minStock }; if (low.isNotEmpty()) { Spacer(modifier = Modifier.height(4.dp)); Text("\u26A0\uFE0F LOW STOCK:", fontSize = 5.sp, fontWeight = FontWeight.Bold, color = Color.Red); low.forEach { m -> Text("  \u2022 ${m.name}: ${m.currentStock} ${m.unit}", fontSize = 5.sp, color = Color.Red) } } } } }
@Composable fun InspectionsTable(it: List<Inspection>, sdf: SimpleDateFormat, emptyRows: Int) { Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = MaterialTheme.shapes.small, shadowElevation = 1.dp) { Column(modifier = Modifier.padding(4.dp)) { ReportTableHeader(listOf("Time", "Test/Inspection", "Location", "Result", "Remarks")); if (it.isNotEmpty()) { it.forEach { e -> Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) { Text(sdf.format(Date(e.timestamp)), fontSize = 5.sp, modifier = Modifier.weight(0.8f)); Text(e.checklistType, fontSize = 5.sp, modifier = Modifier.weight(1.5f)); Text(e.location, fontSize = 5.sp, modifier = Modifier.weight(1.2f)); Text(if (e.result == "pass") "\u2705 PASS" else "\u274C FAIL", fontSize = 5.sp, fontWeight = FontWeight.Bold, color = if (e.result == "pass") Color(0xFF008800) else Color.Red, modifier = Modifier.weight(0.8f)); EditableCell(e.notes, {}, Modifier.weight(1.7f)) } } } else { repeat(emptyRows) { Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) { EditableCell("", {}, Modifier.weight(0.8f)); EditableCell("", {}, Modifier.weight(1.5f)); EditableCell("", {}, Modifier.weight(1.2f)); EditableCell("", {}, Modifier.weight(0.8f)); EditableCell("", {}, Modifier.weight(1.7f)) } } } } } }
@Composable fun NCRTable(no: List<NCR>, emptyRows: Int) { Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = MaterialTheme.shapes.small, shadowElevation = 1.dp) { Column(modifier = Modifier.padding(4.dp)) { ReportTableHeader(listOf("NCR No.", "Description", "Date Raised", "Status", "Action Taken")); if (no.isNotEmpty()) { no.forEach { n -> Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) { Text("NCR-${n.id.take(6)}", fontSize = 5.sp, modifier = Modifier.weight(1f)); Text(n.title, fontSize = 5.sp, modifier = Modifier.weight(2f)); Text(SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(n.timestamp)), fontSize = 5.sp, modifier = Modifier.weight(0.8f)); Text(n.status.uppercase(), fontSize = 5.sp, fontWeight = FontWeight.Bold, color = Color.Red, modifier = Modifier.weight(0.8f)); EditableCell(n.actionRequired, {}, Modifier.weight(1.4f)) } } } else { repeat(emptyRows) { Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) { EditableCell("", {}, Modifier.weight(1f)); EditableCell("", {}, Modifier.weight(2f)); EditableCell("", {}, Modifier.weight(0.8f)); EditableCell("", {}, Modifier.weight(0.8f)); EditableCell("", {}, Modifier.weight(1.4f)) } } } } } }
@Composable fun SITable(si: List<SiteInstruction>, emptyRows: Int) { Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = MaterialTheme.shapes.small, shadowElevation = 1.dp) { Column(modifier = Modifier.padding(4.dp)) { ReportTableHeader(listOf("SI No.", "Description", "Issued To", "Date", "Status")); if (si.isNotEmpty()) { si.forEach { s -> Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) { Text("SI-${s.id.take(6)}", fontSize = 5.sp, modifier = Modifier.weight(1f)); Text(s.description.take(60), fontSize = 5.sp, modifier = Modifier.weight(2.5f)); Text(s.issuedTo, fontSize = 5.sp, modifier = Modifier.weight(1f)); Text(SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(s.timestamp)), fontSize = 5.sp, modifier = Modifier.weight(0.8f)); Text(s.status.uppercase(), fontSize = 5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f)) } } } else { repeat(emptyRows) { Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) { EditableCell("", {}, Modifier.weight(1f)); EditableCell("", {}, Modifier.weight(2.5f)); EditableCell("", {}, Modifier.weight(1f)); EditableCell("", {}, Modifier.weight(0.8f)); EditableCell("", {}, Modifier.weight(0.7f)) } } } } } }
@Composable fun DelaysTable(delays: List<Delay>, emptyRows: Int) { Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = MaterialTheme.shapes.small, shadowElevation = 1.dp) { Column(modifier = Modifier.padding(4.dp)) { ReportTableHeader(listOf("Cause", "Duration", "Impact", "Responsible", "Remarks")); if (delays.isNotEmpty()) { delays.forEach { d -> Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) { Text(d.cause, fontSize = 5.sp, modifier = Modifier.weight(1.5f)); Text("${d.duration} days", fontSize = 5.sp, modifier = Modifier.weight(0.8f)); Text(d.impact, fontSize = 5.sp, modifier = Modifier.weight(1.2f)); EditableCell("", {}, Modifier.weight(1f)); EditableCell("", {}, Modifier.weight(1.5f)) } } } else { repeat(emptyRows) { Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) { EditableCell("", {}, Modifier.weight(1.5f)); EditableCell("", {}, Modifier.weight(0.8f)); EditableCell("", {}, Modifier.weight(1.2f)); EditableCell("", {}, Modifier.weight(1f)); EditableCell("", {}, Modifier.weight(1.5f)) } } } } } }
@Composable fun HSandSChecklist(ppe: Boolean, onPpe: (Boolean) -> Unit, access: Boolean, onAccess: (Boolean) -> Unit, scaffold: Boolean, onScaffold: (Boolean) -> Unit, firstAid: Boolean, onFirstAid: (Boolean) -> Unit, fire: Boolean, onFire: (Boolean) -> Unit, incidents: Boolean, onIncidents: (Boolean) -> Unit, edIncidents: String, onIncidentsChange: (String) -> Unit, edActions: String, onActionsChange: (String) -> Unit) { Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = MaterialTheme.shapes.small, shadowElevation = 1.dp) { Column(modifier = Modifier.padding(8.dp)) { HSCheckRow("All workers wearing PPE (helmets, boots, vests)", ppe, onPpe); HSCheckRow("Site access controlled & safe", access, onAccess); HSCheckRow("Scaffolding & ladders in good condition", scaffold, onScaffold); HSCheckRow("First aid kit available & stocked", firstAid, onFirstAid); HSCheckRow("Fire extinguishers in place", fire, onFire); HSCheckRow("No incidents / accidents reported", incidents, onIncidents); Spacer(modifier = Modifier.height(8.dp)); Text("Incidents (if any):", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555)); BasicTextField(value = edIncidents, onValueChange = onIncidentsChange, textStyle = TextStyle(fontSize = 6.sp, color = Color.Black), modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFDE7)).padding(4.dp), minLines = 1); Text("Actions Taken:", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555)); BasicTextField(value = edActions, onValueChange = onActionsChange, textStyle = TextStyle(fontSize = 6.sp, color = Color.Black), modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFDE7)).padding(4.dp), minLines = 1) } } }
@Composable fun ConcreteTable(cubes: List<ConcreteCube>) { if (cubes.isNotEmpty()) { Surface(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), color = Color.White, shape = MaterialTheme.shapes.small, shadowElevation = 1.dp) { Column(modifier = Modifier.padding(4.dp)) { ReportTableHeader(listOf("Cube ID", "Location", "Grade", "Pour Date", "7-Day", "14-Day", "28-Day")); cubes.forEach { c -> Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) { Text(c.cubeId, fontSize = 5.sp, modifier = Modifier.weight(1f)); Text(c.location, fontSize = 5.sp, modifier = Modifier.weight(1f)); Text(c.grade, fontSize = 5.sp, modifier = Modifier.weight(0.8f)); Text(c.pourDate, fontSize = 5.sp, modifier = Modifier.weight(1f)); Text(c.result7?.toString() ?: "-", fontSize = 5.sp, modifier = Modifier.weight(0.7f), color = if (c.result7 != null && c.result7 >= 25.0) Color(0xFF008800) else Color.Red); Text(c.result14?.toString() ?: "-", fontSize = 5.sp, modifier = Modifier.weight(0.7f)); Text(c.result28?.toString() ?: "-", fontSize = 5.sp, modifier = Modifier.weight(0.7f), color = if (c.result28 != null && c.result28 >= 30.0) Color(0xFF008800) else Color.Red) }; Divider(color = Color(0xFFEEEEEE), thickness = 0.5.dp) } } } }
}
@Composable fun HSCheckRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) { Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = checked, onCheckedChange = onChecked, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(4.dp)); Text(label, fontSize = 6.sp, color = Color.Black) } }

// ====== PDF GENERATION ======
fun generateReportPDF(context: Context, appData: AppData, settings: ProjectSettings, cow: String, remarks: String, hns: String, weather: String, temp: String, contractorRep: String): File {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val dt = appData.diary.filter { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp)) == today }
    val it = appData.inspections.filter { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp)) == today }
    val si = appData.instructions.filter { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp)) == today }
    val no = appData.ncrs.filter { it.status == "open" }
    val at = appData.attendance.filter { it.date == today }
    val delays = appData.delays.filter { it.date == today }
    val todayDel = appData.materialLogs.filter { it.date == today }
    val activePlant = appData.plantEquipment.filter { it.status == "working" || it.status == "idle" }
    val todayPlantLogs = appData.plantDailyLogs.filter { it.date == today }
    val activePlantToday = activePlant.filter { p -> todayPlantLogs.any { it.plantId == p.id } }
    val rn = "COW/${settings.reportCounter}/${Calendar.getInstance().get(Calendar.YEAR)}"
    val rdate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
    val totalWorkers = at.sumOf { (it.count.toIntOrNull() ?: 0) }

    val pdf = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdf.startPage(pageInfo)
    val canvas = page.canvas
    val p = Paint().apply { color = android.graphics.Color.BLACK; textSize = 10f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL) }
    val pb = Paint().apply { color = android.graphics.Color.BLACK; textSize = 12f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
    val pg = Paint().apply { color = android.graphics.Color.GRAY; textSize = 8f }
    var y = 30f

    fun line(l: String, sz: Float = 10f) { p.textSize = sz; canvas.drawText(l, 30f, y, p); y += sz + 4f }
    fun lineB(l: String, sz: Float = 12f) { pb.textSize = sz; canvas.drawText(l, 30f, y, pb); y += sz + 4f }
    fun section(title: String) { y += 4f; lineB(title, 11f); y += 2f }

    lineB("CLERK OF WORKS — DAILY SITE REPORT", 14f)
    line(settings.projectName, 10f); y += 4f
    line("Republic of Kenya — Ministry of Public Works Format", 8f); y += 4f
    line("Report: $rn | Date: $rdate | Project: ${settings.projectName} | Contractor: ${settings.contractorName}", 8f); y += 6f

    section("1. WORK IN PROGRESS (${dt.size} entries)")
    dt.take(8).forEach { e -> line("  • ${e.title} — ${e.location} [${e.percentComplete}%]", 9f); if (e.description.isNotBlank()) { line("    ${e.description.take(100)}", 8f) } }
    y += 4f

    section("2. LABOUR (Total: $totalWorkers)")
    val cats = listOf("Skilled", "Semi-Skilled", "Unskilled"); val catCounts = mutableMapOf<String, Int>()
    at.forEach { e -> val c = e.category.ifEmpty { "Unskilled" }; catCounts[c] = (catCounts[c] ?: 0) + (e.count.toIntOrNull() ?: 0) }
    cats.forEach { cat -> line("  $cat: ${catCounts[cat] ?: 0}", 9f) }; y += 4f

    section("3. PLANT & EQUIPMENT (${activePlantToday.ifEmpty { activePlant }.size} active)")
    activePlantToday.ifEmpty { activePlant }.take(6).forEach { p -> line("  • ${p.name} — ${p.status}", 9f) }; y += 4f

    section("4. MATERIALS (${todayDel.size} deliveries)")
    todayDel.take(5).forEach { log -> val mat = appData.materials.find { it.id == log.materialId }; line("  • ${mat?.name ?: "Material"}: ${log.quantity} ${log.unit}", 9f) }; y += 4f

    section("5. INSPECTIONS (${it.size})")
    it.forEach { e -> line("  • ${e.checklistType} — ${e.result.uppercase()} (${e.items.count { it.passed }}/${e.items.size} passed)", 9f) }; y += 4f

    section("6. NCRs — Open: ${no.size}")
    no.take(5).forEach { n -> line("  • ${n.title} [${n.severity}] — ${n.location}", 9f) }; y += 4f

    section("7. SITE INSTRUCTIONS (${si.size})")
    si.take(5).forEach { s -> line("  • ${s.description.take(80)}", 9f) }; y += 4f

    section("8. DELAYS (${delays.size})")
    delays.take(3).forEach { d -> line("  • ${d.cause} — ${d.duration} days — ${d.impact}", 9f) }; y += 4f

    section("9. HEALTH & SAFETY")
    line("  $hns", 9f); y += 4f

    section("10. GENERAL REMARKS")
    line("  $remarks", 9f); y += 8f

    line("SIGNED: ________________     DATE: ________________", 9f)
    line("$cow — Clerk of Works", 9f)
    line("COUNTERSIGNED: ________________     DATE: ________________", 9f); y += 6f
    line("Generated by CoW Log Pro | $rn | $rdate | Republic of Kenya", 7f)

    pdf.finishPage(page)
    val fileName = "CoW-Report-${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}.pdf"
    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
    try { pdf.writeTo(FileOutputStream(file)) } catch (e: Exception) { e.printStackTrace() }
    pdf.close()
    return file
}

fun printPDF(context: Context, pdfFile: File) {
    try {
        val pm = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "CoW Report"
        val adapter = object : PrintDocumentAdapter() {
            override fun onLayout(oldAttrs: PrintAttributes?, newAttrs: PrintAttributes?, cs: android.os.CancellationSignal?, cb: LayoutResultCallback?, extras: android.os.Bundle?) {
                cb?.onLayoutFinished(PrintDocumentInfo.Builder(jobName).setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build(), true)
            }
            override fun onWrite(pages: Array<android.print.PageRange>, dest: android.os.ParcelFileDescriptor, cs: android.os.CancellationSignal?, cb: WriteResultCallback?) {
                try { val input = java.io.FileInputStream(pdfFile); val output = java.io.FileOutputStream(dest.fileDescriptor); val buf = ByteArray(1024); var len: Int; while (input.read(buf).also { len = it } > 0) { output.write(buf, 0, len) }; input.close(); output.close(); cb?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES)) } catch (e: Exception) { cb?.onWriteFailed(e.message) }
            }
        }
        pm.print(jobName, adapter, null)
    } catch (e: Exception) { Toast.makeText(context, "Print failed: ${e.message}", Toast.LENGTH_SHORT).show() }
}
