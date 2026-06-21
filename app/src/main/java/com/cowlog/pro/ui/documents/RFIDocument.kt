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
fun RFIDocument(appData: AppData, rfiId: String, navController: NavController) {
    val rfi = appData.rfis.find { it.id == rfiId } ?: return
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("cowlog", Context.MODE_PRIVATE)
    val settings = remember {
        val json = prefs.getString("settings", null)
        if (json != null) try { com.google.gson.Gson().fromJson(json, ProjectSettings::class.java) } catch (_: Exception) { ProjectSettings() } else ProjectSettings()
    }
    
    var edQuestion by remember { mutableStateOf(rfi.question) }
    var edResponse by remember { mutableStateOf(rfi.response) }
    
    val rfiNo = "RFI/${rfi.id.take(6)}/${Calendar.getInstance().get(Calendar.YEAR)}"
    val rfiDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(rfi.timestamp))
    
    val shareText = buildString {
        append("*REQUEST FOR INFORMATION — $rfiNo*\n═══════════════════\n\n")
        append("Date: $rfiDate\nTo: ${rfi.sentTo}\nProject: ${settings.projectName}\n\n")
        append("*Question:*\n$edQuestion\n")
        if (edResponse.isNotEmpty()) append("\n*Response:*\n$edResponse\n")
        append("\n— CoW Log Pro")
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Button(onClick = { navController.popBackStack() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("←", fontSize = 10.sp) }
            Button(onClick = { generateKenyanRFIPDF(context, rfi, settings, edQuestion, edResponse); Toast.makeText(context, "PDF saved", Toast.LENGTH_SHORT).show() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))) { Text("📄", fontSize = 10.sp) }
            Button(onClick = { val i = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, shareText) }; context.startActivity(Intent.createChooser(i, "Share RFI")) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))) { Text("📱", fontSize = 10.sp) }
            Button(onClick = { val i = Intent(Intent.ACTION_SENDTO).apply { data = android.net.Uri.parse("mailto:"); putExtra(Intent.EXTRA_SUBJECT, "RFI — $rfiNo"); putExtra(Intent.EXTRA_TEXT, shareText) }; context.startActivity(i) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("📧", fontSize = 10.sp) }
        }
        
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(8.dp)) {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = MaterialTheme.shapes.medium, shadowElevation = 8.dp) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("REQUEST FOR INFORMATION", modifier = Modifier.fillMaxWidth(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center, letterSpacing = 1.sp)
                    Text(settings.projectName.ifEmpty { "Project" }, fontSize = 7.sp, color = Color.DarkGray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Divider(color = Color.Black, thickness = 2.dp, modifier = Modifier.padding(vertical = 6.dp))
                    
                    KenDocField("RFI NO:", rfiNo)
                    KenDocField("DATE:", rfiDate)
                    KenDocField("TO:", rfi.sentTo)
                    KenDocField("PROJECT:", settings.projectName)
                    KenDocField("CONTRACT NO:", settings.contractNo)
                    KenDocField("STATUS:", rfi.status.uppercase(), if (rfi.status == "open") Color.Red else Color(0xFF008800))
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    KenDocSection("QUESTION:", edQuestion) { edQuestion = it }
                    KenDocSection("RESPONSE:", edResponse) { edResponse = it }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Divider(color = Color.Black, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp))
                            BasicTextField(value = settings.cowName, onValueChange = {}, textStyle = TextStyle(fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFDE7)).padding(2.dp), singleLine = true, readOnly = true)
                            Text("Requested By", fontSize = 5.sp, color = Color.DarkGray)
                            Text("Date: ................", fontSize = 5.sp, color = Color.DarkGray)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Divider(color = Color.Black, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp))
                            BasicTextField(value = "................", onValueChange = {}, textStyle = TextStyle(fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFDE7)).padding(2.dp), singleLine = true)
                            Text("Responded By", fontSize = 5.sp, color = Color.DarkGray)
                            Text("Date: ................", fontSize = 5.sp, color = Color.DarkGray)
                        }
                    }
                }
            }
        }
    }
}

fun generateKenyanRFIPDF(ctx: Context, rfi: RFI, s: ProjectSettings, question: String, response: String) {
    val pdf = PdfDocument()
    val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
    val c = page.canvas
    val tp = Paint().apply { textSize = 13f; typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD); textAlign = Paint.Align.CENTER }
    val hp = Paint().apply { textSize = 8f; typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD) }
    val np = Paint().apply { textSize = 7f; typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL) }
    
    var y = 35f
    c.drawText("REQUEST FOR INFORMATION", 297f, y, tp); y += 18
    c.drawText(s.projectName, 297f, y, np); y += 18
    c.drawText("RFI/${rfi.id.take(6)} — ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(rfi.timestamp))}", 40f, y, hp); y += 12
    c.drawText("To: ${rfi.sentTo}    Project: ${s.projectName}", 40f, y, np); y += 14
    c.drawLine(40f, y, 555f, y, Paint().apply { strokeWidth = 0.5f }); y += 10
    c.drawText("QUESTION:", 40f, y, hp); y += 12; c.drawText("  $question", 40f, y, np); y += 24
    if (response.isNotEmpty()) { c.drawText("RESPONSE:", 40f, y, hp); y += 12; c.drawText("  $response", 40f, y, np); y += 20 }
    y += 10
    c.drawText("Requested By: ${s.cowName}     Responded By: ________________", 40f, y, np)
    c.drawText("Date: ________________     Date: ________________", 40f, y + 14, np)
    
    pdf.finishPage(page)
    val f = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "RFI-${rfi.id.take(6)}.pdf")
    pdf.writeTo(FileOutputStream(f)); pdf.close()
}
