package com.cowlog.pro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SignatureBlock(name: String, onEdit: (String) -> Unit, title: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Divider(color = Color(0xFF1A1A1A), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(4.dp))
        BasicTextField(
            value = name, onValueChange = onEdit,
            textStyle = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A), textAlign = TextAlign.Center),
            modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFDE7)).padding(2.dp),
            singleLine = true
        )
        Text(title, fontSize = 6.sp, color = Color(0xFF888888))
        Text("Date: ................", fontSize = 5.sp, color = Color(0xFF888888))
    }
}

@Composable
fun DateFilterBar(onToday: () -> Unit, onAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        androidx.compose.material3.Button(
            onClick = onToday,
            modifier = Modifier.height(32.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))
        ) {
            Text("Today", fontSize = 9.sp)
        }
        androidx.compose.material3.Button(
            onClick = onAll,
            modifier = Modifier.height(32.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))
        ) {
            Text("All", fontSize = 9.sp)
        }
    }
}
