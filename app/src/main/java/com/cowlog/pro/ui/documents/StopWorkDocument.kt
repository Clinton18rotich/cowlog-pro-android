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
fun StopWorkDocument(appData: AppData, swoId: String, navController: NavController) {
    val swo = appData.stopWorkOrders.find { it.id == swoId } ?: return
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("cowlog", Context.MODE_PRIVATE)
    val settings = remember {
        val json = prefs.getString("settings", null)
        if (json != null) try { com.google.gson.Gson().fromJson(json, ProjectSettings::class.java) } catch (_: Exception) { ProjectSettings() } else ProjectSettings()
    }
    
    var edReason by remember { mutableStateOf(swo.reason) }
    var edWork by remember { mutableStateOf(swo.workToStop) }
    var edConditions by remember { mutableStateOf(swo.resumptionConditions) }
    var edCow by remember { mutableStateOf(settings.cowName) }
    
    val swoNo = "SWO/${swo.id.take(6)}/${Calendar.getInstance().get(Calendar.YEAR)}"
    val swoDate = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault()).format(Date(swo.timestamp))
    
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Button(onClick = { navController.popBackStack() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("←") }
            Button(onClick = { generateSWOPDF(context, swo, settings, edReason, edWork, edConditions, edCow); Toast.makeText(context, "PDF saved", Toast.LENGTH_SHORT).show() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))) { Text("📄") }
            Button(onClick = { val i = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "🛑 STOP WORK ORDER — $swoNo\n${swo.reason}\nLocation: ${swo.location}") }; context.startActivity(Intent.createChooser(i, "Share")) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))) { Text("📱") }
        }
        
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(8.dp)) {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = MaterialTheme.shapes.medium, shadowElevation = 8.dp) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Red Header
                    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFFB71C1C)) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🛑 STOP WORK ORDER", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 2.sp)
                            Text("EFFECTIVE IMMEDIATELY", fontSize = 9.sp, color = Color(0xFFFFCDD2))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    KenDocField("ORDER NO:", swoNo)
                    KenDocField("DATE:", swoDate)
                    KenDocField("PROJECT:", settings.projectName)
                    KenDocField("CONTRACTOR:", settings.contractorName)
                    KenDocField("LOCATION:", swo.location)
                    Divider(color = Color(0xFFB71C1C), thickness = 2.dp, modifier = Modifier.padding(vertical = 4.dp))
                    Text("YOU ARE HEREBY ORDERED TO STOP ALL WORK with IMMEDIATE EFFECT.", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB71C1C))
                    KenDocSection("REASON:", edReason) { edReason = it }
                    KenDocSection("WORK TO BE STOPPED:", edWork) { edWork = it }
                    KenDocSection("CONDITIONS FOR RESUMPTION:", edConditions) { edConditions = it }
                    Text("Failure to comply may result in suspension, penalties, or termination of contract.", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB71C1C))
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        KenSig(edCow, { edCow = it }, "Clerk of Works", Modifier.weight(1f))
                        KenSig("................", {}, "Contractor's Rep", Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFFF5F5F5), shape = MaterialTheme.shapes.extraSmall) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("RESUMPTION", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            Text("Work Resumed: ................   Authorized: ................", fontSize = 6.sp, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

fun generateSWOPDF(ctx: Context, swo: StopWorkOrder, s: ProjectSettings, reason: String, work: String, conditions: String, cow: String) {
    val pdf = PdfDocument()
    val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
    val c = page.canvas
    c.drawRect(0f, 0f, 595f, 60f, Paint().apply { color = android.graphics.Color.parseColor("#B71C1C") })
    val tp = Paint().apply { textSize = 14f; typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD); textAlign = Paint.Align.CENTER; color = android.graphics.Color.WHITE }
    val sp = Paint().apply { textSize = 8f; typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL); textAlign = Paint.Align.CENTER; color = android.graphics.Color.parseColor("#FFCDD2") }
    val hp = Paint().apply { textSize = 9f; typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD) }
    val np = Paint().apply { textSize = 7f; typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL) }
    var y = 22f; c.drawText("STOP WORK ORDER", 297f, y, tp); y += 14; c.drawText("EFFECTIVE IMMEDIATELY", 297f, y, sp); y += 26
    fun line(l: String, v: String) { c.drawText("$l $v", 40f, y, hp); y += 12 }
    line("ORDER NO:", "SWO/${swo.id.take(6)}/${Calendar.getInstance().get(Calendar.YEAR)}")
    line("DATE:", SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(swo.timestamp)))
    line("PROJECT:", s.projectName); line("CONTRACTOR:", s.contractorName); line("LOCATION:", swo.location)
    y += 4; c.drawLine(40f, y, 555f, y, Paint().apply { color = android.graphics.Color.RED; strokeWidth = 2f }); y += 10
    c.drawText("REASON: $reason", 40f, y, np); y += 22
    c.drawText("CONDITIONS FOR RESUMPTION: $conditions", 40f, y, np); y += 14
    c.drawText("ISSUED BY: $cow (CoW)     RECEIVED: ________________ (Contractor)", 40f, y + 12, np)
    c.drawText("Date: ________________     Date: ________________", 40f, y + 24, np)
    pdf.finishPage(page)
    val f = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SWO-${swo.id.take(6)}.pdf")
    pdf.writeTo(FileOutputStream(f)); pdf.close()
}
