package com.cowlog.pro.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun BottomNavBar(navController: NavController, currentRoute: String) {
    NavigationBar(
        containerColor = Color(0xE6000000),
        contentColor = Color.White,
        tonalElevation = 0.dp
    ) {
        val items = listOf(
            "diary" to "📔",
            "inspections" to "✅",
            "ncr" to "🚨",
            "si" to "📋",
            "report" to "📄"
        )
        items.forEach { (route, icon) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = { if (currentRoute != route) navController.navigate(route) { popUpTo("diary") } },
                icon = { Text(icon, fontSize = 18.sp) },
                label = { Text(route.replaceFirstChar { it.uppercase() }, fontSize = 9.sp, fontWeight = FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF0A84FF),
                    selectedTextColor = Color(0xFF0A84FF),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color(0x200A84FF)
                )
            )
        }
    }
}

@Composable
fun TopBar(title: String, navController: NavController, showMore: Boolean = true) {
    var showMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF78166))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (showMore) {
                Box {
                    TextButton(onClick = { showMenu = true }) { Text("···", fontSize = 20.sp, color = Color.Gray) }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("🚜 Plant & Equipment") }, onClick = { navController.navigate("plant"); showMenu = false })
                        DropdownMenuItem(text = { Text("🧱 Materials") }, onClick = { navController.navigate("materials"); showMenu = false })
                        DropdownMenuItem(text = { Text("🧪 Concrete Cubes") }, onClick = { navController.navigate("concrete"); showMenu = false })
                        DropdownMenuItem(text = { Text("👷 Attendance") }, onClick = { navController.navigate("attendance"); showMenu = false })
                        DropdownMenuItem(text = { Text("📤 RFIs") }, onClick = { navController.navigate("rfi"); showMenu = false })
                        DropdownMenuItem(text = { Text("📐 Drawings") }, onClick = { navController.navigate("drawings"); showMenu = false })
                        DropdownMenuItem(text = { Text("🛑 Stop Work Orders") }, onClick = { navController.navigate("stopwork"); showMenu = false })
                        DropdownMenuItem(text = { Text("🚧 Delay Notices") }, onClick = { navController.navigate("delaynotices"); showMenu = false })
                        DropdownMenuItem(text = { Text("📦 Rejected Materials") }, onClick = { navController.navigate("rejections"); showMenu = false })
                        DropdownMenuItem(text = { Text("📝 Meeting Minutes") }, onClick = { navController.navigate("meetings"); showMenu = false })
                    }
                }
            }
            TextButton(onClick = { navController.navigate("settings") }) { Text("⚙️", fontSize = 18.sp) }
        }
    }
}
