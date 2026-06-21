package com.cowlog.pro.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

// ─── Drawing Markup Data ───
data class MarkupLine(val points: List<Offset>, val color: Color, val label: String = "")

// ─── Quantity Takeoff Data ───
data class TakeoffItem(
    val id: String = UUID.randomUUID().toString(),
    val label: String = "",
    val unit: String = "m²",
    val length: Float = 0f,
    val width: Float = 0f,
    val quantity: Float = 0f
)

// ─── Spec Hyperlink ───
data class SpecLink(
    val id: String = UUID.randomUUID().toString(),
    val label: String = "",
    val clauseRef: String = "",
    val description: String = ""
)

@Composable
fun DrawingScreen(
    appData: AppData,
    settings: ProjectSettings,
    navController: NavController,
    onUpdate: (AppData) -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }
    var viewingDrawing by remember { mutableStateOf<Drawing?>(null) }
    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var currentPage by remember { mutableStateOf(0) }
    var pageCount by remember { mutableStateOf(0) }
    var pageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Viewer state
    var scale by remember { mutableStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Markup
    var isMarkupMode by remember { mutableStateOf(false) }
    var markupColor by remember { mutableStateOf(Color.Red) }
    var markupLines by remember { mutableStateOf<List<MarkupLine>>(emptyList()) }
    var currentLine by remember { mutableStateOf<MarkupLine?>(null) }

    // AI
    var isAnalyzing by remember { mutableStateOf(false) }
    var aiResults by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Takeoff
    var isTakeoffMode by remember { mutableStateOf(false) }
    var takeoffStart by remember { mutableStateOf<Offset?>(null) }
    var takeoffEnd by remember { mutableStateOf<Offset?>(null) }
    var takeoffItems by remember { mutableStateOf<List<TakeoffItem>>(emptyList()) }

    // Spec Links
    var showSpecDialog by remember { mutableStateOf(false) }
    var specLinks by remember { mutableStateOf<List<SpecLink>>(emptyList()) }

    // ── File Picker ──
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val input = context.contentResolver.openInputStream(it)
            val fName = "drw_${Date().time}.pdf"
            val outFile = File(context.filesDir, fName)
            input?.use { i -> outFile.outputStream().use { o -> i.copyTo(o) } }
            val d = Drawing(id = UUID.randomUUID().toString(), number = fName, title = it.lastPathSegment ?: "Drawing", dateReceived = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()), imageData = outFile.absolutePath, timestamp = System.currentTimeMillis())
            val nd = appData.copy(); nd.drawings.add(d); onUpdate(nd)
        }
    }

    // ── MAIN LIST ──
    if (viewingDrawing == null) {
        Scaffold(
            topBar = { TopBar("📐 Drawings & Plans", navController) },
            bottomBar = { BottomNavBar(navController, "drawings") },
            floatingActionButton = { FloatingActionButton(onClick = { showAdd = true }, containerColor = Color(0xFF0A84FF)) { Text("+", color = Color.White) } }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(12.dp)) {
                Button(onClick = { filePicker.launch("application/pdf") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))) { Text("📂 Import PDF Drawing") }
                Spacer(modifier = Modifier.height(8.dp))
                if (appData.drawings.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("📐", fontSize = 48.sp); Text("No drawings", color = Color.Gray) }
                    }
                } else {
                    LazyColumn {
                        items(appData.drawings.sortedByDescending { it.timestamp }) { dwg ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(dwg.title.take(50), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Rev: ${dwg.revision} | ${dwg.dateReceived}", fontSize = 10.sp, color = Color.Gray)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                                        Button(onClick = {
                                            viewingDrawing = dwg
                                            val f = File(dwg.imageData)
                                            if (f.exists() && f.extension == "pdf") {
                                                try { val fd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY); val r = PdfRenderer(fd); pdfRenderer = r; pageCount = r.pageCount; currentPage = 0; renderPDFPage(r, 0)?.let { pageBitmap = it } } catch (_: Exception) {}
                                            }
                                        }, modifier = Modifier.height(32.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))) { Text("📖 View") }
                                        Button(onClick = { val nd = appData.copy(); nd.drawings.removeAll { it.id == dwg.id }; onUpdate(nd) }, modifier = Modifier.height(32.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("🗑️") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // Add dialog
        if (showAdd) {
            var num by remember { mutableStateOf("") }; var tit by remember { mutableStateOf("") }; var rev by remember { mutableStateOf("A") }
            AlertDialog(onDismissRequest = { showAdd = false }, title = { Text("Add Drawing") },
                text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(value = num, onValueChange = { num = it }, label = { Text("Number *") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(value = tit, onValueChange = { tit = it }, label = { Text("Title *") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(value = rev, onValueChange = { rev = it }, label = { Text("Revision") }, modifier = Modifier.fillMaxWidth()) } },
                confirmButton = { Button(onClick = { if (num.isNotBlank() && tit.isNotBlank()) { val d = Drawing(id = UUID.randomUUID().toString(), number = num, title = tit, revision = rev, dateReceived = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()), timestamp = System.currentTimeMillis()); val nd = appData.copy(); nd.drawings.add(d); onUpdate(nd); showAdd = false } }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))) { Text("Save") } },
                dismissButton = { Button(onClick = { showAdd = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("Cancel") } }
            )
        }
    }
    // ── VIEWER ──
    else {
        Scaffold(
            topBar = {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { viewingDrawing = null; pdfRenderer?.close(); pdfRenderer = null; pageBitmap = null; markupLines = emptyList(); aiResults = "" }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("←") }
                        Text(viewingDrawing?.title?.take(25) ?: "", fontSize = 13.sp, color = Color(0xFFF78166))
                        Text("Pg ${currentPage+1}/$pageCount", fontSize = 10.sp, color = Color.Gray)
                    }
                    // Toolbar
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Markup
                        Button(onClick = { isMarkupMode = !isMarkupMode; isTakeoffMode = false; currentLine = null }, modifier = Modifier.height(30.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isMarkupMode) Color(0xFFFF453A) else Color(0xFF2C2C2E))) { Text(if (isMarkupMode) "✍️ ON" else "✍️", fontSize = 9.sp) }
                        if (isMarkupMode) {
                            listOf(Color.Red, Color.Blue, Color(0xFF30D158), Color(0xFFFF9F0A), Color.White).forEach { c ->
                                Surface(onClick = { markupColor = c }, modifier = Modifier.size(22.dp), shape = MaterialTheme.shapes.small, color = c) {}
                            }
                            Button(onClick = { markupLines = emptyList(); currentLine = null }, modifier = Modifier.height(30.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("Clear", fontSize = 9.sp) }
                        }
                        // Takeoff
                        Button(onClick = { isTakeoffMode = !isTakeoffMode; isMarkupMode = false; takeoffStart = null; takeoffEnd = null }, modifier = Modifier.height(30.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isTakeoffMode) Color(0xFF30D158) else Color(0xFF2C2C2E))) { Text(if (isTakeoffMode) "📏 ON" else "📏", fontSize = 9.sp) }
                        // Spec Links
                        Button(onClick = { showSpecDialog = true }, modifier = Modifier.height(30.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBF5AF2))) { Text("🔗", fontSize = 9.sp) }
                        // AI
                        Button(onClick = {
                            if (settings.geminiKey.isNotEmpty()) { isAnalyzing = true; scope.launch { aiResults = analyzeDrawingAI(pageBitmap, settings.geminiKey) ?: "No results"; isAnalyzing = false } }
                            else Toast.makeText(context, "Set Gemini API key in Settings", Toast.LENGTH_SHORT).show()
                        }, modifier = Modifier.height(30.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBF5AF2))) { Text(if (isAnalyzing) "🤖..." else "🤖 AI", fontSize = 9.sp) }
                    }
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                // Viewer
                Box(modifier = Modifier.weight(1f).fillMaxWidth()
                    .pointerInput(Unit) { detectTransformGestures { _, pan, zoom, _ -> if (!isMarkupMode && !isTakeoffMode) { scale = (scale * zoom).coerceIn(0.5f, 5f); panOffset = Offset(panOffset.x + pan.x, panOffset.y + pan.y) } } }
                    .pointerInput(isMarkupMode, isTakeoffMode) {
                        awaitPointerEventScope {
                            while (true) {
                                val ev = awaitPointerEvent()
                                val pos = ev.changes.firstOrNull()?.position ?: continue
                                if (isMarkupMode && ev.changes.first().pressed) {
                                    if (currentLine == null) currentLine = MarkupLine(mutableListOf(pos), markupColor)
                                    else currentLine = currentLine!!.copy(points = currentLine!!.points + pos)
                                } else if (isMarkupMode && !ev.changes.first().pressed && currentLine != null) {
                                    markupLines = markupLines + currentLine!!; currentLine = null
                                }
                                if (isTakeoffMode && ev.changes.first().pressed) {
                                    if (takeoffStart == null) takeoffStart = pos
                                    else takeoffEnd = pos
                                }
                            }
                        }
                    }
                ) {
                    // PDF Page
                    if (pageBitmap != null) {
                        Image(bitmap = pageBitmap!!.asImageBitmap(), contentDescription = "page", modifier = Modifier.fillMaxSize().graphicsLayer(scaleX = scale, scaleY = scale, translationX = panOffset.x, translationY = panOffset.y))
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("📐 No preview", color = Color.Gray) }
                    }
                    // Markup overlay
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        markupLines.forEach { ml -> drawPath(ml.points.toPath(), ml.color, style = Stroke(width = 3f)) }
                        currentLine?.let { drawPath(it.points.toPath(), it.color, style = Stroke(width = 3f)) }
                        // Takeoff line
                        if (takeoffStart != null && takeoffEnd != null) {
                            drawLine(Color(0xFF30D158), takeoffStart!!, takeoffEnd!!, strokeWidth = 3f)
                            val dist = kotlin.math.sqrt((takeoffEnd!!.x - takeoffStart!!.x).pow(2) + (takeoffEnd!!.y - takeoffStart!!.y).pow(2))
                            drawContext.canvas.nativeCanvas.drawText("%.1f px".format(dist), takeoffEnd!!.x + 10, takeoffEnd!!.y - 10, android.graphics.Paint().apply { color = android.graphics.Color.GREEN; textSize = 30f })
                        }
                    }
                }

                // AI Results
                if (aiResults.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth().padding(8.dp).heightIn(max = 150.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E))) {
                        Column(modifier = Modifier.padding(10.dp).verticalScroll(rememberScrollState())) {
                            Text("🤖 AI Analysis", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFBF5AF2))
                            Text(aiResults, fontSize = 10.sp, color = Color.White)
                        }
                    }
                }

                // Takeoff panel
                if (isTakeoffMode && takeoffStart != null && takeoffEnd != null) {
                    val dist = kotlin.math.sqrt((takeoffEnd!!.x - takeoffStart!!.x).pow(2) + (takeoffEnd!!.y - takeoffStart!!.y).pow(2))
                    Card(modifier = Modifier.fillMaxWidth().padding(8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2E1A))) {
                        Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("📏 ${"%.1f".format(dist)} px", color = Color(0xFF30D158), fontWeight = FontWeight.Bold)
                            var label by remember { mutableStateOf("") }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label") }, modifier = Modifier.width(120.dp).height(50.dp), singleLine = true, textStyle = TextStyle(fontSize = 10.sp))
                                Button(onClick = { takeoffItems = takeoffItems + TakeoffItem(label = label, length = dist, quantity = dist); takeoffStart = null; takeoffEnd = null; Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show() }, modifier = Modifier.height(36.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158))) { Text("+") }
                            }
                        }
                    }
                }

                // Bottom nav
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { if (currentPage > 0) { currentPage--; pdfRenderer?.let { pageBitmap?.recycle(); renderPDFPage(it, currentPage)?.let { b -> pageBitmap = b } } } }, enabled = currentPage > 0) { Text("◀") }
                    Button(onClick = { scale = 1f; panOffset = Offset.Zero }) { Text("↺") }
                    Button(onClick = { if (currentPage < pageCount - 1) { currentPage++; pdfRenderer?.let { pageBitmap?.recycle(); renderPDFPage(it, currentPage)?.let { b -> pageBitmap = b } } } }, enabled = currentPage < pageCount - 1) { Text("▶") }
                }
            }
        }

        // Takeoff summary dialog
        if (takeoffItems.isNotEmpty() && !isTakeoffMode) {
            // Show small card
            Card(modifier = Modifier.fillMaxWidth().padding(8.dp).heightIn(max = 100.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2E1A))) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("📏 Takeoff (${takeoffItems.size} items)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF30D158))
                    LazyColumn { items(takeoffItems) { ti -> Text("${ti.label}: ${"%.1f".format(ti.length)} px", fontSize = 10.sp, color = Color.White) } }
                }
            }
        }

        // Spec Links dialog
        if (showSpecDialog) {
            var clause by remember { mutableStateOf("") }; var desc by remember { mutableStateOf("") }
            AlertDialog(onDismissRequest = { showSpecDialog = false }, title = { Text("🔗 Add Spec Link") },
                text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = clause, onValueChange = { clause = it }, label = { Text("Clause Ref (e.g., 5.2.3)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)
                    if (specLinks.isNotEmpty()) { Text("Existing links:", fontSize = 10.sp, color = Color.Gray); specLinks.forEach { s -> Text("🔗 ${s.clauseRef}: ${s.description}", fontSize = 9.sp, color = Color(0xFFBF5AF2)) } }
                }},
                confirmButton = { Button(onClick = { if (clause.isNotBlank()) { specLinks = specLinks + SpecLink(clauseRef = clause, description = desc); showSpecDialog = false } }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBF5AF2))) { Text("Add") } },
                dismissButton = { Button(onClick = { showSpecDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("Close") } }
            )
        }
    }
}

// ─── Helpers ───
fun renderPDFPage(renderer: PdfRenderer, pageIndex: Int): Bitmap? {
    return try { val p = renderer.openPage(pageIndex); val b = Bitmap.createBitmap(p.width, p.height, Bitmap.Config.ARGB_8888); b.eraseColor(android.graphics.Color.WHITE); p.render(b, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY); p.close(); b } catch (_: Exception) { null }
}

fun List<Offset>.toPath(): androidx.compose.ui.graphics.Path {
    val p = androidx.compose.ui.graphics.Path()
    if (isNotEmpty()) { p.moveTo(first().x, first().y); forEach { p.lineTo(it.x, it.y) } }
    return p
}

suspend fun analyzeDrawingAI(bitmap: Bitmap?, apiKey: String): String? {
    if (bitmap == null) return null
    return withContext(Dispatchers.IO) {
        try {
            val stream = java.io.ByteArrayOutputStream(); bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream)
            val b64 = android.util.Base64.encodeToString(stream.toByteArray(), android.util.Base64.NO_WRAP)
            val body = JSONObject().apply { put("contents", JSONArray().put(JSONObject().apply { put("parts", JSONArray().put(JSONObject().apply { put("text", "Extract from this construction drawing: 1) Drawing type 2) Scale 3) All dimensions 4) Structural elements with sizes 5) Grid lines 6) Materials specified. Return as bullet points.") }).put(JSONObject().apply { put("inlineData", JSONObject().apply { put("mimeType", "image/jpeg"); put("data", b64) }) })) })) }
            val conn = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey").openConnection() as HttpURLConnection
            conn.requestMethod = "POST"; conn.setRequestProperty("Content-Type", "application/json"); conn.doOutput = true
            conn.outputStream.write(body.toString().toByteArray())
            val resp = conn.inputStream.bufferedReader().readText()
            JSONObject(resp).getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
        } catch (e: Exception) { "Error: ${e.message}" }
    }
}

private fun Float.pow(n: Int): Float { var r = 1f; repeat(n) { r *= this }; return r }
