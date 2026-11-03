package com.example.nhsentranet.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.ui.theme.WebCharcoalText
import com.example.nhsentranet.ui.theme.WebHeaderCyan
import com.example.nhsentranet.ui.theme.WebTealBrand

@Composable
fun NewtonHouseBrandHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp, topStart = 4.dp, topEnd = 4.dp),
            color = Color(0xFF129B9D),
            border = BorderStroke(2.dp, Color.Black)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text("NEWTON", fontSize = 6.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Icon(Icons.Default.School, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Text("SCHOOL", fontSize = 6.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = "Newton House",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "S T U D E N T   I N T R A N E T",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.2.sp
            )
        }
    }
}
