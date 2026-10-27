package com.example.nhsentranet.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.ui.theme.NewtonTealPrimary

@Composable
fun DemoRow(label: String, user: String, pass: String, onSelect: (String, String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(user, pass) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = NewtonTealPrimary,
            modifier = Modifier.width(65.dp)
        )
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, Color(0xFFCCE8ED))
        ) {
            Text(
                text = "$user / $pass",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = NewtonTealPrimary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
