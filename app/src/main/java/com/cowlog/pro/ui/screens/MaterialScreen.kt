package com.cowlog.pro.ui.screens
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.cowlog.pro.ui.components.DateFilterBar
import java.text.SimpleDateFormat
import java.util.*

val MATERIAL_CATALOG = linkedMapOf(
    "🧱 Cement & Binders" to listOf("Cement - OPC 42.5N", "Cement - PPC 32.5N", "White Cement", "Lime - Hydrated", "Gypsum"),
    "🔩 Reinforcement Steel" to listOf("Y8 (8mm)", "Y10 (10mm)", "Y12 (12mm)", "Y16 (16mm)", "Y20 (20mm)", "Y25 (25mm)", "BRC A142", "BRC A193", "Binding Wire 1.6mm", "Hoop Iron 25mm x 1.6mm"),
    "🧱 Concrete Blocks" to listOf("Solid Block 4\"", "Solid Block 6\"", "Solid Block 8\"", "Hollow Block 4\"", "Hollow Block 6\"", "Hollow Block 8\"", "Interlocking Block 6\""),
    "🪨 Aggregates & Sand" to listOf("River Sand (Fine)", "River Sand (Coarse)", "Plaster Sand", "M-Sand (Crushed)", "Ballast 14mm", "Ballast 20mm", "Hardcore 50-75mm", "Murram - Red", "Quarry Dust"),
    "🪵 Timber & Boards" to listOf("Timber 4x2 (100x50mm)", "Timber 6x2 (150x50mm)", "Plywood 9mm", "Plywood 12mm", "Plywood 18mm (Shuttering)", "Plywood 25mm"),
    "🏠 Roofing Materials" to listOf("Iron Sheets G30 2.5m", "Iron Sheets G30 3.0m", "Iron Sheets G28 2.5m", "Ridge Caps - Iron", "Roofing Nails 75mm", "Gutters UPVC 100mm", "Downpipes UPVC 100mm"),
    "💧 Waterproofing & DPM" to listOf("DPM 500 Gauge", "DPM 1000 Gauge", "DPM 1200 Gauge", "Torch-on Membrane", "Liquid Waterproofing", "Bituminous Paint"),
    "🚿 Plumbing Materials" to listOf("HDPE Pipe 25mm", "HDPE Pipe 50mm", "HDPE Pipe 110mm", "PPR Pipe 20mm", "PPR Pipe 25mm", "PVC Pipe 50mm (Drainage)", "PVC Pipe 110mm (Soil)", "WC - Floor Mounted", "Basin - Pedestal", "Water Tank 500L", "Water Tank 1000L"),
    "⚡ Electrical Materials" to listOf("Cable 1.5mm²", "Cable 2.5mm²", "Cable 4mm²", "Cable 6mm²", "Conduit PVC 20mm", "Conduit PVC 25mm", "DB 4-Way SPN", "DB 8-Way SPN", "MCB 10A", "MCB 16A", "MCB 20A", "RCD 40A 30mA", "Switch 1-Gang", "Socket 13A Switched", "LED Light 10W", "LED Light 18W"),
    "🎨 Paint & Finishes" to listOf("Undercoat White", "Vinyl Emulsion White", "Gloss White", "Primer - Wood", "Putty - Wall Filler", "Sandpaper 120 Grit"),
    "🧱 Tiles & Adhesives" to listOf("Floor Tile 400x400mm", "Floor Tile 500x500mm", "Wall Tile 200x300mm", "Wall Tile 300x600mm", "Tile Adhesive Standard", "Tile Adhesive Flexible", "Grout White", "Grout Grey"),
    "🚪 Doors & Windows" to listOf("Flush Door 2100x900mm", "Panel Door 2100x900mm", "Fire Door 2100x900mm", "Aluminium Sliding Window 1200x1200mm", "Glass Clear 4mm", "Glass Clear 6mm", "Glass Tinted 5mm"),
    "🏡 External Works" to listOf("Pavers 200x100x60mm", "Pavers 200x100x80mm", "Cabro 200x100x60mm", "Kerbs - Mountable", "Kerbs - Barrier", "Asphalt AC 10", "Asphalt AC 20"),
    "📦 Miscellaneous" to listOf("Nails 40mm", "Nails 75mm", "Nails 100mm", "Screws 25mm", "Screws 50mm", "Form Release Agent", "Curing Compound", "Sealant - Silicone Clear", "Fire Extinguisher CO2", "First Aid Kit", "Warning Tape", "Safety Signage"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialScreen(appData: AppData, settings: ProjectSettings, navController: NavController, onUpdate: (AppData) -> Unit) {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var filterDate by remember { mutableStateOf(today) }
    val displayLogs = appData.materialLogs.filter { it.date == filterDate.take(10) }
    val lowStock = appData.materials.filter { it.currentStock <= it.minStock && it.minStock > 0 }
    var showAdd by remember { mutableStateOf(false) }
    var showDelivery by remember { mutableStateOf(false) }
    var showUsage by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 12.dp)) {
            DateFilterBar(onToday = { filterDate = today }, onAll = { filterDate = "" })
            if (filterDate.isNotEmpty()) Text("${displayLogs.size} transactions on ${filterDate.take(10)}", fontSize = 12.sp, color = Color(0xFF0A84FF), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))

            // Action buttons row
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(onClick = { showAdd = true }, modifier = Modifier.weight(1f).height(44.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9F0A))) { Text("➕ Add Material", fontSize = 12.sp, color = Color.Black) }
                Button(onClick = { showDelivery = true }, modifier = Modifier.weight(1f).height(44.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158))) { Text("🚚 Delivery", fontSize = 12.sp) }
                Button(onClick = { showUsage = true }, modifier = Modifier.weight(1f).height(44.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A))) { Text("📝 Usage", fontSize = 12.sp) }
            }

            // Low stock warning
            if (lowStock.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0x33FF453A))) {
                    Column(modifier = Modifier.padding(10.dp)) { Text("⚠️ Low Stock", fontWeight = FontWeight.Bold, color = Color(0xFFFF453A), fontSize = 13.sp); lowStock.forEach { m -> Text("${m.name}: ${m.currentStock} ${m.unit}", fontSize = 11.sp, color = Color(0xFFFF453A)) } }
                }
            }

            // Material inventory list
            if (appData.materials.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("🧱", fontSize = 48.sp); Text("No materials registered", color = Color.Gray, fontSize = 14.sp); TextButton(onClick = { showAdd = true }) { Text("+ Add First Material", fontSize = 13.sp) } }
                }
            } else {
                LazyColumn {
                    items(appData.materials.sortedBy { it.name }) { m ->
                        val sc = when { m.currentStock <= 0 -> Color(0xFFFF453A); m.currentStock <= m.minStock -> Color(0xFFFF9F0A); else -> Color(0xFF30D158) }
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) { Text(m.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White); Text("Min: ${m.minStock} ${m.unit}", fontSize = 10.sp, color = Color.Gray) }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${m.currentStock} ${m.unit}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = sc)
                                    TextButton(onClick = { val n = appData.copy(); n.materials.removeAll { it.id == m.id }; onUpdate(n) }) { Text("🗑️", fontSize = 12.sp, color = Color(0xFFFF453A)) }
                                }
                            }
                        }
                    }
                    // Daily transactions
                    if (displayLogs.isNotEmpty()) {
                        item { Spacer(modifier = Modifier.height(8.dp)); Text("Transactions on ${filterDate.take(10)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0A84FF)) }
                        items(displayLogs.sortedByDescending { it.timestamp }) { log ->
                            val mat = appData.materials.find { it.id == log.materialId }
                            Text(if (log.type == "delivery") "🚚 +${log.quantity} ${log.unit} ${mat?.name ?: ""}" else "📝 -${log.quantity} ${log.unit} ${mat?.name ?: ""}", fontSize = 12.sp, color = if (log.type == "delivery") Color(0xFF30D158) else Color(0xFFFF9F0A), modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }

    // Add Material Dialog
    if (showAdd) {
        var name by remember { mutableStateOf("") }; var unit by remember { mutableStateOf("Bags") }
        var stock by remember { mutableStateOf("0") }; var minStock by remember { mutableStateOf("10") }
        var selectedCat by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { showAdd = false }, title = { Text("Add Material", fontSize = 15.sp) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Select from catalog:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9F0A))
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).verticalScroll(rememberScrollState())) { MATERIAL_CATALOG.keys.forEach { cat -> TextButton(onClick = { selectedCat = cat }, modifier = Modifier.fillMaxWidth()) { Text(cat, fontSize = 11.sp, color = if (selectedCat == cat) Color(0xFFFF9F0A) else Color.Gray) } }
                }
                if (selectedCat.isNotEmpty()) { MATERIAL_CATALOG[selectedCat]?.forEach { item -> TextButton(onClick = { name = item; unit = when { item.contains("Cement") || item.contains("Lime") -> "Bags"; item.contains("Y") || item.contains("BRC") || item.contains("Steel") -> "Tonnes"; item.contains("Block") || item.contains("Tile") || item.contains("Brick") -> "Pieces"; item.contains("Sand") || item.contains("Ballast") || item.contains("Hardcore") || item.contains("Murram") -> "Tonnes"; item.contains("Timber") || item.contains("Plywood") -> "Pieces"; item.contains("DPM") || item.contains("Membrane") -> "Rolls"; item.contains("Pipe") || item.contains("Conduit") -> "Lengths"; item.contains("Cable") -> "Rolls"; item.contains("Paint") || item.contains("Primer") || item.contains("Sealant") -> "Litres"; item.contains("Nail") || item.contains("Screw") || item.contains("Wire") -> "kg"; item.contains("Water") -> "No."; else -> "No." }; }, modifier = Modifier.fillMaxWidth()) { Text(item, fontSize = 11.sp, color = Color.White) } } }
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Material Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Text("Unit:", fontSize = 12.sp, color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { listOf("Bags","Tonnes","m³","Pieces","Litres","m²","Rolls","kg","No.","Lengths","Pairs","Sets").forEach { u -> FilterChip(selected = unit == u, onClick = { unit = u }, label = { Text(u, fontSize = 10.sp) }) } }
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Current Stock") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = minStock, onValueChange = { minStock = it }, label = { Text("Min Stock Level (for alerts)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }},
            confirmButton = { Button(onClick = { if (name.isNotBlank()) { val n = appData.copy(); n.materials.add(Material(id = UUID.randomUUID().toString(), name = name, unit = unit, currentStock = stock.toDoubleOrNull() ?: 0.0, minStock = minStock.toDoubleOrNull() ?: 0.0)); onUpdate(n); showAdd = false } }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9F0A))) { Text("Save", color = Color.Black) } },
            dismissButton = { Button(onClick = { showAdd = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("Cancel") } }
        )
    }

    // Delivery Dialog
    if (showDelivery) {
        var matId by remember { mutableStateOf(appData.materials.firstOrNull()?.id ?: "") }
        var qty by remember { mutableStateOf("") }; var supplier by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { showDelivery = false }, title = { Text("🚚 Log Delivery", fontSize = 15.sp) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Select material that was delivered:", fontSize = 12.sp, color = Color.Gray)
                appData.materials.forEach { m -> FilterChip(selected = matId == m.id, onClick = { matId = m.id }, label = { Text("${m.name} (${m.unit})", fontSize = 10.sp) }) }
                OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity Delivered *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = supplier, onValueChange = { supplier = it }, label = { Text("Supplier (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }},
            confirmButton = { Button(onClick = { if (matId.isNotEmpty() && qty.isNotBlank()) { val n = appData.copy(); val mat = n.materials.find { it.id == matId }; if (mat != null) { val q = qty.toDoubleOrNull() ?: 0.0; n.materialLogs.add(MaterialLog(id = UUID.randomUUID().toString(), materialId = matId, date = filterDate.ifEmpty { today }.take(10), type = "delivery", quantity = qty.toDoubleOrNull() ?: 0.0, unit = mat.unit, supplier = supplier, timestamp = System.currentTimeMillis())); val idx = n.materials.indexOf(mat); n.materials[idx] = mat.copy(currentStock = mat.currentStock + q); onUpdate(n) } }; showDelivery = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158))) { Text("Save Delivery") } },
            dismissButton = { Button(onClick = { showDelivery = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("Cancel") } }
        )
    }

    // Usage Dialog
    if (showUsage) {
        var matId by remember { mutableStateOf(appData.materials.firstOrNull()?.id ?: "") }
        var qty by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { showUsage = false }, title = { Text("📝 Log Usage", fontSize = 15.sp) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Select material that was used:", fontSize = 12.sp, color = Color.Gray)
                appData.materials.forEach { m -> FilterChip(selected = matId == m.id, onClick = { matId = m.id }, label = { Text("${m.name} (${m.unit})", fontSize = 10.sp) }) }
                OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity Used *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }},
            confirmButton = { Button(onClick = { if (matId.isNotEmpty() && qty.isNotBlank()) { val n = appData.copy(); val mat = n.materials.find { it.id == matId }; if (mat != null) { val q = qty.toDoubleOrNull() ?: 0.0; n.materialLogs.add(MaterialLog(id = UUID.randomUUID().toString(), materialId = matId, date = filterDate.ifEmpty { today }.take(10), type = "usage", quantity = qty.toDoubleOrNull() ?: 0.0, unit = mat.unit, timestamp = System.currentTimeMillis())); val idx = n.materials.indexOf(mat); n.materials[idx] = mat.copy(currentStock = (mat.currentStock - q).coerceAtLeast(0.0)); onUpdate(n) } }; showUsage = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A))) { Text("Save Usage") } },
            dismissButton = { Button(onClick = { showUsage = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))) { Text("Cancel") } }
        )
    }
}
