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
fun SIDocument(appData: AppData, siId: String, navController: NavController) {
    val si = appData.instructions.find { it.id == siId } ?: return
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("cowlog", Context.MODE_PRIVATE)
    val settings = remember {
        val json = prefs.getString("settings", null)
        if (json != null) try { com.google.gson.Gson().fromJson(json, ProjectSettings::class.java) } catch (_: Exception) { ProjectSettings() } else ProjectSettings()
    }
    
    var edDesc by remember { mutableStateOf(si.description) }
    var edReason by remember { mutableStateOf("") }
    var edRef by remember { mutableStateOf(si.reference) }
    var edDeadline by remember { mutableStateOf(si.deadline) }
    var edCow by remember { mutableStateOf(settings.cowName) }
    
    val siNo = "SI/${si.id.take(6)}/${Calendar.getInstance().get(Calendar.YEAR)}"
    val siDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(si.timestamp))
    
    val shareText = buildString {
        append("*SITE INSTRUCTION — $siNo*\n═══════════════════\n\n")
        append("Date: $siDate\nTo: ${si.issuedTo}\nProject: ${settings.projectName}\n\n")
        append("*Instruction:*\n$edDesc\n")
        if (edDeadline.isNotEmpty()) append("\n*Deadline:* $edDeadline\n")
        append("\n— CoW Log Pro")
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Button(onClick = { navController.popBackStack() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("←", fontSize = 10.sp) }
            Button(onClick = { generateKenyanSIPDF(context, si, settings, edDesc, edReason, edRef, edDeadline, edCow); Toast.makeText(context, "PDF saved", Toast.LENGTH_SHORT).show() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))) { Text("📄", fontSize = 10.sp) }
            Button(onClick = { val i = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, shareText) }; context.startActivity(Intent.createChooser(i, "Share SI")) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))) { Text("📱", fontSize = 10.sp) }
            Button(onClick = {
                    generateKenyanSIPDF(context, si, settings, edDesc, edReason, edRef, edDeadline, edCow)
                    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SI-${si.id.take(6)}.pdf")
                    if (file.exists()) {
                        val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = android.net.Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_SUBJECT, "SI — $siNo")
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        try { context.startActivity(intent) } catch (e: Exception) { val i = Intent(Intent.ACTION_SENDTO).apply { data = android.net.Uri.parse("mailto:"); putExtra(Intent.EXTRA_SUBJECT, "SI — $siNo"); putExtra(Intent.EXTRA_TEXT, shareText) }; context.startActivity(i) }
                    }
                }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("📧", fontSize = 10.sp) }
        }
        
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(8.dp)) {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = MaterialTheme.shapes.medium, shadowElevation = 8.dp) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("SITE INSTRUCTION", modifier = Modifier.fillMaxWidth(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center, letterSpacing = 1.sp)
                    Text(settings.projectName.ifEmpty { "Project" }, fontSize = 7.sp, color = Color.DarkGray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Divider(color = Color.Black, thickness = 2.dp, modifier = Modifier.padding(vertical = 6.dp))
                    
                    KenDocField("SI NO:", siNo)
                    KenDocField("DATE:", siDate)
                    KenDocField("PROJECT:", settings.projectName)
                    KenDocField("CONTRACT NO:", settings.contractNo)
                    KenDocField("CONTRACTOR:", settings.contractorName)
                    KenDocField("ISSUED TO:", si.issuedTo)
                    KenDocField("LOCATION:", si.location)
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Instruction type checkboxes
                    Text("You are hereby instructed to:", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Column(modifier = Modifier.padding(start = 8.dp, top = 2.dp)) {
                        listOf("☐ Stop Work Immediately", "☐ Rectify Defective Work", "☐ Comply with Specification", "☐ Accelerate Progress", "☐ Other (specify below)").forEach { Text(it, fontSize = 7.sp, color = Color.Black) }
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    KenDocSection("DETAILS OF INSTRUCTION:", edDesc) { edDesc = it }
                    KenDocSection("REASON FOR INSTRUCTION:", edReason) { edReason = it }
                    KenDocSection("REFERENCE (Drawing/Spec/Contract):", edRef) { edRef = it }
                    KenDocSection("COMPLIANCE REQUIRED BY:", edDeadline) { edDeadline = it }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // ISSUED & RECEIVED
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("ISSUED BY:", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Divider(color = Color.Black, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp))
                            BasicTextField(value = edCow, onValueChange = { edCow = it }, textStyle = TextStyle(fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFDE7)).padding(2.dp), singleLine = true)
                            Text("Clerk of Works", fontSize = 5.sp, color = Color.DarkGray)
                            Text("Date/Time: ................", fontSize = 5.sp, color = Color.DarkGray)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("RECEIVED BY:", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Divider(color = Color.Black, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp))
                            BasicTextField(value = "................", onValueChange = {}, textStyle = TextStyle(fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFDE7)).padding(2.dp), singleLine = true)
                            Text("Contractor's Rep", fontSize = 5.sp, color = Color.DarkGray)
                            Text("Date/Time: ................", fontSize = 5.sp, color = Color.DarkGray)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // ACKNOWLEDGMENT
                    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFFF5F5F5), shape = MaterialTheme.shapes.extraSmall, border = ButtonDefaults.outlinedButtonBorder) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("ACKNOWLEDGMENT", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            Text("I acknowledge receipt of this Site Instruction and understand that failure to comply may result in contractual action.", fontSize = 6.sp, color = Color.Black, modifier = Modifier.padding(vertical = 4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Divider(color = Color.Black, modifier = Modifier.width(100.dp))
                                    Text("Contractor's Rep", fontSize = 5.sp, color = Color.DarkGray)
                                    Text("Date: ................", fontSize = 5.sp, color = Color.DarkGray)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // COMPLETION
                    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFFF5F5F5), shape = MaterialTheme.shapes.extraSmall, border = ButtonDefaults.outlinedButtonBorder) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("COMPLETION", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            Text("Work completed on: ......................................", fontSize = 6.sp, color = Color.Black)
                            Text("Verified by CoW: ........................................", fontSize = 6.sp, color = Color.Black)
                            Text("Status: ☐ Complied  ☐ Partially Complied  ☐ Not Complied", fontSize = 6.sp, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

fun generateKenyanSIPDF(ctx: Context, si: SiteInstruction, s: ProjectSettings, desc: String, reason: String, ref: String, deadline: String, cow: String) {
    val pdf = PdfDocument()
    val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
    val c = page.canvas
    val tp = Paint().apply { textSize = 13f; typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD); textAlign = Paint.Align.CENTER }
    val hp = Paint().apply { textSize = 8f; typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD) }
    val np = Paint().apply { textSize = 7f; typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL) }
    
    var y = 35f
    c.drawText("SITE INSTRUCTION", 297f, y, tp); y += 18
    c.drawText(s.projectName, 297f, y, np); y += 18
    
    fun fl(l: String, v: String) { c.drawText("$l $v", 40f, y, hp); y += 12 }
    fl("SI NO:", "SI/${si.id.take(6)}/${Calendar.getInstance().get(Calendar.YEAR)}")
    fl("DATE:", SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(si.timestamp)))
    fl("PROJECT:", s.projectName)
    fl("CONTRACTOR:", s.contractorName)
    fl("ISSUED TO:", si.issuedTo)
    fl("LOCATION:", si.location)
    y += 4; c.drawLine(40f, y, 555f, y, Paint().apply { strokeWidth = 0.5f }); y += 10
    
    c.drawText("You are instructed to: ☐ Stop Work ☐ Rectify ☐ Comply ☐ Accelerate ☐ Other", 40f, y, np); y += 14
    c.drawText("DETAILS: $desc", 40f, y, np); y += 20
    if (reason.isNotEmpty()) { c.drawText("REASON: $reason", 40f, y, np); y += 14 }
    if (ref.isNotEmpty()) { c.drawText("REFERENCE: $ref", 40f, y, np); y += 14 }
    if (deadline.isNotEmpty()) { c.drawText("DEADLINE: $deadline", 40f, y, np); y += 14 }
    
    y += 10
    c.drawText("ISSUED BY: $cow (CoW)     RECEIVED BY: ________________ (Contractor)", 40f, y, np); y += 14
    c.drawText("Date: ________________     Date: ________________", 40f, y, np); y += 18
    c.drawText("ACKNOWLEDGMENT: I acknowledge receipt of this Site Instruction.", 40f, y, np)
    c.drawText("Signed: ________________     Date: ________________", 40f, y + 14, np)
    
    pdf.finishPage(page)
    val f = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SI-${si.id.take(6)}.pdf")
    pdf.writeTo(FileOutputStream(f)); pdf.close()
}
