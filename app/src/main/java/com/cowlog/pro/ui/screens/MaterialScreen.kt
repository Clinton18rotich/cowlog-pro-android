package com.cowlog.pro.ui.screens

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
import com.cowlog.pro.ui.components.DateFilterBar
import java.text.SimpleDateFormat
import java.util.*

val MATERIAL_CATALOG = linkedMapOf(
    "🧱 CEMENT & BINDERS" to listOf("Cement — OPC 42.5N (Blue Triangle)", "Cement — OPC 42.5N (Bamburi)", "Cement — OPC 42.5N (Simba)", "Cement — PPC 32.5N", "Cement — White Cement", "Lime — Hydrated", "Gypsum"),
    "🔩 REINFORCEMENT STEEL" to listOf("Y8 (8mm)", "Y10 (10mm)", "Y12 (12mm)", "Y16 (16mm)", "Y20 (20mm)", "Y25 (25mm)", "Y32 (32mm)", "BRC A142", "BRC A193", "BRC A252", "Binding Wire 1.6mm", "Hoop Iron 25mm x 1.6mm", "Cover Blocks 20mm", "Cover Blocks 25mm", "Cover Blocks 40mm", "Cover Blocks 50mm"),
    "🧱 CONCRETE BLOCKS" to listOf("Solid Block 4\"", "Solid Block 6\"", "Solid Block 8\"", "Solid Block 9\"", "Hollow Block 4\"", "Hollow Block 6\"", "Hollow Block 8\"", "Interlocking Block 6\"", "Interlocking Block 8\""),
    "🪨 AGGREGATES & SAND" to listOf("River Sand (Fine)", "River Sand (Coarse)", "Plaster Sand", "M-Sand (Crushed)", "Ballast 14mm (½\")", "Ballast 20mm (¾\")", "Ballast 40mm (1½\")", "Hardcore 50-75mm", "Hardcore 100-150mm", "Murram — Red", "Murram — Brown", "Quarry Dust"),
    "🪵 TIMBER & BOARDS" to listOf("Timber 4x2 (100x50mm)", "Timber 6x2 (150x50mm)", "Timber 3x2 (75x50mm)", "Timber 2x2 (50x50mm)", "Plywood 9mm", "Plywood 12mm", "Plywood 18mm (Shuttering)", "Plywood 25mm", "MDF 12mm", "MDF 18mm", "Blockboard 19mm"),
    "🏠 ROOFING" to listOf("Iron Sheets G30 2.5m", "Iron Sheets G30 3.0m", "Iron Sheets G30 3.5m", "Iron Sheets G28 2.5m", "Iron Sheets G28 3.0m", "Ridge Caps — Iron", "Ridge Caps — Aluminium", "Roofing Nails 75mm", "Roofing Nails 100mm", "Washers — Rubber", "Gutters UPVC 100mm", "Gutters UPVC 150mm", "Downpipes UPVC 100mm"),
    "💧 WATERPROOFING & DPM" to listOf("DPM 500 Gauge", "DPM 1000 Gauge", "DPM 1200 Gauge", "Torch-on Membrane", "Self-adhesive Membrane", "Liquid Waterproofing", "Bituminous Paint", "Primer — Bituminous"),
    "🚿 PLUMBING" to listOf("HDPE Pipe 25mm", "HDPE Pipe 32mm", "HDPE Pipe 50mm", "HDPE Pipe 63mm", "HDPE Pipe 110mm", "PPR Pipe 20mm", "PPR Pipe 25mm", "PPR Pipe 32mm", "PVC Pipe 50mm (Drainage)", "PVC Pipe 110mm (Soil)", "GI Pipe 15mm (½\")", "GI Pipe 20mm (¾\")", "GI Pipe 25mm (1\")", "WC — Floor Mounted", "WC — Wall Hung", "Basin — Pedestal", "Sink — Single Bowl", "Taps — Pillar", "Stop Cock 25mm", "Water Tank 500L", "Water Tank 1000L", "Water Tank 5000L"),
    "⚡ ELECTRICAL" to listOf("Cable 1.5mm²", "Cable 2.5mm²", "Cable 4mm²", "Cable 6mm²", "Cable 10mm²", "Cable 16mm²", "Conduit PVC 20mm", "Conduit PVC 25mm", "Trunking PVC 25x16mm", "DB 4-Way SPN", "DB 8-Way SPN", "DB 12-Way SPN", "MCB 6A", "MCB 10A", "MCB 16A", "MCB 20A", "MCB 32A", "RCD 40A 30mA", "Switch 1-Gang", "Switch 2-Gang", "Socket 13A Switched", "Socket 13A Double", "LED Light 10W", "LED Light 18W", "Flood Light 100W"),
    "🎨 PAINT & FINISHES" to listOf("Undercoat White", "Vinyl Emulsion White", "Vinyl Emulsion (Tinted)", "Gloss White", "Gloss (Tinted)", "Primer — Wood", "Primer — Metal", "Putty — Wall Filler", "Sandpaper 120 Grit"),
    "🧱 TILES" to listOf("Floor Tile 400x400mm", "Floor Tile 500x500mm", "Floor Tile 600x600mm", "Wall Tile 200x300mm", "Wall Tile 250x400mm", "Wall Tile 300x600mm", "Tile Adhesive Standard", "Tile Adhesive Flexible", "Grout White", "Grout Grey"),
    "🚪 DOORS & WINDOWS" to listOf("Flush Door 2100x900mm", "Panel Door 2100x900mm", "Fire Door 2100x900mm", "Door Frame — Timber", "Door Frame — Metal", "Aluminium Sliding Window 1200x1200mm", "Aluminium Casement Window 1200x1200mm", "Glass Clear 4mm", "Glass Clear 6mm", "Glass Tinted 5mm", "Glass Frosted 5mm"),
    "🏡 EXTERNAL WORKS" to listOf("Pavers 200x100x60mm", "Pavers 200x100x80mm", "Cabro 200x100x60mm", "Kerbs — Mountable", "Kerbs — Barrier", "Asphalt AC 10", "Asphalt AC 20", "Prime Coat MC-30"),
    "📦 MISCELLANEOUS" to listOf("Nails 40mm", "Nails 75mm", "Nails 100mm", "Nails 150mm", "Screws 25mm", "Screws 50mm", "Screws 75mm", "Form Release Agent", "Curing Compound", "Concrete Admixture", "Sealant — Silicone Clear", "Sealant — Silicone White", "Fire Extinguisher CO2", "Fire Extinguisher Dry Powder", "First Aid Kit", "Warning Tape", "Safety Signage", "Barricade Tape")
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MaterialScreen(appData: AppData, settings: ProjectSettings, navController: NavController, onUpdate: (AppData) -> Unit) {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var filterDate by remember { mutableStateOf(today) }
    val displayLogs = appData.materialLogs.filter { it.date == filterDate.take(10) }
    val todayDeliveries = displayLogs.filter { it.type == "delivery" }
    val todayUsed = displayLogs.filter { it.type == "usage" }
    val lowStock = appData.materials.filter { it.currentStock <= it.minStock && it.minStock > 0 }

    var mode by remember { mutableStateOf("list") }
    var showLogDialog by remember { mutableStateOf("") }

    Scaffold(
        
        
        floatingActionButton = { FloatingActionButton(onClick = { mode = "add" }, containerColor = Color(0xFFFF9F0A)) { Text("+", color = Color.Black) } }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 12.dp)) {
            DateFilterBar(onToday = { filterDate = today }, onAll = { filterDate = "" })
            if (filterDate.isNotEmpty()) Text("${displayLogs.size} logs on ${filterDate.take(10)}", fontSize = 10.sp, color = Color(0xFF0A84FF), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(onClick = { showLogDialog = "delivery" }, modifier = Modifier.weight(1f).height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158))) { Text("🚚 Delivery", fontSize = 11.sp) }
                Button(onClick = { showLogDialog = "usage" }, modifier = Modifier.weight(1f).height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9F0A))) { Text("📝 Use", fontSize = 11.sp, color = Color.Black) }
            }

            if (lowStock.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0x33FF453A))) {
                    Column(modifier = Modifier.padding(10.dp)) { Text("⚠️ Low Stock", fontWeight = FontWeight.Bold, color = Color(0xFFFF453A), fontSize = 12.sp); lowStock.forEach { m -> Text("${m.name}: ${m.currentStock} ${m.unit}", fontSize = 10.sp, color = Color(0xFFFF453A)) } }
                }
            }

            if (appData.materials.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("🧱", fontSize = 48.sp); Text("No materials", color = Color.Gray); TextButton(onClick = { mode = "add" }) { Text("+ Add Material") } }
                }
            } else {
                LazyColumn {
                    items(appData.materials.sortedBy { it.name }) { m ->
                        val sc = when { m.currentStock <= 0 -> Color(0xFFFF453A); m.currentStock <= m.minStock -> Color(0xFFFF9F0A); else -> Color(0xFF30D158) }
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) { Text(m.name, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text(m.unit, fontSize = 9.sp, color = Color.Gray) }
                                Text("${m.currentStock}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = sc)
                            TextButton(onClick = { val newData = appData.copy(); newData.materials.removeAll { it.id == m.id }; onUpdate(newData) }) { Text("🗑️", fontSize = 9.sp, color = Color(0xFFFF453A)) }
                            }
                        }
                    }
                    val allLogs = todayDeliveries + todayUsed
                    if (allLogs.isNotEmpty()) { item { Spacer(modifier = Modifier.height(8.dp)); Text("Logs for ${filterDate.take(10)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0A84FF)) } }
                    items(allLogs.sortedByDescending { it.timestamp }) { log ->
                        val mat = appData.materials.find { it.id == log.materialId }
                        Text(if (log.type == "delivery") "🚚 +${log.quantity} ${mat?.name ?: ""}" else "📝 -${log.quantity} ${mat?.name ?: ""}", fontSize = 11.sp, color = if (log.type == "delivery") Color(0xFF30D158) else Color(0xFFFF9F0A), modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
        }
    }

    // Add Material Dialog
    if (mode == "add") {
        var name by remember { mutableStateOf("") }; var unit by remember { mutableStateOf("Bags") }
        var stock by remember { mutableStateOf("0") }; var minStock by remember { mutableStateOf("0") }
        var selectedCat by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { mode = "list" }, title = { Text("Add Material") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (name.isEmpty()) { Text("Select from catalog:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9F0A))
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
                        MATERIAL_CATALOG.keys.forEach { cat -> TextButton(onClick = { selectedCat = cat }, modifier = Modifier.fillMaxWidth()) { Text(cat, fontSize = 10.sp, color = if (selectedCat == cat) Color(0xFFFF9F0A) else Color.Gray) } }
                        if (selectedCat.isNotEmpty()) { MATERIAL_CATALOG[selectedCat]?.forEach { item -> TextButton(onClick = { name = item; val autoUnit = when { item.contains("Cement") || item.contains("Lime") -> "Bags"; item.contains("Y") || item.contains("BRC") -> "Tonnes"; item.contains("Block") || item.contains("Tile") -> "Pieces"; item.contains("Sand") || item.contains("Ballast") || item.contains("Hardcore") || item.contains("Murram") -> "Tonnes"; item.contains("Timber") || item.contains("Plywood") -> "Pieces"; item.contains("DPM") || item.contains("Membrane") -> "Rolls"; item.contains("Pipe") || item.contains("Conduit") -> "Lengths"; item.contains("Cable") -> "Rolls"; item.contains("Paint") || item.contains("Primer") -> "Litres"; item.contains("Nail") || item.contains("Screw") -> "kg"; else -> "Bags" }; unit = autoUnit }, modifier = Modifier.fillMaxWidth()) { Text(item, fontSize = 10.sp, color = Color.White) } } }
                    }
                }
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Material Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Text("Unit:", fontSize = 11.sp, color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { listOf("Bags","Tonnes","m³","Pieces","Litres","m²","Rolls","kg","No.","Lengths").forEach { u -> FilterChip(selected = unit == u, onClick = { unit = u }, label = { Text(u, fontSize = 10.sp) }) } }
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Current Stock") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = minStock, onValueChange = { minStock = it }, label = { Text("Min Stock Level") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }},
            confirmButton = { Button(onClick = { if (name.isNotBlank()) { val n = appData.copy(); n.materials.add(Material(id = UUID.randomUUID().toString(), name = name, unit = unit, currentStock = stock.toDoubleOrNull() ?: 0.0, minStock = minStock.toDoubleOrNull() ?: 0.0)); onUpdate(n); mode = "list" } }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9F0A))) { Text("Save", color = Color.Black) } },
            dismissButton = { Button(onClick = { mode = "list" }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("Cancel") } }
        )
    }

    // Delivery/Usage Dialog
    if (showLogDialog.isNotEmpty()) {
        val isDelivery = showLogDialog == "delivery"
        var matId by remember { mutableStateOf(appData.materials.firstOrNull()?.id ?: "") }
        var qty by remember { mutableStateOf("1") }
        var supplier by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { showLogDialog = "" }, title = { Text(if (isDelivery) "🚚 Log Delivery" else "📝 Log Usage") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (appData.materials.isNotEmpty()) {
                    Text("Material:", fontSize = 11.sp, color = Color.Gray)
                    appData.materials.take(10).forEach { m -> FilterChip(selected = matId == m.id, onClick = { matId = m.id }, label = { Text(m.name, fontSize = 10.sp) }) }
                }
                OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if (isDelivery) OutlinedTextField(value = supplier, onValueChange = { supplier = it }, label = { Text("Supplier") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }},
            confirmButton = { Button(onClick = { if (matId.isNotEmpty() && qty.isNotBlank()) { val n = appData.copy(); val mat = n.materials.find { it.id == matId }; if (mat != null) { val q = qty.toDoubleOrNull() ?: 0.0; n.materialLogs.add(MaterialLog(id = UUID.randomUUID().toString(), materialId = matId, date = filterDate.ifEmpty { today }.take(10), type = if (isDelivery) "delivery" else "usage", quantity = qty.toDoubleOrNull() ?: 0.0, unit = mat.unit, supplier = supplier, deliveryNote = note, timestamp = System.currentTimeMillis())); val idx = n.materials.indexOf(mat); n.materials[idx] = mat.copy(currentStock = if (isDelivery) mat.currentStock + q else (mat.currentStock - q).coerceAtLeast(0.0)); onUpdate(n) } }; showLogDialog = "" }, colors = ButtonDefaults.buttonColors(containerColor = if (isDelivery) Color(0xFF30D158) else Color(0xFFFF9F0A))) { Text("Save") } },
            dismissButton = { Button(onClick = { showLogDialog = "" }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("Cancel") } }
        )
    }
}
