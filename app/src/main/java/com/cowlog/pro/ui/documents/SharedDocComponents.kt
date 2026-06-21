package com.cowlog.pro.ui.documents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
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
fun KenDocField(label: String, value: String, valueColor: Color = Color.Black, onEdit: (String) -> Unit = {}) {
    Row(modifier = Modifier.padding(vertical = 1.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.width(80.dp))
        BasicTextField(
            value = value,
            onValueChange = onEdit,
            textStyle = TextStyle(fontSize = 7.sp, color = valueColor),
            modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFDE7), MaterialTheme.shapes.extraSmall).padding(horizontal = 4.dp, vertical = 2.dp),
            singleLine = true
        )
    }
}

@Composable
fun KenDocSection(title: String, content: String, onEdit: (String) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 5.dp)) {
        Text(title, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Divider(color = Color.Black, thickness = 0.3.dp)
        BasicTextField(
            value = content,
            onValueChange = onEdit,
            textStyle = TextStyle(fontSize = 7.sp, color = Color.Black),
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp).background(Color(0xFFFFFDE7), MaterialTheme.shapes.extraSmall).padding(4.dp),
            minLines = 2
        )
    }
}

@Composable
fun KenMeta(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = Color.Black, onEdit: (String) -> Unit = {}) {
    Column(modifier = modifier.padding(4.dp)) {
        Text(label, fontSize = 6.sp, color = Color(0xFF888888), fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        BasicTextField(
            value = value,
            onValueChange = onEdit,
            textStyle = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Bold, color = valueColor),
            modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFDE7), MaterialTheme.shapes.extraSmall).padding(horizontal = 2.dp, vertical = 1.dp),
            singleLine = true
        )
    }
}

@Composable
fun KenSig(name: String, onEdit: (String) -> Unit, title: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Divider(color = Color(0xFF1A1A1A), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(4.dp))
        BasicTextField(
            value = name,
            onValueChange = onEdit,
            textStyle = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A), textAlign = TextAlign.Center),
            modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFDE7), MaterialTheme.shapes.extraSmall).padding(2.dp),
            singleLine = true
        )
        Text(title, fontSize = 7.sp, color = Color(0xFF888888))
        Text("Date: ................", fontSize = 6.sp, color = Color(0xFF888888))
    }
}
