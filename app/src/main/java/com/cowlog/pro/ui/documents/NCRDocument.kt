package com.cowlog.pro.ui.documents

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
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NCRDocument(appData: AppData, ncrId: String, navController: NavController) {
    val ncr = appData.ncrs.find { it.id == ncrId } ?: return
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("cowlog", Context.MODE_PRIVATE)
    val settings = remember {
        val json = prefs.getString("settings", null)
        if (json != null) try { com.google.gson.Gson().fromJson(json, ProjectSettings::class.java) } catch (_: Exception) { ProjectSettings() } else ProjectSettings()
    }
    
    var edDesc by remember { mutableStateOf(ncr.description) }
    var edAction by remember { mutableStateOf(ncr.actionRequired) }
    var edRootCause by remember { mutableStateOf("") }
    var edCompletion by remember { mutableStateOf("") }
    var edCow by remember { mutableStateOf(settings.cowName) }
    var edContractor by remember { mutableStateOf(settings.contractorName) }
    
    val ncrNo = "NCR/${ncr.id.take(6)}/${Calendar.getInstance().get(Calendar.YEAR)}"
    val ncrDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(ncr.timestamp))
    
    val shareText = buildString {
        append("*NON-CONFORMANCE REPORT — $ncrNo*\n")
        append("══════════════════════════════\n\n")
        append("Date: $ncrDate\nProject: ${settings.projectName}\nContractor: ${settings.contractorName}\n")
        append("Location: ${ncr.location}\nSeverity: *${ncr.severity.uppercase()}*\n\n")
        append("*Description:*\n$edDesc\n\n*Action Required:*\n$edAction\n")
        append("\n— CoW Log Pro")
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Button(onClick = { navController.popBackStack() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("← Back", fontSize = 10.sp) }
            Button(onClick = { generateKenyanNCRPDF(context, ncr, settings, edDesc, edAction, edRootCause, edCompletion, edCow, edContractor); Toast.makeText(context, "PDF saved", Toast.LENGTH_SHORT).show() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))) { Text("📄 PDF", fontSize = 10.sp) }
            Button(onClick = {
                val pdfFile = generateKenyanNCRPDF(context, ncr, settings, edDesc, edAction, edRootCause, edCompletion, edCow, edContractor)
                val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
                val intent = Intent(Intent.ACTION_SEND).apply { type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, uri); putExtra(Intent.EXTRA_TEXT, shareText); setPackage("com.whatsapp"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                try { context.startActivity(intent) } catch (e: Exception) { val i = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, shareText) }; context.startActivity(Intent.createChooser(i, "Share")) }
            }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))) { Text("📱", fontSize = 10.sp) }
            Button(onClick = {
                val pdfFile = generateKenyanNCRPDF(context, ncr, settings, edDesc, edAction, edRootCause, edCompletion, edCow, edContractor)
                val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
                val intent = Intent(Intent.ACTION_SENDTO).apply { data = android.net.Uri.parse("mailto:"); putExtra(Intent.EXTRA_SUBJECT, "NCR — $ncrNo"); putExtra(Intent.EXTRA_TEXT, shareText); putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                try { context.startActivity(intent) } catch (e: Exception) { val i = Intent(Intent.ACTION_SENDTO).apply { data = android.net.Uri.parse("mailto:"); putExtra(Intent.EXTRA_SUBJECT, "NCR — $ncrNo"); putExtra(Intent.EXTRA_TEXT, shareText) }; context.startActivity(i) }
            }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("📧", fontSize = 10.sp) }
        }
        
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(8.dp)) {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = MaterialTheme.shapes.medium, shadowElevation = 8.dp) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // HEADER
                    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1A1A2E)) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("NON-CONFORMANCE REPORT", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF78166), letterSpacing = 1.sp, textAlign = TextAlign.Center)
                            Text(settings.projectName.ifEmpty { "Project" }, fontSize = 8.sp, color = Color(0xFFA0A0B8), textAlign = TextAlign.Center)
                            Text(ncrDate, fontSize = 7.sp, color = Color(0xFF636366), textAlign = TextAlign.Center)
                        }
                    }
                    
                    // META GRID
                    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFFF8F9FA)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                KenMeta("NCR NO", ncrNo, Modifier.weight(1f))
                                KenMeta("DATE", ncrDate, Modifier.weight(1f))
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                KenMeta("PROJECT", settings.projectName, Modifier.weight(1f))
                                KenMeta("CONTRACTOR", settings.contractorName, Modifier.weight(1f))
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                KenMeta("LOCATION", ncr.location, Modifier.weight(1f))
                                KenMeta("SEVERITY", ncr.severity.uppercase(), Modifier.weight(1f), Color.Red)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // SECTIONS
                    KenSection("DESCRIPTION OF NON-CONFORMANCE", edDesc) { edDesc = it }
                    KenSection("REQUIRED CORRECTIVE ACTION", edAction) { edAction = it }
                    KenSection("ROOT CAUSE", edRootCause) { edRootCause = it }
                    KenSection("CORRECTIVE ACTION COMPLETED BY", edCompletion) { edCompletion = it }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color(0xFFE0E0E0), thickness = 2.dp)
                    
                    // SIGNATURES
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                        KenSig(edCow, { edCow = it }, "Clerk of Works", Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(20.dp))
                        KenSig(edContractor, { edContractor = it }, "Contractor's Representative", Modifier.weight(1f))
                    }
                    
                    // FOOTER
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFFF8F9FA)) {
                        Text("Generated by CoW Log Pro | $ncrNo", fontSize = 6.sp, color = Color(0xFF888888), textAlign = TextAlign.Center, modifier = Modifier.padding(6.dp))
                    }
                }
            }
        }
    }
}



@Composable
fun KenSection(title: String, content: String, onEdit: (String) -> Unit) {
    Column(modifier = Modifier.padding(top = 10.dp)) {
        Text(title, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
        Divider(color = Color(0xFFF78166), thickness = 2.dp)
        Spacer(modifier = Modifier.height(4.dp))
        BasicTextField(value = content, onValueChange = onEdit, textStyle = TextStyle(fontSize = 8.sp, color = Color(0xFF444444), lineHeight = 18.sp), modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFDE7), MaterialTheme.shapes.extraSmall).padding(8.dp), minLines = 3)
    }
}



fun generateKenyanNCRPDF(ctx: Context, ncr: NCR, s: ProjectSettings, desc: String, action: String, root: String, comp: String, cow: String, con: String): File {
    val pdf = PdfDocument()
    val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
    val c = page.canvas
    
    // Dark header background
    c.drawRect(0f, 0f, 595f, 70f, Paint().apply { color = android.graphics.Color.parseColor("#1A1A2E") })
    
    val tp = Paint().apply { textSize = 14f; typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD); textAlign = Paint.Align.CENTER; color = android.graphics.Color.parseColor("#F78166") }
    val sp = Paint().apply { textSize = 8f; typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL); textAlign = Paint.Align.CENTER; color = android.graphics.Color.parseColor("#A0A0B8") }
    val hp = Paint().apply { textSize = 9f; typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD); color = android.graphics.Color.BLACK }
    val np = Paint().apply { textSize = 7f; typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL); color = android.graphics.Color.BLACK }
    val orange = Paint().apply { color = android.graphics.Color.parseColor("#F78166"); strokeWidth = 2f }
    
    var y = 25f
    c.drawText("NON-CONFORMANCE REPORT", 297f, y, tp); y += 16
    c.drawText(s.projectName, 297f, y, sp); y += 30
    
    fun line(l: String, v: String) { c.drawText("$l $v", 40f, y, hp); y += 14 }
    line("NCR NO:", "NCR/${ncr.id.take(6)}/${Calendar.getInstance().get(Calendar.YEAR)}")
    line("DATE:", SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(ncr.timestamp)))
    line("PROJECT:", s.projectName); line("CONTRACTOR:", s.contractorName)
    line("LOCATION:", ncr.location); line("SEVERITY:", ncr.severity.uppercase())
    
    y += 4; c.drawLine(40f, y, 555f, y, orange); y += 12
    
    fun sec(title: String, content: String) {
        c.drawText("$title", 40f, y, hp); y += 14
        val lines = content.chunked(80)
        lines.forEach { l -> c.drawText("  $l", 40f, y, np); y += 12 }
        y += 4
    }
    
    sec("DESCRIPTION:", desc)
    sec("CORRECTIVE ACTION:", action)
    if (root.isNotEmpty()) sec("ROOT CAUSE:", root)
    if (comp.isNotEmpty()) sec("COMPLETION:", comp)
    
    y += 10; c.drawLine(40f, y, 555f, y, Paint().apply { color = android.graphics.Color.parseColor("#E0E0E0"); strokeWidth = 1f }); y += 16
    
    c.drawText("$cow", 150f, y, hp); c.drawText(con, 400f, y, hp)
    y += 12; c.drawText("Clerk of Works", 150f, y, np); c.drawText("Contractor's Rep", 400f, y, np)
    y += 10; c.drawText("Date: ________________", 150f, y, np); c.drawText("Date: ________________", 400f, y, np)
    
    pdf.finishPage(page)
    val f = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "NCR-${ncr.id.take(6)}.pdf")
    pdf.writeTo(FileOutputStream(f)); pdf.close()
    return f
}
